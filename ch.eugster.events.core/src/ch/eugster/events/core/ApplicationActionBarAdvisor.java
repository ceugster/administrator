package ch.eugster.events.core;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor
{
	// File Menu
	private IWorkbenchAction quitAction;

	// Edit Menu
	private IWorkbenchAction undoAction;
	private IWorkbenchAction redoAction;
	private IWorkbenchAction cutAction;
	private IWorkbenchAction copyAction;
	private IWorkbenchAction pasteAction;
	private IWorkbenchAction deleteAction;
	private IWorkbenchAction selectAllAction;

	// Window Menu
	private IWorkbenchAction preferencesAction;

	// Help Menu
	private IWorkbenchAction introAction;
	private IWorkbenchAction aboutAction;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer)
	{
		super(configurer);
	}

	@Override
	protected void makeActions(IWorkbenchWindow window)
	{
		this.makeFileActions(window);
		this.makeEditActions(window);
		this.makeWindowActions(window);
		this.makeHelpActions(window);
	}

	protected void makeFileActions(IWorkbenchWindow window)
	{
		this.quitAction = ActionFactory.QUIT.create(window);
		this.quitAction.setText("Beenden");
		this.quitAction.setToolTipText("Programm beenden");
		this.quitAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("EXIT"));
		this.register(this.quitAction);
	}

	protected void makeEditActions(IWorkbenchWindow window)
	{
		this.undoAction = ActionFactory.UNDO.create(window);
		this.undoAction.setText("Rückgängig");
		this.register(this.undoAction);

		this.redoAction = ActionFactory.REDO.create(window);
		this.redoAction.setText("Wiederholen");
		this.register(this.redoAction);

		this.cutAction = ActionFactory.CUT.create(window);
		this.cutAction.setText("Ausschneiden");
		this.register(this.cutAction);

		this.copyAction = ActionFactory.COPY.create(window);
		this.copyAction.setText("Kopieren");
		this.register(this.copyAction);

		this.pasteAction = ActionFactory.PASTE.create(window);
		this.pasteAction.setText("Einfügen");
		this.register(this.pasteAction);

		this.deleteAction = ActionFactory.DELETE.create(window);
		this.deleteAction.setText("Entfernen");
		this.register(this.deleteAction);

		this.selectAllAction = ActionFactory.SELECT_ALL.create(window);
		this.selectAllAction.setText("Alles auswählen");
		this.register(this.selectAllAction);
	}

	protected void makeWindowActions(IWorkbenchWindow window)
	{
		this.preferencesAction = ActionFactory.PREFERENCES.create(window);
		this.preferencesAction.setText("Einstellungen");
		this.preferencesAction.setToolTipText("Programmeinstellungen");
		this.preferencesAction.setImageDescriptor(Activator.getDefault().getImageRegistry()
				.getDescriptor("PREFERENCES"));
		this.register(this.preferencesAction);
	}

	protected void makeHelpActions(IWorkbenchWindow window)
	{
		this.introAction = ActionFactory.INTRO.create(window);
		this.introAction.setText("Intro");
		this.introAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("INTRO"));

		this.aboutAction = ActionFactory.ABOUT.create(window);
		this.aboutAction.setText("Über den Administrator");
		// this.aboutAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("INTRO"));
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar)
	{
		menuBar.add(this.createFileMenu());
		menuBar.add(this.createEditMenu());
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(this.createViewsMenu());
		menuBar.add(this.createPerspectiveMenu());
		menuBar.add(this.createWindowMenu());
		menuBar.add(this.createHelpMenu());
	}

	protected MenuManager createFileMenu()
	{
		MenuManager fileMenu = new MenuManager("&Datei", IWorkbenchActionConstants.M_FILE);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		fileMenu.add(this.quitAction);
		fileMenu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return fileMenu;
	}

	protected MenuManager createEditMenu()
	{
		MenuManager editMenu = new MenuManager("&Bearbeiten", IWorkbenchActionConstants.M_EDIT);
		editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));
		editMenu.add(this.undoAction);
		editMenu.add(this.redoAction);
		editMenu.add(new Separator("editCutCopyPaste"));
		editMenu.add(this.cutAction);
		editMenu.add(this.copyAction);
		editMenu.add(this.pasteAction);
		editMenu.add(new Separator("editDeleteSelectAll"));
		editMenu.add(this.deleteAction);
		editMenu.add(this.selectAllAction);
		editMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		editMenu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
		return editMenu;
	}

	protected MenuManager createViewsMenu()
	{
		MenuManager windowMenu = new MenuManager("&Sichten", Activator.getDefault().getImageRegistry().getDescriptor(
				"VIEW"), "ch.eugster.events.ui.menu.views");
		windowMenu.add(this.createGeneralViewsMenu());
		windowMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		return windowMenu;
	}

	protected MenuManager createGeneralViewsMenu()
	{
		MenuManager general = new MenuManager("Allgemein", "ch.eugster.events.core.views.general");
		general.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		return general;
	}

	protected MenuManager createPerspectiveMenu()
	{
		MenuManager perspectivesMenu = new MenuManager("&Perspektiven", Activator.getDefault().getImageRegistry()
				.getDescriptor("PERSPECTIVE"), "ch.eugster.events.ui.menu.perspectives");
		perspectivesMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		return perspectivesMenu;
	}

	protected MenuManager createWindowMenu()
	{
		MenuManager windowMenu = new MenuManager("&Fenster", IWorkbenchActionConstants.M_WINDOW);
		windowMenu.add(new GroupMarker(IWorkbenchActionConstants.WINDOW_EXT));
		windowMenu.add(this.preferencesAction);
		return windowMenu;
	}

	protected MenuManager createHelpMenu()
	{
		MenuManager helpMenu = new MenuManager("&Hilfe", IWorkbenchActionConstants.M_HELP);
		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		helpMenu.add(this.introAction);
		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		helpMenu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		helpMenu.add(this.aboutAction);
		return helpMenu;
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar)
	{
		coolBar.add(this.createFileToolBar());
		coolBar.add(this.createEditToolBar());
	}

	private IToolBarManager createFileToolBar()
	{
		IToolBarManager toolBar = new ToolBarManager(SWT.FLAT);
		toolBar.add(this.quitAction);
		return toolBar;
	}

	private IToolBarManager createEditToolBar()
	{
		IToolBarManager toolBar = new ToolBarManager(SWT.FLAT);
		toolBar.add(this.cutAction);
		toolBar.add(this.copyAction);
		toolBar.add(this.pasteAction);
		toolBar.add(this.selectAllAction);
		return toolBar;
	}

	@Override
	protected void fillStatusLine(IStatusLineManager statusLine)
	{
		// TextStatusLineContributionItem item = new
		// TextStatusLineContributionItem("test");
		// statusLine.add(item);
	}

}
