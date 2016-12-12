package ch.eugster.events.ui.helpers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

public class BrowseHelper
{
	private static BrowseHelper helper = null;

	public static BrowseHelper getInstance()
	{
		if (BrowseHelper.helper == null)
			BrowseHelper.helper = new BrowseHelper();

		return BrowseHelper.helper;
	}

	public void browse(String address)
	{
		try
		{
			this.browse(this.createURI(address));
		} 
		catch (URISyntaxException e)
		{
			MessageDialog.openError(PlatformUI.
					getWorkbench().
					getActiveWorkbenchWindow().
					getShell(), 
					"Ungültige Emailadresse", 
			"Die Syntax der Emailadresse ist ungültig.");
		}
	}

	public void browse(URI uri)
	{
		if (this.isBrowsingSupported())
		{
			try
			{
				Desktop.getDesktop().browse(uri);
			} 
			catch (IOException e)
			{
				MessageDialog.openError(PlatformUI.
						getWorkbench().
						getActiveWorkbenchWindow().
						getShell(), 
						"Fehler", 
				"Beim Versuch, einen Internetbrowser zu initialisieren, ist ein Fehler aufgetreten.");
			}
		}
	}

	public boolean isBrowsingSupported()
	{
		if (Desktop.isDesktopSupported())
			return Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);

		return false;
	}

	public boolean isValidAddress(String address)
	{
		try
		{
			this.createURI(address);
			return true;
		}
		catch (URISyntaxException e)
		{
			return false;
		}
	}

	public URI createURI(String address) throws URISyntaxException
	{
		if (!address.startsWith("http://")) address = "http://" + address;
		return new URI(address);
	}

}
