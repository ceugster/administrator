package ch.eugster.events.addressgroup.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
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
import ch.eugster.events.domain.views.DomainContentProvider;
import ch.eugster.events.domain.views.DomainLabelProvider;
import ch.eugster.events.domain.views.DomainSorter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dnd.CourseTransfer;
import ch.eugster.events.ui.dnd.EntityTransfer;
import ch.eugster.events.ui.views.AbstractEntityView;

public class AddressGroupView extends AbstractEntityView implements IDoubleClickListener
{
	public static final String ID = "ch.eugster.events.addressgroup.groupView";

	private ComboViewer domainViewer;

	private Domain singleDomain;

	private TreeViewer addressGroupViewer;

	private IContextActivation ctxActivation;

	private ServiceTracker connectionServiceTracker;

	public AddressGroupView()
	{
		EntityMediator.addListener(Domain.class, this);
		EntityMediator.addListener(AddressGroupCategory.class, this);
		EntityMediator.addListener(AddressGroup.class, this);
		// EntityMediator.addListener(AddressGroupLink.class, this);
		EntityMediator.addListener(AddressGroupMember.class, this);
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

		parent.setLayout(new GridLayout());

		if (PersonSettings.getInstance() != null && PersonSettings.getInstance().getPersonHasDomain())
		{
			Combo combo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
			combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			this.domainViewer = new ComboViewer(combo);
			this.domainViewer.setContentProvider(new DomainContentProvider());
			this.domainViewer.setLabelProvider(new DomainLabelProvider());
			this.domainViewer.setSorter(new DomainSorter());
			this.domainViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
			this.domainViewer.addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(final SelectionChangedEvent event)
				{
					StructuredSelection ssel = (StructuredSelection) event.getSelection();
					if (ssel.isEmpty())
					{
						AddressGroupView.this.addressGroupViewer.setInput(null);
					}
					else
					{
						AddressGroupView.this.addressGroupViewer.setInput(ssel.getFirstElement());
					}
				}
			});
		}

		Tree tree = new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setHeaderVisible(false);

		this.addressGroupViewer = new TreeViewer(tree);
		this.addressGroupViewer.setContentProvider(new AddressGroupContentProvider());
		this.addressGroupViewer.setLabelProvider(new AddressGroupLabelProvider());
		this.addressGroupViewer.setSorter(new AddressGroupSorter());
		this.addressGroupViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.addressGroupViewer.addDoubleClickListener(this);

		Transfer[] transfers = new Transfer[] { EntityTransfer.getTransfer(), CourseTransfer.getTransfer(),
				AddressGroupTransfer.getTransfer() };
		int ops = DND.DROP_MOVE | DND.DROP_COPY;
		this.addressGroupViewer.addDropSupport(ops, transfers, new AddressGroupViewerDropAdapter(
				this.addressGroupViewer));

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.addressGroupViewer);

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
						if (domainViewer == null)
						{
							DomainQuery query = (DomainQuery) connectionService.getQuery(Domain.class);
							Domain[] domains = query.selectAll().toArray(new Domain[0]);
							if (domains.length > 0)
							{
								AddressGroupView.this.singleDomain = domains[0];
								AddressGroupView.this.addressGroupViewer.setInput(singleDomain);
							}
						}
						else
						{
							domainViewer.setInput(connectionService);
							if (User.getCurrent().getDomain() != null)
							{
								domainViewer.setSelection(new StructuredSelection(User.getCurrent().getDomain()));
							}
						}
					}
				});
				return connectionService;
			}

			@Override
			public void removedService(final ServiceReference reference, final Object service)
			{
				if (domainViewer == null)
				{
					AddressGroupView.this.singleDomain = null;
					if (AddressGroupView.this.addressGroupViewer.getContentProvider() != null)
					{
						AddressGroupView.this.addressGroupViewer.setInput(singleDomain);
					}
				}
				else
				{
					if (domainViewer.getContentProvider() != null)
					{
						domainViewer.setInput(null);
					}
				}
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

	private boolean isVisible(final Domain domain, final AddressGroupCategory category)
	{
		if (category.getDomain() == null)
		{
			if (domain instanceof Domain)
			{
				if (category.getDomain().getId().equals(domain.getId()))
				{
					return true;
				}
			}
		}
		else
		{
			if (domain instanceof Domain)
			{
				if (category.getDomain().getId().equals(domain.getId()))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		if (entity instanceof AddressGroupCategory)
		{
			refreshViewer();
		}
		else if (entity instanceof AddressGroup)
		{
			refreshViewer();
		}
	}

	@Override
	public void postPersist(final AbstractEntity entity)
	{
		Domain domain = (Domain) addressGroupViewer.getInput();
		if (entity instanceof AddressGroupCategory)
		{
			AddressGroupCategory category = (AddressGroupCategory) entity;
			if (category.getDomain() == null)
			{
				if (domain instanceof Domain)
				{
					if (category.getDomain().getId().equals(domain.getId()))
					{
						refreshViewer();
					}
				}
			}
			else
			{
				if (domain instanceof Domain)
				{
					if (category.getDomain().getId().equals(domain.getId()))
					{
						refreshViewer();
					}
				}
			}
		}
		else if (entity instanceof AddressGroup)
		{
			AddressGroup addressGroup = (AddressGroup) entity;
			if (addressGroup.getAddressGroupCategory().getDomain() == null)
			{
				if (domain instanceof Domain)
				{
					if (addressGroup.getAddressGroupCategory().getDomain().getId().equals(domain.getId()))
					{
						refreshViewer();
					}
				}
			}
			else
			{
				if (domain instanceof Domain)
				{
					if (addressGroup.getAddressGroupCategory().getDomain().getId().equals(domain.getId()))
					{
						refreshViewer();
					}
				}
			}
		}
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
		Domain domain = (Domain) addressGroupViewer.getInput();
		if (entity instanceof Domain)
		{
			if (domainViewer != null)
			{
				this.domainViewer.refresh(entity);
			}
		}
		else if (entity instanceof AddressGroupCategory)
		{
			AddressGroupCategory category = (AddressGroupCategory) entity;
			if (isVisible(domain, category))
			{
				refreshViewer(category);
			}
		}
		else if (entity instanceof AddressGroup)
		{
			AddressGroup addressGroup = (AddressGroup) entity;
			if (isVisible(domain, addressGroup.getAddressGroupCategory()))
			{
				refreshViewer(addressGroup);
			}
		}
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
}