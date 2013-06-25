package ch.eugster.events.donation.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.donation.editors.DonationEditor;
import ch.eugster.events.donation.editors.DonationEditorInput;
import ch.eugster.events.donation.views.LinkDonationView;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.IEntity;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class AddDonationToLinkHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();

		IEntity entity = null;

		Object part = context.getVariable("activePart");
		Object sel = context.getVariable("selection");
		if (part instanceof LinkDonationView)
		{
			LinkDonationView view = (LinkDonationView) part;
			Object parent = view.getViewer().getInput();
			if (parent instanceof LinkPersonAddress)
			{
				entity = (LinkPersonAddress) parent;
			}
			else if (parent instanceof Address)
			{
				entity = (Address) parent;
			}
		}
		else if (sel instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) sel;
			if (ssel.getFirstElement() instanceof Person)
			{
				Person person = (Person) ssel.getFirstElement();
				entity = person.getDefaultLink();
			}
			else if (ssel.getFirstElement() instanceof LinkPersonAddress)
			{
				entity = (LinkPersonAddress) ssel.getFirstElement();
			}
			else if (ssel.getFirstElement() instanceof Address)
			{
				entity = (Address) ssel.getFirstElement();
			}
		}
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
		return Status.OK_STATUS;
	}

	@Override
	public void setEnabled(Object evaluationContext)
	{
		setBaseEnabled(true);
	}

	// private boolean enabled(Object input)
	// {
	// boolean enabled = false;
	// if (input instanceof Person)
	// {
	// Person person = (Person) input;
	// enabled = person.getDefaultLink().getId() != null;
	// }
	// else if (input instanceof LinkPersonAddress)
	// {
	// LinkPersonAddress link = (LinkPersonAddress) input;
	// enabled = link.getId() != null;
	// }
	// else if (input instanceof Address)
	// {
	// Address address = (Address) input;
	// enabled = address.getId() != null;
	// }
	// return enabled;
	// }
}
