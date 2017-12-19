package com.github.cimsbioko.server.controller.service.impl;

import com.github.cimsbioko.server.dao.Dao;
import com.github.cimsbioko.server.domain.model.Task;
import com.github.cimsbioko.server.controller.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

@Component
public class TaskServiceImpl implements TaskService {

    private Dao<Task, String> dao;

    @Autowired
    public TaskServiceImpl(@Qualifier("taskDao") Dao<Task, String> dao) {
        this.dao = dao;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean taskShouldRun(String name) {
        return taskShouldRun(dao.findByProperty("name", name));
    }

    private boolean taskShouldRun(Task task) {
        return task == null || task.getFinished() != null;
    }

    // REQUIRES_NEW allows status updates to be written during long tasks
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startTask(String name) {
        Task task = dao.findByProperty("name", name);
        if (task == null) {
            task = new Task();
            task.setName(name);
        }
        task.setFinished(null);
        task.setItemCount(0);
        task.setStarted(Calendar.getInstance());
        dao.saveOrUpdate(task);
    }

    // REQUIRES_NEW allows status updates to be written during long tasks
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateTaskProgress(String name, long itemsWritten) {
        Task task = dao.findByProperty("name", name);
        task.setItemCount(itemsWritten);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String getDescriptor(String name) {
        Task task = dao.findByProperty("name", name);
        return task == null ? null : task.getDescriptor();
    }

    // REQUIRES_NEW allows status updates to be written during long tasks
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finishTask(String name, long itemsWritten, String descriptorValue) {
        Task task = dao.findByProperty("name", name);
        task.setItemCount(itemsWritten);
        task.setFinished(Calendar.getInstance());
        task.setDescriptor(descriptorValue);
    }

    @Override
    public List<Task> findAll() {
        return dao.findAll(false);
    }

}
