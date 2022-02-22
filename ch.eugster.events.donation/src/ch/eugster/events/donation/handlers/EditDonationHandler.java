package ch.eugster.events.donation.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.donation.editors.DonationEditor;
import ch.eugster.events.donation.editors.DonationEditorInput;
import ch.eugster.events.persistence.model.Donation;

public class EditDonationHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		ISelection sel = (ISelection) context.getVariable("selection");
		{
			if (sel instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) sel;
				{
					if (!ssel.isEmpty() && ssel.getFirstElement() instanceof Donation)
					{
						Donation donation = (Donation) ssel.getFirstElement();
						try
						{
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
									.openEditor(new DonationEditorInput(donation), DonationEditor.ID);
						}
						catch (PartInitException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext)
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		Object object = context.getVariable("selection");
		if (object instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) object;
			object = ssel.getFirstElement();
			enabled = object instanceof Donation;
		}
		setBaseEnabled(enabled);
	}
}
