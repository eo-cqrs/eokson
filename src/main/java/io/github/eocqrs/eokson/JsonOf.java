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
 * JSON Decorator.
 *
 * @author Aliaksei Bialiauski (abialiauski.dev@gmail.com)
 * @since 0.1.1
 */
public final class JsonOf implements Json {

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
   * @param node JSON represented by {@link JsonNode}
   */
  public JsonOf(final JsonNode node) {
    this(() -> node);
  }

  /**
   * Ctor.
   *
   * @param node JSON represented by {@link JsonNode}
   */
  public JsonOf(final Supplier<JsonNode> node) {
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

  /**
   * Ctor.
   *
   * @param str JSON represented by a {@link String}.
   */
  public JsonOf(final String str) {
    this(str.getBytes());
  }

  /**
   * Ctor.
   *
   * @param bytes JSON represented by an array of bytes
   */
  public JsonOf(final byte[] bytes) {
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
   *               {@link InputStream}
   */
  public JsonOf(final InputStream stream) {
    this.origin = () -> stream;
  }

  /**
   * Ctor.
   *
   * @param path Path to a JSON in a file
   */
  public JsonOf(final Path path) {
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

  /**
   * Ctor.
   *
   * @param cached Cached
   */
  public JsonOf(final Cached<InputStream> cached) {
    this(cached::value);
  }

  /**
   * Ctor.
   *
   * @param json JSON
   */
  public JsonOf(final Json json) {
    this.origin = json;
  }

  private static <T> T flattened(final Supplier<T> scalar) {
    return scalar.get();
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
