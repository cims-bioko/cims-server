package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeviceSearch {
        Page<Device> findBySearch(String query, Pageable page);
}