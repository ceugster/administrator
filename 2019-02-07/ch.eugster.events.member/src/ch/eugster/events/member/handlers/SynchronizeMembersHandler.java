package ch.eugster.events.member.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.member.wizards.ImportMemberSelectSourceFileWizardPage;
import ch.eugster.events.member.wizards.ImportMemberWizard;
import ch.eugster.events.member.wizards.SelectPersonWizardPage;
import ch.eugster.events.persistence.model.Membership;

public class SynchronizeMembersHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		Object selection = context.getVariable("selection");
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.getFirstElement() instanceof Membership)
			{
				Object object = context.getVariable("activeShell");
				if (object instanceof Shell)
				{
					Shell shell = (Shell) object;
					ImportMemberWizard wizard = new ImportMemberWizard((Membership) ssel.getFirstElement());
					wizard.addPage(new ImportMemberSelectSourceFileWizardPage());
					wizard.addPage(new SelectPersonWizardPage(wizard));
					WizardDialog dialog = new WizardDialog(shell, wizard);
					dialog.open();
				}
			}
		}
		return Status.OK_STATUS;
	}

}
