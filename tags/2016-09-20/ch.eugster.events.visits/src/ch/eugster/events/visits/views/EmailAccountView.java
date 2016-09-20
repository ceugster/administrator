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
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
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
import ch.eugster.events.persistence.model.EmailAccount;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.views.AbstractEntityView;
import ch.eugster.events.visits.Activator;
import ch.eugster.events.visits.editors.EmailAccountEditor;
import ch.eugster.events.visits.editors.EmailAccountEditorInput;

public class EmailAccountView extends AbstractEntityView implements IDoubleClickListener, EntityListener
{
	private IContextActivation ctxActivation;

	private TableViewer viewer;

	private ServiceTracker connectionServiceTracker;

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		getSite().registerContextMenu(menuManager, viewer);
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		TableLayout layout = new TableLayout();

		Table table = new Table(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayout(layout);
		table.setHeaderVisible(true);

		viewer = new TableViewer(table);
		viewer.setContentProvider(new EmailAccountContentProvider());
		viewer.setSorter(new EmailAccountSorter());
		viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		viewer.addDoubleClickListener(this);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof EmailAccount)
				{
					cell.setText(((EmailAccount) object).getUsername());
					cell.setImage(Activator.getDefault().getImageRegistry().get("at"));
				}
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Benutzername");

		tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof EmailAccount)
				{
					cell.setText(((EmailAccount) object).getType().label());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Typ");

		tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof EmailAccount)
				{
					cell.setText(((EmailAccount) object).getHost());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Host");

		tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof EmailAccount)
				{
					cell.setText(Integer.valueOf(((EmailAccount) object).getPort()).toString());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Port");

		tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof EmailAccount)
				{
					cell.setText(((EmailAccount) object).isAuth() ? "Ja" : "Nein");
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Auth");

		tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof EmailAccount)
				{
					cell.setText(((EmailAccount) object).isStarttlsEnable() ? "Ja" : "Nein");
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("STARTTLS");

		tableViewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof EmailAccount)
				{
					cell.setText(((EmailAccount) object).isSslEnable() ? "Ja" : "Nein");
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("SSL");

		createContextMenu();

		getSite().setSelectionProvider(viewer);

		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(final ServiceReference reference)
			{
				final ConnectionService connectionService = (ConnectionService) super.addingService(reference);
				UIJob job = new UIJob("Loading data...")
				{
					@Override
					public IStatus runInUIThread(final IProgressMonitor monitor)
					{
						viewer.setInput(connectionService);
						pack();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
				return connectionService;
			}

			@Override
			public void removedService(final ServiceReference reference, final Object service)
			{
				UIJob job = new UIJob("Removing data...")
				{
					@Override
					public IStatus runInUIThread(final IProgressMonitor monitor)
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

	@Override
	public void dispose()
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxService.deactivateContext(ctxActivation);

		EntityMediator.removeListener(EmailAccount.class, this);
		super.dispose();
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof EmailAccount)
		{
			editEmailAccount((EmailAccount) object);
		}
	}

	private void editEmailAccount(final EmailAccount EmailAccount)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new EmailAccountEditorInput(EmailAccount), EmailAccountEditor.ID, true);
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
		EntityMediator.addListener(EmailAccount.class, this);

		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxActivation = ctxService.activateContext("ch.eugster.events.visits.email.context");
	}

	private void pack()
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
		if (entity instanceof EmailAccount)
		{
			this.viewer.refresh();
		}
	}

	@Override
	public void postPersist(final AbstractEntity entity)
	{
		if (entity instanceof EmailAccount)
		{
			this.viewer.add(entity);
			pack();
		}
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
		if (entity instanceof EmailAccount)
		{
			this.viewer.refresh(entity);
			pack();
		}
	}

	@Override
	public void setFocus()
	{
		this.viewer.getTable().setFocus();
	}

}
