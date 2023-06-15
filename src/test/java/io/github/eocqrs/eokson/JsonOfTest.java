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
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class JsonOfTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void constructsFromBytes() throws JsonProcessingException {
    byte[] bytes = MAPPER.writeValueAsBytes(
      MAPPER.createObjectNode()
        .put("field1", "value1")
        .put("field2", "value2")
    );
    assertArrayEquals(
      bytes,
      new ByteArray(new Json.Of(bytes)).value()
    );
  }

  @Test
  void constructsFromString() {
    String string = "{\"number\": 12}";
    assertArrayEquals(
      string.getBytes(),
      new ByteArray(new Json.Of(string)).value()
    );
  }

  @Test
  void constructsFromInputStream() {
    final byte[] bytes = "{\"number\": 12}".getBytes();
    assertArrayEquals(
      bytes,
      new ByteArray(
        new Json.Of(
          new ByteArrayInputStream(bytes)
        )
      ).value()
    );
  }

  @Test
  void constructsFromJsonNode() throws JsonProcessingException {
    JsonNode node = MAPPER.createObjectNode()
      .put("field1", "value1")
      .put("field2", "value2");
    assertArrayEquals(
      MAPPER.writeValueAsBytes(node),
      new ByteArray(new Json.Of(node)).value()
    );
  }

  @Test
  void constructsFromFile() throws IOException, URISyntaxException {
    Path path = Paths.get(
      JsonOfTest.class.getClassLoader().getResource("deep.json").toURI()
    );
    assertArrayEquals(
      Files.readAllBytes(path),
      new ByteArray(new Json.Of(path)).value()
    );
  }

  @Test
  void understandsArrays() {
    String string = "[{\"name\":\"Jason\"},{\"name\":\"Thetis\"}]";
    assertArrayEquals(
      string.getBytes(),
      new ByteArray(new Json.Of(string)).value()
    );
  }

  @Test
  void toStringWorksEvenIfMalformed() {
    assertEquals(
      "malformed",
      new Json.Of("malformed").toString()
    );
  }

  @Test
  void canReadTwice() {
    String string = "{\"number\": 12}";
    Json json = new Json.Of(string);
    assertArrayEquals(string.getBytes(), new ByteArray(json).value());
    assertArrayEquals(string.getBytes(), new ByteArray(json).value());
  }

  @Test
  void doesntReadFileEachTimeJsonIsAccessed() throws IOException {
    String string = "{\"number\": 12}";
    File file = File.createTempFile("whatever", "whatever");
    try (PrintStream ps = new PrintStream(file)) {
      ps.print(string);
    }
    Json json = new Json.Of(file.toPath());
    assertArrayEquals(string.getBytes(), new ByteArray(json).value());
    file.delete();
    assertArrayEquals(string.getBytes(), new ByteArray(json).value());
  }
}
