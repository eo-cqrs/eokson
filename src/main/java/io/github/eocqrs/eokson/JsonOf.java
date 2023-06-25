package io.github.eocqrs.eokson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * JSON Decorator.
 *
 * @author Aliaksei Bialiauski (abialiauski.dev@gmail.com)
 * @since 0.1.1
 */
public final class JsonOf implements Json {

  /**
   * Object Mapper.
   */
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Origin.
   */
  private final Json origin;

  /**
   * Ctor.
   *
   * @param node JSON represented by {@link JsonNode} from
   *             'jackson-databind' library.
   */
  public JsonOf(final JsonNode node) {
    this(() -> node);
  }

  /**
   * Ctor.
   *
   * @param node JSON represented by {@link JsonNode} from
   *             'jackson-databind' library.
   */
  public JsonOf(final Supplier<JsonNode> node) {
    this(
      flattened(
        () -> {
          try {
            return MAPPER.writeValueAsBytes(node.get());
          } catch (final JsonProcessingException e) {
            throw new UncheckedIOException(e);
          }
        }
      )
    );
  }

  private static <T> T flattened(final Supplier<T> scalar) {
    return scalar.get();
  }

  /**
   * Ctor.
   *
   * @param string JSON represented by a {@link String}.
   */
  public JsonOf(final String string) {
    this(string.getBytes());
  }

  /**
   * Ctor.
   *
   * @param bytes JSON represented by an array of bytes.
   */
  public JsonOf(final byte[] bytes) {
    this(
      new AutoResetInputStream(
        new ByteArrayInputStream(bytes)
      )
    );
  }

  /**
   * Ctor.
   *
   * @param stream JSON represented by the bytes in an
   *               {@link InputStream}.
   */
  public JsonOf(final InputStream stream) {
    this.origin = () -> stream;
  }

  /**
   * Ctor.
   *
   * @param path Path to a JSON in a file.
   */
  public JsonOf(final Path path) {
    this(
      new Cached<>(
        () -> new Unchecked<>(
          () -> new AutoResetInputStream(
            new ByteArrayInputStream(Files.readAllBytes(path))
          )
        ).value()
      )
    );
  }

  private JsonOf(final Cached<InputStream> cached) {
    this(cached::value);
  }

  private JsonOf(final Json json) {
    this.origin = json;
  }

  @Override
  public InputStream bytes() {
    return this.origin.bytes();
  }

  @Override
  public String toString() {
    return new String(new ByteArray(this).value());
  }
}
