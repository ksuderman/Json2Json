package org.lappsgrid.prototype.json2json

import groovy.xml.QName
import org.lappsgrid.prototype.json2json.error.XmlException
import org.lappsgrid.serialization.Serializer

/**
 * @author Keith Suderman
 */
class Xml2Json {

    static String convert(String input) {
        def xml = new XmlParser().parse(new StringReader(input))
        return Serializer.toPrettyJson(convert(xml))
    }

    static String getName(Node node) {
        Object name = node.name()
        if (name instanceof String) {
            return ((String) name).replace("at_", "@")
        }
        if (name instanceof QName) {
            QName qname = (QName) name
            String ref = node.attributes()['ref']
            if (ref) {
                return "${qname.namespaceURI}/${qname.localPart}#$ref"
            }
            return "${qname.namespaceURI}/${qname.localPart}"
        }
        return name.toString()
    }

    static Object convert(Node node) {
//        String name = node.name().toString()
        String type = node.attributes()['kind']
        if (type == 'map') {
            Map map = [:]
//            tree[name] = map

            node.children().each { Node child ->
                String name = getName(child) //child.name().toString().replace("at_", "@")
                map[name] = convert(child)
            }
            return map
        }
        if (type == 'list') {
            List list = []
            node.children().each { Node child ->
                // The children are the list:item elements inserted by
                // the Json2Xml converter.  We need to gather those as
                // the list elements
//                String childType = child.attributes()['type']
//                child.children().each {
//                    list << convert(it)
//                }
                list << convert(child)
            }
            return list
        }
        // TODO Ensure type=='val'
//        if (type != 'val') {
//            println node.toString()
//            throw new XmlException("Unsupported type: " + type + " on node " + getName(node))
//        }
        return node.children()[0].toString()
    }
}
