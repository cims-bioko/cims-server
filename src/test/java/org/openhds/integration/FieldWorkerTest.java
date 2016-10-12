package org.openhds.integration;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.controller.service.CurrentUser;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.FieldWorker;
import org.openhds.web.crud.EntityCrud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.Assert.assertNotNull;


@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(locations = {"/httpScopes.xml", "/testContext.xml"})
public class FieldWorkerTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    @Qualifier("fieldWorkerCrud")
    EntityCrud<FieldWorker, String> fieldWorkerCrud;

    @Autowired
    SessionFactory sessionFactory;

    @Autowired
    GenericDao genericDao;

    @Autowired
    @Qualifier("currentUser")
    CurrentUser currentUser;

    MockHttpSession session;
    MockHttpServletRequest request;

    @Before
    public void startRequest() {
        session = new MockHttpSession();
        request = new MockHttpServletRequest();
        request.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @After
    public void endRequest() {
        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).requestCompleted();
        RequestContextHolder.resetRequestAttributes();
        request = null;
        session.clearAttributes();
        session = null;
    }

    @Test
    public void testFieldWorkerCreate() {

        currentUser.setProxyUser("admin", "test", new String[]{"VIEW_ENTITY", "CREATE_ENTITY"});

        FieldWorker worker = new FieldWorker();
        worker.setExtId("FWBD1");
        worker.setFirstName("Bob");
        worker.setLastName("Dow");
        worker.setPassword("test-password");
        worker.setConfirmPassword("test-password");
        fieldWorkerCrud.setItem(worker);
        fieldWorkerCrud.create();

        FieldWorker savedFieldWorker = genericDao.findByProperty(FieldWorker.class, "extId", worker.getExtId());
        assertNotNull(savedFieldWorker);
    }
}