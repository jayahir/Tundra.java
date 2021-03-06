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

/**
 * Compares two IData objects using all the keys and values in each document.
 */
public class BasicIDataComparator implements IDataComparator {
    /**
     * Whether key order is treated as significant, such that two documents with the same keys and values specified
     * in differing orders are not considered equivalent.
     */
    protected boolean isKeyOrderSignificant = true;

    /**
     * Construct a new BasicIDataComparator.
     */
    public BasicIDataComparator() {}

    /**
     * Construct a new BasicIDataComparator.
     *
     * @param isKeyOrderSignificant Whether the ordering of keys is considered significant when comparing documents.
     */
    public BasicIDataComparator(boolean isKeyOrderSignificant) {
        this.isKeyOrderSignificant = isKeyOrderSignificant;
    }

    /**
     * Compares two IData documents.
     *
     * @param document1     The first IData document to be compared.
     * @param document2     The second IData document to be compared.
     * @return              A value less than zero if the first document comes before the second document, a value of
     *                      zero if they are equal, or a value of greater than zero if the first document comes after
     *                      the second document according to the comparison of all the keys and values in each document.
     */
    public int compare(IData document1, IData document2) {
        return IDataHelper.compare(document1, document2, isKeyOrderSignificant);
    }
}
