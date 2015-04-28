# Json2Json
JSON transformations using XSLT

The Json2Json programs transforms JSON documents using XSLT stylesheets 
written in a Groovy DSL.

A working knowledge of XSLT is required to get the most from the Json2Json
program, at least until we write more documentaion.  Understanding how
Groovy's [MarkupBuilder](http://docs.groovy-lang.org/latest/html/api/groovy/xml/MarkupBuilder.html)
works is also useful as the MarkupBuilder used to generate the XSLT stylesheet.

## The Template DSL

All *method* names in the DSL are assumed to be XSLT elements.  Method parameters
become element attributes, and the closure (if any) becomes the element content.
Namespaces and namespace prefixes are handled automatically.

```
/* The Identity transformation */
stylesheet {
    template('/ | @* | node()') {
        copy {
            'apply-templates'()
        }
    }
}
```

This generates the XSLT stylesheet:

```xml
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:list="http://www.lappsgrid.org/ns/json2json/list">
  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes" />
  <xsl:template match="/ | @* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
```

**Notes**

1. The `template` method expects a single String, the pattern to be matched.
Named parameters can be used if other attributes need to be set<br/>
`template(match:'element', mode:'delete') { ... }`
1. If the XSLT element name contains a hyphen then it must be enclosed in quotes.
*(Groovy allows method names with illegal characters for a Java identifier if
the method name is enclosed is single or double quotes.)*
1. Helper methods are provided for *apply-templates*, *value-of* and *copy-of*


# Usage

`java -jar json2json-x.y.z.jar <template> <input_file>`

### Options
* -s : prints the generated XSLT stylesheet.
* -x : generates XML as output.
* -j : generates JSON output (the default).

# Roadmap

1. Provide helper methods for all XSLT names that contain a hyphen: `apply-templates`,
`value-of`, etc.
1. Investigate creating a DOM object directly from the JSON that can be 
fed into an XSLT transformer without having to serialize through XML first.