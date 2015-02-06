package ch.eugster.events.donation.dialogs;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.donation.Activator;
import ch.eugster.events.donation.views.DonationPurposeLabelProvider;
import ch.eugster.events.donation.views.DonationPurposeSorter;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.queries.DonationPurposeQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationDialog extends TitleAreaDialog
{
	private final Donation donation;

	private CDateTime date;

	private Text amount;

	private ComboViewer purpose;

	private Composite composite;

	private final String addMessage = "Erfassen sie die gewünschten Angaben und speichern Sie sie mit 'Speichern'\noder schliessen Sie das Fenster mit 'Abbrechen' ohne die Daten zu speichern.";

	private final String editMessage = "Bearbeiten Sie die gewünschten Angaben und speichern Sie sie mit 'Speichern'.\nSchliessen Sie das Fenster mit 'Abbrechen' ohne die Änderungen zu übernehmen.";

	private boolean pageComplete = false;

	/**
	 * @param parentShell
	 * @param parent
	 * @param addressGroup
	 *            Falls eine neue Adressgruppe erfasst wird, muss diese bereit
	 *            vor der Übergabe an den Konstruktor von
	 *            <code>AddressGroupDialog</code> instantiiert sein und der
	 *            Parent <code>Domain</code> muss - falls ein solcher gesetzt
	 *            werden soll, ebenfalls dem Konstruktur von
	 *            <code>AddressGroup</code> übergeben worden sein.
	 * 
	 */
	public DonationDialog(final Shell parentShell, final Donation donation)
	{
		super(parentShell);
		super.setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
		this.donation = donation;
	}

	private void createAmountControl(final Composite composite)
	{
		GridData layoutData = new GridData();
		layoutData.widthHint = 72;

		NumberFormat currencyFormat = DecimalFormat.getNumberInstance();
		currencyFormat.setMaximumFractionDigits(DecimalFormat.getCurrencyInstance().getMaximumFractionDigits());
		currencyFormat.setMinimumFractionDigits(DecimalFormat.getCurrencyInstance().getMinimumFractionDigits());

		this.amount = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		this.amount.setData("format", currencyFormat);
		this.amount.setData("value", new Double(0d));
		this.amount.setLayoutData(layoutData);
		this.amount.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent event)
			{
				DonationDialog.this.amount.setText(DonationDialog.this.amount.getData("value").toString());
				DonationDialog.this.amount.selectAll();
			}

			@Override
			public void focusLost(final FocusEvent event)
			{
				Double value = (Double) DonationDialog.this.amount.getData("value");
				DonationDialog.this.amount.setText(((NumberFormat) DonationDialog.this.amount.getData("format"))
						.format(value));
			}
		});
		this.amount.addVerifyListener(new VerifyListener()
		{
			@Override
			public void verifyText(final VerifyEvent event)
			{
				if (event.text.length() > 0)
				{
					if (!event.text.equals("."))
					{
						try
						{
							new Double(event.text);
						}
						catch (NumberFormatException e)
						{
							event.doit = false;
						}
					}
				}
			}
		});
		this.amount.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				String text = DonationDialog.this.amount.getText();
				if (text.length() == 0)
					text = "0.0";

				Double value = new Double(text);
				DonationDialog.this.amount.setData("value", value);
				DonationDialog.this.setPageComplete();
			}
		});
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, "Speichern", true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
		this.setPageComplete();
	}

	private void createDateControl(final Composite composite)
	{
		this.date = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM);
		this.date.setLayoutData(new GridData());
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

		this.composite = new Composite(parent, SWT.NONE);
		this.composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.composite.setLayout(new GridLayout(2, false));

		Label label = new Label(this.composite, SWT.NONE);
		label.setText("Datum");
		this.createDateControl(this.composite);

		label = new Label(this.composite, SWT.NONE);
		label.setText("Betrag");
		this.createAmountControl(this.composite);

		label = new Label(this.composite, SWT.NONE);
		label.setText("Zweckbestimmung");
		this.createPurposeControl(this.composite);

		this.setFieldValues();

		return this.composite;
	}

	private void createPurposeControl(final Composite composite)
	{
		Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.purpose = new ComboViewer(combo);
		this.purpose.setContentProvider(new ArrayContentProvider());
		this.purpose.setLabelProvider(new DonationPurposeLabelProvider());
		this.purpose.setSorter(new DonationPurposeSorter());

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();

		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			DonationPurposeQuery query = (DonationPurposeQuery) service.getQuery(DonationPurpose.class);
			List<DonationPurpose> purposes = query.selectAll();
			this.purpose.setInput(purposes.toArray(new DonationPurpose[0]));
		}
		tracker.close();
	}

	public void getFieldValues()
	{
		Date date = this.date.getSelection();
		if (date != null)
		{
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(this.date.getSelection());
			this.donation.setDonationDate(calendar);
		}

		this.donation.setAmount(((Double) this.amount.getData("value")).doubleValue());
		StructuredSelection ssel = (StructuredSelection) this.purpose.getSelection();
		this.donation.setPurpose((DonationPurpose) ssel.getFirstElement());
	}

	public boolean isPageComplete()
	{
		return this.pageComplete;
	}

	@Override
	protected void okPressed()
	{
		this.getFieldValues();
		super.okPressed();
	}

	@Override
	public void setErrorMessage(final String errorMessage)
	{
		super.setErrorMessage(errorMessage);
	}

	public void setFieldValues()
	{
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		this.date.setSelection(calendar.getTime());

		this.amount.setData("value", this.donation.getAmount());
		this.amount.setText(((NumberFormat) this.amount.getData("format")).format(this.donation.getAmount()));

		StructuredSelection ssel = null;
		if (donation.getPurpose() == null)
		{
			ssel = new StructuredSelection();
		}
		else
		{
			ssel = new StructuredSelection(new DonationPurpose[] { donation.getPurpose() });
		}
		this.purpose.setSelection(ssel);
	}

	public void setMessage()
	{
		this.setErrorMessage(null);
		if (this.donation.getId() == null)
			super.setMessage(this.addMessage);
		else
			super.setMessage(this.editMessage);
	}

	public void setPageComplete()
	{
		this.pageComplete = true;
		String err = null;

		if (this.date.getSelection() == null)
		{
			this.pageComplete = false;
			err = "Bitte geben Sie das Spendedatum ein.";
			this.date.setFocus();
			return;
		}

		try
		{
			Double value = (Double) this.amount.getData("value");
			if (value.equals(new Double(0d)))
			{
				this.pageComplete = false;
				err = "Bitte geben Sie einen Betrag ein.";
				this.amount.setFocus();
			}
		}
		catch (NumberFormatException e)
		{
			this.pageComplete = false;
			err = "Bitte geben Sie einen Betrag ein.";
			this.amount.setFocus();
		}

		if (err == null)
		{
			this.setMessage();
			if (this.getButton(IDialogConstants.OK_ID) != null)
				this.getButton(IDialogConstants.OK_ID).setEnabled(true);
		}
		else
		{
			this.setErrorMessage(err);
			if (this.getButton(IDialogConstants.OK_ID) != null)
				this.getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}

	public void setTitle()
	{
		NumberFormat nf = new DecimalFormat(PersonSettings.getInstance().getIdFormat());
		String text = null;
		if (this.donation.getLink() == null || donation.getLink().isDeleted()
				|| donation.getLink().getPerson().isDeleted())
		{
			Address address = this.donation.getAddress();
			text = nf.format(address.getId() + " - " + address.getName());
		}
		else
		{
			Person person = donation.getLink().getPerson();
			text = nf.format(person.getId() + " - " + person.getLastname() + ", " + person.getFirstname());
			this.getShell().setText(text);
		}

		if (this.donation.getId() == null)
			super.setTitle("Neue Spende erfassen");
		else
			super.setTitle("Spende bearbeiten");
	}
}
