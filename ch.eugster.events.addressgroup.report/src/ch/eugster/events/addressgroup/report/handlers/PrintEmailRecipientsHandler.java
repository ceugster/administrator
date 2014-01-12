package ch.eugster.events.addressgroup.report.handlers;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.report.Activator;
import ch.eugster.events.addressgroup.report.RecipientListFactory;
import ch.eugster.events.addressgroup.report.preferences.PreferenceConstants;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.engine.ReportService.Destination;
import ch.eugster.events.report.engine.ReportService.Format;
import ch.eugster.events.ui.helpers.EmailHelper;

public class PrintEmailRecipientsHandler extends AbstractHandler implements IHandler
{
	protected Shell shell;

	public int buildRecipientsList(final IStructuredSelection ssel, Filter filter)
	{
		RecipientListFactory.clear();
		Iterator<?> iterator = ssel.iterator();
		while (iterator.hasNext())
		{
			Object element = iterator.next();
			if (element instanceof AddressGroupCategory)
			{
				this.extract((AddressGroupCategory) element, filter);
			}
			else if (element instanceof AddressGroup)
			{
				this.extract((AddressGroup) element, filter);
			}
			// else if (element instanceof AddressGroupLink)
			// {
			// this.extract(((AddressGroupLink)
			// element).getChild());
			// }
			else if (element instanceof AddressGroupMember)
			{
				this.extract((AddressGroupMember) element, filter);
			}
		}
		return RecipientListFactory.size();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (EmailHelper.getInstance().isEmailSupported())
		{
			if (event.getApplicationContext() instanceof EvaluationContext)
			{
				EvaluationContext context = (EvaluationContext) event.getApplicationContext();
				ISelection sel = (ISelection) context.getParent().getVariable("selection");
				shell = (Shell) context.getParent().getVariable("activeShell");
				if (sel instanceof IStructuredSelection)
				{
					IStructuredSelection ssel = (IStructuredSelection) sel;
					if (!ssel.isEmpty())
					{
						if (buildRecipientsList(ssel, Filter.ALL) > 0)
						{
							printRecipientList();
						}
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	private boolean export(final Format format, final File file)
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ReportService.class.getName(), null);
		try
		{
			tracker.open();
			ReportService reportService = (ReportService) tracker.getService();
			if (reportService != null)
			{
				URL url = Activator.getDefault().getBundle().getEntry("reports/recipient_list.jrxml");
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("addressGroups", RecipientListFactory.getAddressGroupParameter());
				reportService.export(url, RecipientListFactory.getRecipients(), parameters, format, file);
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			tracker.close();
		}
		return false;
	}

	private void extract(final AddressGroup addressGroup, Filter filter)
	{
		if (!addressGroup.isDeleted())
		{
			Collection<AddressGroupMember> addressGroupMembers = addressGroup.getAddressGroupMembers();
			for (AddressGroupMember addressGroupMember : addressGroupMembers)
			{
				this.extract(addressGroupMember, filter);
			}
			// for (AddressGroupLink link : addressGroup.getChildren())
			// {
			// if (!link.isDeleted() && !link.getChild().isDeleted())
			// {
			// extract(link.getChild());
			// }
			// }
		}
	}

	private void extract(final AddressGroupCategory category, Filter filter)
	{
		if (!category.isDeleted())
		{
			Collection<AddressGroup> addressGroups = category.getAddressGroups();
			for (AddressGroup addressGroup : addressGroups)
			{
				this.extract(addressGroup, filter);
			}
		}
	}

	private void extract(final AddressGroupMember member, Filter filter)
	{
		if (!member.isDeleted())
		{
			if ((member.getLink() != null && !member.getLink().isDeleted()) || !member.getAddress().isDeleted())
			{
				boolean added = false;
				if (filter.equals(Filter.ALL))
				{
					added = RecipientListFactory.addRecipient(member);
				}
				else if (filter.equals(Filter.ONLY_WITH_EMAILS))
				{
					added = RecipientListFactory.addRecipientWithEmails(member);

				}
				if (added)
				{
					RecipientListFactory.addAddressGroup(member.getAddressGroup());
				}
			}
		}
	}

	private boolean preview()
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ReportService.class.getName(), null);
		try
		{
			tracker.open();
			ReportService reportService = (ReportService) tracker.getService();
			if (reportService != null)
			{
				URL url = Activator.getDefault().getBundle().getEntry("reports/recipient_list.jrxml");
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("addressGroups", RecipientListFactory.getAddressGroupParameter());
				reportService.view(url, RecipientListFactory.getRecipients(), parameters);
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			tracker.close();
		}
		return false;
	}

	private boolean print(final boolean showPrintDialog)
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ReportService.class.getName(), null);
		try
		{
			tracker.open();
			ReportService reportService = (ReportService) tracker.getService();
			if (reportService != null)
			{
				URL url = Activator.getDefault().getBundle().getEntry("reports/recipient_list.jrxml");
				Map<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("addressGroups", RecipientListFactory.getAddressGroupParameter());
				reportService.print(url, RecipientListFactory.getRecipients(), parameters, showPrintDialog);
				return true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			tracker.close();
		}
		return false;
	}

	public boolean printRecipientList()
	{
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		if (preferenceStore.getBoolean(PreferenceConstants.P_PRINT_RECIPIENT_LIST_AUTOMATICALLY))
		{
			int dest = preferenceStore.getInt(PreferenceConstants.P_DESTINATION);
			Destination destination = Destination.values()[dest];
			switch (destination)
			{
				case PREVIEW:
				{
					return preview();
				}
				case PRINTER:
				{
					boolean useStandardPrinter = preferenceStore.getBoolean(PreferenceConstants.P_USE_STANDARD_PRINTER);
					return print(!useStandardPrinter);
				}
				case EXPORT:
				{
					String dir = preferenceStore.getString(PreferenceConstants.P_DEFAULT_EXPORT_FILE_DIRECTORY);
					Format format = Format.values()[preferenceStore.getInt(PreferenceConstants.P_DEFAULT_FILE_FORMAT)];
					FileDialog dialog = new FileDialog(this.shell);
					dialog.setFilterExtensions(Format.extensions());
					dialog.setFilterIndex(format.ordinal());
					dialog.setFilterPath(dir);
					dialog.setText("Dateiname");
					dialog.setFileName("Empfaengerliste");
					String path = dialog.open();
					if (path == null)
					{
						return false;
					}
					int index = dialog.getFilterIndex();
					if (index > -1)
					{
						format = Format.values()[index];
					}
					if (!path.endsWith(format.extension()))
					{
						path = path + format.extension();
					}
					return export(format, new File(path));
				}
				default:
				{
					return false;
				}
			}
		}
		else
		{
			return true;
		}
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getVariable("selection") instanceof StructuredSelection)
		{
			Object selection = context.getVariable("selection");
			if (selection instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) selection;
				Object[] objects = ssel.toArray();
				{
					for (Object object : objects)
					{
						if (object instanceof AddressGroupCategory)
						{
							enabled = true;
						}
						else if (object instanceof AddressGroup)
						{
							enabled = true;
						}
						else if (object instanceof AddressGroupMember)
						{
							enabled = true;
						}
						if (enabled)
						{
							break;
						}
					}
				}
			}
		}
		setBaseEnabled(enabled);
	}

	public enum Filter
	{
		ALL, ONLY_WITH_EMAILS;
	}
}
