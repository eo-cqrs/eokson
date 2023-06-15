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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.eocqrs.eokson.Json;
import io.github.eocqrs.eokson.SmartJson;
import org.junit.jupiter.api.Test;

final class SmartJsonStringLeafTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Immediate

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

    // Path

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
}
