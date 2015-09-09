package org.openhds.task;

import java.util.List;

import org.h2.engine.Session;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openhds.controller.service.ResidencyService;
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
	protected Residency makeCopyOf(Residency original) {
		return ShallowCopier.makeShallowCopy(original);
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
