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
    private static final String INDIVIDUAL_FORM_POST_XML = "<individualForm>"
            + "<processedByMirth>false</processedByMirth>"
            + "<fieldWorkerExtId>FWEK1D</fieldWorkerExtId>" + "<collectionDateTime>"
            + A_DATE
            + "</collectionDateTime>"
            + "<householdExtId>householdId</householdExtId>"
            + "<individualExtId>TWELVE_DIGIT</individualExtId>"
            + "<individualFirstName>Test First</individualFirstName>"
            + "<individualLastName>Test Last</individualLastName>"
            + "<individualOtherNames>Test Other</individualOtherNames>"
            + "<individualAge>10</individualAge>"
            + "<individualAgeUnits>years</individualAgeUnits>"
            + "<individualDateOfBirth>"
            + A_DATE
            + "</individualDateOfBirth>"
            + "<individualGender>M</individualGender>"
            + "<individualRelationshipToHeadOfHousehold>2</individualRelationshipToHeadOfHousehold>"
            + "<individualPhoneNumber>12345678890</individualPhoneNumber>"
            + "<individualOtherPhoneNumber>0987654321</individualOtherPhoneNumber>"
            + "<individualLanguagePreference>English</individualLanguagePreference>"
            + "<individualPointOfContactName></individualPointOfContactName>"
            + "<individualPointOfContactPhoneNumber></individualPointOfContactPhoneNumber>"
            + "<individualDip>12345</individualDip>"
            + "<individualMemberStatus>permanent</individualMemberStatus>" + "</individualForm>";

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
                        .body(INDIVIDUAL_FORM_POST_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/individualForm/processedByMirth").string("false"))
                .andExpect(xpath("/individualForm/fieldWorkerExtId").string("FWEK1D"))
                .andExpect(xpath("/individualForm/collectionDateTime").string(A_DATE))
                .andExpect(xpath("/individualForm/householdExtId").string("householdId"))
                .andExpect(xpath("/individualForm/individualExtId").string("TWELVE_DIGIT"))
                .andExpect(xpath("/individualForm/individualFirstName").string("Test First"))
                .andExpect(xpath("/individualForm/individualLastName").string("Test Last"))
                .andExpect(xpath("/individualForm/individualOtherNames").string("Test Other"))
                .andExpect(xpath("/individualForm/individualAge").string("10"))
                .andExpect(xpath("/individualForm/individualAgeUnits").string("years"))
                .andExpect(xpath("/individualForm/individualDateOfBirth").string(A_DATE))
                .andExpect(xpath("/individualForm/individualGender").string("M"))
                .andExpect(
                        xpath("/individualForm/individualRelationshipToHeadOfHousehold")
                                .string("2"))
                .andExpect(xpath("/individualForm/individualPhoneNumber").string("12345678890"))
                .andExpect(xpath("/individualForm/individualOtherPhoneNumber").string("0987654321"))
                .andExpect(xpath("/individualForm/individualLanguagePreference").string("English"))
                .andExpect(xpath("/individualForm/individualPointOfContactName").string(""))
                .andExpect(xpath("/individualForm/individualPointOfContactPhoneNumber").string(""))
                .andExpect(xpath("/individualForm/individualDip").string("12345"))
                .andExpect(xpath("/individualForm/individualMemberStatus").string("permanent"));
    }

    @Test
    public void testRepeatPostIndividualFormXml() throws Exception {

        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(INDIVIDUAL_FORM_POST_XML.getBytes())).andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(INDIVIDUAL_FORM_POST_XML.getBytes())).andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));
    }

    @Test
    public void testPostIndividualFormXmlDataCreated() throws Exception {
        mockMvc.perform(
                post("/individualForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(INDIVIDUAL_FORM_POST_XML.getBytes())).andExpect(status().isCreated())
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

    private MockHttpSession getMockHttpSession(String username, String password) throws Exception {
        return (MockHttpSession) mockMvc
                .perform(
                        post("/loginProcess").param("j_username", username).param("j_password",
                                password)).andReturn().getRequest().getSession();
    }
}
