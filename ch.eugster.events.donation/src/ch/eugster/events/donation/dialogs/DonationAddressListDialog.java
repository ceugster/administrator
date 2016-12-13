package ch.eugster.events.donation.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
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
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.DonationQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationAddressListDialog extends TitleAreaDialog
{
	private SelectionMode selectionMode;

	/*
	 * in SelectionMode PERSON, LINK, ADDRESS
	 */
	private IStructuredSelection personSelection;

	private Button yearSelector;

	private Spinner year;

	/*
	 * in SelectionMode DONATIONS
	 */
	private final Donation[] selectedDonations;

	/*
	 * in SelectionMode YEAR
	 */
	
	private Button printAllDonations;
	
	private DonationYear selectedDonationYear;

	private DonationPurpose selectedDonationPurpose;

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

	public DonationAddressListDialog(final Shell parentShell, final Donation[] selectedDonations)
	{
		super(parentShell);
		this.personSelection = null;
		this.selectedDonations = selectedDonations;
		this.selectedDonationYear = null;
		this.selectedDonationPurpose = null;
		this.selectedDomain = null;
		this.selectionMode = SelectionMode.DONATIONS;
	}

	public DonationAddressListDialog(final Shell parentShell, final DonationYear selectedDonationYear,
			final DonationPurpose selectedDonationPurpose, final Domain selectedDomain, String selectedName)
	{
		super(parentShell);
		this.personSelection = null;
		this.selectedDonations = null;
		this.selectedDonationYear = selectedDonationYear;
		this.selectedDonationPurpose = selectedDonationPurpose;
		this.selectedDomain = selectedDomain;
		this.selectedName = selectedName;
		this.selectionMode = SelectionMode.YEAR;
	}

	public DonationAddressListDialog(final Shell parentShell, final IStructuredSelection ssel)
	{
		super(parentShell);
		this.personSelection = ssel;
		this.selectedDonations = null;
		this.selectedDonationYear = null;
		this.selectedDonationPurpose = null;
		this.selectedDomain = null;
		this.selectionMode = SelectionMode.PERSON;
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
		final DataMap<?>[] dataMaps = createDataMaps(selectionMode);
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

	private DataMap<?>[] createDataMaps(SelectionMode selectionMode)
	{
		Map<String, DataMap<?>> dataMaps = new HashMap<String, DataMap<?>>();
		if (selectionMode.equals(SelectionMode.PERSON))
		{
			Object[] elements = personSelection.toArray();
			for (Object element : elements)
			{
				if (element instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) element;
					Integer year = yearSelector == null || !yearSelector.getSelection() ? null : Integer
							.valueOf(this.year.getSelection());
					createDataMaps(link, year, selectedDonationPurpose, selectedDomain, dataMaps);
				}
				else if (element instanceof Person)
				{
					Person person = (Person) element;
					Integer year = yearSelector == null || !yearSelector.getSelection() ? null : Integer
							.valueOf(this.year.getSelection());
					createDataMaps(person.getDefaultLink(), year, selectedDonationPurpose, selectedDomain, dataMaps);
				}
				else if (element instanceof Address)
				{
					Address address = (Address) element;
					Integer year = yearSelector == null || !yearSelector.getSelection() ? null : Integer
							.valueOf(this.year.getSelection());
					createDataMaps(address, year, selectedDonationPurpose, selectedDomain, dataMaps);
				}
			}
		}
		else if (selectionMode.equals(SelectionMode.DONATIONS))
		{
			for (Donation donation : selectedDonations)
			{
				createDataMaps(donation, donation.getDonationYear(), selectedDonationPurpose, selectedDomain, dataMaps);

			}
		}
		else if (selectionMode.equals(SelectionMode.YEAR))
		{
			if (this.printAllDonations.getSelection())
			{
				createDataMaps(dataMaps);
			}
			else
			{
				createDataMaps(selectedDonationYear, selectedDonationPurpose, selectedDomain, dataMaps);
			}
		}
		return dataMaps.values().toArray(new DataMap<?>[0]);
	}

	private void createDataMaps(final Map<String, DataMap<?>> dataMaps)
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = tracker.getService();
			if (service != null)
			{
				DonationQuery query = (DonationQuery) service.getQuery(Donation.class);
				createDataMaps(query.selectValids(), dataMaps);
			}
			else
			{
				MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Keine Verbindung", null, "Es konnte keine Verbindung zur Datenbank hergestellt werden.",
						MessageDialog.INFORMATION, new String[] { "OK" }, 0);
				dialog.open();
			}
		}
		finally
		{
			tracker.close();
		}
	}
	
	private void createDataMaps(final Address address, final Integer year, DonationPurpose purpose, Domain domain,
			final Map<String, DataMap<?>> dataMaps)
	{
		String key = "A" + address.getId().toString();
		DataMap<?> addressMap = dataMaps.get(key);
		if (addressMap == null)
		{
			if (year != null)
			{
				addressMap = new AddressMap(address, year.intValue(), purpose, domain, true);
			}
			else
			{
				addressMap = new AddressMap(address);
			}
			dataMaps.put(key, addressMap);
		}
	}

	private void createDataMaps(final Donation donation, final Integer year, DonationPurpose purpose, Domain domain,
			final Map<String, DataMap<?>> dataMaps)
	{
		if (!donation.isValid())
		{
			return;
		}
		if (donation.getLink() == null || !donation.getLink().isValid())
		{
			createDataMaps(donation.getAddress(), year, purpose, domain, dataMaps);
		}
		else
		{
			createDataMaps(donation.getLink(), year, purpose, domain, dataMaps);
		}
	}

	private void createDataMaps(final DonationYear donationYear, DonationPurpose purpose, Domain domain,
			final Map<String, DataMap<?>> dataMaps)
	{
		for (Donation donation : donationYear.getDonations())
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
	}

	private void createDataMaps(List<Donation> donations, final Map<String, DataMap<?>> dataMaps)
	{
		Map<String, Donation> donators = new HashMap<String, Donation>();
		for (Donation donation : donations)
		{
			if (donation.isValid())
			{
				String key = null;
				Donation d = null;
				if (donation.getLink() == null || !donation.getLink().isValid())
				{
					key = "A" + donation.getAddress().getId().toString();
					d = donators.get(key);
					if (d == null)
					{
						d = Donation.newInstance(donation.getAddress());
						d.setId(donation.getId());
						donators.put(key, d);
					}
				}
				else
				{
					key = "P" + donation.getLink().getId().toString();
					d = donators.get(key);
					if (d == null)
					{
						d = Donation.newInstance(donation.getLink());
						d.setId(donation.getId());
						donators.put(key, d);
					}
				}
				d.setAmount(d.getAmount() + donation.getAmount());
			}
		}
		if (!donators.isEmpty())
		{
			Collection<Donation> ds = donators.values();
			for (Donation d : ds)
			{
				DonationMap map = new DonationMap(d);
				if (d.getLink() == null || !d.getLink().isValid())
				{
					map.setProperties(new AddressMap(d.getAddress()).getProperties());
				}
				else
				{
					map.setProperties(new LinkMap(d.getLink()).getProperties());
				}
				dataMaps.put(d.getId().toString(), map);
			}
		}
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

	private void createDataMaps(final LinkPersonAddress link, final Integer year, DonationPurpose purpose,
			Domain domain, final Map<String, DataMap<?>> dataMaps)
	{
		String key = "P" + link.getId().toString();
		DataMap<?> linkMap = dataMaps.get(key);
		if (linkMap == null)
		{
			if (year != null)
			{
				linkMap = new LinkMap(link, year.intValue(), purpose, domain, false);
			}
			else
			{
				linkMap = new LinkMap(link);
			}
			dataMaps.put(key, linkMap);
		}
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle(DIALOG_TITLE);
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(3, false));

		if (selectionMode.equals(SelectionMode.YEAR))
		{
			GridData gridData = new GridData();
			gridData.heightHint = 12;
			gridData.horizontalSpan = 3;

			Label label = new Label(composite, SWT.None);
			label.setLayoutData(gridData);

			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 3;

			printAllDonations = new Button(composite, SWT.CHECK);
			printAllDonations.setLayoutData(gridData);
			printAllDonations.setText("Sämtliche Spender auflisten (wenn nicht aktiviert, wird die aktuelle Selektion gelistet).");
			printAllDonations.addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetSelected(SelectionEvent e) 
				{
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) 
				{
					widgetSelected(e);
				}
			});
		}

