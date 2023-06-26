package io.github.eocqrs.eokson;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ByteArray}.
 *
 * @author Aliaksei Bialiauski (abialiauski.dev@gmail.com)
 * @since 0.3.5
 */
final class ByteArrayTest {

  @Test
  void readsBytesInRightFormat() {
    final byte[] bytes = "{\"field1\":\"value1\",\"field2\":\"value2\"}"
      .getBytes();
    MatcherAssert.assertThat(
      "Bytes in right format",
      new ByteArray(
        new SmartJson(
          new JsonOf(bytes)
        )
      ).value(),
      Matchers.equalTo(bytes)
    );
  }
}
