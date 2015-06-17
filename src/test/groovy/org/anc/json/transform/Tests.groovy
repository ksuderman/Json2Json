package org.anc.json.transform

import org.junit.*
import org.w3c.dom.Document

/**
 * @author Keith Suderman
 */
class Tests {


    void parse(String name) {
        parse(this.class.getResource(name))
    }

    void parse(URL url) {
        Json2Xslt xslt = new Json2Xslt()
        Document document = xslt.compile(url)
        JsonTransformer.prettyPrint(document)
    }

    @Ignore
    void testJson2Xslt() {
        String template = """
stylesheet {
    template(match:'*') {
        apply_templates()
    }
}
"""
//        JsonToXml xform = new JsonToXml('xsl', 'http://www.w3.org/1999/XSL/Transform')
//        println xform.convert(template)
        Json2Xslt xslt = new Json2Xslt()
        Document document = xslt.compile(template)
        JsonTransformer.prettyPrint(document)
    }
}
