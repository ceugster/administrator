package ch.eugster.events.charity.views;

import java.text.SimpleDateFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.charity.Activator;
import ch.eugster.events.charity.editors.CharityRunEditor;
import ch.eugster.events.charity.editors.CharityRunEditorInput;
import ch.eugster.events.charity.editors.CharityRunnerEditor;
import ch.eugster.events.charity.editors.CharityRunnerEditorInput;
import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityListener;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.CharityRun;
import ch.eugster.events.persistence.model.CharityRunner;
import ch.eugster.events.persistence.queries.CharityRunQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class CharityRunView extends ViewPart implements IDoubleClickListener
{
	private TreeViewer viewer;
	
	private ServiceTracker<ConnectionService, ConnectionService> tracker;
	
	private ConnectionService connectionService;

	private EntityListener charityRunListener;
	
	private EntityListener charityRunnerListener;
	
	public CharityRunView()
	{
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

		Menu menu = menuManager.createContextMenu(this.viewer.getControl());
		this.viewer.getControl().setMenu(menu);

		this.getSite().registerContextMenu(menuManager, this.viewer);
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof CharityRun)
		{
			CharityRun charityRun = (CharityRun) object;
			try
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.openEditor(new CharityRunEditorInput(charityRun), CharityRunEditor.ID);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
		else if (object instanceof CharityRunner)
		{
			CharityRunner charityRunner = (CharityRunner) object;
			try
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.openEditor(new CharityRunnerEditorInput(charityRunner), CharityRunnerEditor.ID);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);
		tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundleContext(), ConnectionService.class, null) {

			@Override
			public ConnectionService addingService(
					ServiceReference<ConnectionService> reference) 
			{
				connectionService = super.addingService(reference);
				return connectionService;
			}

			@Override
			public void modifiedService(
					ServiceReference<ConnectionService> reference,
					ConnectionService service) 
			{
				connectionService = service;
				super.modifiedService(reference, service);
			}

			@Override
			public void removedService(
					ServiceReference<ConnectionService> reference,
					ConnectionService service) 
			{
				connectionService = null;
				super.removedService(reference, service);
			}
		};
		tracker.open();
		
		this.charityRunListener = new EntityAdapter() 
		{
			@Override
			public void postPersist(AbstractEntity entity) 
			{
				UIJob job = new UIJob("Adding new charity run") 
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) 
					{
						viewer.refresh();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public void postUpdate(AbstractEntity entity) 
			{
				UIJob job = new UIJob("Updating charity run") 
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) 
					{
						viewer.refresh();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public void postDelete(AbstractEntity entity) 
			{
				UIJob job = new UIJob("Adding new charity run") 
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) 
					{
						viewer.refresh();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		};
		EntityMediator.addListener(CharityRun.class, charityRunListener);

		this.charityRunnerListener = new EntityAdapter() 
		{
			@Override
			public void postPersist(final AbstractEntity entity) 
			{
				UIJob job = new UIJob("Adding new charity runner") 
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) 
					{
						CharityRunner runner = (CharityRunner) entity;
						viewer.refresh(runner.getCharityRun());
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public void postUpdate(final AbstractEntity entity) 
			{
				UIJob job = new UIJob("Updating charity runner") 
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) 
					{
						CharityRunner runner = (CharityRunner) entity;
						if (runner.getLeader() == null)
						{
							viewer.refresh(runner.getCharityRun());
						}
						else
						{
							viewer.refresh(runner.getLeader());
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public void postDelete(final AbstractEntity entity) 
			{
				UIJob job = new UIJob("Removing charity runner") 
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) 
					{
						CharityRunner runner = (CharityRunner) entity;
						if (runner.getLeader() == null)
						{
							viewer.refresh(runner.getCharityRun());
						}
						else
						{
							viewer.refresh(runner.getLeader());
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		};
		EntityMediator.addListener(CharityRunner.class, charityRunnerListener);
}

	@Override
	public void createPartControl(Composite parent) 
	{
		parent.setLayout(new FillLayout());
		
		Composite composite = new Composite(parent, SWT.None);
		composite.setLayout(new GridLayout());
		
		Tree tree = new Tree(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer = new TreeViewer(tree);
		viewer.setContentProvider(new ITreeContentProvider() 
		{
			@Override
			public void dispose() 
			{
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) 
			{
			}

			@Override
			public Object[] getElements(Object inputElement) 
			{
				return getChildren(inputElement);
			}

			@Override
			public Object[] getChildren(Object parentElement) 
			{
				if (parentElement instanceof ConnectionService)
				{
					ConnectionService service = (ConnectionService) parentElement;
					CharityRunQuery query = (CharityRunQuery) service.getQuery(CharityRun.class);
					return query.selectActives().toArray(new CharityRun[0]);
				}
				else if (parentElement instanceof CharityRun)
				{
					CharityRun charityRun = (CharityRun) parentElement;
					return charityRun.getRunners().toArray(new CharityRunner[0]);
				}
				else if (parentElement instanceof CharityRunner)
				{
					CharityRunner runner = (CharityRunner) parentElement;
					return runner.getRunners().toArray(new CharityRunner[0]);
				}
				return null;
			}

			@Override
			public Object getParent(Object element) 
			{
				if (element instanceof CharityRunner)
				{
					CharityRunner runner = (CharityRunner) element;
					return runner.getLeader() == null ? runner.getCharityRun() : runner.getLeader();
				}
				return null;
			}

			@Override
			public boolean hasChildren(Object element) 
			{
				if (element instanceof ConnectionService)
				{
					ConnectionService service = (ConnectionService) element;
					CharityRunQuery query = (CharityRunQuery) service.getQuery(CharityRun.class);
					return query.selectActives().size() > 0;
				}
				else if (element instanceof CharityRun)
				{
					CharityRun charityRun = (CharityRun) element;
					return charityRun.getRunners().size() > 0;
				}
				else if (element instanceof CharityRunner)
				{
					CharityRunner runner = (CharityRunner) element;
					return runner.getRunners().size() > 0;
				}
				return false;
			}
		});
		this.viewer.setLabelProvider(new LabelProvider() 
		{
			@Override
			public Image getImage(Object element) 
			{
				if (element instanceof CharityRun)
				{
					return Activator.getDefault().getImageRegistry().get("RUNNER");
				}
				return null;
			}

			@Override
			public String getText(Object element) 
			{
				if (element instanceof CharityRun)
				{
					CharityRun charityRun = (CharityRun) element;
					return charityRun.getName() + (charityRun.getDate() == null ? "" : " " + SimpleDateFormat.getDateTimeInstance().format(charityRun.getDate().getTime()) + (charityRun.getPlace().isEmpty() ? "" : " " + charityRun.getPlace()));
				}
				else if (element instanceof CharityRunner)
				{
					CharityRunner runner = (CharityRunner) element;
					if (runner.getGroupName().isEmpty())
					{
						return runner.getPerson().getLastname() + " " + runner.getPerson().getFirstname() + " " + runner.getPerson().getStreet() + " " + runner.getPerson().getZip() + " " + runner.getPerson().getCity();
					}
					else
					{
						return runner.getGroupName() + " (" + runner.getPerson().getLastname() + " " + runner.getPerson().getFirstname() + " " + runner.getPerson().getStreet() + " " + runner.getPerson().getZip() + " " + runner.getPerson().getCity() + ")";
					}
				}
				return "";
			}
		});
		this.viewer.setInput(this.connectionService);
		this.viewer.addDoubleClickListener(this);

		this.createContextMenu();
		
		this.getSite().setSelectionProvider(this.viewer);
	}
	
	public TreeViewer getViewer()
	{
		return this.viewer;
	}

	@Override
	public void setFocus() 
	{
		this.viewer.getTree().setFocus();
	}

	@Override
	public void dispose() 
	{
		this.tracker.close();
		this.connectionService = null;
		EntityMediator.removeListener(CharityRun.class, this.charityRunListener);
		EntityMediator.removeListener(CharityRunner.class, this.charityRunnerListener);
		super.dispose();
	}
}
