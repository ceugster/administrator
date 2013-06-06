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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.export.JRExportProgressMonitor;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import ch.eugster.events.report.Activator;
import ch.eugster.events.report.internal.viewer.IReportViewer;

/**
 * HTML export action
 * 
 * @author Peter Severin (peter_p_s@users.sourceforge.net)
 */
public class ExportAsHtmlAction extends AbstractExportAction
{
	/**
	 * @see AbstractExportAction#AbstractExportAction(IReportViewer)
	 */
	public ExportAsHtmlAction(final IReportViewer viewer)
	{
		super(viewer);

		setText("Export als HTML"); //$NON-NLS-1$
		setToolTipText("Export als HTML"); //$NON-NLS-1$
		setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("save"));
		setDisabledImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("saved"));

		setFileExtensions(new String[] { "*.html", "*.htm" }); //$NON-NLS-1$ //$NON-NLS-2$
		setFilterNames(new String[] { "HTML (*.html)", "HTML (*.htm)" }); //$NON-NLS-1$ //$NON-NLS-2$
		setDefaultFileExtension("html"); //$NON-NLS-1$
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.actions.AbstractExportAction#exportWithProgress(java.io.File,
	 *      net.sf.jasperreports.engine.export.JRExportProgressMonitor)
	 */
	@Override
	protected void exportWithProgress(final File file, final JRExportProgressMonitor monitor) throws JRException
	{
		JRHtmlExporter exporter = new JRHtmlExporter();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, getReportViewer().getDocument());
		exporter.setParameter(JRExporterParameter.OUTPUT_FILE, file);
		exporter.setParameter(JRExporterParameter.PROGRESS_MONITOR, monitor);
		exporter.exportReport();
	}
}
