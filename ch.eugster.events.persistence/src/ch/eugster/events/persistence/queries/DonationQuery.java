package ch.eugster.events.persistence.queries;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.queries.ReportQueryResult;

import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationQuery extends AbstractEntityQuery<Donation>
{

	public DonationQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<Donation> selectByLink(LinkPersonAddress link)
	{
		Expression expression = new ExpressionBuilder(Donation.class).get("link").equal(link);
		return select(Donation.class, expression);
	}

	@SuppressWarnings("unchecked")
	public List<String> selectPurposes()
	{
		ReportQuery query = new ReportQuery();
		query.setJPQLString("SELECT DISTINCT d.purpose FROM Donation AS d");
		return (List<String>) connectionService.getSession().executeQuery(query);
	}

	public List<Donation> selectByYearPurposeDomain(DonationYear year, DonationPurpose purpose, Domain domain)
	{
		Expression donationExpression = new ExpressionBuilder(Donation.class).get("deleted").equal(false);
		if (year != null)
		{
			donationExpression = donationExpression.and(new ExpressionBuilder().get("year").equal(year.getYear()));
		}
		if (purpose != null)
		{
			donationExpression = donationExpression.and(new ExpressionBuilder().get("purpose").equal(purpose));
		}
		if (domain != null)
		{
			if (domain.getName().equals("Ohne Domäne"))
			{
				donationExpression = donationExpression.and(new ExpressionBuilder().get("domain").isNull());
			}
			else
			{
				donationExpression = donationExpression.and(new ExpressionBuilder().get("domain").equal(domain));
			}
		}
		List<Donation> donations = super.select(Donation.class, donationExpression);
		return donations;
	}
	
	public List<Donation> selectByYearRangePurposeDomain(DonationYear fromYear, DonationYear toYear, DonationPurpose purpose, Domain domain)
	{
		Expression donationExpression = new ExpressionBuilder(Donation.class).get("deleted").equal(false);
		donationExpression = donationExpression.and(new ExpressionBuilder().get("year").between(fromYear.getYear(), toYear.getYear()));
		if (purpose != null)
		{
			donationExpression = donationExpression.and(new ExpressionBuilder().get("purpose").equal(purpose));
		}
		if (domain != null)
		{
			if (domain.getName().equals("Ohne Domäne"))
			{
				donationExpression = donationExpression.and(new ExpressionBuilder().get("domain").isNull());
			}
			else
			{
				donationExpression = donationExpression.and(new ExpressionBuilder().get("domain").equal(domain));
			}
		}
		List<Donation> donations = super.select(Donation.class, donationExpression);
		return donations;
	}
	
	@SuppressWarnings("unchecked")
	public List<DonationYear> selectYears()
	{
		Expression expression = new ExpressionBuilder();
		ReportQuery query = new ReportQuery(Donation.class, expression);
		query.addAttribute("year", expression.get("year"), Integer.class);
		query.setDistinctState(ObjectLevelReadQuery.USE_DISTINCT);
		List<ReportQueryResult> results = (List<ReportQueryResult>) connectionService.getSession()
				.executeQuery(query);
		List<DonationYear> years = new ArrayList<DonationYear>();
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

	public List<Donation> selectValids()
	{
		Expression expression = new ExpressionBuilder(Donation.class).get("deleted").equal(false);
//		Expression donator = new ExpressionBuilder().get("link").isNull();
//		Expression validDonator = new ExpressionBuilder().get("link").get("deleted").equal(false);
//		validDonator = validDonator.and(new ExpressionBuilder().get("link").get("person").get("deleted").equal(false));
//		validDonator = validDonator.and(new ExpressionBuilder().get("link").get("address").get("deleted").equal(false));
//		donator = donator.or(validDonator);
//		expression = expression.and(donator).and(new ExpressionBuilder().get("address").get("deleted").equal(false));
		return select(Donation.class, expression);
	}

	@SuppressWarnings("unchecked")
	public List<Donation> selectByYear(Integer year)
	{
		ReadAllQuery query = new ReadAllQuery(Donation.class);
		ExpressionBuilder builder = query.getExpressionBuilder();
		Expression expression = builder.get("year").equal(year);
		query.setSelectionCriteria(expression);
		List<Donation> donations = (List<Donation>) connectionService.getSession().executeQuery(query);
		return donations;
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
