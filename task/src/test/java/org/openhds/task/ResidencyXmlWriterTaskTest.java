package org.openhds.task;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.controller.service.CurrentUser;
import org.openhds.controller.service.ResidencyService;
import org.openhds.domain.model.PrivilegeConstants;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/task-test-context.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/taskTestDb.xml", type = DatabaseOperation.REFRESH)
@Transactional
public class ResidencyXmlWriterTaskTest {

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private CurrentUser currentUser;

    @Before
    public void setUp() {
        initMocks(this);
        currentUser.setProxyUser("admin", "test",
                new String[]{PrivilegeConstants.CREATE_ENTITY, PrivilegeConstants.VIEW_ENTITY});
    }

    @Test
    public void shouldWriteXml() {

        File fileToWrite = new File("residencies-test.xml");
        if (fileToWrite.exists()) {
            fileToWrite.delete();
        }

        try {
            ResidencyXmlWriterTask task = new ResidencyXmlWriterTask(asyncTaskService, sessionFactory);
            TaskContext context = new TaskContext(fileToWrite);
            task.writeXml(context);

            assertTrue(fileToWrite.exists());
            String xmlWritten = new String(Files.readAllBytes(Paths.get(fileToWrite.getPath())));
            assertXpathExists("/residencies", xmlWritten);
            assertXpathExists("/residencies/residency", xmlWritten);
            assertXpathExists("/residencies/residency/startDate", xmlWritten);
            assertXpathExists("/residencies/residency/startType", xmlWritten);
            assertXpathEvaluatesTo("Individual1", "/residencies/residency/individual/uuid", xmlWritten);
            assertXpathEvaluatesTo("LOCATION1", "/residencies/residency/location/uuid", xmlWritten);
        } catch (IOException e) {
            fail("IOException testing Residency XML: " + e.getMessage());
        } catch (SAXException e) {
            fail("SAXException testing Residency XML: " + e.getMessage());
        } catch (XpathException e) {
            fail("XpathException testing Residency XML: " + e.getMessage());
        }

        if (fileToWrite.exists()) {
            fileToWrite.delete();
        }
    }
}
