package org.lappsgrid.prototype.json2json.deprecated

import org.anc.json.transform.XSLTDelegate
import org.anc.json.transform.error.XmlException
import org.w3c.dom.*

/**
 * @author Keith Suderman
 * @deprecated
 */
class OutputBuilder {
    XSLTDelegate delegate
//    Document document
//    StringWriter writer = new StringWriter()
//    MarkupBuilder builder = new MarkupBuilder(writer)

    OutputBuilder(XSLTDelegate delegate) {
        this.delegate = delegate
    }

    def methodMissing(String name, args) {
//        println "Output builder missing $name"
        if (args == null || args.size() == 0) {
//            println "no args"
            delegate.append(name)
            delegate.pop()
        }
        else if (args.size() == 1) {
            if (args[0] instanceof String) {
                Node parent = delegate.peek()
                Node node = delegate.create(name)
                Node text = delegate.text(args[0])
                parent.appendChild(node)
                node.appendChild(text)
            }
            else if (args[0] instanceof Map) {
                delegate.append(name, args[0])
                delegate.pop()
            }
            else if (args[0] instanceof Closure) {
                Closure cl = (Closure) args[0]
                cl.delegate = delegate
                cl.resolveStrategy = Closure.DELEGATE_FIRST
                delegate.append(name)
                cl()
                delegate.pop()
            }
            else {
                throw new XmlException("Unable to handle arg ${arg[0]}")
            }
        }
        else if (args.size() == 2) {

        }
        else {
            throw new XmlException("More parameters than expected: ${args.size()}")
        }
    }

}
