/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lachlan Dowding
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package permafrost.tundra.server.invoke;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.util.ServerException;
import com.wm.util.coder.IDataCodable;
import permafrost.tundra.data.IDataHTMLParser;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.lang.BooleanHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.time.DateTimeHelper;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An invocation chain processor which provides visibility into currently executing services.
 */
public class ServiceUsageProcessor extends AbstractInvokeChainProcessor implements IDataCodable {
    /**
     * The time this processor was started.
     */
    private long startTime = 0;
    /**
     * The total number of invocations this processor has tracked since starting.
     */
    private final AtomicLong totalInvocations = new AtomicLong(0);
    /**
     * The total number of invocation errors this processor has tracked since starting.
     */
    private final AtomicLong totalErrors = new AtomicLong(0);
    /**
     * The currently executing service invocations.
     */
    private final ConcurrentMap<Thread, Invocation> invocations = new ConcurrentHashMap<Thread, Invocation>();

    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final ServiceUsageProcessor INSTANCE = new ServiceUsageProcessor();
    }

    /**
     * Disallow instantiation of this class.
     */
    private ServiceUsageProcessor() {}

    /**
     * Returns the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static ServiceUsageProcessor getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Processes a service invocation.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    @Override
    public void process(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        try {
            // register this call in a try/catch so that any failures do not stop service invocation
            try {
                Invocation invocation = invocations.get(Thread.currentThread());
                if (invocation == null) {
                    invocation = new Invocation(Thread.currentThread());
                    invocations.put(Thread.currentThread(), invocation);
                }

                invocation.push(new Frame(baseService, pipeline, InvokeState.getCurrentState()));
                totalInvocations.incrementAndGet();
            } catch (Throwable ex) {
                // do nothing
            }

            super.process(iterator, baseService, pipeline, serviceStatus);
        } catch (Throwable exception) {
            totalErrors.incrementAndGet();

            // rethrow exception after logging
            if (exception instanceof RuntimeException) {
                throw (RuntimeException)exception;
            } else if (exception instanceof ServerException) {
                throw (ServerException)exception;
            } else {
                throw new ServerException(exception);
            }
        } finally {
            Invocation invocation = invocations.get(Thread.currentThread());
            if (invocation != null) {
                // invocation finished, so remove from call stack
                invocation.pop();

                if (invocation.size() == 0) {
                    // top-level invocation finished, so remove from currently executing threads
                    invocations.remove(Thread.currentThread());
                }
            }
        }
    }

    /**
     * Returns the current invocation context of the server as an IData document.
     *
     * @return The current invocation context of the server as an IData document.
     */
    @Override
    public IData getIData() {
        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        try {
            IDataUtil.put(cursor, "monitoring.started?", BooleanHelper.emit(started));
            if (started) {
                IDataUtil.put(cursor, "monitoring.start", DateTimeHelper.format(startTime));
                IDataUtil.put(cursor, "monitoring.duration", DurationHelper.format(System.currentTimeMillis() - startTime, DurationPattern.XML));
            }
            IDataUtil.put(cursor, "monitoring.datetime", DateTimeHelper.now("datetime"));
            IDataUtil.put(cursor, "invocations.started", totalInvocations.longValue());
            IDataUtil.put(cursor, "invocations.errored", totalErrors.longValue());

            // sort the list of invocations by start time
            SortedSet<Invocation> sortedInvocations = new TreeSet<Invocation>();
            for (Map.Entry<Thread, Invocation> entry : invocations.entrySet()) {
                Thread thread = entry.getKey();
                Invocation invocation = entry.getValue();
                if (invocation == null || invocation.size() == 0) {
                    // remove any entries that don't have any current invocations - this shouldn't happen, but for some
                    // reason Event Manager threads sometimes fall into this category
                    invocations.remove(thread, invocation);
                } else {
                    sortedInvocations.add(invocation);
                }
            }

            // convert the sorted list of invocations to a list of IData documents
            List<IData> currentInvocations = new ArrayList<IData>(sortedInvocations.size());
            for (Invocation invocation : sortedInvocations) {
                IData document = invocation.getIData();
                if (document != null) {
                    currentInvocations.add(document);
                }
            }

            IDataUtil.put(cursor, "invocations.current", currentInvocations.toArray(new IData[0]));
            IDataUtil.put(cursor, "invocations.current.length", currentInvocations.size());
        } finally {
            cursor.destroy();
        }

        return output;
    }

    /**
     * Sets values from the given IData. This method has not been implemented.
     *
     * @param input                             Not used.
     * @throws UnsupportedOperationException    This exception is always thrown.
     */
    @Override
    public void setIData(IData input) {
        throw new UnsupportedOperationException("setIData not implemented");
    }

    /**
     * Registers this class as an invocation handler and starts processing.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            startTime = System.currentTimeMillis();
            super.start();
        }
    }

    /**
     * Unregisters this class as an invocation handler and stops processing.
     */
    @Override
    public synchronized void stop() {
        if (started) {
            super.stop();

            startTime = 0;
            totalInvocations.set(0);
            totalErrors.set(0);
            invocations.clear();
        }
    }

    /**
     * Represents a single currently executing invocation of a service.
     */
    private static class Frame implements IDataCodable {
        /**
         * The time this service invocation started.
         */
        private final long startTime;
        /**
         * The service being invoked.
         */
        private final BaseService service;
        /**
         * The input pipeline to the service.
         */
        private final IData pipeline;
        /**
         * The length of the pipeline.
         */
        private final int pipelineLength;
        /**
         * The input pipeline lazily serialized as HTML.
         */
        private volatile String pipelineHTML;
        /**
         * The user and session invoking the service.
         */
        private final String user, session;
        /**
         * The parser used to emit the pipeline as HTML.
         */
        private static final IDataHTMLParser IDATA_HTML_PARSER = new IDataHTMLParser(true, true, 255, 10, 5, 3);

        /**
         * Constructs a new InvocationFrame.
         *
         * @param service   The service being executed.
         * @param pipeline  The input pipeline to the service.
         * @param state     The current invocation state.
         */
        public Frame(BaseService service, IData pipeline, InvokeState state) {
            this.service = service;
            this.pipeline = IDataHelper.clone(pipeline);
            this.pipelineLength = IDataHelper.size(this.pipeline);
            this.startTime = System.currentTimeMillis();
            this.session = state.getSession().getSessionID();
            this.user = state.getUser().getName();
        }

        /**
         * Returns an IData representation of this object.
         *
         * @return An IData representation of this object.
         */
        @Override
        public IData getIData() {
            IData output = IDataFactory.create();
            IDataCursor cursor = output.getCursor();

            try {
                // lazily initialize pipelineHTML by serializing the pipeline to HTML on first access
                if (pipelineHTML == null) {
                    pipelineHTML = IDATA_HTML_PARSER.emit(pipeline, String.class);
                }
                IDataUtil.put(cursor, "service", service.getNSName().getFullName());
                IDataUtil.put(cursor, "package", service.getPackageName());
                IDataUtil.put(cursor, "pipeline", pipeline);
                IDataUtil.put(cursor, "pipeline.length",pipelineLength);
                IDataUtil.put(cursor, "pipeline.html", pipelineHTML);
                IDataUtil.put(cursor, "start", DateTimeHelper.format(startTime));
                IDataUtil.put(cursor, "duration", DurationHelper.format(System.currentTimeMillis() - startTime, DurationPattern.XML));
                IDataUtil.put(cursor, "session", session);
                IDataUtil.put(cursor, "user", user);
            } catch (IOException ex) {
                ExceptionHelper.raiseUnchecked(ex);
            } catch (ServiceException ex) {
                ExceptionHelper.raiseUnchecked(ex);
            } finally {
                cursor.destroy();
            }

            return output;
        }

        /**
         * Sets values from the given IData. This method has not been implemented.
         *
         * @param input                             Not used.
         * @throws UnsupportedOperationException    This exception is always thrown.
         */
        @Override
        public void setIData(IData input) {
            throw new UnsupportedOperationException("setIData not implemented");
        }
    }

    /**
     * Represents a single currently executing service thread.
     */
    private static class Invocation implements IDataCodable, Comparable<Invocation> {
        /**
         * The call stack.
         */
        private final Deque<Frame> stack = new LinkedBlockingDeque<Frame>();
        /**
         * The thread currently executing.
         */
        private final Thread thread;
        /**
         * The time the current call stack started execution.
         */
        long startTime = System.currentTimeMillis();

        /**
         * Constructs a new InvocationThread.
         *
         * @param thread    The currently executing thread.
         */
        public Invocation(Thread thread) {
            this.thread = thread;
        }

        /**
         * Pushes a new invocation frame on this thread's call stack.
         *
         * @param frame The frame to be pushed onto the call stack.
         */
        public void push(Frame frame) {
            stack.push(frame);
        }

        /**
         * Pops the last pushed invocation frame from this thread's call stack.
         *
         * @return  The last frame pushed onto the call stack.
         */
        public Frame pop() {
            return stack.pop();
        }

        /**
         * Returns the number of frames on the call stack.
         *
         * @return The number of frames on the call stack.
         */
        public int size() {
            return stack.size();
        }

        /**
         * Returns an IData representation of this object.
         *
         * @return An IData representation of this object, or null if there are no current frames.
         */
        @Override
        public IData getIData() {
            IData output = null;

            List<IData> frames = new ArrayList<IData>(stack.size());
            for(Frame frame : stack) {
                frames.add(frame.getIData());
            }

            if (frames.size() > 0) {
                output = IDataFactory.create();
                IDataCursor cursor = output.getCursor();
                try {
                    IDataUtil.put(cursor, "thread.id", thread.getId());
                    IDataUtil.put(cursor, "thread.name", thread.getName());
                    IDataUtil.put(cursor, "thread.object", thread);
                    IDataUtil.put(cursor, "thread.start", DateTimeHelper.format(startTime));
                    IDataUtil.put(cursor, "thread.duration", DurationHelper.format(System.currentTimeMillis() - startTime, DurationPattern.XML));
                    IDataUtil.put(cursor, "callstack", frames.toArray(new IData[0]));
                    IDataUtil.put(cursor, "callstack.length", frames.size());
                } finally {
                    cursor.destroy();
                }
            }

            return output;
        }

        /**
         * Sets values from the given IData. This method has not been implemented.
         *
         * @param input                             Not used.
         * @throws UnsupportedOperationException    This exception is always thrown.
         */
        @Override
        public void setIData(IData input) {
            throw new UnsupportedOperationException("setIData not implemented");
        }

        /**
         * Compares this invocation with another based on start datetime.
         *
         * @param other The other invocation to compare with.
         * @return      The result of the comparison.
         */
        @Override
        public int compareTo(Invocation other) {
            int result;
            if (other == null) {
                result = 1;
            } else if (this.startTime < other.startTime) {
                result = -1;
            } else if (this.startTime > other.startTime) {
                result = 1;
            } else {
                result = 0;
            }
            return result;
        }
    }
}
