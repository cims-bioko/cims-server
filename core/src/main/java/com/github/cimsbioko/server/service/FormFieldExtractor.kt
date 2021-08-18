package com.github.cimsbioko.server.service

import com.github.cimsbioko.server.service.Type.*
import org.jdom2.Attribute
import org.jdom2.Document
import org.jdom2.Element
import org.jdom2.Namespace
import org.jdom2.filter.Filters
import org.jdom2.xpath.XPathExpression
import org.jdom2.xpath.XPathFactory

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
    @Deprecated("not a type in recent odk xforms spec")
    SELECT,
    @Deprecated("not a type in recent odk xforms spec")
    SELECT_ONE,
    STRUCTURE,
    UNKNOWN,
}

data class FormField(
    val name: String,
    val path: String,
    val order: Int = 0,
    val type: Type = UNKNOWN,
    val binary: Boolean = false,
)

data class Binding(
    val nodeset: String,
    val type: Type
)

interface FormFieldExtractor {
    fun extractFields(doc: Document): List<FormField>
}


private val XFORMS_NS = Namespace.getNamespace("xforms", "http://www.w3.org/2002/xforms")
private val XHTML_NS = Namespace.getNamespace("xhtml", "http://www.w3.org/1999/xhtml")

class FormFieldExtractorImpl : FormFieldExtractor {

    /* xpath expressions to extract form metadata, not threadsafe so clone */
    private var modelExpression: XPathExpression<Element>
    private var mainInstanceExpression: XPathExpression<Element>
    private var bindExpression: XPathExpression<Element>
    private var repeatExpression: XPathExpression<Attribute>

    init {
        val xpath = XPathFactory.instance()
        modelExpression = xpath.compile("/xhtml:html/xhtml:head/xforms:model", Filters.element(), null, XHTML_NS, XFORMS_NS)
        mainInstanceExpression = xpath.compile("xforms:instance/*[1]", Filters.element(), null, XFORMS_NS)
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

    private fun String.absolute(root: String): String = if (startsWith("/")) this else "$root/$this"
    private val Element.hasChildren
        get() = children?.isNotEmpty() ?: false

    private fun String.toType(): Type {
        return when (this) {
            "string" -> STRING
            "int" -> INT
            "boolean" -> BOOLEAN
            "decimal" -> DECIMAL
            "date" -> DATE
            "time" -> TIME
            "dateTime" -> DATE_TIME
            "geopoint" -> GEOPOINT
            "geotrace" -> GEOTRACE
            "geoshape" -> GEOSHAPE
            "binary" -> BINARY
            "barcode" -> BARCODE
            "intent" -> INTENT
            "select1" -> SELECT_ONE
            "select" -> SELECT
            else -> UNKNOWN
        }
    }

    private fun Element.toBinding(): Binding? {
        val nodeset = getAttributeValue("nodeset")
        val type = getAttributeValue("type")?.toType()
        return if (nodeset != null && type != null) Binding(nodeset, type)
        else null
    }

    override fun extractFields(doc: Document): List<FormField> {
        val model = extractModel(doc)
        val instance = model?.let { extractMainInstance(model) }
        val bindings = model?.let { extractBinds(it) }?.mapNotNull { it.toBinding() }
        val repeats = extractRepeats(doc).map { it.value }.toSet()
        return if (instance == null || bindings == null) emptyList()
        else formFields(instance, bindings, repeats)
    }


    private fun formFields(
        instance: Element,
        bindings: List<Binding>,
        repeats: Set<String>,
        envelope: String = instance.name,
        basePath: List<String> = emptyList(),
        orderStart: Int = 0
    ): List<FormField> {
        val fields: MutableList<FormField> = mutableListOf()
        val processedRepeats: MutableSet<String> = mutableSetOf()
        var order = orderStart
        for (child in instance.children) {
            val path = basePath + child.name
            val joinedPath = path.joinToString(prefix = "/", separator = "/")
            var field = FormField(
                name = child.name,
                path = joinedPath,
                order = order
            )
            val bindingPath = "/${envelope}${joinedPath}"
            if (bindingPath in repeats) {
                if (bindingPath in processedRepeats) continue
                else processedRepeats += bindingPath
                field = field.copy(type = REPEAT)
            } else {
                val binding = bindings.find { it.nodeset.absolute(envelope) == bindingPath }
                if (binding != null) field = field.copy(type = binding.type)
                else if (child.hasChildren) field = field.copy(type = STRUCTURE)
            }
            fields += field
            order += 1

            if (field.type == BINARY || field.path == "/meta/audit") {
                field = field.copy(binary = true)
            }

            if ((field.type == REPEAT || field.type == STRUCTURE) && child.hasChildren) {
                val nested = formFields(child, bindings, repeats, envelope, path, order)
                fields += nested
                order += nested.size
            }
        }
        return fields
    }
}