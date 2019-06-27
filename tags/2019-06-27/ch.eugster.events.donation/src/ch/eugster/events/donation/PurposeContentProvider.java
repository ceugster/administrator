package ch.eugster.events.donation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;

import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.queries.DonationPurposeQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class PurposeContentProvider extends ArrayContentProvider
{
	private DonationPurpose[] entries;
	
	private DonationPurpose allPurposes;

	public PurposeContentProvider(DonationPurpose allPurposes)
	{
		this.allPurposes = allPurposes;
	}

	@Override
	public Object[] getElements(Object inputElement)
	{
		if (inputElement instanceof ConnectionService)
		{
			List<DonationPurpose> purposes = new ArrayList<DonationPurpose>();
			purposes.add(allPurposes);
			ConnectionService connectionService = (ConnectionService) inputElement;
			DonationPurposeQuery query = (DonationPurposeQuery) connectionService.getQuery(DonationPurpose.class);
			purposes.addAll(query.selectAll());
			this.entries = purposes.toArray(new DonationPurpose[0]);
			return this.entries;
		}
		else if (inputElement instanceof DonationPurpose[])
		{
			this.entries =  (DonationPurpose[]) inputElement;
			return this.entries;
		}
		return new DonationPurpose[0];
	}
	
	public DonationPurpose[] getEntries()
	{
		return this.entries;
	}
}
