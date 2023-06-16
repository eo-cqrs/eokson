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
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MutableJsonTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void createsOneField() {
    new JsonEqualTo(
      new Json.Of(
        MAPPER.createObjectNode()
          .put("field1", "value1")
      )
    ).matches(
      new MutableJson()
        .with("field1", "value1")
    );
  }

  @Test
  void createsOneAndThenAnotherField() {
    MutableJson json = new MutableJson().with("field1", "value1");
    json.with("field2", 9.9);
    new JsonEqualTo(
      new Json.Of(
        MAPPER.createObjectNode()
          .put("field1", "value1")
          .put("field2", 9.9)
      )
    ).matches(
      json.bytes()
    );
  }

  @Test
  void createsDeepJson() throws URISyntaxException {
    new JsonEqualTo(
      new SmartJson(
        new Json.Of(
          Paths.get(
            MutableJsonTest.class.getClassLoader().getResource(
              "deep-noarray.json"
            ).toURI()
          )
        )
      ).pretty()
    ).matches(
      new SmartJson(
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
    );
  }

  @Test
  void buildsOnBase() {
    new JsonEqualTo(
      new MutableJson().with(
        "ocean",
        new MutableJson()
          .with("character", "stormy")
      ).with("nereid", new Empty())
    ).matches(
      new MutableJson(
        new MutableJson().with(
          "ocean",
          new MutableJson()
            .with("character", "stormy")
        )
      ).with("nereid", new Empty())
    );
  }

  @Test
  void toStringOnEmpty() {
    assertEquals(
      "{}",
      new MutableJson().toString()
    );
  }
}
