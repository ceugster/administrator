package ch.eugster.events.donation.dialogs;

import java.awt.Cursor;
import java.io.File;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.documents.maps.AbstractDataMap;
import ch.eugster.events.documents.maps.AddressMap;
import ch.eugster.events.documents.maps.AddressMap.TableKey;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DonationMap;
import ch.eugster.events.documents.maps.LinkMap;
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
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.UserProperty;
import ch.eugster.events.persistence.queries.DonationQuery;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class DonationLetterDialog extends TitleAreaDialog
{
	private final ConnectionService connectionService;

	private ComboViewer yearFromViewer;

	private ComboViewer yearToViewer;

	private ComboViewer excludeYearViewer;

	private ComboViewer purposeViewer;

	private ComboViewer domainViewer;

	private Text name;

	private Text template;

	private Button selectTemplate;

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
	
	private UserProperty userPropertyTemplatePath;

	
	/*
	 * Common
	 */

	private final String message = "W�hlen Sie die gew�nschten Optionen.";

	private static final String MSG_NO_SERVICE_AVAILABLE = "Es ist kein Service f�r die Verarbeitung des Dokuments verf�gbar.";

	private static final String MSG_TITLE_NO_BOOKINGS = "Keine Spenden vorhanden";

	private static final String OK_BUTTON_TEXT = "Generieren";

	private static final String CANCEL_BUTTON_TEXT = "Abbrechen";

	private static final String DIALOG_TITLE = "Spendenbriefe";

	private boolean isPageComplete = false;

	public DonationLetterDialog(final Shell parentShell, final ConnectionService connectionService, final DonationYear[] donationYears, final DonationYear selectedDonationYear, final DonationPurpose[] donationPurposes, final DonationPurpose selectedDonationPurpose, final Domain[] domains, final Domain selectedDomain, final String selectedName)
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
		this.setTitle(DonationLetterDialog.DIALOG_TITLE);
		this.setMessage();

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(4, false));

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
				DonationLetterDialog.this.selectedFromDonationYear = (DonationYear) ssel.getFirstElement();
				final IStructuredSelection toSsel = (IStructuredSelection) DonationLetterDialog.this.yearToViewer.getSelection();
				if (toSsel.isEmpty() || ((DonationYear) toSsel.getFirstElement()).getYear() < ((DonationYear) ssel.getFirstElement()).getYear())
				{
					DonationLetterDialog.this.yearToViewer.setSelection(ssel);
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
				DonationLetterDialog.this.selectedToDonationYear = (DonationYear) ssel.getFirstElement();
				final IStructuredSelection fromSsel = (IStructuredSelection) DonationLetterDialog.this.yearFromViewer.getSelection();
				if (fromSsel.isEmpty() || ((DonationYear) fromSsel.getFirstElement()).getYear() > ((DonationYear) ssel.getFirstElement()).getYear())
				{
					DonationLetterDialog.this.yearFromViewer.setSelection(ssel);
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
				DonationLetterDialog.this.excludeYear = (DonationYear) ssel.getFirstElement();
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
					DonationLetterDialog.this.selectedDonationPurpose = null;
				}
				else
				{
					DonationLetterDialog.this.selectedDonationPurpose = (DonationPurpose) ssel.getFirstElement();
				}
			}
		});
		this.purposeViewer.setSelection(new StructuredSelection(new DonationPurpose[] { this.selectedDonationPurpose }));

		label = new Label(composite, SWT.None);
		label.setText("Dom�nenfilter");
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
					DonationLetterDialog.this.selectedDomain = null;
				}
				else
				{
					DonationLetterDialog.this.selectedDomain = (Domain) ssel.getFirstElement();
				}
			}
		});
		this.domainViewer.setSelection(new StructuredSelection(new Domain[] { this.selectedDomain }));

		label = new Label(composite, SWT.None);
		label.setText("Namenfilter");
		label.setLayoutData(new GridData());
		
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 0;
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
				DonationLetterDialog.this.selectedName = DonationLetterDialog.this.name.getText();
			}
		});
		
		final Button clearName = new Button(nameComposite, SWT.PUSH);
		clearName.setLayoutData(new GridData());
		clearName.setImage(Activator.getDefault().getImageRegistry().get("CLEAR"));
		clearName.addSelectionListener(new SelectionAdapter() 
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				DonationLetterDialog.this.name.setText("");
			}
		});

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

		label = new Label(composite, SWT.None);
		label.setText("Briefvorlage");
		label.setLayoutData(new GridData());
		
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		layout = new GridLayout(2, false);
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		final Composite pathComposite = new Composite(composite, SWT.NONE);
		pathComposite.setLayoutData(gridData);
		pathComposite.setLayout(layout);
		
		this.template = new Text(pathComposite, SWT.BORDER);
		this.template.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.template.setText(file == null ? "" : file.getAbsolutePath());
		this.template.addVerifyListener(new VerifyListener()
		{
			@Override
			public void verifyText(final VerifyEvent e)
			{
				final boolean isFile = new File(e.text).isFile();
				DonationLetterDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(isFile);
				DonationLetterDialog.this.userPropertyTemplatePath.setValue(isFile ? e.text : "");
			}
		});

		this.selectTemplate = new Button(pathComposite, SWT.PUSH);
		this.selectTemplate.setLayoutData(new GridData());
		this.selectTemplate.setImage(Activator.getDefault().getImageRegistry().get("LOAD_LETTER"));
		this.selectTemplate.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				final FileDialog dialog = new FileDialog(DonationLetterDialog.this.getShell());
				dialog.setFilterPath(DonationLetterDialog.this.template.getText());
				dialog.setFilterExtensions(new String[] { "*.odt", "*.docx" });
				dialog.setText("Auswahl Vorlage");
				final String path = dialog.open();
				if (path != null)
				{
					DonationLetterDialog.this.template.setText(path);
					DonationLetterDialog.this.userPropertyTemplatePath.setValue(path);
				}
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}
		});
		
		return composite;
	}

	private void buildDocument()
	{
		final DataMap<?>[] dataMaps = this.createDataMaps();
		if (dataMaps.length == 0)
		{
			final MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), DonationLetterDialog.MSG_TITLE_NO_BOOKINGS, null, "Die Auswahl enth�lt keine auswertbaren Elemente.", MessageDialog.INFORMATION, new String[] { "OK" }, 0);
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
					int result = DonationLetterDialog.this.compareStrings(name1, name2);
					if (result == 0)
					{
						name1 = map1.getProperty("person_firstname");
						name2 = map2.getProperty("person_firstname");
						result = DonationLetterDialog.this.compareStrings(name1, name2);
						if (result == 0)
						{
							name1 = map1.getProperty("address_name");
							name2 = map2.getProperty("address_name");
							return DonationLetterDialog.this.compareStrings(name1, name2);
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
						IStatus status = Status.OK_STATUS;
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
										final DocumentBuilderService service = tracker
												.getService(reference);
										final DocumentBuilderService builderService = service;
										status = builderService.buildDocument(new SubProgressMonitor(monitor,
												dataMaps.length), new File(DonationLetterDialog.this.userPropertyTemplatePath.getValue()),
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
								status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), DonationLetterDialog.MSG_NO_SERVICE_AVAILABLE);
							}
						}
						finally
						{
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
		this.createButton(parent, IDialogConstants.OK_ID, DonationLetterDialog.OK_BUTTON_TEXT, true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, DonationLetterDialog.CANCEL_BUTTON_TEXT, false);
		this.getButton(IDialogConstants.OK_ID).setEnabled(this.userPropertyTemplatePath.getValue() == null || this.userPropertyTemplatePath.getValue().isEmpty() ? false : new File(this.userPropertyTemplatePath.getValue()).isFile());
	}

	private DataMap<?>[] createDataMaps()
	{
		final Map<String, DataMap<?>> dataMaps = new HashMap<String, DataMap<?>>();
		final DonationQuery query = (DonationQuery) this.connectionService.getQuery(Donation.class);
		List<Donation> donations = query.selectByYearRangePurposeDomainName(this.selectedFromDonationYear, this.selectedToDonationYear, this.selectedDonationPurpose, this.selectedDomain, this.selectedName);
		for (final Donation donation : donations)
		{
			if (this.printDonation(donation))
			{
				if (donation.getLink() == null || !donation.getLink().isValid())
				{
					DataMap<?> map = dataMaps.get("A" + donation.getAddress().getId());
					if (map == null)
					{
						map = new AddressMap(donation.getAddress(), donation.getYear(), donation.getPurpose(), donation.getDomain(), false);
						dataMaps.put("A" + donation.getAddress().getId(), map);
					}
//					else
//					{
//						map.getTableMaps(TableKey.DONATIONS.getKey()).add(new DonationMap(donation));
//					}
				}
				else
				{
					DataMap<?> map = dataMaps.get("P" + donation.getLink().getId());
					if (map == null)
					{
						map = new LinkMap(donation.getLink(), this.selectedFromDonationYear, this.selectedToDonationYear, this.selectedDonationPurpose, this.selectedDomain, false);
						dataMaps.put("P" + donation.getLink().getId(), map);
					}
//					else
//					{
//						map.getTableMaps(TableKey.DONATIONS.getKey()).add(new DonationMap(donation));
//					}
				}
			}
		}
		for (final DataMap<?> dataMap : dataMaps.values())
		{
			double amount = 0d;
			final List<DataMap<?>> tableMaps = dataMap.getTableMaps(TableKey.DONATIONS.getKey());
			for (final DataMap<?> tableMap : tableMaps)
			{
				final DonationMap donationMap = (DonationMap) tableMap;
				amount = amount + donationMap.getAmount();
			}
			dataMap.setProperty(LinkMap.Key.TOTAL_DONATIONS.getKey(), AbstractDataMap.getAmountFormatter().format(amount));

		}
		if (this.excludeYear.getYear() > 0)
		{
			donations = query.selectByYear(this.excludeYear);
			for (final Donation donation : donations)
			{
				if (this.printDonation(donation))
				{
					if (donation.getLink() == null || !donation.getLink().isValid())
					{
						dataMaps.remove("A" + donation.getAddress().getId());
					}
					else
					{
						dataMaps.remove("P" + donation.getLink().getId());
					}
				}
			}
		}
		return dataMaps.values().toArray(new DataMap<?>[0]);
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
		this.setUserPath();
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

	private void setUserPath()
	{
		final ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		try
		{
			tracker.open();
			final Object service = tracker.getService();
			if (service instanceof ConnectionService)
			{
				this.userPropertyTemplatePath.setUser(User.getCurrent());
				final ConnectionService connectionService = (ConnectionService) service;
				User.getCurrent().setProperty(this.userPropertyTemplatePath);
				final UserQuery query = (UserQuery) connectionService.getQuery(User.class);
				User.setCurrent(query.merge(User.getCurrent()));
			}
		}
		finally
		{
			tracker.close();
		}
	}

}
