package com.github.cimsbioko.server.webapi;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public abstract class AbstractResourceTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    protected MockHttpSession getMockHttpSession(String username, String password, MockMvc mockMvc) throws Exception {
        return (MockHttpSession) mockMvc
                .perform(
                        post("/loginProcess").param("username", username).param("password",
                                password)).andReturn().getRequest().getSession();
    }

    protected MockMvc buildMockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(springSecurityFilterChain).build();
    }

}
