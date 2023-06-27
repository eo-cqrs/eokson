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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

/**
 * JSON Document.
 * <p>
 * It can represent itself in other data types such as,
 * byte arrays, {@link String}s, {@link InputStream}s, and so forth. It can also
 * give its nested JSONs and leaves, tell if it is missing and do other useful
 * things. To use it, you need to wrap another {@link Json} in it, e.g.
 * <pre>
 * {@code
 * Json original = new JsonOf(...);
 * Jocument document = new Jocument(original);
 * String textual = document.textual();
 * InputStream stream = document.inputStream();
 * Jocument nested = document.at("/path/to/nested/json");
 * if (!nested.isMissing()) {
 *     Optional<String> value = nested.leaf("fieldName");
 * }
 * }
 * </pre>
 *
 * @author Aliaksei Bialiauski (abialiauski.dev@gmail.com)
 * @since 0.0.0
 */
public final class Jocument implements Json {

  /**
   * Object Mapper.
   */
  private static final ObjectMapper MAPPER = new ObjectMapper();
  /**
   * All NULL bytes.
   */
  private static final byte[] NULL_BYTES = {
    (byte) 110, (byte) 117, (byte) 108, (byte) 108
  };
  /**
   * Origin.
   */
  private final Json origin;
  /**
   * Unchecked Node.
   */
  private final Unchecked<ObjectNode> jackson;

  /**
   * Ctor.
   *
   * @param orgn Original JSON
   */
  public Jocument(final Json orgn) {
    this(
      orgn,
      new Unchecked<>(
        () -> MAPPER.readValue(orgn.bytes(), ObjectNode.class)
      )
    );
  }

  /**
   * Ctor.
   *
   * @param orgn Original JSON
   * @param node Object node
   */
  private Jocument(final Json orgn, final Unchecked<ObjectNode> node) {
    this.origin = orgn;
    this.jackson = node;
  }

  /**
   * JSON as text.
   *
   * @return String representing this JSON in textual form
   */
  public String textual() {
    return new Unchecked<>(
      () -> MAPPER.writeValueAsString(this.jackson.value())
    ).value();
  }

  /**
   * JSON as pretty text.
   *
   * @return String representing this JSON in pretty format textual form
   */
  public String pretty() {
    return new Unchecked<>(
      () -> MAPPER.writerWithDefaultPrettyPrinter()
        .writeValueAsString(this.jackson.value())
    ).value();
  }

  /**
   * JSON as an array of bytes.
   *
   * @return Byte array
   */
  public byte[] byteArray() {
    return new ByteArray(this.bytes()).value();
  }

  /**
   * Get a leaf of type {@code String}, boxed in {@code Optional} of this JSON.
   *
   * @param path JSON path
   * @return Optional leaf value
   */
  public Optional<String> optLeaf(final String path) {
    return this.nodeAt(path).map(JsonNode::textValue);
  }

  /**
   * Get a leaf of type {@code String} of this JSON.
   *
   * @param path JSON path
   * @return String leaf value, if the leaf exists
   * @throws IllegalArgumentException if leaf does not exist
   */
  public String leaf(final String path) {
    return this.optLeaf(path).orElseThrow(
      () -> new IllegalArgumentException(
        "No such field of specified type: " + path
      )
    );
  }

  /**
   * Get a leaf of type {@code Integer}, boxed in {@code Optional} of this JSON.
   *
   * @param path JSON path
   * @return Optional leaf value
   */
  public Optional<Integer> optLeafAsInt(final String path) {
    return this.nodeAt(path).map(JsonNode::intValue);
  }

  /**
   * Get a leaf of type {@code int} of this JSON.
   *
   * @param path JSON path
   * @return Int leaf value
   * @throws IllegalArgumentException if leaf does not exist
   */
  public int leafAsInt(final String path) {
    return this.optLeafAsInt(path).orElseThrow(
      () -> new IllegalArgumentException(
        "No such field of specified type: " + path
      )
    );
  }

  /**
   * Get a leaf of type {@code double}, boxed in {@code Optional} of this JSON.
   *
   * @param path JSON path
   * @return Optional leaf value
   */
  public Optional<Double> optLeafAsDouble(final String path) {
    return this.nodeAt(path).map(JsonNode::doubleValue);
  }

  /**
   * Get a leaf of type {@code double} of this JSON.
   *
   * @param path JSON path
   * @return Double leaf value
   * @throws IllegalArgumentException if leaf does not exist
   */
  public double leafAsDouble(final String path) {
    return this.optLeafAsDouble(path).orElseThrow(
      () -> new IllegalArgumentException(
        "No such field of specified type: " + path
      )
    );
  }

  /**
   * Get a leaf of type {@code Boolean}, boxed in {@code Optional} of this JSON.
   *
   * @param path JSON path
   * @return Optional leaf value
   */
  public Optional<Boolean> optLeafAsBool(final String path) {
    return this.nodeAt(path).map(JsonNode::booleanValue);
  }

  /**
   * Get a leaf of type {@code boolean} of this JSON.
   *
   * @param path JSON path
   * @return Boolean leaf value
   * @throws IllegalArgumentException if field does not exist
   */
  public boolean leafAsBool(final String path) {
    return this.optLeafAsBool(path).orElseThrow(
      () -> new IllegalArgumentException(
        "No such field of specified type: " + path
      )
    );
  }

  private Optional<JsonNode> nodeAt(final String path) {
    final JsonNode node;
    if (!path.isEmpty() && path.charAt(0) == '/') {
      node = this.jackson.value().at(path);
    } else {
      node = this.jackson.value().path(path);
    }
    if (node.isMissingNode()) {
      return Optional.empty();
    }
    return Optional.of(node);
  }

  /**
   * Represent this JSON as {@link ObjectNode} in case full JSON manipulation
   * capabilities offered by jackson-databind library are needed.
   *
   * @return This JSON as {@link ObjectNode}
   */
  public ObjectNode objectNode() {
    return this.jackson.value();
  }

  /**
   * Get a JSON nested within this JSON, specified by path.
   * Path starts with a forward slash, and path elements
   * are separated by forward slashes also, e.g.
   * <pre>
   * {@code
   * Jocument nested = json.at("/path/to/nested/json");}
   * </pre>
   * This method never returns null. If there is no JSON as specified by the
   * path, a missing JSON is returned, i.e.
   * {@code returned.isMissing() == true}.
   *
   * @param path Path to the nested JSON
   * @return The nested JSON, which could be missing
   */
  public Jocument at(final String path) {
    return new Jocument(
      new JsonOf(
        this.jackson.value()
          .at(path)
      )
    );
  }

  /**
   * Tells if this JSON is missing.
   *
   * @return Is missing or not
   */
  public boolean isMissing() {
    final byte[] bytes = this.byteArray();
    return bytes.length == 0 || Arrays.equals(bytes, NULL_BYTES);
  }

  @Override
  public InputStream bytes() {
    return this.origin.bytes();
  }

  @Override
  public String toString() {
    return new String(
      new ByteArray(this)
        .value()
    );
  }
}
