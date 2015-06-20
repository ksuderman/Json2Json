package org.lappsgrid.prototype.json2json.deprecated

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Keith Suderman
 * @deprecated
 */
class DOMBuilder {
//    static final String NAMESPACE = 'http://www.w3.org/1999/XSL/Transform'

    Document document
    Stack<Element> stack = new ArrayList<Element>()

    /**
     * The closure used to create new elements. How elements are created
     * depends on whether or not a namespace has been defined.
     */
    Closure createElement

    public DOMBuilder() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
        factory.namespaceAware = false
        DocumentBuilder builder = factory.newDocumentBuilder()
        document = builder.newDocument()
        stack.push(document)
        createElement = { String name, Map attributes = [:] ->
            println "Create: $name"
            Element element = document.createElement(name)
            attributes.each { String key, value ->
                println "$key = $value"
                int colon = key.indexOf(':')
                if (colon > 0) {
                    String namespace = key.substring(0, colon)
                    element.setAttributeNS(namespace, key, value)
                }
                else {
                    element.setAttribute(key, value)
                }
            }
            return element
        }
    }

    public DOMBuilder(String prefix, String namespace) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
        factory.namespaceAware = true
        DocumentBuilder builder = factory.newDocumentBuilder()
        document = builder.newDocument()
        stack.push(document)
        createElement = { String name, Map attributes = [:] ->
            println "Create $name"
            Element element = document.createElementNS(namespace, "${prefix}:${name}")
            attributes.each { String key, value ->
                //element.setAttribute(key, value)
                int colon = key.indexOf(':')
                if (colon > 0) {
                    String ns = key.substring(0, colon)
                    element.setAttributeNS(ns, key, value)
                }
                else {
                    element.setAttribute(key, value)
                }
            }
            return element
        }
    }

//    public void text(String content) {
//        Node node = document.createTextNode(content)
//        stack.peek().appendChild(node)
//    }

//    private Node create(String name) {
//        println "Create $name"
//        Element element = createElement(name)
//        return element
//    }
//
//    private Node create(String name, Map attributes) {
//        println "Create $name $attributes"
//        Element element = createElement(name, attributes)
//        attributes.each { key, value ->
//            element.setAttribute(key, value)
//        }
//        return element
//    }

    def methodMissing(String name, args) {
        println "Missing $name"
        args.each { println "\t$it" }
        if (args.size() > 0) {
            if (args[-1] instanceof Closure) {
                Closure cl = args[-1]
                def params
                if (args.size() == 1) {
                    params = [:]
                }
                else {
                    params = args[0]
                }
                Element element = createElement(name, params)
                if (element == null) {
                    throw new NullPointerException("Unable to create element ${name}")
                }
                Node parent = stack.peek()
                parent.appendChild(element)
                stack.push(element)
                call(cl)
                stack.pop()
            }
            else {
                if (args[0] instanceof String) {
                    Node parent = stack.peek()
                    Element element = createElement(name, [:])
                    Node text = document.createTextNode(args[0])
                    element.appendChild(text)
                    parent.appendChild(element)
                }
                else {
                    stack.peek().appendChild(createElement(name, args[0]))
                }
            }
        }
        else {
            stack.peek().appendChild(createElement(name, [:]))
        }
    }

    def call(Closure cl) {
        cl.delegate = this //new StyleSheetDelegate(this)
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
    }


}