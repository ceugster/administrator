package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.persistence.service.ConnectionService;

public class MemberQuery extends AbstractEntityQuery<Member>
{

	public MemberQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<Member> selectByLink(LinkPersonAddress linkPersonAddress)
	{
		Expression expression = new ExpressionBuilder(Member.class).get("link").equal(linkPersonAddress);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		return select(Member.class, expression);
	}

	public List<Member> selectByAddress(Address address)
	{
		Expression expression = new ExpressionBuilder(Member.class).get("address").equal(address);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		return select(Member.class, expression);
	}

	public List<Member> selectByMembershipAndCode(Membership membership, String code)
	{
		Expression expression = new ExpressionBuilder(Member.class).get("membership").equal(membership);
		expression = expression.and(new ExpressionBuilder().get("code").equal(code));
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		return select(Member.class, expression);
	}

}
