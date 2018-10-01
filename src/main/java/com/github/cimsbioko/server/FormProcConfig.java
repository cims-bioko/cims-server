package com.github.cimsbioko.server;


import com.github.cimsbioko.server.formproc.FormProcessor;
import com.github.cimsbioko.server.formproc.ScheduledProcessing;
import com.github.cimsbioko.server.util.CalendarAdapter;
import org.springframework.context.annotation.*;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import static com.github.cimsbioko.server.FormProcConfig.PROC_PKG;

@Configuration
@ComponentScan(PROC_PKG)
@EnableAspectJAutoProxy
@ImportResource("classpath:/META-INF/spring/formproc-application-context.xml")
public class FormProcConfig {

    static final String PROC_PKG = "com.github.cimsbioko.server.formproc";
    private static final String FORM_PKG = PROC_PKG + ".forms";

    @Bean
    Jaxb2Marshaller formMarshaller(CalendarAdapter calendarAdapter) {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan(FORM_PKG);
        marshaller.setAdapters(calendarAdapter);
        return marshaller;
    }

    @Bean
    ScheduledProcessing scheduledProcessing(FormProcessor processor) {
        return new ScheduledProcessing(processor);
    }
}
