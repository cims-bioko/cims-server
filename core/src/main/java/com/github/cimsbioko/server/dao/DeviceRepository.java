package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.AccessToken;
import com.github.cimsbioko.server.domain.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends PagingAndSortingRepository<Device, String>, DeviceSearch {
    Optional<Device> findByToken(@NotNull AccessToken token);
    Optional<Device> findByName(@NotNull String name);
    Page<Device> findByDeletedIsNull(Pageable pageable);
    Optional<Device> findByNameAndDeletedIsNull(String name);
    @Query("select d from #{#entityName} d where (d.campaign is null or d.campaign.uuid = :uuid) and d.deleted is null order by d.name")
    List<Device> findSelectableForCampaign(String uuid);
}
