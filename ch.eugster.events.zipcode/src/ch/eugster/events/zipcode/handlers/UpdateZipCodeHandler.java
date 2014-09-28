package ch.eugster.events.zipcode.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.GregorianCalendar;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.queries.ZipCodeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.zipcode.Activator;

public class UpdateZipCodeHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		EvaluationContext ctx = (EvaluationContext) event.getApplicationContext();
		Shell shell = (Shell) ctx.getVariable("activeShell");
		
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class.getName(), null);
		try
		{
			tracker.open();
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				String path = getPath(shell);
				if (path != null)
				{
					File file = new File(path);
					if (file.isFile())
					{
						readFile(file, service);
					}
				}
			}
		}
		finally
		{
			tracker.close();
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
	
	private IStatus readFile(File file, ConnectionService service)
	{
		try 
		{
			CountryQuery countryQuery = (CountryQuery) service.getQuery(Country.class);
			Country country = countryQuery.findByIso3166Alpha2Code("CH");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = reader.readLine();
			while (line != null)
			{
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
				}
			}
		} 
		catch (FileNotFoundException e) 
		{
			return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Datei nicht gefunden.", e);
		} 
		catch (IOException e) 
		{
			return new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), "Datei kann nicht gelesen werden.", e);
		}
		return Status.OK_STATUS;
	}
}
