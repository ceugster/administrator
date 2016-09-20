package ch.eugster.events.season.views;

import java.text.DateFormat;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
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
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.season.Activator;
import ch.eugster.events.season.editors.SeasonEditor;
import ch.eugster.events.season.editors.SeasonEditorInput;
import ch.eugster.events.ui.views.AbstractEntityView;

public class SeasonView extends AbstractEntityView implements IDoubleClickListener
{
	public static final String ID = "ch.eugster.events.season.view";

	private static DateFormat df = DateFormat.getDateInstance();

	private TableViewer viewer;

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	private IContextActivation ctxActivation;

	public SeasonView()
	{
		EntityMediator.addListener(Season.class, this);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent)
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxActivation = ctxService.activateContext("ch.eugster.events.season.context");

		TableLayout layout = new TableLayout();

		Table table = new Table(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setLayout(layout);
		table.setHeaderVisible(true);

		this.viewer = new TableViewer(table);
		this.viewer.setContentProvider(new SeasonContentProvider());
		this.viewer.setSorter(new SeasonSorter());
		this.viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.viewer.addDoubleClickListener(this);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Season)
				{
					Season season = (Season) object;
					if (season.isClosed())
						cell.setImage(Activator.getDefault().getImageRegistry().get("SEASON_ORANGE"));
					else
						cell.setImage(Activator.getDefault().getImageRegistry().get("SEASON_GREEN"));

					cell.setText(((Season) object).getCode());
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
				if (object instanceof Season)
				{
					cell.setText(((Season) object).getTitle());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Bezeichnung");

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				Season season = (Season) object;
				if (season.getStart() == null)
					cell.setText("");
				else
				{
					String start = SeasonView.df.format(season.getStart().getTime());
					cell.setText(start);
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Saisonbeginn");

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Season)
				{
					Season season = (Season) object;
					if (season.getEnd() == null)
						cell.setText("");
					else
					{
						String end = SeasonView.df.format(season.getEnd().getTime());
						cell.setText(end);
					}
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Saisonende");

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Season)
				{
					Season season = (Season) object;
					if (season.isClosed())
						cell.setImage(null);
					else
						cell.setImage(Activator.getDefault().getImageRegistry().get("CHECKED"));
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Aktuell");

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.viewer);

		connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null)
		{
			@Override
			public ConnectionService addingService(ServiceReference<ConnectionService> reference)
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
			public void removedService(ServiceReference<ConnectionService> reference, ConnectionService service)
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
		if (object instanceof Season)
		{
			this.editSeason((Season) object);
		}
	}

	private void editSeason(Season season)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new SeasonEditorInput(season), SeasonEditor.ID, true);
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
	public void postPersist(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (entity instanceof Season)
				{
					viewer.add(entity);
					packColumns();
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
				if (entity instanceof Season)
				{
					viewer.refresh(entity);
					packColumns();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
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
				{
					viewer.refresh();
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
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxService.deactivateContext(ctxActivation);

		connectionServiceTracker.close();

		EntityMediator.removeListener(Season.class, this);
		super.dispose();
	}
}