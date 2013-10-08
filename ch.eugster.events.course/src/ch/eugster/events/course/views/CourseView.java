package ch.eugster.events.course.views;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.course.Constants;
import ch.eugster.events.course.actions.CourseCopyAction;
import ch.eugster.events.course.editors.CourseEditor;
import ch.eugster.events.course.editors.CourseEditorInput;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dnd.CourseDragSourceListener;
import ch.eugster.events.ui.dnd.CourseTransfer;
import ch.eugster.events.ui.views.AbstractEntityView;

public class CourseView extends AbstractEntityView implements IDoubleClickListener
{
	public static final String ID = "ch.eugster.events.course.views.courseView";

	private Text filterText;

	private Button filterButton;

	private TreeViewer viewer;

	private CourseContentProvider courseContentProvider;

	private ViewerFilter[] viewerFilters;

	private Label countSeasons;

	private Label countCourses;

	private IDialogSettings dialogSettings;

	private final DateFormat df = DateFormat.getDateInstance();

	private IAction copy;

	private ServiceTracker connectionServiceTracker;

	private IContextActivation ctxActivation;

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener()
		{
			@Override
			public void menuAboutToShow(final IMenuManager manager)
			{
				manager.add(CourseView.this.getCopyAction());
				manager.add(new Separator());
			}
		});

		Menu menu = menuManager.createContextMenu(this.viewer.getControl());
		this.viewer.getControl().setMenu(menu);

		this.getSite().registerContextMenu(menuManager, this.viewer);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(final Composite parent)
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxActivation = ctxService.activateContext("ch.eugster.events.course.context");

		parent.setLayout(new GridLayout());

		Composite filterComposite = new Composite(parent, SWT.NONE);
		filterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterComposite.setLayout(new GridLayout(3, false));

		Label label = new Label(filterComposite, SWT.NONE);
		label.setText("Filter");
		label.setLayoutData(new GridData());

		this.filterText = new Text(filterComposite, SWT.BORDER);
		this.filterText.setText(this.dialogSettings.get(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_TEXT_VALUE_ID));
		this.filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.filterText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				CourseView.this.dialogSettings.put(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_TEXT_VALUE_ID,
						CourseView.this.filterText.getText());
				CourseView.this.refreshViewer(null);
			}
		});

		this.filterButton = new Button(filterComposite, SWT.TOGGLE);
		this.filterButton.setImage(Activator.getDefault().getImageRegistry().get("FILTER"));
		this.filterButton.setSelection(this.dialogSettings
				.getBoolean(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_BUTTON_SELECTED_ID));
		this.filterButton.setLayoutData(new GridData());
		this.filterButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseView.this.dialogSettings.put(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_BUTTON_SELECTED_ID,
						CourseView.this.filterButton.getSelection());
				CourseView.this.refreshViewer(null);
			}
		});

		final Tree tree = new Tree(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setHeaderVisible(true);
		tree.addListener(SWT.Expand, new Listener()
		{
			@Override
			public void handleEvent(final Event event)
			{
				if (tree != null && !tree.isDisposed())
				{
					tree.getDisplay().asyncExec(new Runnable()
					{
						@Override
						public void run()
						{
							if (tree.isDisposed())
								return;
							CourseView.this.packColumns();
						}
					});
				}
			}
		});

		this.viewer = new TreeViewer(tree);
		courseContentProvider = new CourseContentProvider();
		this.viewer.setContentProvider(courseContentProvider);
		this.viewer.setSorter(new CourseSorter());

		SeasonFilter seasonFilter = new SeasonFilter(this.viewer);
		DeletedEntityFilter deletedFilter = new DeletedEntityFilter();
		CourseFilter courseFilter = new CourseFilter(this.filterText, this.filterButton);
		DoneCoursesFilter doneCoursesFilter = new DoneCoursesFilter(this.viewer);
		AnnulatedCoursesFilter annulatedCoursesFilter = new AnnulatedCoursesFilter(this.viewer);

		viewerFilters = new ViewerFilter[] { seasonFilter, deletedFilter, courseFilter, doneCoursesFilter,
				annulatedCoursesFilter };

		this.viewer.setFilters(viewerFilters);
		this.viewer.addDoubleClickListener(this);

		Transfer[] transfers = new Transfer[] { CourseTransfer.getTransfer() };
		int ops = DND.DROP_MOVE | DND.DROP_COPY;
		this.viewer.addDragSupport(ops, transfers, new CourseDragSourceListener(this.viewer));

		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Season)
				{
					Season season = (Season) cell.getElement();
					cell.setImage(Activator.getDefault().getImageRegistry().get(season.getImageKey()));
					cell.setText(season.getCode());
				}
				else if (cell.getElement() instanceof Course)
				{
					Course course = (Course) cell.getElement();
					cell.setImage(Activator.getDefault().getImageRegistry().get(course.getState().imageKey()));
					cell.setText(course.getCode());
				}
			}

		});
		GC gc = new GC(tree);
		FontMetrics fm = gc.getFontMetrics();
		int charWidth = fm.getAverageCharWidth();
		int columnWidth = charWidth * 30;

		TreeColumn treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Code");
		treeColumn.setWidth(columnWidth);
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Course)
				{
					Course course = (Course) cell.getElement();
					cell.setText(course.getTitle());
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Bezeichnung");
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.CENTER);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Course)
				{
					Course course = (Course) cell.getElement();
					cell.setText((course.getFirstDate() instanceof Calendar) ? CourseView.this.df.format(course
							.getFirstDate().getTime()) : "?");
				}
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Beginn");
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Course)
				{
					Course course = (Course) cell.getElement();
					cell.setText(course.getLastDate() instanceof Calendar ? CourseView.this.df.format(course
							.getLastDate().getTime()) : "?");
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Ende");
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Course)
				{
					Course course = (Course) cell.getElement();
					cell.setText(course.getState().toString());
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Status");
		treeColumn.setResizable(true);

		Composite countComposite = new Composite(parent, SWT.NONE);
		countComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		countComposite.setLayout(new GridLayout(2, true));

		countSeasons = new Label(countComposite, SWT.NONE);
		countSeasons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		countCourses = new Label(countComposite, SWT.NONE);
		countCourses.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.viewer);

		this.filterText.setText(this.dialogSettings.get(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_TEXT_VALUE_ID));
		this.filterButton.setSelection(this.dialogSettings
				.getBoolean(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_BUTTON_SELECTED_ID));

		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

		Command command = service.getCommand(Constants.SHOW_ALL_SEASONS_COMMAND);
		State state = command.getState("org.eclipse.ui.commands.toggleState");
		state.addListener(seasonFilter);
		seasonFilter.doFilter(((Boolean) state.getValue()).booleanValue());

		command = service.getCommand(Constants.SHOW_DONE_COURSES_COMMAND);
		state = command.getState("org.eclipse.ui.commands.toggleState");
		state.addListener(doneCoursesFilter);
		doneCoursesFilter.doFilter(((Boolean) state.getValue()).booleanValue());

		command = service.getCommand(Constants.SHOW_ANNULATED_COURSES_COMMAND);
		state = command.getState("org.eclipse.ui.commands.toggleState");
		state.addListener(annulatedCoursesFilter);
		annulatedCoursesFilter.doFilter(((Boolean) state.getValue()).booleanValue());

		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(final ServiceReference reference)
			{
				final ConnectionService connectionService = (ConnectionService) super.addingService(reference);
				Display display = Display.getCurrent();
				if (display == null)
				{
					display = Display.getDefault();
				}
				display.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						setInput(connectionService);
					}
				});
				return connectionService;
			}

			@Override
			public void removedService(final ServiceReference reference, final Object service)
			{
				Display display = Display.getCurrent();
				if (display == null)
				{
					display = Display.getDefault();
				}
				display.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						if (viewer.getContentProvider() != null)
						{
							setInput(null);
						}
					}
				});
				super.removedService(reference, service);
			}

		};

		connectionServiceTracker.open();
	}

	@Override
	public void dispose()
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxService.deactivateContext(ctxActivation);

		connectionServiceTracker.close();

		EntityMediator.removeListener(Season.class, this);
		EntityMediator.removeListener(Course.class, this);
		super.dispose();
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof Season)
		{
			this.viewer.setExpandedState(object, !this.viewer.getExpandedState(object));
		}
		else if (object instanceof Course)
		{
			this.editCourse((Course) object);
		}
	}

	private void editCourse(final Course course)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new CourseEditorInput(course), CourseEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}

	private IAction getCopyAction()
	{
		if (this.copy == null)
		{
			this.copy = new CourseCopyAction(this.viewer, "Teilnehmer kopieren", SWT.CTRL | 'C');
		}
		return this.copy;
	}

	public IDialogSettings getDialogSettings()
	{
		return this.dialogSettings;
	}

	public TreeViewer getViewer()
	{
		return this.viewer;
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);

		this.dialogSettings = Activator.getDefault().getDialogSettings()
				.getSection(Constants.DIALOG_SETTINGS_KEY_COURSE_VIEW_SECTION_ID);
		if (this.dialogSettings == null)
		{
			this.dialogSettings = Activator.getDefault().getDialogSettings()
					.addNewSection(Constants.DIALOG_SETTINGS_KEY_COURSE_VIEW_SECTION_ID);
			this.dialogSettings.put(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_BUTTON_SELECTED_ID, false);
			this.dialogSettings.put(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_TEXT_VALUE_ID, "");
		}

		EntityMediator.addListener(Season.class, this);
		EntityMediator.addListener(Course.class, this);
	}

	private void packColumns()
	{
		TreeColumn[] columns = this.viewer.getTree().getColumns();
		for (int i = 1; i < columns.length; i++)
			columns[i].pack();
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (entity instanceof Season)
					refreshViewer(null);
				else if (entity instanceof Course)
					refreshViewer(null);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void postPersist(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (entity instanceof Season)
				{
					viewer.add(this, entity);
					viewer.refresh();
				}
				else if (entity instanceof Course)
				{
					Course course = (Course) entity;
					viewer.add(course.getSeason(), course);
				}
				packColumns();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (entity instanceof Season)
				{
					refreshViewer(null);
				}
				else if (entity instanceof Course)
				{
					refreshViewer(entity);
				}
				packColumns();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void refreshViewer(final AbstractEntity entity)
	{
		if (entity == null)
		{
			viewer.refresh();
		}
		else
		{
			viewer.refresh(entity);
		}
		Object[] children = courseContentProvider.getChildren(viewer.getInput());
		int seasonCount = 0;
		int courseCount = 0;
		for (Object child : children)
		{
			boolean show = true;
			if (child instanceof Season)
			{
				Season season = (Season) child;
				for (ViewerFilter filter : viewerFilters)
				{
					if (show)
					{
						show = filter.select(this.viewer, null, child);
					}
				}
				if (show)
				{
					seasonCount++;
					Collection<Course> courses = season.getCourses();
					for (Course course : courses)
					{
						show = true;
						for (ViewerFilter filter : viewerFilters)
						{
							if (show)
							{
								show = filter.select(this.viewer, course.getSeason(), course);
							}
						}
						if (show)
						{
							courseCount++;
						}
					}
					if (!season.isClosed())
					{
						this.viewer.setExpandedState(season, true);
					}
				}
			}
		}
		countSeasons.setText("Anzahl Saisons: " + seasonCount);
		countCourses.setText("Anzahl Kurse: " + courseCount);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus()
	{
		this.filterText.setFocus();
	}

	private void setInput(final Object object)
	{
		this.viewer.setInput(object);
		CourseView.this.refreshViewer(null);
		packColumns();
	}
}