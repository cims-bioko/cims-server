package com.github.cimsbioko.server.service;

import com.vividsolutions.jts.geom.Point;

public interface GeometryService {
    Point makePoint(String k, String j);
}
