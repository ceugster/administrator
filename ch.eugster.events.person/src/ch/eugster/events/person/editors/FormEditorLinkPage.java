package ch.eugster.events.person.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
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
import org.eclipse.ui.progress.UIJob;
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
import ch.eugster.events.persistence.model.ExtendedField;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.FieldExtensionTarget;
import ch.eugster.events.persistence.model.FieldExtensionType;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.LinkPersonAddressExtendedField;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.AddressSalutationQuery;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.queries.FieldExtensionQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.queries.ZipCodeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.dialogs.ChangeAddressTypeDialog;
import ch.eugster.events.person.preferences.PreferenceInitializer;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.helpers.BrowseHelper;
import ch.eugster.events.ui.helpers.EmailHelper;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class FormEditorLinkPage extends FormPage implements IPersonFormEditorPage, IContentProposalListener,
		IPropertyChangeListener
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

	private static final String EXTENDED_FIELD_SECTION_EXPANDED = "extended.field.section.expanded";

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

	private Label singlePolite;

	private Label groupPolite;

	private ImageHyperlink deleteHyperlink;

	private ImageHyperlink changeAddressTypeHyperlink;

	private Label singleLabel;

	private Label groupLabel;

	private AddressType addressType;

	private PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

	private Collection<FieldExtension> extensions = new ArrayList<FieldExtension>();

	private Map<Long, ExtendedField> extendedFields = new HashMap<Long, ExtendedField>();

	private Map<Long, Control> extendedFieldControls = new HashMap<Long, Control>();

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

	private void addExtendedFields(final Composite parent, FormToolkit toolkit, int numColumns)
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				FieldExtensionQuery query = (FieldExtensionQuery) service.getQuery(FieldExtension.class);
				Collection<FieldExtension> extensions = query.selectByTarget(FieldExtensionTarget.PERSON, false);
				if (extensions.size() > 0)
				{
					LinkPersonAddress link = getLink();
					if (link != null)
					{
						for (LinkPersonAddressExtendedField field : link.getExtendedFields())
						{
							this.extendedFields.put(field.getFieldExtension().getId(), field);
						}
						for (FieldExtension extension : extensions)
						{
							ExtendedField field = this.extendedFields.get(extension.getId());
							if (field == null)
							{
								if (extension.getTarget().equals(FieldExtensionTarget.PERSON))
								{
									field = LinkPersonAddressExtendedField.newInstance(link, extension);
									field.setValue(extension.getDefaultValue());
									extendedFields.put(field.getFieldExtension().getId(), field);
								}
							}
							Label label = toolkit.createLabel(parent, extension.getLabel());
							label.setLayoutData(new GridData());

							GridData gridData = extension.getWidthHint() == 0 ? new GridData(GridData.FILL_HORIZONTAL)
									: new GridData();
							gridData.horizontalSpan = numColumns - 1;
							if (extension.getWidthHint() != 0)
							{
								gridData.widthHint = extension.getWidthHint();
							}
							if (extension.getHeightHint() != 0)
							{
								gridData.heightHint = extension.getHeightHint();
							}
							if (extension.getType().equals(FieldExtensionType.TEXT))
							{
								Text text = toolkit.createText(parent, "");
								text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
								text.setLayoutData(gridData);
								extension.getType().addListeners(text, this);
								extendedFieldControls.put(extension.getId(), text);
							}
							else
							{
								Control control = extension.getType().createControl(parent, extension.getStyle());
								control.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
								control.setLayoutData(gridData);
								extension.getType().addListeners(control, this);
								extendedFieldControls.put(extension.getId(), control);
							}
						}
					}
				}
			}
		}
		finally
		{
			tracker.close();
		}
	}

	private void loadExtendedFieldValues()
	{
		for (ExtendedField field : extendedFields.values())
		{
			Control control = extendedFieldControls.get(field.getFieldExtension().getId());
			field.getFieldExtension().getType().setInput(control, AbstractEntity.stringValueOf(field.getValue()));
		}
	}

	private void loadExtendedFieldValues(FormEditorLinkPage oldPage)
	{
		for (ExtendedField field : extendedFields.values())
		{
			ExtendedField oldField = oldPage.extendedFields.get(field.getFieldExtension().getId());
			if (oldField != null)
			{
				Control oldControl = oldPage.extendedFieldControls.get(oldField.getFieldExtension().getId());
				String value = oldField.getFieldExtension().getType().getInput(oldControl);
				Control control = extendedFieldControls.get(field.getFieldExtension().getId());
				field.getFieldExtension().getType().setInput(control, AbstractEntity.stringValueOf(value));
			}
		}
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
				setDirty(true);
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
				setDirty(true);
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

		gridData = new GridData(GridData.FILL_HORIZONTAL);
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
		groupLabel.setEnabled((getLink() == null || getLink().isDeleted() || getLink().getPerson().isDeleted()) ? false
				: getLink().getAddress().getPersonLinks().size() > 1);

		label = toolkit.createLabel(client, "Briefanrede", SWT.NONE);
		label.setLayoutData(new GridData());

		label = toolkit.createLabel(client, "Briefanrede", SWT.NONE);
		label.setLayoutData(new GridData());

		singlePolite = toolkit.createLabel(client, "", SWT.BORDER);
		singlePolite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		groupPolite = toolkit.createLabel(client, "", SWT.BORDER);
		groupPolite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		toolkit.paintBordersFor(client);
	}

	private void createExtendedFieldsSection(final IManagedForm managedForm, final String title,
			final String description, final int numColumns)
	{
		Section section = createSection(managedForm, title, description, Section.TWISTIE | Section.TITLE_BAR
				| Section.DESCRIPTION | Section.EXPANDED, EXTENDED_FIELD_SECTION_EXPANDED);
		FormToolkit toolkit = managedForm.getToolkit();
		Composite client = createClientComposite(toolkit, section, numColumns);
		addExtendedFields(client, toolkit, numColumns);
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
				AddressSalutation addressSalutation = (AddressSalutation) ssel.getFirstElement();
				groupPolite.setText(addressSalutation == null ? "" : addressSalutation.getPolite());

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

					String number = formatPhoneNumber(phone.getText());
					phone.setText(number);
					fax.setText(formatPhoneNumber(fax.getText()));
					linkPhone.setText(formatPhoneNumber(linkPhone.getText()));

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
//				city.setEnabled(zip.getData("zipCode") == null);
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

			// @Override
			// public void focusLost(final FocusEvent e)
			// {
			// zipSelected(zip.getText());
			// }
		});

		proposalAdapter = new ContentProposalAdapter(this.zip, new TextContentAdapter(),
				new CityContentProposalProvider(countryViewer, zip), null, null);
		proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		proposalAdapter.addContentProposalListener(this);

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

		// proposalAdapter = new ContentProposalAdapter(this.city, new
		// TextContentAdapter(),
		// new CityContentProposalProvider(countryViewer, zip), null, null);
		// proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		// proposalAdapter.addContentProposalListener(this);

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
		hyperlink.setToolTipText("zur Seite mit den Personenangaben wechseln.");
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
			LinkPersonAddress link = getLink();
			if (link != null && link.getAddressType().getId().equals(addressType.getId()))
			{
				if (addressType.getImage() == null)
				{
					Hyperlink hlink = toolkit.createHyperlink(composite, addressType.getName(), SWT.NONE);
					hlink.setToolTipText("zur Seite " + addressType.getName()
							+ " wechseln (falls noch nicht vorhanden, wird sie eingefügt).");
					hlink.addHyperlinkListener(new HyperlinkAdapter()
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
					hyperlink.setToolTipText("zur Seite " + addressType.getName()
							+ " wechseln (falls noch nicht vorhanden, wird sie eingefügt).");
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
		changeAddressTypeHyperlink.setToolTipText("Adresstyp ändern");
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
							LinkPersonAddress link = page.getLink();
							if (link != null && link.getAddressType().getId().equals(addressType.getId()))
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
				LinkPersonAddress link = FormEditorLinkPage.this.getLink();
				if (link != null)
				{
					link.setDeleted(true);
					getEditor().removePage(getEditor().getActivePage());
				}
			}
		});
		/**
		 * set enabled only and only if link has an id and there are at least
		 * one other active address page!
		 */
		LinkPersonAddress link = getLink();
		boolean enabled = link == null || link.isDeleted() || link.getPerson().isDeleted() ? false : link.getPerson()
				.getDefaultLink() != link;
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
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				FieldExtensionQuery query = (FieldExtensionQuery) service.getQuery(FieldExtension.class);
				extensions = query.selectByTarget(FieldExtensionTarget.PA_LINK, false);
				if (extensions.size() > 0)
				{
					createExtendedFieldsSection(managedForm, "Zusatzinformationen", "", 4);
				}
			}
		}
		finally
		{
			tracker.close();
		}
		createLinkSectionPart(managedForm, "Zusatzinformationen",
				"Hier können zusätzliche Informationen zu " + this.getText()
						+ " erfasst werden, die sich auf diese Adresse beziehen.", 3);
		createAddressLabelExampleSectionPart(managedForm, "Vorschau Adressetikette",
				"Vorschau der Adressetiketten für Einzel- und, falls gegeben, Sammeladresse.", 2);

		IEditorInput input = this.getEditor().getEditorInput();
		if (input instanceof Initializable)
		{
			Initializable init = (Initializable) input;
			initializeFields(init.getInitialValues());
		}
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
				updateSingleLabel();
				updateGroupLabel();
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

		this.linkPhone = toolkit.createText(client, "");
		this.linkPhone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.linkPhone.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
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

	// private ZipCode findZipCode(final Country country, final String zip)
	// {
	// Collection<ZipCode> zipCodes = null;
	// ServiceTracker tracker = new
	// ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
	// ConnectionService.class.getName(), null);
	// tracker.open();
	// ConnectionService service = (ConnectionService) tracker.getService();
	// if (service != null)
	// {
	// ZipCodeQuery query = (ZipCodeQuery) service.getQuery(ZipCode.class);
	// zipCodes = query.selectByCountryAndZipCode(country, zip);
	// }
	// tracker.close();
	// if (zipCodes.iterator().hasNext())
	// {
	// return zipCodes.iterator().next();
	// }
	// return null;
	// }

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
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz)
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
			if (!link.isDeleted())
			{
				if (link.getAddressType().getId().equals(this.addressType.getId()))
				{
					return link;
				}
			}
		}
		return null;
	}

	public String getText()
	{
		LinkPersonAddress link = getLink();
		if (link == null || link.isDeleted() || link.getPerson().isDeleted())
		{
			return "";
		}
		else
		{
			return PersonFormatter.getInstance().formatFirstnameLastname(link.getPerson());
		}
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
					LinkPersonAddress link = page.getLink();
					if (link != null && link.getAddressType().getId().equals(addressType.getId()))
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
			public void postUpdate(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						if (entity instanceof LinkPersonAddress)
						{
							LinkPersonAddress link = getLink();
							if (link != null)
							{
								LinkPersonAddress other = (LinkPersonAddress) entity;
								if (link.getId() != null && link.getPerson().getId() != null)
								{
									if (other.getPerson().getId().equals(link.getPerson().getId()))
									{
										if (deleteHyperlink != null && !deleteHyperlink.isDisposed())
										{
											boolean enable = other.getPerson().getDefaultLink().getId()
													.equals(link.getId());
											deleteHyperlink.setEnabled(enable);
										}
									}
								}
							}
						}
						else if (entity instanceof Person)
						{
							LinkPersonAddress link = getLink();
							if (link != null)
							{
								Person person = (Person) entity;
								if (person.getId().equals(link.getPerson().getId()))
								{
									if (link.getId() != null)
									{
										if (deleteHyperlink != null && !deleteHyperlink.isDisposed())
										{
											boolean enable = person.getDefaultLink() != null
													&& person.getDefaultLink().getId().equals(link.getId());
											deleteHyperlink.setEnabled(!enable);
										}
									}
								}
							}
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
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
		if (link != null)
		{
			this.phone.setText(formatPhoneNumber(link.getAddress().getPhone()));
			this.fax.setText(formatPhoneNumber(link.getAddress().getFax()));
			this.email.setText(link.getAddress().getEmail());
			this.website.setText(link.getAddress().getWebsite());
		}
	}

	private void loadAddressContactsValues(FormEditorLinkPage oldPage)
	{
		this.phone.setText(oldPage.phone.getText());
		this.fax.setText(oldPage.fax.getText());
		this.email.setText(oldPage.email.getText());
		this.website.setText(oldPage.website.getText());
	}

	private void loadAddressValues()
	{
		LinkPersonAddress link = getLink();
		if (link != null)
		{
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
			this.zip.setData("zipCode", link.getAddress().getZipCode());
			this.zip.setText(link.getAddress().getZip());
			this.city.setText(link.getAddress().getCity());
			String province = link.getAddress().getZipCode() == null ? link.getAddress().getProvince() : link
					.getAddress().getZipCode().getState();
			if (province != null)
			{
				this.provinceViewer.setSelection(new StructuredSelection(new String[] { province }));
			}
		}
	}

	private void loadAddressValues(FormEditorLinkPage oldPage)
	{
		this.salutationViewer.setSelection(oldPage.salutationViewer.getSelection());
		this.name.setText(oldPage.name.getText());
		this.anotherLine.setText(oldPage.anotherLine.getText());
		this.address.setText(oldPage.address.getText());
		this.pob.setText(oldPage.pob.getText());
		this.countryViewer.setSelection(oldPage.countryViewer.getSelection());
		this.zip.setData("zipCode", oldPage.zip.getData("zipCode"));
		this.zip.setText(oldPage.zip.getText());
		this.city.setText(oldPage.city.getText());
		this.provinceViewer.setSelection(oldPage.provinceViewer.getSelection());
	}

	private void loadLinkValues()
	{
		LinkPersonAddress link = getLink();
		if (link != null)
		{
			this.linkFunction.setText(link.getFunction());
			this.linkPhone.setText(formatPhoneNumber(link.getPhone()));
			this.linkEmail.setText(link.getEmail());
		}
	}

	private void loadLinkValues(FormEditorLinkPage oldPage)
	{
		this.linkFunction.setText(oldPage.linkFunction.getText());
		this.linkPhone.setText(oldPage.linkPhone.getText());
		this.linkEmail.setText(oldPage.linkEmail.getText());

	}

	public void loadValues()
	{
		this.loadAddressContactsValues();
		this.loadLinkValues();
		this.loadExtendedFieldValues();
		this.loadAddressValues();
		this.setDirty(false);
	}

	public void loadValues(FormEditorLinkPage oldPage)
	{
		this.loadAddressContactsValues(oldPage);
		this.loadLinkValues(oldPage);
		this.loadExtendedFieldValues(oldPage);
		this.loadAddressValues(oldPage);
	}

	private void saveExtendedFieldValues()
	{
		for (ExtendedField field : extendedFields.values())
		{
			Control control = extendedFieldControls.get(field.getFieldExtension().getId());
			field.setValue(field.getFieldExtension().getType().getInput(control));
			if (field.getId() == null && !field.getValue().isEmpty())
			{
				if (field.getFieldExtension().getTarget().equals(FieldExtensionTarget.PA_LINK))
				{
					LinkPersonAddress link = getLink();
					if (link != null)
					{
						link.addExtendedFields((LinkPersonAddressExtendedField) field);
					}
				}
			}
		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event)
	{
		if (this.name != null && !this.name.isDisposed())
		{
			this.updateSingleLabel();
		}
	}

	@Override
	public void proposalAccepted(final IContentProposal contentProposal)
	{
		if (contentProposal instanceof AddressContentProposal)
		{
			AddressContentProposal proposal = (AddressContentProposal) contentProposal;
			Address address = proposal.getAddress();
			if (address.getId() != null)
			{
				LinkPersonAddress link = getLink();
				if (link != null)
				{
					link.setAddress(address);
					loadAddressValues();
					loadAddressContactsValues();
				}
			}
		}
		else if (contentProposal instanceof CityContentProposal)
		{
			CityContentProposal proposal = (CityContentProposal) contentProposal;
			zip.setData("zipCode", proposal.getZipCode());
			zip.setText(proposal.getZipCode().getZip());
			city.setText(proposal.getZipCode().getCity());
			countryViewer.setSelection(new StructuredSelection(proposal.getZipCode().getCountry()));
			countryViewer.setData("country", proposal.getZipCode().getCountry());
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
		if (link != null)
		{
			link.getAddress().setPhone(removeSpaces(this.phone.getText()));
			link.getAddress().setFax(removeSpaces(this.fax.getText()));
			link.getAddress().setEmail(this.email.getText());
			link.getAddress().setWebsite(this.website.getText());
		}
	}

	private void saveAddressValues()
	{
		LinkPersonAddress link = getLink();
		if (link != null)
		{
			AddressSalutation selectedSalutation = null;
			StructuredSelection ssel = (StructuredSelection) this.salutationViewer.getSelection();
			if (ssel.getFirstElement() instanceof AddressSalutation)
			{
				selectedSalutation = (AddressSalutation) ssel.getFirstElement();
				if (selectedSalutation.getId() == null)
				{
					selectedSalutation = null;
				}
			}
			link.getAddress().setSalutation(selectedSalutation);
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
			link.getAddress().setZipCode((ZipCode) this.zip.getData("zipCode"));
			link.getAddress().setCity(this.city.getText());
		}
	}

	private void saveLinkValues()
	{
		LinkPersonAddress link = getLink();
		if (link != null)
		{
			link.setFunction(linkFunction.getText());
			link.setPhone(removeSpaces(this.linkPhone.getText()));
			link.setEmail(this.linkEmail.getText());
		}
	}

	public void saveValues()
	{
		saveAddressValues();
		saveAddressContactsValues();
		saveLinkValues();
		saveExtendedFieldValues();
		this.setDirty(false);
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
		Map<String, String> replacements = new HashMap<String, String>();
		StructuredSelection ssel = (StructuredSelection) this.salutationViewer.getSelection();
		String salutation = "";
		if (ssel.getFirstElement() instanceof AddressSalutation)
		{
			AddressSalutation as = (AddressSalutation) ssel.getFirstElement();
			salutation = as.getSalutation();
		}
		replacements.put("${salutation}", salutation);
		replacements.put("${organization}", this.name.getText());
		replacements.put("${anotherline}", this.anotherLine.getText());
		replacements.put("${address}", this.address.getText());
		replacements.put("${pob}", this.pob.getText());
		ssel = (StructuredSelection) this.countryViewer.getSelection();
		String c = null;
		if (ssel.getFirstElement() instanceof Country)
		{
			Country country = (Country) ssel.getFirstElement();
			c = country.getIso3166alpha2();
		}
		else
		{
			c = "";
		}
		replacements.put("${country}", c);
		replacements.put("${zip}", this.zip.getText());
		replacements.put("${city}", this.city.getText());
		this.groupLabel.setText(PersonFormatter.getInstance().replaceAddressLabelVariables(replacements));
	}

	private void updateSingleLabel()
	{
		Map<String, String> replacements = new HashMap<String, String>();
		replacements.put("${salutation}", personPage.getPersonSex() == null ? "" : personPage.getPersonSex()
				.getSalutation());
		replacements.put("${title}", personPage.getPersonTitle() == null ? "" : personPage.getPersonTitle().getTitle());
		replacements.put("${function}", this.linkFunction.getText());
		IStructuredSelection ssel = (IStructuredSelection) this.salutationViewer.getSelection();
		if (ssel.getFirstElement() instanceof AddressSalutation)
		{
			AddressSalutation salutation = (AddressSalutation) ssel.getFirstElement();
			if (salutation.isShowAddressNameForPersons())
			{
				replacements.put("${organization}", this.name.getText());
			}
			else
			{
				replacements.put("${organization}", "");
			}
		}
		else
		{
			replacements.put("${organization}", "");
		}
		replacements.put("${firstname}", personPage.getFirstname() == null ? "" : personPage.getFirstname());
		replacements.put("${lastname}", personPage.getLastname() == null ? "" : personPage.getLastname());
		replacements.put("${anotherline}", this.anotherLine.getText());
		replacements.put("${address}", this.address.getText());
		replacements.put("${pob}", this.pob.getText());
		ssel = (StructuredSelection) this.countryViewer.getSelection();
		String c = null;
		if (ssel.getFirstElement() instanceof Country)
		{
			Country country = (Country) ssel.getFirstElement();
			c = country.getIso3166alpha2();
		}
		else
		{
			c = "";
		}
		replacements.put("${country}", c);
		replacements.put("${zip}", this.zip.getText());
		replacements.put("${city}", this.city.getText());
		this.singleLabel.setText(PersonFormatter.getInstance().replacePersonLabelVariables(replacements));

		if (personPage.getPersonSex() != null && personPage.getPersonForm() != null)
		{
			String pattern = personPage.getPersonSex().getForm(personPage.getPersonForm());
			this.singlePolite.setText(PersonFormatter.getInstance().replaceSalutationVariables(pattern, replacements));
		}
		else
		{
			this.singlePolite.setText("");
		}
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

	// private void zipSelected(final String code)
	// {
	// if (countryViewer.getData("country") instanceof Country &&
	// !code.isEmpty())
	// {
	// Country country = (Country) countryViewer.getData("country");
	// ZipCode zipCode = findZipCode(country, code);
	// zip.setData("zipCode", zipCode);
	// if (zipCode != null)
	// {
	// city.setText(zipCode.getCity());
	// provinceViewer.setSelection(new StructuredSelection(zipCode.getState()));
	// }
	// }
	// }

	private void initializeFields(Map<String, String> values)
	{
		Set<Entry<String, String>> entries = values.entrySet();
		for (Entry<String, String> entry : entries)
		{
			if (entry.getKey().equals("organization"))
			{
				this.name.setText(entry.getValue());
				this.setDirty(true);
			}
			else if (entry.getKey().equals("pob"))
			{
				this.pob.setText(entry.getValue());
				this.setDirty(true);
			}
			else if (entry.getKey().equals("address"))
			{
				this.address.setText(entry.getValue());
				this.setDirty(true);
			}
			else if (entry.getKey().equals("zip"))
			{
				this.zip.setText(entry.getValue());
				this.setDirty(true);
			}
			else if (entry.getKey().equals("city"))
			{
				this.city.setText(entry.getValue());
				this.setDirty(true);
			}
		}
	}

	private class AddressContentProposalProvider implements IContentProposalProvider
	{
		private final FormEditorLinkPage editorPage;

		public AddressContentProposalProvider(final FormEditorLinkPage editorPage)
		{
			this.editorPage = editorPage;
		}

		private boolean addressExists(final LinkPersonAddress link)
		{
			boolean exists = false;
			PersonEditorInput input = (PersonEditorInput) this.editorPage.getEditor().getEditorInput();
			Person person = input.getEntity();
			if (person.getId() != null && person.getId().equals(link.getPerson().getId()))
			{
				Collection<IFormPage> pages = editorPage.getEditor().getPages();
				for (IFormPage page : pages)
				{
					if (page instanceof FormEditorLinkPage)
					{
						Address address = ((FormEditorLinkPage) page).getLink().getAddress();
						if (address != null)
						{
							if (address.getId() != null)
							{
								if (page != editorPage && link.getAddress().getId().equals(address.getId()))
								{
									exists = true;
								}
							}
						}
					}
				}
			}
			return exists;
		}

		private boolean addressExists(final Address address)
		{
			return false;
		}

		@Override
		public IContentProposal[] getProposals(final String contents, final int position)
		{
			AddressContentProposal[] proposals = new AddressContentProposal[0];
			if (position > 3)
			{
				Map<String, AddressContentProposal> props = new HashMap<String, AddressContentProposal>();
				ConnectionService service = Activator.getDefault().getConnectionService();
				if (service != null)
				{
					LinkPersonAddressQuery linkQuery = (LinkPersonAddressQuery) service.getQuery(LinkPersonAddress.class);
					Collection<LinkPersonAddress> links = linkQuery.selectByAddressAsLike(contents);
					Iterator<LinkPersonAddress> linkIterator = links.iterator();
					while (linkIterator.hasNext())
					{
						LinkPersonAddress link = linkIterator.next();
						if (!addressExists(link))
						{
							props.put("L" + link.getId().toString(), new AddressContentProposal(link));
						}
					}
					AddressQuery addressQuery = (AddressQuery) service.getQuery(Address.class);
					Collection<Address> addresses = addressQuery.selectByAddressAsLike(contents);
					Iterator<Address> addressIterator = addresses.iterator();
					while (addressIterator.hasNext())
					{
						Address address = addressIterator.next();
						if (!addressExists(address))
						{
							props.put("A" + address.getId().toString(), new AddressContentProposal(address));
						}
					}
					Address address = Address.newInstance();
					address.setAddress(this.editorPage.getAddress());
					LinkPersonAddress emptyLink = LinkPersonAddress.newInstance(address);
					props.put("L0", new AddressContentProposal(emptyLink));
					proposals = props.values().toArray(new AddressContentProposal[0]);
					Arrays.sort(proposals);
				}
			}
			return proposals;
		}
	}

	private class AddressContentProposal implements IContentProposal, Comparable<AddressContentProposal>
	{
		private LinkPersonAddress link;

		private Address address;

		public AddressContentProposal(final LinkPersonAddress link)
		{
			this.link = link;
			this.address = link.getAddress();
		}

		public AddressContentProposal(final Address address)
		{
			this.address = address;
		}

		@Override
		public int compareTo(final AddressContentProposal other)
		{
			if (other instanceof AddressContentProposal)
			{
				if (this.link == null || this.link.getId() == null || this.link.isDeleted()
						|| this.link.getPerson().isDeleted())
				{
					if (this.address.getId() == null)
					{
						return -1;
					}
					else if (other.getAddress().getId() == null)
					{
						return 1;
					}
				}
				else
				{
					if (this.link.getId() == null)
					{
						return -1;
					}
					else if (other.getAddress().getId() == null)
					{
						return 1;
					}
				}
				return this.getContent().compareTo(other.getContent());
			}
			return 0;
		}

		@Override
		public String getContent()
		{
			return this.link == null || this.link.isDeleted() || this.link.getPerson().isDeleted() ? this.address
					.getAddress() : this.link.getAddress().getAddress();
		}

		@Override
		public int getCursorPosition()
		{
			return 0;
		}

		@Override
		public String getDescription()
		{
			return "Adressen zur Auswahl";
		}

		@Override
		public String getLabel()
		{
			StringBuilder builder = new StringBuilder(AddressFormatter.getInstance().formatId(this.address));
			builder = builder.append(", " + this.address.getAddress());
			if (!this.address.getCity().isEmpty())
			{
				if (builder.length() > 0)
				{
					builder.append(", ");
				}
				builder.append(AddressFormatter.getInstance().formatCityLine(this.address));
			}
			if (this.link == null || this.link.getId() == null || this.link.isDeleted()
					|| this.link.getPerson().isDeleted())
			{
				if (!this.address.getName().isEmpty())
				{
					if (builder.length() > 0)
					{
						builder.append(", ");
					}
					builder.append(this.address.getName());
				}
			}
			else
			{
				if (this.link.getPerson() != null)
				{
					if (!this.link.getPerson().getLastname().isEmpty())
					{
						if (builder.length() > 0)
							builder.append(", ");
						builder.append(PersonFormatter.getInstance().formatLastnameFirstname(this.link.getPerson()));
					}
				}
			}
			return builder.toString();
		}

		public LinkPersonAddress getPersonAddressLink()
		{
			return this.link;
		}

		public Address getAddress()
		{
			return this.address;
		}
	}
}
