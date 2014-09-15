package org.openhds.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.controller.service.CurrentUser;

import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.*;
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

import java.lang.reflect.Member;


@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(locations={"/controller-test-context.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/serviceTestDb.xml", type = DatabaseOperation.REFRESH)
public class OutMigrationServiceTest {

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private CurrentUser currentUser;

    private Individual individual1;

    private Individual individual2;

    private SocialGroup socialGroup1;

    private Membership membership1;

    @Before
    public void setUp() throws Exception {

        individual1 = genericDao.findByProperty(Individual.class, "extId", "TestIndividual1ExtId");
        assertNotNull(individual1);

        individual2 = genericDao.findByProperty(Individual.class, "extId", "TestIndividual2ExtId");
        assertNotNull(individual2);

        socialGroup1 = genericDao.findByProperty(SocialGroup.class, "extId", "TestSGExtId");
        assertNotNull(socialGroup1);

        membership1 = genericDao.findByProperty(Membership.class, "individual", individual1);
        assertNotNull(membership1);
        assertEquals(individual1.getExtId(),membership1.getIndividual().getExtId());

        currentUser.setProxyUser("admin", "test",
                new String[] {PrivilegeConstants.CREATE_ENTITY, PrivilegeConstants.VIEW_ENTITY});
    }

    @Test
    public void testGetAll() {

    }

	
}
