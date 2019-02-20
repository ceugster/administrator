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

import java.awt.Graphics;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.standard.PrinterName;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRGraphics2DExporter;
import net.sf.jasperreports.engine.export.JRGraphics2DExporterParameter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;

import ch.eugster.events.report.Activator;
import ch.eugster.events.report.internal.viewer.IReportViewer;

/**
 * Print action. Unlike the
 * {@link ch.eugster.events.report.internal.viewer.actions.PrintAction}which
 * uses the AWT print dialog, this action uses a native SWT print dialog.
 * <p>
 * Note that the actual printing is performed using the standard Java printing
 * support and not the one provided by SWT. The downside of this is that not all
 * of the printing settings, made in the PrintDialog, can be taken in account.
 * The settings taken in account are:
 * <li>The printer
 * <li>Page selection
 * <li>The number of copies
 * <p>
 * WARNING: this code is still experimental. So far it was only tested on
 * Windows XP.
 * 
 * @author Peter Severin (peter_p_s@users.sourceforge.net)
 */
public class NativePrintAction extends AbstractReportViewerAction
{
	/**
	 * @see AbstractReportViewerAction#AbstractReportViewerAction(IReportViewer)
	 */
	public NativePrintAction(final IReportViewer viewer)
	{
		super(viewer);

		setText("Drucken..."); //$NON-NLS-1$
		setToolTipText("Drucken"); //$NON-NLS-1$
		setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("print"));
		setDisabledImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("printd"));
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.actions.AbstractReportViewerAction#calculateEnabled()
	 */
	@Override
	protected boolean calculateEnabled()
	{
		return getReportViewer().hasDocument();
	}

	private PrintService lookupPrintService(final PrinterData data) throws PrinterException
	{
		final String printerName = data.name;
		final HashAttributeSet attrSet = new HashAttributeSet();
		attrSet.add(new PrinterName(printerName, null));

		final PrintService[] services = PrintServiceLookup.lookupPrintServices(null, attrSet);
		if (services == null || services.length == 0)
			throw new PrinterException("Failed to lookup service by printer name: " + printerName);

		return services[0];
	}

	private void printDocument(final Display display, final PrinterData printData) throws PrinterException
	{
		final JasperPrint document = getReportViewer().getDocument();
		final int startPage, endPage;

		if (printData.scope == PrinterData.ALL_PAGES)
		{
			startPage = 0;
			endPage = document.getPages().size() - 1;
		}
		else if (printData.scope == PrinterData.PAGE_RANGE)
		{
			startPage = printData.startPage - 1;
			endPage = printData.endPage - 1;
		}
		else
		{
			startPage = endPage = getReportViewer().getPageIndex();
		}

		final int pageCount = endPage - startPage + 1;

		final PrintService service = lookupPrintService(printData);

		final PrinterJob printJob = PrinterJob.getPrinterJob();
		final PageFormat pageFormat = printJob.defaultPage();
		final Paper paper = pageFormat.getPaper();

		printJob.setJobName("JasperReports - " + document.getName());

		switch (document.getOrientationValue())
		{
			case LANDSCAPE:
			{
				pageFormat.setOrientation(PageFormat.LANDSCAPE);
				paper.setSize(document.getPageHeight(), document.getPageWidth());
				paper.setImageableArea(0, 0, document.getPageHeight(), document.getPageWidth());
				break;
			}
			case PORTRAIT:
			default:
			{
				pageFormat.setOrientation(PageFormat.PORTRAIT);
				paper.setSize(document.getPageWidth(), document.getPageHeight());
				paper.setImageableArea(0, 0, document.getPageWidth(), document.getPageHeight());
			}
		}

		pageFormat.setPaper(paper);

		final Book book = new Book();
		book.append(new PrintableDocument(document, startPage), pageFormat, pageCount);
		printJob.setPageable(book);

		printJob.setCopies(printData.copyCount);
		printJob.setPrintService(service);

		printJob.print();
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.actions.AbstractReportViewerAction#run()
	 */
	@Override
	public void run()
	{
		final Display display = Display.getCurrent();
		final PrintDialog dialog = new PrintDialog(display.getActiveShell());
		dialog.setStartPage(1);
		dialog.setEndPage(getReportViewer().getDocument().getPages().size());
		final PrinterData printData = dialog.open();

		if (printData != null)
		{
			try
			{
				printDocument(display, printData);
			}
			catch (PrinterException e)
			{
				e.printStackTrace();
				MessageDialog.openError(display.getActiveShell(),
						"Fehler beim Drucken", //$NON-NLS-1$
						MessageFormat.format(
								"Dokument konnte nicht gedruckt werden: {0}", new Object[] { e.getMessage() })); //$NON-NLS-1$
			}
		}
	}

	private static class PrintableDocument implements Printable
	{
		private final JasperPrint document;

		private final int startPage;

		public PrintableDocument(final JasperPrint document, final int startPage)
		{
			this.document = document;
			this.startPage = startPage;
		}

		@Override
		public int print(final Graphics graphics, final PageFormat pageFormat, int pageIndex) throws PrinterException
		{
			if (Thread.currentThread().isInterrupted())
			{
				throw new PrinterException("Current thread interrupted.");
			}

			pageIndex += startPage;

			if (pageIndex < 0 || pageIndex >= document.getPages().size())
			{
				return Printable.NO_SUCH_PAGE;
			}

			try
			{
				JRGraphics2DExporter exporter = new JRGraphics2DExporter();
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, document);
				exporter.setParameter(JRGraphics2DExporterParameter.GRAPHICS_2D, graphics);
				exporter.setParameter(JRExporterParameter.PAGE_INDEX, new Integer(pageIndex));
				exporter.exportReport();
			}
			catch (JRException e)
			{
				throw new PrinterException(e.getMessage());
			}

			return Printable.PAGE_EXISTS;
		}
	}
}