package ch.eugster.events.persistence.queries;

import java.util.List;

import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.persistence.service.ConnectionService;

public class MembershipQuery extends AbstractEntityQuery<Membership>
{

	public MembershipQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<Membership> selectAll()
	{
		return super.selectAll(Membership.class);
	}

	public List<Membership> selectAll(boolean deletedToo)
	{
		return super.selectAll(Membership.class, deletedToo);
	}
}
