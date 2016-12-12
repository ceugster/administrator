package ch.eugster.events.visits.views;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityListener;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.DateFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.SchoolLevel;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.model.VisitTheme;
import ch.eugster.events.persistence.model.VisitVisitor;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.views.PersonView;
import ch.eugster.events.ui.views.AbstractEntityView;
import ch.eugster.events.visits.Activator;
import ch.eugster.events.visits.editors.VisitEditor;
import ch.eugster.events.visits.editors.VisitEditorInput;

public class VisitView extends AbstractEntityView implements IDoubleClickListener, ISelectionListener, EntityListener
{
	private IContextActivation ctxActivation;

	private TableViewer viewer;

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	@Override
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);
		EntityMediator.addListener(Visit.class, this);

		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxActivation = ctxService.activateContext("ch.eugster.events.visits.visit.context");
	}

	@Override
	public void dispose()
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxService.deactivateContext(ctxActivation);

		EntityMediator.removeListener(Visit.class, this);
		super.dispose();
	}

	@Override
	public void postDelete(AbstractEntity entity)
	{
		if (entity instanceof Visit)
		{
			this.viewer.refresh();
		}
	}

	@Override
	public void postPersist(AbstractEntity entity)
	{
		if (entity instanceof Visit)
		{
			this.viewer.add(entity);
			this.packColumns();
		}
	}

	@Override
	public void postUpdate(AbstractEntity entity)
	{
		if (entity instanceof Visit)
		{
			this.viewer.refresh(entity);
			this.packColumns();
		}
	}

	@Override
	public void createPartControl(Composite parent)
	{
		TableLayout layout = new TableLayout();

		Table table = new Table(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayout(layout);
		table.setHeaderVisible(true);

		viewer = new TableViewer(table);
		viewer.setContentProvider(new VisitContentProvider());
		viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		viewer.addDoubleClickListener(this);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Visit)
				{
					VisitTheme theme = ((Visit) object).getTheme();
					cell.setText(theme == null ? "" : theme.getName());
				}
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Thema");

		tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Visit)
				{
					Visit visit = (Visit) object;
					String className = visit.getClassName();
					cell.setText(className);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Schule");

		tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Visit)
				{
					Visit visit = (Visit) object;
					SchoolLevel schoolLevel = visit.getSchoolLevel();
					cell.setText(schoolLevel == null ? "" : schoolLevel.getName());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Stufe");

		tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Visit)
				{
					Visit visit = (Visit) object;
					StringBuilder names = new StringBuilder();
					List<VisitVisitor> visitors = visit.getVisitors();
					for (VisitVisitor visitor : visitors)
					{
						String name = PersonFormatter.getInstance().formatLastnameFirstname(visitor.getVisitor().getLink().getPerson());
						if (names.length() == 0)
						{
							names = names.append(name);
						}
						else
						{
							names = names.append(", " + name);
						}
					}
					cell.setText(names.toString());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Besucher");

		tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Visit)
				{
					Visit visit = (Visit) object;
					String daterange = DateFormatter.getInstance().formatDateRange(visit.getStart(), visit.getEnd());
					cell.setText(daterange);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Durchführung");

		createContextMenu();

		getSite().setSelectionProvider(viewer);

		connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public ConnectionService addingService(ServiceReference<ConnectionService> reference)
			{
				final ConnectionService connectionService = (ConnectionService) super.addingService(reference);
				UIJob job = new UIJob("Loading data...")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						VisitView.this.viewer.setInput(connectionService);
						VisitView.this.packColumns();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
				return connectionService;
			}

			@Override
			public void removedService(ServiceReference<ConnectionService> reference, ConnectionService service)
			{
				UIJob job = new UIJob("Removing data...")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						viewer.setInput(null);
						return null;
					}
				};
				job.schedule();
				super.removedService(reference, service);
			}

		};
		connectionServiceTracker.open();
		this.getSite().getPage().addSelectionListener(ThemeView.ID, this);
	}

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		getSite().registerContextMenu(menuManager, viewer);
	}

	@Override
	public void doubleClick(DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof Visit)
		{
			editVisit((Visit) object);
		}
	}

	private void editVisit(Visit visit)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new VisitEditorInput(visit), VisitEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setFocus()
	{
		this.viewer.getTable().setFocus();
	}
	
	private void packColumns()
	{
		TableColumn[] columns = this.viewer.getTable().getColumns();
		for (TableColumn column : columns)
		{
			column.pack();
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart workbenchPart, ISelection selection) 
	{
		if (workbenchPart instanceof ThemeView)
		{
			this.viewer.setSelection(selection);
		}
	}
}
