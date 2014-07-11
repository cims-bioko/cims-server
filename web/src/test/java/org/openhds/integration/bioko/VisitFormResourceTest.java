package org.openhds.integration.bioko;

import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Visit;
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
@DatabaseSetup(value = "/visitFormResourceDb.xml", type = DatabaseOperation.REFRESH)
public class VisitFormResourceTest {
	
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private GenericDao genericDao;

    private MockHttpSession session;

    private MockMvc mockMvc;

    private static final String A_DATE = "2000-01-01T00:00:00-05:00";

	private static final String VISIT_FORM_XML =
			"<visitForm>"
			+ "<processed_by_mirth>false</processed_by_mirth>"
			+ "<visit_ext_id>1234567890aa</visit_ext_id>"
			+ "<field_worker_ext_id>FWEK1D</field_worker_ext_id>"
			+ "<location_ext_id>testLocation</location_ext_id>"
			+ "<visit_date>"
			+ A_DATE
			+ "</visit_date>"
			+ "</visitForm>";
	
    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webApplicationContextSetup(webApplicationContext)
                .addFilter(springSecurityFilterChain).build();

        session = getMockHttpSession("admin", "test");
    }
	
    @Test
    public void testPostVisitFormXml() throws Exception {
        mockMvc.perform(
                post("/visitForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(VISIT_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyVisitCrud("1234567890aa");
    }
    
    private MockHttpSession getMockHttpSession(String username, String password) throws Exception {
        return (MockHttpSession) mockMvc
                .perform(
                        post("/loginProcess").param("j_username", username).param("j_password",
                                password)).andReturn().getRequest().getSession();
    }
    
    private void verifyVisitCrud(String visitExtId) {
    	Visit visit = genericDao.findByProperty(Visit.class, "extId", visitExtId);
    	assertNotNull(visit);
    	assertEquals(visit.getExtId(), visitExtId);
    	assertEquals(visit.getVisitLocation().getExtId(), "testLocation");
    	assertEquals(visit.getCollectedBy().getExtId(), "FWEK1D");
    }
    
}
