package ch.eugster.events.importer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.importer.wizards.ImportWizard;

public class StartWwfWizard extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext evaluationContext = (EvaluationContext) event.getApplicationContext();
		Shell shell = (Shell) evaluationContext.getVariable("activeShell");
		ImportWizard wizard = new ImportWizard();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.setPageSize(new Point(600, 400));
		int result = dialog.open();
		// TODO Auto-generated method stub
		return null;
	}

}
