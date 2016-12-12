package ch.eugster.events.persistence.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.persistence.queries.AddressGroupQuery;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class UpdateAddressGroupMembersHandler extends AbstractHandler implements IHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext ctx = (EvaluationContext) event.getApplicationContext();
		Shell shell = (Shell) ctx.getVariable("activeShell");
		if (MessageDialog.openConfirm(shell, "Bitte beachten", "Bitte beachten Sie folgendes:\n1. Alle anderen Benutzer müssen das Programm beenden, bevor Sie diese Routine starten\n2. Nach der Ausführung müssen Sie den Administrator ebenfalls neu starten"))
		{
			ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class, null);
			tracker.open();
			try
			{
				final ConnectionService service = (ConnectionService) tracker.getService();
				if (service == null)
				{
					MessageDialog.openInformation(shell, "Keine Verbindung zur Datenbank", "Es konnte keine Verbindung zur Datenbank hergestellt werden.");
				}
				else
				{
					
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
					dialog.run(false, true, new IRunnableWithProgress() 
					{
						@Override
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException 
						{
							DomainQuery dQuery = (DomainQuery) service.getQuery(Domain.class);
							List<Domain> domains = dQuery.selectAll();
							try
							{
								monitor.beginTask("Adressgruppen werden verarbeitet", domains.size());
								for (Domain domain : domains)
								{
									AddressGroupCategoryQuery agcQuery = (AddressGroupCategoryQuery) service.getQuery(AddressGroupCategory.class);
									List<AddressGroupCategory> categories = agcQuery.selectByDomain(domain);
									doAddressGroupCategories(new SubProgressMonitor(monitor, categories.size()), service, categories);
									monitor.worked(1);
								}
							}
							finally
							{
								monitor.done();
							}
						}
					});
				}
			} 
			catch (InvocationTargetException e) 
			{
				e.printStackTrace();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			finally
			{
				tracker.close();
			}
		}
		return Status.OK_STATUS;
	}

	private void doAddressGroupCategories(IProgressMonitor monitor, ConnectionService service, List<AddressGroupCategory> categories)
	{
		try
		{
			monitor.beginTask("Verarbeite Adressgruppenkategorien", categories.size());
			for (AddressGroupCategory category : categories)
			{
				List<AddressGroup> groups = category.getAddressGroups();
				doAddressGroups(new SubProgressMonitor(monitor, groups.size()), service, groups);
				monitor.worked(1);
			}
		}
		finally
		{
			monitor.done();
		}
	}

	private void doAddressGroups(IProgressMonitor monitor, ConnectionService service, List<AddressGroup> addressGroups)
	{
		try
		{
			monitor.beginTask("Verarbeite Adressgruppen", addressGroups.size());
			for (AddressGroup addressGroup : addressGroups)
			{
				List<AddressGroupMember> members = addressGroup.getValidAddressGroupMembers();
				if (doAddressGroupMembers(new SubProgressMonitor(monitor, members.size()), service, members))
				{
					AddressGroupQuery agQuery = (AddressGroupQuery) service.getQuery(AddressGroup.class);
					agQuery.merge(addressGroup);
				}
				monitor.worked(1);
			}
		}
		finally
		{
			monitor.done();
		}
	}

	private boolean doAddressGroupMembers(IProgressMonitor monitor, ConnectionService service, List<AddressGroupMember> members)
	{
		monitor.beginTask("Verarbeite Adressgruppenmitglieder", members.size());
		boolean doMerge = false;
		try
		{
			Map<Long, AddressGroupMember> valids = new HashMap<Long, AddressGroupMember>();
			for (AddressGroupMember member : members)
			{
				if (member.isValidAddressMember())
				{
					if (member.getAddress().getValidLinks().size() > 0)
					{
						LinkPersonAddress link = member.getAddress().getValidLinks().get(0);
						if (member.getAddressGroup().contains(link))
						{
							member.setDeleted(true);
						}
						else
						{
							member.setLink(link);
						}
						doMerge = true;
					}
				}
				else if (member.isValidLinkMember())
				{
					if (valids.containsKey(member.getLink().getId()))
					{
						member.setDeleted(true);
						doMerge = true;
					}
					else
					{
						valids.put(member.getLink().getId(), member);
					}
				}
				monitor.worked(1);
			}
		}
		finally
		{
			monitor.done();
		}
		return doMerge;
	}
}
