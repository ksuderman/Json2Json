package org.anc.json.transform.error

/**
 * @author Keith Suderman
 */
class XmlException extends Json2JsonException {
    XmlException() {
    }

    XmlException(String message) {
        super(message)
    }

    XmlException(String message, Throwable e) {
        super(message, e)
    }

    XmlException(Throwable e) {
        super(e)
    }
}
