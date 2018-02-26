package ch.eugster.events.donation.views;

import java.awt.Cursor;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import ch.eugster.events.donation.DomainContentProvider;
import ch.eugster.events.donation.DomainFilter;
import ch.eugster.events.donation.DomainLabelProvider;
import ch.eugster.events.donation.PurposeContentProvider;
import ch.eugster.events.donation.PurposeFilter;
import ch.eugster.events.donation.PurposeLabelProvider;
import ch.eugster.events.donation.YearContentProvider;
import ch.eugster.events.donation.YearLabelProvider;
import ch.eugster.events.donation.YearSorter;
import ch.eugster.events.donation.dialogs.DonationLetterDialog;
import ch.eugster.events.donation.dialogs.DonationListDialog;
import ch.eugster.events.donation.dialogs.DonatorListDialog;
import ch.eugster.events.donation.dialogs.SelectedDonationListDialog;
import ch.eugster.events.donation.dialogs.SelectedDonatorListDialog;
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
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.views.AbstractEntityView;

public class DonationView extends AbstractEntityView implements IDoubleClickListener, ISelectionChangedListener
{
	public static final String ID = "ch.eugster.events.donation.view";

	private ComboViewer yearViewer;

	private Button printConfirmation;
	
	private Button generateDonationList;

	private Button generateAddressList;

	private ComboViewer purposeViewer;

	private ComboViewer domainViewer;

	private Text personText;

	private Button clearText;

	private TableViewer donationViewer;

	private Label donationCount;

	private static DateFormat df = SimpleDateFormat.getDateInstance();

	private static NumberFormat nf;

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	private ConnectionService connectionService;

	private IDialogSettings settings;

	private final List<Domain> allAndEmptyDomains = new ArrayList<Domain>();

	private final DonationPurpose allPurposes = DonationPurpose.newInstance("Alle");

	public DonationView()
	{
	}

	private void createContextMenu()
	{
		final MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		final Menu menu = menuManager.createContextMenu(this.donationViewer.getControl());

		this.donationViewer.getControl().setMenu(menu);

		this.getSite().registerContextMenu(menuManager, this.donationViewer);
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		final PurposeFilter purposeFilter = new PurposeFilter();
		final DomainFilter domainFilter = new DomainFilter();

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());

