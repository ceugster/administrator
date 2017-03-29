package ch.eugster.events.donation.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
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
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.editors.EditorSelector;

public class SelectedDonationListDialog extends TitleAreaDialog
{
	private Button ListSelector;

	private final StructuredSelection selection;

	private final String message = "Erstellen einer Adressliste der ausgewählten Spenden.";

	private boolean isPageComplete = false;

	public SelectedDonationListDialog(final Shell parentShell, final StructuredSelection selection)
	{
		super(parentShell);
		this.selection = selection;
	}

	private void buildDocument(final DataMapKey[] keys, final DataMap<?>[] dataMaps)
	{
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try
		{
			dialog.run(true, true, new IRunnableWithProgress()
			{
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle().getBundleContext(),
							DocumentBuilderService.class, null);
					tracker.open();
					try
					{
						Object[] services = tracker.getServices();
						if (services != null)
						{
							IStatus status = Status.CANCEL_STATUS;
							try
							{
								monitor.beginTask("Dokument wird erstellt...", services.length);
								for (Object service : services)
								{
									if (status.isOK())
									{
										monitor.worked(1);
										return;
									}
									if (service instanceof DocumentBuilderService)
									{
										DocumentBuilderService builderService = (DocumentBuilderService) service;
										status = builderService.buildDocument(
												new SubProgressMonitor(monitor, dataMaps.length), keys, dataMaps);
										if (status.isOK())
										{
											break;
										}
									}
									monitor.worked(1);
								}
							}
							finally
							{
								monitor.done();
							}
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

	private void computeDonation(final List<DataMap<?>> maps, final Donation donation)
	{
		if (!donation.isDeleted())
		{
			maps.add(new DonationMap(donation));
		}
	}

	private void computeDonationYear(final List<DataMap<?>> maps, final DonationYear year)
	{
		if (!year.isDeleted())
		{
			List<Donation> donations = year.getDonations();
			for (Donation donation : donations)
			{
				computeDonation(maps, donation);
			}
		}
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, "Generieren", true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
	}

	private DataMap<?>[] createDataMaps(final StructuredSelection ssel)
	{
		List<DataMap<?>> maps = new ArrayList<DataMap<?>>();
		Object[] elements = ssel.toArray();
		for (Object element : elements)
		{
			if (element instanceof DonationYear)
			{
				DonationYear year = (DonationYear) element;
				computeDonationYear(maps, year);
			}
			else if (element instanceof Donation)
			{
				Donation donation = (Donation) element;
				computeDonation(maps, donation);
			}
		}
		return maps.toArray(new DataMap<?>[0]);
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		if (EditorSelector.values()[PersonSettings.getInstance().getEditorSelector()]
				.equals(EditorSelector.MULTI_PAGE_EDITOR))
		{
			ListSelector = new Button(composite, SWT.CHECK);
			ListSelector.setText("Gruppenadressen nur einmal auflisten");
			ListSelector.setLayoutData(new GridData());
		}

		return parent;
	}

	private DataMapKey[] getKeys()
	{
		List<DataMapKey> keys = new ArrayList<DataMapKey>();
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
		keys.add(LinkMap.Key.PHONE);
		keys.add(AddressMap.Key.PHONE);
		keys.add(PersonMap.Key.PHONE);
		keys.add(AddressMap.Key.FAX);
		keys.add(PersonMap.Key.EMAIL);
		keys.add(PersonMap.Key.WEBSITE);
		keys.add(AddressMap.Key.ADDRESS);
		keys.add(AddressMap.Key.POB);
		keys.add(AddressMap.Key.COUNTRY);
		keys.add(AddressMap.Key.ZIP);
		keys.add(AddressMap.Key.CITY);
		keys.add(AddressMap.Key.COUNTY);
		keys.add(DonationMap.Key.MAILING_ADDRESS);
		keys.add(DonationMap.Key.POLITE);
		keys.add(DonationMap.Key.DATE);
		keys.add(DonationMap.Key.YEAR);
		keys.add(DonationMap.Key.PURPOSE_CODE);
		keys.add(DonationMap.Key.PURPOSE_DESCRIPTION);
		keys.add(DonationMap.Key.PURPOSE_NAME);
		keys.add(DonationMap.Key.AMOUNT);
		keys.addAll(PersonMap.getExtendedFieldKeys());
		keys.addAll(LinkMap.getExtendedFieldKeys());
		return keys.toArray(new DataMapKey[0]);
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		setCurrentUser();
		final DataMapKey[] keys = getKeys();
		final DataMap<?>[] dataMaps = createDataMaps(selection);

		super.okPressed();

		buildDocument(keys, dataMaps);
	}

	private void setCurrentUser()
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				UserQuery query = (UserQuery) service.getQuery(User.class);
				User.setCurrent(query.merge(User.getCurrent()));
			}
		}
		finally
		{
			tracker.close();
		}
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

	public void setTitle()
	{
		super.setTitle("Adressliste generieren");
	}

}
