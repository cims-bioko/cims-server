package com.github.cimsbioko.server;

import com.github.batkinson.jxlsform.api.WorkbookFactory;
import com.github.batkinson.jxlsform.api.XLSFormFactory;
import com.github.batkinson.jxlsform.xform.DefaultGenerator;
import com.github.batkinson.jxlsform.xform.Generator;
import com.github.cimsbioko.server.service.XLSFormService;
import com.github.cimsbioko.server.service.impl.XLSFormServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XLSFormConfig {

    @Bean
    XLSFormService xlsformService() {
        return new XLSFormServiceImpl(xformGenerator(), xlsformFactory(), workbookFactory());
    }

    @Bean
    XLSFormFactory xlsformFactory() {
        return new com.github.batkinson.jxlsform.common.XLSFormFactory();
    }

    @Bean
    WorkbookFactory workbookFactory() {
        return new com.github.batkinson.jxlsform.poi.WorkbookFactory();
    }

    @Bean
    Generator xformGenerator() {
        return new DefaultGenerator();
    }
}
