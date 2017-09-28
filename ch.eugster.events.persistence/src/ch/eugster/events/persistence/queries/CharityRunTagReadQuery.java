package ch.eugster.events.persistence.queries;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReportQuery;
import org.eclipse.persistence.queries.ReportQueryResult;

import ch.eugster.events.persistence.model.CharityRun;
import ch.eugster.events.persistence.model.CharityRunTagRead;
import ch.eugster.events.persistence.service.ConnectionService;

public class CharityRunTagReadQuery extends AbstractEntityQuery<CharityRunTagRead>
{
	public CharityRunTagRead selectLastRead(String tagId)
	{
		ExpressionBuilder charityRunTagReadExpression = new ExpressionBuilder();
		ReportQuery reportQuery = new ReportQuery(CharityRunTagRead.class, charityRunTagReadExpression);
		reportQuery.addMaximum("lastSeen", charityRunTagReadExpression.get("lastSeen"));
		reportQuery.addMaximum("id", charityRunTagReadExpression.get("id"));

		reportQuery.setSelectionCriteria(charityRunTagReadExpression.get("tagId").equal(tagId));
		reportQuery.addOrdering(charityRunTagReadExpression.get("lastSeen").descending());
		reportQuery.addGrouping(charityRunTagReadExpression.get("tagId"));
		Query query = JpaHelper.createQuery(reportQuery, getEntityManager());
		query.setMaxResults(1);
		try
		{
			ReportQueryResult result = (ReportQueryResult) query.getSingleResult();
			return getEntityManager().find(CharityRunTagRead.class, result.get("id"));
		}
		catch (NoResultException e)
		{
			return null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public CharityRunTagReadQuery(ConnectionService connectionService)
	{
		super(connectionService);
	}
}
