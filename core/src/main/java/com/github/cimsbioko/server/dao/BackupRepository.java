package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Backup;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BackupRepository extends PagingAndSortingRepository<Backup, String> {
}
