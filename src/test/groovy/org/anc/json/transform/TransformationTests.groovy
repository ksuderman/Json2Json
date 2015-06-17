package org.anc.json.transform

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.anc.json.transform.JsonTransformer.Output

import static org.junit.Assert.*

import org.lappsgrid.serialization.Serializer

/**
 * @author Keith Suderman
 */
class TransformationTests {

    JsonTransformer transformer

    @Before
    void setup() {
    }

    @After
    void cleanup() {
        transformer = null
    }

    @Ignore
    void testIdentityTransformation() {
        checkIdentity('/cds.json', '/identity.j2j')
        checkIdentity('/input.json', '/identity.j2j')
    }

    @Test
    void testRename() {
        println "TransformationTests.testRename"
        println transform('/input.json', '/rename.j2j')
    }

    @Test
    void testTable() {
        println "TransformationTests.testTable"
        println transform('/input.json', '/table.j2j', Output.XML)
    }

    @Test
    void testCds() {
        println "TransformationTests.testCds"
        println transform('/cds.json', '/cds3.j2j')
    }

    String transform(String instance, String stylesheet, Output format = Output.JSON) {
        String original = load(instance)
        String template = load(stylesheet)
        JsonTransformer transformer = new JsonTransformer(template, format)
        return transformer.transform(original)
    }

    void checkIdentity(String instance, String stylesheet) {
        String original = load(instance)
        String template = load(stylesheet)
        JsonTransformer transformer = new JsonTransformer(template)
        String generated = transformer.transform(original)
        Map expected = Serializer.parse(original, Map)
        Map actual = Serializer.parse(generated, Map)
        assertTrue compare(expected, actual)
    }

    String load(String name) {
        return this.class.getResource(name).text
    }

    void compareJson(String expectedJson, String actualJson) {
        ObjectMapper mapper = new ObjectMapper()
        JsonNode expected = mapper.readTree(expectedJson)
        JsonNode actual = mapper.readTree(actualJson)
        assert actual.equals(expected)
    }

    boolean compare(Map expected, Map actual) {
        if (expected.size() != actual.size()) {
            println "Maps are not the same size. Expected:${expected.size()} Actual:${actual.size()}"
            return false
        }
        Set keys = expected.keySet()
        Iterator<String> it = keys.iterator()
        while (it.hasNext()) {
            String key = it.next()
            Object expectedValue = expected[key]
            Object actualValue = actual[key]
            if (actualValue == null) {
                println "Missing value for key:${key}"
                return false
            }
            if (checkTypes(expectedValue, actualValue)) {
                if (!compare(expectedValue, actualValue)) {
                    return false
                }
            }
            else {
                println "Values for $key are not the same type."
                println "Expected: ${expectedValue.getClass()}"
                println "Actual: ${actualValue.getClass()}"
                return false
            }
        }
        return true
    }

    boolean compare(List expected, List actual) {
        if (expected.size() != actual.size()) {
            println "Lists are not the same size. Expected:${expected.size()} Actual:${actual.size()}"
            return false
        }
        Iterator<Object> expectedIt = expected.iterator()
        Iterator<Object> actualIt = actual.iterator()
        while (expectedIt.hasNext()) {
            Object expectedValue = expectedIt.next()
            Object actualValue = actualIt.next()
            if (checkTypes(expectedValue, actualValue)) {
                if (!compare(expectedValue, actualValue)) {
                    return false
                }
            }
            else {
                println "List values are not the same type."
                println "Expected: ${expectedValue.getClass()}"
                println "Actual: ${actualValue.getClass()}"
                return false
            }
        }
        return true
    }

    boolean compare(String expected, String actual) {
        if (!expected.equals(actual)) {
            println "Values do not match. Expected: $expected Actual: $actual"
            return false
        }
        return true
    }

    boolean compare(Integer i, Integer j) {
        return i == j
    }

    boolean compare(Float a, Float b) {
        return a - b < 0.0001f
    }

    boolean checkTypes(Object object1, Object object2) {
//        println "Checking ${object1.getClass()} and ${object2.getClass()}"
        if (object1 instanceof Map) {
//            println "object1 is a map"
            if (object2 instanceof Map) {
                return true
            }
//            println "object2 is not a map"
            return false
        }
        else if (object1 instanceof List) {
            return object2 instanceof List
        }
        else if (object1 instanceof String) {
            return object2 instanceof String
        }
        else if (object1 instanceof Integer) {
            return object2 instanceof Integer
        }
        else if (object1 instanceof Float) {
            return object2 instanceof Float
        }

        return false
    }
}
