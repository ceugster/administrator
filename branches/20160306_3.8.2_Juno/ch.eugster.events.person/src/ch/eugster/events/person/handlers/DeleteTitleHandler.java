package ch.eugster.events.person.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.persistence.queries.PersonTitleQuery;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class DeleteTitleHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getParent().getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
			if (!ssel.isEmpty())
			{
				if (ssel.getFirstElement() instanceof PersonTitle)
				{
					Shell shell = (Shell) context.getParent().getVariable("activeShell");
					String title = "Titel entfernen";

					PersonTitle personTitle = (PersonTitle) ssel.getFirstElement();
					PersonQuery query = (PersonQuery) connectionService.getQuery(Person.class);
					long count = query.countByTitle(personTitle);
					if (count == 0)
					{
						String msg = "Soll der ausgewählte Titel entfernt werden?";
						int type = MessageDialog.QUESTION;
						String[] buttons = new String[] { "Ja", "Nein" };
						MessageDialog dialog = new MessageDialog(shell, title, null, msg, type, buttons, 0);
						if (dialog.open() == 0)
						{
							Iterator<?> iterator = ssel.iterator();
							while (iterator.hasNext())
							{
								Object object = iterator.next();
								if (object instanceof PersonTitle)
								{
									if (connectionService != null)
									{
										PersonTitleQuery deleteQuery = (PersonTitleQuery) connectionService
												.getQuery(PersonTitle.class);
										object = deleteQuery.delete((PersonTitle) object);
									}
								}
							}
						}
					}
					else
					{
						String msg = "Der ausgewählte Titel kann nicht entfernt werden, da er noch verwendet wird.";
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
