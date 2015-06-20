package org.anc.json.transform

import org.lappsgrid.prototype.json2json.deprecated.DOMBuilder
import org.anc.json.transform.error.JsonException
import org.lappsgrid.serialization.Serializer
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.Text

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @author Keith Suderman
 */
class JsonToXml {

    public static final String ANON_NAMESPACE = "http://vocab.lappsgrid.org/ns/anon"
//    public static final String AT_NAMESPACE = "http://vocab.lappsgrid.org/ns/at"
//    public static final int VALUE = 0
//    public static final int MAP = 1
//    public static final int LIST = 2

    Document document
    Closure createElement
    Stack<Element> stack = new ArrayList<Element>()

    public JsonToXml() {
        commonInit()
        createElement = { String name, Map attributes = [:] ->
            int colon = name.indexOf(':')
            Element element
            if (name.startsWith('http')) {
                URL url = new URL(name)
                //TODO The namespace prefix needs to be generated rather than using a hard coded value.
                name = 'x:' + (url.file[1..-1] ?: "unknown")
                String namespace = url.protocol + '://' + url.host
                if (url.ref) {
                    //element.setAttribute('ref', url.ref)
                    name = name + "__hash__" + url.ref
                }
                element = document.createElementNS(namespace, name)
            }
            else if (name.startsWith('@')) {
                name = 'at_' + name.substring(1)
                element = document.createElement(name)
            }
            else if (colon > 0) {
                String prefix = name.substring(0, colon)
                String namespace = ANON_NAMESPACE + '#' + prefix
                element = document.createElementNS(namespace, name)
            }
            else {
                element = document.createElement(name)
            }

            attributes.each { String key, value ->
                colon = key.indexOf(':')
                if (colon > 0) {
                    String namespace = ANON_NAMESPACE + '#' + key.substring(0, colon)
                    element.setAttributeNS(namespace, key, value)
                }
                else {
                    element.setAttribute(key, value)
                }
            }
            return element
        }

    }

    public JsonToXml(String prefix, String namespace) {
        commonInit()
        createElement = { String name, Map attributes = [:] ->
            Element element = document.createElementNS(namespace, "${prefix}:${name}")
            attributes.each { String key, value ->
                // Check if the key has a namespace prefix.
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

    public Document convert(File file) {
        return convert(file.text)
    }

    public Document convert(URL url) {
        return convert(url.text)
    }

    public Document convert(String input) {
        Map json = Serializer.parse(input, Map)

        doConvert(json)
        return document
    }

    protected commonInit() {
        document = createDocument()
        Element root = document.createElement("container")
        root.setAttribute('kind', 'map')
        document.appendChild(root)
        push(root)
    }

    protected Document createDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
        factory.namespaceAware = true
        DocumentBuilder builder = factory.newDocumentBuilder()
        Document document = builder.newDocument()
        return document
    }

    private void doConvert(Map map) {
        map.each { key,value ->
            if (isValue(value)) {
                Text text = document.createTextNode(value.toString())
                Element element = createElement(key)
                element.setAttribute('type', value.getClass().getName())
                element.appendChild(text)
                peek().appendChild(element)
            }
            else if (value instanceof Map) {
                Element element = createElement(key, [kind:'map'])
                Node parent = stack.peek()
                peek().appendChild(element)
                push(element)
                doConvert(value)
                pop()
            }
            else if (value instanceof List) {
                Element element = createElement(key, [kind:'list'])
                peek().appendChild(element)
                push(element)
                doConvert(value)
                pop()
            }
            else {
                throw new JsonException("Unhandled JSON type: ${value.class?.name}")
            }
        }
    }

    private void doConvert(List list) {
        list.each {
            if (isValue(it)) {
                Element element = createElement('item')
                element.setAttribute('kind', 'val')
                Node text = document.createTextNode(it)
                element.appendChild(text)
                peek().appendChild(element)
            }
            else {
                String type = 'val'
                if (it instanceof Map) type = 'map'
                if (it instanceof List) type = 'list'
                Node parent = peek()
                String
                Element element = createElement('item')
                element.setAttribute('kind', type)
                peek().appendChild(element)
                push(element)
                doConvert(it)
                pop()
            }
        }
    }

    private void append(Node node) {
        peek().appendChild(node)
    }

    Node pop() {
        return stack.pop()
    }

    void push(Node node) {
        if (node == null) {
            throw new NullPointerException("Push NULL node onto the stack.")
        }
        stack.push(node)
    }

    Node peek() {
        return stack.peek()
    }

    boolean isValue(Object object) {
        if (object instanceof String) return true
        if (object instanceof Number) return true
        return false
    }
}
