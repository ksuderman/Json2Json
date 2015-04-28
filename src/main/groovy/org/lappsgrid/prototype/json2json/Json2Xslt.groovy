package org.lappsgrid.prototype.json2json

import groovy.xml.MarkupBuilder

/**
 * @author Keith Suderman
 */
class Json2Xslt {

    String compile(String template) {
//        ImportCustomizer customizer = new ImportCustomizer()
//        customizer.addStarImports("groovy.json", "groovy.xml")
//        CompilerConfiguration configuration = new CompilerConfiguration()
//        configuration.addCompilationCustomizers(customizer)
        StringWriter writer = new StringWriter()
        MarkupBuilder xml = new MarkupBuilder(writer)
        MarkupBuilder builder = xml
        Binding binding = new Binding()
//        binding.setVariable("builder", builder)
        binding.setVariable("xml", builder)
        GroovyShell shell = new GroovyShell() //(binding, configuration)
        Script script = shell.parse(template)
        ExpandoMetaClass metaClass = new ExpandoMetaClass(script.class, false)
        xml.doubleQuotes = true
        metaClass.stylesheet = { Closure closure ->
            closure.delegate = new StyleSheetDelegate(builder)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            def atts = [
                    version:'1.0',
                    'xmlns:xsl':'http://www.w3.org/1999/XSL/Transform'
            ]
            xml.'xsl:stylesheet'(atts) {
                "xsl:output"(method:'xml', indent:'yes', 'omit-xml-declaration':'yes')
                closure()
            }
        }
        metaClass.transformation = { Closure closure ->
            closure.delegate = new StyleSheetDelegate(builder)
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            def atts = [
                    version:'1.0',
                    'xmlns:xsl':'http://www.w3.org/1999/XSL/Transform',
                    'xmlns:list':'http://www.lappsgrid.org/ns/json2json/list'
            ]
            xml.'xsl:stylesheet'(atts) {
                "xsl:output"(method:'xml', indent:'yes', 'omit-xml-declaration':'yes')
                closure()
            }
        }
        metaClass.initialize()
        script.metaClass = metaClass
        script.run()
        return writer.toString()
    }
}

//class AbstractDelegate {
//    MarkupBuilder builder
//
//    public AbstractDelegate(MarkupBuilder builder) {
//        this.builder = builder
//    }
//}

class StyleSheetDelegate {
    StringWriter writer
    MarkupBuilder builder

    public StyleSheetDelegate(MarkupBuilder builder) {
//        writer = new StringWriter()
//        builder = new MarkupBuilder(writer)
        this.builder = builder
    }

//    String toString() {
//        return writer.toString()
//    }

    void template(String pattern, String mode, Closure cl) {
        template(match:pattern, mode:mode, cl)
    }

    void template(String pattern, Closure cl) {
//        println "Template"
        template(match:pattern, cl)
    }

    void template(Map args, Closure cl) {
        cl.delegate = this
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        builder.'xsl:template'(args) {
            cl()
        }
    }

//    void 'apply-templates'(String name) {
//        apply_templates(select:name)
//    }
//
//    void 'apply-templates'(String name, String mode) {
//        apply_templates(select:name, mode:mode)
//    }
    void apply_templates(String name, String mode) {
        apply_templates(select:name, mode:mode)
    }

    void apply_templates(Map map) {
        builder.'xsl:apply-templates'(map)
    }

    // TODO: This isn't right... <apply-templates/> is not the same as selecting everything... or is it...
    void apply_templates() {
        apply_templates(select: '@*|node()')
    }

    void apply_templates(String name) {
        apply_templates(select:name)
    }

    void value_of(String name) {
        builder.'xsl:value-of'(select:name)
    }

    /*

    void copy(Closure cl) {
        println "Copy"
        cl.delegate = this
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        builder.'xsl:copy' {
            cl()
        }
    }


    void propertyMissing(String name, value) {
        println "Property missing $name"
    }

    void propertyMissing(String name) {
        println "Getting missing property $name"
    }
    */

    void methodMissing(String name, args) {
//        println "Method missing: $name"
//        args.each { println it }

        if (args == null || args.size() == 0) {
//            println "args empty"
            builder."$name"
        }
        else if (args.size() == 1) {
            if (args[0] instanceof Closure) {
//                println "1 closure"
                Closure cl = (Closure) args[0]
                cl.delegate = this
                cl.resolveStrategy = Closure.DELEGATE_FIRST
                builder."xsl:${name}" {
                    cl()
                }
            }
            else {
//                println "1 arg "
                builder."xsl:${name}"(args[0])
            }
        }
        else {
//            println "${args.size()} args"
            if (args[1] instanceof String) {
                builder."xsl:${name}"(args[0], args[1])
            }
            else {
                Closure cl = (Closure) args[1]
                cl.delegate = this
                cl.resolveStrategy = Closure.DELEGATE_FIRST
                builder."xsl:${name}"(args[0]) {
                    cl()
                }
            }
        }
    }


}
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
