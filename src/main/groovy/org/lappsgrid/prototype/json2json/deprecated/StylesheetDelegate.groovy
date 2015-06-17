package org.lappsgrid.prototype.json2json.deprecated

/**
 * @author Keith Suderman
 * @deprecated
 */
class StylesheetDelegate {
    StringWriter writer
    DOMBuilder xml

    public StyleSheetDelegate(DOMBuilder builder) {
//        writer = new StringWriter()
//        builder = new MarkupBuilder(writer)
        this.xml = builder
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
        xml.template(args) {
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
        xml.'apply-templates'(map)
    }

    // TODO: This isn't right... <apply-templates/> is not the same as selecting everything... or is it...
    void apply_templates() {
        apply_templates(select: '@*|node()')
    }

    void apply_templates(String name) {
        apply_templates(select:name)
    }

    void value_of(String name) {
        xml.'value-of'(select:name)
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
        println "Method missing: $name"
        args.each { println "\t$it" }

        if (args == null || args.size() == 0) {
//            println "args empty"
            xml."$name"
        }
        else if (args.size() == 1) {
            if (args[0] instanceof Closure) {
//                println "1 closure"
                Closure cl = (Closure) args[0]
                cl.delegate = this
                cl.resolveStrategy = Closure.DELEGATE_FIRST
                xml."${name}" {
                    cl()
                }
            }
            else {
//                println "1 arg "
                xml."${name}"(args[0])
            }
        }
        else {
//            println "${args.size()} args"
            println "name:${name} 0:${args[0]} 1:${args[1]}"
            if (args[1] instanceof String) {
                xml."${name}"(args[0], args[1])
            }
            else {
                Closure cl = (Closure) args[1]
                cl.delegate = this
                cl.resolveStrategy = Closure.DELEGATE_FIRST
                xml."${name}"(args[0]) {
                    cl()
                }
            }
        }
    }


}
