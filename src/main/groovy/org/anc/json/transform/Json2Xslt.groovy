package org.anc.json.transform

import org.w3c.dom.Document

/**
 * @author Keith Suderman
 */
class Json2Xslt {

    Document compile(File file) {
        compile(file.text)
    }

    Document compile(URL url) {
        compile(url.text)
    }

    Document compile(String template) {
        Binding binding = new Binding()
        GroovyShell shell = new GroovyShell() //(binding, configuration)
        Script script = shell.parse(template)
        ExpandoMetaClass metaClass = new ExpandoMetaClass(script.class, false)
        XSLTDelegate delegate = new XSLTDelegate()

        metaClass.stylesheet = { Map map=null, Closure closure ->
            if (map) {
                delegate.stylesheet(map, closure)
            }
            else {
                delegate.stylesheet(closure)
            }
        }
        metaClass.transformation = { Closure closure ->
            delegate.stylesheet(closure)
        }
        metaClass.initialize()
        script.metaClass = metaClass
        script.run()
        return delegate.document
    }
}

//class AbstractDelegate {
//    MarkupBuilder builder
//
//    public AbstractDelegate(MarkupBuilder builder) {
//        this.builder = builder
//    }
//}

/*

stylesheet {
    template(match:'/| @* | node()') {
        copy {
            'apply-templates'(select:'* | @* | node()')
        }
    }

    template(match:'') {
    }
}
<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform' >
                        <xsl:output method='xml' indent='yes' omit-xml-declaration='yes' />

                        <xsl:template match='/ | @* | node()'>
                            <xsl:copy>
                                <xsl:apply-templates select='* | @* | node()' />
                            </xsl:copy>
                        </xsl:template>

                        <xsl:template match='foo'>
                            <xsl:element name='bar'>
                                <xsl:apply-templates select='* | @* | node()' />
                            </xsl:element>
                        </xsl:template>
                        <xsl:template match='baz'>
                            <xsl:element name='splitz'>
                                <xsl:copy>
                                    <xsl:apply-templates select='* | @* | node()' />
                                </xsl:copy>
                        </xsl:element></xsl:template>
                    </xsl:stylesheet>
 */
