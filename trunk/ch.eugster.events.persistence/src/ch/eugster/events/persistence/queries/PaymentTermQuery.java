package ch.eugster.events.persistence.queries;

import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.PaymentTerm;
import ch.eugster.events.persistence.service.ConnectionService;

public class PaymentTermQuery extends AbstractEntityQuery<PaymentTerm>
{

	public PaymentTermQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<PaymentTerm> selectAll()
	{
		return super.selectAll(PaymentTerm.class);
	}

	public boolean isTextUnique(final String text, final Long id)
	{
		if (text == null || text.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(PaymentTerm.class).get("text").equal(text);
		expression = expression.and(new ExpressionBuilder().get("deleted").equal(false));
		List<PaymentTerm> paymentTerms = select(PaymentTerm.class, expression);
		if (paymentTerms.isEmpty())
		{
			return true;
		}
		else
		{
			if (id == null)
			{
				return false;
			}
			else
			{
				for (PaymentTerm paymentTerm : paymentTerms)
				{
					if (!paymentTerm.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

}
