package com.github.cimsbioko.server.task.service.impl;

import org.hibernate.SessionFactory;
import com.github.cimsbioko.server.dao.Dao;
import com.github.cimsbioko.server.domain.model.AsyncTask;
import com.github.cimsbioko.server.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

@Component
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private SessionFactory sessionFactory;
    private Dao<AsyncTask, String> dao;

    @Autowired
    public AsyncTaskServiceImpl(SessionFactory sessionFactory, @Qualifier("taskDao") Dao<AsyncTask, String> dao) {
        this.sessionFactory = sessionFactory;
        this.dao = dao;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean taskShouldRun(String name) {
        return taskShouldRun(dao.findByProperty("name", name));
    }

    private boolean taskShouldRun(AsyncTask task) {
        return task == null || task.getFinished() != null;
    }

    // REQUIRES_NEW allows status updates to be written during long tasks
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startTask(String name) {
        AsyncTask task = dao.findByProperty("name", name);
        if (task == null) {
            task = new AsyncTask();
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
        AsyncTask task = dao.findByProperty("name", name);
        task.setItemCount(itemsWritten);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String getDescriptor(String name) {
        AsyncTask task = dao.findByProperty("name", name);
        return task == null ? null : task.getDescriptor();
    }

    // REQUIRES_NEW allows status updates to be written during long tasks
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finishTask(String name, long itemsWritten, String descriptorValue) {
        AsyncTask task = dao.findByProperty("name", name);
        task.setItemCount(itemsWritten);
        task.setFinished(Calendar.getInstance());
        task.setDescriptor(descriptorValue);
    }

    @Override
    public List<AsyncTask> findAll() {
        return dao.findAll(false);
    }

}
