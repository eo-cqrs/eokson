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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * Test case for {@link MutableJson}.
 *
 * @author Aliaksei Bialiauski (abialiauski.dev@gmail.com)
 * @since 0.0.0
 */
final class MutableJsonTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void addsOneFieldAndReadsInRightFormat() {
    final String name = "field1";
    final String value = "value1";
    MatcherAssert.assertThat(
      "JSON in right format",
      new MutableJson()
        .with(name, value)
        .toString(),
      Matchers.equalTo(
        MAPPER.createObjectNode()
          .put(name, value)
          .toString()
      )
    );
  }

  @Test
  void addsFieldsAndReadsInRightFormat() {
    final String name = "field1";
    final String second = "field2";
    final String value = "value1";
    final double secondValue = 9.9;
    final MutableJson json =
      new MutableJson().with(name, value);
    json.with(second, 9.9);
    MatcherAssert.assertThat(
      "JSON in right format",
      json.toString(),
      Matchers.equalTo(
        MAPPER.createObjectNode()
          .put(name, value)
          .put(second, secondValue)
          .toString()
      )
    );
  }

  @Test
  void readJsonFromFileInRightFormat() throws URISyntaxException {
    MatcherAssert.assertThat(
      "JSON from file in right format",
      new Jocument(
        new JsonOf(
          Paths.get(
            MutableJsonTest.class.getClassLoader().getResource(
              "deep-noarray.json"
            ).toURI()
          )
        )
      ).pretty(),
      Matchers.equalTo(
        new Jocument(
          new MutableJson().with(
            "ocean",
            new MutableJson().with(
              "rock1",
              new MutableJson().with(
                "nereid1",
                new MutableJson()
                  .with("hair", "black")
                  .with("age", 100)
              ).with(
                "nereid2",
                new MutableJson()
                  .with("hair", "red")
                  .with("age", 77.5)
              )).with(
              "rock2",
              new MutableJson().with(
                "nereid3",
                new MutableJson()
                  .with("hair", "blonde")
                  .with("age", 88)
                  .with("fair", true)
              )
            )
          )
        ).pretty()
      )
    );
  }

  @Test
  void equalsMutableJsons() {
    MatcherAssert.assertThat(
      "Based JSONs are equal",
      new MutableJson().with(
          "ocean",
          new MutableJson()
            .with("character", "stormy")
        ).with("nereid", new Empty())
        .toString(),
      Matchers.equalTo(
        new MutableJson(
          new MutableJson().with(
            "ocean",
            new MutableJson()
              .with("character", "stormy")
          )
        ).with("nereid", new Empty())
          .toString()
      )
    );
  }

  @Test
  void readsEmptyJsonInRightFormat() {
    MatcherAssert.assertThat(
      "Empty JSON in right format",
      new MutableJson().toString(),
      Matchers.equalTo("{}")
    );
  }
}
