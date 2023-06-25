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

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class JsonEnvelopeTest {

  @Test
  void bytesDoesNotThrowException() {
    Assertions.assertDoesNotThrow(
      () ->
        new JsonEnvelopeTest.TestJsonEnvelope(
          new JsonOf("{}")
        ).bytes()
    );
  }

  @Test
  void bytesAreNotNull() {
    MatcherAssert.assertThat(
      "JSON bytes is not NULL",
      new JsonEnvelopeTest.TestJsonEnvelope(
        new JsonOf("{}")
      ).bytes(),
      Matchers.notNullValue()
    );
  }

  @Test
  void readsJsonWithEnvelopeInRightFormat() {
    final String value = "{\"number\": \"12\"}";
    MatcherAssert.assertThat(
      "JSON in right format",
      new JsonOf(value).toString(),
      Matchers.equalTo(
        new JsonEnvelopeTest.TestJsonEnvelope(
          new JsonOf(
            value
          )
        ).toString()
      )
    );
  }

  private static final class TestJsonEnvelope extends JsonEnvelope {
    TestJsonEnvelope(final Json origin) {
      super(origin);
    }
  }
}
