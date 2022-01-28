package ch.eugster.events.donation.dialogs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.documents.maps.AddressMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.LinkMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.donation.Activator;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.UserProperty;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationConfirmationDialog extends TitleAreaDialog
{
	private SelectionMode selectionMode;

	private Text documentPath;

	private Button documentSelector;

	private UserProperty userPropertyTemplatePath;

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
	private DonationYear selectedDonationYear;

	private DonationPurpose selectedDonationPurpose;

	private Domain selectedDomain;

	private String selectedName;

	private final String message = "Wählen Sie das Dokument, das als Vorlage verwendet werden soll.";

	private static final String MSG_NO_SERVICE_AVAILABLE = "Es ist kein Service für die Verarbeitung des Dokuments verfügbar.";

	private static final String MSG_TITLE_NO_BOOKINGS = "Keine Spenden vorhanden";

	private static final String OK_BUTTON_TEXT = "Generieren";

	private static final String CANCEL_BUTTON_TEXT = "Abbrechen";

	private static final String DIALOG_TITLE = "Vorlage Spendenbestätigung";

	private boolean isPageComplete = false;

	public DonationConfirmationDialog(final Shell parentShell, final Donation[] selectedDonations)
	{
		super(parentShell);
		this.personSelection = null;
		this.selectedDonations = selectedDonations;
		this.selectedDonationYear = null;
		this.selectedDonationPurpose = null;
		this.selectedDomain = null;
		this.selectionMode = SelectionMode.DONATIONS;
	}

	public DonationConfirmationDialog(final Shell parentShell, final DonationYear selectedDonationYear,
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

	public DonationConfirmationDialog(final Shell parentShell, final IStructuredSelection ssel)
	{
		super(parentShell);
		this.personSelection = ssel;
		this.selectedDonations = null;
		this.selectedDonationYear = null;
		this.selectedDonationPurpose = null;
		this.selectedDomain = null;
		this.selectionMode = SelectionMode.PERSON;
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
							result = compareStrings(name1, name2);
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
												dataMaps.length), new File(userPropertyTemplatePath.getValue()),
												dataMaps);
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
		File file = new File(documentPath.getText());
		this.getButton(IDialogConstants.OK_ID).setEnabled(file.isFile());
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
			createDataMaps(selectedDonationYear, selectedDonationPurpose, selectedDomain, dataMaps);
		}
		return dataMaps.values().toArray(new DataMap<?>[0]);
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
		if (!donation.isDeleted())
		{
			if (donation.getLink() == null || donation.getLink().isDeleted()
					|| donation.getLink().getPerson().isDeleted())
			{
				createDataMaps(donation.getAddress(), year, purpose, domain, dataMaps);
			}
			else
			{
				createDataMaps(donation.getLink(), year, purpose, domain, dataMaps);
			}
		}
	}

	private void createDataMaps(final DonationYear donationYear, DonationPurpose purpose, Domain domain,
			final Map<String, DataMap<?>> dataMaps)
	{
		for (Donation donation : donationYear.getDonations())
		{
			if (printDonation(donation))
			{
				if (donation.getLink() == null || donation.getLink().isDeleted()
						|| donation.getLink().getPerson().isDeleted())
				{
					createDataMaps(donation, Integer.valueOf(donationYear.getYear()), purpose, domain, dataMaps);
				}
				else
				{
					createDataMaps(donation, Integer.valueOf(donationYear.getYear()), purpose, domain, dataMaps);
				}
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
		if (donation.getLink() != null || donation.getLink().isDeleted() || donation.getLink().getPerson().isDeleted())
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
		if (donation.isDeleted())
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

		Label label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Vorlage");

		File file = null;
		if (User.getCurrent() != null)
		{
			this.userPropertyTemplatePath = User.getCurrent().getProperty(
					UserProperty.Property.DONATION_CONFIRMATION_TEMPLATE_PATH.key());
			if (this.userPropertyTemplatePath == null)
			{
				this.userPropertyTemplatePath = UserProperty.newInstance(User.getCurrent());
				this.userPropertyTemplatePath.setKey(UserProperty.Property.DONATION_CONFIRMATION_TEMPLATE_PATH.key());
				this.userPropertyTemplatePath.setValue(System.getProperty("user.home"));
			}
			file = new File(this.userPropertyTemplatePath.getValue());
		}
		documentPath = new Text(composite, SWT.BORDER | SWT.SINGLE);
		documentPath.setText(file.getAbsolutePath());
		documentPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		documentPath.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				File file = new File(documentPath.getText());
				if (file.exists())
				{
					userPropertyTemplatePath.setValue(file.getAbsolutePath());
				}
			}
		});

		documentSelector = new Button(composite, SWT.PUSH);
		documentSelector.setText("...");
		documentSelector.setLayoutData(new GridData());
		documentSelector.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				String path = DonationConfirmationDialog.this.documentPath.getText();
				FileDialog dialog = new FileDialog(DonationConfirmationDialog.this.getShell());
				dialog.setFilterPath(path);
				dialog.setFilterExtensions(new String[] { "*.odt", "*.docx" });
				dialog.setText(DIALOG_TITLE);
				path = dialog.open();
				if (path != null)
				{
					DonationConfirmationDialog.this.documentPath.setText(path);

				}
				File file = new File(DonationConfirmationDialog.this.documentPath.getText());
				if (file.exists())
				{
					userPropertyTemplatePath.setValue(file.getAbsolutePath());
				}
				DonationConfirmationDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(file.isFile());
			}
		});

		if (selectionMode.equals(SelectionMode.PERSON))
		{
			yearSelector = new Button(composite, SWT.CHECK);
			yearSelector.setLayoutData(new GridData());
			yearSelector.setText("Auswahl Jahr");

			GridData gridData = new GridData();
			gridData.widthHint = 48;
			gridData.horizontalSpan = 2;

			year = new Spinner(composite, SWT.None);
			year.setLayoutData(gridData);
			year.setDigits(0);
			year.setMinimum(0);
			year.setMaximum(Integer.MAX_VALUE);
			year.setIncrement(1);
			year.setPageIncrement(10);

			Object[] elements = personSelection.toArray();
			for (Object element : elements)
			{
				if (element instanceof Address)
				{
					Address address = (Address) element;
					setRange(address.getDonations());
				}
				else if (element instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) element;
					setRange(link.getDonations());
				}
				if (element instanceof Person)
				{
					Person person = (Person) element;
					setRange(person.getDonations());
				}
			}
			year.setSelection(year.getMaximum());
			yearSelector.setSelection(true);
		}

		return parent;
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		setUserPath();
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

	private void setUserPath()
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		try
		{
			tracker.open();
			Object service = tracker.getService();
			if (service instanceof ConnectionService)
			{
				this.userPropertyTemplatePath.setUser(User.getCurrent());
				ConnectionService connectionService = (ConnectionService) service;
				User.getCurrent().setProperty(this.userPropertyTemplatePath);
				UserQuery query = (UserQuery) connectionService.getQuery(User.class);
				User.setCurrent(query.merge(User.getCurrent()));
			}
		}
		finally
		{
			tracker.close();
		}
	}

	private enum SelectionMode
	{
		DONATIONS, YEAR, PERSON;
	}
}
