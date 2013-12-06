package ch.eugster.events.donation.views;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import ch.eugster.events.donation.Activator;
import ch.eugster.events.donation.editors.DonationEditor;
import ch.eugster.events.donation.editors.DonationEditorInput;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.person.views.PersonView;
import ch.eugster.events.ui.views.AbstractEntityView;

public class LinkDonationView extends AbstractEntityView implements ISelectionListener
{
	public static final String ID = "ch.eugster.events.donation.linkView";

	private static final DateFormat dateFormat = DateFormat.getDateInstance();

	private static NumberFormat numberFormat;

	private TableViewer viewer;

	private IDialogSettings settings;

	public LinkDonationView()
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
		this.viewer.setContentProvider(new LinkDonationContentProvider());
		this.viewer.setSorter(new LinkDonationSorter());
		this.viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(final DoubleClickEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				if (ssel.getFirstElement() instanceof Donation)
				{
					try
					{
						PlatformUI
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.openEditor(new DonationEditorInput((Donation) ssel.getFirstElement()),
										DonationEditor.ID);
					}
					catch (PartInitException e)
					{

					}
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
				if (object instanceof Donation)
				{
					Donation donation = (Donation) object;
					cell.setText(LinkDonationView.dateFormat.format(donation.getDonationDate().getTime()));
					cell.setImage(Activator.getDefault().getImageRegistry().get("MONEY"));
				}
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Datum");

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Donation)
				{
					Donation donation = (Donation) object;
					cell.setText(LinkDonationView.numberFormat.format(donation.getAmount()));
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Betrag");

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Donation)
				{
					Donation donation = (Donation) object;
					DonationPurpose purpose = donation.getPurpose();
					cell.setText(purpose.getCode().isEmpty() ? purpose.getName() : purpose.getCode() + " - "
							+ purpose.getName());
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Zweck");

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.viewer);

		PersonView view = (PersonView) this.getSite().getPage().findView(PersonView.ID);
		if (view != null)
		{
			IStructuredSelection ssel = (IStructuredSelection) view.getViewer().getSelection();
			this.viewer.setInput(ssel.getFirstElement());
		}

		this.getSite().getPage().addSelectionListener(PersonView.ID, this);
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(LinkPersonAddress.class, this);
		EntityMediator.removeListener(Donation.class, this);
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
		settings = Activator.getDefault().getDialogSettings().getSection("donation.view");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("donation.view");
		}

		EntityMediator.addListener(LinkPersonAddress.class, this);
		EntityMediator.addListener(Donation.class, this);
		EntityMediator.addListener(Person.class, this);

		numberFormat = NumberFormat.getInstance();
		numberFormat.setMinimumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
		numberFormat.setMaximumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());

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
				if (entity instanceof Donation)
				{
					viewer.refresh();
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
				if (entity instanceof Donation)
				{
					LinkPersonAddress link = null;
					if (viewer.getInput() instanceof Person)
					{
						link = ((Person) viewer.getInput()).getDefaultLink();
					}
					if (viewer.getInput() instanceof LinkPersonAddress)
					{
						link = (LinkPersonAddress) viewer.getInput();
					}
					if (((Donation) entity).getLink().getId().equals(link.getId()))
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
				if (entity instanceof Donation)
				{
					if (viewer.getInput() instanceof LinkPersonAddress)
					{
						LinkPersonAddress link = (LinkPersonAddress) viewer.getInput();
						if (((Donation) entity).getLink().getId().equals(link.getId()))
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
			LinkPersonAddress link = null;
			StructuredSelection ssel = (StructuredSelection) selection;
			if (ssel.getFirstElement() instanceof Person)
			{
				Person person = (Person) ssel.getFirstElement();
				this.setInput(person.getDefaultLink());
			}
			else if (ssel.getFirstElement() instanceof LinkPersonAddress)
			{
				link = (LinkPersonAddress) ssel.getFirstElement();
				this.setInput(link);
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
