package ch.eugster.events.ui.helpers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

public class EmailHelper
{
	private static EmailHelper generator = null;

	public URI createURI(String address) throws URISyntaxException
	{
		if (!address.startsWith("mailto:"))
			address = "mailto:" + address;
		return new URI(address);
	}

	public boolean isEmailSupported()
	{
		if (Desktop.isDesktopSupported())
			return Desktop.getDesktop().isSupported(Desktop.Action.MAIL);

		return false;
	}

	public boolean isValidAddress(final String address)
	{
		if (address == null || address.isEmpty())
			return false;
		if (address.indexOf("@") < 1 || address.indexOf("@") >= address.length())
			return false;
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

	public void sendEmail(final String address)
	{
		if (address.isEmpty() || !this.isValidAddress(address))
		{
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Ungültige Emailadresse", "Die Emailadresse ist ungültig.");
		}
		else
		{
			try
			{
				String s = "mailto:" + address;
				URI uri = new URI(s);
				Desktop.getDesktop().mail(uri);
			}
			catch (URISyntaxException se)
			{
				MessageDialog
						.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								"Ungültige URI",
								"Eine Emailadresse scheint ungültig zu sein. Bitte überprüfen Sie die fraglichen Emailadressen.");
			}
			catch (IOException e)
			{
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Fehler",
						"Beim Versuch, das Emailprogramm zu initialisieren, ist ein Fehler aufgetreten.");
			}
		}
	}

	public void sendEmail(final String[] addresses)
	{
		try
		{
			Collection<String> emails = new ArrayList<String>();
			for (String address : addresses)
			{
				if (!emails.contains(address))
					emails.add(address);
			}
			StringBuilder s = new StringBuilder();
			for (String email : emails)
				if (s.length() == 0)
					s = s.append("mailto:" + email);
				else
					s = s.append(";" + email);
			URI uri = new URI(s.toString());
			Desktop.getDesktop().mail(uri);
			System.out.println(uri.toString());
		}
		catch (URISyntaxException se)
		{
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Ungültige URI",
					"Eine Emailadresse scheint ungültig zu sein. Bitte überprüfen Sie die fraglichen Emailadressen.");
		}
		catch (IOException e)
		{
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Fehler",
					"Beim Versuch, das Emailprogramm zu initialisieren, ist ein Fehler aufgetreten.");
		}
	}

	public static EmailHelper getInstance()
	{
		if (EmailHelper.generator == null)
			EmailHelper.generator = new EmailHelper();

		return EmailHelper.generator;
	}
}
