package org.openhds.integration.bioko;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.ErrorLog;
import org.openhds.domain.model.Location;
import org.openhds.integration.AbstractResourceTest;
import org.openhds.integration.util.WebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.server.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(loader = WebContextLoader.class, locations = { "/testContext.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/formResourceTestDb.xml", type = DatabaseOperation.REFRESH)
public class LocationFormResourceTest extends AbstractResourceTest {


    @Autowired
    private GenericDao genericDao;

    private MockHttpSession session;

    private MockMvc mockMvc;

    private static final String A_DATE = "2000-01-01T00:00:00-05:00";

    private static final String LOCATION_FORM_XML =
            "<locationForm>"
                    + "<processed_by_mirth>false</processed_by_mirth>"
                    + "<field_worker_ext_id>UNK</field_worker_ext_id>"
                    + "<field_worker_uuid>UnknownFieldWorker</field_worker_uuid>"
                    + "<location_ext_id>M1000S57E09P1</location_ext_id>"
                    + "<collected_date_time>"
                    + A_DATE
                    + "</collected_date_time>"
                    + "<hierarchy_ext_id>IFB</hierarchy_ext_id>"
                    + "<hierarchy_uuid>hierarchy3</hierarchy_uuid>"
                    + "<entity_uuid>newLocation-UUID</entity_uuid>"
                    + "<location_name>newLocationName</location_name>"
                    + "<location_type>RUR</location_type>"
                    + "<community_name>newCommunityName</community_name>"
                    + "<map_area_name>newMapAreaName</map_area_name>"
                    + "<location_building_number>9</location_building_number>"
                    + "<locality_name>newLocalityName</locality_name>"
                    + "<sector_name>newSectorName</sector_name>"
            + "</locationForm>";

    private static final String DUPLICATE_LOCATION_FORM_XML =
            "<locationForm>"
                    + "<processed_by_mirth>false</processed_by_mirth>"
                    + "<field_worker_ext_id>UNK</field_worker_ext_id>"
                    + "<field_worker_uuid>UnknownFieldWorker</field_worker_uuid>"
                    + "<location_ext_id>M1000S57E09P1</location_ext_id>"
                    + "<collected_date_time>"
                    + A_DATE
                    + "</collected_date_time>"
                    + "<hierarchy_ext_id>IFB</hierarchy_ext_id>"
                    + "<hierarchy_uuid>hierarchy3</hierarchy_uuid>"
                    + "<entity_uuid>duplicateLocation-UUID</entity_uuid>"
                    + "<location_name>newLocationName</location_name>"
                    + "<location_type>RUR</location_type>"
                    + "<community_name>newCommunityName</community_name>"
                    + "<map_area_name>newMapAreaName</map_area_name>"
                    + "<location_building_number>9</location_building_number>"
                    + "<locality_name>newLocalityName</locality_name>"
                    + "<sector_name>newSectorName</sector_name>"
                    + "</locationForm>";

    private static final String DUPLICATE_LOCATION_FORM_XML_2 =
            "<locationForm>"
                    + "<processed_by_mirth>false</processed_by_mirth>"
                    + "<field_worker_ext_id>UNK</field_worker_ext_id>"
                    + "<field_worker_uuid>UnknownFieldWorker</field_worker_uuid>"
                    + "<location_ext_id>M1000S57E09P1</location_ext_id>"
                    + "<collected_date_time>"
                    + A_DATE
                    + "</collected_date_time>"
                    + "<hierarchy_ext_id>IFB</hierarchy_ext_id>"
                    + "<hierarchy_uuid>hierarchy3</hierarchy_uuid>"
                    + "<entity_uuid>duplicateLocation2-UUID</entity_uuid>"
                    + "<location_name>newLocationName</location_name>"
                    + "<location_type>RUR</location_type>"
                    + "<community_name>newCommunityName</community_name>"
                    + "<map_area_name>newMapAreaName</map_area_name>"
                    + "<location_building_number>9</location_building_number>"
                    + "<locality_name>newLocalityName</locality_name>"
                    + "<sector_name>newSectorName</sector_name>"
                    + "</locationForm>";

    @Before
    public void setUp() throws Exception {
        mockMvc = buildMockMvc();
        session = getMockHttpSession("admin", "test", mockMvc);
    }


    @Test
    public void testPostLocationFormXml() throws Exception {
        mockMvc.perform(
                post("/locationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(LOCATION_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyLocationCrud("M1000S57E09P1");

    }

    @Test
    public void testPostLocationFormDuplicateExtId() throws Exception {

        mockMvc.perform(
                post("/locationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(LOCATION_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        mockMvc.perform(
                post("/locationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(DUPLICATE_LOCATION_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        mockMvc.perform(
                post("/locationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(DUPLICATE_LOCATION_FORM_XML_2.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        Location location = genericDao.findByProperty(Location.class, "extId", "M1000S57E09P1A");
        assertNotNull(location);
        Location location2 = genericDao.findByProperty(Location.class, "extId", "M1000S57E09P1B");
        assertNotNull(location2);
        List<ErrorLog> loggedErrors = genericDao.findAll(ErrorLog.class, true);
        assertNotNull(loggedErrors);
        assertEquals(2, loggedErrors.size());

    }

    public void verifyLocationCrud(String locationExtId) {

        Location persistedLocation = genericDao.findByProperty(Location.class, "extId", locationExtId);
        assertNotNull(persistedLocation);
        assertEquals("newLocationName", persistedLocation.getLocationName());
        assertEquals("RUR", persistedLocation.getLocationType());
        assertEquals("newCommunityName", persistedLocation.getCommunityName());
        assertEquals("newMapAreaName", persistedLocation.getMapAreaName());

    }


}
