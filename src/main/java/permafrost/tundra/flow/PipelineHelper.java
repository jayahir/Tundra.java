/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Lachlan Dowding
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

package permafrost.tundra.flow;

import com.wm.app.b2b.server.InvokeState;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.lang.ns.NSField;
import com.wm.lang.ns.NSRecord;
import com.wm.lang.ns.NSService;
import com.wm.lang.ns.NSSignature;
import com.wm.lang.schema.Validator;
import com.wm.lang.xml.WMDocumentException;
import permafrost.tundra.content.ValidationHelper;
import permafrost.tundra.content.ValidationResult;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.server.ServiceHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenience services for working with the invoke pipeline.
 */
public class PipelineHelper {
    /**
     * Disallow instantiation.
     */
    private PipelineHelper() {}

    /**
     * Validates the given pipeline against the calling service's input or output signature.
     *
     * @param pipeline  The pipeline to be validated.
     * @param direction Whether to validate the input or output signature.
     * @return          Whether the validation succeeded or failed.
     */
    public static ValidationResult validate(IData pipeline, InputOutputSignature direction) {
        return validate(ServiceHelper.self(), pipeline, direction);
    }

    /**
     * Validates the given pipeline against the given service's input or output signature.
     *
     * @param service   The service whose signature is used to validate against.
     * @param pipeline  The pipeline to be validated.
     * @param direction Whether to validate the input or output signature.
     * @return          Whether the validation succeeded or failed.
     */
    public static ValidationResult validate(NSService service, IData pipeline, InputOutputSignature direction) {
        ValidationResult result = ValidationResult.VALID;

        // we can only validate the pipeline when not debugging
        if (service != null && !"wm.server.flow:stepFlow".equals(service.getNSName().getFullName())) {
            NSSignature signature = service.getSignature();
            if (signature != null) {
                NSRecord record;
                if (direction == InputOutputSignature.INPUT) {
                    record = signature.getInput();
                } else {
                    record = signature.getOutput();
                }

                if (record != null) {
                    Validator validator = Validator.create(pipeline, record, Validator.getDefaultOptions());
                    validator.setLocale(InvokeState.getCurrentLocale());
                    validator.setMaximumErrors(-1); // return all errors

                    IDataCursor cursor = null;
                    try {
                        IData scope = validator.validate();
                        cursor = scope.getCursor();

                        boolean isValid = IDataHelper.getOrDefault(cursor, "isValid", Boolean.class, true);

                        if (!isValid) {
                            IData[] errors = IDataHelper.get(cursor, "errors", IData[].class);
                            result = ValidationHelper.buildResult(direction, isValid, errors, pipeline);
                        }
                    } catch (WMDocumentException ex) {
                        ExceptionHelper.raiseUnchecked(ex);
                    } finally {
                        if (cursor != null) cursor.destroy();
                    }
                }
            }
        }

        return result;
    }

    /**
     * Sanitizes the given pipeline against the current service's input or output signature by dropping all undeclared
     * variables from the top-level of the pipeline.
     *
     * @param pipeline  The pipeline to be sanitized.
     * @param direction Whether to sanitize using the input or output signature.
     */
    public static void sanitize(IData pipeline, InputOutputSignature direction) {
        sanitize(ServiceHelper.self(), pipeline, direction);
    }

    /**
     * Sanitizes the given pipeline against the given service's input or output signature by dropping all undeclared
     * variables from the top-level of the pipeline.
     *
     * @param service   The service whose signature is used to sanitized against.
     * @param pipeline  The pipeline to be sanitized.
     * @param direction Whether to sanitize using the input or output signature.
     */
    public static void sanitize(NSService service, IData pipeline, InputOutputSignature direction) {
        // we can only sanitize the pipeline when not debugging
        if (service != null && !"wm.server.flow:stepFlow".equals(service.getNSName().getFullName())) {
            NSRecord record;
            if (direction == InputOutputSignature.INPUT) {
                record = service.getSignature().getInput();
            } else {
                record = service.getSignature().getOutput();
            }

            if (record != null) {
                NSField[] fields = record.getFields();
                if (fields != null) {
                    List<String> names = new ArrayList<String>(fields.length);
                    for (int i = 0; i < fields.length; i++) {
                        if (fields[i] != null) {
                            names.add(fields[i].getName());
                        }
                    }
                    IDataHelper.clear(pipeline, names.toArray(new String[0]));
                }
            }
        }
    }

    /**
     * Specifies whether to use the input or output signature.
     */
    public enum InputOutputSignature {
        INPUT, OUTPUT;
    }
}
