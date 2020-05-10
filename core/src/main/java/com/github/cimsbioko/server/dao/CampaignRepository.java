package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, String>, CampaignSearch {
    @Query("select c from #{#entityName} c where" +
            " (c.end is null or current_date() < c.end) and (c.start is null or current_date() > c.start)" +
            " and c.disabled is null and c.deleted is null")
    List<Campaign> findActive();
    @Query("select c from #{#entityName} c where" +
            " (c.end is null or current_date() < c.end) and (c.start is null or current_date() > c.start)" +
            " and c.disabled is null and c.deleted is null and c.uuid = :uuid")
    Optional<Campaign> findActiveByUuid(@Param("uuid") String uuid);
    Optional<Campaign> findByName(String name);
    @Query("select c from #{#entityName} c where c.defaultCampaign = TRUE")
    Optional<Campaign> findDefault();
    Page<Campaign> findByDeletedIsNull(Pageable pageable);
    @Query("select c from #{#entityName} c where" +
            " (c.end is null or current_date() < c.end) and (c.start is null or current_date() > c.start)" +
            " and c.disabled is null and c.deleted is null" +
            " and exists (select d from Device d where d.name = :deviceName and d member of c.devices)")
    List<Campaign> findActiveForDevice(@Param("deviceName") String deviceName);
    @Query("select c from #{#entityName} c where" +
            " (c.end is null or current_date() < c.end) and (c.start is null or current_date() > c.start)" +
            " and c.disabled is null and c.deleted is null" +
            " and exists (select u from User u where u.username = :userName and u member of c.users)")
    List<Campaign> findActiveForUser(String userName);
    @Query("select case when count(c) > 0 then true else false end" +
            " from #{#entityName} c" +
            " join c.devices d where" +
            " (c.end is null or current_date() < c.end) and (c.start is null or current_date() > c.start)" +
            " and c.disabled is null and c.deleted is null" +
            " and c.uuid = :campaignUuid and d.name = :deviceName")
    boolean isDeviceActiveMember(String campaignUuid, String deviceName);
    @Query("select case when count(c) > 0 then true else false end" +
            " from #{#entityName} c" +
            " join c.users u where" +
            " (c.end is null or current_date() < c.end) and (c.start is null or current_date() > c.start)" +
            " and c.disabled is null and c.deleted is null" +
            " and c.uuid = :campaignUuid and u.username = :userName")
    boolean isUserActiveMember(String campaignUuid, String userName);
}