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
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test case for {@link JsonOf}.
 *
 * @author Aliaksei Bialiauski (abialiauski.dev@gmail.com)
 * @since 0.1.1
 */
final class JsonOfTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void constructsFromBytes() throws JsonProcessingException {
    final byte[] bytes = MAPPER.writeValueAsBytes(
      MAPPER.createObjectNode()
        .put("field1", "value1")
        .put("field2", "value2")
    );
    MatcherAssert.assertThat(
      "JSONs are equal by bytes",
      new ByteArray(new JsonOf(bytes)).value(),
      new IsEqual<>(bytes)
    );
  }

  @Test
  void constructsFromString() {
    final String text = "{\"number\": 12}";
    MatcherAssert.assertThat(
      "JSON in right format",
      new ByteArray(new JsonOf(text)).value(),
      new IsEqual<>(text.getBytes())
    );
  }

  @Test
  void constructsFromInputStream() {
    final byte[] bytes = "{\"number\": 12}".getBytes();
    MatcherAssert.assertThat(
      "JSON's bytes in right format",
      new ByteArray(
        new JsonOf(
          new ByteArrayInputStream(bytes)
        )
      ).value(),
      new IsEqual<>(bytes)
    );
  }

  @Test
  void constructsFromJsonNode() throws JsonProcessingException {
    final JsonNode node = MAPPER.createObjectNode()
      .put("field1", "value1")
      .put("field2", "value2");
    MatcherAssert.assertThat(
      "JSON in right format",
      new ByteArray(new JsonOf(node)).value(),
      new IsEqual<>(MAPPER.writeValueAsBytes(node))
    );
  }

  @Test
  void constructsFromFile() throws IOException, URISyntaxException {
    final Path path = Paths.get(
      JsonOfTest.class.getClassLoader()
        .getResource("deep.json").toURI()
    );
    MatcherAssert.assertThat(
      "JSON in right format",
      new ByteArray(
        new JsonOf(path)
      ).value(),
      new IsEqual<>(Files.readAllBytes(path))
    );
  }

  @Test
  void readsBytesOfJsonArray() {
    final String json = "[{\"name\":\"Jason\"},{\"name\":\"Thetis\"}]";
    MatcherAssert.assertThat(
      "JSON in right format",
      new ByteArray(new JsonOf(json)).value(),
      new IsEqual<>(json.getBytes())
    );
  }

  @Test
  void stringifiesMalformed() {
    MatcherAssert.assertThat(
      "JSON in right format",
      new JsonOf("malformed").toString(),
      new IsEqual<>("malformed")
    );
  }

  @Test
  void readsMultipleTimes() {
    final String text = "{\"number\": 12}";
    MatcherAssert.assertThat(
      "JSON can be read multiple times",
      new ByteArray(new JsonOf(text)).value(),
      new IsEqual<>(text.getBytes())
    );
    MatcherAssert.assertThat(
      "JSON can be read multiple times",
      new ByteArray(new JsonOf(text)).value(),
      new IsEqual<>(text.getBytes())
    );
  }

  @Test
  void cachesFile() throws IOException {
    final String text = "{\"number\": 12}";
    final File file = File.createTempFile("whatever", "whatever");
    try (final PrintStream ps = new PrintStream(file)) {
      ps.print(text);
    }
    final Json json = new JsonOf(file.toPath());
    MatcherAssert.assertThat(
      "JSON in right format",
      new ByteArray(json).value(),
      new IsEqual<>(text.getBytes())
    );
    file.delete();
    MatcherAssert.assertThat(
      "JSON in right format",
      new ByteArray(json).value(),
      new IsEqual<>(text.getBytes())
    );
  }
}
