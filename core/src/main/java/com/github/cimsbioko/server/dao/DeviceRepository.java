package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.AccessToken;
import com.github.cimsbioko.server.domain.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public interface DeviceRepository extends PagingAndSortingRepository<Device, String>, DeviceSearch {
    Optional<Device> findByToken(@NotNull AccessToken token);
    Optional<Device> findByName(@NotNull String name);
    Page<Device> findByDeletedIsNull(Pageable pageable);
    Optional<Device> findByNameAndDeletedIsNull(String name);
}
