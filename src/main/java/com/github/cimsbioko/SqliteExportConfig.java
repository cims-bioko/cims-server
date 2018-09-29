package com.github.cimsbioko;

import com.github.cimsbioko.server.sqliteexport.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class SqliteExportConfig {

    @Bean
    public Source source(DataSource dataSource, @Value("${app.export.fetchsize}") int fetchSize) {
        return new DSSource(dataSource, fetchSize);
    }

    @Bean(initMethod = "loadDriver")
    public Target target() {
        return new SqliteTarget();
    }

    @Bean
    public TypeExtractor dbTypeExtractor() {
        return new DBTypeExtractor();
    }

    @Bean
    public MapperFactory mapperFactory() {
        return new SqliteMapperFactory(dbTypeExtractor());
    }

    @Bean
    Exporter exporter(Source source) {
        return new Exporter(source, target(), mapperFactory());
    }
}
