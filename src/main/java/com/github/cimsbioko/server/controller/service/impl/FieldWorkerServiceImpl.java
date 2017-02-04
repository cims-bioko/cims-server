package com.github.cimsbioko.server.controller.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.github.cimsbioko.server.controller.idgeneration.FieldWorkerGenerator;
import com.github.cimsbioko.server.controller.idgeneration.Generator;
import com.github.cimsbioko.server.controller.service.FieldWorkerService;
import com.github.cimsbioko.server.dao.service.GenericDao;
import com.github.cimsbioko.server.domain.model.FieldWorker;
import com.github.cimsbioko.server.controller.exception.ConstraintViolations;

@SuppressWarnings("unchecked")
public class FieldWorkerServiceImpl implements FieldWorkerService {

    private GenericDao genericDao;
    private Generator generator;

    public FieldWorkerServiceImpl() {
    }

    public FieldWorkerServiceImpl(GenericDao genericDao, Generator generator) {
        this.genericDao = genericDao;
        this.generator = generator;
    }

    public FieldWorker getUnknownFieldWorker() {
        return genericDao.findByProperty(FieldWorker.class, "extId", "UNK");
    }

    public FieldWorker evaluateFieldWorker(FieldWorker entityItem) throws ConstraintViolations {

        if (findFieldWorkerByExtId(entityItem.getExtId()) != null)
            throw new ConstraintViolations("The Id specified already exists");

        return generateId(entityItem);

    }

    public FieldWorker generateId(FieldWorker entityItem) throws ConstraintViolations {

        FieldWorkerGenerator fwGen = (FieldWorkerGenerator) generator;

        if (fwGen.generated)
            entityItem.setExtId(generator.generateId(entityItem));
        return entityItem;
    }

    /**
     * Retrieves all Field Worker extId's that contain the term provided.
     */
    public List<String> getFieldWorkerExtIds(String term) {
        List<String> ids = new ArrayList<>();
        List<FieldWorker> list = genericDao.findListByPropertyPrefix(FieldWorker.class, "extId", term, 10, true);
        for (FieldWorker fw : list) {
            ids.add(fw.getExtId());
        }

        return ids;
    }

    public FieldWorker findFieldWorkerByExtId(String fieldWorkerId) {
        return genericDao.findByProperty(FieldWorker.class, "extId", fieldWorkerId, true);
    }

    @Override
    public List<FieldWorker> getAllFieldWorkers() {
        return genericDao.findAll(FieldWorker.class, true);
    }
}
