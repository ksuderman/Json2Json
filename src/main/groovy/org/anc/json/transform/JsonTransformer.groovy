package org.anc.json.transform

import org.lappsgrid.serialization.Serializer
import org.w3c.dom.Document
import org.w3c.dom.Node

import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMResult
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * @author Keith Suderman
 */
class JsonTransformer {
    enum Output {
        JSON, XML
    }

    Transformer transformer
    Output format

    public JsonTransformer(String template, Output format = Output.JSON) {
        Document xml = new Json2Xslt().compile(template)
        Source source = new DOMSource(xml)
//        println xml
//        new File("/tmp/template.xsl").text = xml
        this.format = format
        transformer = createTransformer(source)
    }

    String transform(String json) {
        JsonToXml jsonToXml = new JsonToXml()
        Document xml = jsonToXml.convert(json)
//        println xml
//        new File("/tmp/input.xml").text = xml
//        StringWriter writer = new StringWriter()
//        StreamResult result = new StreamResult(writer)
        Source source = new DOMSource(xml)
        DOMResult result = new DOMResult()
        transformer.transform(source, result)
        if (format == Output.XML) {
            //return writer.toString()
            return JsonTransformer.toString(result.getNode())
        }
        Xml2Json converter = new Xml2Json()
        Object object = converter.convert(result.getNode())
        return Serializer.toPrettyJson(object)
//        return Json2Json.toString(document)
//        return Xml2Json.convert(writer.toString())
    }

    public static final void prettyPrint(Node xml) throws Exception {

        Transformer tf = TransformerFactory.newInstance().newTransformer()
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4")
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));
        System.out.println(out.toString());
    }

    public static final String toString(Node xml) throws Exception {

        Transformer tf = TransformerFactory.newInstance().newTransformer()
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4")
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));
        return out.toString();
    }

    public static Transformer createTransformer(String string) {
        return createTransformer(new StreamSource(new StringReader(string)))
    }

    public static Transformer createTransformer(Document document) {
        return createTransformer(new DOMSource(document.documentElement))
    }

    public static Transformer createTransformer(Source source) {
//        TransformerFactory factory = new net.sf.saxon.TransformerFactoryImpl()
        TransformerFactory factory = TransformerFactory.newInstance()
        return factory.newTransformer(source)
    }
}
