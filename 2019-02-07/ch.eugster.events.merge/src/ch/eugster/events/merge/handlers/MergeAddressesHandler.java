package ch.eugster.events.merge.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.merge.Activator;
import ch.eugster.events.merge.views.PersonView;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class MergeAddressesHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("activePart") instanceof PersonView)
			{
				PersonView personView = (PersonView) context.getParent().getVariable("activePart");
				merge(personView.getViewer());
			}
		}
		return null;
	}

	@Override
	public boolean isEnabled()
	{
		return super.isEnabled();
	}

	private void merge(final TableViewer viewer)
	{
		Address selectedAddress = null;
		StructuredSelection ssel = (StructuredSelection) viewer.getSelection();
		Object[] objects = ssel.toArray();
		for (Object object : objects)
		{
			TableItem item = (TableItem) viewer.testFindItem(object);
			if (item.getChecked())
			{
				if (item.getData() instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) item.getData();
					selectedAddress = link.getAddress();
				}
			}
		}
		if (selectedAddress != null)
		{
			for (Object object : objects)
			{
				if (object instanceof LinkPersonAddress)
				{
					Address oldAddress = null;
					LinkPersonAddress link = (LinkPersonAddress) object;
					if (!link.getAddress().getId().equals(selectedAddress.getId()))
					{
						oldAddress = link.getAddress();
						oldAddress.removeLink(link);
						if (oldAddress.getPersonLinks().size() == 0)
						{
							oldAddress.setDeleted(true);
						}
						link.setAddress(selectedAddress);
						selectedAddress.addLink(link);
						if (link.getAddress().isDeleted())
						{
							link.getAddress().setDeleted(false);
						}

						ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle()
								.getBundleContext(), ConnectionService.class, null);
						tracker.open();
						try
						{
							ConnectionService service = (ConnectionService) tracker.getService();
							if (service != null)
							{
								LinkPersonAddressQuery linkQuery = (LinkPersonAddressQuery) service
										.getQuery(LinkPersonAddress.class);
								link = linkQuery.merge(link);
								AddressQuery addressQuery = (AddressQuery) service.getQuery(Address.class);
								oldAddress = addressQuery.merge(oldAddress);
							}
						}
						finally
						{
							tracker.close();
						}
					}
				}
			}
		}
	}

	@Override
	public void setEnabled(final Object object)
	{
		if (object instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) object;
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				setBaseEnabled(ssel.size() > 1);
			}
		}
	}

}
