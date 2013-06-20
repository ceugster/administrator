package ch.eugster.events.person.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.ui.PartInitException;
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
import org.eclipse.ui.forms.widgets.Hyperlink;
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
import ch.eugster.events.ui.editors.Dirtyable;
import ch.eugster.events.ui.editors.Saveable;
import ch.eugster.events.ui.editors.Validateable;
import ch.eugster.events.ui.helpers.BrowseHelper;
import ch.eugster.events.ui.helpers.EmailHelper;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class FormEditorLinkPage extends FormPage implements IContentProposalListener, IPropertyChangeListener,
		Validateable, Saveable, Dirtyable
{
	private static final String ID = FormEditorLinkPage.class.getName();

	private boolean dirty;

	private static final String EMAIL_LABEL = "Email (senden)";

	private static final String EMAIL_LINK = "Email (<a>senden</a>)";

	private static final String WEBSITE_LABEL = "Webseite (öffnen)";

	private static final String WEBSITE_LINK = "Webseite (<a>öffnen</a>)";

	private static final String LINK_SECTION_EXPANDED = "link.section.expanded";

	private static final String ADDRESS_CONTACTS_SECTION_EXPANDED = "address.contacts.section.expanded";

	private static final String ADDRESS_SECTION_EXPANDED = "address.section.expanded";

	private static final String ADDRESS_SALUTATION_SECTION_EXPANDED = "address.salutation.section.expanded";

	private final FormEditorPersonPage personPage;

	private IDialogSettings dialogSettings;

	private EntityAdapter entityAdapter;

	private Text linkFunction;

	private Text email;

	private Link sendEmail;

	private Text phonePrefix;

	private Text phone;

	private Text faxPrefix;

	private Text fax;

	private Text website;

	private Link browseWebsite;

	private Text linkPhonePrefix;

	private Text linkPhone;

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

	private AddressType addressType;

	private PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

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
		this.addressType = addressType;
		this.setTitleImage(link.getAddressType().getImage());
	}

	private void addAddressPage(final AddressType addressType, final String id)
	{
		try
		{
			Person person = ((PersonEditorInput) getEditor().getEditorInput()).getEntity();
			Address address = Address.newInstance();
			LinkPersonAddress link = LinkPersonAddress.newInstance(person, address);
			link.setAddressType(addressType);
			FormEditorLinkPage page = new FormEditorLinkPage(getEditor(), personPage, id, link);
			getEditor().addPage(page);
			getEditor().setActivePage(page.getId());
		}
		catch (PartInitException pie)
		{
		}
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

		phone = toolkit.createText(client, "");
		phone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		phone.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty();
			}
		});
		phone.addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				phone.setText(removeSpaces(phone.getText()));
			}

			@Override
			public void focusLost(final FocusEvent e)
			{
				phone.setText(formatPhoneNumber(phone.getText()));
			}
		});

		label = toolkit.createLabel(client, "Fax", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 48;

		faxPrefix = toolkit.createText(client, "");
		faxPrefix.setLayoutData(gridData);
		faxPrefix.setEnabled(false);

		this.fax = toolkit.createText(client, "");
		fax.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fax.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty();
			}
		});
		fax.addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				fax.setText(removeSpaces(fax.getText()));
			}

			@Override
			public void focusLost(final FocusEvent e)
			{
				fax.setText(formatPhoneNumber(fax.getText()));
			}
		});

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
				setDirty();
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
				setDirty();
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
		groupLabel.setEnabled(getLink() == null ? false : getLink().getAddress().getPersonLinks().size() > 1);

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
				setDirty();
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
				setDirty();
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
				setDirty();
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
					setDirty();
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
				setDirty();
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
				setDirty();
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
				setDirty();
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				if (ssel.getFirstElement() instanceof Country)
				{
					Country country = (Country) ssel.getFirstElement();
					countryViewer.setData("country", country);

					linkPhonePrefix.setText(country.getPhonePrefix());
					phonePrefix.setText(country.getPhonePrefix());
					faxPrefix.setText(country.getPhonePrefix());

					phone.setText(formatPhoneNumber(phone.getText()));
					fax.setText(formatPhoneNumber(fax.getText()));
					linkPhone.setText(formatPhoneNumber(linkPhone.getText()));

					LinkPersonAddress link = getLink();
					if (link != null)
					{
						String p = link.getAddress().getPhone();
						p = p.startsWith(phonePrefix.getText()) ? p.substring(phonePrefix.getText().length()) : p;
						FormEditorLinkPage.this.phone.setText(formatPhoneNumber(p));
						p = link.getAddress().getFax();
						p = p.startsWith(faxPrefix.getText()) ? p.substring(faxPrefix.getText().length()) : p;
						FormEditorLinkPage.this.fax.setText(formatPhoneNumber(p));
						p = link.getPhone();
						p = p.startsWith(linkPhonePrefix.getText()) ? p.substring(linkPhonePrefix.getText().length())
								: p;
						FormEditorLinkPage.this.linkPhone.setText(formatPhoneNumber(p));
					}

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
				setDirty();
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
				setDirty();
			}
		});
		this.city.addFocusListener(new FocusListener()
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
				Text text = (Text) e.getSource();
				String value = text.getText();
				if (PersonSettings.getInstance().isAddBlankAfterPointInCity())
				{
					if (value.contains("."))
					{
						if (!value.contains(". "))
						{
							value = value.replace(".", ". ");
							text.setText(value);
						}
					}
				}
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
				setDirty();
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
		final AddressType[] addressTypes = getAddressTypes();

		ScrolledForm scrolledForm = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		final Composite composite = toolkit.createComposite(scrolledForm.getBody());
		composite.setLayoutData(new ColumnLayoutData(ColumnLayoutData.FILL));
		composite.setLayout(new GridLayout(4 + addressTypes.length - 1, false));

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

		for (final AddressType addressType : addressTypes)
		{
			if (!this.getLink().getAddressType().getId().equals(addressType.getId()))
			{
				if (addressType.getImage() == null)
				{
					Hyperlink link = toolkit.createHyperlink(composite, addressType.getName(), SWT.NONE);
					link.addHyperlinkListener(new HyperlinkAdapter()
					{
						@Override
						public void linkActivated(final HyperlinkEvent e)
						{
							String id = "link.page." + addressType.getId().toString();
							IFormPage page = getEditor().findPage(id);
							if (page == null)
							{
								addAddressPage(addressType, id);
							}
							else
							{
								getEditor().setActivePage(id);
							}
						}
					});
				}
				else
				{
					hyperlink = toolkit.createImageHyperlink(composite, SWT.NONE);
					hyperlink.setImage(addressType.getImage());
					hyperlink.addHyperlinkListener(new HyperlinkAdapter()
					{
						@Override
						public void linkActivated(final HyperlinkEvent e)
						{
							String id = "link.page." + addressType.getId().toString();
							IFormPage page = getEditor().findPage(id);
							if (page == null)
							{
								addAddressPage(addressType, id);
							}
							else
							{
								getEditor().setActivePage(id);
							}
						}
					});
				}
			}
		}

		changeAddressTypeHyperlink = toolkit.createImageHyperlink(composite, SWT.NONE);
		changeAddressTypeHyperlink.addHyperlinkListener(new HyperlinkAdapter()
		{
			@Override
			public void linkActivated(final HyperlinkEvent e)
			{
				List<AddressType> unusedAddressTypes = new ArrayList<AddressType>();
				Collection<IFormPage> usedPages = getEditor().getPages();
				for (AddressType addressType : addressTypes)
				{
					boolean found = false;
					for (IFormPage usedPage : usedPages)
					{
						if (usedPage instanceof FormEditorLinkPage)
						{
							FormEditorLinkPage page = (FormEditorLinkPage) usedPage;
							if (page.getLink().getAddressType().getId().equals(addressType.getId()))
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
		});
		changeAddressTypeHyperlink.setEnabled(true);
		Image image = Activator.getDefault().getImageRegistry().get(Activator.KEY_CHANGE_ADDRESS);
		changeAddressTypeHyperlink.setImage(image);
		changeAddressTypeHyperlink.setEnabled(!getUnusedAddressTypes(addressTypes).isEmpty());

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
		LinkPersonAddress link = getLink();
		boolean enabled = link == null ? false : link.getPerson().getDefaultLink() != link;
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
				"Geben Sie hier die gewünschten Adressdaten ein. Im Feld Strasse können Sie CTRL+Space verwenden, um aus bestehenden Adressen auszuwählen.",
				5);

		createAddressContactsSectionPart(managedForm, "Kontakte", "Adressenbezogene Kontaktmöglichkeiten", 3);

		createLinkSectionPart(managedForm, "Zusatzinformationen",
				"Hier können zusätzliche Informationen zu " + this.getText()
						+ " erfasst werden, die sich auf diese Adresse beziehen.", 3);

		createAddressLabelExampleSectionPart(managedForm, "Vorschau Adressetikette",
				"Vorschau der Adressetiketten für Einzel- und, falls gegeben, Sammeladresse.", 2);

		loadValues();
		getEditor().clearDirty();
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
				setDirty();
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

		this.linkPhone = toolkit.createText(client, "");
		this.linkPhone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.linkPhone.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty();
			}
		});
		this.linkPhone.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				linkPhone.setText(removeSpaces(linkPhone.getText()));
			}

			@Override
			public void focusLost(final FocusEvent e)
			{
				linkPhone.setText(formatPhoneNumber(linkPhone.getText()));
			}
		});

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
				setDirty();
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

	private String formatPhoneNumber(String value)
	{
		if (!FormEditorLinkPage.this.countryViewer.getSelection().isEmpty())
		{
			IStructuredSelection ssel = (IStructuredSelection) FormEditorLinkPage.this.countryViewer.getSelection();
			if (ssel.getFirstElement() instanceof Country)
			{
				if (!value.isEmpty())
				{
					Country country = (Country) ssel.getFirstElement();
					try
					{
						PhoneNumber phoneNumber = phoneUtil.parse(value, country.getIso3166alpha2());
						value = phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL);
					}
					catch (NumberParseException ex)
					{

					}
				}
			}
		}
		return value;
	}

	@Override
	public Object getAdapter(final Class clazz)
	{
		if (clazz.equals(LinkPersonAddress.class))
		{
			return this.getLink();
		}
		return super.getAdapter(clazz);
	}

	public String getAddress()
	{
		return this.address.getText();
	}

	private AddressType[] getAddressTypes()
	{
		Collection<AddressType> addressTypes = null;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			AddressTypeQuery query = (AddressTypeQuery) service.getQuery(AddressType.class);
			addressTypes = query.selectAll(false);
		}
		tracker.close();
		return addressTypes == null ? new AddressType[0] : addressTypes.toArray(new AddressType[0]);
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
			msg.setMessage("Der Ländercode fehlt.");
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
		PersonEditorInput input = (PersonEditorInput) this.getEditor().getEditorInput();
		Collection<LinkPersonAddress> links = input.getEntity().getLinks();
		for (LinkPersonAddress link : links)
		{
			if (link.getAddressType().getId().equals(this.addressType.getId()))
			{
				return link;
			}
		}
		return null;
	}

	public String getText()
	{
		return PersonFormatter.getInstance().formatFirstnameLastname(getLink().getPerson());
	}

	private List<AddressType> getUnusedAddressTypes(final AddressType[] addressTypes)
	{
		List<AddressType> unusedAddressTypes = new ArrayList<AddressType>();
		Collection<IFormPage> usedPages = getEditor().getPages();
		for (AddressType addressType : addressTypes)
		{
			boolean found = false;
			for (IFormPage usedPage : usedPages)
			{
				if (usedPage instanceof FormEditorLinkPage)
				{
					FormEditorLinkPage page = (FormEditorLinkPage) usedPage;
					if (page.getLink().getAddressType().getId().equals(addressType.getId()))
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
		return unusedAddressTypes;
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
					if (person.getId().equals(getLink().getPerson().getId()))
					{

					}
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = getLink();
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
					LinkPersonAddress link = getLink();
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
		LinkPersonAddress link = getLink();
		phone.setText(formatPhoneNumber(link.getAddress().getPhone()));
		fax.setText(formatPhoneNumber(link.getAddress().getFax()));
		this.website.setText(link.getAddress().getWebsite());
	}

	private void loadAddressValues()
	{
		LinkPersonAddress link = getLink();
		if (link.getAddress().getSalutation() != null)
		{
			this.salutationViewer.setSelection(new StructuredSelection(new AddressSalutation[] { link.getAddress()
					.getSalutation() }));
		}
		this.name.setText(link.getAddress().getName());
		this.anotherLine.setText(link.getAddress().getAnotherLine());
		this.address.setText(link.getAddress().getAddress());
		this.pob.setText(link.getAddress().getPob());
		if (link.getAddress().getCountry() == null)
		{
			link.getAddress().setCountry(AddressFormatter.getInstance().getCountry());
		}
		this.countryViewer.setSelection(new StructuredSelection(link.getAddress().getCountry()));
		this.zip.setText(link.getAddress().getZip());
		this.city.setText(link.getAddress().getCity());
	}

	private void loadLinkValues()
	{
		LinkPersonAddress link = getLink();
		this.linkFunction.setText(link.getFunction());
		linkPhone.setText(formatPhoneNumber(link.getPhone()));
		this.linkEmail.setText(link.getEmail());

	}

	@Override
	public void loadValues()
	{
		loadAddressValues();
		loadAddressContactsValues();
		loadLinkValues();
		this.getEditor().clearDirty();
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
			Address address = proposal.getPersonAddressLink().getAddress();
			if (address.getId() != null)
			{
				LinkPersonAddress link = getLink();
				link.setAddress(proposal.getPersonAddressLink().getAddress());
				loadAddressValues();
				loadAddressContactsValues();
			}
		}
		else if (contentProposal instanceof CityContentProposal)
		{
			CityContentProposal proposal = (CityContentProposal) contentProposal;
			city.setText(proposal.getZipCode().getCity());
		}
	}

	private String removeSpaces(final String value)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < value.length(); i++)
		{
			if ("0123456789".contains(value.substring(i, i + 1)))
			{
				builder = builder.append(value.substring(i, i + 1));
			}
		}
		return builder.toString();
	}

	private void saveAddressContactsValues()
	{
		LinkPersonAddress link = getLink();
		link.getAddress().setPhone(removeSpaces(this.phone.getText()));
		link.getAddress().setFax(removeSpaces(this.fax.getText()));
		link.getAddress().setEmail(this.email.getText());
		link.getAddress().setWebsite(this.website.getText());
	}

	private void saveAddressValues()
	{
		LinkPersonAddress link = getLink();
		StructuredSelection ssel = (StructuredSelection) this.salutationViewer.getSelection();
		if (ssel.isEmpty())
		{
			link.getAddress().setSalutation(null);
		}
		else
		{
			link.getAddress().setSalutation((AddressSalutation) ssel.getFirstElement());
		}
		link.getAddress().setName(name.getText());
		link.getAddress().setAnotherLine(anotherLine.getText());
		link.getAddress().setAddress(this.address.getText());
		link.getAddress().setPob(this.pob.getText());
		ssel = (StructuredSelection) countryViewer.getSelection();
		if (ssel.getFirstElement() instanceof Country)
		{
			link.getAddress().setCountry((Country) ssel.getFirstElement());
		}
		else
		{
			link.getAddress().setCountry(null);
		}
		link.getAddress().setZip(this.zip.getText());
		link.getAddress().setCity(this.city.getText());
	}

	private void saveLinkValues()
	{
		LinkPersonAddress link = getLink();
		link.setFunction(linkFunction.getText());
		link.setPhone(removeSpaces(this.linkPhone.getText()));
		link.setEmail(this.linkEmail.getText());
	}

	@Override
	public void saveValues()
	{
		saveAddressValues();
		saveAddressContactsValues();
		saveLinkValues();
		getEditor().clearDirty();
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

	@Override
	public void setDirty()
	{
		this.setDirty(true);
		this.getEditor().setDirty();
	}

	@Override
	public void setDirty(final boolean dirty)
	{
		this.dirty = dirty;
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
						else if (variable.equals("${organization}"))
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
						else if (variable.equals("${function}"))
						{
							line = line.replace(variable, this.linkFunction.getText());
						}
						else if (variable.equals("${organization}"))
						{
							IStructuredSelection ssel = (IStructuredSelection) this.salutationViewer.getSelection();
							if (ssel.getFirstElement() instanceof AddressSalutation)
							{
								AddressSalutation salutation = (AddressSalutation) ssel.getFirstElement();
								if (salutation.isShowAddressNameForPersons())
								{
									line = line.replace(variable, this.name.getText());
								}
								else
								{
									line = line.replace(variable, "").trim();
								}
							}
							else
							{
								line = line.replace(variable, this.name.getText());
							}
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

	@Override
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
