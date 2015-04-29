package org.lappsgrid.prototype.json2json

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Keith Suderman
 */
class DOMBuilder {
//    static final String NAMESPACE = 'http://www.w3.org/1999/XSL/Transform'

    Document document
    Stack<Element> stack = new ArrayList<Element>()
    Closure createElement
//    Node current

    public DOMBuilder() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
        factory.namespaceAware = false
        DocumentBuilder builder = factory.newDocumentBuilder()
        document = builder.newDocument()
        stack.push(document)
        createElement = { String name, Map attributes = [:] ->
            Element element = document.createElement(name)
            attributes.each { key, value ->
                element.setAttribute(key, value)
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
            Element element = document.createElementNS(namespace, "${prefix}:${name}")
            attributes.each { key, value ->
                element.setAttribute(key, value)
            }
            return element
        }
    }

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
                stack.peek().appendChild(createElement(name, args[0]))
            }
        }
        else {
            stack.peek().appendChild(createElement(name, [:]))
        }
    }

    def call(Closure cl) {
        cl.delegate = this
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
    }
}