package ch.eugster.events.persistence.model;

import java.util.ArrayList;
import java.util.List;

import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.queries.DonationQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationYear implements IEntity
{
	private final int year;

	public DonationYear(final int year)
	{
		this.year = year;
	}

	@Override
	public Long getId()
	{
		return null;
	}
	
	@Override
	public boolean isDeleted()
	{
		return false;
	}

	public int getYear()
	{
		return this.year;
	}

	public List<Donation> getDonations()
	{
		final ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			final ConnectionService service = tracker.getService();
			if (service != null)
			{
				final DonationQuery query = (DonationQuery) service.getQuery(Donation.class);
				return query.selectByYear(Integer.valueOf(this.year));
			}
		}
		finally
		{
			tracker.close();
		}
		return new ArrayList<Donation>();
	}

	@Override
	public boolean equals(final Object other)
	{
		if (other instanceof DonationYear)
		{
			return ((DonationYear) other).getYear() == this.getYear();
		}
		return false;

	}
}
