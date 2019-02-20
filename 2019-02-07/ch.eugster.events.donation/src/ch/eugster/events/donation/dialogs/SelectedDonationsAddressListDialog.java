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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
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
import ch.eugster.events.persistence.model.Donation;

public class SelectedDonationsAddressListDialog extends TitleAreaDialog
{
	/*
	 * in SelectionMode DONATIONS
	 */
	private final Donation[] selectedDonations;
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

	public SelectedDonationsAddressListDialog(final Shell parentShell, final Donation[] selectedDonations)
	{
		super(parentShell);
		this.selectedDonations = selectedDonations;
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
		Map<String, Donation> donators = new HashMap<String, Donation>();
		for (Donation donation : selectedDonations)
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
		return dataMaps.values().toArray(new DataMap<?>[0]);
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle(DIALOG_TITLE);
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(2, false));

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

}
