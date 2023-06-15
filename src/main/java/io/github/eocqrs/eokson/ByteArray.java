/*
 *  Copyright (c) 2023 Aliaksei Bialiauski, EO-CQRS
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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.eocqrs.eokson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Array of Bytes.
 */
final class ByteArray {

  /**
   * Byte array.
   */
  private final byte[] bytes;

  /**
   * Ctor.
   *
   * @param json JSON
   */
  ByteArray(final Json json) {
    this(json.bytes());
  }

  /**
   * Ctor.
   *
   * @param stream InputStream
   */
  ByteArray(final InputStream stream) {
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      final byte[] data = new byte[1024];
      while (true) {
        final int size = stream.read(data, 0, data.length);
        if (size == -1) {
          stream.close();
          break;
        }
        output.write(data, 0, size);
      }
      output.flush();
      this.bytes = output.toByteArray();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Bytes.
   *
   * @return All bytes
   */
  public byte[] value() {
    return this.bytes.clone();
  }
}
