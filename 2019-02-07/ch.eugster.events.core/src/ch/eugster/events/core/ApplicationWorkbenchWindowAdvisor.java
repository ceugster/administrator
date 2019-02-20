package ch.eugster.events.core;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor
{

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer)
	{
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer)
	{
		return new ApplicationActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen()
	{
		IWorkbenchWindowConfigurer configurer = this.getWindowConfigurer();
		configurer.getWindow().getWorkbench().getDisplay().getClientArea();
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowPerspectiveBar(true);
		configurer.setShowProgressIndicator(true);
	}

	@Override
	public void postWindowOpen()
	{
		super.postWindowOpen();

		IActionBarConfigurer configurer = getWindowConfigurer().getActionBarConfigurer();
		/* deletes unwanted Contribution from Toolbar */
		IContributionItem[] coolItems = configurer.getCoolBarManager().getItems();
		for (int i = 0; i < coolItems.length; i++)
		{
			if (coolItems[i] instanceof ToolBarContributionItem)
			{
				ToolBarContributionItem toolbarItem = (ToolBarContributionItem) coolItems[i];
				if (toolbarItem.getId() != null
						&& (toolbarItem.getId().equals("org.eclipse.ui.WorkingSetActionSet")
								|| toolbarItem.getId()
										.equals("org.eclipse.ui.edit.text.actionSet.annotationNavigation")
								|| toolbarItem.getId().equals("org.eclipse.ui.edit.text.actionSet.navigation")
								|| toolbarItem.getId().equals("org.eclipse.search.searchActionSet") || toolbarItem
								.getId().equals("org.eclipse.debug.ui.launchActionSet")))
				{
					toolbarItem.getToolBarManager().removeAll();
				}
			}
		}
		// deletes unwanted Menuitems
		IContributionItem[] menuItems = configurer.getMenuManager().getItems();
		for (int i = 0; i < menuItems.length; i++)
		{
			IContributionItem menuItem = menuItems[i];
			if (menuItem.getId().equals("org.eclipse.search.menu"))
			{
				configurer.getMenuManager().remove(menuItem);
			}
			else if (menuItem.getId().equals(IWorkbenchActionConstants.M_FILE))
			{
				IContributionItem[] items = ((IMenuManager) menuItem).getItems();
				for (IContributionItem item : items)
				{
					// loesche unerwuenschte Contributions aus dem Menue
					if (item.getId() != null
							&& (item.getId().equals("org.eclipse.ui.edit.text.openExternalFile")
									|| item.getId().equals("converstLineDelimitersTo") || item.getId().equals(
									"org.eclipse.ui.openLocalFile")))
					{
						((IMenuManager) menuItem).remove(item);
					}
				}
			}
			else if (menuItem.getId().equals(IWorkbenchActionConstants.M_WINDOW))
			{
				IContributionItem[] items = ((IMenuManager) menuItem).getItems();
				for (IContributionItem item : items)
				{
					// loesche unerwuenschte Contributions aus dem Menue
					if (item.getId() != null && item.getId().equals("selectWorkingSets"))
					{
						((IMenuManager) menuItem).remove(item);
					}
				}
			}
			else if (menuItem.getId().equals(IWorkbenchActionConstants.M_HELP))
			{
				IContributionItem[] items = ((IMenuManager) menuItem).getItems();
				for (IContributionItem item : items)
				{
					// loesche unerwuenschte Contributions aus dem Menue
					if (item.getId() != null
							&& (item.getId().equals("org.eclipse.update.ui.updateMenu") || item.getId().equals(
									"org.eclipse.ui.actions.showKeyAssistHandler")))
					{
						((IMenuManager) menuItem).remove(item);
					}
				}
			}
		}
		configurer.getCoolBarManager().update(true);
		configurer.getMenuManager().update(true);

		// deletes unwanted Preferences
		PreferenceManager pm = getWindowConfigurer().getWindow().getWorkbench().getPreferenceManager();
		IPreferenceNode[] prefNodes = pm.getRootSubNodes();
		for (IPreferenceNode node : prefNodes)
		{
			if ("org.eclipse.update.internal.ui.preferences.MainPreferencePage".equals(node.getId()))
			{
				pm.remove(node);
			}
			else if ("org.eclipse.ui.preferencePages.Workbench".equals(node.getId()))
			{
				pm.remove(node);
			}
		}
	}

}
