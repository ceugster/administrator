package ch.eugster.events.donation.dialogs;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.documents.maps.AddressMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.LinkMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.donation.Activator;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.UserProperty;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationConfirmationDialog extends TitleAreaDialog
{
	private Text documentPath;

	private Button documentSelector;

	private Button yearSelector;

	private Spinner year;

	private final StructuredSelection selection;

	private UserProperty userPropertyTemplatePath;

	private final String message = "Wählen Sie das Dokument, das als Vorlage verwendet werden soll.";

	private static final String MSG_NO_SERVICE_AVAILABLE = "Es ist kein Service für die Verarbeitung des Dokuments verfügbar.";

	private static final String MSG_TITLE_NO_BOOKINGS = "Keine Spenden vorhanden";

	private static final String OK_BUTTON_TEXT = "Generieren";

	private static final String CANCEL_BUTTON_TEXT = "Abbrechen";

	private static final String DIALOG_TITLE = "Vorlage Spendenbestätigung";

	private boolean isPageComplete = false;

	public DonationConfirmationDialog(final Shell parentShell, final StructuredSelection selection)
	{
		super(parentShell);
		this.selection = selection;
	}

	private void buildDocument(final Collection<DataMap> dataMaps)
	{
		Collection<DataMap> maps = createDataMaps();
		if (maps.size() == 0)
		{
			MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					MSG_TITLE_NO_BOOKINGS, null, "Die Auswahl enthält keine auswertbaren Elemente.",
					MessageDialog.INFORMATION, new String[] { "OK" }, 0);
			dialog.open();
		}
		else
		{
			UIJob job = new UIJob("Generiere Dokument...")
			{
				@Override
				public IStatus runInUIThread(final IProgressMonitor monitor)
				{
					IStatus status = Status.OK_STATUS;
					ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
							DocumentBuilderService.class.getName(), null);
					try
					{
						tracker.open();
						ServiceReference[] references = tracker.getServiceReferences();
						if (references != null)
						{
							for (ServiceReference reference : references)
							{
								DocumentBuilderService service = (DocumentBuilderService) tracker.getService(reference);
								DocumentBuilderService builderService = service;
								status = builderService.buildDocument(new File(userPropertyTemplatePath.getValue()),
										dataMaps);
								if (status.isOK())
								{
									break;
								}
							}
						}
						else
						{
							status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
									MSG_NO_SERVICE_AVAILABLE);
						}
					}
					finally
					{
						tracker.close();
					}
					return status;
				}
			};
			job.addJobChangeListener(new JobChangeAdapter()
			{
				@Override
				public void done(final IJobChangeEvent event)
				{
					IStatus status = event.getResult();
					if (!status.isOK())
					{
						if (status.getSeverity() == IStatus.CANCEL)
						{
							MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(), MSG_TITLE_NO_BOOKINGS, null, status
									.getMessage(), MessageDialog.INFORMATION, new String[] { "OK" }, 0);
							dialog.open();
						}
						else
						{
							ErrorDialog dialog = new ErrorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
									.getShell(), MSG_TITLE_NO_BOOKINGS, status.getMessage(), status, 0);
							dialog.open();
						}
					}
				}
			});
			job.setUser(true);
			job.schedule();
		}
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, OK_BUTTON_TEXT, true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, CANCEL_BUTTON_TEXT, false);
		File file = new File(documentPath.getText());
		this.getButton(IDialogConstants.OK_ID).setEnabled(file.isFile());
	}

	private Collection<DataMap> createDataMaps()
	{
		Map<String, DataMap> dataMaps = new HashMap<String, DataMap>();
		Object[] elements = selection.toArray();
		for (Object element : elements)
		{
			if (element instanceof DonationYear)
			{
				DonationYear donationYear = (DonationYear) element;
				createDataMaps(donationYear, dataMaps);
			}
			else if (element instanceof LinkPersonAddress)
			{
				LinkPersonAddress link = (LinkPersonAddress) element;
				Integer year = yearSelector == null || !yearSelector.getSelection() ? null : Integer.valueOf(this.year
						.getSelection());
				createDataMaps(link, year, dataMaps);
			}
			else if (element instanceof Person)
			{
				Person person = (Person) element;
				Integer year = yearSelector == null || !yearSelector.getSelection() ? null : Integer.valueOf(this.year
						.getSelection());
				createDataMaps(person.getDefaultLink(), year, dataMaps);
			}
			else if (element instanceof Address)
			{
				Address address = (Address) element;
				Integer year = yearSelector == null || !yearSelector.getSelection() ? null : Integer.valueOf(this.year
						.getSelection());
				createDataMaps(address, year, dataMaps);
			}
			else if (element instanceof Donation)
			{
				Donation donation = (Donation) element;
				createDataMaps(donation, Integer.valueOf(donation.getDonationYear()), dataMaps);
			}
		}
		return dataMaps.values();
	}

	private void createDataMaps(final Address address, final Integer year, final Map<String, DataMap> dataMaps)
	{
		String key = "A" + address.getId().toString();
		DataMap addressMap = dataMaps.get(key);
		if (addressMap == null)
		{
			if (year != null)
			{
				addressMap = new AddressMap(address, year.intValue());
			}
			else
			{
				addressMap = new AddressMap(address);
			}
			dataMaps.put(key, addressMap);
		}
	}

	private void createDataMaps(final Donation donation, final Integer year, final Map<String, DataMap> dataMaps)
	{
		if (!donation.isDeleted())
		{
			if (donation.getLink() == null)
			{
				createDataMaps(donation.getAddress(), year, dataMaps);
			}
			else
			{
				createDataMaps(donation.getLink(), year, dataMaps);
			}
		}
	}

	private void createDataMaps(final DonationYear donationYear, final Map<String, DataMap> dataMaps)
	{
		for (Donation donation : donationYear.getDonations())
		{
			if (!donation.isDeleted())
			{
				if (donation.getLink() == null)
				{
					createDataMaps(donation.getAddress(), Integer.valueOf(donationYear.getYear()), dataMaps);
				}
				else
				{
					createDataMaps(donation.getLink(), Integer.valueOf(donationYear.getYear()), dataMaps);
				}
			}
		}
	}

	private void createDataMaps(final LinkPersonAddress link, final Integer year, final Map<String, DataMap> dataMaps)
	{
		String key = "P" + link.getId().toString();
		DataMap linkMap = dataMaps.get(key);
		if (linkMap == null)
		{
			if (year != null)
			{
				linkMap = new LinkMap(link, year.intValue());
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
				dialog.setFilterExtensions(new String[] { "*.odt" });
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

		if (selection.getFirstElement() instanceof LinkPersonAddress || selection.getFirstElement() instanceof Person
				|| selection.getFirstElement() instanceof Address)
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

			Object[] elements = selection.toArray();
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
		Collection<DataMap> dataMaps = createDataMaps();
		super.okPressed();
		buildDocument(dataMaps);
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

	private void setRange(final Collection<Donation> donations)
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
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
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
}
