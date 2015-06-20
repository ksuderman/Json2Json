package org.lappsgrid.prototype.json2json.deprecated

import org.w3c.dom.*

import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * @author Keith Suderman
 * @deprecated
 */
class Json2Dom {

    void run() {
        DOMBuilder xslt = new DOMBuilder()
        //Node stylesheet = builder.create('apply-templates', [match:'/|@*|node()'])
        xslt.stylesheet(version:'1.0') {
            template(match:'/|@*|node()') {
                copy {
                    'apply-templates'(select:'@*|node()')
                }
            }
            template(match:'type') { }
            template(match:'annotation/features/word') {
                element(name:'string') {
                    'value-of'(select:'.')
                }
            }
        }
        Json2Dom.prettyPrint(xslt.document)
//        println dom.'xsl:stylesheet'('xmlns:xsl':'something', version:'1.0') {
//            'xsl:template'(match:'*|@*|node()') {
//                'xsl:copy' {
//                    'xsl:apply-templates'(select:'@*|node()')
//                }
//            }
//        }

    }

    public static final void prettyPrint(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));
        System.out.println(out.toString());
    }

    static void main(args) {
        new Json2Dom().run()
    }
}

