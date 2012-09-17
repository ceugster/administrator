package ch.eugster.events.persistence.queries;

import java.util.Collection;

import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.persistence.service.ConnectionService;

public class MembershipQuery extends AbstractEntityQuery<Membership>
{

	public MembershipQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public Collection<Membership> selectAll()
	{
		return super.selectAll(Membership.class);
	}
}
