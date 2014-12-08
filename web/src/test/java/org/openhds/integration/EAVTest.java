package org.openhds.integration;

import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.controller.service.CurrentUser;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.ClassExtension;
import org.openhds.domain.model.EntityType;
import org.openhds.domain.model.Extension;
import org.openhds.domain.model.FieldWorker;
import org.openhds.domain.model.Location;
import org.openhds.domain.model.PrimitiveType;
import org.openhds.domain.model.Visit;
import org.openhds.domain.util.CalendarUtil;
import org.openhds.integration.util.JsfServiceMock;
import org.openhds.web.crud.impl.ExtensionCrudImpl;
import org.openhds.web.crud.impl.VisitCrudImpl;
import org.openhds.web.service.JsfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration("/testContext.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/formResourceTestDb.xml", type = DatabaseOperation.REFRESH)
public class EAVTest {
	
	 @Autowired
	 @Qualifier("extensionCrud")
	 ExtensionCrudImpl extensionCrud;
	 
	 @Autowired
	 @Qualifier("visitCrud")
	 VisitCrudImpl visitCrud;

	 @Autowired
	 SessionFactory sessionFactory;
	 
	 @Autowired
	 GenericDao genericDao;
	 
	 @Autowired
	 CalendarUtil calendarUtil;
	
	 @Autowired
	 JsfService jsfService;
	 	 
	 @Autowired
	 @Qualifier("currentUser")
	 CurrentUser currentUser;
	 
	 JsfServiceMock jsfServiceMock;
	 FieldWorker fieldWorker;
	 Location location;
	
	 @Before
	 public void setUp() {
		 
		 jsfServiceMock = (JsfServiceMock)jsfService;
		 currentUser.setProxyUser("admin", "test", new String[] {"VIEW_ENTITY", "CREATE_ENTITY"});
		 
		 fieldWorker = genericDao.findByProperty(FieldWorker.class, "extId", "FWEK1D");
		 location = genericDao.findByProperty(Location.class, "extId", "testLocation1");
	 }
	 
	 @Test
	 public void testAttributeCreate() {
		 
		 ClassExtension extension = createAttribute();
		 		 
		 ClassExtension savedAttribute = genericDao.findByProperty(ClassExtension.class, "name", extension.getName());
		 assertNotNull(savedAttribute);
	 }

	 
	 private ClassExtension createAttribute() {
		 
		 ClassExtension extension = new ClassExtension();
		 extension.setName("waterSource");
		 extension.setDescription("Location water source");
		 extension.setAnswers("Bore Hole, Taps, Well, Other");
		 extension.setPrimType(PrimitiveType.MULTIPLECHOICE);
		 extension.setRoundNumber(1);
		 extension.setEntityClass(EntityType.LOCATION);
		 
		 extensionCrud.setItem(extension);
		 extensionCrud.create();
		 
		 return extension;
	 }
}
