package ch.eugster.events.charity.preferences;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.charity.Activator;

import com.impinj.octane.ImpinjReader;

public class CharityPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	private StringFieldEditor readerEditor;

	private IntegerFieldEditor messageTimeoutEditor;

	private Button check;

	private String  readerAddress;
	
	private int timeout;
	
	private boolean success;
	
	public CharityPreferencePage()
	{
		this(GRID);
	}

	public CharityPreferencePage(int style)
	{
		super(style);
	}

	public CharityPreferencePage(String title, int style)
	{
		super(title, style);
	}

	public CharityPreferencePage(String title, ImageDescriptor image, int style)
	{
		super(title, image, style);
	}

	@Override
	protected void createFieldEditors()
	{
		this.readerEditor = new StringFieldEditor(PreferenceInitializer.KEY_READER_ADDRESS, "Tag Leser Adresse", this.getFieldEditorParent());
		this.addField(readerEditor);
		this.messageTimeoutEditor = new IntegerFieldEditor(PreferenceInitializer.KEY_REACHABLE_TIMEOUT, "Timeout Erreichbarkeit", this.getFieldEditorParent());
		this.addField(messageTimeoutEditor);
		
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		this.check = new Button(this.getFieldEditorParent(), SWT.PUSH);
		this.check.setLayoutData(gridData);
		this.check.setText(readerEditor.getStringValue().isEmpty() ? "Suchen" : "Testen");
		this.check.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				timeout = messageTimeoutEditor.getIntValue();
				String readerAddress = readerEditor.getStringValue();
				if (readerAddress.isEmpty())
				{
					findReader();
				}
				else
				{
					checkReader();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				this.widgetSelected(e);
			}
		});
		IntegerFieldEditor timeBetweenEditor = new IntegerFieldEditor(PreferenceInitializer.KEY_TIME_BETWEEN_READS, "Mindestzeit zwischen zwei Lesungen", this.getFieldEditorParent());
		this.addField(timeBetweenEditor);

	}

	@Override
	public void init(IWorkbench workbench)
	{
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		this.setPreferenceStore(store);
		this.setDescription("Sponsorlauf Tag Leser");
	}

	private void findReader()
	{
	    try 
	    {
	        IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress()
	        {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException 
				{
					try
					{
						monitor.beginTask("Das lokale Netzwerk wird auf Tag Leser durchsucht...", 254);
						InetAddress localAddress = InetAddress.getLocalHost();
						byte[] ip = localAddress.getAddress();
						for (int i = 1; i < 255; i++)
						{
							if (monitor.isCanceled())
							{
								throw new InterruptedException();
							}
							try
							{
								final InetAddress address = InetAddress.getByAddress(new byte[] { ip[0], ip[1], ip[2], Integer.valueOf(i).byteValue() });
								if (!address.getHostAddress().equals(address.getHostName()))
								{
									if (address.isReachable(timeout))
									{
										checkConnection(monitor);
										CharityPreferencePage.this.getShell().getDisplay().asyncExec(new Runnable() 
										{
											@Override
											public void run() 
											{
												readerEditor.setStringValue(address.getHostName());
												check.setText("Testen");
											}
										});
										return;
									}
								}
							}
							catch (IOException e1) 
							{
								e1.printStackTrace();
							}
							finally
							{
							}
							monitor.worked(1);
						}
					}
					catch (UnknownHostException uhe)
					{
						
					}
					finally
					{
						monitor.done();
					}
				}
			};
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(CharityPreferencePage.this.getShell());
	        dialog.run(true, true, runnableWithProgress);
	    } 
	    catch (InvocationTargetException ite) 
	    {
	    } 
	    catch (InterruptedException ie) 
	    {
	    }
	}

	private void checkReader()
	{
		this.success = false;
		this.readerAddress = this.readerEditor.getStringValue();
	    try 
	    {
	        IRunnableWithProgress op = new IRunnableWithProgress()
	        {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException 
				{
					checkConnection(monitor);
				}
			};
	        new ProgressMonitorDialog(CharityPreferencePage.this.getShell()).run(true, false, op);
	    } 
	    catch (InvocationTargetException ite) 
	    {
	    } 
	    catch (InterruptedException ie) 
	    {
	    }
	    finally
	    {
	    	if (success)
	    	{
		    	String title = "Verbindung hergestellt";
		    	String message = "Die Verbindung zum Tag Leser " + readerAddress + " wurde erfolgreich hergestellt.";
				MessageDialog.openConfirm(CharityPreferencePage.this.getShell(), title, message);
	    	}
	    	else
	    	{
		    	String title = "Verbindung nicht hergestellt";
		    	String message = "Die Verbindung zum Tag Leser " + readerAddress + " konnte nicht hergestellt werden.";
				MessageDialog.openConfirm(CharityPreferencePage.this.getShell(), title, message);
	    	}
	    }
	}

	private void checkConnection(IProgressMonitor monitor)
	{
		ImpinjReader reader = new ImpinjReader();
		try
		{
			monitor.beginTask("Es wird versucht, eine Verbindung mit dem angegebenen Tag Leser herzustellen...", 1);
			reader.setMessageTimeout(timeout);
			reader.connect(readerAddress);
			reader.disconnect();
			success = true;
			monitor.worked(1);
		} 
		catch (Exception e) 
		{
			success = false;
		}
		finally
		{
			monitor.done();
		}
	}
}
