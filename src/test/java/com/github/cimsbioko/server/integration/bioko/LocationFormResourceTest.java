package com.github.cimsbioko.server.integration.bioko;

import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.integration.AbstractResourceTest;
import com.github.cimsbioko.server.integration.util.WebContextLoader;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.github.cimsbioko.server.domain.model.ErrorLog;
import com.github.cimsbioko.server.domain.model.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(loader = WebContextLoader.class, locations = {"/testContext.xml"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
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
                        .content(LOCATION_FORM_XML))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        verifyLocationCrud("M1000S57E09P1");

    }

    @Test
    public void testPostLocationFormDuplicateExtId() throws Exception {

        mockMvc.perform(
                post("/locationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(LOCATION_FORM_XML))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        mockMvc.perform(
                post("/locationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(DUPLICATE_LOCATION_FORM_XML))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        mockMvc.perform(
                post("/locationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(DUPLICATE_LOCATION_FORM_XML_2))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        Location location = genericDao.findByProperty(Location.class, "extId", "M1000S57E09P1-d1");
        assertNotNull(location);
        Location location2 = genericDao.findByProperty(Location.class, "extId", "M1000S57E09P1-d2");
        assertNotNull(location2);
        List<ErrorLog> loggedErrors = genericDao.findAll(ErrorLog.class, true);
        assertNotNull(loggedErrors);
        assertEquals(2, loggedErrors.size());

    }

    @Test
    public void testReusingExtIdCreatesNoDuplicate() throws Exception {

        mockMvc.perform(
                post("/locationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(LOCATION_FORM_XML))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        Location original = genericDao.findByProperty(Location.class, "extId", "M1000S57E09P1", true);
        assertNotNull("location should exist after first form", original);
        original.setDeleted(true);
        original = genericDao.findByProperty(Location.class, "extId", "M1000S57E09P1", true);
        assertNull("location should not exist after being marked deleted", original);

        mockMvc.perform(
                post("/locationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(DUPLICATE_LOCATION_FORM_XML))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        Location duplicate = genericDao.findByProperty(Location.class, "extId", "M1000S57E09P1-d1");
        assertNull("no duplicate should exist after second form", duplicate);
        Location second = genericDao.findByProperty(Location.class, "extId", "M1000S57E09P1", true);
        assertNotNull("location should exist after second form", second);
        List<ErrorLog> loggedErrors = genericDao.findAll(ErrorLog.class, true);
        assertNotNull(loggedErrors);
        assertEquals("no errors should have been created after both forms processed", 0, loggedErrors.size());
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
