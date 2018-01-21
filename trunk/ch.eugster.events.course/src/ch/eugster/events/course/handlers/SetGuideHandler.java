package ch.eugster.events.course.handlers;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IStateListener;
import org.eclipse.core.commands.State;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import ch.eugster.events.persistence.model.Guide;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.person.views.PersonView;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class SetGuideHandler extends ConnectionServiceDependentAbstractHandler implements IStateListener, IElementUpdater
{
	public SetGuideHandler()
	{
		this.addListenerObject(this);
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
			LinkPersonAddress link = null;
			if (ssel.getFirstElement() instanceof Person)
			{
				Person person = (Person) ssel.getFirstElement();
				link = person.getDefaultLink();
			}
			else if (ssel.getFirstElement() instanceof LinkPersonAddress)
			{
				link = (LinkPersonAddress) ssel.getFirstElement();
			}
			if (link != null)
			{
				boolean update = false;
				Guide guide = link.getGuide();
				if (guide == null)
				{
					guide = Guide.newInstance(link);
					link.setGuide(guide);
					update = true;
				}
				else
				{
					guide.setDeleted(!guide.isDeleted());
					update = true;
				}

				if (update)
				{
					if (connectionService != null)
					{
						LinkPersonAddressQuery query = (LinkPersonAddressQuery) connectionService
								.getQuery(LinkPersonAddress.class);
						link = query.merge(link);
					}
				}

			}
		}
		return null;
	}

	@Override
	public void handleStateChange(final State state, final Object oldValue)
	{
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(final UIElement element, final Map parameters)
	{
		IViewSite site = (IViewSite) parameters.get("org.eclipse.ui.part.IWorkbenchPartSite");
		if (site != null && site.getPart() instanceof PersonView)
		{
			PersonView view = (PersonView) site.getPart();
			if (!view.getViewer().getSelection().isEmpty())
			{
				StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
				{
					Guide guide = null;
					if (ssel.getFirstElement() instanceof Person)
					{
						guide = ((Person) ssel.getFirstElement()).getDefaultLink().getGuide();
					}
					else if (ssel.getFirstElement() instanceof LinkPersonAddress)
					{
						guide = ((LinkPersonAddress) ssel.getFirstElement()).getGuide();
					}
					element.setChecked(guide != null && !guide.isDeleted());
				}
			}
		}
	}

}
