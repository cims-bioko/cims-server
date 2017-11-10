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
        assertNull(dbLoc.getGlobalPos());
    }

    @Test
    public void testGPSAction() throws Exception {
        mockMvc.perform(
                post(DUPLICATE_LOCATION_FORM_PATH)
                        .session(session)
                        .accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(getTestForm("gps-only")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        Location dbLoc = lookupLocation();
        assertNotNull(dbLoc);
        assertNotEquals(Boolean.TRUE, dbLoc.isDeleted());
        assertEquals("new_desc", dbLoc.getDescription());
        assertNotNull(dbLoc.getGlobalPos());
        assertEquals(3.0, dbLoc.getGlobalPos().getCoordinate().y, 0.1);
        assertEquals(8.0, dbLoc.getGlobalPos().getCoordinate().x, 0.1);
    }

    private String getTestForm(String action) {
        return "<duplicateLocationForm>" +
                "<entity_uuid>TestLocation1</entity_uuid>" +
                "<action>" + action + "</action>" +
                "<global_position_lat>3.0</global_position_lat>" +
                "<global_position_lng>8.0</global_position_lng>" +
                "<description>new_desc</description>" +
                "</duplicateLocationForm>";
    }

    private Location lookupLocation() {
        return genericDao.findByProperty(Location.class, "uuid", "TestLocation1");
    }
}
