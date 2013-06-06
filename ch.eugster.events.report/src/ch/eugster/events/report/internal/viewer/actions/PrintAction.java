/*
 * SWTJasperViewer - Free SWT/JFace report viewer for JasperReports.
 * Copyright (C) 2004  Peter Severin (peter_p_s@users.sourceforge.net)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 */
package ch.eugster.events.report.internal.viewer.actions;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Random;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.export.JRPdfExporter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import ch.eugster.events.report.Activator;
import ch.eugster.events.report.internal.viewer.IReportViewer;

/**
 * Print action.
 * 
 * @author Peter Severin (peter_p_s@users.sourceforge.net)
 * @author Cyrill Ruettimann
 */
public class PrintAction extends AbstractReportViewerAction
{
	private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));

	private static final String PDF_EXTENSION = ".pdf";

	/**
	 * @see AbstractReportViewerAction#AbstractReportViewerAction(IReportViewer)
	 */
	public PrintAction(final IReportViewer viewer)
	{
		super(viewer);

		setText("Drucken..."); //$NON-NLS-1$
		setToolTipText("Drucken"); //$NON-NLS-1$
		setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("print"));
		setDisabledImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("printd"));
	}

	/**
	 * @see com.jasperassistant.designer.viewer.actions.AbstractReportViewerAction#calculateEnabled()
	 */
	@Override
	protected boolean calculateEnabled()
	{
		return getReportViewer().hasDocument();
	}

	/**
	 * Exports the current document to a PDF.
	 * 
	 * @param file
	 *            The file to export the PDF to.
	 * @throws JRException
	 *             if there are problems exporting the Report to a PDF.
	 * 
	 * @since 03.05.2005
	 */
	private void exportAsPDF(final File file) throws JRException
	{
		JRPdfExporter exporter = new JRPdfExporter();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, getReportViewer().getDocument());
		exporter.setParameter(JRExporterParameter.OUTPUT_FILE, file);
		exporter.exportReport();
	}

	/**
	 * Opens the PDF with the default registered application.
	 * 
	 * @param file
	 *            The PDF to open.
	 * @throws IOException
	 *             If the PDF can not be found.
	 * 
	 * @since 03.05.2005
	 */
	private void openPDF(final File file) throws IOException
	{
		final Runtime runtime = Runtime.getRuntime();
		runtime.exec("open " + file);
	}

	/**
	 * @see com.jasperassistant.designer.viewer.actions.AbstractReportViewerAction#run()
	 */
	@Override
	public void run()
	{
		final Display display = Display.getCurrent();
		display.asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if ("carbon".equals(SWT.getPlatform()))
					{
						Random random = new Random();
						int integer = random.nextInt();
						final String reportName = getReportViewer().getDocument().getName();
						final String fileName = reportName + integer + PDF_EXTENSION;
						final File file = new File(TEMP_DIR, fileName);
						file.deleteOnExit();
						exportAsPDF(file);
						openPDF(file);
					}
					else
					{
						JasperPrintManager.printReport(getReportViewer().getDocument(), true);
					}
				}
				catch (Throwable e)
				{
					e.printStackTrace();
					MessageDialog.openError(display.getActiveShell(),
							"Fehler beim Drucken", //$NON-NLS-1$
							MessageFormat.format(
									"Dokument konnte nicht gedruckt werden: {0}", new Object[] { e.getMessage() })); //$NON-NLS-1$
				}
			}
		});
	}

}