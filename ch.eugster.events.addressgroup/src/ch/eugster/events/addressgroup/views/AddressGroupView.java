package ch.eugster.events.addressgroup.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.dnd.AddressGroupTransfer;
import ch.eugster.events.addressgroup.dnd.AddressGroupViewerDropAdapter;
import ch.eugster.events.addressgroup.editors.AddressGroupEditor;
import ch.eugster.events.addressgroup.editors.AddressGroupEditorInput;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dnd.CourseTransfer;
import ch.eugster.events.ui.dnd.EntityTransfer;
import ch.eugster.events.ui.views.AbstractEntityView;

public class AddressGroupView extends AbstractEntityView implements IDoubleClickListener
{
	public static final String ID = "ch.eugster.events.addressgroup.groupView";

	private IDialogSettings settings;
	
	private Text filterExpression;
	
	private Button clearFilterExpression;
	
	private TreeViewer addressGroupViewer;

	private IContextActivation ctxActivation;

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	public AddressGroupView()
	{
		EntityMediator.addListener(Domain.class, this);
		EntityMediator.addListener(AddressGroupCategory.class, this);
		EntityMediator.addListener(AddressGroup.class, this);
		EntityMediator.addListener(AddressGroupMember.class, this);
		
		settings = Activator.getDefault().getDialogSettings().getSection("addressgroup.view");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("addressgroup.view");
		}
		if (settings.get("filter.expression") == null)
		{
			settings.put("filter.expression", "");
		}
	}

	public AddressGroupCategory getSelectedCategory()
	{
		IStructuredSelection ssel = (IStructuredSelection) addressGroupViewer.getSelection();
		return ssel.getFirstElement() instanceof AddressGroupCategory ? (AddressGroupCategory) ssel.getFirstElement()
				: null;
	}

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		IMenuListener listener = new IMenuListener()
		{
			@Override
			public void menuAboutToShow(IMenuManager manager)
			{
				IContributionItem[] items = manager.getItems();
				for (IContributionItem item : items)
				{
					System.out.println(item.getId());
				}
			}
		};
		menuManager.addMenuListener(listener);

		Menu menu = menuManager.createContextMenu(this.addressGroupViewer.getControl());
		this.addressGroupViewer.getControl().setMenu(menu);

		this.getSite().registerContextMenu(menuManager, this.addressGroupViewer);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(final Composite parent)
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxActivation = ctxService.activateContext("ch.eugster.events.addressgroup.context");

		parent.setLayout(new GridLayout(3, false));

		Label label = new Label(parent, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Filter");
		
		filterExpression = new Text(parent, SWT.BORDER | SWT.SINGLE);
		filterExpression.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterExpression.setText(settings.get("filter.expression"));
		filterExpression.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e) 
			{
				if (addressGroupViewer != null)
				{
					addressGroupViewer.refresh();
				}
			}
		});
		
		clearFilterExpression = new Button(parent, SWT.PUSH);
		clearFilterExpression.setLayoutData(new GridData());
		clearFilterExpression.setImage(Activator.getDefault().getImageRegistry().get("CLEAR"));
		clearFilterExpression.setToolTipText("Filter leeren");
		clearFilterExpression.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				filterExpression.setText("");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;

		Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		tree.setLayoutData(gridData);
		tree.setHeaderVisible(false);

		this.addressGroupViewer = new TreeViewer(tree);
		this.addressGroupViewer.setContentProvider(new AddressGroupContentProvider());
		this.addressGroupViewer.setLabelProvider(new AddressGroupLabelProvider());
		this.addressGroupViewer.setSorter(new AddressGroupSorter());
		this.addressGroupViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter(), new ExpressionFilter() });
		this.addressGroupViewer.addDoubleClickListener(this);

		Transfer[] transfers = new Transfer[] { EntityTransfer.getTransfer(), CourseTransfer.getTransfer(),
				AddressGroupTransfer.getTransfer() };
		int ops = DND.DROP_MOVE | DND.DROP_COPY;
		this.addressGroupViewer.addDropSupport(ops, transfers, new AddressGroupViewerDropAdapter(
				this.addressGroupViewer));

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.addressGroupViewer);

		connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null)
		{
			@Override
			public ConnectionService addingService(final ServiceReference<ConnectionService> reference)
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
						AddressGroupView.this.addressGroupViewer.setInput(connectionService);
					}
				});
				return connectionService;
			}

			@Override
			public void removedService(final ServiceReference<ConnectionService> reference, final ConnectionService service)
			{
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
		if (connectionServiceTracker != null)
			connectionServiceTracker.close();
		EntityMediator.removeListener(Domain.class, this);
		EntityMediator.removeListener(AddressGroupCategory.class, this);
		EntityMediator.removeListener(AddressGroup.class, this);
		// EntityMediator.removeListener(AddressGroupLink.class, this);
		EntityMediator.removeListener(AddressGroupMember.class, this);
		super.dispose();
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof AddressGroupCategory)
		{
			this.addressGroupViewer.expandToLevel(object, 1);
		}
		else if (object instanceof AddressGroup)
		{
			AddressGroup addressGroup = (AddressGroup) object;
			try
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.openEditor(new AddressGroupEditorInput(addressGroup), AddressGroupEditor.ID);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
	}

	public TreeViewer getViewer()
	{
		return this.addressGroupViewer;
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		refreshViewer();
	}

	@Override
	public void postPersist(final AbstractEntity entity)
	{
		UIJob job = new UIJob("Aktualisiere Sicht...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				if (entity instanceof Domain)
				{
					AddressGroupView.this.addressGroupViewer.add(null, entity);
				}
				else if (entity instanceof AddressGroupCategory)
				{
					AddressGroupCategory category = (AddressGroupCategory) entity;
					AddressGroupView.this.addressGroupViewer.add(category.getDomain(), category);
				}
				else if (entity instanceof AddressGroup)
				{
					AddressGroup group = (AddressGroup) entity;
					AddressGroupView.this.addressGroupViewer.add(group.getAddressGroupCategory(), group);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
		this.refreshViewer(entity);
	}

	private void refreshViewer()
	{
		UIJob job = new UIJob("Aktualisiere Sicht...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				addressGroupViewer.refresh();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void refreshViewer(final Object object)
	{
		UIJob job = new UIJob("Aktualisiere Sicht...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				addressGroupViewer.refresh(object);
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
		this.addressGroupViewer.getControl().setFocus();
	}
	
	private class ExpressionFilter extends ViewerFilter
	{
		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) 
		{
			if (filterExpression.getText().isEmpty())
			{
				return true;
			}
			if (element instanceof AddressGroup)
			{
				AddressGroup addressGroup = (AddressGroup) element;
				if (addressGroup.getCode().toLowerCase().contains(filterExpression.getText().toLowerCase()))
				{
					return true;
				}
				if (addressGroup.getName().toLowerCase().contains(filterExpression.getText().toLowerCase()))
				{
					return true;
				}
				return false;
			}
			return true;
		}
	}
}