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

import net.sf.jasperreports.engine.util.JRSaver;

import org.eclipse.swt.custom.BusyIndicator;

import ch.eugster.events.report.Activator;
import ch.eugster.events.report.internal.viewer.IReportViewer;

/**
 * JasperReports export action
 * 
 * @author Peter Severin (peter_p_s@users.sourceforge.net)
 */
public class ExportAsJasperReportsAction extends AbstractExportAction
{
	/**
	 * @see AbstractExportAction#AbstractExportAction(IReportViewer)
	 */
	public ExportAsJasperReportsAction(final IReportViewer viewer)
	{
		super(viewer);

		setText("Export als JasperReports"); //$NON-NLS-1$
		setToolTipText("Export als JasperReports"); //$NON-NLS-1$
		setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("save"));
		setDisabledImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("saved"));

		setFileExtensions(new String[] { "*.jrprint" }); //$NON-NLS-1$
		setFilterNames(new String[] { "JasperReports (*.jrprint)" }); //$NON-NLS-1$
		setDefaultFileExtension("jrprint"); //$NON-NLS-1$
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.actions.AbstractExportAction#export(java.io.File)
	 */
	@Override
	protected void export(final File file) throws Throwable
	{
		final Throwable[] ex = new Throwable[1];
		BusyIndicator.showWhile(null, new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					JRSaver.saveObject(getReportViewer().getDocument(), file);
				}
				catch (Throwable e)
				{
					ex[0] = e;
				}
			}
		});
		if (ex[0] != null)
			throw ex[0];
	}
}
