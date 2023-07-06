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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.cactoos.Text;

/**
 * JSON in XML.
 *
 * @author Aliaksei Bialiauski (abialiauski.dev@gmail.com)
 * @since 0.3.2
 */
public final class JsonXML implements Text {

  /**
   * Json.
   */
  private final Json json;
  /**
   * Root XML node.
   */
  private final String root;

  /**
   * Ctor.
   *
   * @param jsn JSON
   * @param rt  Root XML node
   */
  public JsonXML(final Json jsn, final String rt) {
    this.json = jsn;
    this.root = rt;
  }

  @Override
  public String asString() throws Exception {
    final XmlMapper xml = new XmlMapper();
    final ObjectMapper mapper = new ObjectMapper();
    final JsonNode node = mapper.readTree(
      new Jocument(
        this.json
      ).pretty()
    );
    xml.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
    return xml.writer()
      .withRootName(this.root)
      .writeValueAsString(node);
  }
}
