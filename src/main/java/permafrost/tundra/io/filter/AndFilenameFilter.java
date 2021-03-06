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

package permafrost.tundra.io.filter;

import permafrost.tundra.collection.ListHelper;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;

/**
 * Chains multiple FilenameFilter objects with a logical AND operation.
 */
public class AndFilenameFilter extends ConditionalFilenameFilter {
    /**
     * Constructs a new AndFilenameFilter.
     */
    public AndFilenameFilter() {}

    /**
     * Constructs a new AndFilenameFilter.
     *
     * @param filters   The list of filters to be chained with a logical AND operation.
     */
    public AndFilenameFilter(FilenameFilter ...filters) {
        this(ListHelper.of(filters));
    }

    /**
     * Constructs a new AndFilenameFilter.
     *
     * @param filters   The list of filters to be chained with a logical AND operation.
     */
    public AndFilenameFilter(Collection<FilenameFilter> filters) {
        addAll(filters);
    }

    /**
     * Returns true if the given parent and child passes all chained filters.
     *
     * @param parent    The parent directory being filtered.
     * @param child     The child filename being filtered.
     * @return          True if the given parent and child passes all chained filters.
     */
    public boolean accept(File parent, String child) {
        for (FilenameFilter filter : filters) {
            if (filter != null) {
                if (!filter.accept(parent, child)) return false;
            }
        }
        return !filters.isEmpty();
    }
}
