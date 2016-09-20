package ch.eugster.events.person.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.ui.helpers.BrowseHelper;

public class OpenWebsiteHandler extends AbstractHandler implements IHandler
{
	private boolean enabled;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				String url = null;
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				if (ssel.getFirstElement() instanceof LinkPersonAddress)
				{

					LinkPersonAddress link = (LinkPersonAddress) ssel.getFirstElement();
					if (!link.getPerson().getWebsite().isEmpty())
					{
						url = link.getPerson().getWebsite();
					}
				}
				else if (ssel.getFirstElement() instanceof Person)
				{
					url = ((Person) ssel.getFirstElement()).getWebsite();
				}
				else if (ssel.getFirstElement() instanceof Address)
				{
					url = ((Address) ssel.getFirstElement()).getWebsite();
				}

				if (url != null && !url.isEmpty())
				{
					if (BrowseHelper.getInstance().isBrowsingSupported())
					{
						BrowseHelper.getInstance().browse(url);
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(Object object)
	{
		if (object instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) object;
			{
				if (context.getParent().getVariable("selection") instanceof StructuredSelection)
				{
					StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
					if (ssel.size() == 1 && ssel.getFirstElement() instanceof LinkPersonAddress)
					{
						LinkPersonAddress link = (LinkPersonAddress) ssel.getFirstElement();
						this.enabled = !link.getPerson().getWebsite().isEmpty();
					}
				}
			}
		}
	}

	@Override
	public boolean isEnabled()
	{
		return this.enabled;
	}

}