		final Composite selectorComposite = new Composite(composite, SWT.NULL);
		selectorComposite.setLayout(new GridLayout(3, false));
		selectorComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(selectorComposite, SWT.None);
		label.setText("Auswahl Jahr");
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		Combo combo = new Combo(selectorComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(gridData);

		this.yearViewer = new ComboViewer(combo);
		this.yearViewer.setContentProvider(new YearContentProvider());
		this.yearViewer.setLabelProvider(new YearLabelProvider());
		this.yearViewer.setSorter(new YearSorter());

		label = new Label(selectorComposite, SWT.None);
		label.setText("Zweckfilter");
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		combo = new Combo(selectorComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(gridData);
		combo.setCursor(this.getSite().getShell().getDisplay().getSystemCursor(Cursor.DEFAULT_CURSOR));

		this.purposeViewer = new ComboViewer(combo);
		this.purposeViewer.setContentProvider(new PurposeContentProvider(this.allPurposes));
		this.purposeViewer.setLabelProvider(new PurposeLabelProvider());
		this.purposeViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.purposeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent event) 
			{
				final IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				DonationPurpose purpose = (DonationPurpose) ssel.getFirstElement();
				purpose = purpose == null || purpose.getId() == null ? null : purpose;
				purposeFilter.setSelectedPurpose(purpose);
				DonationView.this.settings.put("donation.view.purpose.id", purpose == null ? Long.valueOf(0L) : (purpose.getId() == null ? Long.valueOf(0L) : purpose.getId()));
				DonationView.this.donationViewer.refresh();
				DonationView.this.setDonationCount(DonationView.this.donationViewer.getTable().getItemCount());
			}
		});

		label = new Label(selectorComposite, SWT.None);
		label.setText("Domänenfilter");
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		combo = new Combo(selectorComposite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(gridData);
		combo.setCursor(this.getSite().getShell().getDisplay().getSystemCursor(Cursor.DEFAULT_CURSOR));

		this.allAndEmptyDomains.add(Domain.newInstance("Alle"));
		this.allAndEmptyDomains.add(Domain.newInstance("Ohne Domäne"));

		this.domainViewer = new ComboViewer(combo);
		this.domainViewer.setContentProvider(new DomainContentProvider(this.allAndEmptyDomains));
		this.domainViewer.setLabelProvider(new DomainLabelProvider());
		this.domainViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.domainViewer.setInput(this.connectionService);
		this.domainViewer.addSelectionChangedListener(new ISelectionChangedListener() 
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event) 
			{
				final IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				Domain domain = (Domain) ssel.getFirstElement();
				if (domain == null || domain.getName().equals("Alle")) 
				{
					domain = null;
				}
				final Long domainId = domain == null ? Long.valueOf(0L) : (domain.getId() == null ? Long.valueOf(0L) : domain.getId());
				DonationView.this.settings.put("donation.view.domain.id", domainId);
				domainFilter.setSelectedDomain(domain);
				DonationView.this.donationViewer.refresh();
				DonationView.this.setDonationCount(DonationView.this.donationViewer.getTable().getItemCount());
			}
		});
		
		label = new Label(selectorComposite, SWT.None);
		label.setText("Personenfilter");
		label.setLayoutData(new GridData());

		this.personText = new Text(selectorComposite, SWT.SINGLE | SWT.BORDER);
		this.personText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final PersonFilter personFilter = new PersonFilter(this.personText);

		this.clearText = new Button(selectorComposite, SWT.PUSH);
		this.clearText.setLayoutData(new GridData());
		this.clearText.setImage(Activator.getDefault().getImageRegistry().get("CLEAR"));
		this.clearText.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				DonationView.this.personText.setText("");
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}
		});

		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 4;

		final Table table = new Table(composite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);

		final DonationSorter sorter = new DonationSorter();

		this.donationViewer = new TableViewer(table);
		this.donationViewer.setContentProvider(new DonationTableViewerContentProvider());
		this.donationViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter(), personFilter, purposeFilter,
				domainFilter });
		this.donationViewer.addDoubleClickListener(this);
		this.donationViewer.setSorter(sorter);

		int order = 0;
		TableViewerColumn tableViewerColumn = new TableViewerColumn(this.donationViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					final Donation donation = (Donation) cell.getElement();
					cell.setImage(Activator.getDefault().getImageRegistry().get("MONEY"));
					cell.setText(DonationView.df.format(donation.getDonationDate().getTime()));
				}
			}

		});
		this.configureColumn("Datum", order++, tableViewerColumn.getColumn(), sorter);

		tableViewerColumn = new TableViewerColumn(this.donationViewer, SWT.RIGHT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					final Donation donation = (Donation) cell.getElement();
					cell.setText(DonationView.nf.format(donation.getAmount()));
				}
			}

		});
		this.configureColumn("Betrag", order++, tableViewerColumn.getColumn(), sorter);

		tableViewerColumn = new TableViewerColumn(this.donationViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					final Donation donation = (Donation) cell.getElement();
					final DonationPurpose purpose = donation.getPurpose();
					cell.setText(purpose.getCode().isEmpty() ? purpose.getName() : purpose.getCode() + " - "
							+ purpose.getName());
				}
			}

		});
		this.configureColumn("Zweck", order++, tableViewerColumn.getColumn(), sorter);

		tableViewerColumn = new TableViewerColumn(this.donationViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					final Donation donation = (Donation) cell.getElement();
					cell.setText(DonationFormatter.getInstance().formatDonatorName(donation));
				}
			}

		});
		this.configureColumn("Name", order++, tableViewerColumn.getColumn(), sorter);

		tableViewerColumn = new TableViewerColumn(this.donationViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					final Donation donation = (Donation) cell.getElement();
					cell.setText(DonationFormatter.getInstance().formatDonatorAddress(donation));
				}
			}

		});
		this.configureColumn("Strasse", order++, tableViewerColumn.getColumn(), sorter);

		tableViewerColumn = new TableViewerColumn(this.donationViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					final Donation donation = (Donation) cell.getElement();
					cell.setText(DonationFormatter.getInstance().formatDonatorCityLine(donation));
				}
			}

		});
		this.configureColumn("Ort", order++, tableViewerColumn.getColumn(), sorter);

		tableViewerColumn = new TableViewerColumn(this.donationViewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					final Donation donation = (Donation) cell.getElement();
					cell.setText(donation.getDomain() == null ? "" : donation.getDomain().getName());
				}
			}

		});
		this.configureColumn("Domäne", order++, tableViewerColumn.getColumn(), sorter);

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		this.donationCount = new Label(composite, SWT.None);
		this.donationCount.setLayoutData(gridData);

		final Composite actionComposite = new Composite(composite, SWT.NONE);
		actionComposite.setLayout(new GridLayout(3, false));
		actionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		this.printConfirmation = new Button(actionComposite, SWT.PUSH);
		this.printConfirmation.setToolTipText("Spendenbestätigung drucken");
		this.printConfirmation.setImage(Activator.getDefault().getImageRegistry().get("PRINT"));
		this.printConfirmation.setLayoutData(new GridData());
		this.printConfirmation.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				final DonationYear[] years = ((YearContentProvider)DonationView.this.yearViewer.getContentProvider()).getEntries();
				IStructuredSelection ssel = (IStructuredSelection) DonationView.this.yearViewer.getSelection();
				final DonationYear year = (DonationYear) ssel.getFirstElement();
				final DonationPurpose[] purposes = ((PurposeContentProvider)DonationView.this.purposeViewer.getContentProvider()).getEntries();
				ssel = (IStructuredSelection) DonationView.this.purposeViewer.getSelection();
				final DonationPurpose purpose = (DonationPurpose) ssel.getFirstElement();
				final Domain[] domains = ((DomainContentProvider)DonationView.this.domainViewer.getContentProvider()).getEntries();
				ssel = (IStructuredSelection) DonationView.this.domainViewer.getSelection();
				final Domain domain = (Domain) ssel.getFirstElement();
				final DonationLetterDialog dialog = new DonationLetterDialog(DonationView.this.getSite()
							.getShell(), DonationView.this.connectionService, years, year, purposes, purpose, domains, domain, DonationView.this.personText.getText());
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}
		});

		this.generateDonationList = new Button(actionComposite, SWT.PUSH);
		this.generateDonationList.setImage(Activator.getDefault().getImageRegistry().get("LIST"));
		this.generateDonationList.setToolTipText("Spendenliste exportieren");
		this.generateDonationList.setLayoutData(new GridData());
		this.generateDonationList.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				if (DonationView.this.donationViewer.getSelection().isEmpty())
				{
					final DonationYear[] years = ((YearContentProvider)DonationView.this.yearViewer.getContentProvider()).getEntries();
					IStructuredSelection ssel = (IStructuredSelection) DonationView.this.yearViewer.getSelection();
					final DonationYear year = (DonationYear) ssel.getFirstElement();
					final DonationPurpose[] purposes = ((PurposeContentProvider)DonationView.this.purposeViewer.getContentProvider()).getEntries();
					ssel = (IStructuredSelection) DonationView.this.purposeViewer.getSelection();
					final DonationPurpose purpose = (DonationPurpose) ssel.getFirstElement();
					final Domain[] domains = ((DomainContentProvider)DonationView.this.domainViewer.getContentProvider()).getEntries();
					ssel = (IStructuredSelection) DonationView.this.domainViewer.getSelection();
					final Domain domain = (Domain) ssel.getFirstElement();
					final DonationListDialog dialog = new DonationListDialog(DonationView.this.getSite()
								.getShell(), DonationView.this.connectionService, years, year, purposes, purpose, domains, domain, DonationView.this.personText.getText());
					dialog.open();
				}
				else
				{
					final SelectedDonationListDialog dialog = new SelectedDonationListDialog(DonationView.this.getSite().getShell(), (StructuredSelection) DonationView.this.donationViewer.getSelection());
					dialog.open();
				}
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}
		});
		
		this.generateAddressList = new Button(actionComposite, SWT.PUSH);
		this.generateAddressList.setImage(Activator.getDefault().getImageRegistry().get("LIST"));
		this.generateAddressList.setToolTipText("Personenliste exportieren");
		this.generateAddressList.setLayoutData(new GridData());
		this.generateAddressList.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				if (DonationView.this.donationViewer.getSelection().isEmpty())
				{
					final DonationYear[] years = ((YearContentProvider)DonationView.this.yearViewer.getContentProvider()).getEntries();
					IStructuredSelection ssel = (IStructuredSelection) DonationView.this.yearViewer.getSelection();
					final DonationYear year = (DonationYear) ssel.getFirstElement();
					final DonationPurpose[] purposes = ((PurposeContentProvider)DonationView.this.purposeViewer.getContentProvider()).getEntries();
					ssel = (IStructuredSelection) DonationView.this.purposeViewer.getSelection();
					final DonationPurpose purpose = (DonationPurpose) ssel.getFirstElement();
					final Domain[] domains = ((DomainContentProvider)DonationView.this.domainViewer.getContentProvider()).getEntries();
					ssel = (IStructuredSelection) DonationView.this.domainViewer.getSelection();
					final Domain domain = (Domain) ssel.getFirstElement();
					final DonatorListDialog dialog = new DonatorListDialog(DonationView.this.getSite()
								.getShell(), DonationView.this.connectionService, years, year, purposes, purpose, domains, domain, DonationView.this.personText.getText());
					dialog.open();
				}
				else
				{
					final SelectedDonatorListDialog dialog = new SelectedDonatorListDialog(DonationView.this.getSite().getShell(), (StructuredSelection) DonationView.this.donationViewer.getSelection());
					dialog.open();
				}
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}
		});
		
		this.createContextMenu();

		this.yearViewer.addSelectionChangedListener(this);
		this.personText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DonationView.this.settings.put("donation.view.filter.person", DonationView.this.personText.getText());
				DonationView.this.donationViewer.refresh();
				DonationView.this.setDonationCount(DonationView.this.donationViewer.getTable().getItemCount());
			}
		});

		this.getSite().setSelectionProvider(this.donationViewer);

		this.connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null)
		{
			@Override
			public ConnectionService addingService(final ServiceReference<ConnectionService> reference)
			{
				DonationView.this.connectionService = super.addingService(reference);
				final UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(final IProgressMonitor monitor)
					{
						DonationView.this.purposeViewer.setInput(DonationView.this.connectionService);
						final long purposeId = DonationView.this.settings.getLong("donation.view.purpose.id");
						final DonationPurposeQuery purposeQuery = (DonationPurposeQuery) DonationView.this.connectionService
								.getQuery(DonationPurpose.class);
						final DonationPurpose purpose = purposeQuery.find(DonationPurpose.class, Long.valueOf(purposeId));
						if (purpose == null)
						{
							DonationView.this.purposeViewer.setSelection(new StructuredSelection(new DonationPurpose[] { DonationView.this.allPurposes }));
						}
						else
						{
							DonationView.this.purposeViewer.setSelection(new StructuredSelection(new DonationPurpose[] { purpose }));
						}
						DonationView.this.domainViewer.setInput(DonationView.this.connectionService);
						final long domainId = DonationView.this.settings.getLong("donation.view.domain.id");
						final DomainQuery domainQuery = (DomainQuery) DonationView.this.connectionService.getQuery(Domain.class);
						final Domain domain = domainQuery.find(Domain.class, Long.valueOf(domainId));
						if (domain == null)
						{
							DonationView.this.domainViewer.setSelection(new StructuredSelection(new Domain[] { DonationView.this.allAndEmptyDomains
									.iterator().next() }));
						}
						else
						{
							DonationView.this.domainViewer.setSelection(new StructuredSelection(new Domain[] { domain }));
						}
						DonationView.this.yearViewer.setInput(DonationView.this.connectionService);
						if (DonationView.this.yearViewer.getCombo().getItemCount() > 0)
						{
							final DonationYear year = (DonationYear) DonationView.this.yearViewer.getElementAt(0);
							DonationView.this.yearViewer.setSelection(new StructuredSelection(new DonationYear[] { year }));
						}
						DonationView.this.printConfirmation.setEnabled(!DonationView.this.yearViewer.getSelection().isEmpty());
						DonationView.this.packColumns();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
				return DonationView.this.connectionService;
			}

			@Override
			public void removedService(final ServiceReference<ConnectionService> reference, final ConnectionService service)
			{
				DonationView.this.connectionService = null;
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
						DonationView.this.yearViewer.setInput(null);
						DonationView.this.purposeViewer.setInput(null);
						DonationView.this.domainViewer.setInput(null);

					}
				});
				super.removedService(reference, service);
			}

		};
		this.connectionServiceTracker.open();
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
				this.widgetSelected(e);
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
				DonationView.this.donationViewer.refresh();
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
		final ISelection selection = event.getSelection();
		final Object object = ((IStructuredSelection) selection).getFirstElement();
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
		catch (final PartInitException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);
		this.settings = Activator.getDefault().getDialogSettings().getSection("donation.view");
		if (this.settings == null)
		{
			this.settings = Activator.getDefault().getDialogSettings().addNewSection("donation.view");
		}
		try
		{
			this.settings.get("donation.view.filter.person");
		}
		catch (final Exception e)
		{
			this.settings.put("donation.view.filter.person", "");
		}
		try
		{
			this.settings.getLong("donation.view.purpose.id");
		}
		catch (final Exception e)
		{
			this.settings.put("donation.view.purpose.id", Long.valueOf(0L));
		}
		try
		{
			this.settings.getLong("donation.view.domain.id");
		}
		catch (final Exception e)
		{
			this.settings.put("donation.view.domain.id", Long.valueOf(0L));
		}
		EntityMediator.addListener(Donation.class, this);
		DonationView.nf = NumberFormat.getNumberInstance();
		DonationView.nf.setMinimumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
		DonationView.nf.setMaximumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
	}

	private void packColumns()
	{
		final TableColumn[] columns = this.donationViewer.getTable().getColumns();
		for (final TableColumn column : columns)
			column.pack();
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		final UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				if (entity instanceof Donation)
				{
					DonationView.this.donationViewer.refresh();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void postPersist(final AbstractEntity entity)
	{
		final UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				if (entity instanceof Donation)
				{
					DonationView.this.donationViewer.refresh();
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
				if (entity instanceof Donation)
				{
					DonationView.this.donationViewer.refresh(entity);
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
	public void selectionChanged(final SelectionChangedEvent event)
	{
		if (event.getSelectionProvider().equals(this.yearViewer))
		{
			final IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
			this.donationViewer.setInput(ssel.getFirstElement());
			this.packColumns();
			this.setDonationCount(this.donationViewer.getTable().getItemCount());
		}
	}

	private void setDonationCount(final int count)
	{
		this.donationCount.setText("Anzahl Spenden: " + count);
	}

	public TableViewer getViewer()
	{
		return this.donationViewer;
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
				final Donation d1 = (Donation) e1;
				final Donation d2 = (Donation) e2;

				switch (this.column)
				{
					case 0:
					{
						final Date dd1 = d1.getDonationDate().getTime();
						final Date dd2 = d2.getDonationDate().getTime();
						return this.compareDates(dd1, dd2);
					}
					case 1:
					{
						return this.compareAmounts(Double.valueOf(d1.getAmount()), Double.valueOf(d2.getAmount()));
					}
					case 2:
					{
						final String p1 = d1.getPurpose() == null ? "" : d1.getPurpose().getName();
						final String p2 = d2.getPurpose() == null ? "" : d2.getPurpose().getName();
						return this.compareStrings(p1, p2);
					}
					case 3:
					{
						final String name1 = DonationFormatter.getInstance().formatDonatorName(d1);
						final String name2 = DonationFormatter.getInstance().formatDonatorName(d2);
						return this.compareStrings(name1, name2);
					}
					case 4:
					{
						final String a1 = AddressFormatter.getInstance().formatAddressLine(
								d1.getLink() == null ? d1.getAddress() : d1.getLink().getAddress());
						final String a2 = AddressFormatter.getInstance().formatAddressLine(
								d2.getLink() == null ? d2.getAddress() : d2.getLink().getAddress());
						return this.compareStrings(a1, a2);
					}
					case 5:
					{
						final String c1 = AddressFormatter.getInstance().formatCityLine(
								d1.getLink() == null ? d1.getAddress() : d1.getLink().getAddress());
						final String c2 = AddressFormatter.getInstance().formatCityLine(
								d2.getLink() == null ? d2.getAddress() : d2.getLink().getAddress());
						return this.compareStrings(c1, c2);
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
			if (this.asc)
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
			if (this.asc)
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
			if (this.asc)
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
				final DonationYear year = (DonationYear) inputElement;
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
		private final Text personText;

		public PersonFilter(final Text personText)
		{
			this.personText = personText;
		}

		@Override
		public boolean select(final Viewer viewer, final Object parentElement, final Object element)
		{
			if (this.personText.getText().isEmpty())
			{
				return true;
			}
			else
			{
				final String name = this.personText.getText().toLowerCase();
				if (element instanceof Donation)
				{
					boolean select = false;
					final Donation donation = (Donation) element;
					final Address address = donation.getAddress();
					select = address.getName().toLowerCase().contains(name);
					if (!select && donation.getLink() != null)
					{
						final Person person = donation.getLink().getPerson();
						select = person.getLastname().toLowerCase().contains(name)
								|| person.getFirstname().toLowerCase().contains(name);
					}
					return select;
				}
			}
			return false;
		}
	}
}
