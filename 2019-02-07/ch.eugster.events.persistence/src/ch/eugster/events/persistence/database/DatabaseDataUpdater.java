package ch.eugster.events.persistence.database;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventAdapter;
import org.eclipse.persistence.sessions.UnitOfWork;

public abstract class DatabaseDataUpdater implements SessionCustomizer
{
	@Override
	public void customize(final Session session) throws Exception
	{
		session.getEventManager().addListener(new SessionEventAdapter()
		{
			@Override
			public void postLogin(final SessionEvent event)
			{
				UnitOfWork unitOfWork = event.getSession().acquireUnitOfWork();
				updateDatabase(unitOfWork);
				unitOfWork.commit();
			}
		});
	}

	protected void updateDatabase(final UnitOfWork unitOfWork)
	{
	}
}
