package ch.eugster.events.course.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.course.wizards.BookingWizard;
import ch.eugster.events.course.wizards.ParticipantWizardPage;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.ui.wizards.WizardDialog;

public class AddParticipantHandler extends AbstractHandler implements IHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		StructuredSelection ssel = (StructuredSelection) context.getParent().getVariable("selection");
		if (ssel.getFirstElement() instanceof Booking)
		{
			Booking booking = (Booking) ssel.getFirstElement();
			BookingWizard wizard = new BookingWizard(booking);
			ParticipantWizardPage participantPage = new ParticipantWizardPage("participantWizardPage", booking);
			wizard.addPage(participantPage);

			Shell shell = (Shell) context.getParent().getVariable("activeShell");
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.open();
		}
		return null;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getVariable("selection") instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) context.getVariable("selection");
			if (ssel.getFirstElement() instanceof Booking)
			{
				enabled = true;
			}
		}
		this.setBaseEnabled(enabled);
	}

}
