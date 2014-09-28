package ch.eugster.events.persistence.queries;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.queries.ReadAllQuery;
import org.eclipse.persistence.queries.ReportQuery;
import org.osgi.service.log.LogService;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.service.ConnectionService;

public abstract class AbstractEntityQuery<T extends AbstractEntity>
{
	protected final ConnectionService connectionService;

	public AbstractEntityQuery(final ConnectionService connectionService)
	{
		this.connectionService = connectionService;
	}

	protected long count(final Class<T> clazz, final Expression expression)
	{
		Query query = null;
		final ReportQuery reportQuery = new ReportQuery(clazz, expression);
		reportQuery.addCount("id", Long.class);
		reportQuery.setShouldReturnSingleValue(true);
		if (getEntityManager() != null)
		{
			query = JpaHelper.createQuery(reportQuery, getEntityManager());
			return ((Long) query.getSingleResult()).longValue();
		}
		else
		{
			return Long.valueOf(0L);
		}
	}

	public T delete(final T entity)
	{
		entity.setDeleted(true);
		return merge(entity);
	}

	public T find(final Class<T> clazz, final Long id)
	{
		if (getEntityManager() != null)
		{
			return getEntityManager().find(clazz, id);
		}
		return null;
	}

	private EntityManager getEntityManager()
	{
		if (connectionService == null)
		{
			return null;
		}
		else
		{
			return connectionService.getEntityManager();
		}
	}

	// @SuppressWarnings("unchecked")
	// protected T find(Class<T> clazz, Expression expression)
	// {
	// ReadObjectQuery databaseQuery = new ReadObjectQuery(clazz, expression);
	// Query query = ((JpaEntityManager)
	// getEntityManager().getDelegate()).createQuery(databaseQuery);
	// try
	// {
	// return (T) query.getSingleResult();
	// }
	// catch (NoResultException e)
	// {
	// return null;
	// }
	// }

	public T merge(T entity)
	{
		if (!(entity instanceof User))
		{
			/**
			 * Achtung: Dieses if ist notwendig, weil sonst ein unendlicher Loop
			 * folgt.
			 */
			entity.setUser(User.getCurrent());
		}
		if (entity.getId() == null)
		{
			entity.setInserted(GregorianCalendar.getInstance());
		}
		else
		{
			entity.setUpdated(GregorianCalendar.getInstance());
		}
		EntityManager em = getEntityManager();
		if (em != null)
		{
			try
			{
				em.getTransaction().begin();
				entity = em.merge(entity);
				em.flush();
				em.getTransaction().commit();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				this.connectionService.log(LogService.LOG_ERROR, e.getLocalizedMessage());
				MessageDialog dialog = new MessageDialog(null, "Fehler beim Speichern", null,
						"Beim Versuch, die Änderungen zu speichern, ist ein Fehler aufgetreten.", MessageDialog.ERROR,
						new String[] { "OK" }, 0);
				dialog.open();
			}
			finally
			{
				if (em.getTransaction().isActive())
				{
					em.getTransaction().rollback();
				}
			}
		}
		return entity;
	}

	public AbstractEntity refresh(final AbstractEntity entity)
	{
		EntityManager em = getEntityManager();
		if (em != null)
		{
			em.refresh(entity);
		}
		return entity;
	}

	protected List<T> select(final Class<T> clazz, final Expression expression)
	{
		return select(clazz, expression, 0);
	}

	@SuppressWarnings("unchecked")
	protected List<T> select(final Class<T> clazz, final Expression expression, final int maxResults)
	{
		ReadAllQuery databaseQuery = new ReadAllQuery(clazz, expression);
		final Query query = JpaHelper.createQuery(databaseQuery, getEntityManager());
		if (maxResults > 0)
		{
			query.setMaxResults(maxResults);
		}
		List<T> result = query.getResultList();
		return result;
	}

	public List<T> selectAll(final Class<T> clazz)
	{
		return selectAll(clazz, true);
	}

	@SuppressWarnings("unchecked")
	protected List<T> selectAll(final Class<T> clazz, final boolean deletedToo)
	{
		List<T> result = new ArrayList<T>();
		Expression expression = new ExpressionBuilder(clazz);
		if (!deletedToo)
		{
			expression = expression.get("deleted").equal(false);
		}
		ReadAllQuery databaseQuery = new ReadAllQuery(clazz, expression);
		if (getEntityManager() != null)
		{
			Query query = ((JpaEntityManager) getEntityManager().getDelegate()).createQuery(databaseQuery);
			result = query.getResultList();
		}
		return result;
	}

}
