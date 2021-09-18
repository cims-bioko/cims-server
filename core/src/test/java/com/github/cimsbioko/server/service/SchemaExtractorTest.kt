package com.github.cimsbioko.server.service

import org.jdom2.Document
import org.jdom2.input.SAXBuilder
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test

class SchemaExtractorTest {

    companion object {

        private var fieldExtractor: FormFieldExtractor? = null
        private var schemaExtractor: SchemaExtractor? = null
        private var doc: Document? = null

        @BeforeClass
        @JvmStatic
        fun setupOnce() {
            fieldExtractor = FormFieldExtractorImpl()
            schemaExtractor = SchemaExtractorImpl()
            doc = loadForm("/malaria_indicator_survey.xml")
        }

        private fun loadForm(s: String): Document? {
            return FormFieldExtractorTest::class.java.getResourceAsStream(s)?.let { SAXBuilder().build(it) }
        }
    }

    @Test
    fun `basic structure`() {
        doc?.let { doc ->
            schemaExtractor?.extractSchema(doc)?.also { schema ->
                assertEquals("data", schema.root)
                assertEquals(43, schema.fields.size)
                assertEquals(6, schema.repeats.size)
                assertEquals(88, schema.selects.size)
            }
        }
    }

    @Test
    fun `all items accounted for and order matches`() {
        doc?.let { doc ->
            val schema = schemaExtractor?.extractSchema(doc)
            val fields = fieldExtractor?.extractFields(doc)
            if (schema != null && fields != null) {
                assertEquals(fields.map { it.name }, schema.fields.findRecursive { true }.map { it.name })
            } else {
                fail("extraction failed to produce values")
            }
        }
    }
}