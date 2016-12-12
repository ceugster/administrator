package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class DeleteCategoryHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
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
					if (ssel.getFirstElement() instanceof AddressGroupCategory)
					{
						AddressGroupCategory category = (AddressGroupCategory) ssel.getFirstElement();
						Shell shell = part.getSite().getShell();
						String title = "Löschbestätigung";
						StringBuilder msg = new StringBuilder("Soll die ausgewählte Kategorie ");
						msg = msg.append(category.getCode().equals("") ? category.getName() : category.getCode()
								+ " - " + category.getName());
						msg = msg.append(" entfernt werden?");
						msg = msg
								.append("\nBitte beachten Sie, dass alle Adressgruppen dieser Kategorie ebenfalls entfernt werden.");
						int icon = MessageDialog.QUESTION;
						String[] buttons = new String[] { "Ja", "Nein" };
						MessageDialog dialog = new MessageDialog(shell, title, null, msg.toString(), icon, buttons, 0);
						if (dialog.open() == 0)
						{
							if (connectionService != null)
							{
								AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) connectionService
										.getQuery(AddressGroupCategory.class);
								query.delete(category);
							}
						}
					}
				}
			}
		}
		return null;
	}

}
