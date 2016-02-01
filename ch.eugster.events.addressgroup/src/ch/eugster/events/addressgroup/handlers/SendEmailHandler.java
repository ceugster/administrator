package ch.eugster.events.addressgroup.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.ui.helpers.EmailHelper;

public class SendEmailHandler extends AbstractHandler implements IHandler
{
	private final List<String> addresses = new ArrayList<String>();

	private void addAddress(final String address)
	{
		if (EmailHelper.getInstance().isValidAddress(address))
		{
			if (!this.addresses.contains(address))
				this.addresses.add(address);
		}
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (EmailHelper.getInstance().isEmailSupported())
		{
			if (!this.addresses.isEmpty())
				this.addresses.clear();

			if (event.getApplicationContext() instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) event.getApplicationContext();
				ISelection sel = (ISelection) context.getParent().getVariable("selection");
				if (sel instanceof StructuredSelection)
				{
					StructuredSelection ssel = (StructuredSelection) sel;
					Iterator<?> iterator = ssel.iterator();
					while (iterator.hasNext())
					{
						Object element = iterator.next();
						if (element instanceof AddressGroupCategory)
						{
							this.extract((AddressGroupCategory) element);
						}
						else if (element instanceof AddressGroup)
						{
							this.extract((AddressGroup) element);
						}
						// else if (element instanceof AddressGroupLink)
						// {
						// this.extract(((AddressGroupLink)
						// element).getChild());
						// }
						else if (element instanceof AddressGroupMember)
						{
							this.extract((AddressGroupMember) element);
						}
					}

					if (!this.addresses.isEmpty())
						EmailHelper.getInstance().sendEmail(this.addresses.toArray(new String[0]));
				}
			}
		}
		return null;
	}

	private void extract(final AddressGroup addressGroup)
	{
		if (!addressGroup.isDeleted())
		{
			List<AddressGroupMember> addressGroupMembers = addressGroup.getAddressGroupMembers();
			for (AddressGroupMember addressGroupMember : addressGroupMembers)
			{
				this.extract(addressGroupMember);
			}
			// for (AddressGroupLink link : addressGroup.getChildren())
			// {
			// if (!link.isDeleted() && !link.getChild().isDeleted())
			// {
			// extract(link.getChild());
			// }
			// }
		}
	}

	private void extract(final AddressGroupCategory category)
	{
		if (!category.isDeleted())
		{
			List<AddressGroup> addressGroups = category.getAddressGroups();
			for (AddressGroup addressGroup : addressGroups)
			{
				this.extract(addressGroup);
			}
		}
	}

	private void extract(final AddressGroupMember member)
	{
		if (!member.isDeleted())
		{
			if (member.getLink() == null && !member.getAddress().isDeleted())
			{
				addAddress(member.getAddress().getEmail());
			}
			else if (!member.getLink().isDeleted() && !member.getLink().getPerson().isDeleted())
			{
				if (EmailHelper.getInstance().isValidAddress(member.getLink().getPerson().getEmail()))
				{
					addAddress(member.getLink().getPerson().getEmail());
				}
				else if (EmailHelper.getInstance().isValidAddress(member.getLink().getEmail()))
				{
					addAddress(member.getLink().getEmail());
				}
				else if (EmailHelper.getInstance().isValidAddress(member.getAddress().getEmail()))
				{
					addAddress(member.getAddress().getEmail());
				}
			}
		}
	}

	private boolean hasValidEmailAddress(final AddressGroup addressGroup)
	{
		if (!addressGroup.isDeleted())
		{
			for (AddressGroupMember member : addressGroup.getAddressGroupMembers())
			{
				if (hasValidEmailAddress(member))
				{
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasValidEmailAddress(final AddressGroupCategory category)
	{
		if (!category.isDeleted())
		{
			for (AddressGroup addressGroup : category.getAddressGroups())
			{
				if (hasValidEmailAddress(addressGroup))
				{
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasValidEmailAddress(final AddressGroupMember member)
	{
		if (!member.isDeleted())
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
