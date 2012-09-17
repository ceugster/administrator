package ch.eugster.events.person.views;

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
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.editors.PersonTitleEditor;
import ch.eugster.events.person.editors.PersonTitleEditorInput;
import ch.eugster.events.ui.views.AbstractEntityView;

public class PersonTitleView extends AbstractEntityView implements IDoubleClickListener
{
	private TableViewer viewer;

	private ServiceTracker connectionServiceTracker;

	private IContextActivation ctxActivation;

	@Override
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);
		EntityMediator.addListener(PersonTitle.class, this);
	}

	@Override
	public void createPartControl(Composite parent)
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxActivation = ctxService.activateContext("ch.eugster.events.person.title.context");

		TableLayout layout = new TableLayout();

		Table table = new Table(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayout(layout);
		table.setHeaderVisible(true);

		viewer = new TableViewer(table);
		viewer.setContentProvider(new PersonTitleContentProvider());
		viewer.setSorter(new PersonTitleSorter());
		viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		viewer.addDoubleClickListener(this);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof PersonTitle)
				{
					cell.setText(((PersonTitle) object).getTitle());
				}
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Titelbezeichnung");

		createContextMenu();

		getSite().setSelectionProvider(viewer);

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
		TableColumn[] columns = viewer.getTable().getColumns();
		for (TableColumn column : columns)
		{
			column.pack();
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
	public void setFocus()
	{
		viewer.getTable().setFocus();
	}

	@Override
	public void doubleClick(DoubleClickEvent event)
	{
		if (!event.getSelection().isEmpty() && event.getSelection() instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) event.getSelection();
			if (ssel.getFirstElement() instanceof PersonTitle)
			{
				editTitle((PersonTitle) ssel.getFirstElement());
			}
		}
	}

	@Override
	public void postPersist(AbstractEntity entity)
	{
		if (entity instanceof PersonTitle)
		{
			viewer.add(entity);
		}
	}

	@Override
	public void postUpdate(AbstractEntity entity)
	{
		if (entity instanceof PersonTitle)
		{
			viewer.update(entity, null);
		}
	}

	@Override
	public void postDelete(AbstractEntity entity)
	{
		if (entity instanceof PersonTitle)
		{
			viewer.refresh();
		}
	}

	private void editTitle(PersonTitle title)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new PersonTitleEditorInput(title), PersonTitleEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void dispose()
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxService.deactivateContext(ctxActivation);

		connectionServiceTracker.close();
		EntityMediator.removeListener(PersonTitle.class, this);
	}

}
