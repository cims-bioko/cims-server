package org.openhds.integration.bioko;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.controller.service.refactor.IndividualService;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Residency;
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


import java.util.Set;

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
public class InMigrationFormResourceTest extends AbstractResourceTest {

    @Autowired
    private IndividualService individualService;

    @Autowired
    private GenericDao genericDao;

    private MockHttpSession session;

    private MockMvc mockMvc;

    private static final String A_DATE = "2001-01-01T00:00:00-05:00";

    private static final String DATE_PRIOR_TO_RESIDENCY = "1950-01-01T00:00:00-05:00";

    private static final String INMIGRATION_FORM_XML_VALID =
            "<inMigrationForm>"
                    + "<processed_by_mirth>false</processed_by_mirth>"
                    +"<collection_date_time>"
                    + A_DATE
                    + "</collection_date_time>"
                    + "<individual_ext_id>individual1</individual_ext_id>"
                    + "<individual_uuid>Individual1</individual_uuid>"
                    + "<location_ext_id>testLocation2</location_ext_id>"
                    + "<location_uuid>TestLocation2</location_uuid>"
                    + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>"
                    + "<field_worker_uuid>FWEK1D</field_worker_uuid>"
                    + "<visit_ext_id>migrateVisit</visit_ext_id>"
                    + "<visit_uuid>MigrateVisit</visit_uuid>"
                    + "<migration_date>"
                    + A_DATE
                    + "</migration_date>"
                    + "<migration_name_of_destination>DestinationName</migration_name_of_destination>"
                    + "<migration_reason>ReasonForMigration</migration_reason>"
                    + "<migration_type>1</migration_type>"
                    + "<migration_origin>testLocation1</migration_origin>"
                    + "</inMigrationForm>";

    private static final String INMIGRATION_FORM_XML_INVALID_DATE = "";

    @Before
    public void setUp() throws Exception {
        mockMvc = buildMockMvc();
        session = getMockHttpSession("admin", "test", mockMvc);
    }

    @Test
    public void testPostInMigrationFormXml() throws Exception {
        mockMvc.perform(
                post("/inMigrationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(INMIGRATION_FORM_XML_VALID.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyInMigrationCrud("individual1");

    }

    private void verifyInMigrationCrud(String individualExtId) {

        Individual individual = individualService.getByExtId(individualExtId);

        Set<Residency> residencySet = individual.getAllResidencies();
        assertEquals(2,residencySet.size());

        Residency currentResidency = individual.getCurrentResidency();

        assertEquals("testLocation2",currentResidency.getLocation().getExtId());
        assertEquals("NA",currentResidency.getEndType());
        assertNotNull(currentResidency.getStartDate());
        assertNotNull(currentResidency.getInsertDate());

    }

}
