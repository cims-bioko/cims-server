package org.openhds.integration;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.controller.service.CurrentUser;
import org.openhds.controller.service.MembershipService;
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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(locations={"/controller-test-context.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/membershipServiceDb.xml", type = DatabaseOperation.REFRESH)
public class MembershipServiceTest {

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private CurrentUser currentUser;

    private Individual individual1;

    private Individual individual2;

    private SocialGroup socialGroup1;

    private SocialGroup socialGroup2;

    private FieldWorker fieldWorker;

    @Before
    public void setUp() throws Exception {
        individual1 = genericDao.findByProperty(Individual.class, "extId", "individual1");
        assertNotNull(individual1);

        individual2 = genericDao.findByProperty(Individual.class, "extId", "individual2");
        assertNotNull(individual2);

        socialGroup1 = genericDao.findByProperty(SocialGroup.class, "extId", "sg123456789");
        assertNotNull(socialGroup1);

        socialGroup2 = genericDao.findByProperty(SocialGroup.class, "extId", "sg234567890");
        assertNotNull(socialGroup2);

        fieldWorker = genericDao.findByProperty(FieldWorker.class, "extId", "UNK");
        assertNotNull(fieldWorker);

        currentUser.setProxyUser("admin", "test",
                new String[] {PrivilegeConstants.CREATE_ENTITY, PrivilegeConstants.VIEW_ENTITY});
    }

    @Test
    public void testGetAll() throws Exception {
        List<Membership> membershipList = membershipService.getAllMemberships();
        assertNotNull(membershipList);
        assertEquals(1, membershipList.size());
    }

    @Test
    public void testGetAllForIndividual() throws Exception {
        List<Membership> membershipList = membershipService.getAllMemberships(individual1);
        assertNotNull(membershipList);
        assertEquals(1, membershipList.size());

        Membership membership = membershipList.get(0);
        assertEquals(individual1, membership.getIndividual());
    }

    @Test
    public void testCreateMembership() throws Exception {
        Membership membership = new Membership();
        membership.setIndividual(individual2);
        membership.setSocialGroup(socialGroup1);
        membership.setCollectedBy(fieldWorker);

        membershipService.evaluateMembership(membership);
        membershipService.createMembership(membership);

        List<Membership> membershipList = membershipService.getAllMemberships(individual2);
        assertNotNull(membershipList);
        assertEquals(1, membershipList.size());

        Membership savedMembership = membershipList.get(0);
        assertEquals(individual2, savedMembership.getIndividual());
    }

    @Test
    public void testNoSuchMembership() throws Exception {
        List<Membership> membershipList = membershipService.getAllMemberships(individual2);
        assertNotNull(membershipList);
        assertEquals(0, membershipList.size());
    }
}
