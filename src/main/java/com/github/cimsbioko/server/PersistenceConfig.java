package com.github.cimsbioko.server;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class PersistenceConfig {

    @Autowired
    private DataSource dataSource;

    @Value("${app.hibernate.export}")
    private String ddlMode;

    @Value("${app.hibernate.dialect}")
    private String dialect;

    @Bean
    LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
        sfb.setDataSource(dataSource);
        sfb.setPackagesToScan("com.github.cimsbioko.server.domain");
        Properties props = new Properties();
        props.setProperty("hibernate.dialect", dialect);
        props.setProperty("javax.persistence.validation.mode", "none");
        props.setProperty("hibernate.hbm2ddl.auto", ddlMode);
        sfb.setHibernateProperties(props);
        return sfb;
    }

    @Bean
    PlatformTransactionManager transactionManager(@Autowired SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }
}
