package com.github.cimsbioko.server.service

import com.github.cimsbioko.server.dao.FormRepository
import com.github.cimsbioko.server.domain.Form
import com.github.cimsbioko.server.domain.FormId
import com.github.cimsbioko.server.webapi.odk.RepeatExtractor
import com.github.cimsbioko.server.webapi.odk.RepeatExtractorImpl
import io.mockk.every
import io.mockk.mockk
import org.jdom2.Document
import org.jdom2.input.SAXBuilder
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.util.*

class FormMetadataServiceTest {

    companion object {

        private var doc: Document? = null
        private var metadataService: FormMetadataServiceImpl? = null
        private var formRepo: FormRepository = mockk()
        private var repeatExtractor: RepeatExtractor? = null

        @BeforeClass
        @JvmStatic
        fun setupOnce() {
            doc = loadForm("/malaria_indicator_survey.xml")
            metadataService = FormMetadataServiceImpl(formRepo, SchemaExtractorImpl())
            every { formRepo.findById(any()) } returns Optional.of(Form(FormId("whatever", "don't care"), doc))
            repeatExtractor = RepeatExtractorImpl()
        }

        private fun loadForm(s: String): Document? {
            return FormFieldExtractorTest::class.java.getResourceAsStream(s)?.let { SAXBuilder().build(it) }
        }
    }

    @Test
    fun `test repeat data`() {
        val expected = listOf(
            arrayOf("data", "individual"),
            arrayOf("data", "individual", "travelcount2weeks"),
            arrayOf("data", "individual", "travelcont8weeks"),
            arrayOf("data", "individual", "travelislandcountrep"),
            arrayOf("data", "visitor"),
            arrayOf("data", "Nt", "net")
        )
        val result = metadataService?.getRepeats(FormId("something", "orother"))
        assertEquals(expected.size, result?.size)
        for ((i,e) in expected.withIndex()) {
            assertArrayEquals(e, result?.get(i))
        }
    }

    @Test
    fun `can replace repeat extractor`() {
        val expected = doc?.let { repeatExtractor?.extractRepeats(it) } ?: error("repeat extraction failed")
        val result = metadataService?.getRepeats(FormId("something", "orother"))
        assertEquals(expected.size, result?.size)
        for ((i,e) in expected.withIndex()) {
            assertArrayEquals(e, result?.get(i))
        }
    }

}