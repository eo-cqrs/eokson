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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
  void leafsArrays() {
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
  void stringifiesOnMailFormed() {
    assertEquals(
      "malformed",
      new SmartJson(new Json.Of("malformed")).toString()
    );
  }

  @Test
  void readsTwice() {
    SmartJson json = new SmartJson(
      new Json.Of("{\"field1\":\"value1\",\"field2\":\"value2\"}")
    );
    assertEquals("value1", json.leaf("field1"));
    assertEquals("value1", json.leaf("field1"));
  }

  @Test
  void findsOptLeaf() {
    assertEquals(
      "value2",
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", "value2")
        )
      ).optLeaf("field2").get()
    );
  }

  @Test
  void findsLeaf() {
    assertEquals(
      "value2",
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", "value2")
        )
      ).leaf("field2")
    );
  }

  @Test
  void returnsEmptyOptionalForNonexistentLeaf() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
        )
      ).optLeaf("nonexistent").isPresent()
    );
  }

  @Test
  void throwsForNonexistentLeaf() {
    assertTrue(
      assertThrows(
        IllegalArgumentException.class,
        () -> new SmartJson(
          new Json.Of(
            MAPPER.createObjectNode()
          )
        ).leaf("nonexistent")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsEmptyOptionalIfLeafIsNotString() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("intField", 5)
        )
      ).optLeaf("intField").isPresent()
    );
  }

  @Test
  void throwsIfLeafIsNotString() {
    assertTrue(
      assertThrows(
        IllegalArgumentException.class,
        () -> new SmartJson(
          new Json.Of(
            MAPPER.createObjectNode()
              .put("field1", "value1")
              .put("intField", 5)
          )
        ).leaf("intField")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void emptyFieldName() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("intField", 5)
        )
      ).optLeaf("").isPresent()
    );
  }

  @Test
  void findsOptLeafInPath() {
    assertEquals(
      "innerValue",
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", "innerValue")
            )
        )
      ).optLeaf("/field2/innerField").get()
    );
  }

  @Test
  void findsLeafInPath() {
    assertEquals(
      "innerValue",
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", "innerValue")
            )
        )
      ).leaf("/field2/innerField")
    );
  }

  @Test
  void returnsEmptyOptionalForNonexistentLeafInPath() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
        )
      ).optLeaf("/nonexistent/path").isPresent()
    );
  }

  @Test
  void throwsForNonexistentLeafInPath() {
    assertTrue(
      assertThrows(
        IllegalArgumentException.class,
        () -> new SmartJson(
          new Json.Of(
            MAPPER.createObjectNode()
          )
        ).leaf("/nonexistent/path")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsEmptyOptionalIfLeafInPathIsNotString() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", 7)
            )
        )
      ).optLeaf("/field2/innerField").isPresent()
    );
  }

  @Test
  void throwsIfLeafInPathIsNotString() {
    assertTrue(
      assertThrows(
        IllegalArgumentException.class,
        () -> new SmartJson(
          new Json.Of(
            MAPPER.createObjectNode()
              .put("field1", "value1")
              .<ObjectNode>set(
                "field2",
                MAPPER.createObjectNode()
                  .put("innerField", true)
              )
          )
        ).leaf("/field2/innerField")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void findsOptLeafAsInt() {
    assertEquals(
      14,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", 14)
        )
      ).optLeafAsInt("field2").get().intValue()
    );
  }

  @Test
  void findsLeafAsInt() {
    assertEquals(
      14,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", 14)
        )
      ).leafAsInt("field2")
    );
  }

  @Test
  void returnsEmptyOptionalForNonexistentIntLeaf() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
        )
      ).optLeafAsInt("nonexistent").isPresent()
    );
  }

  @Test
  void throwsForNonexistentIntLeaf() {
    assertTrue(
      assertThrows(
        IllegalArgumentException.class,
        () -> new SmartJson(
          new Json.Of(
            MAPPER.createObjectNode()
          )
        ).leafAsInt("nonexistent")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsZeroIfOptLeafIsNotInt() {
    assertEquals(
      0,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("intField", 5)
        )
      ).optLeafAsInt("stringField").get().intValue()
    );
  }

  @Test
  void returnsZeroIfLeafIsNotInt() {
    assertEquals(
      0,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("intField", 5)
        )
      ).leafAsInt("stringField")
    );
  }

  @Test
  void returnsIntEvenIfOptLeafIsDouble() {
    assertEquals(
      5,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("doubleField", 5.9)
        )
      ).optLeafAsInt("doubleField").get().intValue()
    );
  }

  @Test
  void returnsIntEvenIfLeafIsDouble() {
    assertEquals(
      5,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("doubleField", 5.9)
        )
      ).leafAsInt("doubleField")
    );
  }

  @Test
  void findsOptIntLeafInPath() {
    assertEquals(
      999,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", 999)
            )
        )
      ).optLeafAsInt("/field2/innerField").get().intValue()
    );
  }

  @Test
  void findsLeafIntInPath() {
    assertEquals(
      12,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", 12)
            )
        )
      ).leafAsInt("/field2/innerField")
    );
  }

  @Test
  void returnsEmptyOptionalForNonexistentLeafIntInPath() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
        )
      ).optLeafAsInt("/nonexistent/path").isPresent()
    );
  }

  @Test
  void throwsForNonexistentLeafIntInPath() {
    assertTrue(
      assertThrows(
        IllegalArgumentException.class,
        () -> new SmartJson(
          new Json.Of(
            MAPPER.createObjectNode()
          )
        ).leafAsInt("/nonexistent/path")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsZeroIfOptLeafInPathIsNotInt() {
    assertEquals(
      0,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", true)
            )
        )
      ).optLeafAsInt("/field2/innerField").get().intValue()
    );
  }

  @Test
  void returnsZeroIfLeafInPathIsNotInt() {
    assertEquals(
      0,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", true)
            )
        )
      ).leafAsInt("/field2/innerField")
    );
  }

  @Test
  void returnsIntEvenIfOptLeafInPathIsDouble() {
    assertEquals(
      5,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", 5.9)
            )
        )
      ).optLeafAsInt("/field2/innerField").get().intValue()
    );
  }

  @Test
  void findsOptLeafAsDouble() {
    assertEquals(
      14.9,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", 14.9)
            .put("field2", "value2")
        )
      ).optLeafAsDouble("field1").get().doubleValue()
    );
  }

  @Test
  void findsLeafAsDouble() {
    assertEquals(
      14.9,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", 14.9)
            .put("field2", "value2")
        )
      ).leafAsDouble("field1")
    );
  }

  @Test
  void returnsEmptyOptionalForNonexistentDoubleLeaf() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
        )
      ).optLeafAsDouble("nonexistent").isPresent()
    );
  }

  @Test
  void throwsForNonexistentDoubleLeaf() {
    assertTrue(
      assertThrows(
        IllegalArgumentException.class,
        () -> new SmartJson(
          new Json.Of(
            MAPPER.createObjectNode()
          )
        ).leafAsDouble("nonexistent")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsZeroIfOptLeafIsNotDouble() {
    assertEquals(
      0.0,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("doubleField", 5.9)
        )
      ).optLeafAsDouble("stringField").get().doubleValue()
    );
  }

  @Test
  void returnsZeroIfLeafIsNotDouble() {
    assertEquals(
      0.0,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("doubleField", 5.9)
        )
      ).leafAsDouble("stringField")
    );
  }

  @Test
  void returnsDoubleEvenIfOptLeafIsInt() {
    assertEquals(
      5.0,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("intField", 5)
        )
      ).optLeafAsDouble("intField").get().doubleValue()
    );
  }

  @Test
  void returnsDoubleEvenIfLeafIsInt() {
    assertEquals(
      5.0,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("intField", 5)
        )
      ).leafAsDouble("intField")
    );
  }

  @Test
  void findsOptDoubleLeafInPath() {
    assertEquals(
      999.17,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", 999.17)
            )
        )
      ).optLeafAsDouble("/field2/innerField").get().doubleValue()
    );
  }

  @Test
  void findsLeafDoubleInPath() {
    assertEquals(
      12.45,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", 12.45)
            )
        )
      ).leafAsDouble("/field2/innerField")
    );
  }

  @Test
  void returnsEmptyOptionalForNonexistentLeafDoubleInPath() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
        )
      ).optLeafAsDouble("/nonexistent/path").isPresent()
    );
  }

  @Test
  void throwsForNonexistentLeafDoubleInPath() {
    assertTrue(
      assertThrows(
        IllegalArgumentException.class,
        () -> new SmartJson(
          new Json.Of(
            MAPPER.createObjectNode()
          )
        ).leafAsDouble("/nonexistent/path")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsZeroIfOptLeafInPathIsNotDouble() {
    assertEquals(
      0.0,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", true)
            )
        )
      ).optLeafAsDouble("/field2/innerField").get().doubleValue()
    );
  }

  @Test
  void returnsZeroIfLeafInPathIsNotDouble() {
    assertEquals(
      0.0,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", true)
            )
        )
      ).leafAsDouble("/field2/innerField")
    );
  }

  @Test
  void returnsDoubleEvenIfOptLeafInPathIsInt() {
    assertEquals(
      5.0,
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", 5)
            )
        )
      ).optLeafAsDouble("/field2/innerField").get().doubleValue()
    );
  }

  @Test
  void findsOptLeafAsBool() {
    assertTrue(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", true)
        )
      ).optLeafAsBool("field2").get()
    );
  }

  @Test
  void findsLeafAsBool() {
    assertTrue(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", true)
        )
      ).leafAsBool("field2")
    );
  }

  @Test
  void returnsEmptyOptionalForNonexistentBooleanLeaf() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
        )
      ).optLeafAsBool("nonexistent").isPresent()
    );
  }

  @Test
  void throwsForNonexistentBooleanLeaf() {
    assertTrue(
      assertThrows(
        IllegalArgumentException.class,
        () -> new SmartJson(
          new Json.Of(
            MAPPER.createObjectNode()
          )
        ).leafAsBool("nonexistent")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsFalseIfOptLeafIsNotBool() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("boolField", true)
        )
      ).optLeafAsBool("stringField").get()
    );
  }

  @Test
  void returnsFalseIfLeafIsNotBool() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("boolField", true)
        )
      ).leafAsBool("stringField")
    );
  }

  @Test
  void findsOptBoolLeafInPath() {
    assertTrue(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", true)
            )
        )
      ).optLeafAsBool("/field2/innerField").get()
    );
  }

  @Test
  void findsLeafBoolInPath() {
    assertTrue(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", true)
            )
        )
      ).leafAsBool("/field2/innerField")
    );
  }

  @Test
  void returnsEmptyOptionalForNonexistentLeafBoolInPath() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
        )
      ).optLeafAsBool("/nonexistent/path").isPresent()
    );
  }

  @Test
  void throwsForNonexistentLeafBoolInPath() {
    assertTrue(
      assertThrows(
        IllegalArgumentException.class,
        () -> new SmartJson(
          new Json.Of(
            MAPPER.createObjectNode()
          )
        ).leafAsBool("/nonexistent/path")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsFalseIfOptLeafInPathIsNotBool() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", "string")
            )
        )
      ).optLeafAsBool("/field2/innerField").get()
    );
  }

  @Test
  void returnsFalseIfLeafInPathIsNotBool() {
    assertFalse(
      new SmartJson(
        new Json.Of(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", 17)
            )
        )
      ).leafAsBool("/field2/innerField")
    );
  }
}
