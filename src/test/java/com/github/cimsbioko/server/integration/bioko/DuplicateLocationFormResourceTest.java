package com.github.cimsbioko.server.integration.bioko;

import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.model.Location;
import com.github.cimsbioko.server.integration.AbstractResourceTest;
import com.github.cimsbioko.server.integration.util.WebContextLoader;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.github.cimsbioko.server.webapi.DuplicateLocationFormResource.DUPLICATE_LOCATION_FORM_PATH;
import static org.junit.Assert.*;
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
@ActiveProfiles("test")
public class DuplicateLocationFormResourceTest extends AbstractResourceTest {

    @Autowired
    private GenericDao genericDao;

    private MockHttpSession session;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = buildMockMvc();
        session = getMockHttpSession("admin", "test", mockMvc);
    }

    @Test
    public void testRemoveAction() throws Exception {

        mockMvc.perform(
                post(DUPLICATE_LOCATION_FORM_PATH)
                        .session(session)
                        .accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(getTestForm("remove")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        Location dbLoc = lookupLocation();
        assertNotNull(dbLoc);
        assertEquals(Boolean.TRUE, dbLoc.isDeleted());
        assertEquals("new_desc", dbLoc.getDescription());
        assertNotEquals("new_lat", dbLoc.getLatitude());
        assertNotEquals("new_lng", dbLoc.getLongitude());
        assertNotEquals("new_acc", dbLoc.getAccuracy());
    }

    @Test
    public void testGPSAction() throws Exception {
        mockMvc.perform(
                post(DUPLICATE_LOCATION_FORM_PATH)
                        .session(session)
                        .accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(getGPSTestForm("new_lat", "new_lng")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        Location dbLoc = lookupLocation();
        assertNotNull(dbLoc);
        assertNotEquals(Boolean.TRUE, dbLoc.isDeleted());
        assertEquals("new_desc", dbLoc.getDescription());
        assertEquals("new_lat", dbLoc.getLatitude());
        assertEquals("new_lng", dbLoc.getLongitude());
        assertEquals("new_acc", dbLoc.getAccuracy());
    }

    @Test
    public void testGPSActionWithoutCoordinatesUpdatesDescription() throws Exception {
        mockMvc.perform(
                post(DUPLICATE_LOCATION_FORM_PATH)
                        .session(session)
                        .accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(getGPSTestForm(null, null)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        Location dbLoc = lookupLocation();
        assertNotNull(dbLoc);
        assertNotEquals(Boolean.TRUE, dbLoc.isDeleted());
        assertEquals("new_desc", dbLoc.getDescription());
        assertNull("new_lat", dbLoc.getLatitude());
        assertNull("new_lng", dbLoc.getLongitude());
        assertNull("new_acc", dbLoc.getAccuracy());
    }

    private String getTestForm(String action) {
        return "<duplicateLocationForm>" +
                "<entity_uuid>TestLocation1</entity_uuid>" +
                "<action>" + action + "</action>" +
                "<global_position_lat>new_lat</global_position_lat>" +
                "<global_position_lng>new_lng</global_position_lng>" +
                "<global_position_acc>new_acc</global_position_acc>" +
                "<description>new_desc</description>" +
                "</duplicateLocationForm>";
    }

    private String getGPSTestForm(String latitude, String longitude) {
        String gpsPart = "";
        if (latitude != null && longitude != null) {
            gpsPart = "<global_position_lat>" + latitude + "</global_position_lat>" +
                    "<global_position_lng>" + longitude + "</global_position_lng>" +
                    "<global_position_acc>new_acc</global_position_acc>";
        }
        return "<duplicateLocationForm>" +
                "<entity_uuid>TestLocation1</entity_uuid>" +
                "<action>gps-only</action>" +
                gpsPart +
                "<description>new_desc</description>" +
                "</duplicateLocationForm>";
    }

    private Location lookupLocation() {
        return genericDao.findByProperty(Location.class, "uuid", "TestLocation1");
    }
}
