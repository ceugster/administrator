package ch.eugster.events.todo.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.editors.CourseEditor;
import ch.eugster.events.course.editors.CourseEditorInput;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.TodoMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.TodoEntry;
import ch.eugster.events.persistence.model.TodoEntry.DueType;
import ch.eugster.events.todo.Activator;
import ch.eugster.events.todo.service.TodoCollectorService;
import ch.eugster.events.ui.views.AbstractEntityView;

public class TodoView extends AbstractEntityView implements IDoubleClickListener
{
	public static final String ID = "ch.eugster.events.todo.views.TodoView";

	public static final String SETTINGS_KEY_FROM_DATE = "todo.from.date";

	public static final String SETTINGS_KEY_UNTIL_DATE = "todo.until.date";

	private CDateTime start;

	private CDateTime end;

	private Label countLabel;

	private TableViewer viewer;

	private IDialogSettings dialogSettings;

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
		ctxActivation = ctxService.activateContext("ch.eugster.events.todo.context");

		GridLayout gridLayout = new GridLayout();
		gridLayout.marginBottom = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginLeft = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginWidth = 0;

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(gridLayout);

		Composite topComposite = new Composite(composite, SWT.NONE);
		topComposite.setLayout(new GridLayout(2, false));
		topComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite startComposite = new Composite(topComposite, SWT.NONE);
		startComposite.setLayout(new GridLayout(2, false));
		startComposite.setLayoutData(new GridData());

