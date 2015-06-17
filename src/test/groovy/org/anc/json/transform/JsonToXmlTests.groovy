package org.anc.json.transform

import org.junit.Test
import org.lappsgrid.serialization.Serializer
import org.w3c.dom.Document

/**
 * @author Keith Suderman
 */
class JsonToXmlTests {

    @Test
    void inputToXml() {
        URL url = this.class.getResource('/input.json')
        JsonToXml converter = new JsonToXml()
        Document xml = converter.convert(url)
        JsonTransformer.prettyPrint(xml)

        Object object = Xml2Json.convert(xml)
        println Serializer.toPrettyJson(object)
    }

}
