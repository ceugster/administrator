package ch.eugster.events.addressgroup.report.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.report.Activator;
import ch.eugster.events.addressgroup.report.LabelFactory;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.engine.ReportService.Format;
import ch.eugster.events.ui.helpers.EmailHelper;

public class PrintLabelHandler extends AbstractHandler implements IHandler
{
	protected Shell shell;

	private int buildLabelList(final IStructuredSelection ssel)
	{
		LabelFactory.clear();
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
		return LabelFactory.size();
	}

	protected void execute()
	{
		Job job = new Job("Etiketten werden erstellt...")
		{
			@Override
			protected IStatus run(final IProgressMonitor monitor)
			{
				printLabel();
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
					if (buildLabelList((IStructuredSelection) sel) > 0)
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
				InputStream is = getReport();
				Map<String, Object> parameters = new HashMap<String, Object>();
				reportService.export(is, LabelFactory.getEntries(), parameters, format, file);
				is.close();
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
		if (LabelFactory.addEntry(member))
		{
			LabelFactory.addAddressGroup(member.getAddressGroup());
		}
	}

	private InputStream getReport() throws IOException
	{
		InputStream is = null;
		String path = System.getProperty("user.home") + File.separator
				+ ".administrator/Reports/Etiketten/avery_zweckform_3422.jrxml";
		File report = new File(path);
		if (!report.isFile())
		{
			new File(report.getParent()).mkdirs();
			URL url = Activator.getDefault().getBundle().getEntry("reports/avery_zweckform_3422.jrxml");
			is = url.openStream();
			byte[] buffer = new byte[is.available()];
			OutputStream os = new FileOutputStream(report);
			while (is.read(buffer) > -1)
			{
				os.write(buffer);
			}
			is.close();
			os.flush();
			os.close();
		}

		if (report.isFile())
		{
			is = new FileInputStream(report);
		}
		else
		{
			URL url = Activator.getDefault().getBundle().getEntry("reports/avery_zweckform_3422.jrxml");
			is = url.openStream();
		}
		return is;
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
				InputStream is = getReport();
				Map<String, Object> parameters = new HashMap<String, Object>();
				reportService.view(is, LabelFactory.getEntries(), parameters);
				is.close();
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
				InputStream is = getReport();
				Map<String, Object> parameters = new HashMap<String, Object>();
				reportService.print(is, LabelFactory.getEntries(), parameters, showPrintDialog);
				is.close();
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

	private boolean printLabel()
	{
		return preview();
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		setBaseEnabled(true);
	}
}
