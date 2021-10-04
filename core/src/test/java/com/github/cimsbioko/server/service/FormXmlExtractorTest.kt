package com.github.cimsbioko.server.service

import org.jdom2.Document
import org.jdom2.input.SAXBuilder
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class FormXmlExtractorTest {

    companion object {

        private var xmlExtractor: FormXmlExtractor? = null
        private var doc: Document? = null

        @BeforeClass
        @JvmStatic
        fun setupOnce() {
            xmlExtractor = FormXmlExtractorImpl()
            doc = loadForm("/malaria_indicator_survey.xml")
        }

        private fun loadForm(s: String): Document? {
            return FormFieldExtractorTest::class.java.getResourceAsStream(s)?.let { SAXBuilder().build(it) }
        }
    }

    @Test
    fun testExtractModel() {
        val model = doc?.let { xmlExtractor?.extractModel(it) }
        Assert.assertNotNull(model)
        Assert.assertEquals("model", model?.name)
    }

    @Test
    fun testExtractMainInstance() {
        val mainInstance = doc?.let { xmlExtractor?.extractModel(it) }?.let { xmlExtractor?.extractMainInstance(it) }
        Assert.assertNotNull(mainInstance)
        Assert.assertEquals("data", mainInstance?.name)
    }

    @Test
    fun testExtractBinds() {
        val binds = doc?.let { xmlExtractor?.extractModel(it) }?.let { xmlExtractor?.extractBinds(it) }
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
        val repeats = doc?.let { xmlExtractor?.extractRepeats(it) }
        Assert.assertNotNull(repeats)
        repeats?.also {
            Assert.assertTrue(repeats.isNotEmpty())
            for (repeat in repeats) {
                Assert.assertTrue(repeat.value.isNotBlank())
            }
        }
    }

    @Test
    fun testExtractChoices() {
        val choices = doc?.let { xmlExtractor?.extractChoices(it) }
        Assert.assertNotNull(choices)
        val sample = choices?.get("/data/Nt/net/WhyNotUseNet")
        Assert.assertNotNull(sample)
        Assert.assertEquals(15, sample?.size)
        val firstChoice = sample?.get(0)
        Assert.assertEquals("1", firstChoice?.value)
        Assert.assertEquals("Provoca calor", firstChoice?.label)
        val lastChoice = sample?.get(14)
        Assert.assertEquals("88", lastChoice?.value)
        Assert.assertEquals("Otro", lastChoice?.label)
    }

}