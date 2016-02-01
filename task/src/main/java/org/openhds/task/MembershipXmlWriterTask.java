package org.openhds.task;

import org.hibernate.SessionFactory;
import org.openhds.domain.model.Membership;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("membershipXmlWriter")
public class MembershipXmlWriterTask extends XmlWriterTemplate<Membership> {

	@Autowired
	public MembershipXmlWriterTask(AsyncTaskService asyncTaskService, SessionFactory factory) {
		super(asyncTaskService, factory, AsyncTaskService.MEMBERSHIP_TASK_NAME);
	}

	@Override
	protected String getExportQuery() {
		return "from Membership" +
				" where deleted = false";
	}

	@Override
	protected Class<?> getBoundClass() {
		return Membership.class;
	}

	@Override
	protected String getStartElementName() {
		return "memberships";
	}

}
