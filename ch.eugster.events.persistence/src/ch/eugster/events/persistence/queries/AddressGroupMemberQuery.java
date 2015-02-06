package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.service.ConnectionService;

public class AddressGroupMemberQuery extends AbstractEntityQuery<AddressGroupMember>
{

	public AddressGroupMemberQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<AddressGroupMember> selectByLink(LinkPersonAddress link)
	{
		Expression expression = new ExpressionBuilder(AddressGroupMember.class).get("link").equal(link);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		return select(AddressGroupMember.class, expression);
	}

	public List<AddressGroupMember> selectByAddress(Address address)
	{
		Expression expression = new ExpressionBuilder(AddressGroupMember.class).get("address").equal(address);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		return select(AddressGroupMember.class, expression);
	}

	public List<AddressGroupMember> selectByAddressAndAddressGroup(Address address, AddressGroup addressGroup)
	{
		Expression expression = new ExpressionBuilder(AddressGroupMember.class).get("address").equal(address);
		expression = expression.and(new ExpressionBuilder().get("addressGroup").equal(addressGroup));
		return select(AddressGroupMember.class, expression);
	}

	public List<AddressGroupMember> selectByLinkPersonAddressAndAddressGroup(LinkPersonAddress link,
			AddressGroup addressGroup)
	{
		Expression expression = new ExpressionBuilder(AddressGroupMember.class).get("link").equal(link);
		expression = expression.and(new ExpressionBuilder().get("addressGroup").equal(addressGroup));
		return select(AddressGroupMember.class, expression);
	}
}
