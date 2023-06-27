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
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Optional;

/**
 * Evaluate JSON Node at a given path.
 *
 * @author Aliaksei Bialiauski (abialiauski.dev@gmail.com)
 * @since 0.1.2
 */
public final class NodeAt implements Scalar<Optional<JsonNode>> {

  /**
   * JSON path.
   */
  private final String path;
  /**
   * Jackson Node.
   */
  private final Unchecked<ObjectNode> jackson;

  /**
   * Ctor.
   *
   * @param pth     Path
   * @param jackson Jackson Node
   */
  public NodeAt(
    final String pth,
    final Unchecked<ObjectNode> jackson
  ) {
    this.path = pth;
    this.jackson = jackson;
  }

  @Override
  public Optional<JsonNode> value() {
    final JsonNode node;
    if (!this.path.isEmpty() && this.path.charAt(0) == '/') {
      node = this.jackson.value().at(this.path);
    } else {
      node = this.jackson.value().path(this.path);
    }
    if (node.isMissingNode()) {
      return Optional.empty();
    }
    return Optional.of(node);
  }
}
