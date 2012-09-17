package ch.eugster.events.guide.views;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.guide.Activator;
import ch.eugster.events.guide.editors.CompensationTypeEditor;
import ch.eugster.events.guide.editors.CompensationTypeEditorInput;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.CompensationType;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.views.AbstractEntityView;

public class CompensationTypeView extends AbstractEntityView implements IDoubleClickListener
{
	public static final String ID = "ch.eugster.events.guide.views.compensation";

	private TableViewer viewer;

	private ServiceTracker connectionServiceTracker;

	private IContextActivation ctxActivation;

	public CompensationTypeView()
	{
		EntityMediator.addListener(CompensationType.class, this);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent)
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxActivation = ctxService.activateContext("ch.eugster.events.guide.compensation.context");

		TableLayout layout = new TableLayout();

		Table table = new Table(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayout(layout);
		table.setHeaderVisible(true);

		this.viewer = new TableViewer(table);
		this.viewer.setContentProvider(new CompensationTypeContentProvider());
		this.viewer.setSorter(new CompensationTypeSorter());
		this.viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.viewer.addDoubleClickListener(this);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof CompensationType)
				{
					cell.setText(((CompensationType) object).getCode());
					cell.setImage(Activator.getDefault().getImageRegistry().get("CATEGORY"));
				}
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Code");

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof CompensationType)
				{
					cell.setText(((CompensationType) object).getName());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Bezeichnung");

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.viewer);

		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(ServiceReference reference)
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
						viewer.setInput(connectionService);
						packColumns();
					}
				});
				return connectionService;
			}

			@Override
			public void removedService(ServiceReference reference, Object service)
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
							viewer.setInput(null);
						}
					}
				});
				super.removedService(reference, service);
			}

		};
		connectionServiceTracker.open();
	}

	private void packColumns()
	{
		TableColumn[] tableColumns = this.viewer.getTable().getColumns();
		for (TableColumn tableColumn : tableColumns)
		{
			tableColumn.pack();
		}

	}

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(this.viewer.getControl());
		this.viewer.getControl().setMenu(menu);

		this.getSite().registerContextMenu(menuManager, this.viewer);
	}

	@Override
	public void doubleClick(DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof CompensationType)
		{
			this.editCompensationType((CompensationType) object);
		}
	}

	private void editCompensationType(CompensationType compensationType)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new CompensationTypeEditorInput(compensationType), CompensationTypeEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}

	public TableViewer getViewer()
	{
		return this.viewer;
	}

	@Override
	public void postPersist(AbstractEntity entity)
	{
		if (entity instanceof CompensationType)
		{
			this.viewer.add(entity);
			this.packColumns();
		}
	}

	@Override
	public void postUpdate(AbstractEntity entity)
	{
		if (entity instanceof CompensationType)
		{
			this.viewer.refresh(entity);
			this.packColumns();
		}
	}

	@Override
	public void postDelete(AbstractEntity entity)
	{
		if (entity instanceof CompensationType)
		{
			this.viewer.refresh();
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus()
	{
		this.viewer.getControl().setFocus();
	}

	@Override
	public void dispose()
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxService.deactivateContext(ctxActivation);

		connectionServiceTracker.close();
		EntityMediator.removeListener(CompensationType.class, this);
		super.dispose();
	}
}