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
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    final String leaf = "value2";
    MatcherAssert.assertThat(
      "Leaf in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", leaf)
        )
      ).optLeaf("field2").get(),
      new IsEqual<>(leaf)
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
    Assertions.assertTrue(
      Assertions.assertThrows(
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
    Assertions.assertTrue(
      Assertions.assertThrows(
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
    MatcherAssert.assertThat(
      "Leaf in right format",
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
      ).optLeaf("/field2/innerField").get(),
      new IsEqual<>("innerValue")
    );
  }

  @Test
  void findsLeafInPath() {
    MatcherAssert.assertThat(
      "Leaf in right format",
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
      ).leaf("/field2/innerField"),
      new IsEqual<>(
        "innerValue"
      )
    );
  }

  @Test
  void returnsEmptyOnNonexistentLeafInPath() {
    MatcherAssert.assertThat(
      "Empty optional on non existing",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
        )
      ).optLeaf("/nonexistent/path").isPresent(),
      new IsEqual<>(false)
    );
  }

  @Test
  void throwsOnNonexistentLeafInPath() {
    Assertions.assertTrue(
      Assertions.assertThrows(
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
  void returnsEmptyOnNonTextLeaf() {
    MatcherAssert.assertThat(
      "Empty optional on non text leaf",
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
      ).optLeaf("/field2/innerField").isPresent(),
      new IsEqual<>(false)
    );
  }

  @Test
  void throwsOnNonTextLeaf() {
    Assertions.assertTrue(
      Assertions.assertThrows(
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
  void findsOptIntLeaf() {
    final int value = 14;
    MatcherAssert.assertThat(
      "Int leaf in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", value)
        )
      ).optLeafAsInt("field2").get().intValue(),
      new IsEqual<>(value)
    );
  }

  @Test
  void findsIntLeaf() {
    final int value = 14;
    MatcherAssert.assertThat(
      "Int leaf in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", value)
        )
      ).leafAsInt("field2"),
      new IsEqual<>(value)
    );
  }

  @Test
  void returnsEmptyOnNonexistentIntLeaf() {
    MatcherAssert.assertThat(
      "Empty on non existing",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
        )
      ).optLeafAsInt("nonexistent").isPresent(),
      new IsEqual<>(false)
    );
  }

  @Test
  void throwsForNonexistentIntLeaf() {
    Assertions.assertTrue(
      Assertions.assertThrows(
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
  void returnsZeroOnOptNonIntLeaf() {
    MatcherAssert.assertThat(
      "Returns zero on non int leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("intField", 5)
        )
      ).optLeafAsInt("stringField").get().intValue(),
      new IsEqual<>(0)
    );
  }

  @Test
  void returnsZeroOnNonIntLeaf() {
    MatcherAssert.assertThat(
      "Returns zero on non int leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("intField", 5)
        )
      ).leafAsInt("stringField"),
      new IsEqual<>(0)
    );
  }

  @Test
  void returnsIntOnOptDoubleLeaf() {
    MatcherAssert.assertThat(
      "Returns int on optional double leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("doubleField", 5.9)
        )
      ).optLeafAsInt("doubleField").get().intValue(),
      new IsEqual<>(5)
    );
  }

  @Test
  void returnsIntOnDoubleLeaf() {
    MatcherAssert.assertThat(
      "Returns int on double leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("doubleField", 5.9)
        )
      ).leafAsInt("doubleField"),
      new IsEqual<>(5)
    );
  }

  @Test
  void findsOptIntLeafInPath() {
    final int value = 999;
    MatcherAssert.assertThat(
      "Int leaf in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", value)
            )
        )
      ).optLeafAsInt("/field2/innerField").get().intValue(),
      new IsEqual<>(value)
    );
  }

  @Test
  void findsLeafIntInPath() {
    final int value = 12;
    MatcherAssert.assertThat(
      "Int leaf in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", value)
            )
        )
      ).leafAsInt("/field2/innerField"),
      new IsEqual<>(value)
    );
  }

  @Test
  void returnsEmptyOnNonexistentIntLeafInPath() {
    MatcherAssert.assertThat(
      "Empty on non existing Int Leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
        )
      ).optLeafAsInt("/nonexistent/path").isPresent(),
      new IsEqual<>(false)
    );
  }

  @Test
  void throwsForNonexistentLeafIntInPath() {
    Assertions.assertTrue(
      Assertions.assertThrows(
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
  void returnsZeroOnOptLeafInPathIsNotInt() {
    MatcherAssert.assertThat(
      "Returns zero on optional non int leaf in path",
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
      ).optLeafAsInt("/field2/innerField").get().intValue(),
      new IsEqual<>(0)
    );
  }

  @Test
  void returnsZeroOnNonIntLeafInPath() {
    MatcherAssert.assertThat(
      "Returns zero on non int leaf in path",
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
      ).leafAsInt("/field2/innerField"),
      new IsEqual<>(0)
    );
  }

  @Test
  void returnsIntOnOptLeafInPathIsDouble() {
    MatcherAssert.assertThat(
      "Returns Int on optional leaf in path as double",
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
      ).optLeafAsInt("/field2/innerField").get().intValue(),
      new IsEqual<>(5)
    );
  }

  @Test
  void findsOptLeafAsDouble() {
    final double value = 14.9;
    MatcherAssert.assertThat(
      "Optional leaf in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", value)
            .put("field2", "value2")
        )
      ).optLeafAsDouble("field1").get().doubleValue(),
      new IsEqual<>(value)
    );
  }

  @Test
  void findsLeafAsDouble() {
    final double value = 14.9;
    MatcherAssert.assertThat(
      "Double leaf in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", value)
            .put("field2", "value2")
        )
      ).leafAsDouble("field1"),
      new IsEqual<>(value)
    );
  }

  @Test
  void returnsEmptyOnNonexistentDoubleLeaf() {
    MatcherAssert.assertThat(
      "Empty on non existing double leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
        )
      ).optLeafAsDouble("nonexistent").isPresent(),
      new IsEqual<>(false)
    );
  }

  @Test
  void throwsForNonexistentDoubleLeaf() {
    Assertions.assertTrue(
      Assertions.assertThrows(
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
    MatcherAssert.assertThat(
      "Returns Zero on optional non double leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("doubleField", 5.9)
        )
      ).optLeafAsDouble("stringField").get().doubleValue(),
      new IsEqual<>(0.0)
    );
  }

  @Test
  void returnsZeroIfLeafIsNotDouble() {
    MatcherAssert.assertThat(
      "Returns zero on non double leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("doubleField", 5.9)
        )
      ).leafAsDouble("stringField"),
      new IsEqual<>(0.0)
    );
  }

  @Test
  void returnsDoubleIfOptLeafIsInt() {
    MatcherAssert.assertThat(
      "Returns optional double on int leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("intField", 5)
        )
      ).optLeafAsDouble("intField").get().doubleValue(),
      new IsEqual<>(5.0)
    );
  }

  @Test
  void returnsDoubleOnIfLeafIsInt() {
    MatcherAssert.assertThat(
      "Returns double on int leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("intField", 5)
        )
      ).leafAsDouble("intField"),
      new IsEqual<>(5.0)
    );
  }

  @Test
  void findsOptDoubleLeafInPath() {
    final double value = 999.17;
    MatcherAssert.assertThat(
      "Double leaf in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", value)
            )
        )
      ).optLeafAsDouble("/field2/innerField").get().doubleValue(),
      new IsEqual<>(value)
    );
  }

  @Test
  void findsLeafDoubleInPath() {
    final double value = 12.45;
    MatcherAssert.assertThat(
      "Double leaf in path in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .<ObjectNode>set(
              "field2",
              MAPPER.createObjectNode()
                .put("innerField", value)
            )
        )
      ).leafAsDouble("/field2/innerField"),
      new IsEqual<>(value)
    );
  }

  @Test
  void returnsEmptyOnNonexistentLeafDoubleInPath() {
    MatcherAssert.assertThat(
      "Empty on non existing leaf in path",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
        )
      ).optLeafAsDouble("/nonexistent/path").isPresent(),
      new IsEqual<>(false)
    );
  }

  @Test
  void throwsForNonexistentLeafDoubleInPath() {
    Assertions.assertTrue(
      Assertions.assertThrows(
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
  void returnsZeroOnOptLeafInPathIsNotDouble() {
    MatcherAssert.assertThat(
      "Returns zero on optional non double leaf in path",
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
      ).optLeafAsDouble("/field2/innerField").get().doubleValue(),
      new IsEqual<>(0.0)
    );
  }

  @Test
  void returnsZeroOnLeafInPathIsNotDouble() {
    MatcherAssert.assertThat(
      "Returns zero on non double leaf in path",
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
      ).leafAsDouble("/field2/innerField"),
      new IsEqual<>(0.0)
    );
  }

  @Test
  void returnsDoubleOnOptLeafInPathIsInt() {
    MatcherAssert.assertThat(
      "Returns double on int leaf in path",
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
      ).optLeafAsDouble("/field2/innerField").get().doubleValue(),
      new IsEqual<>(5.0)
    );
  }

  @Test
  void findsOptLeafAsBool() {
    MatcherAssert.assertThat(
      "Boolean leaf in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put("field2", true)
        )
      ).optLeafAsBool("field2").get(),
      new IsEqual<>(true)
    );
  }

  @Test
  void findsLeafAsBool() {
    final String field = "field2";
    MatcherAssert.assertThat(
      "Boolean leaf in right format",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("field1", "value1")
            .put(field, true)
        )
      ).leafAsBool(field),
      new IsEqual<>(true)
    );
  }

  @Test
  void returnsEmptyOnNonexistentBooleanLeaf() {
    MatcherAssert.assertThat(
      "Empty on non existing boolean leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
        )
      ).optLeafAsBool("nonexistent").isPresent(),
      new IsEqual<>(false)
    );
  }

  @Test
  void throwsForNonexistentBooleanLeaf() {
    Assertions.assertTrue(
      Assertions.assertThrows(
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
  void returnsFalseOnOptLeafIsNotBool() {
    MatcherAssert.assertThat(
      "False on non boolean leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("boolField", true)
        )
      ).optLeafAsBool("stringField").get(),
      new IsEqual<>(false)
    );
  }

  @Test
  void returnsFalseOnLeafIsNotBool() {
    MatcherAssert.assertThat(
      "False on non boolean leaf",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
            .put("stringField", "value1")
            .put("boolField", true)
        )
      ).leafAsBool("stringField"),
      new IsEqual<>(false)
    );
  }

  @Test
  void findsOptBoolLeafInPath() {
    MatcherAssert.assertThat(
      "Boolean leaf in path in right format",
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
      ).optLeafAsBool("/field2/innerField").get(),
      new IsEqual<>(true)
    );
  }

  @Test
  void findsLeafBoolInPath() {
    MatcherAssert.assertThat(
      "Boolean leaf in path in right format",
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
      ).leafAsBool("/field2/innerField"),
      new IsEqual<>(true)
    );
  }

  @Test
  void returnsEmptyOnNonexistentLeafBoolInPath() {
    MatcherAssert.assertThat(
      "Empty on non existing boolean in path",
      new Jocument(
        new JsonOf(
          MAPPER.createObjectNode()
        )
      ).optLeafAsBool("/nonexistent/path").isPresent(),
      new IsEqual<>(false)
    );
  }

  @Test
  void throwsForNonexistentLeafBoolInPath() {
    Assertions.assertTrue(
      Assertions.assertThrows(
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
    MatcherAssert.assertThat(
      "False on leaf is not boolean in path",
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
      ).optLeafAsBool("/field2/innerField").get(),
      new IsEqual<>(false)
    );
  }

  @Test
  void returnsFalseIfLeafInPathIsNotBool() {
    MatcherAssert.assertThat(
      "False on leaf is not boolean in path",
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
      ).leafAsBool("/field2/innerField"),
      new IsEqual<>(false)
    );
  }
}
