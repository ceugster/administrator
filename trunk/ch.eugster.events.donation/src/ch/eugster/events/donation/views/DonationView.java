package ch.eugster.events.donation.views;

import java.awt.Cursor;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.donation.Activator;
import ch.eugster.events.donation.dialogs.DonationAddressListDialog;
import ch.eugster.events.donation.dialogs.DonationConfirmationDialog;
import ch.eugster.events.donation.editors.DonationEditor;
import ch.eugster.events.donation.editors.DonationEditorInput;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.DonationFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.queries.DonationPurposeQuery;
import ch.eugster.events.persistence.queries.DonationQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.views.AbstractEntityView;

public class DonationView extends AbstractEntityView implements IDoubleClickListener, ISelectionChangedListener
{
	public static final String ID = "ch.eugster.events.donation.view";

	private ComboViewer yearViewer;

	private Button printConfirmation;
	
	private Button generateList;

	private Button buildList;

	private ComboViewer purposeViewer;

	private ComboViewer domainViewer;

	private Text personText;

	private Button clearText;

	private TableViewer donationViewer;

	private Label donationCount;

	private static DateFormat df = SimpleDateFormat.getDateInstance();

	private static NumberFormat nf;

	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	private IDialogSettings settings;

	private Collection<Domain> allAndEmptyDomains = new ArrayList<Domain>();

	private DonationPurpose allPurposes = DonationPurpose.newInstance("Alle");

	public DonationView()
	{
	}

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(donationViewer.getControl());

		donationViewer.getControl().setMenu(menu);

