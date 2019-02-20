package ch.eugster.events.visits;

import java.util.Collection;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.osgi.service.component.ComponentContext;

import ch.eugster.events.persistence.model.EmailAccount;
import ch.eugster.events.persistence.queries.EmailAccountQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.visits.service.RegistrationService;

public class RegistrationServiceComponent implements RegistrationService
{
	private ConnectionService connectionService;

//	private VisitSettings settings;

	protected void activate(final ComponentContext context)
	{

	}

	protected void deactivate(final ComponentContext context)
	{

	}

	@Override
	public void getRegistrations()
	{
		EmailAccountQuery query = (EmailAccountQuery) connectionService.getQuery(EmailAccount.class);
		Collection<EmailAccount> accounts = query.selectAll(EmailAccount.class);
		for (EmailAccount account : accounts)
		{
			String host = account.getHost();
			String username = account.getUsername();
			String password = account.getPassword();

			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);

			Store store = null;
			try
			{
				store = session.getStore(account.getType().storeKey());
				store.connect(host, username, password);
				Folder folder = store.getFolder("INBOX");
				folder.open(Folder.READ_ONLY);
				Message message[] = folder.getMessages();
				for (int i = 0, n = message.length; i < n; i++)
				{
					System.out.println(i + ": " + message[i].getFrom()[0] + "\t" + message[i].getSubject());
				}
				folder.close(false);
				store.close();
			}
			catch (NoSuchProviderException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (MessagingException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
			}
		}
	}

	protected void setConnectionService(final ConnectionService service)
	{
		this.connectionService = service;
	}

	protected void unsetConnectionService(final ConnectionService service)
	{
		this.connectionService = null;
	}

}
