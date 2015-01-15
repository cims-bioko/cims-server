package org.openhds.integration.bioko;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.PregnancyObservation;
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
public class PregnancyObservationFormResourceTest extends AbstractResourceTest {

    @Autowired
    private GenericDao genericDao;

    private MockHttpSession session;

    private MockMvc mockMvc;

    private static final String A_CURRENT_DATE = "2014-01-01T00:00:00-05:00";

    private static final String A_PREVIOUS_DATE = "1999-01-01T00:00:00-05:00";

    private static final String A_FUTURE_DATE = "2020-01-01T00:00:00-05:00";

    private static final String VALID_OBSERVATION_XML =
            "<pregnancyObservationForm>" +
            "<recorded_date>"+ A_CURRENT_DATE +"</recorded_date>" +
            "<visit_ext_id>pregObsVisit</visit_ext_id>" +
            "<individual_ext_id>pregnantIndividual</individual_ext_id>" +
            "<processed_by_mirth>null</processed_by_mirth>" +
                    "<entity_uuid>PREG-OBS-UUID</entity_uuid>" +
                    "<field_worker_ext_id>UNK</field_worker_ext_id>" +
                    "<field_worker_uuid>UnknownFieldWorker</field_worker_uuid>" +
                    "<expected_delivery_date>"+A_FUTURE_DATE+"</expected_delivery_date>" +
            "</pregnancyObservationForm>";

    @Before
    public void setUp() throws Exception {
        mockMvc = buildMockMvc();
        session = getMockHttpSession("admin", "test", mockMvc);
    }

    @Test
    public void testPostPregnancyObservationFormXml() throws Exception {

        mockMvc.perform(
                post("/pregnancyObservationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(VALID_OBSERVATION_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyPregnancyObservationCrud("pregnantIndividual");

    }

    private void verifyPregnancyObservationCrud(String individualExtId) {

        Individual mother = genericDao.findByProperty(Individual.class, "extId", individualExtId);
        PregnancyObservation obs = genericDao.findByProperty(PregnancyObservation.class, "mother", mother);
        assertNotNull(obs);
        assertEquals(mother, obs.getMother());

    }

    private static final String OBSERVATION_EXPECTED_DELIVERY_DATE_IN_PAST =
            "<pregnancyObservationForm>" +
                    "<recorded_date>"+ A_CURRENT_DATE +"</recorded_date>" +
                    "<visit_ext_id>pregObsVisit</visit_ext_id>" +
                    "<individual_ext_id>pregnantIndividual</individual_ext_id>" +
                    "<processed_by_mirth>null</processed_by_mirth>" +
                    "<entity_uuid>PREG-OBS-IN-PAST</entity_uuid>" +
                    "<field_worker_ext_id>UNK</field_worker_ext_id>" +
                    "<field_worker_uuid>UnknownFieldWorker</field_worker_uuid>" +
                    "<expected_delivery_date>"+A_PREVIOUS_DATE+"</expected_delivery_date>" +
                    "</pregnancyObservationForm>";

    @Test
    public void testPostExpectedDateInPast() throws Exception {

        mockMvc.perform(
                post("/pregnancyObservationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(OBSERVATION_EXPECTED_DELIVERY_DATE_IN_PAST.getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

    }

    private static final String INVALID_PREG_OBS_MALE_XML =
            "<pregnancyObservationForm>" +
                    "<recorded_date>"+ A_CURRENT_DATE +"</recorded_date>" +
                    "<visit_ext_id>pregObsVisit</visit_ext_id>" +
                    "<individual_ext_id>pregnantMale</individual_ext_id>" +
                    "<processed_by_mirth>null</processed_by_mirth>" +
                    "<entity_uuid>PREG-OBS-MALE</entity_uuid>" +
                    "<field_worker_ext_id>UNK</field_worker_ext_id>" +
                    "<field_worker_uuid>UnknownFieldWorker</field_worker_uuid>" +
                    "<expected_delivery_date>"+A_FUTURE_DATE+"</expected_delivery_date>" +
                    "</pregnancyObservationForm>";

    @Test
    public void testPostMalePregnancy() throws Exception {

        mockMvc.perform(
                post("/pregnancyObservationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(INVALID_PREG_OBS_MALE_XML.getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

    }

}
