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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * JSON document. This is the type to be implemented by all objects which
 * represent JSONs.
 * In addition to writing custom {@code Json} objects, various data types
 * can be represented as {@code Json} by instantiating a {@link Json.Of} object.
 */
public interface Json {

  /**
   * Tell this {@code Json} to represent itself as bytes.
   *
   * @return {@link InputStream} with bytes representing this {@code Json}.
   */
  InputStream bytes();

  /**
   * {@link Json}, constructed from JSON represented by other data types
   * such as byte array, {@code String}, {@code InputStream} and so forth.
   * E.g.
   * <pre>
   * {@code
   * String jsonAsString = ...;
   * Json json = new Json.Of(jsonAsString);
   * }
   * </pre>
   */
  final class Of implements Json {

    /**
     * Object Mapper.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Origin.
     */
    private final Json origin;

    /**
     * Ctor.
     *
     * @param node JSON represented by {@link JsonNode} from
     *             'jackson-databind' library.
     */
    public Of(final JsonNode node) {
      this(() -> node);
    }

    /**
     * Ctor.
     *
     * @param node JSON represented by {@link JsonNode} from
     *             'jackson-databind' library.
     */
    public Of(final Supplier<JsonNode> node) {
      this(
        flattened(
          () -> {
            try {
              return MAPPER.writeValueAsBytes(node.get());
            } catch (final JsonProcessingException e) {
              throw new UncheckedIOException(e);
            }
          }
        )
      );
    }

    private static <T> T flattened(final Supplier<T> scalar) {
      return scalar.get();
    }

    /**
     * Ctor.
     *
     * @param string JSON represented by a {@link String}.
     */
    public Of(final String string) {
      this(string.getBytes());
    }

    /**
     * Ctor.
     *
     * @param bytes JSON represented by an array of bytes.
     */
    public Of(final byte[] bytes) {
      this(
        new AutoResetInputStream(
          new ByteArrayInputStream(bytes)
        )
      );
    }

    /**
     * Ctor.
     *
     * @param stream JSON represented by the bytes in an
     *               {@link InputStream}.
     */
    public Of(final InputStream stream) {
      this.origin = () -> stream;
    }

    /**
     * Ctor.
     *
     * @param path Path to a JSON in a file.
     */
    public Of(final Path path) {
      this(
        new Cached<>(
          () -> new Unchecked<>(
            () -> new AutoResetInputStream(
              new ByteArrayInputStream(Files.readAllBytes(path))
            )
          ).value()
        )
      );
    }

    private Of(final Cached<InputStream> cached) {
      this(cached::value);
    }

    private Of(final Json json) {
      this.origin = json;
    }

    @Override
    public InputStream bytes() {
      return this.origin.bytes();
    }

    @Override
    public String toString() {
      return new String(new ByteArray(this).value());
    }
  }
}
