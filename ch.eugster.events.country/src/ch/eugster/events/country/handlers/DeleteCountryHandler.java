package ch.eugster.events.country.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class DeleteCountryHandler extends ConnectionServiceDependentAbstractHandler
{
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			Object ctx = event.getApplicationContext();
			if (ctx instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) ctx;
				StructuredSelection ssel = (StructuredSelection) context.getVariable("selection");
				IWorkbenchPart part = (IWorkbenchPart) context.getVariable("activePart");
				if (!ssel.isEmpty() && ssel.size() == 1)
				{
					if (ssel.getFirstElement() instanceof Country)
					{
						long count = 0L;
						Country country = (Country) ssel.getFirstElement();
						if (connectionService != null)
						{
							PersonQuery personQuery = (PersonQuery) connectionService.getQuery(Person.class);
							count = personQuery.countByCountry(country);

							if (count == 0L)
							{
								AddressQuery addressQuery = (AddressQuery) connectionService.getQuery(Address.class);
								count = addressQuery.countByCountry(country);
							}
							Shell shell = part.getSite().getShell();
							if (count == 0L)
							{
								String title = "Löschbestätigung";
								StringBuilder msg = new StringBuilder("Soll das ausgewählte Land " + country.getName()
										+ " entfernt werden?");
								int icon = MessageDialog.QUESTION;
								String[] buttons = new String[] { "Ja", "Nein" };
								MessageDialog dialog = new MessageDialog(shell, title, null, msg.toString(), icon,
										buttons, 0);
								if (dialog.open() == 0)
								{
									if (connectionService != null)
									{
										CountryQuery deleteQuery = (CountryQuery) connectionService
												.getQuery(Country.class);
										deleteQuery.delete(country);
									}
								}
							}
							else
							{
								String title = country.getName();
								StringBuilder msg = new StringBuilder("Das ausgewählte Land " + country.getName()
										+ " kann nicht entfernt werden");
								int icon = MessageDialog.INFORMATION;
								String[] buttons = new String[] { "OK" };
								MessageDialog dialog = new MessageDialog(shell, title, null, msg.toString(), icon,
										buttons, 0);
								dialog.open();
							}
						}
					}
				}
			}
		}
		return null;
	}
}
