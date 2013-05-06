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
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import net.sf.jasperreports.engine.export.JRExportProgressMonitor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import ch.eugster.events.report.internal.viewer.IReportViewer;

/**
 * Base class for export actions
 * 
 * @author Peter Severin (peter_p_s@users.sourceforge.net)
 */
public abstract class AbstractExportAction extends AbstractReportViewerAction
{

	private String[] filterNames;

	private String[] fileExtensions;

	private String defaultFileExtension;

	private String fileName;

	private String filterPath;

	/**
	 * @see AbstractReportViewerAction#AbstractReportViewerAction(IReportViewer)
	 */
	public AbstractExportAction(final IReportViewer viewer)
	{
		super(viewer);
	}

	/**
	 * @see AbstractExportAction#AbstractExportAction(IReportViewer, int)
	 */
	public AbstractExportAction(final IReportViewer viewer, final int style)
	{
		super(viewer, style);
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
	 * Subclasses can override this method to implement an export method.
	 * Default implementation opens a progress monitor dialog and then calls the
	 * {@link #exportWithProgress(File, JRExportProgressMonitor)}method.
	 * 
	 * @param file
	 *            the destination file
	 * @throws Throwable
	 *             if an error occurs during the export
	 * @see #exportWithProgress(File, JRExportProgressMonitor)
	 */
	protected void export(final File file) throws Throwable
	{
		ProgressMonitorDialog pm = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());

		try
		{
			pm.run(true, true, new IRunnableWithProgress()
			{
				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					try
					{
						int totalPages = getReportViewer().getDocument().getPages().size();
						monitor.beginTask(Messages.getString("AbstractExportAction.taskLabel"), totalPages); //$NON-NLS-1$
						exportWithProgress(file, new ProgressMonitorAdapter(monitor, totalPages));
					}
					catch (Throwable e)
					{
						throw new InvocationTargetException(e);
					}
					finally
					{
						monitor.done();
					}
				}
			});
		}
		catch (InvocationTargetException e)
		{
			if (pm.getReturnCode() != ProgressMonitorDialog.CANCEL)
			{
				throw e;
			}
		}
		catch (InterruptedException e)
		{
			if (pm.getReturnCode() != ProgressMonitorDialog.CANCEL)
			{
				throw e;
			}
		}
		finally
		{
			if (pm.getReturnCode() == ProgressMonitorDialog.CANCEL)
			{
				file.delete();
			}
		}
	}

	/**
	 * Subclasses should override this method to implement a progress monitor
	 * aware export method.
	 * 
	 * @param file
	 *            the destination file
	 * @param monitor
	 *            the progress monitor
	 * @throws Throwable
	 *             if an error occurs during the export
	 */
	protected void exportWithProgress(final File file, final JRExportProgressMonitor monitor) throws Throwable
	{
	}

	public String getDefaultFileExtension()
	{
		return defaultFileExtension;
	}

	public String getFileName()
	{
		return fileName;
	}

	public String getFilterPath()
	{
		return filterPath;
	}

	/**
	 * @see com.jasperassistant.designer.viewer.actions.AbstractReportViewerAction#run()
	 */
	@Override
	public void run()
	{
		FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SINGLE | SWT.SAVE);
		if (filterNames != null)
			dialog.setFilterNames(filterNames);
		if (fileExtensions != null)
			dialog.setFilterExtensions(fileExtensions);
		if (filterPath != null)
			dialog.setFilterPath(filterPath);

		if (fileName != null)
			dialog.setFileName(fileName);
		else
			dialog.setFileName(getReportViewer().getDocument().getName());

		String filePath = dialog.open();
		if (filePath != null)
		{
			if (defaultFileExtension != null && fileExtensions != null)
			{
				String extension = getFileExtension(filePath);

				boolean fix = true;

				if (extension != null)
				{
					int i = 0;
					for (i = 0; i < fileExtensions.length; i++)
					{
						if (fileExtensions[i].endsWith(extension))
						{
							fix = false;
							break;
						}
					}
				}

				if (fix)
				{
					filePath += '.' + defaultFileExtension;
				}
			}

			final File file = new File(filePath);

			try
			{
				export(file);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param defaultFileExtension
	 *            The defaultFileExtension to set.
	 */
	public void setDefaultFileExtension(final String defaultFileExtension)
	{
		this.defaultFileExtension = defaultFileExtension;
	}

	/**
	 * @param fileExtensions
	 *            The fileExtensions to set.
	 */
	public void setFileExtensions(final String[] fileExtensions)
	{
		this.fileExtensions = fileExtensions;
	}

	public void setFileName(final String fileName)
	{
		this.fileName = fileName;
	}

	/**
	 * @param filterNames
	 *            The filterNames to set.
	 */
	public void setFilterNames(final String[] filterNames)
	{
		this.filterNames = filterNames;
	}

	public void setFilterPath(final String filterPath)
	{
		this.filterPath = filterPath;
	}

	private static String getFileExtension(final String fileName)
	{
		if (fileName != null)
		{
			int dotIndex = fileName.lastIndexOf('.');
			if (dotIndex != -1)
				return fileName.substring(dotIndex + 1);
		}

		return null;
	}

}

class ProgressMonitorAdapter implements JRExportProgressMonitor
{

	private IProgressMonitor monitor;

	private int totalPages;

	private int currentPage = 1;

	ProgressMonitorAdapter(final IProgressMonitor monitor, final int totalPages)
	{
		this.monitor = monitor;
		this.totalPages = totalPages;
		updateSubtask();
	}

	/**
	 * @see net.sf.jasperreports.engine.export.JRExportProgressMonitor#afterPageExport()
	 */
	@Override
	public void afterPageExport()
	{
		monitor.worked(1);
		if (++currentPage <= totalPages)
		{
			updateSubtask();
		}
		if (monitor.isCanceled())
		{
			Thread.currentThread().interrupt();
		}
	}

	private void updateSubtask()
	{
		monitor.subTask(MessageFormat.format(Messages.getString("AbstractExportAction.pageMofN"), new Object[] { //$NON-NLS-1$
				new Integer(currentPage), new Integer(totalPages) }));
	}
}
