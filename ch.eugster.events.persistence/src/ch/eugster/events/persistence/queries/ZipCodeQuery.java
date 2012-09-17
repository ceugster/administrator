package ch.eugster.events.persistence.queries;

import java.util.Collection;
import java.util.Vector;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.queries.ReportQueryResult;

import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.persistence.service.ConnectionService;

public class ZipCodeQuery extends AbstractEntityQuery<ZipCode>
{

	public ZipCodeQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public Collection<ZipCode> selectByCountryAndZipCode(Country country, String code)
	{
		Expression expression = new ExpressionBuilder().get("deleted").equal(false);
		expression = expression.and(new ExpressionBuilder().get("country").equal(country));
		expression = expression.and(new ExpressionBuilder().get("zip").equal(code));
		return this.select(ZipCode.class, expression);
	}

	@SuppressWarnings("unchecked")
	public Collection<String> selectStates(Country country)
	{
		Collection<String> states = new Vector<String>();
		ReportQuery query = new ReportQuery(ZipCode.class, new ExpressionBuilder().get("country").equal(country));
		query.setDistinctState(ReportQuery.USE_DISTINCT);
		query.addAscendingOrdering("state");
		query.addAttribute("state");
		Collection<ReportQueryResult> results = (Collection<ReportQueryResult>) connectionService.getSession()
				.executeQuery(query);
		for (ReportQueryResult result : results)
		{
			states.add((String) result.get("state"));
		}
		return states;
	}
}
