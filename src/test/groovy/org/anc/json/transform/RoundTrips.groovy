package org.anc.json.transform

import org.anc.json.transform.JsonToXml
import org.anc.json.transform.Xml2Json
import org.junit.Test
import org.lappsgrid.serialization.Serializer
import org.w3c.dom.Document

/**
 * Another test suite that do not assert anything. These tests just do a round trip
 * conversion and hope no exceptions are thrown.
 *
 * @author Keith Suderman
 */
class RoundTrips {
    @Test
    void testCds() {
        println "RoundTrips.testCds"
        parse('/cds.json')
    }

    @Test
    void testInput() {
        println "RoundTrips.testInput"
        parse('/input.json')
    }

    private parse(String resource) {
        URL url = this.class.getResource(resource)
        JsonToXml converter = new JsonToXml()
        Document document = converter.convert(url)
        Object object = Xml2Json.convert(document.firstChild)
        println Serializer.toPrettyJson(object)
    }

}
