package ch.eugster.events.person.editors;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Text;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.persistence.queries.ZipCodeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;

public class CityContentProposalProvider implements IContentProposalProvider
{
	private final ComboViewer countryViewer;

	private final Text zip;

	public CityContentProposalProvider(ComboViewer countryViewer, Text zip)
	{
		this.countryViewer = countryViewer;
		this.zip = zip;
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position)
	{
		CityContentProposal[] proposals = new CityContentProposal[0];

		if (!countryViewer.getSelection().isEmpty())
		{
			StructuredSelection ssel = (StructuredSelection) countryViewer.getSelection();
			if (ssel.getFirstElement() instanceof Country)
			{
				Country country = (Country) ssel.getFirstElement();
				ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
						ConnectionService.class.getName(), null);
				tracker.open();
				ConnectionService service = (ConnectionService) tracker.getService();
				if (service != null)
				{
					ZipCodeQuery query = (ZipCodeQuery) service.getQuery(ZipCode.class);
					Collection<ZipCode> zipCodes = query.selectByCountryAndZipCode(country, zip.getText());
					proposals = new CityContentProposal[zipCodes.size()];
					Iterator<ZipCode> iterator = zipCodes.iterator();
					for (int i = 0; iterator.hasNext(); i++)
					{
						ZipCode zipCode = iterator.next();
						proposals[i] = new CityContentProposal(zipCode);
					}
				}
				tracker.close();
			}
		}
		return proposals == null ? new CityContentProposal[0] : proposals;
	}

}
