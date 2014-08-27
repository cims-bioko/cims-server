package org.openhds.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.controller.service.CurrentUser;
import org.openhds.controller.service.ResidencyService;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Individual;
import org.openhds.domain.model.PrivilegeConstants;
import org.openhds.domain.model.Residency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(locations={"/controller-test-context.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/residencyServiceDb.xml", type = DatabaseOperation.REFRESH)
public class ResidencyServiceTest {

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private ResidencyService residencyService;

    @Autowired
    private CurrentUser currentUser;
    
    Residency residency;
    
    @Before
    public void setUp() throws Exception {
    	
    	Individual individual = genericDao.findByProperty(Individual.class, "extId", "individual1");

    	
        residency = genericDao.findByProperty(Residency.class, "individual", individual);
        assertNotNull(residency);

        currentUser.setProxyUser("admin", "test",
                new String[] {PrivilegeConstants.CREATE_ENTITY, PrivilegeConstants.VIEW_ENTITY});
    }
    
    @Test
    public void testGetAllResidencies() {
    	int count = (int)residencyService.getTotalResidencyCount();
    	assertEquals(1, count);
    }

    @Test
    public void testUpdateResidency() {

    }
	
}
