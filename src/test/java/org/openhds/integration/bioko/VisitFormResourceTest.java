package org.openhds.integration.bioko;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Visit;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;


@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(loader = WebContextLoader.class, locations = {"/testContext.xml"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup(value = "/formResourceTestDb.xml", type = DatabaseOperation.REFRESH)
public class VisitFormResourceTest extends AbstractResourceTest {

    @Autowired
    private GenericDao genericDao;

    private MockHttpSession session;

    private MockMvc mockMvc;

    private static final String A_DATE = "2000-01-01T00:00:00-05:00";

    private static final String VISIT_FORM_XML =
            "<visitForm>"
                    + "<processed_by_mirth>false</processed_by_mirth>"
                    + "<visit_ext_id>1234567890aa</visit_ext_id>"
                    + "<visit_uuid>1234567890aa</visit_uuid>"
                    + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>"
                    + "<field_worker_uuid>FWEK1D</field_worker_uuid>"
                    + "<location_ext_id>testLocation1</location_ext_id>"
                    + "<location_uuid>TestLocation1</location_uuid>"
                    + "<visit_date>"
                    + A_DATE
                    + "</visit_date>"
                    + "</visitForm>";

    private static final String VISIT_FORM_XML_OUTDATED_LOCATION_EXTID =
            "<visitForm>"
                    + "<processed_by_mirth>false</processed_by_mirth>"
                    + "<visit_ext_id>2015-02-19_testLocationOUTDATED</visit_ext_id>"
                    + "<visit_uuid>1234567890aa</visit_uuid>"
                    + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>"
                    + "<field_worker_uuid>FWEK1D</field_worker_uuid>"
                    + "<location_ext_id>testLocationOUTDATED</location_ext_id>"
                    + "<location_uuid>TestLocation1</location_uuid>"
                    + "<visit_date>"
                    + A_DATE
                    + "</visit_date>"
                    + "</visitForm>";

    @Before
    public void setUp() throws Exception {
        mockMvc = buildMockMvc();
        session = getMockHttpSession("admin", "test", mockMvc);
    }

    @Test
    public void testPostVisitFormXml() throws Exception {
        mockMvc.perform(
                post("/visitForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(VISIT_FORM_XML))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        verifyVisitCrud("1234567890aa", "testLocation1", "FWEK1D");
    }

    @Test
    public void testPostVisitFormXmlOudatedLocationExtId() throws Exception {
        mockMvc.perform(
                post("/visitForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(VISIT_FORM_XML_OUTDATED_LOCATION_EXTID))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        verifyVisitCrud("2015-02-19_testLocation1", "testLocation1", "FWEK1D");
    }

    private void verifyVisitCrud(String visitExtId, String locationExtId, String fieldworkerExtId) {
        Visit visit = genericDao.findByProperty(Visit.class, "extId", visitExtId);
        assertNotNull(visit);
        assertEquals(visit.getExtId(), visitExtId);
        assertEquals(visit.getVisitLocation().getExtId(), locationExtId);
        assertEquals(visit.getCollectedBy().getExtId(), fieldworkerExtId);
    }

}
