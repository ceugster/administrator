package ch.eugster.events.persistence.queries;

import java.util.Collection;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.service.ConnectionService;

public class MemberQuery extends AbstractEntityQuery<Member>
{

	public MemberQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public Collection<Member> selectByLink(LinkPersonAddress linkPersonAddress)
	{
		Expression expression = new ExpressionBuilder(Member.class).get("link").equal(linkPersonAddress);
		return select(Member.class, expression);
	}

	public Collection<Member> selectByAddress(Address address)
	{
		Expression expression = new ExpressionBuilder(Member.class).get("address").equal(address);
		return select(Member.class, expression);
	}
}
