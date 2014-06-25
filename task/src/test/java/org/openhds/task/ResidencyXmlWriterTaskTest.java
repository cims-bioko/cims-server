package org.openhds.task;

import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.controller.service.CurrentUser;
import org.openhds.controller.service.ResidencyService;
import org.openhds.domain.model.PrivilegeConstants;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
//@Transactional
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
                new String[] {PrivilegeConstants.CREATE_ENTITY, PrivilegeConstants.VIEW_ENTITY});
	}
	
	@Test
	public void shouldWriteXml() {
		
		ResidencyXmlWriterTask task = new ResidencyXmlWriterTask(asyncTaskService, residencyService);
		File fileToWrite = new File("residencies.xml");
		TaskContext context = new TaskContext(fileToWrite);
		task.writeXml(context);

	}
	

	

}
