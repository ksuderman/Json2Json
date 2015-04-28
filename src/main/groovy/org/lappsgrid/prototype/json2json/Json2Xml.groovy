package org.lappsgrid.prototype.json2json

import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder

/**
 * @author Keith Suderman
 */
class Json2Xml {
    private JsonToXMl() {}

    static String convert(URL url) {
        return convert(new JsonSlurper().parse(url))
    }

    static String convert(File file) {
        return convert(new JsonSlurper().parse(file))
    }

    static String convert(String json) {
        return convert(new JsonSlurper().parseText(json))
    }

    static String convert(Object object) {
        StringWriter writer = new StringWriter()
        MarkupBuilder builder = new MarkupBuilder(writer)
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
        return writer.toString()
    }

    static void convert(Object object, String parent, MarkupBuilder builder) {
        builder.mkp.yield(object.toString())
    }

    static void convert(List list, String parent, MarkupBuilder builder) {
        String name = parent[0..-2]
        list.each { li ->
            String type = getType(li)
            builder."$name"(kind:type) {
                convert(li, name, builder)
            }
        }
    }

    static void convert(Map map, String parent, MarkupBuilder builder) {
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
