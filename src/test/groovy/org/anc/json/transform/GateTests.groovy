package org.anc.json.transform

import groovy.json.JsonOutput
import org.junit.Ignore
import org.junit.Test
import org.lappsgrid.serialization.Serializer
import org.w3c.dom.Document

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMResult
import javax.xml.transform.dom.DOMSource

/**
 * @author Keith Suderman
 */
class GateTests {

    @Ignore
    void generateGateJson() {
        URL url = this.class.getResource('/gate.xml')
        GenericXmlToJson converter = new GenericXmlToJson()
        String json = converter.convert(url)
        println JsonOutput.prettyPrint(json)
    }

    @Ignore
    void parseGateJ2J() {
        String template = load('/gate.j2j')
        Json2Xslt xslt = new Json2Xslt()
        Document document = xslt.compile(template)
        JsonTransformer.prettyPrint(document.documentElement)
    }

    @Ignore
    void testTransform() {
        Json2Xslt xslt = new Json2Xslt()
        Document template = xslt.compile(load('/gate.j2j'))
        JsonTransformer.prettyPrint(template.documentElement)
        Document xml = loadXml('/gate.xml')
        Transformer transformer = TransformerFactory.newInstance().newTransformer(new DOMSource(template))
        DOMResult result = new DOMResult()
        transformer.transform(new DOMSource(xml), result)
        JsonTransformer.prettyPrint(result.node)

        GenericXmlToJson converter = new GenericXmlToJson()
        println converter.convert(result.node)
    }

    @Test
    void testXml2Json() {
        Json2Xslt xslt = new Json2Xslt()
        Document template = xslt.compile(load('/gate.j2j'))
        JsonTransformer.prettyPrint(template.documentElement)
        Document xml = loadXml('/gate.xml')
        Transformer transformer = JsonTransformer.createTransformer(new DOMSource(template))
        DOMResult result = new DOMResult()
        transformer.transform(new DOMSource(xml), result)
        JsonTransformer.prettyPrint(result.node)

        Object object = Xml2Json.convert(result.node)
        println Serializer.toPrettyJson(object)
    }

    Document loadXml(String resourceName) {
        URL url = this.class.getResource(resourceName)
        InputStream stream = new ByteArrayInputStream(url.text.bytes)
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        return builder.parse(stream)
    }

    String load(String resourceName) {
        return this.class.getResource(resourceName).text
    }
}