//		Label label = new Label(composite, SWT.None);
//		label.setLayoutData(new GridData());
//		label.setText("Vorlage");
//
//		if (selectionMode.equals(SelectionMode.PERSON))
//		{
//			yearSelector = new Button(composite, SWT.CHECK);
//			yearSelector.setLayoutData(new GridData());
//			yearSelector.setText("Auswahl Jahr");
//
//			GridData gridData = new GridData();
//			gridData.widthHint = 48;
//			gridData.horizontalSpan = 2;
//
//			year = new Spinner(composite, SWT.None);
//			year.setLayoutData(gridData);
//			year.setDigits(0);
//			year.setMinimum(0);
//			year.setMaximum(Integer.MAX_VALUE);
//			year.setIncrement(1);
//			year.setPageIncrement(10);
//
//			Object[] elements = personSelection.toArray();
//			for (Object element : elements)
//			{
//				if (element instanceof Address)
//				{
//					Address address = (Address) element;
//					setRange(address.getDonations());
//				}
//				else if (element instanceof LinkPersonAddress)
//				{
//					LinkPersonAddress link = (LinkPersonAddress) element;
//					setRange(link.getDonations());
//				}
//				if (element instanceof Person)
//				{
//					Person person = (Person) element;
//					setRange(person.getDonations());
//				}
//			}
//			year.setSelection(year.getMaximum());
//			yearSelector.setSelection(true);
//		}

		return parent;
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

	private void setRange(final List<Donation> donations)
	{
		for (Donation donation : donations)
		{
			if (year.getMinimum() == 0 || donation.getDonationYear() < year.getMinimum())
			{
				year.setMinimum(donation.getDonationYear());
			}
			if (year.getMaximum() == Integer.MAX_VALUE || donation.getDonationYear() > year.getMaximum())
			{
				year.setMaximum(donation.getDonationYear());
			}
		}
	}

	private enum SelectionMode
	{
		DONATIONS, YEAR, PERSON;
	}
}
