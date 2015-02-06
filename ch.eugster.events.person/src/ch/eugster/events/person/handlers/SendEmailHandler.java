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
						if (!addresses.contains(person.getEmail()))
						{
							addresses.add(person.getEmail());
						}
						LinkPersonAddress[] links = person.getLinks().toArray(new LinkPersonAddress[0]);
						for (LinkPersonAddress link : links)
						{
							if (!link.getEmail().isEmpty())
							{
								if (!addresses.contains(link.getEmail()))
								{
									addresses.add(link.getEmail());
								}
							}
						}
					}
					if (element instanceof Address)
					{
						Address address = (Address) element;
						if (!address.getEmail().isEmpty())
						{
							if (!addresses.contains(address.getEmail()))
								addresses.add(address.getEmail());
						}
					}
					if (element instanceof LinkPersonAddress)
					{
						LinkPersonAddress link = (LinkPersonAddress) element;
						if (!link.getPerson().getEmail().isEmpty())
						{
							if (!addresses.contains(link.getPerson().getEmail()))
								addresses.add(link.getPerson().getEmail());
						}
						if (!link.getEmail().isEmpty())
						{
							if (!addresses.contains(link.getEmail()))
								addresses.add(link.getEmail());
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

}
