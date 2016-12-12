package ch.eugster.events.person.dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.Bank;
import ch.eugster.events.persistence.queries.BankQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;

public class UpdateBankDialog extends TitleAreaDialog
{
	private static final String[] FILTERS = { "*", "", "txt", "xls", "xlsx" };

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;
	
	private Text sourcePath;
	
	private Button sourceSelector;

	private IDialogSettings settings;
	
	private File source;
	
	public UpdateBankDialog(Shell shell)
	{
		super(shell);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		this.setTitle("Banken aktualisieren");
		this.setMessage("Wählen Sie eine Importdatei zum Aktualisieren der Bankendaten aus.");
		
		this.settings = Activator.getDefault().getDialogSettings().getSection("update.bank.dialog");
		if (this.settings == null)
			this.settings = Activator.getDefault().getDialogSettings().addNewSection("update.bank.dialog");
		if (this.settings.get("source.path") == null)
			this.settings.put("source.path", "");

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Quelle");

		source = new File(this.settings.get("source.path"));
		
		sourcePath = new Text(composite, SWT.BORDER);
		sourcePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sourcePath.setText(source.getAbsolutePath());
		sourcePath.addModifyListener(new ModifyListener() 
		{
			@Override
			public void modifyText(ModifyEvent e) 
			{
				getButton(IDialogConstants.OK_ID).setEnabled(source.isFile());
			}
		});
		
		sourceSelector = new Button(composite, SWT.PUSH);
		sourceSelector.setLayoutData(new GridData());
		sourceSelector.setText("...");
		sourceSelector.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				FileDialog dialog = new FileDialog(getShell());
				dialog.setFilterExtensions(FILTERS);
				dialog.setFileName(source.isFile() ? source.getAbsolutePath() : System.getProperty("user.home"));
				String result = dialog.open();
				if (result != null)
				{
					sourcePath.setText(result);
					source = new File(result);
					if (source.isFile())
					{
						settings.put("source.path", result);
					}
					getButton(IDialogConstants.OK_ID).setEnabled(source.isFile());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});
		
		connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class, null);
		connectionServiceTracker.open();
		
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, "Abgleichen", false);
		createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", true);
		getButton(IDialogConstants.OK_ID).setEnabled(source.isFile());
	}

	public boolean close()
	{
		connectionServiceTracker.close();
		return super.close();
	}
	
	@Override
	protected void okPressed() 
	{
		ConnectionService service = (ConnectionService) connectionServiceTracker.getService();
		if (service != null)
		{
			if (source.getName().endsWith(".xls") || source.getName().endsWith(".xlsx"))
			{
			}
			else
			{
			    ProgressMonitorDialog dialog = new ProgressMonitorDialog(this.getShell());
			    try 
			    {
					dialog.run(true, true, new AsciiFileReader(source, service));
				} 
			    catch (InvocationTargetException e) 
			    {
			    	MessageDialog.openError(this.getShell(), "Fehler", "Es ist ein Fehler aufgetreten: " + e.getLocalizedMessage());
				} 
			    catch (InterruptedException e) 
			    {
			    	MessageDialog.openInformation(this.getShell(), "Abbruch", "Die Aktualisierung wurde durch den Benutzer abgebrochen. Die Aktualisierung wurde daher nicht vollständig durchgeführt.");
				}
			}
		}
		super.okPressed();
	}
	
	private class AsciiFileReader implements IRunnableWithProgress
	{
		private ConnectionService service;
		
		public AsciiFileReader(File file, ConnectionService service)
		{
			this.service = service;
		}

		@Override
		public void run(IProgressMonitor monitor)
				throws InvocationTargetException,
				InterruptedException 
		{
			int row = 0;
			try
			{
				BankQuery query = (BankQuery) service.getQuery(Bank.class);
				InputStream is = new FileInputStream(source);
				int rows = query.countBankListEntries(is);
				is = new FileInputStream(source);
				query.updateBankList(monitor, is, rows);
				monitor.worked(1);
			} 
			catch (Exception e) 
			{
				MessageDialog.openError(UpdateBankDialog.this.getShell(), "Fehler", "Fehler in Zeile " + row + ": " + e.getLocalizedMessage());
			}
			finally
			{
		        monitor.done();
			}
		}
	}
}
