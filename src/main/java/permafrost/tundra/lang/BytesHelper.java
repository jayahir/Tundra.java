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

import permafrost.tundra.io.StreamHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class BytesHelper {
    /**
     * Disallow instantiation of this class.
     */
    private BytesHelper() {}

    /**
     * Converts the given String to an byte[].
     * @param string        A String to be converted to a byte[].
     * @return              A byte[] representation of the given String.
     */
    public static byte[] normalize(String string) {
        return normalize(string, StringHelper.DEFAULT_CHARSET);
    }

    /**
     * Converts the given String to an byte[] using the given character encoding set.
     * @param string        A string to be converted to a byte[].
     * @param charsetName   The character encoding set to use.
     * @return              A byte[] representation of the given String.
     */
    public static byte[] normalize(String string, String charsetName) {
        return normalize(string, Charset.forName(charsetName));
    }

    /**
     * Converts the given String to an byte[] using the given character encoding set.
     * @param string        A string to be converted to a byte[].
     * @param charset       The character encoding set to use.
     * @return              A byte[] representation of the given String.
     */
    public static byte[] normalize(String string, Charset charset) {
        if (string == null) return null;
        return string.getBytes(charset);
    }

    /**
     * Converts the given java.io.InputStream to a byte[] by reading all
     * data from the stream and then closing the stream.
     * @param inputStream       A java.io.InputStream to be converted to a byte[]
     * @return                  A byte[] representation of the given java.io.InputStream.
     * @throws IOException      If there is a problem reading from the java.io.InputStream.
     */
    public static byte[] normalize(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamHelper.copy(inputStream, out);
        return out.toByteArray();
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a byte[].
     * @param object            The object to be normalized to a byte[].
     * @return                  A byte[] representation of the given object.
     * @throws IOException      If there is a problem reading from the java.io.InputStream.
     */
    public static byte[] normalize(Object object) throws IOException {
        return normalize(object, StringHelper.DEFAULT_CHARSET);
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a byte[].
     * @param object            The object to be normalized to a string.
     * @param charsetName       The character set to use.
     * @return                  A byte[] representation of the given object.
     * @throws IOException      If there is a problem reading from the java.io.InputStream.
     */
    public static byte[] normalize(Object object, String charsetName) throws IOException {
        return normalize(object, Charset.forName(charsetName));
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a byte[].
     * @param object            The object to be normalized to a string.
     * @param charset           The character set to use.
     * @return                  A byte[] representation of the given object.
     * @throws IOException      If there is a problem reading from the java.io.InputStream.
     */
    public static byte[] normalize(Object object, Charset charset) throws IOException {
        if (object == null) return null;

        byte[] output;

        if (object instanceof byte[]) {
            output = (byte[])object;
        } else if (object instanceof String) {
            output = normalize((String)object, charset);
        } else if (object instanceof InputStream) {
            output = normalize((InputStream)object);
        } else {
            throw new IllegalArgumentException("object must be a String, byte[], or java.io.InputStream");
        }

        return output;
    }

    /**
     * Encodes binary data as a base64-encoded string.
     *
     * @param input Binary data to be base64-encoded.
     * @return The given data as a base64-encoded string.
     */
    public static String base64Encode(byte[] input) {
        return input == null ? null : javax.xml.bind.DatatypeConverter.printBase64Binary(input);
    }

    /**
     * Decodes a base64-encoded string to binary data.
     *
     * @param input A base64-encoded string.
     * @return The base64-encoded string decoded to binary data.
     */
    public static byte[] base64Decode(String input) {
        return input == null ? null : javax.xml.bind.DatatypeConverter.parseBase64Binary(input);
    }
}