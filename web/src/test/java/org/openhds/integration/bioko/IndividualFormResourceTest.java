package org.openhds.integration.bioko;

import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.xpath;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.Membership;
import org.openhds.domain.model.Residency;
import org.openhds.integration.util.WebContextLoader;
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
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(loader = WebContextLoader.class, locations = { "/testContext.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/individualFormResourceDb.xml", type = DatabaseOperation.REFRESH)
public class IndividualFormResourceTest {

    private static final String A_DATE = "2000-01-01T00:00:00-05:00";
    private static final String INDIVIDUAL_FORM_GOOD = "<individualForm>"
            + "<processed_by_mirth>false</processed_by_mirth>"
            + "<field_worker_ext_id>FWEK1D</field_worker_ext_id>" + "<collection_date_time>"
            + A_DATE
            + "</collection_date_time>"
            + "<household_ext_id>householdId</household_ext_id>"
            + "<individual_ext_id>TWELVE_DIGIT</individual_ext_id>"
            + "<individual_first_name>Test First</individual_first_name>"
            + "<individual_last_name>Test Last</individual_last_name>"
            + "<individual_other_names>Test Other</individual_other_names>"
            + "<individual_age>10</individual_age>"
            + "<individual_age_units>years</individual_age_units>"
            + "<individual_date_of_birth>"
            + A_DATE
            + "</individual_date_of_birth>"
            + "<individual_gender>M</individual_gender>"
            + "<individual_relationship_to_head_of_household>2</individual_relationship_to_head_of_household>"
            + "<individual_phone_number>12345678890</individual_phone_number>"
            + "<individual_other_phone_number>0987654321</individual_other_phone_number>"
            + "<individual_language_preference>English</individual_language_preference>"
            + "<individual_point_of_contact_name></individual_point_of_contact_name>"
            + "<individual_point_of_contact_phone_number></individual_point_of_contact_phone_number>"
            + "<individual_dip>12345</individual_dip>"
            + "<individual_member_status>permanent</individual_member_status>"
            + "</individualForm>";
    private static final String INDIVIDUAL_FORM_INCOMPLETE = "<individualForm>"
            + "<individual_first_name>Test First</individual_first_name>"
            + "<individual_last_name>Test Last</individual_last_name>"
            + "<individual_other_names>Test Other</individual_other_names>"
            + "<individual_age>10</individual_age>"
            + "<individual_age_units>years</individual_age_units>" + "</individualForm>";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private GenericDao genericDao;

    private MockHttpSession session;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webApplicationContextSetup(webApplicationContext)
                .addFilter(springSecurityFilterChain).build();

        session = getMockHttpSession("admin", "test");
    }

    @Test
    public void testPostIndividualFormXml() throws Exception {
        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(INDIVIDUAL_FORM_GOOD.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/individualForm/processed_by_mirth").string("false"))
                .andExpect(xpath("/individualForm/field_worker_ext_id").string("FWEK1D"))
                .andExpect(xpath("/individualForm/collection_date_time").string(A_DATE))
                .andExpect(xpath("/individualForm/household_ext_id").string("householdId"))
                .andExpect(xpath("/individualForm/individual_ext_id").string("TWELVE_DIGIT"))
                .andExpect(xpath("/individualForm/individual_first_name").string("Test First"))
                .andExpect(xpath("/individualForm/individual_last_name").string("Test Last"))
                .andExpect(xpath("/individualForm/individual_other_names").string("Test Other"))
                .andExpect(xpath("/individualForm/individual_age").string("10"))
                .andExpect(xpath("/individualForm/individual_age_units").string("years"))
                .andExpect(xpath("/individualForm/individual_date_of_birth").string(A_DATE))
                .andExpect(xpath("/individualForm/individual_gender").string("M"))
                .andExpect(
                        xpath("/individualForm/individual_relationship_to_head_of_household")
                                .string("2"))
                .andExpect(xpath("/individualForm/individual_phone_number").string("12345678890"))
                .andExpect(xpath("/individualForm/individual_other_phone_number").string("0987654321"))
                .andExpect(xpath("/individualForm/individual_language_preference").string("English"))
                .andExpect(xpath("/individualForm/individual_point_of_contact_name").string(""))
                .andExpect(xpath("/individualForm/individual_point_of_contact_phone_number").string(""))
                .andExpect(xpath("/individualForm/individual_dip").string("12345"))
                .andExpect(xpath("/individualForm/individual_member_status").string("permanent"));
    }

    @Test
    public void testRepeatPostIndividualFormXml() throws Exception {

        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(INDIVIDUAL_FORM_GOOD.getBytes())).andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(INDIVIDUAL_FORM_GOOD.getBytes())).andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));
    }

    @Test
    public void testPostIndividualFormXmlDataCreated() throws Exception {
        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(INDIVIDUAL_FORM_GOOD.getBytes())).andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        Individual savedIndividual = genericDao.findByProperty(Individual.class, "extId",
                "TWELVE_DIGIT");
        Assert.assertNotNull(savedIndividual);

        List<Membership> savedMemberships = genericDao.findListByProperty(Membership.class,
                "individual", savedIndividual);
        Assert.assertEquals(1, savedMemberships.size());

        List<Residency> savedResidencies = genericDao.findListByProperty(Residency.class,
                "individual", savedIndividual);
        Assert.assertEquals(1, savedResidencies.size());
    }

    @Test
    public void testPostIndividualFormIncomplete() throws Exception {

        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(INDIVIDUAL_FORM_INCOMPLETE.getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));
    }

    private MockHttpSession getMockHttpSession(String username, String password) throws Exception {
        return (MockHttpSession) mockMvc
                .perform(
                        post("/loginProcess").param("j_username", username).param("j_password",
                                password)).andReturn().getRequest().getSession();
    }
}
