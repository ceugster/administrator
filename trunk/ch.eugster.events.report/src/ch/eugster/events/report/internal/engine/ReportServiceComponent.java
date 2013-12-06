package ch.eugster.events.report.internal.engine;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

import ch.eugster.events.report.dialogs.LabelSelectionDialog;
import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.internal.viewer.ViewerApp;

public class ReportServiceComponent implements ReportService
{

	@Override
	public void export(final URL report, final Comparable<?>[] beanArray, final Map<String, Object> parameters,
			final Format format, final File file) throws IllegalArgumentException
	{
		if (report != null)
		{
			try
			{
				Arrays.sort(beanArray);
				JRDataSource dataSource = new JRBeanArrayDataSource(beanArray);
				JasperReport jasperReport = JasperCompileManager.compileReport(report.openStream());
				final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
				switch (format)
				{
					case PDF:
					{
						JasperExportManager.exportReportToPdfFile(jasperPrint, file.getAbsolutePath());
					}
					case HTML:
					{
						JasperExportManager.exportReportToHtmlFile(jasperPrint, file.getAbsolutePath());
					}
					case XML:
					{
						JasperExportManager.exportReportToXmlFile(jasperPrint, file.getAbsolutePath(), true);
					}
				}
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException(e);
			}
		}
		else
		{
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					MessageDialog.openWarning(null, "Ung�ltiger Report", "Der gew�hlte Report ist ung�ltig.");
				}
			});
		}
	}

	@Override
	public void print(final URL report, final Comparable<?>[] beanArray, final Map<String, Object> parameters,
			final boolean doNotShowPrintDialog) throws IllegalArgumentException
	{
		if (report != null)
		{
			try
			{
				Arrays.sort(beanArray);
				JRDataSource dataSource = new JRBeanArrayDataSource(beanArray);
				JasperReport jasperReport = JasperCompileManager.compileReport(report.openStream());
				JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
				JasperPrintManager.printReport(jasperPrint, doNotShowPrintDialog);
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException(e);
			}
		}
		else
		{
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					MessageDialog.openWarning(null, "Ung�ltiger Report", "Der gew�hlte Report ist ung�ltig.");
				}
			});
		}
	}

	@Override
	public void processLabels(final Comparable<?>[] beanArray, final Map<String, Object> parameters,
			final Destination[] destinations)
	{
		Display.getDefault().syncExec(new Runnable()
		{

			@Override
			public void run()
			{
				LabelSelectionDialog dialog = new LabelSelectionDialog(new Shell());
				if (dialog.open() == Dialog.OK)
				{
					Destination destination = dialog.getDestination();
					URL report = dialog.getReport();
					if (report != null)
					{
						switch (destination)
						{
							case PREVIEW:
							{
								ReportServiceComponent.this.view(report, beanArray, parameters);
								break;
							}
							case PRINTER:
							{
								ReportServiceComponent.this.print(report, beanArray, parameters, false);
								break;
							}
						}
					}
					else
					{
						MessageDialog.openWarning(null, "Ung�ltiger Report", "Der gew�hlte Report ist ung�ltig.");
					}
				}
			}
		});
	}

	@Override
	public void view(final URL report, final Comparable<?>[] beanArray, final Map<String, Object> parameters)
			throws IllegalArgumentException
	{
		if (report != null)
		{
			try
			{
				Arrays.sort(beanArray);
				JRDataSource dataSource = new JRBeanArrayDataSource(beanArray);
				JasperReport jasperReport = JasperCompileManager.compileReport(report.openStream());
				final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

				UIJob job = new UIJob("Preview")
				{
					@Override
					public IStatus runInUIThread(final IProgressMonitor monitor)
					{
						ViewerApp app = new ViewerApp();
						app.getReportViewer().setDocument(jasperPrint);
						app.open();
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.schedule();
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException(e);
			}
		}
		else
		{
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					MessageDialog.openWarning(null, "Ung�ltiger Report", "Der gew�hlte Report ist ung�ltig.");
				}
			});
		}
	}
}
