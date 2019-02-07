package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.BankAccount;
import ch.eugster.events.persistence.service.ConnectionService;

public class BankAccountQuery extends AbstractEntityQuery<BankAccount>
{
	public BankAccountQuery(final ConnectionService service)
	{
		super(service);
	}
}
