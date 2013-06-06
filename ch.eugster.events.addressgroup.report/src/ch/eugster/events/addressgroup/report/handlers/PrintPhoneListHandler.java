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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.report.Activator;
import ch.eugster.events.addressgroup.report.PhoneListFactory;
import ch.eugster.events.addressgroup.report.preferences.PreferenceConstants;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.engine.ReportService.Destination;
import ch.eugster.events.report.engine.ReportService.Format;
import ch.eugster.events.ui.helpers.EmailHelper;

public class PrintPhoneListHandler extends AbstractHandler implements IHandler
{
	protected Shell shell;

	private int buildPhoneList(final IStructuredSelection ssel)
	{
		PhoneListFactory.clear();
		Iterator<?> iterator = ssel.iterator();
		while (iterator.hasNext())
		{
			Object element = iterator.next();
			if (element instanceof AddressGroupCategory)
			{
				this.extract((AddressGroupCategory) element);
			}
			else if (element instanceof AddressGroup)
			{
				this.extract((AddressGroup) element);
			}
			// else if (element instanceof AddressGroupLink)
			// {
			// this.extract(((AddressGroupLink)
			// element).getChild());
			// }
			else if (element instanceof AddressGroupMember)
			{
				this.extract((AddressGroupMember) element);
			}
		}
		return PhoneListFactory.size();
	}

	protected void execute()
	{
		Job job = new Job("Telefonliste wird erstellt...")
		{
			@Override
			protected IStatus run(final IProgressMonitor monitor)
			{
				printPhoneList();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.INTERACTIVE);
		job.setUser(true);
		job.schedule();
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
					if (buildPhoneList((IStructuredSelection) sel) > 0)
					{
						execute();
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
				URL url = Activator.getDefault().getBundle().getEntry("reports/phone_list.jrxml");
				Map<String, Object> parameters = new HashMap<String, Object>();
				reportService.export(url, PhoneListFactory.getEntries(), parameters, format, file);
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

	private void extract(final AddressGroup addressGroup)
	{
		if (!addressGroup.isDeleted())
		{
			Collection<AddressGroupMember> addressGroupMembers = addressGroup.getAddressGroupMembers();
			for (AddressGroupMember addressGroupMember : addressGroupMembers)
			{
				this.extract(addressGroupMember);
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

	private void extract(final AddressGroupCategory category)
	{
		if (!category.isDeleted())
		{
			Collection<AddressGroup> addressGroups = category.getAddressGroups();
			for (AddressGroup addressGroup : addressGroups)
			{
				this.extract(addressGroup);
			}
		}
	}

	private void extract(final AddressGroupMember member)
	{
		if (PhoneListFactory.addEntry(member))
		{
			PhoneListFactory.addAddressGroup(member.getAddressGroup());
		}
	}

	private boolean hasValidEmailAddress(final AddressGroup addressGroup)
	{
		for (AddressGroupMember member : addressGroup.getAddressGroupMembers())
		{
			if (hasValidEmailAddress(member))
			{
				return true;
			}
		}
		return false;
	}

	private boolean hasValidEmailAddress(final AddressGroupCategory category)
	{
		for (AddressGroup addressGroup : category.getAddressGroups())
		{
			if (hasValidEmailAddress(addressGroup))
			{
				return true;
			}
		}
		return false;
	}

	private boolean hasValidEmailAddress(final AddressGroupMember member)
	{
		if (member.getLink() == null)
		{
			if (EmailHelper.getInstance().isValidAddress(member.getAddress().getEmail()))
			{
				return true;
			}
		}
		else
		{
			if (EmailHelper.getInstance().isValidAddress(member.getLink().getPerson().getEmail()))
			{
				return true;
			}
			if (EmailHelper.getInstance().isValidAddress(member.getLink().getEmail()))
			{
				return true;
			}
		}
		return false;
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
				URL url = Activator.getDefault().getBundle().getEntry("reports/phone_list.jrxml");
				Map<String, Object> parameters = new HashMap<String, Object>();
				reportService.view(url, PhoneListFactory.getEntries(), parameters);
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
				URL url = Activator.getDefault().getBundle().getEntry("reports/phone_list.jrxml");
				Map<String, Object> parameters = new HashMap<String, Object>();
				reportService.print(url, PhoneListFactory.getEntries(), parameters, showPrintDialog);
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

	private boolean printPhoneList()
	{
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
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
				dialog.setFileName("Telefonliste");
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
		}
		return false;
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
							AddressGroupCategory category = (AddressGroupCategory) object;
							enabled = hasValidEmailAddress(category);
						}
						else if (object instanceof AddressGroup)
						{
							AddressGroup addressGroup = (AddressGroup) object;
							enabled = hasValidEmailAddress(addressGroup);
						}
						else if (object instanceof AddressGroupMember)
						{
							AddressGroupMember member = (AddressGroupMember) object;
							enabled = hasValidEmailAddress(member);
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
}
