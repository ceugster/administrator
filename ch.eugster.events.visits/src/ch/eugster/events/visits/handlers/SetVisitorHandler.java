package ch.eugster.events.visits.handlers;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.Visitor;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.views.PersonView;
import ch.eugster.events.visits.Activator;

public class SetVisitorHandler extends AbstractHandler implements IHandler, IElementUpdater
{

	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public SetVisitorHandler()
	{
		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(ServiceReference reference)
			{
				connectionService = (ConnectionService) super.addingService(reference);
				setBaseEnabled(connectionService != null);
				return connectionService;
			}

			@Override
			public void removedService(ServiceReference reference, Object service)
			{
				connectionService = null;
				setBaseEnabled(false);
				super.removedService(reference, service);
			}
		};
		connectionServiceTracker.open();
		this.addListenerObject(this);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				LinkPersonAddress link = null;
				if (ssel.getFirstElement() instanceof Person)
				{
					Person person = (Person) ssel.getFirstElement();
					link = person.getDefaultLink();
					if (link == null && person.getLinks().size() > 0)
					{
						link = person.getLinks().iterator().next();
					}
				}
				else if (ssel.getFirstElement() instanceof LinkPersonAddress)
				{
					link = (LinkPersonAddress) ssel.getFirstElement();
				}
				if (link != null)
				{
					if (connectionService != null)
					{
						if (link.getVisitor() == null)
						{
							link.setVisitor(Visitor.newInstance(link));
						}
						else
						{
							link.getVisitor().setDeleted(!link.getVisitor().isDeleted());
						}
						LinkPersonAddressQuery query = (LinkPersonAddressQuery) connectionService
								.getQuery(LinkPersonAddress.class);
						link = query.merge(link);
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(UIElement element, Map parameters)
	{
		boolean checked = false;
		IWorkbenchPartSite site = (IWorkbenchPartSite) parameters.get("org.eclipse.ui.part.IWorkbenchPartSite");
		if (site instanceof IViewSite && site.getPart() instanceof PersonView)
		{
			PersonView view = (PersonView) site.getPart();
			if (!view.getViewer().getSelection().isEmpty())
			{
				StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
				LinkPersonAddress link = null;
				if (ssel.getFirstElement() instanceof Person)
				{
					Person person = (Person) ssel.getFirstElement();
					link = person.getDefaultLink();
					if (link == null && person.getLinks().size() > 0)
					{
						link = person.getLinks().iterator().next();
					}
				}
				else if (ssel.getFirstElement() instanceof LinkPersonAddress)
				{
					link = (LinkPersonAddress) ssel.getFirstElement();
				}
				if (link != null)
				{
					if (link.getVisitor() == null)
					{
						checked = false;
					}
					else
					{
						checked = !link.getVisitor().isDeleted();
					}
				}
			}
		}
		element.setChecked(checked);
	}

}
