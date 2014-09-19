package org.openhds.integration.bioko;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.dao.service.GenericDao;
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
    private GenericDao genericDao;

    private MockHttpSession session;

    private MockMvc mockMvc;

    private static final String A_DATE = "2000-01-01T00:00:00-05:00";

    private static final String DATE_PRIOR_TO_RESIDENCY = "1950-01-01T00:00:00-05:00";

    private static final String INMIGRATION_FORM_XML_VALID =
            "<outMigrationForm>"
                    + "<processed_by_mirth>false</processed_by_mirth>"
                    + "<in_migration_individual_ext_id>individual1</in_migration_individual_ext_id>"
                    + "<in_migration_location_ext_id>individual1</in_migration_location_ext_id>"
                    + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>"
                    + "<visit_ext_id>migrateVisit</visit_ext_id>"
                    + "<in_migration_date>"
                    + A_DATE
                    + "</in_migration_date>"
                    + "<in_migration_name_of_destination>DestinationName</in_migration_name_of_destination>"
                    + "<in_migration_reason>ReasonForMigration</in_migration_reason>"
                    + "<in_migration_type>1</in_migration_type>"
                    + "</outMigrationForm>";

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

//        Individual individual = genericDao.findByProperty(Individual.class, "extId", individualExtId);
//        Residency formerResidence = genericDao.findByProperty(Residency.class, "individual", individual);
//        assertNotNull(formerResidence);
//        assertEquals("OMG", formerResidence.getEndType());
//        OutMigration outMigration = genericDao.findByProperty(OutMigration.class, "individual", individual);
//        assertEquals("ReasonForMigration", outMigration.getReason());
//        assertEquals("DestinationName", outMigration.getDestination());
//        Membership membership = genericDao.findByProperty(Membership.class, "individual", individual);
//        assertEquals("OMG", membership.getEndType());
    }


}
