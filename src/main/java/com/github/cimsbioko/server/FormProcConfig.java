package com.github.cimsbioko.server;


import com.github.cimsbioko.server.util.CalendarAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import static com.github.cimsbioko.server.FormProcConfig.PROC_PKG;

@Configuration
@ComponentScan(PROC_PKG)
public class FormProcConfig {

    public static final String PROC_PKG = "com.github.cimsbioko.server.formproc";

    @Bean
    Jaxb2Marshaller formMarshaller(CalendarAdapter calendarAdapter) {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan(PROC_PKG);
        marshaller.setAdapters(calendarAdapter);
        return marshaller;
    }
}
