package ch.eugster.events.persistence.queries;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.queries.ReportQueryResult;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationQuery extends AbstractEntityQuery<Donation>
{
	public DonationQuery(final ConnectionService connectionService)
	{
		super(connectionService);
	}

	public List<Donation> selectByLink(final LinkPersonAddress link)
	{
		final Expression expression = new ExpressionBuilder(Donation.class).get("link").equal(link);
		return this.select(Donation.class, expression);
	}

	public List<Donation> selectByAddress(final Address address)
	{
		final Expression expression = new ExpressionBuilder(Donation.class).get("address").equal(address);
		return this.select(Donation.class, expression);
	}

	public List<Donation> selectByYear(final DonationYear donationYear)
	{
		Expression donationExpression = new ExpressionBuilder(Donation.class).get("deleted").equal(false);
		if (donationYear.getYear() != 0)
		{
			donationExpression = donationExpression.and(new ExpressionBuilder().get("year").equal(donationYear.getYear()));
		}
		return this.select(Donation.class, donationExpression);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> selectPurposes()
	{
		final ReportQuery query = new ReportQuery();
		query.setJPQLString("SELECT DISTINCT d.purpose FROM Donation AS d");
		return (List<String>) this.connectionService.getSession().executeQuery(query);
	}

	public List<Donation> selectByYearPurposeDomain(final DonationYear year, final DonationPurpose purpose, final Domain domain)
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
		final List<Donation> donations = super.select(Donation.class, donationExpression);
		return donations;
	}
	
	public List<Donation> selectByYearRangePurposeDomainName(final DonationYear fromYear, final DonationYear toYear, final DonationPurpose purpose, final Domain domain, final String name)
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
		if (!name.isEmpty())
		{
			final Expression linkNameExpression = new ExpressionBuilder().get("link").notNull().and((new ExpressionBuilder().get("link").get("person").get("lastname").containsSubstringIgnoringCase(name)).or(new ExpressionBuilder().get("link").get("person").get("firstname").containsSubstringIgnoringCase(name)));
			final Expression addressNameExpression = new ExpressionBuilder().get("address").notNull().and(new ExpressionBuilder().get("address").get("name").containsSubstringIgnoringCase(name));
			donationExpression = donationExpression.and((linkNameExpression.or(addressNameExpression)));
		}
		final List<Donation> donations = super.select(Donation.class, donationExpression);
		return donations;
	}
	
	@SuppressWarnings("unchecked")
	public List<DonationYear> selectYears()
	{
		final Expression expression = new ExpressionBuilder();
		final ReportQuery query = new ReportQuery(Donation.class, expression);
		query.addAttribute("year", expression.get("year"), Integer.class);
		query.setDistinctState(ObjectLevelReadQuery.USE_DISTINCT);
		final List<ReportQueryResult> results = (List<ReportQueryResult>) this.connectionService.getSession()
				.executeQuery(query);
		final List<DonationYear> years = new ArrayList<DonationYear>();
		for (final ReportQueryResult result : results)
		{
			final Integer year = (Integer) result.get("year");
			if (year != null && year > 0)
			{
				years.add(new DonationYear(year));
			}
		}
		return years;
	}

	public List<Donation> selectValids()
	{
		final Expression expression = new ExpressionBuilder(Donation.class).get("deleted").equal(false);
//		Expression donator = new ExpressionBuilder().get("link").isNull();
//		Expression validDonator = new ExpressionBuilder().get("link").get("deleted").equal(false);
//		validDonator = validDonator.and(new ExpressionBuilder().get("link").get("person").get("deleted").equal(false));
//		validDonator = validDonator.and(new ExpressionBuilder().get("link").get("address").get("deleted").equal(false));
//		donator = donator.or(validDonator);
//		expression = expression.and(donator).and(new ExpressionBuilder().get("address").get("deleted").equal(false));
		return this.select(Donation.class, expression);
	}

	@SuppressWarnings("unchecked")
	public List<Donation> selectByYear(final Integer year)
	{
		final ReadAllQuery query = new ReadAllQuery(Donation.class);
		final ExpressionBuilder builder = query.getExpressionBuilder();
		final Expression expression = builder.get("year").equal(year);
		query.setSelectionCriteria(expression);
		final List<Donation> donations = (List<Donation>) this.connectionService.getSession().executeQuery(query);
		return donations;
	}

	public int countByYear(final Integer year)
	{
		final ExpressionBuilder builder = new ExpressionBuilder();
		final ReportQuery query = new ReportQuery(Donation.class, builder);
		query.setSelectionCriteria(builder.get("year").equal(year.intValue()));
		query.addCount();
		query.returnSingleValue();
		final Long result = (Long) this.connectionService.getSession().executeQuery(query);
		return result.intValue();
	}

}
