package org.openhds.integration;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.integration.util.WebContextLoader;
import org.openhds.task.support.FileResolver;
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

import java.io.File;

import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

/**
 * Created by ADT on 12/5/14.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(loader=WebContextLoader.class, locations={"/testContext.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/formResourceTestDb.xml", type = DatabaseOperation.REFRESH)
public class CachedEndpointResourceTest {


    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private FileResolver fileResolver;

    private MockHttpSession session;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webApplicationContextSetup(webApplicationContext)
                .addFilter(springSecurityFilterChain)
                .build();

        createTestCachedXml();

        session = getMockHttpSession("admin", "test");
    }

    private void createTestCachedXml() throws Exception {

            fileResolver.resolveIndividualFile().createNewFile();
            fileResolver.resolveLocationFile().createNewFile();
            fileResolver.resolveMembershipFile().createNewFile();
            fileResolver.resolveRelationshipFile().createNewFile();
            fileResolver.resolveSocialGroupFile().createNewFile();
            fileResolver.resolveVisitFile().createNewFile();

    }

    @After
    public void tearDown() throws Exception {

        FileUtils.cleanDirectory(new File(fileResolver.resolveIndividualFile().getParent()));

    }

    @Test
    public void testGetCachedLocations() throws Exception {
        mockMvc.perform(get("/locations/cached").session(session)
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
    }

    @Test(expected = AssertionError.class)
    public void testGetCachedLocationsWithoutSession() throws Exception {
        mockMvc.perform(get("/locations/cached")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
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
    public void testGetCachedVisits() throws Exception {
        mockMvc.perform(get("/visits/cached").session(session)
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
    }

    @Test(expected = AssertionError.class)
    public void testGetCachedVisitsWithoutSession() throws Exception {
        mockMvc.perform(get("/visits/cached")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetCachedRelationships() throws Exception {
        mockMvc.perform(get("/relationships/cached").session(session)
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
    }

    @Test(expected = AssertionError.class)
    public void testGetCachedRelationshipsWithoutSession() throws Exception {
        mockMvc.perform(get("/relationships/cached")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetCachedSocialGroups() throws Exception {
        mockMvc.perform(get("/socialgroups/cached").session(session)
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
    }

    @Test(expected = AssertionError.class)
    public void testGetCachedSocialGroupsWithoutSession() throws Exception {
        mockMvc.perform(get("/socialgroups/cached")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetCachedIndividualsGroups() throws Exception {
        mockMvc.perform(get("/individuals/cached").session(session)
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
    }

    @Test(expected = AssertionError.class)
    public void testGetCachedIndividualsWithoutSession() throws Exception {
        mockMvc.perform(get("/individuals/cached")
                .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk());
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
