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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.io.InputStream;
/*
 * @todo #6:90m/DEV create eokson-matchers package
 */

/**
 * JSON Equality Expression Matcher.
 */
public final class JsonEqualTo extends BaseMatcher<Json> {

  /**
   * JSON to compare.
   */
  private final Json compare;

  /**
   * Ctor.
   *
   * @param jsn JSON to compare
   */
  public JsonEqualTo(final Json jsn) {
    this.compare = jsn;
  }

  /**
   * Ctor.
   *
   * @param jsn JSON to compare
   */
  public JsonEqualTo(final String jsn) {
    this(new Json.Of(jsn));
  }

  /**
   * Ctor.
   *
   * @param bytes Bytes
   */
  public JsonEqualTo(final InputStream bytes) {
    this(new Json.Of(bytes));
  }

  @Override
  public boolean matches(final Object json) {
    return new SmartJson(this.compare).pretty()
      .equals(json);
  }

  @Override
  public void describeTo(final Description description) {
    description.appendText(" JSON to compare: ")
      .appendValue(this.compare);
  }
}
