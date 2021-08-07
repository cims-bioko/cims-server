package com.github.cimsbioko.server.service

import org.jdom2.Document
import org.jdom2.input.SAXBuilder
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class FormFieldExtractorTest {

    companion object {

        private var extractor: FormFieldExtractorImpl? = null
        private var doc: Document? = null

        @BeforeClass
        @JvmStatic
        fun setupOnce() {
            extractor = FormFieldExtractorImpl()
            doc = loadForm("/malaria_indicator_survey.xml")
        }

        private fun loadForm(s: String): Document? {
            return FormFieldExtractorTest::class.java.getResourceAsStream(s)?.let { SAXBuilder().build(it) }
        }
    }

    @Test
    fun testExtractModel() {
        val model = doc?.let { extractor?.extractModel(it) }
        Assert.assertNotNull(model)
        Assert.assertEquals("model", model?.name)
    }

    @Test
    fun testExtractMainInstance() {
        val mainInstance = doc?.let { extractor?.extractModel(it) }?.let { extractor?.extractMainInstance(it) }
        Assert.assertNotNull(mainInstance)
        Assert.assertEquals("data", mainInstance?.name)
    }

    @Test
    fun testExtractBinds() {
        val binds = doc?.let { extractor?.extractModel(it) }?.let { extractor?.extractBinds(it) }
        Assert.assertNotNull(binds)
        binds?.also {
            Assert.assertTrue(binds.isNotEmpty())
            for (bind in binds) {
                Assert.assertTrue(bind.getAttributeValue("type") != null)
            }
        }
    }

    @Test
    fun testExtractRepeats() {
        val repeats = doc?.let { extractor?.extractRepeats(it) }
        Assert.assertNotNull(repeats)
        repeats?.also {
            Assert.assertTrue(repeats.isNotEmpty())
            for (repeat in repeats) {
                Assert.assertTrue(repeat.value.isNotBlank())
            }
        }
    }
}