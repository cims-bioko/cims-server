package org.openhds.task;

import java.util.List;

import org.openhds.controller.service.MembershipService;
import org.openhds.domain.model.Membership;
import org.openhds.domain.util.ShallowCopier;
import org.openhds.task.service.AsyncTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("membershipXmlWriter")
public class MembershipXmlWriterTask extends XmlWriterTemplate<Membership> {

	private MembershipService membershipService;

	@Autowired
	public MembershipXmlWriterTask(AsyncTaskService asyncTaskService,
			MembershipService membershipService) {
		super(asyncTaskService, AsyncTaskService.MEMBERSHIP_TASK_NAME);
		this.membershipService = membershipService;
	}

	@Override
	protected Membership makeCopyOf(Membership original) {
		return ShallowCopier.makeShallowCopy(original);
	}

	@Override
	protected List<Membership> getEntitiesInRange(TaskContext taskContext,
			int i, int pageSize) {
		return membershipService.getAllMembershipsInRange(i, pageSize);
	}

	@Override
	protected Class<?> getBoundClass() {
		return Membership.class;
	}

	@Override
	protected String getStartElementName() {
		return "memberships";
	}

	@Override
	protected int getTotalEntityCount(TaskContext taskContext) {
		return (int) membershipService.getTotalMembershipCount();
	}

}
