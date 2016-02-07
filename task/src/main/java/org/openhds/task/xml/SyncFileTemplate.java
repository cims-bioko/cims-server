package org.openhds.task.xml;

import com.github.batkinson.jrsync.Metadata;
import com.github.batkinson.jrsync.MetadataOutputWrapper;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionContract;
import org.hibernate.StatelessSession;
import org.openhds.domain.model.UuidIdentifiable;
import org.openhds.domain.util.CalendarAdapter;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.task.SyncFileTask;
import org.openhds.task.TaskContext;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

/**
 * Template for writing entities to an XML file
 *
 * @param <T> The type of entities to write to the file
 */
public abstract class SyncFileTemplate<T> implements SyncFileTask {

    private static final int PAGE_SIZE = 1000;
    private static final int DEFAULT_SYNC_BLOCK_SIZE = 8192;
    private static final String MD5 = "MD5";

    private SessionFactory sessionFactory;
    private CalendarAdapter calendarAdapter;
    private AsyncTaskService asyncTaskService;
    private String taskName;

    public SyncFileTemplate(AsyncTaskService asyncTaskService, SessionFactory factory, String taskName) {
        this.asyncTaskService = asyncTaskService;
        this.sessionFactory = factory;
        this.taskName = taskName;
        calendarAdapter = new CalendarAdapter();
    }

    @Async
    @Transactional(readOnly = true)
    public void run(TaskContext taskContext) {
        writeXml(taskContext);
    }

    @SuppressWarnings("unchecked")
    public void writeXml(TaskContext taskContext) {

        long totalWritten = 0;
        try {

            File dest = taskContext.getDestinationFile();

            // Protect clients from downloading partial content (write and move)
            File scratch = new File(dest.getParentFile(), dest.getName() + ".tmp");
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(scratch));

            // Wrap the stream so we compute sync metadata on-the-fly, keep reference for post-processing
            MetadataOutputWrapper metadataOut = new MetadataOutputWrapper(outputStream, "", getSyncBlockSize(), MD5, MD5);
            outputStream = metadataOut;

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

                Query exportQuery = createQuery(session, getExportQuery())
                        .setReadOnly(true)
                        .setFetchSize(Integer.MIN_VALUE);

                for (Map.Entry<String, Object> param : getQueryParams(taskContext).entrySet()) {
                    exportQuery.setParameter(param.getKey(), param.getValue());
                }

                results = exportQuery.scroll(ScrollMode.FORWARD_ONLY);

                while (results.next()) {

                    T original = ((T[]) results.get())[0];

                    if (!skipEntity(original)) {
                        writeXml(xmlStreamWriter, marshaller, original);
                        totalWritten++;
                    }

                    if (totalWritten % PAGE_SIZE == 0) {
                        xmlStreamWriter.flush();
                        asyncTaskService.updateTaskProgress(taskName, totalWritten);
                    }
                }

                xmlStreamWriter.writeEndElement();
                xmlStreamWriter.close();

            } finally {
                if (results != null) {
                    try {
                        results.close();
                    } catch (Exception e) {
                    }
                }
                if (session != null) {
                    try {
                        session.close();
                    } catch (Exception e) {
                    }
                }
                try {
                    outputStream.close();
                } catch (Exception e) {
                }
            }

            if (scratch.renameTo(dest)) {

                // Install the new file, generated metadata and update content hash

                File metaDest = new File(dest.getParentFile(), dest.getName() + ".jrsmd");
                metadataOut.getMetadataFile().renameTo(metaDest);

                String md5;
                try (DataInputStream metaStream = new DataInputStream(new FileInputStream(metaDest))) {
                    md5 = encodeHexString(Metadata.read(metaStream).getFileHash());
                }

                asyncTaskService.finishTask(taskName, totalWritten, md5);

            } else {
                throw new RuntimeException("failed to move generated file");
            }

        } catch (Exception e) {
            asyncTaskService.finishTask(taskName, totalWritten, e.getMessage());
        }
    }

    protected int getSyncBlockSize() {
        return DEFAULT_SYNC_BLOCK_SIZE;
    }

    abstract void writeXml(XMLStreamWriter xmlStreamWriter, Marshaller marshaller, T original) throws JAXBException;

    protected boolean skipEntity(T entity) {
        return false;
    }

    protected Query createQuery(SharedSessionContract session, String query) {
        return session.createQuery(query);
    }

    protected abstract String getExportQuery();

    protected Map<String, Object> getQueryParams(TaskContext ctx) {
        return Collections.emptyMap();
    }

    protected abstract Class<?> getBoundClass();

    protected abstract String getStartElementName();

}
