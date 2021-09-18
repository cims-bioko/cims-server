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
    fun testExtractFields() {
        val fields = doc?.let { extractor?.extractFields(it) }
        Assert.assertNotNull(fields)
        fields?.also {
            Assert.assertTrue(fields.isNotEmpty())
            for ((idx, field) in MIS_DATA.withIndex()) {
                Assert.assertEquals(field, fields[idx])
            }
        }
    }
}

val MIS_DATA = listOf(
    FormField("fieldWorkerExtId", "/fieldWorkerExtId", type= Type.STRING),
    FormField("fieldWorkerUuid", "/fieldWorkerUuid", 1, type = Type.STRING),
    FormField("collectionDateTime", "/collectionDateTime", 2, type = Type.STRING),
    FormField("entityUuid", "/entityUuid", 3, type = Type.STRING),
    FormField("entityExtId", "/entityExtId", 4, type = Type.STRING),
    FormField("survey_date", "/survey_date", 5, type = Type.DATE),
    FormField("Household", "/Household", 6, type = Type.STRING),
    FormField("Nationality", "/Nationality", 7, type = Type.SELECT_ONE),
    FormField("HousingType", "/HousingType", 8, type = Type.SELECT_ONE),
    FormField("HousingTypeOther", "/HousingTypeOther", 9, type = Type.STRING),
    FormField("Rooms", "/Rooms", 10, type = Type.INT),
    FormField("SleepingRooms", "/SleepingRooms", 11, type = Type.INT),
    FormField("Generalgoods", "/Generalgoods", 12, type = Type.STRUCTURE),
    FormField("Radio", "/Generalgoods/Radio", 13, type = Type.SELECT_ONE),
    FormField("Television", "/Generalgoods/Television", 14, type = Type.SELECT_ONE),
    FormField("Computer", "/Generalgoods/Computer", 15, type = Type.SELECT_ONE),
    FormField("Clock", "/Generalgoods/Clock", 16, type = Type.SELECT_ONE),
    FormField("Sofa", "/Generalgoods/Sofa", 17, type = Type.SELECT_ONE),
    FormField("Table", "/Generalgoods/Table", 18, type = Type.SELECT_ONE),
    FormField("Amoire", "/Generalgoods/Amoire", 19, type = Type.SELECT_ONE),
    FormField("Cabinet", "/Generalgoods/Cabinet", 20, type = Type.SELECT_ONE),
    FormField("Fans", "/Generalgoods/Fans", 21, type = Type.SELECT_ONE),
    FormField("AirCon", "/Generalgoods/AirCon", 22, type = Type.SELECT_ONE),
    FormField("Refrigerator", "/Generalgoods/Refrigerator", 23, type = Type.SELECT_ONE),
    FormField("Stove", "/Generalgoods/Stove", 24, type = Type.SELECT_ONE),
    FormField("WashingMachine", "/Generalgoods/WashingMachine", 25, type = Type.SELECT_ONE),
    FormField("Car", "/Generalgoods/Car", 26, type = Type.SELECT_ONE),
    FormField("HouseHoldSprayed", "/HouseHoldSprayed", 27, type = Type.SELECT_ONE),
    FormField("Sticker", "/Sticker", 28, type = Type.SELECT_ONE),
    FormField("StickerDate", "/StickerDate", 29, type = Type.DATE),
    FormField("HouseholdSize", "/HouseholdSize", 30, type = Type.INT),
    FormField("HHVisitors", "/HHVisitors", 31, type = Type.SELECT_ONE),
    FormField("HHVisitorCount", "/HHVisitorCount", 32, type = Type.INT),
    FormField("Roster", "/Roster", 33, type = Type.STRING),
    FormField("individual_count", "/individual_count", 34, type = Type.STRING),
    FormField("individual", "/individual", 35, type = Type.REPEAT),
    FormField("id", "/individual/id", 36, type = Type.STRING),
    FormField("Name", "/individual/Name", 37, type = Type.STRING),
    FormField("RelationToHead", "/individual/RelationToHead", 38, type = Type.SELECT_ONE),
)