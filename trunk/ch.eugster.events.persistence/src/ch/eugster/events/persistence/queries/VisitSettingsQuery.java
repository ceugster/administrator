package ch.eugster.events.persistence.queries;

import java.util.Collection;

import org.eclipse.persistence.expressions.ExpressionBuilder;

import ch.eugster.events.persistence.model.VisitSettings;
import ch.eugster.events.persistence.service.ConnectionService;

public class VisitSettingsQuery extends AbstractEntityQuery<VisitSettings>
{

	public VisitSettingsQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public VisitSettings select()
	{
		Collection<VisitSettings> settings = select(VisitSettings.class, new ExpressionBuilder());
		return settings.isEmpty() ? null : settings.iterator().next();
	}
}
