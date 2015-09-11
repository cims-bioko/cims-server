package org.openhds.task;

import java.util.List;

import org.openhds.controller.service.ResidencyService;
import org.openhds.domain.model.Residency;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("residencyXmlWriter")
public class ResidencyXmlWriterTask extends XmlWriterTemplate<Residency> {

	private ResidencyService residencyService;

	@Autowired
	public ResidencyXmlWriterTask(AsyncTaskService asyncTaskService,
			ResidencyService residencyService) {
		super(asyncTaskService, AsyncTaskService.RESIDENCY_TASK_NAME);
		this.residencyService = residencyService;
	}

	@Override
	protected Residency makeCopyOf(Residency original) {
		return ShallowCopier.makeShallowCopy(original);
	}

	@Override
	protected List<Residency> getEntitiesInRange(TaskContext taskContext,
			Residency start, int pageSize) {
		return residencyService.getAllResidenciesInRange(start, pageSize);
	}

	@Override
	protected Class<?> getBoundClass() {
		return Residency.class;
	}

	@Override
	protected String getStartElementName() {
		return "residencies";
	}
}
