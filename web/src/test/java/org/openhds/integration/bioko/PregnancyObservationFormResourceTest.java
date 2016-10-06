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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private static final String A_FUTURE_DATE = "2020-01-01T00:00:00-05:00";

    private static final String VALID_OBSERVATION_XML =
            "<pregnancyObservationForm>" +
            "<recorded_date>"+ A_CURRENT_DATE +"</recorded_date>" +
            "<collection_date_time>"+ A_CURRENT_DATE +"</collection_date_time>" +
            "<visit_ext_id>pregObsVisit</visit_ext_id>" +
                    "<visit_uuid>PregObsVisit</visit_uuid>" +
                    "<individual_ext_id>pregnantIndividual</individual_ext_id>" +
                    "<individual_uuid>PregnantIndividual</individual_uuid>" +
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
                        .content(VALID_OBSERVATION_XML))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        verifyPregnancyObservationCrud("PregnantIndividual");

    }

    private void verifyPregnancyObservationCrud(String uuid) {

        Individual mother = genericDao.findByProperty(Individual.class, "uuid", uuid);
        PregnancyObservation obs = genericDao.findByProperty(PregnancyObservation.class, "mother", mother);
        assertNotNull(obs);
        assertEquals(mother, obs.getMother());

    }

    private static final String INVALID_PREG_OBS_MALE_XML =
            "<pregnancyObservationForm>" +
                    "<recorded_date>"+ A_CURRENT_DATE +"</recorded_date>" +
                    "<visit_ext_id>pregObsVisit</visit_ext_id>" +
                    "<individual_ext_id>pregnantMale</individual_ext_id>" +
                    "<individual_uuid>PregnantMale</individual_uuid>" +
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
                        .content(INVALID_PREG_OBS_MALE_XML))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

    }

}