		Label label = new Label(startComposite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Beginn");

		GridData gridData = new GridData();
		gridData.widthHint = 112;

		Calendar startCalendar = GregorianCalendar.getInstance();
		Calendar endCalendar = GregorianCalendar.getInstance();
		startCalendar.add(Calendar.DATE, dialogSettings.getInt(SETTINGS_KEY_FROM_DATE));
		endCalendar.add(Calendar.DATE, dialogSettings.getInt(SETTINGS_KEY_UNTIL_DATE));

		this.start = new CDateTime(startComposite, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		this.start.setSelection(startCalendar.getTime());
		this.start.setLayoutData(gridData);
		this.start.setNullText("");
		this.start.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				if (event.data != null)
					dialogSettings.put(SETTINGS_KEY_FROM_DATE, ((Date)event.data).compareTo(GregorianCalendar.getInstance().getTime()));
			}
		});
		
		Composite endComposite = new Composite(topComposite, SWT.NONE);
		endComposite.setLayout(new GridLayout(2, false));
		endComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(endComposite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Ende");

		gridData = new GridData();
		gridData.widthHint = 112;

		this.end = new CDateTime(endComposite, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		this.end.setSelection(endCalendar.getTime());
		this.end.setLayoutData(gridData);
		this.end.setNullText("");
		this.end.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				if (event.data != null)
					dialogSettings.put(SETTINGS_KEY_UNTIL_DATE, ((Date)event.data).compareTo(GregorianCalendar.getInstance().getTime()));
			}
		});

		TableLayout layout = new TableLayout();

		Table table = new Table(composite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLayout(layout);
		table.setHeaderVisible(true);

		this.viewer = new CheckboxTableViewer(table);
		this.viewer.setContentProvider(new ArrayContentProvider());
		this.viewer.setSorter(new ViewerSorter() 
		{
			public int compare(Viewer viewer, Object o1, Object o2)
			{
				TodoEntry entry1 = (TodoEntry) o1;
				TodoEntry entry2 = (TodoEntry) o2;
				return entry1.getDueDate().compareTo(entry2.getDueDate());
			}
			
		});
		this.viewer.addDoubleClickListener(this);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				TodoEntry entry = (TodoEntry) cell.getElement();
				cell.setText(entry.getEntityName());
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Typ");

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				TodoEntry entry = (TodoEntry) cell.getElement();
				cell.setText(entry.getInstanceName());
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Bezeichnung");

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				TodoEntry entry = (TodoEntry) cell.getElement();
				cell.setText(entry.getDueType());
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Aufgabe");

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				TodoEntry entry = (TodoEntry) cell.getElement();
				cell.setText(entry.getDueDate() == null ? "" : SimpleDateFormat.getDateInstance().format(entry.getDueDate().getTime()));
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Fällig");

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				TodoEntry entry = (TodoEntry) cell.getElement();
				cell.setText(entry.getCourseBeginTime() == null ? "" : SimpleDateFormat.getDateTimeInstance().format(entry.getCourseBeginTime().getTime()));
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Kursbeginn");

		this.createContextMenu();

		Composite bottomComposite = new Composite(composite, SWT.NONE);
		bottomComposite.setLayout(new GridLayout(2, true));
		bottomComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.countLabel = new Label(bottomComposite, SWT.NONE);
		this.countLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.getSite().setSelectionProvider(this.viewer);

		EntityMediator.addListener(Course.class, this);
		
		this.reloadTodoList();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Course.class, this);
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxService.deactivateContext(ctxActivation);
		super.dispose();
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof TodoEntry)
		{
			TodoEntry entry = (TodoEntry) object;
			AbstractEntity entity = entry.getEntity();
			if (entity instanceof Course)
			{
				this.editCourse((Course) entity);
			}
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

	public Label getCountLabel()
	{
		return this.countLabel;
	}

	public TableViewer getViewer()
	{
		return this.viewer;
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);

		this.dialogSettings = Activator.getDefault().getDialogSettings().getSection(TodoView.ID);
		if (this.dialogSettings == null)
		{
			this.dialogSettings = Activator.getDefault().getDialogSettings().addNewSection(TodoView.ID);
		}
		if (this.dialogSettings.get(SETTINGS_KEY_FROM_DATE) == null)
		{
			this.dialogSettings.put(SETTINGS_KEY_FROM_DATE, 0);
		}
		if (this.dialogSettings.get(SETTINGS_KEY_UNTIL_DATE) == null)
		{
			this.dialogSettings.put(SETTINGS_KEY_UNTIL_DATE, 7);
		}
	}

	private void internalRefresh()
	{
		if (!viewer.getControl().isDisposed())
		{
			viewer.refresh();
			packColumns();
			updateCountLabel();
		}
	}

	private void updateCountLabel()
	{
		if (countLabel != null && !countLabel.isDisposed())
		{
			countLabel.setText("Pendenzen: " + viewer.getTable().getItemCount());
		}
	}

	public void packColumns()
	{
		if (!viewer.getTable().isDisposed())
		{
			TableColumn[] tableColumns = this.viewer.getTable().getColumns();
			for (TableColumn tableColumn : tableColumns)
			{
				if (tableColumn != null && !tableColumn.isDisposed())
				{
					tableColumn.pack();
				}
			}
		}
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		if (doUpdate(entity))
		{
			reloadTodoList();
		}
	}

	@Override
	public void postPersist(final AbstractEntity entity)
	{
		if (doUpdate(entity))
		{
			reloadTodoList();
		}
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
		if (doUpdate(entity))
		{
			reloadTodoList();
		}
	}

	private Calendar getStart()
	{
		Calendar startDate = null;
		if (this.start.getSelection() == null)
		{
			startDate = GregorianCalendar.getInstance();
			startDate.set(Calendar.YEAR, 0);
		}
		else
		{
			startDate = this.start.getCalendarInstance(this.start.getSelection());
			startDate.set(Calendar.HOUR_OF_DAY, 0);
			startDate.set(Calendar.MINUTE, 0);
			startDate.set(Calendar.SECOND, 0);
		}
		return startDate;
	}
	
	private Calendar getEnd()
	{
		Calendar endDate = null;
		if (this.end.getSelection() == null)
		{
			endDate = GregorianCalendar.getInstance();
			endDate.set(Calendar.YEAR, Integer.MAX_VALUE);
		}
		else
		{
			endDate = this.end.getCalendarInstance(this.end.getSelection());
			endDate.add(Calendar.DATE, 1);
			endDate.set(Calendar.HOUR_OF_DAY, 0);
			endDate.set(Calendar.MINUTE, 0);
			endDate.set(Calendar.SECOND, 0);
		}
		return endDate;
	}

	private boolean doUpdate(AbstractEntity entity)
	{
		if (entity instanceof Course)
		{
			Calendar startDate = getStart();
			Calendar endDate = getEnd();
			Course course = (Course) entity;
			if (course.getAdvanceNoticeDate().after(startDate) && course.getAdvanceNoticeDate().before(endDate))
			{
				return true;
			}
			if (course.getInvitationDate().after(startDate) && course.getInvitationDate().before(endDate))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus()
	{
		this.viewer.getControl().setFocus();
	}

	private void updateViewer(final TodoEntry[] entries)
	{
		UIJob updateViewer = new UIJob("Pendenzen werden aufbereitet...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				if (!TodoView.this.getViewer().getControl().isDisposed())
				{
					TodoView.this.getViewer().getTable().setEnabled(false);
					TodoView.this.showBusy(true);
					TodoView.this.setInput(entries);
					TodoView.this.internalRefresh();
					TodoView.this.showBusy(false);
					TodoView.this.getViewer().getTable().setEnabled(true);
				}
				return Status.OK_STATUS;
			}

		};
		updateViewer.setUser(true);
		updateViewer.schedule();
	}

	private void setInput(Object object)
	{
		if (!this.viewer.getTable().isDisposed())
		{
			this.viewer.setInput(object);
		}
	}

	public void reloadTodoList()
	{
		List<TodoEntry> entries = new ArrayList<TodoEntry>(); 
		Calendar startDate = getStart();
		Calendar endDate = getEnd();
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(), TodoCollectorService.class.getName(), null);
		tracker.open();
		try
		{
			TodoCollectorService service = (TodoCollectorService) tracker.getService();
			if (service != null)
			{
				List<Course> courses = service.collectCoursesWithDueAdvanceNoticeDates(startDate.getTimeInMillis(), endDate.getTimeInMillis());
				for (Course course : courses)
				{
					entries.add(new TodoEntry(course, DueType.COURSE_ADVANCE_NOTICE_DATE));
				}
				courses = service.collectCoursesWithDueInvitationDates(startDate.getTimeInMillis(), endDate.getTimeInMillis());
				for (Course course : courses)
				{
					entries.add(new TodoEntry(course, DueType.COURSE_INVITATION_DATE));
				}
			}
		}
		finally
		{
			tracker.close();
		}
		this.updateViewer(entries.toArray(new TodoEntry[0]));
	}
	
	public IStatus generateTodoList()
	{
		TableItem[] items = this.viewer.getTable().getItems();
		final DataMap[] dataMaps = new DataMap[items.length];
		for (int i = 0; i < items.length; i++)
		{
			TodoEntry entry = (TodoEntry) items[i].getData();
			dataMaps[i] = new TodoMap(entry);
		}
		UIJob job = new UIJob("Dokument wird generiert...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				IStatus status = Status.OK_STATUS;
				final ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
						DocumentBuilderService.class.getName(), null);
				try
				{
					tracker.open();
					Object[] services = tracker.getServices();
					for (Object service : services)
					{
						if (service instanceof DocumentBuilderService)
						{

							DocumentBuilderService builderService = (DocumentBuilderService) service;
							status = builderService.buildDocument(monitor, TodoMap.Key.values(), dataMaps);
						}
					}
				}
				finally
				{
					tracker.close();
				}
				return status;
			}
		};
		job.addJobChangeListener(new JobChangeAdapter()
		{
			@Override
			public void done(final IJobChangeEvent event)
			{
				if (!event.getResult().isOK())
				{
					ErrorDialog.openError(TodoView.this.getSite().getShell(), "Verarbeitungsfehler",
							"Beim Generieren des Dokuments ist ein Fehler aufgetreten.", event.getResult(), 0);
				}
			}
		});
		job.schedule();
		IStatus status = Status.CANCEL_STATUS;
		return status;
	}

}