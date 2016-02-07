package org.openhds.task;

import static org.junit.Assert.fail;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.IOException;

import org.dom4j.DocumentException;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.openhds.domain.model.Location;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.core.io.ClassPathResource;

public class LocationSyncFileTaskTest extends AbstractXmlWriterTest {

    @Mock
    private AsyncTaskService asyncTaskService;

    @Mock
    private SessionFactory sessionFactory;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    @Ignore
    public void shouldWriteXml() {
        File locationFile = new File("location.xml");
        if (locationFile.exists()) {
            locationFile.delete();
        }

        try {
            LocationSyncFileTask task = new LocationSyncFileTask(asyncTaskService, sessionFactory);

            task.writeXml(new TaskContext(locationFile));

            ClassPathResource expected = new ClassPathResource("xml/locations.xml");

            compareXmlDocuments(expected.getFile(), locationFile);
        } catch (DocumentException e) {
            fail("DocumentException testing Location XML: " + e.getMessage());
        } catch (IOException e) {
            fail("IOException testing Location XML: " + e.getMessage());
        }

        if (locationFile.exists()) {
            locationFile.delete();
        }
    }

    private Location createLocation() {
        Location location = new Location();
        location.setExtId("MBI01");

        return location;
    }

}
