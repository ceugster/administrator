package ch.eugster.events.course.reporting.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.persistence.model.Booking;

public class UpdateBookingStateQueryDialog extends TitleAreaDialog
{
	private Button button;

	private String reportType;

	public UpdateBookingStateQueryDialog(Shell parentShell, Booking[] bookings, String reportType)
	{
		super(parentShell);
		this.reportType = reportType;
	}

	@Override
	public Control createDialogArea(Composite parent)
	{
		this.getShell().setText("Drucken");
		this.setTitle("Drucken mit automatischer Aktualisierung");
		this.setMessage("Bestätigung für ausgewählte Buchungen drucken und Datum aktualisieren.");

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(2, false));

		this.button = new Button(composite, SWT.CHECK);
		this.button.setText("Datum " + this.reportType + " aktualisieren");
		this.button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		this.createButton(parent, Dialog.OK, "Drucken", true);
		this.createButton(parent, 16, "Drucken und Aktualisieren", false);
		this.createButton(parent, Dialog.CANCEL, "Abbrechen", false);
	}

	@Override
	protected void buttonPressed(int id)
	{
		super.buttonPressed(id);

		if (id == 16)
		{

		}
	}

	@Override
	protected void okPressed()
	{

		this.close();
	}
}
