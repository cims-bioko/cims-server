package com.github.cimsbioko.server.integration;

import com.github.cimsbioko.server.integration.util.WebContextLoader;
import com.github.cimsbioko.server.webapi.CacheFileResource;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.github.cimsbioko.server.task.support.FileResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;

import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(loader = WebContextLoader.class, locations = {"/testContext.xml"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
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
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(springSecurityFilterChain)
                .build();

        createTestFiles();

        session = getMockHttpSession("admin", "test");
    }

    private void createTestFiles() throws Exception {
        fileResolver.resolveMobileDBFile().createNewFile();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.cleanDirectory(new File(fileResolver.resolveMobileDBFile().getParent()));
    }

    @Test
    public void testGetCachedDB() throws Exception {
        mockMvc.perform(get("/mobiledb/cached").session(session)
                .accept(parseMediaType(CacheFileResource.SQLITE_MIME_TYPE)))
                .andExpect(status().isOk());
    }

    @Test(expected = AssertionError.class)
    public void testGetCachedDBWithoutSession() throws Exception {
        mockMvc.perform(get("/mobiledb/cached")
                .accept(parseMediaType(CacheFileResource.SQLITE_MIME_TYPE)))
                .andExpect(status().isOk());
    }

    private MockHttpSession getMockHttpSession(String username, String password) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/loginProcess")
                .param("username", username)
                .param("password", password)
        ).andReturn()
                .getRequest()
                .getSession();
    }


}
