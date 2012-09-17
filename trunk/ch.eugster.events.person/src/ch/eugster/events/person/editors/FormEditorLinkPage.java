package ch.eugster.events.person.editors;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.MaskFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.persistence.queries.AddressSalutationQuery;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.queries.ZipCodeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.dialogs.ChangeAddressTypeDialog;
import ch.eugster.events.person.preferences.PreferenceInitializer;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.helpers.BrowseHelper;
import ch.eugster.events.ui.helpers.EmailHelper;

public class FormEditorLinkPage extends FormPage implements IContentProposalListener, IPropertyChangeListener
{
	private static final String ID = FormEditorLinkPage.class.getName();

	private static final String EMAIL_LABEL = "Email (senden)";

	private static final String EMAIL_LINK = "Email (<a>senden</a>)";

	private static final String WEBSITE_LABEL = "Webseite (�ffnen)";

	private static final String WEBSITE_LINK = "Webseite (<a>�ffnen</a>)";

	private static final String LINK_SECTION_EXPANDED = "link.section.expanded";

	private static final String ADDRESS_CONTACTS_SECTION_EXPANDED = "address.contacts.section.expanded";

	private static final String ADDRESS_SECTION_EXPANDED = "address.section.expanded";

	private static final String ADDRESS_SALUTATION_SECTION_EXPANDED = "address.salutation.section.expanded";

	private boolean dirty;

	private final FormEditorPersonPage personPage;

	private IDialogSettings dialogSettings;

	private EntityAdapter entityAdapter;

	private final LinkPersonAddress link;

	private Address selectedAddress;

	private Text linkFunction;

	private Text email;

	private Link sendEmail;

	private Text phonePrefix;

	private FormattedText phone;

	private Text faxPrefix;

	private FormattedText fax;

	private Text website;

	private Link browseWebsite;

	private Text linkPhonePrefix;

	private FormattedText linkPhone;

	private Link linkSendEmail;

	private Text linkEmail;

	private Text name;

	private Text anotherLine;

	private Text address;

	private Text pob;

	private ComboViewer countryViewer;

	private Text zip;

	private Text city;

	private ComboViewer provinceViewer;

	private ComboViewer salutationViewer;

	private Label polite;

	private ImageHyperlink deleteHyperlink;

	private ImageHyperlink changeAddressTypeHyperlink;

	private Label singleLabel;

	private Label groupLabel;

	public FormEditorLinkPage(final FormEditor editor, final FormEditorPersonPage personPage, final String id,
			final LinkPersonAddress link)
	{
		this(editor, personPage, id, link, link.getAddressType());
	}

	public FormEditorLinkPage(final FormEditor editor, final FormEditorPersonPage personPage, final String id,
			final LinkPersonAddress link, final AddressType addressType)
	{
		super(editor, id, addressType.getName());
		this.personPage = personPage;
		this.link = link;
		this.setTitleImage(link.getAddressType().getImage());
	}

