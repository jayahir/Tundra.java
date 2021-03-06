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

package permafrost.tundra.lang;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import java.util.Locale;

/**
 * A collection of convenience methods for working with Locale objects.
 */
public final class LocaleHelper {
    /**
     * Disallow instantiation of this class.
     */
    private LocaleHelper() {}

    /**
     * Returns the given locale if not null, otherwise returns the default locale.
     *
     * @param locale The locale to be normalized.
     * @return       The given locale if not null, otherwise the default locale.
     */
    public static Locale normalize(Locale locale) {
        if (locale == null) locale = Locale.getDefault();
        return locale;
    }

    /**
     * Returns a new Locale object for the given language, country and variant
     *
     * @param language An ISO 639 alpha-2 or alpha-3 language code, or a language subtag up to 8 characters in length.
     *                 See the Locale class description about valid language values.
     * @param country  An ISO 3166 alpha-2 country code or a UN M.49 numeric-3 area code. See the Locale class
     *                 description about valid country values.
     * @param variant  Any arbitrary value used to indicate a variation of a Locale. See the Locale class description
     *                 for the details.
     * @return A new Locale object.
     */
    public static Locale toLocale(String language, String country, String variant) {
        Locale locale = Locale.getDefault();

        if (language != null) {
            if (country == null) {
                locale = new Locale(language);
            } else if (variant == null) {
                locale = new Locale(language, country);
            } else {
                locale = new Locale(language, country, variant);
            }
        }

        return locale;
    }

    /**
     * Converts an IData locale object to a Locale object.
     *
     * @param document The IData locale object to be converted.
     * @return A Locale object representing the given locale.
     */
    public static Locale toLocale(IData document) {
        String language = null, country = null, variant = null;

        if (document != null) {
            IDataCursor cursor = document.getCursor();
            language = IDataUtil.getString(cursor, "language");
            country = IDataUtil.getString(cursor, "country");
            variant = IDataUtil.getString(cursor, "variant");
            cursor.destroy();
        }

        return toLocale(language, country, variant);
    }
}
