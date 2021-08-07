package com.github.cimsbioko.server.service

import org.jdom2.Attribute
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.filter.Filters
import org.jdom2.xpath.XPathExpression
import org.jdom2.xpath.XPathFactory

data class FormField(
    val name: String,
    val path: String,
    val order: Int = 0,
    val type: Type = Type.UNKNOWN,
    val binary: Boolean = false
) {
    enum class Type {
        STRING,
        INT,
        BOOLEAN,
        DECIMAL,
        DATE,
        TIME,
        DATE_TIME,
        GEOPOINT,
        GEOTRACE,
        GEOSHAPE,
        BINARY,
        BARCODE,
        INTENT,
        REPEAT,
        STRUCTURE,
        SELECT_ONE,
        SELECT,
        UNKNOWN,
    }
}

interface FormFieldExtractor {
    fun extractFields(doc: Document): List<FormField>
}


private val XFORMS_NS = Namespace.getNamespace("xforms", "http://www.w3.org/2002/xforms")
private val XHTML_NS = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml")

class FormFieldExtractorImpl : FormFieldExtractor {

    /* xpath expressions to extract important form metadata, not threadsafe so clone */
    private var modelExpression: XPathExpression<Element>
    private var mainInstanceExpression: XPathExpression<Element>
    private var bindExpression: XPathExpression<Element>
    private var repeatExpression: XPathExpression<Attribute>

    init {
        val xpath = XPathFactory.instance()
        modelExpression = xpath.compile("/xhtml:html/xhtml:head/xforms:model", Filters.element(), null, XHTML_NS, XFORMS_NS)
        mainInstanceExpression = xpath.compile("xforms:instance/.[1]", Filters.element(), null, XFORMS_NS)
        bindExpression = xpath.compile("xforms:bind[@type]", Filters.element(), null, XFORMS_NS)
        repeatExpression = xpath.compile(
            "/xhtml:html/xhtml:body//xforms:repeat[@nodeset]/@nodeset", Filters.attribute(), null, XHTML_NS, XFORMS_NS
        )
    }

    fun extractModel(doc: Document): Element? {
        return modelExpression.clone().evaluateFirst(doc)
    }

    fun extractMainInstance(model: Element): Element? {
        return mainInstanceExpression.clone().evaluateFirst(model)
    }

    fun extractBinds(model: Element): List<Element> {
        return bindExpression.clone().evaluate(model)
    }

    fun extractRepeats(doc: Document): List<Attribute> {
        return repeatExpression.clone().evaluate(doc)
    }

    override fun extractFields(doc: Document): List<FormField> {
        TODO("Not yet implemented")
    }
}