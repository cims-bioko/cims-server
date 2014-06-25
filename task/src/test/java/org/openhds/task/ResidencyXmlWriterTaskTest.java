package org.openhds.task;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
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

import java.io.File;
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
public class ResidencyXmlWriterTaskTest {

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Autowired
    private ResidencyService residencyService;

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

        try {
            ResidencyXmlWriterTask task = new ResidencyXmlWriterTask(asyncTaskService, residencyService);
            TaskContext context = new TaskContext(fileToWrite);
            task.writeXml(context);

            assertTrue(fileToWrite.exists());
            String xmlWritten = new String(Files.readAllBytes(Paths.get(fileToWrite.getPath())));
            assertXpathExists("/residencies", xmlWritten);
            assertXpathExists("/residencies/residency", xmlWritten);
            assertXpathExists("/residencies/residency/startDate", xmlWritten);
            assertXpathExists("/residencies/residency/startType", xmlWritten);
            assertXpathEvaluatesTo("individual1", "/residencies/residency/individual/extId", xmlWritten);
            assertXpathEvaluatesTo("testLocation", "/residencies/residency/location/extId", xmlWritten);

        } catch (Exception e) {
            fail("Problem testing Residency XML: " + e.getMessage());

        } finally {
            if (fileToWrite.exists()) {
                fileToWrite.delete();
            }
        }
    }
}
