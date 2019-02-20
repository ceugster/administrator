package ch.eugster.events.person.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.AddressSalutationQuery;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class DeleteSalutationHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getParent().getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
			if (!ssel.isEmpty())
			{
				if (ssel.getFirstElement() instanceof AddressSalutation)
				{
					Shell shell = (Shell) context.getParent().getVariable("activeShell");
					String title = "Anrede entfernen";

					AddressSalutation salutation = (AddressSalutation) ssel.getFirstElement();
					AddressQuery query = (AddressQuery) connectionService.getQuery(Address.class);
					long count = query.countBySalutation(salutation);
					if (count == 0)
					{
						String msg = "Soll die ausgewählte Anrede entfernt werden?";
						int type = MessageDialog.QUESTION;
						String[] buttons = new String[] { "Ja", "Nein" };
						MessageDialog dialog = new MessageDialog(shell, title, null, msg, type, buttons, 0);
						if (dialog.open() == 0)
						{
							Iterator<?> iterator = ssel.iterator();
							while (iterator.hasNext())
							{
								Object object = iterator.next();
								if (object instanceof AddressSalutation)
								{
									if (connectionService != null)
									{
										AddressSalutationQuery deleteQuery = (AddressSalutationQuery) connectionService
												.getQuery(AddressSalutation.class);
										object = deleteQuery.delete((AddressSalutation) object);
									}
								}
							}
						}
					}
					else
					{
						String msg = "Die ausgewählte Anrede kann nicht entfernt werden, da sie noch verwendet wird.";
						int type = MessageDialog.INFORMATION;
						String[] buttons = new String[] { "OK" };
						MessageDialog dialog = new MessageDialog(shell, title, null, msg, type, buttons, 0);
						dialog.open();
					}
				}
			}
		}
		return null;
	}
}
