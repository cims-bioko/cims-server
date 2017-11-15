package com.github.cimsbioko.server.controller.service.impl;

import com.github.cimsbioko.server.controller.idgeneration.FieldWorkerGenerator;
import com.github.cimsbioko.server.controller.idgeneration.Generator;
import com.github.cimsbioko.server.controller.service.FieldWorkerService;
import com.github.cimsbioko.server.dao.GenericDao;
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

    public FieldWorker generateId(FieldWorker entityItem) throws ConstraintViolations {
        FieldWorkerGenerator fwGen = (FieldWorkerGenerator) generator;
        if (fwGen.generated)
            entityItem.setExtId(generator.generateId(entityItem));
        return entityItem;
    }
}