		this.getSite().registerContextMenu(menuManager, donationViewer);
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());

		Composite selectorComposite = new Composite(composite, SWT.NULL);
		selectorComposite.setLayout(new GridLayout(3, false));
		selectorComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(selectorComposite, SWT.None);
		label.setText("Auswahl Jahr");
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		Combo combo = new Combo(selectorComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(gridData);

		yearViewer = new ComboViewer(combo);
		yearViewer.setContentProvider(new YearContentProvider());
		yearViewer.setLabelProvider(new YearLabelProvider());
		yearViewer.setSorter(new YearSorter());

		// buildList = new Button(composite, SWT.PUSH);
		// buildList.setImage(Activator.getDefault().getImageRegistry().get("LIST"));
		// buildList.setLayoutData(new GridData());
		// buildList.addSelectionListener(new SelectionListener()
		// {
		// @Override
		// public void widgetSelected(SelectionEvent e)
		// {
		// IStructuredSelection ssel = (IStructuredSelection)
		// yearViewer.getSelection();
		// DonationYear year = (DonationYear) ssel.getFirstElement();
		// ssel = (IStructuredSelection) purposeViewer.getSelection();
		// DonationPurpose purpose = (DonationPurpose) ssel.getFirstElement();
		// if (purpose != null)
		// {
		// purpose = purpose.getId() == null ? null : purpose;
		// }
		// ssel = (IStructuredSelection) domainViewer.getSelection();
		// Domain domain = (Domain) ssel.getFirstElement();
		// if (domain != null)
		// {
		// domain = domain.getName().equals("Alle") ? null : domain;
		// }
		//
		// DonationAddressListDialog dialog = new DonationAddressListDialog(
		// DonationView.this.getSite().getShell(), year, purpose, domain,
		// personText.getText());
		// dialog.open();
		// }
		//
		// @Override
		// public void widgetDefaultSelected(SelectionEvent e)
		// {
		// widgetSelected(e);
		// }
		// });

		label = new Label(selectorComposite, SWT.None);
		label.setText("Zweckfilter");
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		combo = new Combo(selectorComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(gridData);
		combo.setCursor(this.getSite().getShell().getDisplay().getSystemCursor(Cursor.DEFAULT_CURSOR));

		purposeViewer = new ComboViewer(combo);
		purposeViewer.setContentProvider(new PurposeContentProvider(allPurposes));
		purposeViewer.setLabelProvider(new PurposeLabelProvider());
		purposeViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });

		label = new Label(selectorComposite, SWT.None);
		label.setText("Domänenfilter");
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		combo = new Combo(selectorComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(gridData);
		combo.setCursor(this.getSite().getShell().getDisplay().getSystemCursor(Cursor.DEFAULT_CURSOR));

		allAndEmptyDomains.add(Domain.newInstance("Alle"));
		allAndEmptyDomains.add(Domain.newInstance("Keine"));

		domainViewer = new ComboViewer(combo);
		domainViewer.setContentProvider(new DomainContentProvider(allAndEmptyDomains));
		domainViewer.setLabelProvider(new DomainLabelProvider());
		domainViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		domainViewer.setInput(connectionService);

		label = new Label(selectorComposite, SWT.None);
		label.setText("Personenfilter");
		label.setLayoutData(new GridData());

		personText = new Text(selectorComposite, SWT.SINGLE | SWT.BORDER);
		personText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		clearText = new Button(selectorComposite, SWT.PUSH);
		clearText.setLayoutData(new GridData());
		clearText.setImage(Activator.getDefault().getImageRegistry().get("CLEAR"));
		clearText.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				personText.setText("");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});

		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 4;

		Table table = new Table(composite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);

		final DonationSorter sorter = new DonationSorter();

		PersonFilter personFilter = new PersonFilter(personText);
		PurposeFilter purposeFilter = new PurposeFilter();
		DomainFilter domainFilter = new DomainFilter();

		donationViewer = new TableViewer(table);
		donationViewer.setContentProvider(new DonationTableViewerContentProvider());
		donationViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter(), personFilter, purposeFilter,
				domainFilter });
		donationViewer.addDoubleClickListener(this);
		donationViewer.setSorter(sorter);

		int order = 0;
		TableViewerColumn tableViewerColumn = new TableViewerColumn(donationViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					cell.setImage(Activator.getDefault().getImageRegistry().get("MONEY"));
					cell.setText(df.format(donation.getDonationDate().getTime()));
				}
			}

		});
		this.configureColumn("Datum", order++, tableViewerColumn.getColumn(), sorter);

		tableViewerColumn = new TableViewerColumn(donationViewer, SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					cell.setText(nf.format(donation.getAmount()));
				}
			}

		});
		this.configureColumn("Betrag", order++, tableViewerColumn.getColumn(), sorter);

		tableViewerColumn = new TableViewerColumn(donationViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					DonationPurpose purpose = donation.getPurpose();
					cell.setText(purpose.getCode().isEmpty() ? purpose.getName() : purpose.getCode() + " - "
							+ purpose.getName());
				}
			}

		});
		this.configureColumn("Zweck", order++, tableViewerColumn.getColumn(), sorter);

		tableViewerColumn = new TableViewerColumn(donationViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					cell.setText(DonationFormatter.getInstance().formatDonatorName(donation));
				}
			}

		});
		this.configureColumn("Name", order++, tableViewerColumn.getColumn(), sorter);

		tableViewerColumn = new TableViewerColumn(donationViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					cell.setText(DonationFormatter.getInstance().formatDonatorAddress(donation));
				}
			}

		});
		this.configureColumn("Strasse", order++, tableViewerColumn.getColumn(), sorter);

		tableViewerColumn = new TableViewerColumn(donationViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					cell.setText(DonationFormatter.getInstance().formatDonatorCityLine(donation));
				}
			}

		});
		this.configureColumn("Domäne", order++, tableViewerColumn.getColumn(), sorter);

		tableViewerColumn = new TableViewerColumn(donationViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					cell.setText(donation.getDomain() == null ? "" : donation.getDomain().getName());
				}
			}

		});
		this.configureColumn("Ort", order++, tableViewerColumn.getColumn(), sorter);

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		donationCount = new Label(composite, SWT.None);
		donationCount.setLayoutData(gridData);

		Composite actionComposite = new Composite(composite, SWT.NONE);
		actionComposite.setLayout(new GridLayout(3, false));
		actionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		printConfirmation = new Button(actionComposite, SWT.PUSH);
		printConfirmation.setToolTipText("Spendenbestätigung drucken");
		printConfirmation.setImage(Activator.getDefault().getImageRegistry().get("PRINT"));
		printConfirmation.setLayoutData(new GridData());
		printConfirmation.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				IStructuredSelection ssel = (IStructuredSelection) yearViewer.getSelection();
				DonationYear year = (DonationYear) ssel.getFirstElement();
				ssel = (IStructuredSelection) purposeViewer.getSelection();
				DonationPurpose purpose = (DonationPurpose) ssel.getFirstElement();
				if (purpose != null)
				{
					purpose = purpose.getId() == null ? null : purpose;
				}
				ssel = (IStructuredSelection) domainViewer.getSelection();
				Domain domain = (Domain) ssel.getFirstElement();
				if (domain != null)
				{
					domain = domain.getName().equals("Alle") ? null : domain;
				}
				DonationConfirmationDialog dialog = new DonationConfirmationDialog(DonationView.this.getSite()
						.getShell(), year, purpose, domain, personText.getText());
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});

		generateList = new Button(actionComposite, SWT.PUSH);
		generateList.setImage(Activator.getDefault().getImageRegistry().get("LIST"));
		generateList.setToolTipText("Liste exportieren");
		generateList.setLayoutData(new GridData());
		generateList.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				IStructuredSelection ssel = (IStructuredSelection) yearViewer.getSelection();
				DonationYear year = (DonationYear) ssel.getFirstElement();
				ssel = (IStructuredSelection) purposeViewer.getSelection();
				DonationPurpose purpose = (DonationPurpose) ssel.getFirstElement();
				if (purpose != null)
				{
					purpose = purpose.getId() == null ? null : purpose;
				}
				ssel = (IStructuredSelection) domainViewer.getSelection();
				Domain domain = (Domain) ssel.getFirstElement();
				if (domain != null)
				{
					domain = domain.getName().equals("Alle") ? null : domain;
				}
				DonationAddressListDialog dialog = new DonationAddressListDialog(DonationView.this.getSite()
						.getShell(), year, purpose, domain, personText.getText());
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});
		
		this.createContextMenu();

		yearViewer.addSelectionChangedListener(this);
		purposeViewer.addSelectionChangedListener(purposeFilter);
		domainViewer.addSelectionChangedListener(domainFilter);
		personText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				settings.put("donation.view.filter.person", personText.getText());
				donationViewer.refresh();
				setDonationCount(donationViewer.getTable().getItemCount());
			}
		});

		this.getSite().setSelectionProvider(donationViewer);

		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(final ServiceReference reference)
			{
				connectionService = (ConnectionService) super.addingService(reference);
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(final IProgressMonitor monitor)
					{
						purposeViewer.setInput(connectionService);
						long purposeId = settings.getLong("donation.view.purpose.id");
						DonationPurposeQuery purposeQuery = (DonationPurposeQuery) connectionService
								.getQuery(DonationPurpose.class);
						DonationPurpose purpose = purposeQuery.find(DonationPurpose.class, Long.valueOf(purposeId));
						if (purpose == null)
						{
							purposeViewer.setSelection(new StructuredSelection(new DonationPurpose[] { allPurposes }));
						}
						else
						{
							purposeViewer.setSelection(new StructuredSelection(new DonationPurpose[] { purpose }));
						}
						domainViewer.setInput(connectionService);
						long domainId = settings.getLong("donation.view.domain.id");
						DomainQuery domainQuery = (DomainQuery) connectionService.getQuery(Domain.class);
						Domain domain = domainQuery.find(Domain.class, Long.valueOf(domainId));
						if (domain == null)
						{
							domainViewer.setSelection(new StructuredSelection(new Domain[] { allAndEmptyDomains
									.iterator().next() }));
						}
						else
						{
							domainViewer.setSelection(new StructuredSelection(new Domain[] { domain }));
						}
						yearViewer.setInput(connectionService);
						DonationYear year = new DonationYear(GregorianCalendar.getInstance().get(Calendar.YEAR));
						yearViewer.setSelection(new StructuredSelection(new DonationYear[] { year }));
						packColumns();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
				return connectionService;
			}

			@Override
			public void removedService(final ServiceReference reference, final Object service)
			{
				connectionService = null;
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
						yearViewer.setInput(null);
						purposeViewer.setInput(null);
						domainViewer.setInput(null);

					}
				});
				super.removedService(reference, service);
			}

		};
		connectionServiceTracker.open();
	}

	private void configureColumn(final String name, final int order, final TableColumn tableColumn,
			final DonationSorter sorter)
	{
		tableColumn.setText(name);
		tableColumn.setResizable(true);
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				if (sorter.column == order)
				{
					sorter.asc = !sorter.asc;
				}
				else
				{
					sorter.column = order;
				}
				donationViewer.refresh();
			}
		});
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Donation.class, this);
		super.dispose();
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof Donation)
		{
			this.editDonation((Donation) object);
		}
	}

	private void editDonation(final Donation donation)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new DonationEditorInput(donation), DonationEditor.ID, true);
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
		settings = Activator.getDefault().getDialogSettings().getSection("donation.view");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("donation.view");
		}
		try
		{
			settings.get("donation.view.filter.person");
		}
		catch (Exception e)
		{
			settings.put("donation.view.filter.person", "");
		}
		try
		{
			settings.getLong("donation.view.purpose.id");
		}
		catch (Exception e)
		{
			settings.put("donation.view.purpose.id", Long.valueOf(0L));
		}
		try
		{
			settings.getLong("donation.view.domain.id");
		}
		catch (Exception e)
		{
			settings.put("donation.view.domain.id", Long.valueOf(0L));
		}
		EntityMediator.addListener(Donation.class, this);
		nf = DecimalFormat.getNumberInstance();
		nf.setMinimumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
		nf.setMaximumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
	}

	private void packColumns()
	{
		TableColumn[] columns = donationViewer.getTable().getColumns();
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
					donationViewer.refresh();
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
					donationViewer.refresh();
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
					donationViewer.refresh(entity);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void setFocus()
	{
		this.donationViewer.getTable().setFocus();
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event)
	{
		if (event.getSelectionProvider().equals(yearViewer))
		{
			IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
			donationViewer.setInput(ssel.getFirstElement());
			packColumns();
			setDonationCount(donationViewer.getTable().getItemCount());
		}
	}

	private void setDonationCount(int count)
	{
		donationCount.setText("Anzahl Spenden: " + count);
	}

	public TableViewer getViewer()
	{
		return donationViewer;
	}

	private class YearContentProvider extends ArrayContentProvider
	{
		@Override
		public Object[] getElements(Object inputElement)
		{
			if (inputElement instanceof ConnectionService)
			{
				ConnectionService connectionService = (ConnectionService) inputElement;
				DonationQuery query = (DonationQuery) connectionService.getQuery(Donation.class);
				Collection<DonationYear> years = query.selectYears();
				return years.toArray(new DonationYear[0]);
			}
			return new DonationYear[0];
		}
	}

	private class YearLabelProvider extends LabelProvider
	{
		@Override
		public String getText(Object element)
		{
			if (element instanceof DonationYear)
			{
				DonationYear donationYear = (DonationYear) element;
				return Integer.toString(donationYear.getYear());
			}
			return "";
		}

	}

	private class PurposeContentProvider extends ArrayContentProvider
	{
		private DonationPurpose allPurposes;

		public PurposeContentProvider(DonationPurpose allPurposes)
		{
			this.allPurposes = allPurposes;
		}

		@Override
		public Object[] getElements(Object inputElement)
		{
			if (inputElement instanceof ConnectionService)
			{
				Collection<DonationPurpose> purposes = new ArrayList<DonationPurpose>();
				purposes.add(allPurposes);
				ConnectionService connectionService = (ConnectionService) inputElement;
				DonationPurposeQuery query = (DonationPurposeQuery) connectionService.getQuery(DonationPurpose.class);
				purposes.addAll(query.selectAll());
				return purposes.toArray(new DonationPurpose[0]);
			}
			return new DonationPurpose[0];
		}
	}

	private class PurposeLabelProvider extends LabelProvider
	{
		@Override
		public String getText(Object element)
		{
			if (element instanceof DonationPurpose)
			{
				DonationPurpose purpose = (DonationPurpose) element;
				return purpose.getName();
			}
			return "";
		}
	}

	private class DomainContentProvider extends ArrayContentProvider
	{
		private Collection<Domain> allAndEmptyDomains;

		public DomainContentProvider(Collection<Domain> allAndEmptyDomains)
		{
			this.allAndEmptyDomains = allAndEmptyDomains;
		}

		@Override
		public Object[] getElements(Object inputElement)
		{
			if (inputElement instanceof ConnectionService)
			{
				Collection<Domain> domains = new ArrayList<Domain>();
				domains.addAll(allAndEmptyDomains);
				ConnectionService connectionService = (ConnectionService) inputElement;
				DomainQuery query = (DomainQuery) connectionService.getQuery(Domain.class);
				domains.addAll(query.selectAll());
				return domains.toArray(new Domain[0]);
			}
			return new Domain[0];
		}
	}

	private class DomainLabelProvider extends LabelProvider
	{
		@Override
		public String getText(Object element)
		{
			if (element instanceof Domain)
			{
				Domain domain = (Domain) element;
				return domain.getName();
			}
			return "";
		}
	}

	private class YearSorter extends ViewerSorter
	{
		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2)
		{
			if (e1 instanceof DonationYear && e2 instanceof DonationYear)
			{
				DonationYear d1 = (DonationYear) e1;
				DonationYear d2 = (DonationYear) e2;

				return Integer.valueOf(d2.getYear()).compareTo(Integer.valueOf(d1.getYear()));
			}
			return 0;
		}
	}

	private class DonationSorter extends ViewerSorter
	{
		private boolean asc = true;

		private int column = 0;

		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2)
		{
			if (e1 instanceof Donation && e2 instanceof Donation)
			{
				Donation d1 = (Donation) e1;
				Donation d2 = (Donation) e2;

				switch (column)
				{
					case 0:
					{
						Date dd1 = d1.getDonationDate().getTime();
						Date dd2 = d2.getDonationDate().getTime();
						return compareDates(dd1, dd2);
					}
					case 1:
					{
						return compareAmounts(Double.valueOf(d1.getAmount()), Double.valueOf(d2.getAmount()));
					}
					case 2:
					{
						String p1 = d1.getPurpose() == null ? "" : d1.getPurpose().getName();
						String p2 = d2.getPurpose() == null ? "" : d2.getPurpose().getName();
						return compareStrings(p1, p2);
					}
					case 3:
					{
						String name1 = DonationFormatter.getInstance().formatDonatorName(d1);
						String name2 = DonationFormatter.getInstance().formatDonatorName(d2);
						return compareStrings(name1, name2);
					}
					case 4:
					{
						String a1 = AddressFormatter.getInstance().formatAddressLine(
								d1.getLink() == null ? d1.getAddress() : d1.getLink().getAddress());
						String a2 = AddressFormatter.getInstance().formatAddressLine(
								d2.getLink() == null ? d2.getAddress() : d2.getLink().getAddress());
						return compareStrings(a1, a2);
					}
					case 5:
					{
						String c1 = AddressFormatter.getInstance().formatCityLine(
								d1.getLink() == null ? d1.getAddress() : d1.getLink().getAddress());
						String c2 = AddressFormatter.getInstance().formatCityLine(
								d2.getLink() == null ? d2.getAddress() : d2.getLink().getAddress());
						return compareStrings(c1, c2);
					}
					default:
					{
						return 0;
					}
				}
			}
			return 0;
		}

		private int compareAmounts(final Double amount1, final Double amount2)
		{
			if (asc)
			{
				return amount1.compareTo(amount2);
			}
			else
			{
				return amount2.compareTo(amount1);
			}
		}

		private int compareDates(final Date date1, final Date date2)
		{
			if (asc)
			{
				return date1.compareTo(date2);
			}
			else
			{
				return date2.compareTo(date1);
			}
		}

		private int compareStrings(final String s1, final String s2)
		{
			if (asc)
			{
				return s1.compareTo(s2);
			}
			else
			{
				return s2.compareTo(s1);
			}
		}
	}

	private class DonationTableViewerContentProvider implements IStructuredContentProvider
	{
		@Override
		public void dispose()
		{
		}

		@Override
		public Object[] getElements(final Object inputElement)
		{
			Donation[] donations = new Donation[0];
			if (inputElement instanceof DonationYear)
			{
				DonationYear year = (DonationYear) inputElement;
				donations = year.getDonations().toArray(new Donation[0]);
			}
			return donations;
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
		{
		}
	}

	private class PersonFilter extends ViewerFilter
	{
		private Text personText;

		public PersonFilter(Text personText)
		{
			this.personText = personText;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element)
		{
			if (personText.getText().isEmpty())
			{
				return true;
			}
			else
			{
				String name = personText.getText().toLowerCase();
				if (element instanceof Donation)
				{
					boolean select = false;
					Donation donation = (Donation) element;
					Address address = donation.getAddress();
					select = address.getName().toLowerCase().contains(name);
					if (!select && donation.getLink() != null)
					{
						Person person = donation.getLink().getPerson();
						select = person.getLastname().toLowerCase().contains(name)
								|| person.getFirstname().toLowerCase().contains(name);
					}
					return select;
				}
			}
			return false;
		}
	}

	private class PurposeFilter extends ViewerFilter implements ISelectionChangedListener
	{
		private DonationPurpose purpose;

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element)
		{
			if (purpose == null)
			{
				return true;
			}
			if (element instanceof Donation)
			{
				Donation donation = (Donation) element;
				return donation.getPurpose().getId().equals(purpose.getId());
			}
			return false;
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event)
		{
			if (event.getSelectionProvider().equals(purposeViewer))
			{
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				DonationPurpose purpose = (DonationPurpose) ssel.getFirstElement();
				PurposeFilter.this.purpose = purpose == null || purpose.getId() == null ? null : purpose;
			}
			settings.put("donation.view.purpose.id", this.purpose == null ? Long.valueOf(0L)
					: (this.purpose.getId() == null ? Long.valueOf(0L) : this.purpose.getId()));
			donationViewer.refresh();
			setDonationCount(donationViewer.getTable().getItemCount());
		}

	}

	private class DomainFilter extends ViewerFilter implements ISelectionChangedListener
	{
		private Domain domain;

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element)
		{
			if (domain == null)
			{
				return true;
			}
			if (element instanceof Donation)
			{
				Donation donation = (Donation) element;
				if (domain.getName().equals("Keine"))
				{
					return donation.getDomain() == null;
				}
				return donation.getDomain() != null && donation.getDomain().getId().equals(domain.getId());
			}
			return false;
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event)
		{
			if (event.getSelectionProvider().equals(domainViewer))
			{
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				Domain domain = (Domain) ssel.getFirstElement();
				if (domain == null || domain.getName().equals("Alle"))
				{
					this.domain = null;
				}
				else
				{
					this.domain = domain;
				}
				Long domainId = this.domain == null ? Long.valueOf(0L) : (this.domain.getId() == null ? Long
						.valueOf(0L) : this.domain.getId());
				settings.put("donation.view.domain.id", domainId);
				donationViewer.refresh();
				setDonationCount(donationViewer.getTable().getItemCount());
			}
		}
	}
}
