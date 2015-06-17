package org.anc.json.transform

import groovy.xml.QName
import groovy.xml.dom.DOMCategory
import org.lappsgrid.serialization.Serializer
import org.w3c.dom.Document
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.Element
import org.w3c.dom.NodeList


/**
 * @author Keith Suderman
 */
class GenericXmlToJson {

    String convert(File file) {
        convert(file.text)
    }
    String convert(URL url) {
        convert(url.text)
    }

    String convert(String xml) {
        Node node = new XmlParser().parseText(xml)
        Map map = transform(node)
        return Serializer.toPrettyJson(map)
    }

    String convert(Document document) {
        return convert(document.documentElement)
    }

    String convert(Node node) {
        Map map = transform(node)
        return Serializer.toPrettyJson(map)
    }

    Map transform(org.w3c.dom.Node node) {
//        println "Converting ${node.name()}"
        Map map = [:]
        Element element = (Element) node
        NamedNodeMap atts = element.attributes
        for (int i = 0; i < atts.length; ++i) {
            Node att = atts.item(i)
            String name = '@' + att.localName
            String value = att.nodeValue
            map[name] = value
        }

        String name = element.localName
        NodeList children = element.childNodes
        if (children.length == 1) {
            Node child = children.item(0)
            if (child.nodeType == Node.TEXT_NODE) {
                map[name] = child.nodeValue
            }
            else {
                map[name] = transform(child)
            }
        }
        else {
            List list = []
            for(int i = 0; i <children.length; ++i) {
                Node child = children.item(i)
                list << transform(child)
            }
            map[name] = list
        }
        return map
    }

    Map transform(groovy.util.Node node) {
//        println "Converting ${node.name()}"
        Map map = [:]
        node.attributes().each { name,value ->
            map['@' + name] = value
        }
        String name = node.name()
        NodeList children = node.children()
        if (children.size() == 1) {
            def child = children[0]
            if (child instanceof String) {
                map[name] = child
            }
            else {
                map[name] = transform(child)
            }
        }
        else {
            List list = []
            node.children().each { child ->
                list << transform(child)
            }
            map[name] = list
        }
        return map
    }

    String transform(String input) {
        return input
    }
}
