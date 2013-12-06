package ch.eugster.events.persistence.model;

import java.util.ArrayList;
import java.util.Collection;

import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.queries.DonationQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationYear implements IEntity
{
	private final int year;

	public DonationYear(int year)
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
		return year;
	}

	public Collection<Donation> getDonations()
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			DonationQuery query = (DonationQuery) service.getQuery(Donation.class);
			return query.selectByYear(Integer.valueOf(year));
		}
		return new ArrayList<Donation>();
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof DonationYear)
		{
			return ((DonationYear) other).getYear() == this.getYear();
		}
		return false;

	}
}
