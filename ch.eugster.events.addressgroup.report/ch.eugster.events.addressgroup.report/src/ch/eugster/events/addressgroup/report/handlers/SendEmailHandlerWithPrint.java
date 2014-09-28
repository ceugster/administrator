package ch.eugster.events.addressgroup.report.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.addressgroup.report.RecipientListFactory;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.ui.helpers.EmailHelper;

public class SendEmailHandlerWithPrint extends PrintEmailRecipientsHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (EmailHelper.getInstance().isEmailSupported())
		{
			if (event.getApplicationContext() instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) event.getApplicationContext();
				ISelection sel = (ISelection) context.getParent().getVariable("selection");
				shell = (Shell) context.getParent().getVariable("activeShell");
				if (sel instanceof IStructuredSelection)
				{
					IStructuredSelection ssel = (IStructuredSelection) sel;
					if (ssel.isEmpty())
					{

					}
					else
					{
						if (buildRecipientsList(ssel, Filter.ONLY_WITH_EMAILS) > 0)
						{
							printRecipientList();
							EmailHelper.getInstance().sendEmail(RecipientListFactory.getEmails());
						}
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	private boolean hasValidEmailAddress(final AddressGroup addressGroup)
	{
		for (AddressGroupMember member : addressGroup.getAddressGroupMembers())
		{
			if (hasValidEmailAddress(member))
			{
				return true;
			}
		}
		return false;
	}

	private boolean hasValidEmailAddress(final AddressGroupCategory category)
	{
		for (AddressGroup addressGroup : category.getAddressGroups())
		{
			if (hasValidEmailAddress(addressGroup))
			{
				return true;
			}
		}
		return false;
	}

	private boolean hasValidEmailAddress(final AddressGroupMember member)
	{
		if (member.getLink() == null || member.getLink().isDeleted() || member.getLink().getPerson().isDeleted())
		{
			if (EmailHelper.getInstance().isValidAddress(member.getAddress().getEmail()))
			{
				return true;
			}
		}
		else
		{
			if (EmailHelper.getInstance().isValidAddress(member.getLink().getPerson().getEmail()))
			{
				return true;
			}
			if (EmailHelper.getInstance().isValidAddress(member.getLink().getEmail()))
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
				Object[] objects = ssel.toArray();
				{
					for (Object object : objects)
					{
						if (object instanceof AddressGroupCategory)
						{
							AddressGroupCategory category = (AddressGroupCategory) object;
							enabled = hasValidEmailAddress(category);
						}
						else if (object instanceof AddressGroup)
						{
							AddressGroup addressGroup = (AddressGroup) object;
							enabled = hasValidEmailAddress(addressGroup);
						}
						else if (object instanceof AddressGroupMember)
						{
							AddressGroupMember member = (AddressGroupMember) object;
							enabled = hasValidEmailAddress(member);
						}
						if (enabled)
						{
							break;
						}
					}
				}
			}
		}
		setBaseEnabled(enabled);
	}

}
