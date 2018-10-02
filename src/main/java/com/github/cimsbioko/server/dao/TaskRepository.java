package com.github.cimsbioko.server.dao;

import com.github.cimsbioko.server.domain.Task;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TaskRepository extends PagingAndSortingRepository<Task, String> {
    Task findByName(String name);
}
