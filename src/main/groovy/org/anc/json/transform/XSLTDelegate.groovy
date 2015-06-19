package org.anc.json.transform

import org.lappsgrid.prototype.json2json.deprecated.OutputBuilder
import org.anc.json.transform.error.XmlException
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Keith Suderman
 */
class XSLTDelegate {

    static final String XSLT_NAMESPACE = 'http://www.w3.org/1999/XSL/Transform'
    static final String XSLT_VERSION = '2.0'
    Document document
    Stack<Node> stack = new ArrayList<Node>()
    OutputBuilder out = new OutputBuilder(this)

    public XSLTDelegate() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
        factory.namespaceAware = true
        DocumentBuilder builder = factory.newDocumentBuilder()
        document = builder.newDocument()
//        out = new OutputBuilder(document)
    }

    def methodMissing(String name, args) {
//        println "Missing $name"
        if (name.startsWith('@')) {
            name = 'at_' + name.substring(1)
        }
        if (args == null || args.size() == 0) {
//            println "no args"
            append(name)
            pop()
        }
        else if (args.size() == 1) {
            if (args[0] instanceof String) {
                Node parent = peek()
                Node node = create('element', [name:name])
                Node text = text(args[0])
                parent.appendChild(node)
                node.appendChild(text)
            }
            else if (args[0] instanceof Map) {
                Map map = (Map) args[0]
                Element element = append('element', [name:name])
                map.each { key,value ->
                    addAttribute(element, key, value)
                }
                pop()
            }
            else if (args[0] instanceof Closure) {
                Closure cl = (Closure) args[0]
                cl.delegate = this
                cl.resolveStrategy = Closure.DELEGATE_FIRST
                append('element', [name:name])
                cl()
                pop()
            }
            else {
                throw new XmlException("Unable to handle arg ${arg[0]}")
            }
        }
        else if (args.size() == 2) {
            Map atts = (Map) args[0]
            Closure cl = (Closure) args[1]
            cl.delegate = this
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            Element element = append('element', [name:name])
            atts.each { key,value ->
                addAttribute(element, key, value)
            }
            cl()
            pop()
        }
        else {
            throw new XmlException("More parameters than expected: ${args.size()}")
        }
    }

    private void addAttribute(Element element, String name, String value) {
        Element attribute = create('attribute', [name:name])
        attribute.appendChild(text(value))
        element.appendChild(attribute)
    }

    void stylesheet(Closure cl) {
        Node node = create('stylesheet', [version:XSLT_VERSION])
        push(node)
        append('output', [method:'xml', indent:'yes', 'omit-xml-declaration':'yes'])
        pop()
        call(cl)
        document.appendChild(pop())
    }

    void stylesheet(Map map, Closure cl) {
        Map atts = [version: XSLT_VERSION]
        map.each { prefix,uri ->
            atts["xmlns:$prefix"] = uri
        }
        Node node = create('stylesheet', atts)
        push(node)
        append('output', [method:'xml', indent:'yes', 'omit-xml-declaration':'yes'])
        pop()
        call(cl)
        document.appendChild(pop())
    }

    void template(String pattern, Closure cl) {
        append('template', [match:fix(pattern)])
        call(cl)
        pop()
    }

    void template(String pattern, String mode, Closure cl) {
        append('template', [match:fix(pattern), mode:mode])
        call(cl)
        pop()
    }

    void template(Map map, Closure cl) {
        String pattern = map.match
        if (pattern) {
            map.match = fix(pattern)
        }
        append('template', map)
        call(cl)
        pop()
    }

    void apply_templates() {
        append('apply-templates')
        pop()
    }

    void apply_templates(String pattern) {
        append('apply-templates', [select:fix(pattern)])
        pop()
    }

    void apply_templates(String pattern, String mode) {
        append('apply-templates', [select:fix(pattern), mode:mode])
        pop()
    }

    void apply_templates(Map map) {
        String pattern = map.select
        if (pattern) {
            map.select = fix(pattern)
        }
        append('apply-templates', map)
        pop()
    }

    void for_each(String select, Closure cl) {
        append('for-each', [select:select])
        call(cl)
        pop()
    }

    void for_each(Map map, Closure cl) {
        append('for-each', map)
        call(cl)
        pop()
    }

    void copy(Closure cl) {
        append('copy')
        call(cl)
        pop()
    }

    void copy_of(String pattern) {
        append('copy-of', [select:fix(pattern)])
        pop()
    }

    void copy_of(Map map) {
        append('copy-of', map)
        pop()
    }

    void copy_of(String pattern, Closure cl) {
        append('copy-of', [select:fix(pattern)])
        call(cl)
        pop()
    }

    void copy_of(Map map, Closure cl) {
        append('copy-of', map)
        call(cl)
        pop()
    }

    void value_of(Map map) {
        append('value-of', map)
        pop()
    }

    void value_of(String pattern) {
        append('value-of', [select:pattern])
        pop()
    }

    void element(String name) {
        append(name)
        pop()
    }

    void element(String name, String content) {
        Node node = create('element', [name:name])
        Node parent = peek()
        parent.appendChild(node)
        node.appendChild(document.createTextNode(content))
    }

    void element(Map map, String content) {
        Node node = create('element', map)
        Node parent = peek()
        parent.appendChild(node)
        node.appendChild(document.createTextNode(content))
    }

    void element(String name, Map attributes, Closure cl) {
        Element element = create('element', [name:name])
        attributes.each { key,value ->
//            println "Setting attribute $key = $value"
//            element.setAttribute(key, value)
            Element attribute = create('attribute', [name:key])
            attribute.appendChild(text(value))
            element.appendChild(attribute)
        }
        Node parent = peek()
        parent.appendChild(element)
        push(element)
        call(cl)
        pop()
    }

    void element(String name, Closure cl) {
        append('element', [name:name])
        call(cl)
        pop()
    }

    void element(Map map, Closure cl) {
        append('element', map)
        call(cl)
        pop()
    }

    Node pop() { return stack.pop() }
    void push(Node node) { stack.push(node) }
    Node peek() { return stack.peek() }
    Node append(String name, Map attributes = [:]) {
        return append(create(name,attributes))
    }

    Node append(Node node) {
        Node parent = stack.peek()
        parent.appendChild(node)
        push(node)
        return node
    }

    private Node call(Closure cl) {
        cl.delegate = this
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
    }

    Node text(String content) {
        return document.createTextNode(content)
    }

    Node create(String name, Map attributes = [:]) {
//        println "Creating element ${name}"
        Element element = document.createElementNS(XSLT_NAMESPACE, 'xsl:' + name)
        attributes.each { key, value ->
            element.setAttribute(key, value)
        }
        return element
    }

    String fix(String name) {
        return name.replaceAll('#', '__hash__')
    }
}