	private void createAddressContactsSectionPart(final IManagedForm managedForm, final String title,
			final String description, final int numColumns)
	{
		Section section = createSection(managedForm, title, description, Section.TWISTIE | Section.TITLE_BAR
				| Section.DESCRIPTION | Section.EXPANDED, ADDRESS_CONTACTS_SECTION_EXPANDED);
		FormToolkit toolkit = managedForm.getToolkit();
		Composite client = createClientComposite(toolkit, section, numColumns);

		Label label = toolkit.createLabel(client, "Telefon", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData();
		gridData.widthHint = 48;

		phonePrefix = toolkit.createText(client, "");
		phonePrefix.setLayoutData(gridData);
		phonePrefix.setEnabled(false);

		Text phoneControl = toolkit.createText(client, "");
		phoneControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		phoneControl.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
			}
		});
		phoneControl.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.phone = new FormattedText(phoneControl);
		if (countryViewer.getSelection() != null)
		{
			StructuredSelection ssel = (StructuredSelection) countryViewer.getSelection();
			if (ssel.getFirstElement() instanceof Country)
			{
				Country country = (Country) ssel.getFirstElement();
				if (country.getPhonePattern().isEmpty())
				{
					phone.setFormatter(null);
				}
				else
				{
					phone.setFormatter(new MaskFormatter(country.getPhonePattern()));
				}
				phone.setValue(this.link.getAddress().getPhone());
			}
		}

		label = toolkit.createLabel(client, "Fax", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 48;

		faxPrefix = toolkit.createText(client, "");
		faxPrefix.setLayoutData(gridData);
		faxPrefix.setEnabled(false);

		phoneControl = toolkit.createText(client, "");
		phoneControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		phoneControl.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
			}
		});
		phoneControl.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.fax = new FormattedText(phoneControl);
		if (countryViewer.getSelection() != null)
		{
			StructuredSelection ssel = (StructuredSelection) countryViewer.getSelection();
			if (ssel.getFirstElement() instanceof Country)
			{
				Country country = (Country) ssel.getFirstElement();
				this.fax.setFormatter(new MaskFormatter(country.getPhonePattern()));
			}
		}

		sendEmail = new Link(client, SWT.NONE);
		sendEmail.setText(FormEditorLinkPage.EMAIL_LABEL);
		sendEmail.setToolTipText("Um ein Email zu senden, klicken Sie hier");
		sendEmail.setLayoutData(new GridData());
		sendEmail.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				EmailHelper.getInstance().sendEmail(FormEditorLinkPage.this.email.getText());
			}
		});

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.email = toolkit.createText(client, "");
		this.email.setLayoutData(gridData);
		this.email.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
				if (email.getText().length() >= 3
						&& email.getText().substring(1, email.getText().length() - 2).contains("@"))
				{
					sendEmail.setText(FormEditorLinkPage.EMAIL_LINK);
				}
				else
				{
					sendEmail.setText(FormEditorLinkPage.EMAIL_LABEL);
				}
			}
		});
		this.email.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		browseWebsite = new Link(client, SWT.NONE);
		browseWebsite.setText(FormEditorLinkPage.WEBSITE_LABEL);
		browseWebsite.setToolTipText("Klicken Sie hier, um die Webseite zu besuchen");
		browseWebsite.setLayoutData(new GridData());
		browseWebsite.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				BrowseHelper.getInstance().browse(FormEditorLinkPage.this.website.getText());
			}
		});

		gridData = new GridData();
		gridData.horizontalSpan = 2;

		this.website = toolkit.createText(client, "");
		this.website.setLayoutData(gridData);
		this.website.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
				if (website.getText().length() > 11 && website.getText().startsWith("http://"))
				{
					browseWebsite.setText(FormEditorLinkPage.WEBSITE_LINK);
				}
				else
				{
					browseWebsite.setText(FormEditorLinkPage.WEBSITE_LABEL);
				}
			}
		});
		this.website.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		toolkit.paintBordersFor(client);
	}

	private void createAddressLabelExampleSectionPart(final IManagedForm managedForm, final String title,
			final String description, final int numColumns)
	{
		Section section = createSection(managedForm, title, description, Section.TWISTIE | Section.TITLE_BAR
				| Section.DESCRIPTION | Section.EXPANDED, ADDRESS_SALUTATION_SECTION_EXPANDED);
		FormToolkit toolkit = managedForm.getToolkit();
		Composite client = createClientComposite(toolkit, section, numColumns);

		Label label = toolkit.createLabel(client, "Einzeladresse", SWT.NONE);
		label.setLayoutData(new GridData());

		label = toolkit.createLabel(client, "Sammeladresse", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 64;

		singleLabel = toolkit.createLabel(client, "", SWT.BORDER);
		singleLabel.setLayoutData(gridData);

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 64;

		groupLabel = toolkit.createLabel(client, "", SWT.BORDER);
		groupLabel.setLayoutData(gridData);
		groupLabel.setEnabled(link.getAddress().getPersonLinks().size() > 1);

		toolkit.paintBordersFor(client);
	}

	private void createAddressSectionPart(final IManagedForm managedForm, final String title, final String description,
			final int numColumns)
	{
		Section section = createSection(managedForm, title, description, Section.TWISTIE | Section.TITLE_BAR
				| Section.DESCRIPTION | Section.EXPANDED, ADDRESS_SECTION_EXPANDED);
		FormToolkit toolkit = managedForm.getToolkit();
		Composite client = createClientComposite(toolkit, section, numColumns);

		Label label = toolkit.createLabel(client, "Anrede", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		CCombo combo = new CCombo(client, SWT.READ_ONLY | SWT.FLAT);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(gridData);
		toolkit.adapt(combo);

		this.salutationViewer = new ComboViewer(combo);
		this.salutationViewer.setContentProvider(new ArrayContentProvider());
		this.salutationViewer.setLabelProvider(new AddressSalutationLabelProvider());
		this.salutationViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.salutationViewer.setInput(selectSalutations());
		this.salutationViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				AddressSalutation salutation = (AddressSalutation) ssel.getFirstElement();
				polite.setText(salutation == null ? "" : salutation.getPolite());
				updateSingleLabel();
				updateGroupLabel();
				setDirty(true);
			}
		});

		label = toolkit.createLabel(client, "Organisation", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		this.name = toolkit.createText(client, "");
		this.name.setLayoutData(gridData);
		this.name.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateSingleLabel();
				updateGroupLabel();
				setDirty(true);
			}
		});
		this.name.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		label = toolkit.createLabel(client, "Zusatz", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		this.anotherLine = toolkit.createText(client, "");
		this.anotherLine.setLayoutData(gridData);
		this.anotherLine.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateSingleLabel();
				updateGroupLabel();
				setDirty(true);
			}
		});
		this.anotherLine.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		label = toolkit.createLabel(client, "Strasse", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		this.address = toolkit.createText(client, "");
		this.address.setLayoutData(gridData);
		this.address.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}

			@Override
			public void focusLost(final FocusEvent e)
			{
				Text address = (Text) e.getSource();
				String value = address.getText();
				if (value.toLowerCase().trim().endsWith("str."))
				{
					value = value.substring(0, value.length() - "str.".length());
					if (value.endsWith(" "))
						value = value.concat("Strasse");
					else
						value = value.concat("strasse");
				}
				else if (value.toLowerCase().indexOf("strasse ") < value.length() - "strasse ".length())
				{
					value = value.replace("trasse ", "tr. ");
				}
				if (!address.getText().equals(value))
				{
					setDirty(true);
					address.setText(value);
				}
			}

		});
		this.address.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateSingleLabel();
				updateGroupLabel();
				setDirty(true);
			}
		});

		ContentProposalAdapter proposalAdapter = new ContentProposalAdapter(this.address, new TextContentAdapter(),
				new AddressContentProposalProvider(this), null, null);
		proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		proposalAdapter.addContentProposalListener(this);

		label = toolkit.createLabel(client, "Postfach", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		this.pob = toolkit.createText(client, "");
		this.pob.setLayoutData(gridData);
		this.pob.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateSingleLabel();
				updateGroupLabel();
				setDirty(true);
			}
		});
		this.pob.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		label = toolkit.createLabel(client, "Land PLZ Ort Kanton", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 36;

		combo = new CCombo(client, SWT.READ_ONLY | SWT.FLAT);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(gridData);
		toolkit.adapt(combo);

		this.countryViewer = new ComboViewer(combo);
		this.countryViewer.setContentProvider(new ArrayContentProvider());
		this.countryViewer.setLabelProvider(new CountryIso3166Alpha2LabelProvider());
		this.countryViewer.setSorter(new CountryIso3166Alpha2Sorter());
		this.countryViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.countryViewer.setInput(selectCountries());
		this.countryViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				setDirty(true);
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				if (ssel.getFirstElement() instanceof Country)
				{
					Country country = (Country) ssel.getFirstElement();
					countryViewer.setData("country", country);
					linkPhonePrefix.setText(country.getPhonePrefix());
					phonePrefix.setText(country.getPhonePrefix());
					faxPrefix.setText(country.getPhonePrefix());
					if (country.getPhonePattern().isEmpty())
					{
						linkPhone.setFormatter(null);
						phone.setFormatter(null);
						fax.setFormatter(null);
					}
					else
					{
						linkPhone.setFormatter(new MaskFormatter(country.getPhonePattern()));
						phone.setFormatter(new MaskFormatter(country.getPhonePattern()));
						fax.setFormatter(new MaskFormatter(country.getPhonePattern()));
					}
					String p = FormEditorLinkPage.this.link.getAddress().getPhone();
					p = p.startsWith(phonePrefix.getText()) ? p.substring(phonePrefix.getText().length()) : p;
					FormEditorLinkPage.this.phone.setValue(p);
					p = FormEditorLinkPage.this.link.getAddress().getFax();
					p = p.startsWith(faxPrefix.getText()) ? p.substring(faxPrefix.getText().length()) : p;
					FormEditorLinkPage.this.fax.setValue(p);
					p = FormEditorLinkPage.this.link.getPhone();
					p = p.startsWith(linkPhonePrefix.getText()) ? p.substring(linkPhonePrefix.getText().length()) : p;
					FormEditorLinkPage.this.linkPhone.setValue(p);

					String[] states = selectProvinceCodes(country);
					provinceViewer.setInput(states);
					if (zip.getData("zipCode") instanceof ZipCode)
					{
						ZipCode zipCode = (ZipCode) zip.getData("zipCode");
						provinceViewer.setSelection(new StructuredSelection(zipCode.getState()));
					}
				}
				updateSingleLabel();
				updateGroupLabel();
			}
		});

		gridData = new GridData();
		gridData.widthHint = 48;

		this.zip = toolkit.createText(client, "");
		this.zip.setLayoutData(gridData);
		this.zip.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				zipSelected(zip.getText());
				updateSingleLabel();
				updateGroupLabel();
				setDirty(true);
			}
		});
		this.zip.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}

			@Override
			public void focusLost(final FocusEvent e)
			{
				zipSelected(zip.getText());
			}
		});

		this.city = toolkit.createText(client, "");
		this.city.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.city.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateSingleLabel();
				updateGroupLabel();
				setDirty(true);
			}
		});
		this.city.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		proposalAdapter = new ContentProposalAdapter(this.city, new TextContentAdapter(),
				new CityContentProposalProvider(countryViewer, zip), null, null);
		proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		proposalAdapter.addContentProposalListener(this);

		gridData = new GridData();

		combo = new CCombo(client, SWT.READ_ONLY | SWT.FLAT);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(gridData);
		toolkit.adapt(combo);

		this.provinceViewer = new ComboViewer(combo);
		this.provinceViewer.setContentProvider(new ArrayContentProvider());
		this.provinceViewer.setLabelProvider(new ProvinceLabelProvider());
		this.provinceViewer.setSorter(new ProvinceSorter());
		this.provinceViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				setDirty(true);
			}
		});

		label = toolkit.createLabel(client, "Briefanrede", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		polite = toolkit.createLabel(client, "", SWT.BORDER);
		polite.setLayoutData(gridData);

		toolkit.paintBordersFor(client);
	}

	private void createButtons(final IManagedForm managedForm)
	{
		ScrolledForm scrolledForm = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		final Composite composite = toolkit.createComposite(scrolledForm.getBody());
		composite.setLayoutData(new ColumnLayoutData(ColumnLayoutData.FILL));
		composite.setLayout(new GridLayout(4, false));

		Label label = toolkit.createLabel(composite, "");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ImageHyperlink hyperlink = toolkit.createImageHyperlink(composite, SWT.NONE);
		hyperlink.setImage(Activator.getDefault().getImageRegistry().get(Activator.KEY_PERSON_BLUE));
		hyperlink.addHyperlinkListener(new HyperlinkAdapter()
		{
			@Override
			public void linkActivated(final HyperlinkEvent e)
			{
				IFormPage page = getEditor().findPage("person.page");
				if (page != null)
				{
					getEditor().setActivePage(page.getId());
				}
			}
		});

		changeAddressTypeHyperlink = toolkit.createImageHyperlink(composite, SWT.NONE);
		changeAddressTypeHyperlink.addHyperlinkListener(new HyperlinkAdapter()
		{
			@Override
			public void linkActivated(final HyperlinkEvent e)
			{
				ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
						ConnectionService.class.getName(), null);
				tracker.open();
				try
				{
					ConnectionService service = (ConnectionService) tracker.getService();
					if (service != null)
					{
						Collection<AddressType> unusedAddressTypes = new ArrayList<AddressType>();
						AddressTypeQuery query = (AddressTypeQuery) service.getQuery(AddressType.class);
						Collection<AddressType> allAddressTypes = query.selectAll();

						Collection<IFormPage> usedPages = getEditor().getPages();
						for (AddressType addressType : allAddressTypes)
						{
							boolean found = false;
							for (IFormPage usedPage : usedPages)
							{
								if (usedPage instanceof FormEditorLinkPage)
								{

									FormEditorLinkPage page = (FormEditorLinkPage) usedPage;
									if (page.getLink().getAddressType().getId().equals(addressType))
									{
										found = true;
									}
								}
							}
							if (!found)
							{
								unusedAddressTypes.add(addressType);
							}
						}
						if (unusedAddressTypes.size() > 0)
						{
							ChangeAddressTypeDialog dialog = new ChangeAddressTypeDialog(composite.getShell(),
									FormEditorLinkPage.this, unusedAddressTypes.toArray(new AddressType[0]));
							dialog.open();
						}
					}
				}
				finally
				{
					tracker.close();
				}
			}
		});
		changeAddressTypeHyperlink.setEnabled(true);
		Image image = Activator.getDefault().getImageRegistry().get(Activator.KEY_CHANGE_ADDRESS);
		changeAddressTypeHyperlink.setImage(image);

		deleteHyperlink = toolkit.createImageHyperlink(composite, SWT.NONE);
		deleteHyperlink.addHyperlinkListener(new HyperlinkAdapter()
		{
			@Override
			public void linkActivated(final HyperlinkEvent e)
			{
				getEditor().removePage(getEditor().getActivePage());
			}
		});
		/**
		 * set enabled only and only if link has an id and there are at least
		 * one other active address page!
		 */
		boolean enabled = link.getPerson().getDefaultLink() != link;
		deleteHyperlink.setEnabled(enabled);
		String key = enabled ? Activator.KEY_DELETE : Activator.KEY_DELETE_INACTIVE;
		image = Activator.getDefault().getImageRegistry().get(key);
		deleteHyperlink.setImage(image);
	}

	private Composite createClientComposite(final FormToolkit toolkit, final Section section, final int numColumns)
	{
		Composite client = toolkit.createComposite(section);
		client.setLayout(new GridLayout(numColumns, false));
		section.setClient(client);
		return client;
	}

	@Override
	protected void createFormContent(final IManagedForm managedForm)
	{
		ScrolledForm scrolledForm = managedForm.getForm();
		scrolledForm.setText(getText());
		scrolledForm.setBackgroundImage(Activator.getDefault().getImageRegistry().get(Activator.KEY_FORM_EDITOR_BG));

		ColumnLayout layout = new ColumnLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 5;
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 10;
		layout.maxNumColumns = User.getCurrent().getMaxEditorColumns();
		layout.minNumColumns = User.getCurrent().getMinEditorColumns();
		scrolledForm.getBody().setLayout(layout);

		createButtons(managedForm);

		createAddressSectionPart(
				managedForm,
				"Anschrift",
				"Geben Sie hier die gew�nschten Adressdaten ein. Im Feld Strasse k�nnen Sie CTRL+Space verwenden, um aus bestehenden Adressen auszuw�hlen.",
				5);

		createAddressContactsSectionPart(managedForm, "Kontakte", "Adressenbezogene Kontaktm�glichkeiten", 3);

		createLinkSectionPart(managedForm, "Zusatzinformationen",
				"Hier k�nnen zus�tzliche Informationen zu " + this.getText()
						+ " erfasst werden, die sich auf diese Adresse beziehen.", 3);

		createAddressLabelExampleSectionPart(managedForm, "Vorschau Adressetikette",
				"Vorschau der Adressetiketten f�r Einzel- und, falls gegeben, Sammeladresse.", 2);

		selectedAddress = link.getAddress();
		loadValues();
	}

	private void createLinkSectionPart(final IManagedForm managedForm, final String title, final String description,
			final int numColumns)
	{
		Section section = createSection(managedForm, title, description, Section.TWISTIE | Section.TITLE_BAR
				| Section.DESCRIPTION | Section.EXPANDED, LINK_SECTION_EXPANDED);
		FormToolkit toolkit = managedForm.getToolkit();
		Composite client = createClientComposite(toolkit, section, numColumns);

		Label label = toolkit.createLabel(client, "Funktion", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.linkFunction = toolkit.createText(client, "");
		this.linkFunction.setLayoutData(gridData);
		this.linkFunction.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
			}
		});
		this.linkFunction.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		label = toolkit.createLabel(client, "Telefon", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 48;

		linkPhonePrefix = toolkit.createText(client, "");
		linkPhonePrefix.setLayoutData(gridData);
		linkPhonePrefix.setEnabled(false);

		Text phoneControl = toolkit.createText(client, "");
		phoneControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		phoneControl.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
			}
		});
		phoneControl.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.linkPhone = new FormattedText(phoneControl);
		if (countryViewer.getSelection() != null)
		{
			StructuredSelection ssel = (StructuredSelection) countryViewer.getSelection();
			if (ssel.getFirstElement() instanceof Country)
			{
				Country country = (Country) ssel.getFirstElement();
				this.linkPhone.setFormatter(new MaskFormatter(country.getPhonePattern()));
			}
		}

		linkSendEmail = new Link(client, SWT.NONE);
		linkSendEmail.setText(FormEditorLinkPage.EMAIL_LABEL);
		linkSendEmail.setToolTipText("Um ein Email zu senden, klicken Sie hier");
		linkSendEmail.setLayoutData(new GridData());
		linkSendEmail.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				EmailHelper.getInstance().sendEmail(FormEditorLinkPage.this.linkEmail.getText());
			}
		});

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.linkEmail = toolkit.createText(client, "");
		this.linkEmail.setLayoutData(gridData);
		this.linkEmail.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
				if (linkEmail.getText().length() >= 3
						&& linkEmail.getText().substring(1, linkEmail.getText().length() - 2).contains("@"))
				{
					linkSendEmail.setText(FormEditorLinkPage.EMAIL_LINK);
				}
				else
				{
					linkSendEmail.setText(FormEditorLinkPage.EMAIL_LABEL);
				}
			}
		});
		this.linkEmail.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		toolkit.paintBordersFor(client);
	}

	private Section createSection(final IManagedForm managedForm, final String title, final String description,
			final int style, final String id)
	{
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		Section section = toolkit.createSection(form.getBody(), style);
		section.setText(title);
		section.setDescription(description);
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				dialogSettings.put(id, e.getState());
				form.reflow(true);
			}
		});
		String expanded = this.dialogSettings.get(id);
		section.setExpanded(expanded == null || Boolean.valueOf(expanded).booleanValue());

		return section;
	}

	@Override
	public void dispose()
	{
		this.personPage.removeListener(this);
		EntityMediator.removeListener(Person.class, entityAdapter);
		EntityMediator.removeListener(Domain.class, entityAdapter);
		EntityMediator.removeListener(PersonTitle.class, entityAdapter);
		EntityMediator.removeListener(LinkPersonAddress.class, entityAdapter);
		EntityMediator.removeListener(Address.class, entityAdapter);
	}

	private ZipCode findZipCode(final Country country, final String zip)
	{
		Collection<ZipCode> zipCodes = null;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			ZipCodeQuery query = (ZipCodeQuery) service.getQuery(ZipCode.class);
			zipCodes = query.selectByCountryAndZipCode(country, zip);
		}
		tracker.close();
		if (zipCodes.iterator().hasNext())
		{
			return zipCodes.iterator().next();
		}
		return null;
	}

	public String getAddress()
	{
		return this.address.getText();
	}

	@Override
	public PersonFormEditor getEditor()
	{
		return (PersonFormEditor) super.getEditor();
	}

	private Message getEmptyCityMessage()
	{
		Message msg = null;

		if (this.city.getText().isEmpty())
		{
			msg = new Message(this.city, "Fehler");
			msg.setMessage("Die Person muss einen Nachnamen haben.");
			FormToolkit.ensureVisible(this.city);
			this.city.setFocus();
		}

		return msg;
	}

	private Message getEmptyCountryMessage()
	{
		Message msg = null;

		if (this.countryViewer.getSelection().isEmpty())
		{
			msg = new Message(this.city, "Fehler");
			msg.setMessage("Der L�ndercode fehlt.");
			FormToolkit.ensureVisible(this.countryViewer.getControl());
			this.countryViewer.getControl().setFocus();
		}

		return msg;
	}

	private Message getEmptyZipMessage()
	{
		Message msg = null;

		if (this.zip.getText().isEmpty())
		{
			msg = new Message(this.city, "Fehler");
			msg.setMessage("Sie haben keine Postleitzahl eingegeben.");
			FormToolkit.ensureVisible(this.zip);
			this.zip.setFocus();
		}

		return msg;
	}

	public LinkPersonAddress getLink()
	{
		return link;
	}

	private String getText()
	{
		PersonEditorInput input = (PersonEditorInput) this.getEditor().getEditorInput();
		return input.getName();
	}

	@Override
	public void init(final IEditorSite site, final IEditorInput input)
	{
		super.init(site, input);
		String value = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceInitializer.KEY_EDITOR_SECTION_BEHAVIOUR);

		Long id = ((PersonEditorInput) this.getEditor().getEditorInput()).getEntity().getId();
		if (id == null || value.equals(PreferenceInitializer.EDITOR_SECTION_BEHAVIOUR_EDITOR))
		{
			id = Long.valueOf(0L);
		}
		this.initializeDialogSettings(id == null ? ID : ID + "." + id);

		entityAdapter = new EntityAdapter()
		{

			@Override
			public void postDelete(final AbstractEntity entity)
			{
				if (entity instanceof Person)
				{
					Person person = (Person) entity;
					if (person.getId().equals(link.getPerson().getId()))
					{

					}
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof LinkPersonAddress)
				{
					LinkPersonAddress other = (LinkPersonAddress) entity;
					if (link.getId() != null && link.getPerson().getId() != null)
					{
						if (other.getPerson().getId().equals(link.getPerson().getId()))
						{
							if (deleteHyperlink != null)
							{
								boolean enable = other.getPerson().getDefaultLink().getId().equals(link.getId());
								deleteHyperlink.setEnabled(enable);
							}
						}
					}
				}
				else if (entity instanceof Person)
				{
					Person person = (Person) entity;
					if (person.getId().equals(link.getPerson().getId()))
					{
						if (link.getId() != null)
						{
							if (deleteHyperlink != null)
							{
								boolean enable = person.getDefaultLink() != null
										&& person.getDefaultLink().getId().equals(link.getId());
								deleteHyperlink.setEnabled(!enable);
							}
						}
					}
				}
			}
		};
		EntityMediator.addListener(Person.class, entityAdapter);
		EntityMediator.addListener(Domain.class, entityAdapter);
		EntityMediator.addListener(PersonTitle.class, entityAdapter);
		EntityMediator.addListener(LinkPersonAddress.class, entityAdapter);
		EntityMediator.addListener(Address.class, entityAdapter);

		this.personPage.addListener(this);
	}

	private void initializeDialogSettings(final String section)
	{
		this.dialogSettings = Activator.getDefault().getDialogSettings().getSection(section);
		if (this.dialogSettings == null)
			this.dialogSettings = Activator.getDefault().getDialogSettings().addNewSection(section);
	}

	@Override
	public boolean isDirty()
	{
		return dirty;
	}

	private void loadAddressContactsValues()
	{
		phone.setFormatter(link.getAddress().getCountry() == null ? null : new MaskFormatter(link.getAddress()
				.getCountry().getPhonePattern()));
		phone.setValue(selectedAddress.getPhone());
		fax.setFormatter(link.getAddress().getCountry() == null ? null : new MaskFormatter(link.getAddress()
				.getCountry().getPhonePattern()));
		fax.setValue(selectedAddress.getFax());
		this.website.setText(selectedAddress.getWebsite());
	}

	private void loadAddressValues()
	{
		if (selectedAddress.getSalutation() == null)
		{
			this.salutationViewer.setSelection(new StructuredSelection(new AddressSalutation[] { selectedAddress
					.getSalutation() }));
		}
		this.name.setText(selectedAddress.getName());
		this.anotherLine.setText(selectedAddress.getAnotherLine());
		this.address.setText(selectedAddress.getAddress());
		this.pob.setText(selectedAddress.getPob());
		if (selectedAddress.getCountry() == null)
		{
			selectedAddress.setCountry(AddressFormatter.getInstance().getCountry());
		}
		this.countryViewer.setSelection(new StructuredSelection(selectedAddress.getCountry()));
		this.zip.setText(selectedAddress.getZip());
		this.city.setText(selectedAddress.getCity());
	}

	private void loadLinkValues()
	{
		this.linkFunction.setText(link.getFunction());
		this.linkPhone.setFormatter(link.getAddress().getCountry() == null ? null : new MaskFormatter(link.getAddress()
				.getCountry().getPhonePattern()));
		this.linkPhone.setValue(link.getPhone());
		this.linkEmail.setText(link.getEmail());

	}

	public void loadValues()
	{
		loadAddressValues();
		loadAddressContactsValues();
		loadLinkValues();
		setDirty(false);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event)
	{
		if (this.name != null && !this.name.isDisposed())
			this.updateSingleLabel();
	}

	@Override
	public void proposalAccepted(final IContentProposal contentProposal)
	{
		if (contentProposal instanceof AddressContentProposal)
		{
			AddressContentProposal proposal = (AddressContentProposal) contentProposal;
			selectedAddress = proposal.getPersonAddressLink().getAddress();
			loadAddressValues();
			loadAddressContactsValues();
		}
		else if (contentProposal instanceof CityContentProposal)
		{
			CityContentProposal proposal = (CityContentProposal) contentProposal;
			city.setText(proposal.getZipCode().getCity());
		}
	}

	private void saveAddressContactsValues()
	{
		selectedAddress.setPhone((String) this.phone.getValue());
		selectedAddress.setFax((String) this.fax.getValue());
		selectedAddress.setWebsite(this.website.getText());
	}

	private void saveAddressValues()
	{
		StructuredSelection ssel = (StructuredSelection) this.salutationViewer.getSelection();
		if (ssel.isEmpty())
		{
			selectedAddress.setSalutation(null);
		}
		else
		{
			selectedAddress.setSalutation((AddressSalutation) ssel.getFirstElement());
		}
		selectedAddress.setName(name.getText());
		selectedAddress.setAnotherLine(anotherLine.getText());
		selectedAddress.setAddress(this.address.getText());
		selectedAddress.setPob(this.pob.getText());
		ssel = (StructuredSelection) countryViewer.getSelection();
		if (ssel.getFirstElement() instanceof Country)
		{
			selectedAddress.setCountry((Country) ssel.getFirstElement());
		}
		else
		{
			selectedAddress.setCountry(null);
		}
		selectedAddress.setZip(this.zip.getText());
		selectedAddress.setCity(this.city.getText());
	}

	private void saveLinkValues()
	{
		link.setFunction(linkFunction.getText());
		link.setPhone((String) this.linkPhone.getValue());
		link.setEmail(this.linkEmail.getText());
		link.setAddress(selectedAddress);
	}

	public void saveValues()
	{
		saveAddressValues();
		saveAddressContactsValues();
		saveLinkValues();
		setDirty(false);
	}

	private Country[] selectCountries()
	{
		Collection<Country> countries = null;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			CountryQuery query = (CountryQuery) service.getQuery(Country.class);
			countries = query.selectVisibles();
		}
		tracker.close();
		return countries == null ? new Country[0] : countries.toArray(new Country[0]);
	}

	private String[] selectProvinceCodes(final Country country)
	{
		Collection<String> states = null;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			ZipCodeQuery query = (ZipCodeQuery) service.getQuery(ZipCode.class);
			states = query.selectStates(country);
		}
		tracker.close();
		return states == null ? new String[0] : states.toArray(new String[0]);
	}

	@Override
	public boolean selectReveal(final Object object)
	{
		return super.selectReveal(object);
	}

	private AddressSalutation[] selectSalutations()
	{
		Collection<AddressSalutation> salutations = new ArrayList<AddressSalutation>();
		salutations.add(AddressSalutation.newInstance());

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			AddressSalutationQuery query = (AddressSalutationQuery) service.getQuery(AddressSalutation.class);
			salutations.addAll(query.selectAll());
		}
		tracker.close();
		return salutations.toArray(new AddressSalutation[0]);
	}

	public void setDirty(final boolean dirty)
	{
		if (this.dirty == dirty)
			return;

		this.dirty = dirty;
		this.firePropertyChange(PROP_DIRTY);
	}

	public void setFocus(final Control control)
	{
		control.setFocus();
	}

	protected int showMessage(final String title, final Image image, final String message, final int dialogType,
			final String[] buttonLabels, final int defaultButton)
	{
		MessageDialog dialog = new MessageDialog(this.getEditorSite().getShell(), title, image, message, dialogType,
				buttonLabels, defaultButton);
		return dialog.open();
	}

	protected int showWarningMessage(final Message msg)
	{
		int result = this.showMessage(msg.getTitle(), Dialog.getImage(Dialog.DLG_IMG_MESSAGE_WARNING),
				msg.getMessage(), MessageDialog.WARNING, new String[] { "OK" }, 0);
		this.setFocus(msg.getControl());
		return result;
	}

	private void updateGroupLabel()
	{
		Collection<String> labelLines = new ArrayList<String>();
		String pattern = PersonSettings.getInstance().getAddressLabelFormat();
		if (pattern.isEmpty())
		{
			pattern = AddressFormatter.getInstance().createVisibleAddressLabel();
			pattern = AddressFormatter.getInstance().convertAddressLabelToStored(pattern);
		}
		String[] lines = pattern.split("[|]");
		for (String line : lines)
		{
			if (line.contains("${"))
			{
				String[] variables = AddressFormatter.getInstance().getAddressLabelStoredVariables();
				for (String variable : variables)
				{
					if (line.contains("${"))
					{
						if (variable.equals("${salutation}"))
						{
							StructuredSelection ssel = (StructuredSelection) this.salutationViewer.getSelection();
							String salutation = "";
							if (ssel.getFirstElement() instanceof AddressSalutation)
							{
								AddressSalutation as = (AddressSalutation) ssel.getFirstElement();
								salutation = as.getSalutation();
							}
							line = line.replace(variable, salutation);
						}
						else if (variable.equals("${organisation}"))
						{
							line = line.replace(variable, this.name.getText());
						}
						else if (variable.equals("${anotherline}"))
						{
							line = line.replace(variable, this.anotherLine.getText());
						}
						else if (variable.equals("${address}"))
						{
							line = line.replace(variable, this.address.getText());
						}
						else if (variable.equals("${pob}"))
						{
							line = line.replace(variable, this.pob.getText());
						}
						else if (variable.equals("${country}"))
						{
							String c = null;
							StructuredSelection ssel = (StructuredSelection) this.countryViewer.getSelection();
							if (ssel.getFirstElement() instanceof Country)
							{
								Country country = (Country) ssel.getFirstElement();
								c = country.getIso3166alpha2();
							}
							else
							{
								c = "";
							}
							line = line.replace(variable, c);
							if (c.equals(""))
							{
								line = line.replace("-", "");
							}
						}
						else if (variable.equals("${zip}"))
						{
							line = line.replace(variable, this.zip.getText());
						}
						else if (variable.equals("${city}"))
						{
							line = line.replace(variable, this.city.getText());
						}
					}
				}
			}
			if (!line.trim().isEmpty())
			{
				labelLines.add(line.trim());
			}
		}
		StringBuilder label = new StringBuilder();
		for (String labelLine : labelLines)
		{
			if (label.length() > 0)
			{
				label = label.append("\n");
			}
			label = label.append(labelLine);
		}
		this.groupLabel.setText(label.toString());
	}

	private void updateSingleLabel()
	{
		Collection<String> labelLines = new ArrayList<String>();
		String pattern = PersonSettings.getInstance().getPersonLabelFormat();
		if (pattern.isEmpty())
		{
			pattern = PersonFormatter.getInstance().createVisiblePersonLabel();
			pattern = PersonFormatter.getInstance().convertPersonLabelToStored(pattern);
		}
		String[] lines = pattern.split("[|]");
		for (String line : lines)
		{
			if (line.contains("${"))
			{
				String[] variables = PersonFormatter.getInstance().getPersonLabelStoredVariables();
				for (String variable : variables)
				{
					if (line.contains("${"))
					{
						if (variable.equals("${salutation}"))
						{
							line = line.replace(variable, personPage.getPersonSex() == null ? "" : personPage
									.getPersonSex().getSalutation());
						}
						else if (variable.equals("${title}"))
						{
							line = line.replace(variable, personPage.getPersonTitle() == null ? "" : personPage
									.getPersonTitle().getTitle());
						}
						else if (variable.equals("${organisation}"))
						{
							line = line.replace(variable, this.name.getText());
						}
						else if (variable.equals("${firstname}"))
						{
							line = line.replace(variable,
									personPage.getFirstname() == null ? "" : personPage.getFirstname());
						}
						else if (variable.equals("${lastname}"))
						{
							line = line.replace(variable,
									personPage.getLastname() == null ? "" : personPage.getLastname());
						}
						else if (variable.equals("${anotherline}"))
						{
							line = line.replace(variable, this.anotherLine.getText());
						}
						else if (variable.equals("${address}"))
						{
							line = line.replace(variable, this.address.getText());
						}
						else if (variable.equals("${pob}"))
						{
							line = line.replace(variable, this.pob.getText());
						}
						else if (variable.equals("${country}"))
						{
							String c = null;
							StructuredSelection ssel = (StructuredSelection) this.countryViewer.getSelection();
							if (ssel.getFirstElement() instanceof Country)
							{
								Country country = (Country) ssel.getFirstElement();
								c = country.getIso3166alpha2();
							}
							else
							{
								c = "";
							}
							line = line.replace(variable, c);
							if (c.equals(""))
							{
								line = line.replace("-", "");
							}
						}
						else if (variable.equals("${zip}"))
						{
							line = line.replace(variable, this.zip.getText());
						}
						else if (variable.equals("${city}"))
						{
							line = line.replace(variable, this.city.getText());
						}
					}
				}
			}
			if (!line.trim().isEmpty())
			{
				labelLines.add(line.trim());
			}
		}
		StringBuilder label = new StringBuilder();
		for (String labelLine : labelLines)
		{
			if (label.length() > 0)
			{
				label = label.append("\n");
			}
			label = label.append(labelLine);
		}
		this.singleLabel.setText(label.toString());
	}

	public boolean validate()
	{
		Message msg = null;

		// if (msg == null)
		// {
		// msg = this.getEmptyAddressAndPobMessage();
		// }

		if (msg == null)
		{
			msg = this.getEmptyCountryMessage();
		}

		if (msg == null)
		{
			msg = this.getEmptyZipMessage();
		}

		if (msg == null)
		{
			msg = this.getEmptyCityMessage();
		}

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	private void zipSelected(final String code)
	{
		if (countryViewer.getData("country") instanceof Country && !code.isEmpty())
		{
			Country country = (Country) countryViewer.getData("country");
			ZipCode zipCode = findZipCode(country, code);
			zip.setData("zipCode", zipCode);
			if (zipCode != null)
			{
				city.setText(zipCode.getCity());
				provinceViewer.setSelection(new StructuredSelection(zipCode.getState()));
			}
		}
	}
}
