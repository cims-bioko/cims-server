package com.github.cimsbioko.server.config;


import com.github.cimsbioko.server.formproc.FormProcessor;
import com.github.cimsbioko.server.formproc.FormProcessorLoggingPostProcessor;
import com.github.cimsbioko.server.formproc.ScheduledProcessing;
import com.github.cimsbioko.server.formproc.forms.*;
import com.github.cimsbioko.server.util.CalendarAdapter;
import com.github.cimsbioko.server.util.CalendarUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
@EnableAspectJAutoProxy
@ImportResource("classpath:/META-INF/spring/formproc-application-context.xml")
public class FormProcConfig {

    private static final String PROC_PKG = "com.github.cimsbioko.server.formproc";
    private static final String FORM_PKG = PROC_PKG + ".forms";

    @Bean
    CalendarUtil calendarUtil(@Value("${dateFormat:yyyy-MM-dd}") String dateFormat, @Value("${dateTimeFormat:yyyy-MM-dd HH:mm:ss}") String dateTimeFormat) {
        return new CalendarUtil(dateFormat, dateTimeFormat);
    }

    @Bean
    CalendarAdapter calendarAdapter(CalendarUtil util) {
        return new CalendarAdapter(util);
    }

    @Bean
    Jaxb2Marshaller formMarshaller(CalendarAdapter adapter) {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan(FORM_PKG);
        marshaller.setAdapters(adapter);
        return marshaller;
    }

    /**
     * This is satisfied by a javascript bean loaded by the xml config
     */
    @Bean
    ScheduledProcessing scheduledProcessing(FormProcessor processor) {
        return new ScheduledProcessing(processor);
    }

    @Bean
    BeanPostProcessor formProcessingLoggingPostProcessor() {
        return new FormProcessorLoggingPostProcessor();
    }

    @Bean
    CreateMapFormProcessor createMapFormProcessor() {
        return new CreateMapFormProcessor();
    }

    @Bean
    CreateSectorFormProcessor createSectorFormProcessor() {
        return new CreateSectorFormProcessor();
    }

    @Bean
    DuplicateLocationFormProcessor duplicateLocationFormProcessor() {
        return new DuplicateLocationFormProcessor();
    }

    @Bean
    IndividualFormProcessor individualFormProcessor() {
        return new IndividualFormProcessor();
    }

    @Bean
    LocationEvalFormProcessor locationEvalFormProcessor() {
        return new LocationEvalFormProcessor();
    }

    @Bean
    LocationFormProcessor locationFormProcessor() {
        return new LocationFormProcessor();
    }
}
