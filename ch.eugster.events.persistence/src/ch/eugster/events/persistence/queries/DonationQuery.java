package ch.eugster.events.persistence.queries;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.queries.ReportQueryResult;

import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationQuery extends AbstractEntityQuery<Donation>
{

	public DonationQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public Collection<Donation> selectByLink(LinkPersonAddress link)
	{
		Expression expression = new ExpressionBuilder(Donation.class).get("link").equal(link);
		return select(Donation.class, expression);
	}

	@SuppressWarnings("unchecked")
	public Collection<String> selectPurposes()
	{
		ReportQuery query = new ReportQuery();
		query.setJPQLString("SELECT DISTINCT d.purpose FROM Donation AS d");
		return (Collection<String>) connectionService.getSession().executeQuery(query);
	}

	@SuppressWarnings("unchecked")
	public Collection<DonationYear> selectYears()
	{
		Expression expression = new ExpressionBuilder();
		ReportQuery query = new ReportQuery(Donation.class, expression);
		query.addAttribute("year", expression.get("year"), Integer.class);
		query.setDistinctState(ObjectLevelReadQuery.USE_DISTINCT);
		Collection<ReportQueryResult> results = (Collection<ReportQueryResult>) connectionService.getSession()
				.executeQuery(query);
		Collection<DonationYear> years = new ArrayList<DonationYear>();
		for (ReportQueryResult result : results)
		{
			Integer year = (Integer) result.get("year");
			if (year != null && year > 0)
			{
				years.add(new DonationYear(year));
			}
		}
		return years;
	}

	@SuppressWarnings("unchecked")
	public Collection<Donation> selectByYear(Integer year)
	{
		ReadAllQuery query = new ReadAllQuery(Donation.class);
		ExpressionBuilder builder = query.getExpressionBuilder();
		Expression expression = builder.get("year").equal(year);
		query.setSelectionCriteria(expression);
		return (Collection<Donation>) connectionService.getSession().executeQuery(query);
	}

	public int countByYear(Integer year)
	{
		ExpressionBuilder builder = new ExpressionBuilder();
		ReportQuery query = new ReportQuery(Donation.class, builder);
		query.setSelectionCriteria(builder.get("year").equal(year.intValue()));
		query.addCount();
		query.returnSingleValue();
		Long result = (Long) connectionService.getSession().executeQuery(query);
		return result.intValue();
	}

}
