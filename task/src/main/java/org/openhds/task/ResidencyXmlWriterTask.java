package org.openhds.task;

import org.hibernate.SessionFactory;
import org.openhds.domain.model.Residency;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("residencyXmlWriter")
public class ResidencyXmlWriterTask extends XmlWriterTemplate<Residency> {

	@Autowired
	public ResidencyXmlWriterTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
		super(asyncTaskService, factory, AsyncTaskService.RESIDENCY_TASK_NAME);
	}

	@Override
	protected String getExportQuery() {
		return "from Residency" +
				" where deleted = false";
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
