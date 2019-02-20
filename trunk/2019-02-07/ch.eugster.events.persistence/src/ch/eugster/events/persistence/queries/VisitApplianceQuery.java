package ch.eugster.events.persistence.queries;

import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.VisitAppliance;
import ch.eugster.events.persistence.service.ConnectionService;

public class VisitApplianceQuery extends AbstractEntityQuery<VisitAppliance>
{

	public VisitApplianceQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public long count()
	{
		return this
				.count(VisitAppliance.class, new ExpressionBuilder(VisitAppliance.class).get("deleted").equal(false));
	}
}
