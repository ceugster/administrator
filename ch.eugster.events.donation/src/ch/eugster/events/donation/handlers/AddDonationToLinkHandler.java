package ch.eugster.events.donation.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.donation.editors.DonationEditor;
import ch.eugster.events.donation.editors.DonationEditorInput;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class AddDonationToLinkHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		ISelection sel = (ISelection) context.getVariable("selection");
		if (sel instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) sel;
			if (!ssel.isEmpty())
			{
				AbstractEntity entity = null;
				if (ssel.getFirstElement() instanceof Person)
				{
					entity = ((Person) ssel.getFirstElement()).getDefaultLink();
				}
				else if (ssel.getFirstElement() instanceof LinkPersonAddress)
				{
					entity = (LinkPersonAddress) ssel.getFirstElement();
				}
				else if (ssel.getFirstElement() instanceof Address)
				{
					entity = (Address) ssel.getFirstElement();
				}
				if (entity != null)
				{
					Donation donation = null;
					if (entity instanceof LinkPersonAddress)
					{
						donation = Donation.newInstance((LinkPersonAddress) entity);
					}
					else if (entity instanceof Address)
					{
						donation = Donation.newInstance((Address) entity);
					}
					if (donation != null)
					{
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
}
