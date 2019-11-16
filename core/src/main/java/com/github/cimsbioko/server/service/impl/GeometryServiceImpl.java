package com.github.cimsbioko.server.service.impl;

import com.github.cimsbioko.server.service.GeometryService;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class GeometryServiceImpl implements GeometryService {

    private final GeometryFactory geometryFactory;

    public GeometryServiceImpl(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    @Override
    public Point makePoint(String longitude, String latitude) {
        Double lng = Double.parseDouble(longitude), lat = Double.parseDouble(latitude);
        Coordinate coord = new Coordinate(lng, lat);
        return geometryFactory.createPoint(coord);
    }
}
