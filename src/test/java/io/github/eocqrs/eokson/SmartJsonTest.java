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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SmartJsonTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final Path deep;

  SmartJsonTest() throws URISyntaxException {
    this.deep = Paths.get(
      SmartJsonTest.class.getClassLoader()
        .getResource("deep.json").toURI()
    );
  }

  @Test
  void givesByteStream() {
    byte[] bytes = "{\"field1\":\"value1\",\"field2\":\"value2\"}"
      .getBytes();
    assertArrayEquals(
      bytes,
      new ByteArray(
        new SmartJson(
          new Json.Of(bytes)
        )
      ).value()
    );
  }

  @Test
  void convertsToString() {
    assertEquals(
      "{\"field1\":\"value1\","
        + "\"field2\":\"value2\"}",
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", "value2")
        )
      ).textual()
    );
  }

  @Test
  void convertsToPrettyString() {
    assertEquals(
      '{' + System.lineSeparator()
        + "  \"field1\" : \"value1\"," + System.lineSeparator()
        + "  \"field2\" : \"value2\"" + System.lineSeparator()
        + '}',
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", "value2")
        )
      ).pretty()
    );
  }

  @Test
  void convertsToByteArray() throws JsonProcessingException {
    final byte[] bytes = MAPPER.writeValueAsBytes(
      MAPPER.createObjectNode()
        .put("field1", "value1")
        .put("field2", "value2")
    );
    assertArrayEquals(
      bytes,
      new SmartJson(new Json.Of(bytes)).byteArray()
    );
  }

  @Test
  void convertsToObjectNode() {
    ObjectNode node = MAPPER.createObjectNode()
      .put("field1", "value1")
      .put("field2", "value2");
    assertEquals(
      node,
      new SmartJson(
        new Json.Of(node)
      ).objectNode()
    );
  }

  @Test
  void findsPath() {
    assertEquals(
      "red",
      new SmartJson(
        new Json.Of(deep)
      ).at("/ocean/rock1/nereid2").leaf("hair")
    );
  }

  @Test
  void handlesNonExistentPaths() {
    assertTrue(
      new SmartJson(
        new Json.Of(deep)
      ).at("/ocean/nothing").isMissing()
    );
  }

  @Test
  void findsPathInArray() {
    assertEquals(
      "Jason",
      new SmartJson(
        new Json.Of(deep)
      ).at("/ocean/rock1/nereid1/associates/0").leaf("name")
    );
  }

  @Test
  void readsArraysInRightFormat() {
    MatcherAssert.assertThat(
      "JSON in right format",
      new SmartJson(
        new Json.Of(
          this.deep
        )
      ).at("/ocean/rock1/nereid1/associates/0").textual(),
      Matchers.equalTo("{\"name\":\"Jason\"}")
    );
  }

  @Disabled
  @Test
  void reallyUnderstandsArrays() {
    String array = "[{\"name\":\"Jason\"},{\"name\":\"Thetis\"}]";
    assertEquals(
      "Jason",
      new SmartJson(
        new Json.Of(deep)
      ).at("/ocean/rock1/nereid1/associates").at("/0").leaf("name")
    );
  }

  @Test
  void knowsIfMissing() {
    assertTrue(new SmartJson(new Missing()).isMissing());
  }

  @Test
  void knowsIfNotMissing() {
    assertFalse(new SmartJson(new Json.Of("{}")).isMissing());
  }

  @Test
  void toStringWorksWhenMalformed() {
    assertEquals(
      "malformed",
      new SmartJson(new Json.Of("malformed")).toString()
    );
  }

  @Test
  void canReadTwice() {
    SmartJson json = new SmartJson(
      new Json.Of("{\"field1\":\"value1\",\"field2\":\"value2\"}")
    );
    assertEquals("value1", json.leaf("field1"));
    assertEquals("value1", json.leaf("field1"));
  }
}
