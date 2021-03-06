/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Lachlan Dowding
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

package permafrost.tundra.server;

import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.Session;
import com.wm.app.b2b.server.StateManager;
import com.wm.app.b2b.server.User;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.time.DateTimeHelper;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;

/**
 * A collection of convenience methods for working with webMethods Integration Server sessions.
 */
public final class SessionHelper {
    /**
     * Disallow instantiation of this class.
     */
    private SessionHelper() {}

    /**
     * Converts the given session object to an IData representation.
     *
     * @param session The session to be converted.
     * @return The IData representation of the given session.
     */
    public static IData toIData(Session session) {
        if (session == null) return null;

        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();

        IDataUtil.put(cursor, "id", session.getSessionID());
        IDataUtil.put(cursor, "name", session.getName());
        IDataUtil.put(cursor, "birth", DateTimeHelper.emit(session.getStart()));
        IDataUtil.put(cursor, "age", DurationHelper.format(session.getAge(), DurationPattern.XML));
        IDataUtil.put(cursor, "timeout", DurationHelper.format(session.getTimeout(), DurationPattern.XML));
        IDataUtil.put(cursor, "user", session.getUser().getName());
        IDataUtil.put(cursor, "invocations", "" + session.getCalls());
        IDataUtil.put(cursor, "state", IDataHelper.normalize(session));

        cursor.destroy();

        return document;
    }

    /**
     * Creates a new session object which does not replace the current session, and can therefore be used in another
     * thread context.
     *
     * @param name  The name of the session.
     * @param user  The user the session is for.
     * @return      A new session.
     */
    public static Session create(String name, User user) {
        return create(name, user, Long.MAX_VALUE);
    }

    /**
     * Creates a new session object which does not replace the current session, and can therefore be used in another
     * thread context.
     *
     * @param name      The name of the session.
     * @param user      The user the session is for.
     * @param timeout   The timeout for the session.
     * @return          A new session.
     */
    public static Session create(String name, User user, long timeout) {
        Session currentSession = InvokeState.getCurrentSession();
        Session newSession = StateManager.createContext(timeout, name, user);
        // restore the current session that was overwritten by creating the above new session
        InvokeState.setCurrentSession(currentSession);

        return newSession;
    }

    /**
     * Returns the current session.
     *
     * @return The current session.
     */
    public static Session getCurrentSession() {
        Session currentSession = null;
        InvokeState currentState = InvokeState.getCurrentState();
        if (currentState != null) {
            currentSession = currentState.getSession();
        }
        return currentSession;
    }

    /**
     * Returns the current session in an IData document representation.
     *
     * @return The current session in an IData document representation.
     */
    public static IData getCurrentSessionAsIData() {
        return toIData(Service.getSession());
    }

    /**
     * Stores the given key and value in current session's state.
     *
     * @param key       The key to be stored in current session state.
     * @param value     The value to be associated with the given key.
     */
    public static void put(String key, Object value) {
        put(getCurrentSession(), key, value);
    }

    /**
     * Stores the given key and value in given session's state.
     *
     * @param session   The session whose state is to be updated.
     * @param key       The key to be stored in current session state.
     * @param value     The value to be associated with the given key.
     */
    public static void put(Session session, String key, Object value) {
        if (key != null) {
            if (session != null) {
                session.put(key, value);
            }
        }
    }

    /**
     * Returns the value associated with the given key from the current session's state.
     *
     * @param key       The key whose value is to be retrieved.
     * @return          The value associated with the given key, or null if the key does not exist.
     */
    public static Object get(String key) {
        return get(getCurrentSession(), key);
    }

    /**
     * Returns the value associated with the given key from the given session's state.
     *
     * @param session   The session whose state is to be updated.
     * @param key       The key whose value is to be retrieved.
     * @return          The value associated with the given key, or null if the key does not exist.
     */
    public static Object get(Session session, String key) {
        Object value = null;
        if (key != null) {
            if (session != null) {
                value = session.get(key);
            }
        }
        return value;
    }

    /**
     * Removes the element associated with the given key from current session's state.
     *
     * @param key       The key whose associated element is to be removed.
     * @return          The value associated with the removed element.
     */
    public static Object remove(String key) {
        return remove(getCurrentSession(), key);
    }

    /**
     * Removes the element associated with the given key from the given session's state.
     *
     * @param session   The session whose state is to be updated.
     * @param key       The key whose associated element is to be removed.
     * @return          The value associated with the removed element.
     */
    public static Object remove(Session session, String key) {
        Object value = null;
        if (key != null) {
            if (session != null) {
                value = session.remove(key);
            }
        }
        return value;
    }
}
