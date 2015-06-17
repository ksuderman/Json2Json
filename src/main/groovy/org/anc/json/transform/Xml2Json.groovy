package org.anc.json.transform

import groovy.xml.QName
import groovy.xml.dom.DOMCategory
import org.anc.json.transform.error.XmlException
import org.lappsgrid.serialization.Serializer
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Keith Suderman
 */
class Xml2Json {

    String convert(URL url) {
        return convert(url.text)
    }

    String convert(String input) {
//        def xml = new XmlParser().parse(new StringReader(input))
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        StringReader reader = new StringReader(input)
        InputStream stream = new ByteArrayInputStream(input.bytes)

        Document document = builder.parse(stream)
        return Serializer.toPrettyJson(convert(document))
    }

    protected String getName(Element node) {
        String name = node.tagName
        if (!name) {
            throw new XmlException("No name for element $node")
        }
        if (name.startsWith('at_')) {
            name = '@' + name.substring(3)
        }
        if (node.namespaceURI && !node.namespaceURI.startsWith(JsonToXml.ANON_NAMESPACE)) {
            int colon = name.indexOf(':')
            if (colon > 0) {
                name = name.substring(colon+1)
            }
            name = node.namespaceURI + '/' + name
            String ref = node.getAttribute("ref")
            if (ref) {
                name = name = '#' + ref
            }
        }
        return name
    }

//    static String _getName(Node node) {
//        Object name = node.name()
//        if (name instanceof String) {
//            return ((String) name).replace("at_", "@")
//        }
//        if (name instanceof QName) {
//            QName qname = (QName) name
//            String ref = node.attributes()['ref']
//            if (ref) {
//                return "${qname.namespaceURI}/${qname.localPart}#$ref"
//            }
//            return "${qname.namespaceURI}/${qname.localPart}"
//        }
//        return name.toString()
//    }

    Object convert(Document document) {
        convert(document.documentElement)
    }

    Object convert(Node node) {
        Element element = (Element) node
        String type = element.getAttribute('kind')
        if (type == 'map') {
            Map map = [:]
            node.childNodes.each { Node child ->
                String name = getName(child)
                map[name] = convert(child)
            }
            return map
        }

        if (type == 'list') {
            List list = []
            node.childNodes.each {
                list << convert(it)
            }
            return list
        }

        String className = element.getAttribute('type')
        String lcName = className.toLowerCase()
        String content = element.textContent
        if (className == null || lcName == 'string' || className == String.class.name) {
            return content
        }
        if (className == Integer.class.name || lcName == 'integer' || lcName == 'int') {
            return Integer.parseInt(content)
        }
        if (className == Float.class.name || lcName == 'float') {
            return Float.parseFloat(content)
        }
        return content
    }

//    static Object _convert(Node node) {
////        String name = node.name().toString()
//
//        use (DOMCategory) {
//            String type = node.attributes()['kind']
//            if (type == 'map') {
//                Map map = [:]
////            tree[name] = map
//
//                node.children().each { Node child ->
//                    String name = getName(child) //child.name().toString().replace("at_", "@")
//                    map[name] = convert(child)
//                }
//                return map
//            }
//            if (type == 'list') {
//                List list = []
//                node.children().each { Node child ->
//                    // The children are the list:item elements inserted by
//                    // the Json2Xml converter.  We need to gather those as
//                    // the list elements
////                String childType = child.attributes()['type']
////                child.children().each {
////                    list << convert(it)
////                }
//                    list << convert(child)
//                }
//                return list
//            }
//            // TODO Ensure type=='val'
////        if (type != 'val') {
////            println node.toString()
////            throw new XmlException("Unsupported type: " + type + " on node " + getName(node))
////        }
//            return node.children()[0].toString()
//        }
//    }
}
