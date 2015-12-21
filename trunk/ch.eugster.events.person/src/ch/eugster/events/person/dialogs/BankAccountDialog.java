package ch.eugster.events.person.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
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
import org.iban4j.Iban;
import org.iban4j.IbanUtil;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Bank;
import ch.eugster.events.persistence.model.BankAccount;
import ch.eugster.events.persistence.queries.BankAccountQuery;
import ch.eugster.events.persistence.queries.BankQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;

public class BankAccountDialog extends TitleAreaDialog
{
	private ServiceTracker tracker;
	
	private BankAccount account;

	private Text iban;
	
	private Label status;
	
	private Text code;
	
	private ComboViewer bankViewer;

	public BankAccountDialog(Shell shell, BankAccount account)
	{
		super(shell);
		this.account = account;
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		this.setTitle(PersonFormatter.getInstance().formatLastnameFirstname(account.getPerson()));
		this.setMessage("Erfassen oder bearbeiten Sie die Bankverbindung.");
		
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("IBAN");

		iban = new Text(composite, SWT.BORDER);
		iban.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    iban.addVerifyListener(new VerifyListener() 
	    {
	        public void verifyText(VerifyEvent e) 
	        {
	            e.text = e.text.toUpperCase();
	        }
	      });
		iban.addModifyListener(new ModifyListener() 
		{
			@Override
			public void modifyText(ModifyEvent e) 
			{
				try
				{
					IbanUtil.validate(iban.getText());
					status.setImage(Activator.getDefault().getImageRegistry().get(Activator.KEY_OK));
					Iban i = Iban.valueOf(iban.getText());
					code.setText(i.getAccountNumber());
					String bcNr = Long.valueOf(i.getBankCode()).toString();
					
					ConnectionService service = (ConnectionService) tracker.getService();
					if (service != null)
					{
						BankQuery query = (BankQuery) service.getQuery(Bank.class);
						List<Bank> banks = query.selectByBcNr(bcNr);
						bankViewer.setSelection(new StructuredSelection(banks.isEmpty() ? new Bank[0] : new Bank[] { banks.get(0) }));
					}
				}
				catch (Exception ex)
				{
					status.setImage(Activator.getDefault().getImageRegistry().get(Activator.KEY_WARN));
				}
			}
		});

		status = new Label(composite, SWT.None);
		status.setLayoutData(new GridData());
		
		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Konto");

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		code = new Text(composite, SWT.BORDER);
		code.setLayoutData(gridData);
		code.setText(account.getAccountNumber());

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Bank");
		
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(gridData);

		bankViewer = new ComboViewer(combo);
		bankViewer.setContentProvider(new ArrayContentProvider());
		bankViewer.setLabelProvider(new LabelProvider() 
		{
			@Override
			public String getText(Object element) 
			{
				if (element instanceof Bank)
				{
					Bank bank = (Bank) element;
					return bank.toString();
				}
				return "";
			}
		});

		tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class.getName(), null);
		tracker.open();

		bankViewer.setInput(getInput());
//		bankViewer.setSelection(account.getBank() == null ? new StructuredSelection() : new StructuredSelection(new Bank[] { account.getBank() }));
		
		iban.setText(account.getIban());

		return composite;
	}

	private Bank[] getInput()
	{
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			BankQuery query = (BankQuery) service.getQuery(Bank.class);
			return query.selectValid().toArray(new Bank[0]);
		}
		return new Bank[0];
	}
	
	public void okPressed()
	{
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			IStructuredSelection ssel = (IStructuredSelection) bankViewer.getSelection();
			Bank bank = ssel.isEmpty() ? null : (Bank) ssel.getFirstElement();
			BankAccountQuery query = (BankAccountQuery) service.getQuery(BankAccount.class);
			account.setAccountNumber(code.getText());
			account.setBank(bank);
			account.setIban(iban.getText());
			account = query.merge(account);
		}
		super.okPressed();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
	}

	public boolean close()
	{
		tracker.close();
		return super.close();
	}
}
