package ch.eugster.events.ui.wizards;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class WizardDialog extends org.eclipse.jface.wizard.WizardDialog
{
	public WizardDialog(Shell parentShell, Wizard wizard)
	{
		super(parentShell, wizard);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		super.createButtonsForButtonBar(parent);
		if (this.getButton(IDialogConstants.FINISH_ID) != null)
			this.getButton(IDialogConstants.FINISH_ID).setText("Ausführen");
		if (this.getButton(IDialogConstants.BACK_ID) != null)
			this.getButton(IDialogConstants.BACK_ID).setText("Zurück");
		if (this.getButton(IDialogConstants.NEXT_ID) != null)
			this.getButton(IDialogConstants.NEXT_ID).setText("Weiter");
		if (this.getButton(IDialogConstants.CANCEL_ID) != null)
			this.getButton(IDialogConstants.CANCEL_ID).setText("Abbrechen");
		this.getShell().addControlListener(new ControlListener()
		{

			@Override
			public void controlMoved(ControlEvent e)
			{
			}

			@Override
			public void controlResized(ControlEvent e)
			{
			}

		});
	}
}
