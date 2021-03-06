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

import java.text.MessageFormat;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import ch.eugster.events.report.internal.viewer.IReportViewer;
import ch.eugster.events.report.internal.viewer.IReportViewerListener;
import ch.eugster.events.report.internal.viewer.ReportViewerEvent;

/**
 * Current page contribution item
 * 
 * @author Peter Severin (peter_p_s@users.sourceforge.net)
 */
public class StatusContributionItem extends ContributionItem implements IReportViewerListener
{

	private IReportViewer viewer;

	private Label label;

	private ToolItem toolitem;

	/**
	 * Constructs the action by specifying the report viewer to associate with
	 * the item.
	 * 
	 * @param viewer
	 *            the report viewer
	 */
	public StatusContributionItem(final IReportViewer viewer)
	{
		Assert.isNotNull(viewer);
		this.viewer = viewer;
		this.viewer.addReportViewerListener(this);
		refresh();
	}

	private Control createControl(final Composite parent)
	{
		label = new Label(parent, SWT.CENTER);
		label.setText("88888 of 88888"); //$NON-NLS-1$
		label.pack();

		refresh();
		return label;
	}

	/**
	 * @see org.eclipse.jface.action.ContributionItem#dispose()
	 */
	@Override
	public void dispose()
	{
		viewer.removeReportViewerListener(this);
		label = null;
		viewer = null;
	}

	/**
	 * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public final void fill(final Composite parent)
	{
		createControl(parent);
	}

	/**
	 * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Menu,
	 *      int)
	 */
	@Override
	public final void fill(final Menu parent, final int index)
	{
		Assert.isTrue(false, "Seitennummer kann nicht zu Menu hinzugef?gt werden"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.ToolBar,
	 *      int)
	 */
	@Override
	public void fill(final ToolBar parent, final int index)
	{
		toolitem = new ToolItem(parent, SWT.SEPARATOR, index);
		Control control = createControl(parent);
		toolitem.setWidth(label.getSize().x);
		toolitem.setControl(control);
	}

	private String getText()
	{
		if (viewer == null || !viewer.hasDocument())
		{
			return ""; //$NON-NLS-1$
		}
		else
		{
			return MessageFormat.format("{0} von {1}", //$NON-NLS-1$
					new Object[] { new Integer(viewer.getPageIndex() + 1),
							new Integer(viewer.getDocument().getPages().size()) });
		}
	}

	void refresh()
	{
		if (label == null || label.isDisposed())
			return;
		try
		{
			if (!viewer.hasDocument())
			{
				label.setEnabled(false);
				label.setText(""); //$NON-NLS-1$
			}
			else
			{
				label.setText(getText());
				label.setEnabled(true);
			}
		}
		catch (SWTException exception)
		{
			if (!SWT.getPlatform().equals("gtk")) //$NON-NLS-1$
				throw exception;
		}
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewerListener#viewerStateChanged(ch.eugster.events.report.internal.viewer.ReportViewerEvent)
	 */
	@Override
	public void viewerStateChanged(final ReportViewerEvent evt)
	{
		refresh();
	}

}