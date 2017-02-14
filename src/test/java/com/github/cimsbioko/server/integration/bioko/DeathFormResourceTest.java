package com.github.cimsbioko.server.integration.bioko;

import com.github.cimsbioko.server.dao.GenericDao;
import com.github.cimsbioko.server.domain.model.Death;
import com.github.cimsbioko.server.domain.model.Individual;
import com.github.cimsbioko.server.domain.model.Membership;
import com.github.cimsbioko.server.domain.model.Residency;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
public class DeathFormResourceTest extends AbstractResourceTest {

    @Autowired
    GenericDao genericDao;

    private MockHttpSession session;

    private MockMvc mockMvc;

    private static final String A_DATE = "2000-01-01T00:00:00-05:00";


    private static final String DEATH_FORM_XML_VALID =
            "<deathForm>"
                    + "<processed_by_mirth>false</processed_by_mirth>"
                    + "<individual_ext_id>individual1</individual_ext_id>"
                    + "<individual_uuid>Individual1</individual_uuid>"
                    + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>"
                    + "<field_worker_uuid>FWEK1D</field_worker_uuid>"
                    + "<visit_ext_id>deathVisit</visit_ext_id>"
                    + "<visit_uuid>DeathVisit</visit_uuid>"
                    + "<collection_date_time>"
                    + A_DATE
                    + "</collection_date_time>"
                    + "<date_of_death>"
                    + A_DATE
                    + "</date_of_death>"
                    + "<place_of_death>placeOfDeath</place_of_death>"
                    + "<cause_of_death>causeOfDeath</cause_of_death>"
                    + "</deathForm>";

    private static final String DEATH_FORM_XML_INVALID =
            "<deathForm>"
                    + "<processed_by_mirth>false</processed_by_mirth>"
                    + "<individual_ext_id>notAnIndividualExtId</individual_ext_id>"
                    + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>"
                    + "<visit_ext_id>deathVisit</visit_ext_id>"
                    + "<collection_date_time>"
                    + A_DATE
                    + "</collection_date_time>"
                    + "<date_of_death>"
                    + A_DATE
                    + "</date_of_death>"
                    + "<place_of_death>place</place_of_death>"
                    + "<cause_of_death>cause</cause_of_death>"
                    + "</deathForm>";

    @Before
    public void setUp() throws Exception {
        mockMvc = buildMockMvc();
        session = getMockHttpSession("admin", "test", mockMvc);
    }

    @Test
    public void testPostDeathFormXmlNoIndividual() throws Exception {
        mockMvc.perform(
                post("/deathForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(DEATH_FORM_XML_INVALID))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));
    }

    @Test
    public void testPostDeathFormXml() throws Exception {
        mockMvc.perform(
                post("/deathForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .content(DEATH_FORM_XML_VALID))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));

        verifyDeathCrud("individual1");

    }

    public void verifyDeathCrud(String individualExtId) throws Exception {

        Individual deceasedIndividual = genericDao.findByProperty(Individual.class, "extId", individualExtId);
        assertNotNull(deceasedIndividual);
        assertEquals("DTH", deceasedIndividual.getCurrentResidency().getEndType());

        Residency formerResidence = genericDao.findByProperty(Residency.class, "individual", deceasedIndividual);
        assertNotNull(formerResidence);
        assertEquals("DTH", formerResidence.getEndType());

        Membership formerMembership = genericDao.findByProperty(Membership.class, "individual", deceasedIndividual);
        assertNotNull(formerMembership);
        assertEquals("DTH", formerMembership.getEndType());

        Death death = genericDao.findByProperty(Death.class, "individual", deceasedIndividual);
        assertNotNull(death);
        assertEquals("placeOfDeath", death.getDeathPlace());
        assertEquals("causeOfDeath", death.getDeathCause());

    }
}
