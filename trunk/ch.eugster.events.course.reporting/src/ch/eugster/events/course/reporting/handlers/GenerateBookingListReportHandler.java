package ch.eugster.events.course.reporting.handlers;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.course.reporting.BookingListFactory;
import ch.eugster.events.course.reporting.preferences.PreferenceConstants;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.engine.ReportService.Destination;
import ch.eugster.events.report.engine.ReportService.Format;

public class GenerateBookingListReportHandler extends AbstractHandler implements IHandler
{
	protected Shell shell;

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			ISelection selection = (ISelection) context.getParent().getVariable("selection");
			shell = (Shell) context.getParent().getVariable("activeShell");
			if (selection instanceof IStructuredSelection)
			{
				IStructuredSelection ssel = (IStructuredSelection) selection;
				if (ssel.getFirstElement() instanceof Season)
				{
					Season season = (Season) ssel.getFirstElement();

					BookingListFactory factory = BookingListFactory.create(User.getCurrent(), season);
					if (factory.size() == 0)
					{
						MessageDialog.openInformation(shell, "Keine Kurse vorhanden",
								"In der gewählten Saison sind keine bevorstehenden Kurse vorhanden.");
					}
					else
					{
						printBookingListReport(factory);
					}
				}
			}

		}
		return Status.OK_STATUS;
	}

	private boolean export(final BookingListFactory factory, final Format format, final File file)
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ReportService.class.getName(), null);
		try
		{
			tracker.open();
			ReportService reportService = (ReportService) tracker.getService();
			if (reportService != null)
			{
				URL url = Activator.getDefault().getBundle().getEntry("reports/booking_list.jrxml");
				Map<String, Object> parameters = factory.getParticipantListReportParameters();
				reportService.export(url, factory.getCourses(), parameters, format, file);
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

	private boolean preview(final BookingListFactory factory)
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ReportService.class.getName(), null);
		try
		{
			tracker.open();
			ReportService reportService = (ReportService) tracker.getService();
			if (reportService != null)
			{
				URL url = Activator.getDefault().getBundle().getEntry("reports/booking_list.jrxml");
				Map<String, Object> parameters = factory.getParticipantListReportParameters();
				reportService.view(url, factory.getCourses(), parameters);
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

	private boolean print(final BookingListFactory factory, final boolean showPrintDialog)
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ReportService.class.getName(), null);
		try
		{
			tracker.open();
			ReportService reportService = (ReportService) tracker.getService();
			if (reportService != null)
			{
				URL url = Activator.getDefault().getBundle().getEntry("reports/booking_list.jrxml");
				Map<String, Object> parameters = factory.getParticipantListReportParameters();
				reportService.print(url, factory.getCourses(), parameters, showPrintDialog);
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

	private boolean printBookingListReport(final BookingListFactory factory)
	{
		IEclipsePreferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
		int dest = prefs.getInt(PreferenceConstants.P_DESTINATION, 0);
		Destination destination = Destination.values()[dest];
		switch (destination)
		{
			case PREVIEW:
			{
				return preview(factory);
			}
			case PRINTER:
			{
				boolean useStandardPrinter = prefs.getBoolean(PreferenceConstants.P_USE_STANDARD_PRINTER, false);
				return print(factory, !useStandardPrinter);
			}
			case EXPORT:
			{
				String dir = prefs.get(PreferenceConstants.P_DEFAULT_EXPORT_FILE_DIRECTORY,
						System.getProperty("user.home"));
				Format format = Format.values()[prefs.getInt(PreferenceConstants.P_DEFAULT_FILE_FORMAT,
						Format.PDF.ordinal())];
				FileDialog dialog = new FileDialog(this.shell);
				dialog.setFilterExtensions(Format.extensions());
				dialog.setFilterIndex(format.ordinal());
				dialog.setFilterPath(dir);
				dialog.setText("Dateiname");
				dialog.setFileName("Kursliste mit Buchungsstand");
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
				return export(factory, format, new File(path));
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
				if (ssel.getFirstElement() instanceof Season)
				{
					enabled = true;
				}
			}
		}
		setBaseEnabled(enabled);
	}
}
