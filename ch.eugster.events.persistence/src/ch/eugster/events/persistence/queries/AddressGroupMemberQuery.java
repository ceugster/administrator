package ch.eugster.events.persistence.queries;

import java.util.Collection;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressGroupMemberQuery extends AbstractEntityQuery<AddressGroupMember>
{

	public AddressGroupMemberQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public Collection<AddressGroupMember> selectByLink(LinkPersonAddress link)
	{
		Expression expression = new ExpressionBuilder(AddressGroupMember.class).get("link").equal(link);
		return select(AddressGroupMember.class, expression);
	}

	public Collection<AddressGroupMember> selectByAddress(Address address)
	{
		Expression expression = new ExpressionBuilder(AddressGroupMember.class).get("address").equal(address);
		return select(AddressGroupMember.class, expression);
	}
}
