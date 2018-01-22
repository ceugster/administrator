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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
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
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Teacher;
import ch.eugster.events.persistence.model.VisitTheme;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.views.AbstractEntityView;
import ch.eugster.events.visits.Activator;
import ch.eugster.events.visits.editors.TeacherEditor;
import ch.eugster.events.visits.editors.TeacherEditorInput;

public class TeacherView extends AbstractEntityView implements IDoubleClickListener, EntityListener
{
	private IContextActivation ctxActivation;

	private TreeViewer viewer;

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	@Override
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);
		EntityMediator.addListener(Teacher.class, this);

		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxActivation = ctxService.activateContext("ch.eugster.events.visits.teacher.context");
	}

	@Override
	public void dispose()
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxService.deactivateContext(ctxActivation);

		EntityMediator.removeListener(VisitTheme.class, this);
		super.dispose();
	}

	@Override
	public void postDelete(AbstractEntity entity)
	{
		if (entity instanceof Teacher)
		{
			this.viewer.refresh();
		}
	}

	@Override
	public void postPersist(AbstractEntity entity)
	{
		if (entity instanceof Teacher)
		{
			this.viewer.add(viewer, entity);
		}
	}

	@Override
	public void postUpdate(AbstractEntity entity)
	{
		if (entity instanceof Teacher)
		{
			this.viewer.refresh(entity);
		}
	}

	@Override
	public void createPartControl(Composite parent)
	{
		TableLayout layout = new TableLayout();

		Tree tree = new Tree(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setLayout(layout);

		viewer = new TreeViewer(tree);
		viewer.setContentProvider(new TeacherTreeViewerContentProvider());
		viewer.setLabelProvider(new TeacherTreeViewerLabelProvider());
		viewer.setSorter(new VisitThemeSorter());
		viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		viewer.addDoubleClickListener(this);

		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object element = cell.getElement();
				if (element instanceof Teacher)
				{
					Teacher teacher = (Teacher) element;
					cell.setText(PersonFormatter.getInstance().formatLastnameFirstname(teacher.getLink().getPerson()));
				}
			}
		});
		TreeColumn treeColumn = treeViewerColumn.getColumn();
		treeColumn.setResizable(true);
		treeColumn.setText("Name");

		treeViewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object element = cell.getElement();
				if (element instanceof Teacher)
				{
					Teacher teacher = (Teacher) element;
					cell.setText(teacher.getLink().getAddress().getName());
				}
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setResizable(true);
		treeColumn.setText("Schulhaus");

		treeViewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object element = cell.getElement();
				if (element instanceof Teacher)
				{
					Teacher teacher = (Teacher) element;
					if (teacher.getLink().getAddress() instanceof Address)
					{
						cell.setText(AddressFormatter.getInstance().formatAddressLine(teacher.getLink().getAddress()));
					}
				}
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setResizable(true);
		treeColumn.setText("Adresse");

		treeViewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object element = cell.getElement();
				if (element instanceof Teacher)
				{
					Teacher teacher = (Teacher) element;
					if (teacher.getLink().getAddress() instanceof Address)
					{
						cell.setText(AddressFormatter.getInstance().formatCityLine(teacher.getLink().getAddress()));
					}
				}
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setResizable(true);
		treeColumn.setText("Ort");

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
						TreeColumn[] columns = viewer.getTree().getColumns();
						for (TreeColumn column : columns)
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
		if (object instanceof Teacher)
		{
			editTeacher((Teacher) object);
		}
	}

	private void editTeacher(Teacher teacher)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new TeacherEditorInput(teacher), TeacherEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setFocus()
	{
		this.viewer.getTree().setFocus();
	}

}
