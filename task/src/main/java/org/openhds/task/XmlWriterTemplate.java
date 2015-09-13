package org.openhds.task;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Template for writing entities to an XML file
 *
 * @param <T> The type of entities to write to the file
 */
public abstract class XmlWriterTemplate<T> implements XmlWriterTask {

    private static final int PAGE_SIZE = 100;

    private SessionFactory sessionFactory;
    private CalendarAdapter calendarAdapter;
    private AsyncTaskService asyncTaskService;
    private String taskName;

    public XmlWriterTemplate(AsyncTaskService asyncTaskService, SessionFactory factory, String taskName) {
        this.asyncTaskService = asyncTaskService;
        this.sessionFactory = factory;
        this.taskName = taskName;
        calendarAdapter = new CalendarAdapter();
    }

    @Async
    @Transactional(readOnly = true)
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

            StatelessSession session = null;
            ScrollableResults results = null;

            try {

                session = sessionFactory.openStatelessSession();

                Query exportQuery = session.createQuery(getExportQuery())
                        .setReadOnly(true)
                        .setFetchSize(Integer.MIN_VALUE);

                for (Map.Entry<String,Object> param : getQueryParams(taskContext).entrySet()) {
                    exportQuery.setParameter(param.getKey(), param.getValue());
                }

                results = exportQuery.scroll(ScrollMode.FORWARD_ONLY);

                while (results.next()) {

                    T original = ((T[]) results.get())[0];

                    boolean writeEntity = !skipEntity(original);

                    if (writeEntity) {
                        T copy = makeCopyOf(original);
                        marshaller.marshal(copy, xmlStreamWriter);
                        totalWritten++;
                    }

                    if (totalWritten % PAGE_SIZE == 0) {
                        xmlStreamWriter.flush();
                        asyncTaskService.updateTaskProgress(taskName, totalWritten);
                    }
                }
            } finally {
                if (results != null) {
                    results.close();
                }
                if (session != null) {
                    session.close();
                }
            }

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

    protected boolean skipEntity(T entity) {
        return false;
    }

    ;

    protected abstract T makeCopyOf(T original);

    protected abstract String getExportQuery();

    protected Map<String, Object> getQueryParams(TaskContext ctx) {
        return Collections.emptyMap();
    }

    protected abstract Class<?> getBoundClass();

    protected abstract String getStartElementName();

}
