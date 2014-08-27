package org.openhds.integration.bioko;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.OutMigration;
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

import org.openhds.domain.model.Residency;


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
public class OutMigrationFormResourceTest extends AbstractFormResourceTest {

    @Autowired
    private GenericDao genericDao;

    private MockHttpSession session;

    private MockMvc mockMvc;

    private static final String A_DATE = "2000-01-01T00:00:00-05:00";

    private static final String DATE_PRIOR_TO_RESIDENCY = "1950-01-01T00:00:00-05:00";

    private static final String OUTMIGRATION_FORM_XML_VALID =
            "<outMigrationForm>"
                    + "<processed_by_mirth>false</processed_by_mirth>"
                    + "<out_migration_individual_ext_id>individual1</out_migration_individual_ext_id>"
                    + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>"
                    + "<visit_ext_id>migrateVisit</visit_ext_id>"
                    + "<out_migration_date>"
                    + A_DATE
                    + "</out_migration_date>"
                    + "<out_migration_name_of_destination>DestinationName</out_migration_name_of_destination>"
                    + "<out_migration_reason>ReasonForMigration</out_migration_reason>"
                    + "</outMigrationForm>";

    private static final String OUTMIGRATION_FORM_XML_INVALID_DATE =
            "<outMigrationForm>"
                    + "<processed_by_mirth>false</processed_by_mirth>"
                    + "<out_migration_individual_ext_id>individual2</out_migration_individual_ext_id>"
                    + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>"
                    + "<visit_ext_id>migrateVisit</visit_ext_id>"
                    + "<out_migration_date>"
                    + DATE_PRIOR_TO_RESIDENCY
                    + "</out_migration_date>"
                    + "<out_migration_name_of_destination>DestinationName</out_migration_name_of_destination>"
                    + "<out_migration_reason>ReasonForMigration</out_migration_reason>"
                    + "</outMigrationForm>";

    @Before
    public void setUp() throws Exception {
        mockMvc = buildMockMvc();
        session = getMockHttpSession("admin", "test", mockMvc);
    }

    @Test
    public void testPostOutMigrationFormXml() throws Exception {
        mockMvc.perform(
                post("/outMigrationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(OUTMIGRATION_FORM_XML_VALID.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyOutMigrationCrud("individual1");

    }

    private void verifyOutMigrationCrud(String individualExtId) {

        Individual individual = genericDao.findByProperty(Individual.class, "extId", individualExtId);
        Residency formerResidence = genericDao.findByProperty(Residency.class, "individual", individual);
        assertNotNull(formerResidence);
        assertEquals("OMG", formerResidence.getEndType());
        OutMigration outMigration = genericDao.findByProperty(OutMigration.class, "individual", individual);
        assertEquals("ReasonForMigration", outMigration.getReason());
        assertEquals("DestinationName", outMigration.getDestination());
        Membership membership = genericDao.findByProperty(Membership.class, "individual", individual);
        assertEquals("OMG", membership.getEndType());
    }

    @Test
    public void testPostOutMigrationFormXmlInvalidDate() throws Exception {
        mockMvc.perform(
                post("/outMigrationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(OUTMIGRATION_FORM_XML_INVALID_DATE.getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyOutMigrationNotPersisted("individual2");

    }

    private void verifyOutMigrationNotPersisted(String individualExtId) {

        Individual individual = genericDao.findByProperty(Individual.class, "extId", individualExtId);
        Residency residency = genericDao.findByProperty(Residency.class, "individual", individual);
        assertNotNull(residency);
        assertEquals("NA", residency.getEndType());

    }

}
