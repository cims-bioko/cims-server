package org.openhds.task;

import org.apache.commons.codec.digest.DigestUtils;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Template for writing entities to an XML file
 *
 * @param <T>
 *            The type of entities to write to the file
 */
public abstract class XmlWriterTemplate<T> implements XmlWriterTask {

    private static final int PAGE_SIZE = 100;

    private CalendarAdapter calendarAdapter;
    private AsyncTaskService asyncTaskService;
    private String taskName;

    public XmlWriterTemplate(AsyncTaskService asyncTaskService, String taskName) {
        this.asyncTaskService = asyncTaskService;
        this.taskName = taskName;
        calendarAdapter = new CalendarAdapter();
    }

    @Async
    @Transactional
    public void writeXmlAsync(TaskContext taskContext) {
        writeXml(taskContext);
    }

    public void writeXml(TaskContext taskContext) {

        long totalWritten = 0;

        try {
            OutputStream outputStream = new FileOutputStream(taskContext.getDestinationFile());

            asyncTaskService.startTask(taskName);

            XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
            xmlStreamWriter.writeStartDocument();
            xmlStreamWriter.writeStartElement(getStartElementName());
            JAXBContext context = JAXBContext.newInstance(getBoundClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setAdapter(calendarAdapter);

            long batchCount;
            T lastWritten = null;
            do {

                batchCount = 0;

                List<T> entities = getEntitiesInRange(taskContext, lastWritten, PAGE_SIZE);
                for (T original : entities) {
                    T copy = makeCopyOf(original);
                    marshaller.marshal(copy, xmlStreamWriter);
                    lastWritten = copy;
                    batchCount += 1;
                }

                totalWritten += batchCount;
                asyncTaskService.updateTaskProgress(taskName, totalWritten);

                // Empty the Hibernate cache
                // Prevents excessive memory use for large data sets like locations or individuals
                // See: http://docs.jboss.org/hibernate/orm/4.0/devguide/en-US/html/ch04.html
                asyncTaskService.clearSession();

            } while ( batchCount >= PAGE_SIZE );

            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.close();
            outputStream.close();

            InputStream inputStream = new FileInputStream(taskContext.getDestinationFile());
            String md5 = DigestUtils.md5Hex(inputStream);
            inputStream.close();

            asyncTaskService.finishTask(taskName, totalWritten, md5);

        } catch (Exception e) {
            asyncTaskService.finishTask(taskName, totalWritten, e.getMessage());
        }
    }

    protected abstract T makeCopyOf(T original);

    protected abstract List<T> getEntitiesInRange(TaskContext taskContext, T lastObject, int pageSize);

    protected abstract Class<?> getBoundClass();

    protected abstract String getStartElementName();

}
