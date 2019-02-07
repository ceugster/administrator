package ch.eugster.events.donation.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
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
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
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

import ch.eugster.events.donation.Activator;
import ch.eugster.events.donation.editors.DonationPurposeEditor;
import ch.eugster.events.donation.editors.DonationPurposeEditorInput;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.views.AbstractEntityView;

public class DonationPurposeView extends AbstractEntityView implements IDoubleClickListener
{
	public static final String ID = "ch.eugster.events.donation.purpose.view";

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	private TableViewer viewer;

	private IContextActivation ctxActivation;

	public DonationPurposeView()
	{
		EntityMediator.addListener(DonationPurpose.class, this);
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(final Composite parent)
	{
		final IContextService ctxService = (IContextService) this.getSite().getService(IContextService.class);
		this.ctxActivation = ctxService.activateContext("ch.eugster.events.donation.purpose.context");

		final TableLayout layout = new TableLayout();

		final Table table = new Table(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayout(layout);
		table.setHeaderVisible(true);

		this.viewer = new TableViewer(table);
		this.viewer.setContentProvider(new DonationPurposeContentProvider());
		this.viewer.setSorter(new DonationPurposeSorter());
		this.viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.viewer.addDoubleClickListener(this);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				final Object object = cell.getElement();
				if (object instanceof DonationPurpose)
				{
					cell.setText(((DonationPurpose) object).getCode());
					// cell.setImage(Activator.getDefault().getImageRegistry().get("DOMAIN"));
				}
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Code");

		layout.addColumnData(new ColumnWeightData(50, true));

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				final Object object = cell.getElement();
				if (object instanceof DonationPurpose)
				{
					cell.setText(((DonationPurpose) object).getName());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Bezeichnung");

		layout.addColumnData(new ColumnWeightData(200, true));

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.viewer);

		this.connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null)
		{
			@Override
			public ConnectionService addingService(final ServiceReference<ConnectionService> reference)
			{
				final ConnectionService connectionService = super.addingService(reference);
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
						DonationPurposeView.this.viewer.setInput(connectionService);
						DonationPurposeView.this.packColumns();
					}
				});
				return connectionService;
			}

			@Override
			public void removedService(final ServiceReference<ConnectionService> reference, final ConnectionService service)
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
						if (DonationPurposeView.this.viewer.getContentProvider() != null)
						{
							DonationPurposeView.this.viewer.setInput(null);
						}
					}
				});
				super.removedService(reference, service);
			}

		};
		this.connectionServiceTracker.open();

		final int ops = DND.DROP_MOVE;
		final Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };
		this.viewer.addDragSupport(ops, transfers, new DonationPurposeViewerDragListener(this.viewer));
		this.viewer.addDropSupport(ops, transfers, new DonationPurposeViewerDropListener(this.viewer, this.connectionServiceTracker.getService()));
	}

	private void packColumns()
	{
		final TableColumn[] tableColumns = this.viewer.getTable().getColumns();
		for (final TableColumn tableColumn : tableColumns)
		{
			tableColumn.pack();
		}

	}

	private void createContextMenu()
	{
		final MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		final Menu menu = menuManager.createContextMenu(this.viewer.getControl());
		this.viewer.getControl().setMenu(menu);

		this.getSite().registerContextMenu(menuManager, this.viewer);
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		final ISelection selection = event.getSelection();
		final Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof DonationPurpose)
		{
			this.editDonationPurpose((DonationPurpose) object);
		}
	}

	private void editDonationPurpose(final DonationPurpose purpose)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new DonationPurposeEditorInput(purpose), DonationPurposeEditor.ID, true);
		}
		catch (final PartInitException e)
		{
			e.printStackTrace();
		}
	}

	public TableViewer getViewer()
	{
		return this.viewer;
	}

	@Override
	public void postPersist(final AbstractEntity entity)
	{
		final UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				if (entity instanceof DonationPurpose)
				{
					DonationPurposeView.this.viewer.add(entity);
					DonationPurposeView.this.packColumns();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
		final UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				if (entity instanceof DonationPurpose)
				{
					DonationPurposeView.this.viewer.refresh(entity);
					DonationPurposeView.this.packColumns();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		final UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				if (entity instanceof DonationPurpose)
				{
					DonationPurposeView.this.viewer.refresh();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
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
		final IContextService ctxService = (IContextService) this.getSite().getService(IContextService.class);
		ctxService.deactivateContext(this.ctxActivation);

		this.connectionServiceTracker.close();
		EntityMediator.removeListener(DonationPurpose.class, this);
		super.dispose();
	}
}