package ch.eugster.events.donation.dialogs;

import java.awt.Cursor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.documents.maps.AbstractDataMap;
import ch.eugster.events.documents.maps.AddressMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;
import ch.eugster.events.documents.maps.DonationMap;
import ch.eugster.events.documents.maps.LinkMap;
import ch.eugster.events.documents.maps.PersonMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.donation.Activator;
import ch.eugster.events.donation.DomainContentProvider;
import ch.eugster.events.donation.DomainLabelProvider;
import ch.eugster.events.donation.PurposeContentProvider;
import ch.eugster.events.donation.PurposeLabelProvider;
import ch.eugster.events.donation.YearContentProvider;
import ch.eugster.events.donation.YearLabelProvider;
import ch.eugster.events.donation.YearSorter;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.persistence.queries.DonationQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationListDialog extends TitleAreaDialog
{
	private final ConnectionService connectionService;

	private ComboViewer yearFromViewer;

	private ComboViewer yearToViewer;

	private ComboViewer purposeViewer;

	private ComboViewer domainViewer;

	private Text name;
	
	private Button summary;

	private ComboViewer excludeYearViewer;

	private final DonationPurpose allPurposes = DonationPurpose.newInstance("Alle");

	private final List<Domain> allAndEmptyDomains = new ArrayList<Domain>();

	private final DonationYear[] donationYears;

	private DonationYear selectedFromDonationYear;

	private DonationYear selectedToDonationYear;

	private DonationYear excludeYear;

	private final DonationPurpose[] donationPurposes;

	private DonationPurpose selectedDonationPurpose;

	private final Domain[] domains;

	private Domain selectedDomain;

	private String selectedName;

	/*
	 * Common
	 */

	private final String message = "Wählen Sie die gewünschten Optionen.";

	private static final String MSG_NO_SERVICE_AVAILABLE = "Es ist kein Service für die Verarbeitung des Dokuments verfügbar.";

	private static final String MSG_TITLE_NO_BOOKINGS = "Keine Spenden vorhanden";

	private static final String OK_BUTTON_TEXT = "Generieren";

	private static final String CANCEL_BUTTON_TEXT = "Abbrechen";

	private static final String DIALOG_TITLE = "Spendenliste";

	private boolean isPageComplete = false;

	public DonationListDialog(final Shell parentShell, final ConnectionService connectionService, final DonationYear[] donationYears, final DonationYear selectedDonationYear, final DonationPurpose[] donationPurposes, final DonationPurpose selectedDonationPurpose, final Domain[] domains, final Domain selectedDomain, final String selectedName)
	{
		super(parentShell);
		this.connectionService = connectionService;
		this.donationYears = donationYears;
		this.selectedFromDonationYear = selectedDonationYear;
		this.selectedToDonationYear = selectedDonationYear;
		this.donationPurposes = donationPurposes;
		this.selectedDonationPurpose = selectedDonationPurpose;
		this.domains = domains;
		this.selectedDomain = selectedDomain;
		this.selectedName = selectedName;
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle(DonationListDialog.DIALOG_TITLE);
		this.setMessage();

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(4, true));

		Label label = new Label(composite, SWT.None);
		label.setText("Von");
		label.setLayoutData(new GridData());

		Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.yearFromViewer = new ComboViewer(combo);
		this.yearFromViewer.setContentProvider(new YearContentProvider());
		this.yearFromViewer.setLabelProvider(new YearLabelProvider());
		this.yearFromViewer.setSorter(new YearSorter());
		this.yearFromViewer.setInput(this.donationYears);
		this.yearFromViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				final IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				DonationListDialog.this.selectedFromDonationYear = (DonationYear) ssel.getFirstElement();
				final IStructuredSelection toSsel = (IStructuredSelection) DonationListDialog.this.yearToViewer.getSelection();
				if (toSsel.isEmpty() || ((DonationYear) toSsel.getFirstElement()).getYear() < ((DonationYear) ssel.getFirstElement()).getYear())
				{
					DonationListDialog.this.yearToViewer.setSelection(ssel);
				}
			}
		});

		label = new Label(composite, SWT.None);
		label.setText("bis");
		label.setLayoutData(new GridData());

		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.yearToViewer = new ComboViewer(combo);
		this.yearToViewer.setContentProvider(new YearContentProvider());
		this.yearToViewer.setLabelProvider(new YearLabelProvider());
		this.yearToViewer.setSorter(new YearSorter());
		this.yearToViewer.setInput(this.donationYears);
		this.yearToViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				final IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				DonationListDialog.this.selectedToDonationYear = (DonationYear) ssel.getFirstElement();
				final IStructuredSelection fromSsel = (IStructuredSelection) DonationListDialog.this.yearFromViewer.getSelection();
				if (fromSsel.isEmpty() || ((DonationYear) fromSsel.getFirstElement()).getYear() > ((DonationYear) ssel.getFirstElement()).getYear())
				{
					DonationListDialog.this.yearFromViewer.setSelection(ssel);
				}
			}
		});
		this.yearFromViewer.setSelection(new StructuredSelection(new DonationYear[] { this.selectedFromDonationYear }));

		label = new Label(composite, SWT.None);
		label.setText("Spender von");
		label.setLayoutData(new GridData());

		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setCursor(this.getShell().getDisplay().getSystemCursor(Cursor.DEFAULT_CURSOR));

		this.excludeYearViewer = new ComboViewer(combo);
		this.excludeYearViewer.setContentProvider(new YearContentProvider());
		this.excludeYearViewer.setLabelProvider(new YearLabelProvider());
		this.excludeYearViewer.setSorter(new YearSorter());
		this.excludeYearViewer.setInput(this.donationYears);
		this.excludeYear = new DonationYear(0);
		this.excludeYearViewer.insert(this.excludeYear, 0);
		this.excludeYearViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				final IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				DonationListDialog.this.excludeYear = (DonationYear) ssel.getFirstElement();
			}
		});
		this.excludeYearViewer.setSelection(new StructuredSelection(new DonationYear[] { this.excludeYear }));

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		label = new Label(composite, SWT.None);
		label.setText("ausschliessen");
		label.setLayoutData(gridData);

		label = new Label(composite, SWT.None);
		label.setText("Zweckfilter");
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(gridData);
		combo.setCursor(this.getShell().getDisplay().getSystemCursor(Cursor.DEFAULT_CURSOR));

		this.purposeViewer = new ComboViewer(combo);
		this.purposeViewer.setContentProvider(new PurposeContentProvider(this.allPurposes));
		this.purposeViewer.setLabelProvider(new PurposeLabelProvider());
		this.purposeViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.purposeViewer.setInput(this.donationPurposes);
		this.purposeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				final IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				if (ssel.isEmpty() || ((DonationPurpose) ssel.getFirstElement()).getId() == null)
				{
					DonationListDialog.this.selectedDonationPurpose = null;
				}
				else
				{
					DonationListDialog.this.selectedDonationPurpose = (DonationPurpose) ssel.getFirstElement();
				}
			}
		});
		this.purposeViewer.setSelection(new StructuredSelection(new DonationPurpose[] { this.selectedDonationPurpose }));

		label = new Label(composite, SWT.None);
		label.setText("Domänenfilter");
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(gridData);
		combo.setCursor(this.getShell().getDisplay().getSystemCursor(Cursor.DEFAULT_CURSOR));

		this.allAndEmptyDomains.add(Domain.newInstance("Alle"));
		this.allAndEmptyDomains.add(Domain.newInstance("Keine"));

		this.domainViewer = new ComboViewer(combo);
		this.domainViewer.setContentProvider(new DomainContentProvider(this.allAndEmptyDomains));
		this.domainViewer.setLabelProvider(new DomainLabelProvider());
		this.domainViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.domainViewer.setInput(this.domains);
		this.domainViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				final IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				if (ssel.isEmpty() || ((Domain) ssel.getFirstElement()).getName().equals("Alle"))
				{
					DonationListDialog.this.selectedDomain = null;
				}
				else
				{
					DonationListDialog.this.selectedDomain = (Domain) ssel.getFirstElement();
				}
			}
		});
		this.domainViewer.setSelection(new StructuredSelection(new Domain[] { this.selectedDomain }));

		label = new Label(composite, SWT.None);
		label.setText("Namenfilter");
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		final GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		final Composite nameComposite = new Composite(composite, SWT.NONE);
		nameComposite.setLayoutData(gridData);
		nameComposite.setLayout(layout);
		
		this.name = new Text(nameComposite, SWT.BORDER);
		this.name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.name.setText(this.selectedName);
		this.name.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DonationListDialog.this.selectedName = DonationListDialog.this.name.getText();
			}
		});
		
		this.summary = new Button(composite, SWT.CHECK);
		this.summary.setLayoutData(new GridData());
		this.summary.setText("Nur Gesamtbetrag angeben");
		
		final Button clearName = new Button(nameComposite, SWT.PUSH);
		clearName.setLayoutData(new GridData());
		clearName.setImage(Activator.getDefault().getImageRegistry().get("CLEAR"));
		clearName.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				DonationListDialog.this.name.setText("");
			}
		});

		return composite;
	}

	private DataMapKey[] getKeys()
	{
		final List<DataMapKey> keys = new ArrayList<DataMapKey>();
		keys.add(DonationMap.Key.TYPE);
		keys.add(DonationMap.Key.ID);
		keys.add(PersonMap.Key.SEX);
		keys.add(PersonMap.Key.FORM);
		keys.add(DonationMap.Key.SALUTATION);
		keys.add(PersonMap.Key.TITLE);
		keys.add(PersonMap.Key.FIRSTNAME);
		keys.add(PersonMap.Key.LASTNAME);
		keys.add(AddressMap.Key.NAME);
		keys.add(DonationMap.Key.ANOTHER_LINE);
		keys.add(PersonMap.Key.BIRTHDATE);
		keys.add(PersonMap.Key.PROFESSION);
		keys.add(LinkMap.Key.FUNCTION);
		keys.add(LinkMap.Key.PHONE);
		keys.add(AddressMap.Key.PHONE);
		keys.add(PersonMap.Key.PHONE);
		keys.add(AddressMap.Key.FAX);
		keys.add(PersonMap.Key.EMAIL);
		keys.add(LinkMap.Key.EMAIL);
		keys.add(AddressMap.Key.EMAIL);
		keys.add(PersonMap.Key.WEBSITE);
		keys.add(AddressMap.Key.WEBSITE);
		keys.add(AddressMap.Key.ADDRESS);
		keys.add(AddressMap.Key.POB);
		keys.add(AddressMap.Key.COUNTRY);
		keys.add(AddressMap.Key.ZIP);
		keys.add(AddressMap.Key.CITY);
		keys.add(AddressMap.Key.COUNTY);
		keys.add(DonationMap.Key.POLITE);
		keys.add(LinkMap.Key.MEMBER);
		keys.add(PersonMap.Key.NOTE);
		keys.add(DonationMap.Key.POLITE);
		keys.add(DonationMap.Key.YEAR);
		keys.add(DonationMap.Key.PURPOSE_CODE);
		keys.add(DonationMap.Key.PURPOSE_NAME);
		keys.add(DonationMap.Key.DATE);
		keys.add(DonationMap.Key.AMOUNT);
		return keys.toArray(new DataMapKey[0]);
	}

	private void buildDocument()
	{
		DataMap<?>[] maps = null;
		if (this.summary.getSelection())
		{
			maps = this.createSummarizedDataMaps();
		}
		else
		{
			maps = this.createDataMaps();
		}
		final DataMap<?>[] dataMaps = maps;
		if (dataMaps.length == 0)
		{
			final MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), DonationListDialog.MSG_TITLE_NO_BOOKINGS, null, "Die Auswahl enthält keine auswertbaren Elemente.", MessageDialog.INFORMATION, new String[] { "OK" }, 0);
			dialog.open();
		}
		else
		{
			Arrays.sort(dataMaps, new Comparator<DataMap<?>>()
			{
				@Override
				public int compare(final DataMap<?> map1, final DataMap<?> map2)
				{
					String name1 = map1.getProperty("person_lastname");
					String name2 = map2.getProperty("person_lastname");
					int result = DonationListDialog.this.compareStrings(name1, name2);
					if (result == 0)
					{
						name1 = map1.getProperty("person_firstname");
						name2 = map2.getProperty("person_firstname");
						result = DonationListDialog.this.compareStrings(name1, name2);
						if (result == 0)
						{
							name1 = map1.getProperty("address_name");
							name2 = map2.getProperty("address_name");
							return DonationListDialog.this.compareStrings(name1, name2);
						}
					}
					return result;
				}
			});
			final ProgressMonitorDialog dialog = new ProgressMonitorDialog(this.getShell());
			try
			{
				dialog.run(true, true, new IRunnableWithProgress()
				{
					@Override
					public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
					{
						IStatus status = Status.CANCEL_STATUS;
						final ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle().getBundleContext(), DocumentBuilderService.class, null);
						try
						{
							tracker.open();
							final ServiceReference<DocumentBuilderService>[] references = tracker.getServiceReferences();
							if (references != null)
							{
								try
								{
									monitor.beginTask("Dokument wird erstellt...", references.length);
									for (final ServiceReference<DocumentBuilderService> reference : references)
									{
										final DocumentBuilderService service = tracker.getService(reference);
										final DocumentBuilderService builderService = service;
										status = builderService.buildDocument(new SubProgressMonitor(monitor, dataMaps.length), DonationListDialog.this.getKeys(), dataMaps);
										if (status.isOK() || status.getSeverity() == IStatus.ERROR)
										{
											break;
										}
										monitor.worked(1);
									}
									monitor.worked(1);
								}
								finally
								{
									monitor.done();
								}
							}
							else
							{
								status = new Status(IStatus.CANCEL, Activator.getDefault().getBundle().getSymbolicName(), DonationListDialog.MSG_NO_SERVICE_AVAILABLE);
							}
						}
						finally
						{
							if (!status.isOK())
							{
								MessageDialog.openInformation(DonationListDialog.this.getShell(), "Information", status.getMessage());
							}
							tracker.close();
						}
					}
				});
			}
			catch (final InvocationTargetException e)
			{
				MessageDialog.openError(this.getShell(), "Fehler", "Bei der Verarbeitung ist ein Fehler aufgetreten.\n(" + e.getLocalizedMessage() + ")");
			}
			catch (final InterruptedException e)
			{
			}
		}
	}

	private int compareStrings(String s1, String s2)
	{
		if (s1 == null)
		{
			s1 = "";
		}
		if (s2 == null)
		{
			s2 = "";
		}
		return s1.toLowerCase().compareTo(s2.toLowerCase());
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, DonationListDialog.OK_BUTTON_TEXT, true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, DonationListDialog.CANCEL_BUTTON_TEXT, false);
	}

	private DataMap<?>[] createDataMaps()
	{
		final Map<String, DataMap<?>> dataMaps = new HashMap<String, DataMap<?>>();
		final DonationQuery query = (DonationQuery) this.connectionService.getQuery(Donation.class);
		List<Donation> donations = query.selectByYearRangePurposeDomainName(this.selectedFromDonationYear, this.selectedToDonationYear, this.selectedDonationPurpose, this.selectedDomain, this.name.getText());
		for (final Donation donation : donations)
		{
			if (this.printDonation(donation))
			{
				final DonationMap map = new DonationMap(donation);
				if (donation.getLink() == null || !donation.getLink().isValid())
				{
					map.setProperties(new AddressMap(donation.getAddress()).getProperties());
				}
				else
				{
					map.setProperties(new LinkMap(donation.getLink()).getProperties());
				}
				dataMaps.put(donation.getId().toString(), map);
			}
		}
		if (this.excludeYear.getYear() > 0)
		{
			donations = query.selectByYear(this.excludeYear);
			for (final Donation donation : donations)
			{
				if (this.printDonation(donation))
				{
					dataMaps.remove(donation.getId().toString());
				}
			}
		}
		return dataMaps.values().toArray(new DataMap<?>[0]);
	}

	private DataMap<?>[] createSummarizedDataMaps()
	{
		final Map<String, DonationMap> dataMaps = new HashMap<String, DonationMap>();
		final DonationQuery query = (DonationQuery) this.connectionService.getQuery(Donation.class);
		List<Donation> donations = query.selectByYearRangePurposeDomainName(this.selectedFromDonationYear, this.selectedToDonationYear, this.selectedDonationPurpose, this.selectedDomain, this.name.getText());
		for (final Donation donation : donations)
		{
			if (this.printDonation(donation))
			{
				String key = this.getKey(donation);
				DonationMap map = dataMaps.get(key);
				if (map == null)
				{
					map = new DonationMap(donation);
					if (donation.getLink() == null || !donation.getLink().isValid())
					{
						map.setProperties(new AddressMap(donation.getAddress()).getProperties());
					}
					else
					{
						map.setProperties(new LinkMap(donation.getLink()).getProperties());
					}
					dataMaps.put(key, map);
				}
				else
				{
					double amount = 0D;
					try
					{
						amount = Double.parseDouble(map.getProperty(DonationMap.Key.AMOUNT.getKey()));
					}
					catch (NumberFormatException e)
					{
					}
					amount = amount += donation.getAmount();
					map.setProperty(DonationMap.Key.AMOUNT.getKey(), String.valueOf(amount));

					String donationDate = AbstractDataMap.getDateFormatter().format(donation.getDonationDate().getTime());
					String date = map.getProperty(DonationMap.Key.DATE.getKey());
					if (date == null || date.isEmpty())
					{
						date = donationDate;
					}
					else if (!date.contains(donationDate))
					{
						date = date + ", " + donationDate;
					}
					map.setProperty(DonationMap.Key.DATE.getKey(), date);

					String purpose = map.getProperty(DonationMap.Key.PURPOSE_NAME.getKey());
					if (!purpose.contains(donation.getPurpose().getName()))
					{
						purpose = purpose + ", " + donation.getPurpose().getName();
						map.setProperty(DonationMap.Key.PURPOSE_NAME.getKey(), purpose);
					}
				}
			}
		}
		if (this.excludeYear.getYear() > 0)
		{
			donations = query.selectByYear(this.excludeYear);
			for (final Donation donation : donations)
			{
				if (this.printDonation(donation))
				{
					dataMaps.remove(donation.getId().toString());
				}
			}
		}
		return dataMaps.values().toArray(new DataMap<?>[0]);
	}

	private String getKey(Donation donation)
	{
		String id = null;
		if (donation.getLink() == null || !donation.getLink().isValid())
		{
			id = "A" + donation.getAddress().getId().toString();
		}
		else
		{
			id = "P" + donation.getLink().getId().toString();
		}
		return id;
	}
	
	private boolean isNameValid(final Donation donation)
	{
		if (this.selectedName == null || this.selectedName.isEmpty())
		{
			return true;
		}
		if (donation.getAddress().getName().toLowerCase().contains(this.selectedName.toLowerCase()))
		{
			return true;
		}
		if (donation.getLink() != null)
		{
			if (donation.getLink().getPerson().getFirstname().toLowerCase().contains(this.selectedName.toLowerCase()))
			{
				return true;
			}
			if (donation.getLink().getPerson().getLastname().toLowerCase().contains(this.selectedName.toLowerCase()))
			{
				return true;
			}
		}
		return false;
	}

	private boolean printDonation(final Donation donation)
	{
		if (!donation.isValid())
		{
			return false;
		}
		if (this.selectedDonationPurpose == null && this.selectedDomain == null)
		{
			return this.isNameValid(donation);
		}

		if (this.selectedDonationPurpose == null)
		{
			if (this.selectedDomain.getId() != null)
			{
				if (donation.getDomain() == null)
				{
					return false;
				}
				else
				{
					if (this.selectedDomain.getId().equals(donation.getDomain().getId()))
					{
						return this.isNameValid(donation);
					}
					else
					{
						return false;
					}
				}
			}
		}
		if (this.selectedDomain == null)
		{
			return donation.getPurpose().getId().equals(this.selectedDonationPurpose.getId());
		}
		if (donation.getDomain() == null || donation.getPurpose() == null)
		{
			return false;
		}
		if (donation.getDomain() != null && this.selectedDomain.getId().equals(donation.getDomain().getId()))
		{
			if (donation.getPurpose().getId().equals(this.selectedDonationPurpose.getId()))
			{
				return this.isNameValid(donation);
			}
		}
		return false;
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		this.buildDocument();
		super.okPressed();
	}

	@Override
	public void setErrorMessage(final String errorMessage)
	{
		super.setErrorMessage(errorMessage);
		this.setPageComplete(false);
	}

	public void setMessage()
	{
		this.setErrorMessage(null);
		super.setMessage(this.message);
		this.setPageComplete(true);
	}

	public void setPageComplete(final boolean isComplete)
	{
		this.isPageComplete = isComplete;
		if (this.getButton(IDialogConstants.OK_ID) != null)
			this.getButton(IDialogConstants.OK_ID).setEnabled(this.isPageComplete);
	}
}
