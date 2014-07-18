package org.openhds.integration.bioko;


import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.webservice.WebServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;

public abstract class AbstractFormResourceTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    protected MockHttpSession getMockHttpSession(String username, String password, MockMvc mockMvc) throws Exception {
        return (MockHttpSession) mockMvc
                .perform(
                        post("/loginProcess").param("j_username", username).param("j_password",
                                password)).andReturn().getRequest().getSession();
    }

    protected MockMvc buildMockMvc() {
        return MockMvcBuilders.webApplicationContextSetup(webApplicationContext)
                .addFilter(springSecurityFilterChain).build();
    }

}
