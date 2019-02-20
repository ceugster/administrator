package ch.eugster.events.zipcode.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.GregorianCalendar;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.queries.ZipCodeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;
import ch.eugster.events.zipcode.Activator;
import ch.eugster.events.zipcode.views.ZipCodeView;

public class UpdateZipCodeHandler extends ConnectionServiceDependentAbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		EvaluationContext ctx = (EvaluationContext) event.getApplicationContext();
		Shell shell = (Shell) ctx.getVariable("activeShell");
		IWorkbenchPart part = (IWorkbenchPart)ctx.getVariable("activePart");
		if (connectionService != null)
		{
			String path = getPath(shell);
			if (path != null)
			{
				File file = new File(path);
				if (file.isFile())
				{
					ZipCodeView view = null;
					if (part instanceof ZipCodeView)
					{
						view = (ZipCodeView) part;
					}
					readFile(file, connectionService, view, shell);
				}
			}
		}
		return Status.OK_STATUS;
	}

	private String getPath(Shell shell)
	{
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		String [] filterNames = new String [] {"Text-Dateien", "Alle Textdateien (*)"};
		String [] filterExtensions = new String [] {"*.csv;*.txt", "*"};
		String filterPath = "";
		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) 
		{
			filterNames = new String [] {"Image Files", "All Files (*.*)"};
			filterExtensions = new String [] {"*.csv;*.txt", "*.*"};
			filterPath = "C:\\";
		}
		dialog.setFilterNames (filterNames);
		dialog.setFilterExtensions (filterExtensions);
		dialog.setFilterPath (filterPath);
		dialog.setFileName ("");
		return dialog.open();
	}
	
	private IStatus readFile(final File file, final ConnectionService service, final ZipCodeView view, final Shell shell)
	{
		try
		{
			EntityMediator.removeListener(ZipCode.class, view);
			view.getViewer().getTable().setEnabled(false);
			final CountryQuery countryQuery = (CountryQuery) service.getQuery(Country.class);
			final Country country = countryQuery.findByIso3166Alpha2Code("CH");
			
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
			dialog.run(true, true, new IRunnableWithProgress() 
			{
				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException
				{
					BufferedReader reader = null;
					try
					{
						int rows = 0;
						reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
						String line = reader.readLine();
						while (line != null)
						{
							rows++;
							line = reader.readLine();
							if (monitor.isCanceled())
							{
								return;
							}
						}
						reader.close();
						
						monitor.beginTask("Postleitzahlen werden aktualisiert...", rows);
						reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
						line = reader.readLine();
						while (line != null)
						{
							if (monitor.isCanceled())
							{
								return;
							}
							String[] cols = line.split("\t");
							if (cols.length == 7)
							{
								ZipCode zipCodeRead = ZipCode.newInstance();
								zipCodeRead.setId(Long.valueOf(cols[0].trim()).longValue());
								zipCodeRead.setType(Integer.valueOf(cols[1].trim()).intValue());
								zipCodeRead.setZip(cols[2].trim());
								zipCodeRead.setPostfix(Integer.valueOf(cols[3].trim()).intValue());
								zipCodeRead.setCity(cols[5].trim());
								zipCodeRead.setState(cols[6].trim());
								ZipCodeQuery query = (ZipCodeQuery) service.getQuery(ZipCode.class);
								ZipCode zipCode = query.find(ZipCode.class, zipCodeRead.getId());
								if (zipCode == null)
								{
									zipCode = ZipCode.newInstance();
								}
								zipCode.setInserted(zipCode.getId() == null ? GregorianCalendar.getInstance() : zipCode.getInserted());
								zipCode.setCity(zipCodeRead.getCity());
								zipCode.setCountry(country);
								zipCode.setDeleted(false);
								zipCode.setId(zipCodeRead.getId());
								zipCode.setUpdated(GregorianCalendar.getInstance());
								zipCode.setPostfix(zipCodeRead.getPostfix());
								zipCode.setState(zipCodeRead.getState());
								zipCode.setType(zipCodeRead.getType());
								zipCode.setUser(User.getCurrent());
								zipCode.setZip(zipCodeRead.getZip());
								zipCode = (ZipCode) query.merge(zipCode);
								System.out.println(zipCode.getZip());
							}
							monitor.worked(1);
							line = reader.readLine();
						}
					} 
					catch (Exception e)
					{
						throw new InvocationTargetException(e);
					}
					finally
					{
						monitor.done();
						if (reader != null)
						{
							try
							{
								reader.close();
							}
							catch (Exception e)
							{
								throw new InvocationTargetException(e);
							}
						}
					}
				}
			});
		}
		catch (InvocationTargetException e)
		{
			return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Fehler beim Aktualisieren der Postleitzahlen", e);
		} 
		catch (InterruptedException e) 
		{
		}
		finally
		{
			EntityMediator.addListener(ZipCode.class, view);
			view.getViewer().getTable().setEnabled(true);
			view.getViewer().refresh();
		}
		return Status.OK_STATUS;
	}
}
