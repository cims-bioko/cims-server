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

import static com.github.cimsbioko.server.webapi.SprayingFormResource.SPRAYING_FORM_PATH;
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
public class SprayingFormResourceTest extends AbstractResourceTest {

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
    public void testDestroyedEvaluation() throws Exception {
        mockMvc.perform(
                post(SPRAYING_FORM_PATH)
                        .session(session)
                        .accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(getTestForm("5")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        Location dbLoc = lookupLocation();
        assertNotNull(dbLoc);
        assertEquals(Boolean.TRUE, dbLoc.isDeleted());
    }

    @Test
    public void testNotDestroyedEvaluation() throws Exception {
        mockMvc.perform(
                post(SPRAYING_FORM_PATH)
                        .session(session)
                        .accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(getTestForm("4")))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        Location dbLoc = lookupLocation();
        assertNotNull(dbLoc);
        assertNotEquals(Boolean.TRUE, dbLoc.isDeleted());
    }

    private String getTestForm(String evaluation) {
        return String.format(
                "<sprayingForm><entity_uuid>TestLocation1</entity_uuid><evaluation>%s</evaluation></sprayingForm>",
                evaluation
        );
    }

    private Location lookupLocation() {
        return genericDao.findByProperty(Location.class, "uuid", "TestLocation1");
    }
}
