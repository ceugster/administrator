/*
 * Created 13.07.2007
 * (c) Orthagis GmbH
 * All Rights Reserved. This file is subject to license restrictions.
 */
package ch.eugster.events.report.internal.viewer.actions;

import java.io.File;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.export.JRExportProgressMonitor;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import ch.eugster.events.report.Activator;
import ch.eugster.events.report.internal.viewer.IReportViewer;

/**
 * RTF export action
 * 
 * @author d.schier
 */
public class ExportAsRtfAction extends AbstractExportAction
{
	public ExportAsRtfAction(final IReportViewer viewer)
	{
		super(viewer);

		setText("Export als RTF"); //$NON-NLS-1$
		setToolTipText("Export als RTF"); //$NON-NLS-1$
		setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("save"));
		setDisabledImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("saved"));

		setFileExtensions(new String[] { "*.rtf" }); //$NON-NLS-1$
		setFilterNames(new String[] { "RTF (*.rtf)" }); //$NON-NLS-1$
		setDefaultFileExtension("rtf"); //$NON-NLS-1$
	}

	@Override
	protected void exportWithProgress(final File file, final JRExportProgressMonitor monitor) throws JRException
	{
		JRRtfExporter exporter = new JRRtfExporter();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, getReportViewer().getDocument());
		exporter.setParameter(JRExporterParameter.OUTPUT_FILE, file);
		exporter.setParameter(JRExporterParameter.PROGRESS_MONITOR, monitor);
		exporter.exportReport();
	}
}
