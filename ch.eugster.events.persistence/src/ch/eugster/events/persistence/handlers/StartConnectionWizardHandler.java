package ch.eugster.events.persistence.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleException;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.wizards.ConnectionWizard;

public class StartConnectionWizardHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			Display display = Display.getDefault();
			display.syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					Display display = Display.getDefault();
					Shell shell = new Shell(display);
					ConnectionWizard wizard = new ConnectionWizard();
					WizardDialog dialog = new WizardDialog(shell, wizard);
					if (dialog.open() == IDialogConstants.OK_ID)
					{
						try
						{
							String symbolicName = Activator.getDefault().getBundle().getSymbolicName();
							Activator.getDefault().getBundle().stop();
							Platform.getBundle(symbolicName).start();
						}
						catch (final BundleException e)
						{
						}
					}
				}
			});
		}
		return null;
	}

}
