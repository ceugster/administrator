package ch.eugster.events.course.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.course.Activator;
import ch.eugster.events.course.handlers.CoursePasteHandler.Option;

public class CourseCopyDialog extends TitleAreaDialog
{
	private IDialogSettings settings;

	private final IStructuredSelection ssel;

	private final String message = "Kurse kopieren.";

	private boolean isPageComplete = false;

	public CourseCopyDialog(final Shell parentShell, IStructuredSelection ssel)
	{
		super(parentShell);
		this.ssel = ssel;
		settings = Activator.getDefault().getDialogSettings().getSection("course.copy.dialog.settings");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("course.copy.dialog.settings");
		}
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, "Weiter", true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		super.setTitle(ssel.size() > 1 ? "Kurse kopieren" : "Kurs kopieren");
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setLayout(new GridLayout());
		group.setText("Optionen");

		for (Option option : Option.values())
		{
			option.getButton(group, settings);
		}

		return parent;
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		super.okPressed();
	}
	
	@Override
	public void setErrorMessage(final String errorMessage)
	{
		super.setErrorMessage(errorMessage);
		this.setPageComplete(false);
	}

	public void setMessage()
	{
		this.setErrorMessage(null);
		super.setMessage(this.message);
		this.setPageComplete(true);
	}

	public void setPageComplete(final boolean isComplete)
	{
		this.isPageComplete = isComplete;
		if (this.getButton(IDialogConstants.OK_ID) != null)
			this.getButton(IDialogConstants.OK_ID).setEnabled(this.isPageComplete);
	}

}
