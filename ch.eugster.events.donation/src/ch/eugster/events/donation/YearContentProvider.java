package ch.eugster.events.donation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;

import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.persistence.queries.DonationQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class YearContentProvider extends ArrayContentProvider
{
	private DonationYear allYears;
	
	private DonationYear[] entries;
	
	public YearContentProvider()
	{
		this.allYears = null;
	}
	
	public YearContentProvider(DonationYear year)
	{
		this.allYears = year;
	}
	
	@Override
	public Object[] getElements(Object inputElement)
	{
		if (inputElement instanceof ConnectionService)
		{
			ConnectionService connectionService = (ConnectionService) inputElement;
			DonationQuery query = (DonationQuery) connectionService.getQuery(Donation.class);
			List<DonationYear> years = new ArrayList<DonationYear>();
			if (this.allYears != null)
			{
				years.add(this.allYears);
			}
			years.addAll(query.selectYears());
			this.entries = years.toArray(new DonationYear[0]);
			return this.entries;
		}
		else if (inputElement instanceof DonationYear[])
		{
			if (this.allYears == null)
			{
				this.entries = (DonationYear[]) inputElement;
			}
			else
			{
				DonationYear[] years = (DonationYear[]) inputElement;
				this.entries = new DonationYear[years.length + 1];
				this.entries[0] = this.allYears;
				for (int i = 0; i < years.length; i++)
				{
					this.entries[i + 1] = years[i];
				}
			}
			return this.entries;
		}
		return new DonationYear[0];
	}
	
	public DonationYear[] getEntries()
	{
		return this.entries;
	}
}
