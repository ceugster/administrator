package ch.eugster.events.visits.views;

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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IViewSite;
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
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Appliance;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.views.AbstractEntityView;
import ch.eugster.events.visits.Activator;
import ch.eugster.events.visits.editors.ApplianceEditor;
import ch.eugster.events.visits.editors.ApplianceEditorInput;

public class ApplianceView extends AbstractEntityView implements IDoubleClickListener, EntityListener
{
	private IContextActivation ctxActivation;

	private TableViewer viewer;

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	@Override
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);
		EntityMediator.addListener(Appliance.class, this);

		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxActivation = ctxService.activateContext("ch.eugster.events.visits.appliance.context");
	}

	@Override
	public void dispose()
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxService.deactivateContext(ctxActivation);

		EntityMediator.removeListener(Appliance.class, this);
		super.dispose();
	}

	@Override
	public void postDelete(AbstractEntity entity)
	{
		if (entity instanceof Appliance)
		{
			this.viewer.refresh();
		}
	}

	@Override
	public void postPersist(AbstractEntity entity)
	{
		if (entity instanceof Appliance)
		{
			this.viewer.add(entity);
		}
	}

	@Override
	public void postUpdate(AbstractEntity entity)
	{
		if (entity instanceof Appliance)
		{
			this.viewer.refresh(entity);
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
		viewer.setContentProvider(new ApplianceContentProvider());
		viewer.setSorter(new ViewerSorter()
		{
			@Override
			public int compare(Viewer viewer, Object e1, Object e2)
			{
				Appliance a1 = (Appliance) e1;
				Appliance a2 = (Appliance) e2;
				return a1.getName().compareTo(a2.getName());
			}
		});
		viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		viewer.addDoubleClickListener(this);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Appliance)
				{
					cell.setText(((Appliance) object).getName());
					cell.setImage(Activator.getDefault().getImageRegistry().get("appliance"));
				}
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Bezeichnung");

		tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Appliance)
				{
					cell.setText(((Appliance) object).getDescription());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Beschreibung");

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
						viewer.setInput(connectionService);
						TableColumn[] columns = viewer.getTable().getColumns();
						for (TableColumn column : columns)
						{
							column.pack();
						}
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
		if (object instanceof Appliance)
		{
			editAppliance((Appliance) object);
		}
	}

	private void editAppliance(Appliance appliance)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new ApplianceEditorInput(appliance), ApplianceEditor.ID, true);
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

}
