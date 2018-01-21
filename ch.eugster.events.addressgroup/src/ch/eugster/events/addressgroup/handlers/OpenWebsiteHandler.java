package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.ui.helpers.BrowseHelper;
import ch.eugster.events.ui.helpers.EmailHelper;

public class OpenWebsiteHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (BrowseHelper.getInstance().isBrowsingSupported())
		{
			if (event.getApplicationContext() instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) event.getApplicationContext();
				ISelection sel = (ISelection) context.getParent().getVariable("selection");
				if (sel instanceof StructuredSelection)
				{
					StructuredSelection ssel = (StructuredSelection) sel;
					if (ssel.getFirstElement() instanceof AddressGroupMember)
					{
						AddressGroupMember member = (AddressGroupMember) ssel.getFirstElement();
						if (member.getLink() == null || member.getLink().isDeleted()
								|| member.getLink().getPerson().isDeleted())
						{
							if (!member.getAddress().getWebsite().isEmpty())
							{
								BrowseHelper.getInstance().browse(member.getAddress().getWebsite());
							}
						}
						else if (!member.getLink().getPerson().getWebsite().isEmpty())
						{
							BrowseHelper.getInstance().browse(member.getLink().getPerson().getWebsite());
						}
					}
				}
			}
		}
		return null;
	}

	private boolean hasValidInternetAddress(final AddressGroupMember member)
	{
		if (member.isValidAddressMember())
		{
			if (BrowseHelper.getInstance().isValidAddress(member.getAddress().getWebsite()))
			{
				return true;
			}
		}
		else if (member.isValidLinkMember())
		{
			if (EmailHelper.getInstance().isValidAddress(member.getLink().getPerson().getWebsite()))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getVariable("selection") instanceof StructuredSelection)
		{
			Object selection = context.getVariable("selection");
			if (selection instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) selection;
				if (ssel.getFirstElement() instanceof AddressGroupMember)
				{
					AddressGroupMember member = (AddressGroupMember) ssel.getFirstElement();
					enabled = hasValidInternetAddress(member);
				}
			}
		}
		setBaseEnabled(enabled);
	}
}
