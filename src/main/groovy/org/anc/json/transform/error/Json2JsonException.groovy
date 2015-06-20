package org.anc.json.transform.error

/**
 * @author Keith Suderman
 */
class Json2JsonException extends Exception {
    Json2JsonException() {
    }

    Json2JsonException(String message) {
        super(message)
    }

    Json2JsonException(String message, Throwable e) {
        super(message, e)
    }

    Json2JsonException(Throwable e) {
        super(e)
    }

}
