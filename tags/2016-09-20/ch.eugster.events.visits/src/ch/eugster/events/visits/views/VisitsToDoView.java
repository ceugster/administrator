package ch.eugster.events.visits.views;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
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
import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.model.VisitTheme;
import ch.eugster.events.persistence.model.VisitVisitor;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.views.AbstractEntityView;
import ch.eugster.events.visits.Activator;
import ch.eugster.events.visits.editors.VisitEditor;
import ch.eugster.events.visits.editors.VisitEditorInput;

public class VisitsToDoView extends AbstractEntityView implements IDoubleClickListener, EntityListener
{
	private final Map<Long, Color> colors = new HashMap<Long, Color>();

	private IContextActivation ctxActivation;

	private TableViewer viewer;

	private ServiceTracker connectionServiceTracker;

	@Override
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);
		EntityMediator.addListener(Visit.class, this);

		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxActivation = ctxService.activateContext("ch.eugster.events.visits.todo.context");
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
		}
	}

	@Override
	public void postUpdate(AbstractEntity entity)
	{
		if (entity instanceof Visit)
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
		viewer.setContentProvider(new VisitTableContentProvider());
		viewer.setSorter(new ViewerSorter()
		{
			@Override
			public int compare(Viewer viewer, Object e1, Object e2)
			{
				Visit v1 = (Visit) e1;
				Visit v2 = (Visit) e2;
				Calendar start1 = v1.getStart();
				Calendar start2 = v2.getStart();
				if (start1 == null)
				{
					return 1;
				}
				if (start2 == null)
				{
					return -1;
				}
				return start1.compareTo(start2);
			}
		});
		viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter(), new VisitTodoFilter() });
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
					Visit visit = (Visit) object;
					String period = visit.getFormattedPeriod();
					cell.setText(period);
					if (period.isEmpty())
					{
						cell.setBackground(getErrorBackground());
					}
					else if (visit.getTheme() != null && visit.getTheme().getColor() != null)
					{
						cell.setBackground(getBackground(visit.getTheme()));
					}
				}
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Schulbesuch");

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
					VisitTheme theme = visit.getTheme();
					cell.setText(theme == null ? "" : theme.getName());
					if (theme == null)
					{
						cell.setBackground(getErrorBackground());
					}
					else if (visit.getTheme().getColor() != null)
					{
						cell.setBackground(getBackground(visit.getTheme()));
					}
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Thema");

		for (final VisitVisitor.VisitorType visitorType : VisitVisitor.VisitorType.values())
		{
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
						Collection<VisitVisitor> visitors = visit.getVisitors();
						if (visitors.isEmpty())
						{
							cell.setBackground(getErrorBackground());
						}
						else
						{
							for (VisitVisitor visitor : visitors)
							{
								if (!visitor.isDeleted() && visitor.getType().equals(visitorType))
								{
									cell.setText(PersonFormatter.getInstance().formatLastnameFirstname(
											visitor.getVisitor().getLink().getPerson()));
								}
							}
							cell.setBackground(getBackground(visit.getTheme()));
						}
					}
				}
			});
			tableColumn = tableViewerColumn.getColumn();
			tableColumn.setResizable(true);
			tableColumn.setText(visitorType.label());
		}

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
					if (visit.getTeacher() == null)
					{
						cell.setBackground(getErrorBackground());
					}
					else
					{
						cell.setText(PersonFormatter.getInstance().formatLastnameFirstname(
								visit.getTeacher().getLink().getPerson()));
						cell.setBackground(getBackground(visit.getTheme()));
					}
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Lehrperson");

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
					if (visit.getSchoolClass() == null)
					{
						cell.setBackground(getErrorBackground());
					}
					else
					{
						cell.setText(visit.getSchoolClass().getName());
						cell.setBackground(getBackground(visit.getTheme()));
					}
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Schulklasse");

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
					cell.setText(Integer.valueOf(visit.getPupils()).toString());
					if (visit.getPupils() == 0)
					{
						cell.setBackground(getErrorBackground());
					}
					else
					{
						cell.setBackground(getBackground(visit.getTheme()));
					}
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Klassengrösse");

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
					if (visit.getTeacher() == null)
					{
						cell.setBackground(getErrorBackground());
					}
					else
					{
						cell.setText(AddressFormatter.getInstance().formatCityLine(visit.getTeacher().getLink().getAddress()));
						cell.setBackground(getBackground(visit.getTheme()));
					}
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Ort");

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
					if (visit.getTeacher() == null)
					{
						cell.setBackground(getErrorBackground());
					}
					else
					{
						cell.setText(visit.getTeacher().getLink().getAddress().getProvince());
						cell.setBackground(getBackground(visit.getTheme()));
					}
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Kanton");

		createContextMenu();

		getSite().setSelectionProvider(viewer);

		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(ServiceReference reference)
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
			public void removedService(ServiceReference reference, Object service)
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
		if (object instanceof Visit)
		{
			editVisit((Visit) object);
		}
	}

	private Color getErrorBackground()
	{
		return viewer.getTable().getDisplay().getSystemColor(SWT.COLOR_RED);
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

	private Color getBackground(VisitTheme theme)
	{
		if (theme == null || theme.getColor() == null)
		{
			return this.viewer.getTable().getDisplay().getSystemColor(SWT.COLOR_WHITE);
		}

		Color color = colors.get(theme.getId());
		if (color == null)
		{
			java.awt.Color c = new java.awt.Color(theme.getColor().intValue());
			color = new Color(this.viewer.getTable().getDisplay(), new RGB(c.getRed(), c.getGreen(), c.getBlue()));
			colors.put(theme.getId(), color);
		}
		return color;
	}

	@Override
	public void setFocus()
	{
		this.viewer.getTable().setFocus();
	}

}
