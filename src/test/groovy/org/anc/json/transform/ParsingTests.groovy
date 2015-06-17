package org.anc.json.transform

import org.junit.Test
import org.w3c.dom.Document

/**
 * These tests don't actually assert anything.  The various test scripts are
 * run the parser and any exceptions thrown are considered to be failures.
 *
 * @author Keith Suderman
 */
class ParsingTests {

    @Test
    void testNewJson2Xml() {
        println "ParsingTests.testNewJson2Xml"
        URL url = this.class.getResource('/input.json')
        JsonToXml converter = new JsonToXml()
        Document document = converter.convert(url)
        JsonTransformer.prettyPrint(document)
    }

    @Test
    void testParseCds() {
        println "ParsingTests.testParseCds"
        URL url = this.class.getResource('/cds.json')
        JsonToXml converter = new JsonToXml()
        Document document = converter.convert(url)
        JsonTransformer.prettyPrint(document)
    }

    @Test
    void testParseTable() {
        println "ParsingTests.testParseTable"
        parseXslt('/table.j2j')
    }

    @Test
    void testParseRename() {
        println "ParsingTests.testParseRename"
        parseXslt('/rename.j2j')
    }

    @Test
    void testParsecds3() {
        println "ParsingTests.testParsecds3"
        parseXslt('/cds3.j2j')
    }

    void parseXslt(String name) {
        parseXslt(this.class.getResource(name))
    }

    void parseXslt(URL url) {
        Json2Xslt xslt = new Json2Xslt()
        Document document = xslt.compile(url)
        JsonTransformer.prettyPrint(document)
    }
}
