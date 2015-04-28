package org.lappsgrid.prototype.json2json

import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * @author Keith Suderman
 */
class Json2Json {
    enum Output {
        JSON, XML
    }

    Transformer transformer
    Output format

    public Json2Json(String template, Output format = Output.JSON) {
        String xml = new Json2Xslt().compile(template)
//        println xml
//        new File("/tmp/template.xsl").text = xml
        this.format = format
        transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(xml)))
    }

    String transform(String json) {
        def xml = Json2Xml.convert(json)
//        println xml
//        new File("/tmp/input.xml").text = xml
        StringWriter writer = new StringWriter()
        StreamResult result = new StreamResult(writer)
        transformer.transform(new StreamSource(new StringReader(xml)), result)
        if (format == Output.XML) {
            return writer.toString()
        }
        return Xml2Json.convert(writer.toString())
    }
}
