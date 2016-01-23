package ch.eugster.events.person.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Bank;
import ch.eugster.events.persistence.model.BankAccount;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.dialogs.BankAccountDialog;
import ch.eugster.events.ui.views.AbstractEntityView;

public class PersonBankAccountView extends AbstractEntityView implements ISelectionListener
{
	public static final String ID = "ch.eugster.events.bank.account.personView";

	private TableViewer viewer;

	private IDialogSettings settings;

	public PersonBankAccountView()
	{
		System.out.println();
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
	public void createPartControl(final Composite parent)
	{
		TableLayout layout = new TableLayout();

		Table table = new Table(parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLayout(layout);
		table.setHeaderVisible(true);

		this.viewer = new TableViewer(table);
		this.viewer.setContentProvider(new PersonBankAccountContentProvider());
		this.viewer.setSorter(new PersonBankAccountSorter());
		this.viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(final DoubleClickEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				if (ssel.getFirstElement() instanceof BankAccount)
				{
					BankAccount account = (BankAccount) ssel.getFirstElement();
					BankAccountDialog dialog = new BankAccountDialog(PersonBankAccountView.this.getSite().getShell(), account);
					dialog.open();
				}
			}
		});

		TableViewerColumn tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof BankAccount)
				{
					BankAccount account = (BankAccount) object;
					cell.setText(account.getIban());
				}
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("IBAN");

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof BankAccount)
				{
					BankAccount account = (BankAccount) object;
					cell.setText(account.getAccountNumber());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Kontonummer");

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof BankAccount)
				{
					BankAccount account = (BankAccount) object;
					Bank bank = account.getBank();
					cell.setText(bank == null ? "" : bank.getInstitute());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Bankverbindung");

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.viewer);

		this.getSite().getPage().addSelectionListener(PersonView.ID, this);

		PersonView view = (PersonView) this.getSite().getPage().findView(PersonView.ID);
		if (view != null)
		{
			IStructuredSelection ssel = (IStructuredSelection) view.getViewer().getSelection();
			this.viewer.setInput(ssel.getFirstElement());
		}

	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(LinkPersonAddress.class, this);
		EntityMediator.removeListener(BankAccount.class, this);
		EntityMediator.removeListener(Person.class, this);
		this.getSite().getPage().removeSelectionListener(PersonView.ID, this);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(final Class adaptable)
	{
		if (adaptable.equals(this.viewer.getClass()))
		{
			return this.viewer;
		}
		return null;
	}

	public TableViewer getViewer()
	{
		return viewer;
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		settings = Activator.getDefault().getDialogSettings().getSection("person.bank.account.view");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("person.bank.account.view");
		}

		EntityMediator.addListener(LinkPersonAddress.class, this);
		EntityMediator.addListener(BankAccount.class, this);
		EntityMediator.addListener(Person.class, this);

		super.init(site);
	}

	public void packColumns()
	{
		TableColumn[] columns = this.viewer.getTable().getColumns();
		for (TableColumn column : columns)
			column.pack();
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (entity instanceof BankAccount)
				{
					viewer.refresh();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void postLoad(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (entity instanceof BankAccount)
				{
					viewer.refresh();
				}
				else if (entity instanceof Person)
				{
					if (viewer.getInput() instanceof Person)
					{
						Person input = (Person) viewer.getInput();
						if (entity.getId().equals(input.getId()))
						{
							viewer.setInput(entity);
							viewer.refresh();
							packColumns();
						}
					}
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
				if (entity instanceof BankAccount)
				{
					Person person = null;
					if (viewer.getInput() instanceof Person)
					{
						person = (Person) viewer.getInput();
					}
					if (viewer.getInput() instanceof LinkPersonAddress)
					{
						person = ((LinkPersonAddress) viewer.getInput()).getPerson();
					}
					if (((BankAccount) entity).getPerson().getId().equals(person.getId()))
					{
						viewer.add(entity);
						packColumns();
					}
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
				if (entity instanceof BankAccount)
				{
					if (viewer.getInput() instanceof LinkPersonAddress)
					{
						Person person = ((LinkPersonAddress) viewer.getInput()).getPerson();
						if (((BankAccount) entity).getPerson().getId().equals(person.getId()))
						{
							viewer.refresh(entity);
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection)
	{
		if (selection instanceof StructuredSelection)
		{
			Person person = null;
			StructuredSelection ssel = (StructuredSelection) selection;
			if (ssel.getFirstElement() instanceof Person)
			{
				person = (Person) ssel.getFirstElement();
				this.setInput(person);
			}
			else if (ssel.getFirstElement() instanceof LinkPersonAddress)
			{
				person = ((LinkPersonAddress) ssel.getFirstElement()).getPerson();
				this.setInput(person);
			}
			else if (ssel.getFirstElement() instanceof Address)
			{
				Address address = (Address) ssel.getFirstElement();
				this.setInput(address);
			}
			else
			{
				this.setInput(null);
			}
		}
	}

	@Override
	public void setFocus()
	{
		if (this.viewer != null)
			this.viewer.getTable().setFocus();
	}

	public void setInput(final Object inputElement)
	{
		if (this.viewer != null)
		{
			this.viewer.setInput(inputElement);
			this.packColumns();
		}
	}
}
