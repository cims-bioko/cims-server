package org.openhds.task;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openhds.controller.service.MembershipService;
import org.openhds.domain.model.Membership;
import org.openhds.domain.util.ShallowCopier;
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
	protected Membership makeCopyOf(Membership original) {
		return ShallowCopier.makeShallowCopy(original);
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
