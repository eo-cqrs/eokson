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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link Jocument}.
 *
 * @author Aliaksei Bialiauski (abialiauski.dev@gmail.com)
 * @since 0.0.0
 */
final class JocumentTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private Path deep;

  @BeforeEach
  void setUp() throws URISyntaxException {
    this.deep = Paths.get(
      JocumentTest.class.getClassLoader()
        .getResource("deep.json")
        .toURI()
    );
  }

  @Test
  void readsAsTextInRightFormat() {
    MatcherAssert.assertThat(
      "JSON as text in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", "value2")
        )
      ).textual(),
      Matchers.equalTo(
        "{\"field1\":\"value1\","
          + "\"field2\":\"value2\"}"
      )
    );
  }

  @Test
  void readsAsPrettyInRightFormat() {
    MatcherAssert.assertThat(
      "JSON as pretty text in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", "value2")
        )
      ).pretty(),
      Matchers.equalTo(
        '{' + System.lineSeparator()
          + "  \"field1\" : \"value1\"," + System.lineSeparator()
          + "  \"field2\" : \"value2\"" + System.lineSeparator()
          + '}'
      )
    );
  }

  @Test
  void readsByteArray() throws JsonProcessingException {
    final byte[] bytes = MAPPER.writeValueAsBytes(
      MAPPER.createObjectNode()
        .put("field1", "value1")
        .put("field2", "value2")
    );
    MatcherAssert.assertThat(
      "Reads byte array",
      new Jocument(
        new JsonOf(bytes)
      ).byteArray(),
      Matchers.equalTo(
        bytes
      )
    );
  }

  @Test
  void readsAsObjectNode() {
    final ObjectNode node = MAPPER.createObjectNode()
      .put("field1", "value1")
      .put("field2", "value2");
    MatcherAssert.assertThat(
      "Reads Object Node",
      new Jocument(
        new JsonOf(
          node
        )
      ).objectNode(),
      Matchers.equalTo(node)
    );
  }

  @Test
  void readsAsLeaf() {
    MatcherAssert.assertThat(
      "Reads as JSON leaf",
      new Jocument(
        new JsonOf(this.deep)
      ).at("/ocean/rock1/nereid2").leaf("hair"),
      Matchers.equalTo("red")
    );
  }

  @Test
  void handlesMissingDataAtLeaf() {
    MatcherAssert.assertThat(
      "Handles missing data at leaf",
      new Jocument(
        new JsonOf(this.deep)
      ).at("/ocean/nothing")
        .isMissing(),
      Matchers.equalTo(true)
    );
  }

  @Test
  void readsLeafInArray() {
    MatcherAssert.assertThat(
      "Leaf in array in right format",
      new Jocument(
        new JsonOf(this.deep)
      ).at("/ocean/rock1/nereid1/associates/0")
        .leaf("name"),
      Matchers.equalTo("Jason")
    );
  }

  @Test
  void readsArraysInRightFormat() {
    MatcherAssert.assertThat(
      "JSON in right format",
      new Jocument(
        new JsonOf(
          this.deep
        )
      ).at("/ocean/rock1/nereid1/associates/0").textual(),
      Matchers.equalTo("{\"name\":\"Jason\"}")
    );
  }

  @Test
  void readsAmazon() throws URISyntaxException {
    MatcherAssert.assertThat(
      "JSON in right format",
      new Jocument(
        new JsonOf(
          Paths.get(
            JocumentTest.class.getClassLoader()
              .getResource("amazon.json")
              .toURI()
          )
        )
      ).at("/amazon/shop/books/0").textual(),
      Matchers.equalTo("{\"name\":\"Code Complete\",\"price\":30}")
    );
  }

  /*
  @todo #6:90m/DEV array leafing test doesn't work
   */
  @Disabled
  @Test
  void leafsArrays() {
    final String array = "[{\"name\":\"Jason\"},{\"name\":\"Thetis\"}]";
    assertEquals(
      "Jason",
      new Jocument(
        new JsonOf(this.deep)
      ).at("/ocean/rock1/nereid1/associates").at("/0").leaf("name")
    );
  }

  @Test
  void knowsIfMissing() {
    MatcherAssert.assertThat(
      "Missing in right format",
      new Jocument(new Missing()).isMissing(),
      Matchers.equalTo(true)
    );
  }

  @Test
  void knowsEmptyJsonIsNotMissing() {
    MatcherAssert.assertThat(
      "Empty JSON is not missing",
      new Jocument(
        new JsonOf("{}")
      ).isMissing(),
      Matchers.equalTo(false)
    );
  }

  @Test
  void stringifiesInRightFormat() {
    final String data = "test";
    MatcherAssert.assertThat(
      "Stringifies in right format",
      new Jocument(
        new JsonOf(data)
      ).toString(),
      Matchers.equalTo(data)
    );
  }

  @Test
  void readsTwice() {
    final Jocument json = new Jocument(
      new JsonOf("{\"field1\":\"value1\",\"field2\":\"value2\"}")
    );
    MatcherAssert.assertThat(
      "Reads first time in right format",
      json.leaf("field1"),
      Matchers.equalTo("value1")
    );
    MatcherAssert.assertThat(
      "Reads second time in right format",
      json.leaf("field1"),
      Matchers.equalTo("value1")
    );
  }

  @Test
  void findsOptLeaf() {
    assertEquals(
      "value2",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", "value2")
        )
      ).optLeaf("field2").get()
    );
  }

  @Test
  void leafsInRightFormat() {
    MatcherAssert.assertThat(
      "Leafs in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", "value2")
        )
      ).leaf("field2"),
      Matchers.equalTo("value2")
    );
  }

  @Test
  void returnsEmptyOnEmptyLeaf() {
    MatcherAssert.assertThat(
      "Returns False on empty leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
        )
      ).optLeaf("doesnotexists")
        .isPresent(),
      Matchers.equalTo(false)
    );
  }

  @Test
  void throwsOnNonexistentLeaf() {
    assertTrue(
      assertThrows(
        IllegalArgumentException.class,
        () -> new Jocument(
          new JsonOf(
            MAPPER.createObjectNode()
          )
        ).leaf("nonexistent")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsEmptyOnLeafIsNotString() {
    final String field = "intField";
    MatcherAssert.assertThat(
      "Returns empty on leaf is not string",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("intField", 5)
        )
      ).optLeaf(field).isPresent(),
      Matchers.equalTo(false)
    );
  }

  @Test
  void throwsOnLeafIsNotString() {
    final String field = "intField";
    assertTrue(
      assertThrows(
        IllegalArgumentException.class,
        () -> new Jocument(
          new JsonOf(
            MAPPER.createObjectNode()
              .put("field1", "value1")
              .put(field, 5)
          )
        ).leaf(field)
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsEmptyOnEmptyString() {
    MatcherAssert.assertThat(
      "Returns empty on empty string",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("intField", 5)
        )
      ).optLeaf("").isPresent(),
      Matchers.equalTo(false)
    );
  }

  @Test
  void findsOptLeafInPath() {
    assertEquals(
      "innerValue",
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
        () -> new Jocument(
          new JsonOf(
            MAPPER.createObjectNode()
          )
        ).leaf("/nonexistent/path")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsEmptyOptionalIfLeafInPathIsNotString() {
    assertFalse(
      new Jocument(
        new JsonOf(
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
        () -> new Jocument(
          new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
        () -> new Jocument(
          new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
        () -> new Jocument(
          new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
        () -> new Jocument(
          new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
        () -> new Jocument(
          new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
        () -> new Jocument(
          new JsonOf(
            MAPPER.createObjectNode()
          )
        ).leafAsBool("nonexistent")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsFalseIfOptLeafIsNotBool() {
    assertFalse(
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
        () -> new Jocument(
          new JsonOf(
            MAPPER.createObjectNode()
          )
        ).leafAsBool("/nonexistent/path")
      ).getMessage().contains("No such field")
    );
  }

  @Test
  void returnsFalseIfOptLeafInPathIsNotBool() {
    assertFalse(
      new Jocument(
        new JsonOf(
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
      new Jocument(
        new JsonOf(
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
