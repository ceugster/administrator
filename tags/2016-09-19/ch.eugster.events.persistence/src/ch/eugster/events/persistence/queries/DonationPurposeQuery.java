package ch.eugster.events.persistence.queries;

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationPurposeQuery extends AbstractEntityQuery<DonationPurpose>
{
	public DonationPurposeQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public boolean isCodeUnique(final String code, final Long id)
	{
		if (code == null || code.length() == 0)
		{
			return true;
		}
		Expression expression = new ExpressionBuilder(DonationPurpose.class).get("code").equal(code);
		List<DonationPurpose> purposes = select(DonationPurpose.class, expression);
		if (purposes.isEmpty())
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
				for (DonationPurpose purpose : purposes)
				{
					if (!purpose.getId().equals(id))
					{
						return false;
					}
				}
				return true;
			}
		}
	}

	public List<DonationPurpose> selectAll()
	{
		return super.selectAll(DonationPurpose.class);
	}

	public DonationPurpose selectByCode(final String code)
	{
		Expression expression = new ExpressionBuilder(DonationPurpose.class).get("code").equal(code);
		List<DonationPurpose> purposes = select(DonationPurpose.class, expression);
		try
		{
			return purposes.iterator().next();
		}
		catch (NoSuchElementException e)
		{
			return null;
		}
	}

}
