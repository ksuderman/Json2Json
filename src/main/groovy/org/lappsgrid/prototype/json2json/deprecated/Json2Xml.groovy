package org.lappsgrid.prototype.json2json.deprecated

import groovy.json.JsonSlurper
import org.w3c.dom.Document

/**
 * @author Keith Suderman
 * @deprecated
 */
class Json2Xml {
    private Json2XMl() {}

    static Document convert(URL url) {
        return convert(new JsonSlurper().parse(url))
    }

    static Document convert(File file) {
        return convert(new JsonSlurper().parse(file))
    }

    static Document convert(String json) {
        return convert(new JsonSlurper().parseText(json))
    }

    static Document convert(Object object) {
//        StringWriter writer = new StringWriter()
        DOMBuilder builder = new DOMBuilder()
        String base = 'http://www.lappsgrid.org/ns/json2json'
        def namespaces = [
//                'xmlns:lapps':base,
//                'xmlns:map':"$base/map",
//                'xmlns:list':"$base/list",
                kind:'map'
        ]
        builder.'container'(namespaces) {
            convert(object, "", builder)
        }
//        return writer.toString()
        return builder.document
    }

    static void convert(Object object, String parent, DOMBuilder builder) {
        println "Converting object $object"
        //builder.mkp.yield(object.toString())
        builder.text(object.toString())
    }

    static void convert(List list, String parent, DOMBuilder builder) {
        println "Converting list"
        String name = parent[0..-2]
        list.each { li ->
            println "List element: ${li}"
            String type = getType(li)
            builder."$name"(kind:type) {
                convert(li, name, builder)
            }
        }
    }

    static void convert(Map map, String parent, DOMBuilder builder) {
        println "Converting map"
        map.each { k, v ->
            def atts = [:]
            atts.kind = getType(v)
            String name
            if (k.startsWith('http')) {
                URL url = new URL(k)
                name = url.file[1..-1] ?: "unknown"
                name = 'x:' + name
                atts.'xmlns:x' = "${url.protocol}://${url.host}"
                if (url.ref) {
                    atts.ref = url.ref
                }
            }
            else {
                name = k.replace("@", "at_")
            }

            println "Map element: ${v}"
            builder."$name"(atts) {
                convert(v, name, builder)
            }

        }
    }

    static String getType(Object object) {
        if (object instanceof Map) return "map"
        if (object instanceof List) return "list"
        return "val"
    }

}
