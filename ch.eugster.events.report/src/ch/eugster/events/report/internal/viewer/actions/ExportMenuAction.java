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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import ch.eugster.events.report.Activator;
import ch.eugster.events.report.internal.viewer.IReportViewer;

/**
 * Export menu action. Provides a pull down menu that can be used to place
 * specific export action.
 * 
 * @author Peter Severin (peter_p_s@users.sourceforge.net)
 */
public class ExportMenuAction extends AbstractReportViewerAction implements IMenuCreator
{
	private MenuManager menuManager = new MenuManager();

	private Menu menu;

	private IAction defaultAction;

	/**
	 * @see AbstractReportViewerAction#AbstractReportViewerAction(IReportViewer)
	 */
	public ExportMenuAction(final IReportViewer viewer)
	{
		super(viewer, AS_DROP_DOWN_MENU);

		setText("Expxort"); //$NON-NLS-1$
		setToolTipText("Export"); //$NON-NLS-1$
		setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("save"));
		setDisabledImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("saved"));
		setMenuCreator(this);
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
	 * @see com.jasperassistant.designer.viewer.actions.AbstractReportViewerAction#dispose()
	 */
	@Override
	public void dispose()
	{
		menuManager.dispose();
	}

	/**
	 * @return the default action
	 * @see #setDefaultAction(IAction)
	 */
	public IAction getDefaultAction()
	{
		return defaultAction;
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	@Override
	public Menu getMenu(final Control parent)
	{
		if (menu == null)
			menu = menuManager.createContextMenu(parent);
		return menu;
	}

	/**
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	@Override
	public Menu getMenu(final Menu parent)
	{
		return null;
	}

	/**
	 * Returns the menu manager associated with this action. Actions added to
	 * the menu manager will appear in the drop-down menu.
	 * 
	 * @return the menu manager
	 */
	public MenuManager getMenuManager()
	{
		return menuManager;
	}

	/**
	 * @see com.jasperassistant.designer.viewer.actions.AbstractReportViewerAction#run()
	 */
	@Override
	public void run()
	{
		if (defaultAction != null && defaultAction.isEnabled())
			defaultAction.run();
	}

	/**
	 * Sets the default action that gets executed when the menu button is
	 * clicked.
	 * 
	 * @param defaultAction
	 *            the default action
	 */
	public void setDefaultAction(final IAction defaultAction)
	{
		this.defaultAction = defaultAction;
	}
}
