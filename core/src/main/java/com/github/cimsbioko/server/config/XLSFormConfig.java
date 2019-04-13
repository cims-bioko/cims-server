package com.github.cimsbioko.server.config;

import com.github.cimsbioko.server.service.XLSFormService;
import com.github.cimsbioko.server.service.impl.XLSFormServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class XLSFormConfig {

    @Bean
    XLSFormService xlsformService(RestTemplate restTemplate) {
        return new XLSFormServiceImpl(restTemplate);
    }

}
