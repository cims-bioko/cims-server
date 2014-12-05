package org.openhds.integration;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.dao.service.GenericDao;
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

import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(loader=WebContextLoader.class, locations={"/testContext.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/membershipResourceDb.xml", type = DatabaseOperation.REFRESH)
public class MembershipResourceTest {

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
                .addFilter(springSecurityFilterChain)
                .build();

        session = getMockHttpSession("admin", "test");
    }

    @Test
    public void testGetCachedMemberships() throws Exception {
        mockMvc.perform(get("/memberships/cached").session(session)
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
    }

    @Test(expected = AssertionError.class)
    public void testGetCachedMembershipsWithoutSession() throws Exception {
        mockMvc.perform(get("/memberships/cached")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAllMembershipsXml() throws Exception {
        mockMvc.perform(get("/memberships").session(session)
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/memberships").nodeCount(1))
                .andExpect(xpath("/memberships/membership/bIsToA").string("1"))
                .andExpect(xpath("/memberships/membership/individual/extId").string("individual1"))
                .andExpect(xpath("/memberships/membership/socialGroup/extId").string("sg123456789"));
    }

    @Test
    public void testGetAllMembershipsForIndividualXml() throws Exception {
        String individualExtId = "individual1";

        mockMvc.perform(get("/memberships/{extId}", individualExtId).session(session)
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/memberships").nodeCount(1))
                .andExpect(xpath("/memberships/membership/bIsToA").string("1"))
                .andExpect(xpath("/memberships/membership/individual/extId").string("individual1"))
                .andExpect(xpath("/memberships/membership/socialGroup/extId").string("sg123456789"));
    }

    @Test
    public void testGetAllMembershipsForNonexistentIndividualXml() throws Exception {
        String individualExtId = "IAmInvalid";

        mockMvc.perform(get("/memberships/{extId}", individualExtId).session(session)
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPostLocationXml() throws Exception {
        final String LOCATION_POST_XML =  "<membership>"
                + "<collectedBy>"
                + "<extId>UNK</extId>"
                + "</collectedBy>"
                + "<socialGroup>"
                + "<extId>sg234567890</extId>"
                + "</socialGroup>"
                + "<individual>"
                + "<extId>individual2</extId>"
                + "</individual>"
                + "<bIsToA>3</bIsToA>"
                + "</membership>";

        mockMvc.perform(post("/memberships").session(session)
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .body(LOCATION_POST_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML))
                .andExpect(xpath("/membership/bIsToA").string("3"))
                .andExpect(xpath("/membership/individual/extId").string("individual2"))
                .andExpect(xpath("/membership/socialGroup/extId").string("sg234567890"));
    }

    @Test
    public void testPostLocationInvalidXml() throws Exception {
        final String LOCATION_POST_XML =  "<membership>"
                + "<collectedBy>"
                + "<extId>IAmInvalid</extId>"
                + "</collectedBy>"
                + "<socialGroup>"
                + "<extId>IAmInvalid</extId>"
                + "</socialGroup>"
                + "<individual>"
                + "<extId>IAmInvalid</extId>"
                + "</individual>"
                + "<bIsToA>IAmInvalid</bIsToA>"
                + "</membership>";

        mockMvc.perform(post("/memberships").session(session)
                .accept(MediaType.APPLICATION_XML)
                .contentType(MediaType.APPLICATION_XML)
                .body(LOCATION_POST_XML.getBytes()))
                .andExpect(status().isBadRequest());
    }

    private MockHttpSession getMockHttpSession(String username, String password) throws Exception {
        return (MockHttpSession)mockMvc.perform(post("/loginProcess")
                        .param("j_username", username)
                        .param("j_password", password)
        ).andReturn()
                .getRequest()
                .getSession();
    }
}
