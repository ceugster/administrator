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

import ch.eugster.events.report.Activator;
import ch.eugster.events.report.internal.viewer.IReportViewer;
import ch.eugster.events.report.internal.viewer.ReportViewerEvent;

/**
 * Zoom to actual document size action
 * 
 * @author Peter Severin (peter_p_s@users.sourceforge.net)
 */
public class ZoomActualSizeAction extends AbstractReportViewerAction
{
	/**
	 * @see AbstractReportViewerAction#AbstractReportViewerAction(IReportViewer)
	 */
	public ZoomActualSizeAction(final IReportViewer viewer)
	{
		super(viewer);

		setText("Aktuelle Grösse"); //$NON-NLS-1$
		setToolTipText("Aktuelle Grösse"); //$NON-NLS-1$
		setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("zoomactual"));
		setDisabledImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("zoomactuald"));
		update();
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.actions.AbstractReportViewerAction#calculateEnabled()
	 */
	@Override
	protected boolean calculateEnabled()
	{
		return getReportViewer().canChangeZoom();
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.actions.AbstractReportViewerAction#runBusy()
	 */
	@Override
	protected void runBusy()
	{
		getReportViewer().setZoomMode(IReportViewer.ZOOM_MODE_ACTUAL_SIZE);
		update();
	}

	private void update()
	{
		setChecked(getReportViewer().getZoomMode() == IReportViewer.ZOOM_MODE_ACTUAL_SIZE);
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.actions.AbstractReportViewerAction#viewerStateChanged(ch.eugster.events.report.internal.viewer.ReportViewerEvent)
	 */
	@Override
	public void viewerStateChanged(final ReportViewerEvent evt)
	{
		update();
		super.viewerStateChanged(evt);
	}
}
