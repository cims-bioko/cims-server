package org.openhds.integration;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.ErrorLog;
import org.openhds.domain.model.Error;
import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.bioko.VisitForm;
import org.openhds.errorhandling.service.ErrorHandlingService;
import org.openhds.integration.util.WebContextLoader;
import org.openhds.webservice.resources.bioko.AbstractFormResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.server.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(loader=WebContextLoader.class, locations={"/testContext.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/formResourceTestDb.xml", type = DatabaseOperation.REFRESH)
public class ErrorLogResourceTest extends AbstractResourceTest {

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private ErrorHandlingService errorService;

    private MockHttpSession session;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = buildMockMvc();
        session = getMockHttpSession("admin", "test", mockMvc);
    }

    @Test
    public void testErrorLogBadLocationPost() throws Exception {

        final String BAD_LOCATION_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<location>"
                + "<collectedBy>"
                + "<extId>UNK</extId>"
                + "</collectedBy>"
                + "<accuracy></accuracy>"
                + "<altitude></altitude>"
                + "<extId>testLocation3</extId>"
                + "<latitude></latitude>"
                + "<locationHierarchy>"
                + "<extId>9999999</extId>"
                + "</locationHierarchy>"
                + "<locationName>Test House</locationName>"
                + "<locationType>RUR</locationType>"
                + "<longitude></longitude>"
                + "</location>";

        mockMvc.perform(post("/locations").session(session)
                        .contentType(MediaType.APPLICATION_XML)
                        .accept(MediaType.APPLICATION_XML)
                        .body(BAD_LOCATION_XML.getBytes()))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        FieldWorker fieldWorker = genericDao.findByProperty(FieldWorker.class, "extId", "UNK");
        List<ErrorLog> errorLogs = errorService.findAllErrorsByFieldWorker(fieldWorker);
        assertEquals(1, errorLogs.size());
        ErrorLog badLocationErrorLog = errorLogs.get(0);
        assertNotNull(badLocationErrorLog);
        List<Error> badLocationErrors = badLocationErrorLog.getErrors();
        Error error = badLocationErrors.get(0);
        assertEquals(ConstraintViolations.INVALID_LOCATION_HIERARCHY_EXT_ID, error.getErrorMessage());

    }

    @Test
    public void testErrorBadOutMigrationForm() throws Exception {

        final String DATE_PRIOR_TO_RESIDENCY = "1950-01-01T00:00:00-05:00";

        final String OUTMIGRATION_FORM_XML_INVALID_DATE =
                "<outMigrationForm>"
                        + "<processed_by_mirth>false</processed_by_mirth>"
                        + "<individual_ext_id>individual2</individual_ext_id>"
                        + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>"
                        + "<visit_ext_id>migrateVisit</visit_ext_id>"
                        + "<out_migration_date>"
                        + DATE_PRIOR_TO_RESIDENCY
                        + "</out_migration_date>"
                        + "<out_migration_name_of_destination>DestinationName</out_migration_name_of_destination>"
                        + "<out_migration_reason>ReasonForMigration</out_migration_reason>"
                        + "</outMigrationForm>";

        mockMvc.perform(
                post("/outMigrationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(OUTMIGRATION_FORM_XML_INVALID_DATE.getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        FieldWorker fieldWorker = genericDao.findByProperty(FieldWorker.class, "extId", "FWEK1D");
        List<ErrorLog> errorLogs = errorService.findAllErrorsByFieldWorker(fieldWorker);
        assertEquals(1, errorLogs.size());
        ErrorLog badOutMigrationFormErrorLog = errorLogs.get(0);
        assertNotNull(badOutMigrationFormErrorLog);
        List<Error> badOutMigrationErrors = badOutMigrationFormErrorLog.getErrors();
        Error error = badOutMigrationErrors.get(0);
        assertEquals(ConstraintViolations.OUT_MIGRATION_BEFORE_INDIVIDUAL_RESIDENCY_START, error.getErrorMessage());

    }

    @Test
    public void testErrorBadVisitForm() throws Exception {

        final String A_DATE = "2000-01-01T00:00:00-05:00";

        final String VISIT_FORM_XML_INVALID_LOCATION_UUID =
                "<visitForm>"
                        + "<processed_by_mirth>false</processed_by_mirth>"
                        + "<visit_ext_id>1234567890aa</visit_ext_id>"
                        + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>"
                        + "<field_worker_uuid>FWEK1D</field_worker_uuid>"
                        + "<location_ext_id>notALocation</location_ext_id>"
                        + "<location_uuid>notALocationUuid</location_uuid>"
                        + "<visit_date>"
                        + A_DATE
                        + "</visit_date>"
                        + "</visitForm>";

        mockMvc.perform(
                post("/visitForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(VISIT_FORM_XML_INVALID_LOCATION_UUID.getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        FieldWorker fieldWorker = genericDao.findByProperty(FieldWorker.class, "extId", "FWEK1D");
        List<ErrorLog> errorLogs = errorService.findAllErrorsByFieldWorker(fieldWorker);
        assertEquals(1, errorLogs.size());
        ErrorLog badVisitFormErrorLog = errorLogs.get(0);
        assertNotNull(badVisitFormErrorLog);

    }



}
