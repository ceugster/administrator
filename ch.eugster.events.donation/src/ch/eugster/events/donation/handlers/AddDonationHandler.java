package ch.eugster.events.donation.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.donation.editors.DonationEditor;
import ch.eugster.events.donation.editors.DonationEditorInput;
import ch.eugster.events.donation.views.LinkDonationView;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class AddDonationHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		AbstractEntity entity = null;
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		IViewPart viewPart = (IViewPart) context.getParent().getVariable("activePart");
		if (viewPart instanceof LinkDonationView)
		{
			LinkDonationView view = (LinkDonationView) viewPart;
			TableViewer viewer = (TableViewer) view.getAdapter(TableViewer.class);
			if (viewer != null)
			{
				if (viewer.getInput() instanceof Person)
				{
					Person person = (Person) viewer.getInput();
					entity = person.getDefaultLink();
				}
				else if (viewer.getInput() instanceof LinkPersonAddress)
				{
					entity = (LinkPersonAddress) viewer.getInput();
				}
				else if (viewer.getInput() instanceof Address)
				{
					entity = (Address) viewer.getInput();
				}
			}
		}
		else
		{
			ISelection sel = (ISelection) context.getVariable("selection");
			if (sel instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) sel;
				if (!ssel.isEmpty())
				{
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
			}
		}
		if (entity instanceof LinkPersonAddress || entity instanceof Address)
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
		return null;
	}
}
