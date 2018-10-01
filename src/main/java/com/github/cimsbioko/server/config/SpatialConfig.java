package com.github.cimsbioko.server.config;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpatialConfig {

    @Bean
    public PrecisionModel precisionModel() {
        return new PrecisionModel();
    }

    @Bean
    public GeometryFactory geometryFactory(@Value("${app.spatial.srid}") int spatialSrid) {
        return new GeometryFactory(precisionModel());
    }

}
