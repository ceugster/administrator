package ch.eugster.events.person.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.widgets.Text;
import org.osgi.util.tracker.ServiceTracker;

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
		List<CityContentProposal> proposals = new ArrayList<CityContentProposal>();
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			ZipCodeQuery query = (ZipCodeQuery) service.getQuery(ZipCode.class);
			List<ZipCode> zipCodes = query.selectByZipCode(zip.getText());
			Iterator<ZipCode> iterator = zipCodes.iterator();
			for (int i = 0; iterator.hasNext(); i++)
			{
				ZipCode zipCode = iterator.next();
				proposals.add(new CityContentProposal(zipCode));
			}
		}
		tracker.close();
		if (proposals.size() == 0)
		{
			zip.setData("zipCode", null);
			countryViewer.setData("country", null);
		}
		return proposals.toArray(new CityContentProposal[0]);
	}

}
