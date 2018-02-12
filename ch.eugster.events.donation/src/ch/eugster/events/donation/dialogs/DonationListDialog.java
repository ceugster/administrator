package ch.eugster.events.donation.dialogs;

import java.awt.Cursor;
import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

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
	private ConnectionService connectionService;
	
	private ComboViewer yearFromViewer;

	private ComboViewer yearToViewer;

	private ComboViewer purposeViewer;

	private ComboViewer domainViewer;
	
	private Text name;

	private DonationPurpose allPurposes = DonationPurpose.newInstance("Alle");
	
	private List<Domain> allAndEmptyDomains = new ArrayList<Domain>();

	private DonationYear[] donationYears;
	
	private DonationYear selectedFromDonationYear;

	private DonationYear selectedToDonationYear;

	private DonationPurpose[] donationPurposes;
	
	private DonationPurpose selectedDonationPurpose;

	private Domain[] domains;
	
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

	public DonationListDialog(final Shell parentShell, final ConnectionService connectionService, final DonationYear[] donationYears, final DonationYear selectedDonationYear, final DonationPurpose[] donationPurposes, final DonationPurpose selectedDonationPurpose, final Domain[] domains, final Domain selectedDomain, String selectedName)
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
		this.setTitle(DIALOG_TITLE);
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(4, false));

		Label label = new Label(composite, SWT.None);
		label.setText("Auswahl Jahr von");
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);

		Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(gridData);

		yearFromViewer = new ComboViewer(combo);
		yearFromViewer.setContentProvider(new YearContentProvider());
// r533		yearViewer.setContentProvider(new YearContentProvider(DonationYear.all()));
		yearFromViewer.setLabelProvider(new YearLabelProvider());
		yearFromViewer.setSorter(new YearSorter());
		yearFromViewer.setInput(this.donationYears);
		yearFromViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
// r533			if (ssel.isEmpty())
// 				if (ssel.isEmpty() || ((DonationYear) ssel.getFirstElement()).isAll())
//				{
//					selectedFromDonationYear = null;
//				}
//				else
//				{
				selectedFromDonationYear = (DonationYear) ssel.getFirstElement();
				IStructuredSelection toSsel = (IStructuredSelection) yearToViewer.getSelection();
				if (toSsel.isEmpty() || ((DonationYear) toSsel.getFirstElement()).getYear() > ((DonationYear) ssel.getFirstElement()).getYear())
				{
					yearToViewer.setSelection(ssel);
				}
//				}
			}
		});
		
		label = new Label(composite, SWT.None);
		label.setText("bis");
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);

		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(gridData);

		yearToViewer = new ComboViewer(combo);
		yearToViewer.setContentProvider(new YearContentProvider());
// r533		yearViewer.setContentProvider(new YearContentProvider(DonationYear.all()));
		yearToViewer.setLabelProvider(new YearLabelProvider());
		yearToViewer.setSorter(new YearSorter());
		yearToViewer.setInput(this.donationYears);
		yearToViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
// r533			if (ssel.isEmpty())
// 				if (ssel.isEmpty() || ((DonationYear) ssel.getFirstElement()).isAll())
//				{
//					selectedFromDonationYear = null;
//				}
//				else
//				{
				selectedToDonationYear = (DonationYear) ssel.getFirstElement();
				IStructuredSelection fromSsel = (IStructuredSelection) yearFromViewer.getSelection();
				if (fromSsel.isEmpty() || ((DonationYear) fromSsel.getFirstElement()).getYear() > ((DonationYear) ssel.getFirstElement()).getYear())
				{
					yearFromViewer.setSelection(ssel);
				}
