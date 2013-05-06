package ch.eugster.events.report.internal.engine;

import java.io.File;
import java.io.InputStream;
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
import org.eclipse.ui.progress.UIJob;

import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.internal.viewer.ViewerApp;

public class ReportServiceComponent implements ReportService
{

	@Override
	public void export(final InputStream report, final Comparable[] beanArray, final Map<String, Object> parameters,
			final Format format, final File file) throws IllegalArgumentException
	{
		try
		{
			Arrays.sort(beanArray);
			JRDataSource dataSource = new JRBeanArrayDataSource(beanArray);
			JasperReport jasperReport = JasperCompileManager.compileReport(report);
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

	@Override
	public void print(final InputStream report, final Comparable[] beanArray, final Map<String, Object> parameters,
			final boolean doNotShowPrintDialog) throws IllegalArgumentException
	{
		try
		{
			Arrays.sort(beanArray);
			JRDataSource dataSource = new JRBeanArrayDataSource(beanArray);
			JasperReport jasperReport = JasperCompileManager.compileReport(report);
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
			JasperPrintManager.printReport(jasperPrint, doNotShowPrintDialog);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void view(final InputStream report, final Comparable[] beanArray, final Map<String, Object> parameters)
			throws IllegalArgumentException
	{
		try
		{
			Arrays.sort(beanArray);
			JRDataSource dataSource = new JRBeanArrayDataSource(beanArray);
			JasperReport jasperReport = JasperCompileManager.compileReport(report);
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
}
