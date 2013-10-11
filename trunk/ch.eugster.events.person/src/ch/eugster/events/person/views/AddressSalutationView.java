package ch.eugster.events.person.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.editors.AddressSalutationEditor;
import ch.eugster.events.person.editors.AddressSalutationEditorInput;
import ch.eugster.events.ui.views.AbstractEntityView;

public class AddressSalutationView extends AbstractEntityView implements IDoubleClickListener
{
	private TableViewer viewer;

	private ServiceTracker connectionServiceTracker;

	private IContextActivation ctxActivation;

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(this.viewer.getControl());
		this.viewer.getControl().setMenu(menu);

		this.getSite().registerContextMenu(menuManager, this.viewer);
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxActivation = ctxService.activateContext("ch.eugster.events.person.salutation.context");

		TableLayout layout = new TableLayout();

		Table table = new Table(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayout(layout);
		table.setHeaderVisible(true);

		viewer = new TableViewer(table);
		viewer.setContentProvider(new AddressSalutationContentProvider());
		viewer.setSorter(new AddressSalutationSorter());
		viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		viewer.addDoubleClickListener(this);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof AddressSalutation)
				{
					cell.setText(((AddressSalutation) object).getName());
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
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof AddressSalutation)
				{
					cell.setText(((AddressSalutation) object).getSalutation());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Anrede");

		tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof AddressSalutation)
				{
					cell.setText(((AddressSalutation) object).getPolite());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Briefanrede");

		createContextMenu();

		getSite().setSelectionProvider(viewer);

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
						viewer.setInput(connectionService);
						packColumns();
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
							viewer.setInput(null);
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
		EntityMediator.removeListener(AddressSalutation.class, this);
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		if (!event.getSelection().isEmpty() && event.getSelection() instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) event.getSelection();
			if (ssel.getFirstElement() instanceof AddressSalutation)
			{
				editSalutation((AddressSalutation) ssel.getFirstElement());
			}
		}
	}

	private void editSalutation(final AddressSalutation salutation)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new AddressSalutationEditorInput(salutation), AddressSalutationEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);
		EntityMediator.addListener(AddressSalutation.class, this);
	}

	private void packColumns()
	{
		TableColumn[] columns = viewer.getTable().getColumns();
		for (TableColumn column : columns)
		{
			column.pack();
		}
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (entity instanceof AddressSalutation)
				{
					viewer.refresh();
				}
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
				if (entity instanceof AddressSalutation)
				{
					viewer.add(entity);
				}
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
				if (entity instanceof AddressSalutation)
				{
					viewer.update(entity, null);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void setFocus()
	{
		viewer.getTable().setFocus();
	}

}