//				}
			}
		});
		yearFromViewer.setSelection(new StructuredSelection(new DonationYear[] { this.selectedFromDonationYear }));
		
		label = new Label(composite, SWT.None);
		label.setText("Zweckfilter");
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		
		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(gridData);
		combo.setCursor(this.getShell().getDisplay().getSystemCursor(Cursor.DEFAULT_CURSOR));

		purposeViewer = new ComboViewer(combo);
		purposeViewer.setContentProvider(new PurposeContentProvider(allPurposes));
		purposeViewer.setLabelProvider(new PurposeLabelProvider());
		purposeViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		purposeViewer.setInput(this.donationPurposes);
		purposeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				if (ssel.isEmpty() || ((DonationPurpose) ssel.getFirstElement()).getId() == null)
				{
					DonationListDialog.this.selectedDonationPurpose= null;
				}
				else
				{
					DonationListDialog.this.selectedDonationPurpose = (DonationPurpose) ssel.getFirstElement();
				}
			}
		});
		purposeViewer.setSelection(new StructuredSelection(new DonationPurpose[] { this.selectedDonationPurpose }));

		label = new Label(composite, SWT.None);
		label.setText("Domänenfilter");
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		combo.setLayoutData(gridData);
		combo.setCursor(this.getShell().getDisplay().getSystemCursor(Cursor.DEFAULT_CURSOR));

		allAndEmptyDomains.add(Domain.newInstance("Alle"));
		allAndEmptyDomains.add(Domain.newInstance("Keine"));

		domainViewer = new ComboViewer(combo);
		domainViewer.setContentProvider(new DomainContentProvider(allAndEmptyDomains));
		domainViewer.setLabelProvider(new DomainLabelProvider());
		domainViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		domainViewer.setInput(this.domains);
		domainViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				if (ssel.isEmpty() || ((Domain) ssel.getFirstElement()).getName().equals("Alle"))
				{
					DonationListDialog.this.selectedDomain= null;
				}
				else
				{
					DonationListDialog.this.selectedDomain = (Domain) ssel.getFirstElement();
				}
			}
		});
		domainViewer.setSelection(new StructuredSelection(new Domain[] { this.selectedDomain }));

		label = new Label(composite, SWT.None);
		label.setText("Namenfilter");
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		name = new Text(composite, SWT.BORDER);
		name.setLayoutData(gridData);
		name.setText(selectedName);
		name.addModifyListener(new ModifyListener() 
		{
			@Override
			public void modifyText(ModifyEvent e) 
			{
				selectedName = name.getText();
			}
		});
		
		return parent;
	}

	private DataMapKey[] getKeys()
	{
		List<DataMapKey> keys = new ArrayList<DataMapKey>();
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
		final DataMap<?>[] dataMaps = createDataMaps();
		if (dataMaps.length == 0)
		{
			MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					MSG_TITLE_NO_BOOKINGS, null, "Die Auswahl enthält keine auswertbaren Elemente.",
					MessageDialog.INFORMATION, new String[] { "OK" }, 0);
			dialog.open();
		}
		else
		{
			Arrays.sort(dataMaps, new Comparator<DataMap<?>>()
			{
				@Override
				public int compare(DataMap<?> map1, DataMap<?> map2)
				{
					String name1 = map1.getProperty("person_lastname");
					String name2 = map2.getProperty("person_lastname");
					int result = compareStrings(name1, name2);
					if (result == 0)
					{
						name1 = map1.getProperty("person_firstname");
						name2 = map2.getProperty("person_firstname");
						result = compareStrings(name1, name2);
						if (result == 0)
						{
							name1 = map1.getProperty("address_name");
							name2 = map2.getProperty("address_name");
							return compareStrings(name1, name2);
						}
					}
					return result;
				}
			});
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
			try
			{
				dialog.run(true, true, new IRunnableWithProgress()
				{
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
					{
						IStatus status = Status.OK_STATUS;
						ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle()
								.getBundleContext(), DocumentBuilderService.class, null);
						try
						{
							tracker.open();
							ServiceReference<DocumentBuilderService>[] references = tracker.getServiceReferences();
							if (references != null)
							{
								try
								{
									monitor.beginTask("Dokument wird erstellt...", references.length);
									for (ServiceReference<DocumentBuilderService> reference : references)
									{
										DocumentBuilderService service = (DocumentBuilderService) tracker
												.getService(reference);
										DocumentBuilderService builderService = service;
										status = builderService.buildDocument(new SubProgressMonitor(monitor,
												dataMaps.length), getKeys(), dataMaps);
										if (status.isOK())
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
								status = new Status(IStatus.ERROR,
										Activator.getDefault().getBundle().getSymbolicName(), MSG_NO_SERVICE_AVAILABLE);
							}
						}
						finally
						{
							tracker.close();
						}
					}
				});
			}
			catch (InvocationTargetException e)
			{
				MessageDialog.openError(getShell(), "Fehler",
						"Bei der Verarbeitung ist ein Fehler aufgetreten.\n(" + e.getLocalizedMessage() + ")");
			}
			catch (InterruptedException e)
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
		this.createButton(parent, IDialogConstants.OK_ID, OK_BUTTON_TEXT, true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, CANCEL_BUTTON_TEXT, false);
	}

	private DataMap<?>[] createDataMaps()
	{
		Map<String, DataMap<?>> dataMaps = new HashMap<String, DataMap<?>>();
		DonationQuery query = (DonationQuery) connectionService.getQuery(Donation.class);
		List<Donation> donations = query.selectByYearRangePurposeDomain(this.selectedFromDonationYear, this.selectedToDonationYear, this.selectedDonationPurpose, this.selectedDomain);
		for (Donation donation : donations)
		{
			if (printDonation(donation))
			{
				DonationMap map = new DonationMap(donation);
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
		return dataMaps.values().toArray(new DataMap<?>[0]);
	}

	private boolean isNameValid(Donation donation)
	{
		if (selectedName == null || selectedName.isEmpty())
		{
			return true;
		}
		if (donation.getAddress().getName().toLowerCase().contains(selectedName.toLowerCase()))
		{
			return true;
		}
		if (donation.getLink() != null)
		{
			if (donation.getLink().getPerson().getFirstname().toLowerCase().contains(selectedName.toLowerCase()))
			{
				return true;
			}
			if (donation.getLink().getPerson().getLastname().toLowerCase().contains(selectedName.toLowerCase()))
			{
				return true;
			}
		}
		return false;
	}

	private boolean printDonation(Donation donation)
	{
		if (!donation.isValid())
		{
			return false;
		}
		if (selectedDonationPurpose == null && selectedDomain == null)
		{
			return isNameValid(donation);
		}

		if (selectedDonationPurpose == null)
		{
			if (selectedDomain.getId() != null)
			{
				if (donation.getDomain() == null)
				{
					return false;
				}
				else
				{
					if (selectedDomain.getId().equals(donation.getDomain().getId()))
					{
						return isNameValid(donation);
					}
					else
					{
						return false;
					}
				}
			}
		}
		if (selectedDomain == null)
		{
			return donation.getPurpose().getId().equals(selectedDonationPurpose.getId());
		}
		if (donation.getDomain() == null || donation.getPurpose() == null)
		{
			return false;
		}
		if (donation.getDomain() != null && selectedDomain.getId().equals(donation.getDomain().getId()))
		{
			if (donation.getPurpose().getId().equals(selectedDonationPurpose.getId()))
			{
				return isNameValid(donation);
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
		buildDocument();
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
