package org.lappsgrid.prototype.json2json

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.Uri

/**
 * @author Keith Suderman
 */
class Main {

    void testJsonToXml() {
        String json = createSample()
        println Json2Xml.convert(json)
    }

    void testIdentityTransform() {
        transform """
stylesheet {
    template('/ | @* | node()') {
        copy {
            apply_templates('@* | node()')
        }
    }
}
"""

    }

    void testApplyTemplates() {
        transform2Xml """
stylesheet {
    template('/') {
        element(name:'html') {
            element(name:'body') {
                apply_templates()
            }
        }
    }

    template('node()') {
        apply_templates()
    }

    template('annotations') {
        element(name:'h1', 'Word List')
        element(name:'ol') {
            apply_templates()
        }
    }

    template('word') {
        element(name:'li') {
            value_of('.')
        }
    }
}
"""
    }

    void testBuilder() {
        transform2Xml """
        transformation {
            template('container') {
                builder.html {
                    builder.head {
                        builder.title 'This is a test'
                    }
                    builder.body {
                        builder.h1 'Heading'
                        builder.table {
                            apply_templates()
                        }
                    }
                }
            }

            template('annotation') {
                builder.tr {
                    builder.td {
                        value_of('id')
                    }
                    builder.td {
                        value_of('label')
                    }
                    builder.td {
                        value_of('./features/word')
                    }
                }
            }

            template('* | node()') {
                apply_templates()
            }
        }
"""
    }

    void testJsonToXslt() {
        transform """
        stylesheet {
            template('/ | @* | node()') {
                copy {
                    'apply-templates'(select:'* | @* | node()')
                }
            }
            template('annotation') {
                copy {
                    'apply-templates'(select:'node()')
                    element(name:'copy', 'Inserted by the template.')
                }
            }
        }

"""
    }

    void testDelete() {
        String template = """
stylesheet {
    template('/ | @* | node()') {
        copy {
            apply_templates('@*|node()')
        }
    }
    template('annotation/type') { }
}
"""
        String json = createSample()
//        println json
        Json2Json transformer = new Json2Json(template)
        String xml = transformer.transform(json)
        println xml
//        println Xml2Json.convert(xml)
    }

    void transform(String template) {
        Json2Json transformer = new Json2Json(template)
        String json = createSample()
        println transformer.transform(json)

    }

    void transform2Xml(String template) {
        Json2Json transformer = new Json2Json(template, Json2Json.Output.XML)
        String json = createSample()
        println transformer.transform(json)

    }

    static String createSample() {
        Container container = new Container()
//                                  1         2         3         4
//                        012345678901234567890123456789012345678901234
        container.text = "Goodbye cruel world, I am leaving you today."
        container.language = "en"
        View view = container.newView()
        view.addContains(Uri.TOKEN, "http://www.anc.org", "gate:tokenization")
        view.newAnnotation("w1", Uri.TOKEN, 0, 7).features.word = 'Goodbye'
        view.newAnnotation("w2", Uri.TOKEN, 8, 13).features.word = 'cruel'
        view.newAnnotation("w3", Uri.TOKEN, 14, 19).features.word = 'world'
        view.newAnnotation("w4", Uri.TOKEN, 21, 22).features.word = 'I'
        view.newAnnotation("w5", Uri.TOKEN, 23, 25).features.word = 'am'
        view.newAnnotation("w6", Uri.TOKEN, 26, 33).features.word = 'leaving'
        view.newAnnotation("w7", Uri.TOKEN, 34, 37).features.word = 'you'
        view.newAnnotation("w8", Uri.TOKEN, 38, 43).features.word = 'today'
        return Serializer.toPrettyJson(container)
    }

    void run(String template, String input, def output) {
        Json2Json transformer = new Json2Json(template)
        output.write(transformer.transform(input))

    }

    void run(String template, String input, File output) {
        // Automagically import the groovy json and xml packages for the user script.
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStarImports("groovy.json", "groovy.xml")
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers(customizer)

        // "System" properties can be added to the binding object so they are available
        // inside the user script.
        Binding binding = new Binding()

        GroovyShell shell = new GroovyShell(binding, configuration)
        Script script = shell.parse(template)

        ExpandoMetaClass meta = new ExpandoMetaClass(script.class, false)
        meta.match = { String pattern, Closure cl ->

        }
        script.metaClass = meta
    }

    void testXml2Json() {
        String json = createSample()
        String xml = Json2Xml.convert(json)
        println xml
        println Xml2Json.convert(xml)
    }

//    static void main(args) {
//        File file = new File("src/test/resources/input.json")
//        file.text = new Main().createSample()
//    }

    static void main(args) {
//        new Main().testDelete()
//        return

        CliBuilder cli = new CliBuilder()
        cli.header = "Transforms JSON instances with XSLT stylesheets\n"
        cli.usage = "java -jar json2json-${Version.version}.jar [options] <template> <input>\n"
        cli.x(longOpt: 'xml', 'generates XML output')
        cli.j(longOpt: 'json', 'generates JSON output (default)')
        cli.v(longOpt: 'version', 'displays the version number')
        cli.h(longOpt: 'help', 'displays this help message')

        def params = cli.parse(args)
        if (!params) {
            return
        }

        if (params.h) {
            println()
            cli.usage()
            println()
            return
        }

        if (params.v) {
            println()
            println "LAPPS Json2Json v${Version.version}"
            println "Copyright 2015 The Language Application Grid"
            println()
            return
        }

        List<String> files = params.arguments()
        if (files.size() != 2) {
            println "No template and/or input file specified."
            println()
            cli.usage()
            println()
            return
        }

        File template = new File(files[0])
        if (!template.exists()) {
            println "Template file not found."
            return
        }

        File input = new File(files[1])
        if (!input.exists()) {
            println "Input file not found."
            return
        }

        Json2Json.Output format = Json2Json.Output.JSON
        if (params.x) {
            format = Json2Json.Output.XML
        }
        Json2Json transformer = new Json2Json(template.text, format)
        println transformer.transform(input.text)
    }
}

//        URL resource = Main.classLoader.getResource('rename.j2j')
//        if (!resource) {
//            println "Unable to load template"
//            return
//        }
////        def template = Main.getResource('/identity.j2j')?.text
////        if (!template) {
////            println "Unable to load template"
////            return
////        }
////        def template = """
////stylesheet {
////    template('/ | @* | node()') {
////        copy {
////            apply_templates('@* | node()')
////        }
////    }
////}
////"""
//        def template = resource.text
//        if (!template) {
//            println "No template text"
//            return
//        }
////        println template
////        println resource.text
////        if (true) return
//        String json = createSample()
//        Main app = new Main()
//        app.run(template, json, System.out)
////        app.testXml2Json()
////        app.testDelete()
////        new Main().testJsonToXslt()
////        new Main().testJsonToXml()
////        new Main().testBuilder()
////        new Main().testApplyTemplates()
////       app.testIdentityTransform()
//    }
//}


/*

match(view.contains['Token']) { view ->

}
 */