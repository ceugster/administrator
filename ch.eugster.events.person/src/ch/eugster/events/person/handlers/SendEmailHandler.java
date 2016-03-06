package ch.eugster.events.person.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.StructuredSelection;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.ui.helpers.EmailHelper;

public class SendEmailHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				List<String> addresses = new ArrayList<String>();
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				Iterator<?> iterator = ssel.iterator();
				while (iterator.hasNext())
				{
					Object element = iterator.next();

					if (element instanceof Person)
					{
						Person person = (Person) element;
						if (addEmail(addresses, person.getEmail()))
						{
							continue;
						}
						LinkPersonAddress[] links = person.getLinks().toArray(new LinkPersonAddress[0]);
						for (LinkPersonAddress link : links)
						{
							if (addEmail(addresses, link.getEmail()))
							{
								continue;
							}
						}
						for (LinkPersonAddress link : links)
						{
							if (addEmail(addresses, link.getAddress().getEmail()))
							{
								continue;
							}
						}
					}
					if (element instanceof LinkPersonAddress)
					{
						LinkPersonAddress link = (LinkPersonAddress) element;
						if (addEmail(addresses, link.getEmail()))
						{
							continue;
						}
						if (addEmail(addresses, link.getAddress().getEmail()))
						{
							continue;
						}
					}
					if (element instanceof Address)
					{
						Address address = (Address) element;
						if (addEmail(addresses, address.getEmail()))
						{
							continue;
						}
					}
				}

				if (!addresses.isEmpty())
				{
					if (EmailHelper.getInstance().isEmailSupported())
					{
						EmailHelper.getInstance().sendEmail(addresses.toArray(new String[0]));
					}
				}
			}
		}
		return null;
	}

	private boolean addEmail(List<String> emails, String email)
	{
		if (EmailHelper.getInstance().isValidAddress(email))
		{
			if (!emails.contains(email))
			{
				emails.add(email);
				return true;
			}
		}
		return false;
	}
}
