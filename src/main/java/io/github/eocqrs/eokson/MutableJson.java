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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.InputStream;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * JSON, which is mutable and can be used to build custom JSONs, e.g.
 * <pre>
 * {@code
 * new MutableJson().with(
 *     "ocean",
 *     new MutableJson().with(
 *         "nereid",
 *         new MutableJson()
 *             .with("hair", "black")
 *             .with("age", 100)
 *             .with("fair", true)
 *     )
 * )
 * }
 * </pre>
 */
public final class MutableJson implements Json {

  /**
   * Object Mapper.
   */
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Base node.
   */
  private final ObjectNode base;

  /**
   * Ctor.
   */
  public MutableJson() {
    this(MAPPER.createObjectNode());
  }

  /**
   * Ctor.
   *
   * @param base The base JSON to build upon.
   */
  public MutableJson(final Json base) {
    this(
      (ObjectNode) new Unchecked<>(
        () -> MAPPER.readTree(base.bytes())
      ).value()
    );
  }

  private MutableJson(final ObjectNode base) {
    this.base = base;
  }

  /**
   * Add a {@code String} field to this JSON.
   *
   * @param name  Name of the field.
   * @param value Value of the field.
   * @return This JSON.
   */
  public MutableJson with(final String name, final String value) {
    this.base.put(name, value);
    return this;
  }

  /**
   * Add an {@code int} field to this JSON.
   *
   * @param name  Name of the field.
   * @param value Value of the field.
   * @return This JSON.
   */
  public MutableJson with(final String name, final int value) {
    this.base.put(name, value);
    return this;
  }

  /**
   * Add a {@code double} field to this JSON.
   *
   * @param name  Name of the field.
   * @param value Value of the field.
   * @return This JSON.
   */
  public MutableJson with(final String name, final double value) {
    this.base.put(name, value);
    return this;
  }

  /**
   * Add a {@code boolean} field to this JSON.
   *
   * @param name  Name of the field.
   * @param value Value of the field.
   * @return This JSON.
   */
  public MutableJson with(final String name, final boolean value) {
    this.base.put(name, value);
    return this;
  }

  /**
   * Add a {@link Json} field to this JSON. If the added {@link Json} is a,
   * other fields can be added to it, thus enabling nesting.
   *
   * @param name  Name of the field.
   * @param value Value of the field.
   * @return This JSON.
   */
  public MutableJson with(final String name, final Json value) {
    this.base.set(name, new Jocument(value).objectNode());
    return this;
  }

  /**
   * Add a JSON Array into the current JSON.
   *
   * @param name  Name of the field.
   * @param jsons JSON array.
   * @return This JSON.
   */
  public MutableJson with(
    final String name,
    final Collection<MutableJson> jsons
  ) {
    final ArrayNode node = MAPPER.createArrayNode();
    this.base.set(name, node);
    jsons.forEach(
      json ->
        node.add(new Jocument(json).objectNode())
    );
    return this;
  }

  @Override
  public InputStream bytes() {
    return new JsonOf(this.base).bytes();
  }

  @Override
  public String toString() {
    return new String(new ByteArray(this).value());
  }
}
