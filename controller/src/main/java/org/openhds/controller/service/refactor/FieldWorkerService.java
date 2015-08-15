package org.openhds.controller.service.refactor;




import org.openhds.controller.exception.ConstraintViolations;
import org.openhds.domain.model.FieldWorker;

public interface FieldWorkerService extends EntityService<FieldWorker> {


    void generatePasswordHash(FieldWorker fieldWorker) throws ConstraintViolations;

    void generateIdPrefix(FieldWorker fieldWorker);

    FieldWorker generateId(FieldWorker fieldWorker) throws ConstraintViolations;

}
