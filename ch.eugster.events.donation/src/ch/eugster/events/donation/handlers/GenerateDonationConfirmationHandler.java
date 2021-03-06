package ch.eugster.events.donation.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.donation.dialogs.DonationConfirmationDialog;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class GenerateDonationConfirmationHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			if (context.getParent().getVariable("selection") instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
				List<Donation> donations = new ArrayList<Donation>();
				Object[] sels = ssel.toArray();
				{
					for (Object sel : sels)
					{
						if (sel instanceof Donation)
						{
							donations.add((Donation) sel);
						}
					}
				}
				Shell shell = (Shell) context.getParent().getVariable("activeShell");
				DonationConfirmationDialog dialog = new DonationConfirmationDialog(shell,
						donations.toArray(new Donation[0]));
				dialog.open();
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public void setEnabled(final Object object)
	{
		boolean enabled = connectionService != null;
		if (enabled)
		{
			if (object instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) object;
				if (context.getParent().getVariable("selection") instanceof StructuredSelection)
				{
					StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
					Object[] elements = ssel.toArray();
					for (Object element : elements)
					{
						if (element instanceof DonationYear)
						{

						}
						else if (element instanceof Donation)
						{

						}
						else
						{
							enabled = false;
							break;
						}
					}
				}
			}
		}
		setBaseEnabled(enabled);
	}
}
