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

package permafrost.tundra.data;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataHashCursor;
import com.wm.data.IDataIndexCursor;
import com.wm.data.IDataPortable;
import com.wm.data.IDataSharedCursor;
import com.wm.data.IDataTreeCursor;
import com.wm.data.ISMemDataImpl;
import com.wm.util.Values;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import java.io.Serializable;

/**
 * A convenience wrapper for an IData object, that implements the IData, IDataCodable, IDataPortable, and ValuesCodable
 * interfaces for maximum API compatibility.
 */
@SuppressWarnings("deprecation")
public class IDataAdapter extends ISMemDataImpl implements IData, IDataCodable, IDataPortable, ValuesCodable, Cloneable, Serializable {
    private static final long serialVersionUID = 1;

    /**
     * The wrapped IData document.
     */
    protected volatile IData document;

    /**
     * Constructs a new empty IDataAdapter document.
     */
    public IDataAdapter() {
        setIData(null);
    }

    /**
     * Constructs a new IDataAdapter wrapping the given IData document.
     *
     * @param document The IData document to be wrapped.
     */
    public IDataAdapter(IData document) {
        setIData(document instanceof IDataAdapter ? ((IDataAdapter)document).document : document);
    }

    /**
     * Constructs a new IDataAdapter wrapping the given IDataCodable object.
     *
     * @param codable The IDataCodable object to be wrapped.
     */
    public IDataAdapter(IDataCodable codable) {
        this(codable == null ? null : codable.getIData());
    }

    /**
     * Constructs a new IDataAdapter wrapping the given IDataPortable object.
     *
     * @param portable The IDataPortable object to be wrapped.
     */
    @SuppressWarnings("deprecation")
    public IDataAdapter(IDataPortable portable) {
        this(portable == null ? null : portable.getAsData());
    }

    /**
     * Constructs a new IDataAdapter wrapping the given ValuesCodable object.
     *
     * @param codable The ValuesCodable object to be wrapped.
     */
    public IDataAdapter(ValuesCodable codable) {
        this(codable == null ? null : codable.getValues());
    }

    /**
     * Returns an IDataCursor for this IData object. An IDataCursor contains the basic methods you use to traverse an
     * IData object and get or set elements within it.
     *
     * @return An IDataCursor for this object.
     */
    @Override
    public IDataCursor getCursor() {
        return document.getCursor();
    }

    /**
     * Returns an IDataSharedCursor for the IData object. A shared cursor is a hash cursor with the additional
     * capability of expressing an underlying data structure that is shared or constrained by data. It differs from
     * other cursors in that its operations can fail and throw exceptions.
     *
     * @return An IDataSharedCursor for this object.
     */
    @Override
    public IDataSharedCursor getSharedCursor() {
        return document.getSharedCursor();
    }

    /**
     * Returns an IDataHashCursor for the IData object. A hash cursor is a cursor that you use to access elements of the
     * IData object by key.
     *
     * @return An IDataHashCursor for this object.
     * @deprecated Replaced by key-based methods in {@link IDataCursor}.
     */
    @Override
    @Deprecated
    public IDataHashCursor getHashCursor() {
        return document.getHashCursor();
    }

    /**
     * Returns an IDataIndexCursor for this IData object. An IDataIndexCursor contains methods you use to traverse an
     * IData object by absolute position.
     *
     * @return An IDataIndexCursor for this object.
     * @deprecated No replacement. See {@link IDataIndexCursor} for alternative sample code.
     */
    @Override
    @Deprecated
    public IDataIndexCursor getIndexCursor() {
        return document.getIndexCursor();
    }

    /**
     * Returns an IDataTreeCursor for this IData object. A tree cursor contains methods you use to traverse an IData
     * object as a tree structure.
     *
     * @return An IDataTreeCursor for this object.
     * @deprecated No replacement.
     */
    @Override
    @Deprecated
    public IDataTreeCursor getTreeCursor() {
        return document.getTreeCursor();
    }

    /**
     * Returns the IData document that this object iterates over.
     *
     * @return The IData document this object wraps.
     */
    @Override
    @Deprecated
    public IData getIData() {
        return document;
    }

    /**
     * Set the IData document this object wraps.
     *
     * @param document The IData document to be wrapped.
     */
    @Override
    public void setIData(IData document) {
        if (document == null) {
            this.document = IDataFactory.create();
        } else {
            this.document = document;
        }
    }

    /**
     * Returns the IData document that this object wraps.
     *
     * @return The IData document this object wraps.
     */
    @Override
    @SuppressWarnings("deprecation")
    public IData getAsData() {
        return getIData();
    }

    /**
     * Set the IData document this object wraps.
     *
     * @param document The IData document to be wrapped.
     */
    @Override
    @SuppressWarnings("deprecation")
    public void setFromData(IData document) {
        setIData(document);
    }

    /**
     * Set the Values object this object wraps.
     *
     * @param values The Values object to be wrapped.
     */
    @Override
    public void setValues(Values values) {
        setIData(values);
    }

    /**
     * Returns the Values object this object wraps.
     *
     * @return The Values object this object wraps.
     */
    @Override
    public Values getValues() {
        return Values.use(getIData());
    }

    /**
     * Returns a string representation of this object.
     *
     * @return A string representation of this object.
     */
    @Override
    public String toString() {
        return document.toString();
    }

    /**
     * Returns a copy of this IData object.
     *
     * @return A copy of this IData object.
     */
    @Override
    public IData copy() {
        return clone();
    }

    /**
     * Returns a newly created IData object.
     *
     * @return A newly created IData object.
     */
    public static IData create() {
        return new IDataAdapter();
    }

    /**
     * Returns a clone of this IData object.
     *
     * @return A clone of this IData object.
     */
    @Override
    public IDataAdapter clone() {
        return new IDataAdapter(IDataHelper.duplicate(document, false));
    }

    /**
     * Returns a new IDataAdapter wrapping the given IData document.
     *
     * @param document  The document to be wrapped.
     * @return          A new IDataAdapter wrapping the given IData document.
     */
    public static IDataAdapter of(IData document) {
        return new IDataAdapter(document);
    }

    /**
     * Returns a new IDataAdapter[] representation of the given IData[] document list.
     *
     * @param array An IData[] document list.
     * @return      A new IDataAdapter[] representation of the given IData[] document list.
     */
    public static IDataAdapter[] of(IData[] array) {
        IDataAdapter[] output = null;

        if (array != null) {
            output = new IDataAdapter[array.length];
            for (int i = 0; i < array.length; i++) {
                output[i] = new IDataAdapter(array[i]);
            }
        }

        return output;
    }
}
