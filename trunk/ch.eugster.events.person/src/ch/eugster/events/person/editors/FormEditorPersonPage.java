package ch.eugster.events.person.editors;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.nebula.jface.viewer.radiogroup.RadioGroupViewer;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.radiogroup.RadioGroup;
import org.eclipse.nebula.widgets.radiogroup.forms.RadioGroupFormToolkit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
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
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.ExtendedField;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.FieldExtensionTarget;
import ch.eugster.events.persistence.model.FieldExtensionType;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonExtendedField;
import ch.eugster.events.persistence.model.PersonForm;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.model.PersonSex;
import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.queries.FieldExtensionQuery;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.persistence.queries.PersonSexQuery;
import ch.eugster.events.persistence.queries.PersonTitleQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.preferences.PreferenceInitializer;
import ch.eugster.events.person.views.PersonTitleSorter;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.helpers.BrowseHelper;
import ch.eugster.events.ui.helpers.EmailHelper;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class FormEditorPersonPage extends FormPage implements IPersonFormEditorPage
{
	private static final String ID = FormEditorPersonPage.class.getName();

	private static final String IDENTITY_SECTION_EXPANDED = "identity.section.expanded";

	private static final String CONTACTS_SECTION_EXPANDED = "contacts.section.expanded";

	private static final String FURTHER_SECTION_EXPANDED = "further.section.expanded";

	private static final String NOTE_SECTION_EXPANDED = "note.section.expanded";

	private static final String EMAIL_LABEL = "Email (senden)";

	private static final String EMAIL_LINK = "Email (<a>senden</a>)";

	private static final String WEBSITE_LABEL = "Webseite (öffnen)";

	private static final String WEBSITE_LINK = "Webseite (<a>öffnen</a>)";

	private boolean dirty;

	private EntityAdapter entityAdapter;

	private IDialogSettings dialogSettings;

	private RadioGroupViewer sexRadioGroupViewer;

	private RadioGroupViewer formRadioGroupViewer;

	private Text salutation;

	private ComboViewer titleViewer;

	private Text firstname;

	private ControlDecoration decoration;

	private Text lastname;

	private Section notesSection;

	private ImageHyperlink notesSelector;

	private CDateTime birthdate;

	private CDateTime birthyear;

	private Text profession;

	private Text notes;

	private ComboViewer domainViewer;

	private ComboViewer countryViewer;

	private Text phone;

	private Link send;

	private Text email;

	private Link browse;

	private Text website;

	private Collection<FieldExtension> extensions = new ArrayList<FieldExtension>();

	private PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

	private final Collection<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();

	private Map<Long, ExtendedField> extendedFields = new HashMap<Long, ExtendedField>();

	private Map<Long, Control> extendedFieldControls = new HashMap<Long, Control>();

	public FormEditorPersonPage(final FormEditor editor, final String id, final String title)
	{
		super(editor, id, title);
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
				if (extensions.size() > 0)
				{
					Person person = getPerson();
					for (PersonExtendedField field : person.getExtendedFields())
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
								field = PersonExtendedField.newInstance(person, extension);
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
		finally
		{
			tracker.close();
		}
	}

	public void addListener(final IPropertyChangeListener listener)
	{
		this.listeners.add(listener);
	}

	private void loadExtendedFieldValues()
	{
		for (ExtendedField field : extendedFields.values())
		{
			Control control = extendedFieldControls.get(field.getFieldExtension().getId());
			field.getFieldExtension().getType().setInput(control, AbstractEntity.stringValueOf(field.getValue()));
		}
	}

	private void createButtons(final IManagedForm managedForm, final String title, final String description)
	{
		Person person = getPerson();

		AddressType[] addressTypes = getAddressTypes();

		ScrolledForm scrolledForm = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		Composite composite = toolkit.createComposite(scrolledForm.getBody());
		composite.setLayoutData(new ColumnLayoutData(ColumnLayoutData.FILL));
		composite.setLayout(new GridLayout(addressTypes.length + 3, false));

		notesSelector = toolkit.createImageHyperlink(composite, SWT.NONE);
		if (person.getNotes() == null || person.getNotes().isEmpty())
		{
			notesSelector.setImage(Activator.getDefault().getImageRegistry().get(Activator.KEY_OFF));
		}
		else
		{
			notesSelector.setImage(Activator.getDefault().getImageRegistry().get(Activator.KEY_ON));
		}
		notesSelector.setLayoutData(new GridData());
		notesSelector.addHyperlinkListener(new HyperlinkAdapter()
		{
			@Override
			public void linkActivated(final HyperlinkEvent e)
			{
				notesSection.setExpanded(!notesSection.isExpanded());
				dialogSettings.put(NOTE_SECTION_EXPANDED, notesSection.isExpanded());
				if (notesSection.isExpanded())
				{
					notes.setFocus();
				}
			}
		});

		Label label = toolkit.createLabel(composite, "");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = toolkit.createLabel(composite, "Adressen: ");
		label.setLayoutData(new GridData());

		for (final AddressType addressType : addressTypes)
		{
			if (addressType.getImage() == null)
			{
				Hyperlink hyperlink = toolkit.createHyperlink(composite, addressType.getName(), SWT.NONE);
				hyperlink.addHyperlinkListener(new HyperlinkAdapter()
				{
					@Override
					public void linkActivated(final HyperlinkEvent e)
					{
						String id = "link.page." + addressType.getId().toString();
						IFormPage page = getEditor().findPage(id);
						if (page == null)
						{
							try
							{
								Person person = ((PersonEditorInput) getEditor().getEditorInput()).getEntity();
								Address address = Address.newInstance();
								LinkPersonAddress link = LinkPersonAddress.newInstance(person, address);
								link.setAddressType(addressType);
								page = new FormEditorLinkPage(getEditor(), FormEditorPersonPage.this, id, link);
								getEditor().addPage(page);
								getEditor().setActivePage(page.getId());
							}
							catch (PartInitException pie)
							{
							}
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
				ImageHyperlink hyperlink = toolkit.createImageHyperlink(composite, SWT.NONE);
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
							try
							{
								Person person = ((PersonEditorInput) getEditor().getEditorInput()).getEntity();
								Address address = Address.newInstance();
								LinkPersonAddress link = LinkPersonAddress.newInstance(person, address);
								link.setAddressType(addressType);
								page = new FormEditorLinkPage(getEditor(), FormEditorPersonPage.this, id, link);
								getEditor().addPage(page);
								getEditor().setActivePage(page.getId());
							}
							catch (PartInitException pie)
							{
								pie.printStackTrace();
							}
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

	private Composite createClientComposite(final FormToolkit toolkit, final Section section, final int numColumns)
	{
		Composite client = toolkit.createComposite(section);
		client.setLayout(new GridLayout(numColumns, false));
		section.setClient(client);
		return client;
	}

	private void createContactsSectionPart(final IManagedForm managedForm, final String title,
			final String description, final int numColumns)
	{
		Section section = createSection(managedForm, title, description, CONTACTS_SECTION_EXPANDED);
		FormToolkit toolkit = managedForm.getToolkit();
		final Composite client = createClientComposite(toolkit, section, numColumns);

		Label label = toolkit.createLabel(client, "Handy", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData();
		gridData.widthHint = 48;

		final CCombo combo = new CCombo(client, SWT.FLAT | SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(gridData);
		toolkit.adapt(combo);

		this.countryViewer = new ComboViewer(combo);
		this.countryViewer.setContentProvider(new ArrayContentProvider());
		this.countryViewer.setLabelProvider(new CountryPrefixLabelProvider());
		this.countryViewer.setSorter(new CountryPrefixSorter());
		this.countryViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter(), new CountryPrefixFilter() });
		this.countryViewer.setInput(selectPrefixes());
		this.countryViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				if (ssel.getFirstElement() instanceof Country)
				{
					Country country = (Country) ssel.getFirstElement();
					String numValue = getNumValue(phone.getText());
					if (!numValue.isEmpty())
					{
						try
						{
							PhoneNumber phoneNumber = phoneUtil.parse(numValue, country.getIso3166alpha2());
							String number = phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL);
							phone.setText(number);
						}
						catch (NumberParseException e)
						{

						}
					}
				}
				setDirty(true);
			}
		});

		this.phone = toolkit.createText(client, "");
		this.phone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.phone.addModifyListener(new ModifyListener()
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
				IStructuredSelection ssel = (IStructuredSelection) FormEditorPersonPage.this.countryViewer
						.getSelection();
				if (ssel.getFirstElement() instanceof Country)
				{
					StringBuilder editValue = new StringBuilder();
					String value = FormEditorPersonPage.this.phone.getText().trim();
					value = removeSpaces(value);
					boolean dirty = isDirty();
					FormEditorPersonPage.this.phone.setText(value);
					FormEditorPersonPage.this.phone.setSelection(0, FormEditorPersonPage.this.phone.getText().length());
					if (dirty)
					{
						setDirty(true);
					}
					else
					{
						setDirty(false);
					}
				}
			}

			@Override
			public void focusLost(final FocusEvent e)
			{
				if (!FormEditorPersonPage.this.countryViewer.getSelection().isEmpty())
				{
					IStructuredSelection ssel = (IStructuredSelection) FormEditorPersonPage.this.countryViewer
							.getSelection();
					if (ssel.getFirstElement() instanceof Country)
					{
						String phoneString = phone.getText();
						if (!phoneString.isEmpty())
						{
							Country country = (Country) ssel.getFirstElement();
							try
							{
								PhoneNumber phoneNumber = phoneUtil.parse(phoneString, country.getIso3166alpha2());
								phoneString = phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL);
								boolean dirty = isDirty();
								phone.setText(phoneString);
								if (dirty)
								{
									setDirty(true);
								}
								else
								{
									setDirty(false);
								}

							}
							catch (NumberParseException ex)
							{

							}
						}
					}
				}
			}
		});

		this.send = new Link(client, SWT.NONE);
		this.send.setText(FormEditorPersonPage.EMAIL_LABEL);
		this.send.setToolTipText("Um ein Email zu senden, klicken Sie hier");
		this.send.setLayoutData(new GridData());
		this.send.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				EmailHelper.getInstance().sendEmail(FormEditorPersonPage.this.email.getText());
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
				boolean enabled = EmailHelper.getInstance().isEmailSupported()
						&& EmailHelper.getInstance().isValidAddress(FormEditorPersonPage.this.email.getText());
				if (enabled)
				{
					FormEditorPersonPage.this.send.setText(FormEditorPersonPage.EMAIL_LINK);
					client.layout(true, true);
				}
				else
					FormEditorPersonPage.this.send.setText(FormEditorPersonPage.EMAIL_LABEL);
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

		this.browse = new Link(client, SWT.NONE);
		this.browse.setText(FormEditorPersonPage.WEBSITE_LABEL);
		this.browse.setLayoutData(new GridData());
		this.browse.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				BrowseHelper.getInstance().browse(FormEditorPersonPage.this.website.getText());
			}
		});
		GC gc = new GC(this.browse);
		FontMetrics fm = gc.getFontMetrics();
		int width = FormEditorPersonPage.WEBSITE_LINK.length() * fm.getAverageCharWidth();
		int height = fm.getHeight();
		gc.dispose();
		this.browse.setSize(this.browse.computeSize(width, height));

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
				boolean enabled = BrowseHelper.getInstance().isBrowsingSupported()
						&& BrowseHelper.getInstance().isValidAddress(FormEditorPersonPage.this.website.getText());
				if (enabled)
				{
					FormEditorPersonPage.this.browse.setText(FormEditorPersonPage.WEBSITE_LINK);
				}
				else
				{
					FormEditorPersonPage.this.browse.setText(FormEditorPersonPage.WEBSITE_LABEL);
				}
				client.layout(true, true);
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

		createButtons(managedForm, "Adressen", "Adressen hinzufügen und bearbeiten");
		createIdentitySectionPart(managedForm, "Identität", "", 2);
		createContactsSectionPart(managedForm, "Kontakt", "", 3);

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				FieldExtensionQuery query = (FieldExtensionQuery) service.getQuery(FieldExtension.class);
				extensions = query.selectByTarget(FieldExtensionTarget.PERSON, false);
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
		createFurtherSection(managedForm, "Weitere Angaben", "", 4);
		createNotesSection(managedForm, "Bemerkungen", "", 1);

		loadValues();
		IEditorInput input = this.getEditor().getEditorInput();
		if (input instanceof Initializable)
		{
			Initializable init = (Initializable) input;
			initializeFields(init.getInitialValues());
		}
	}

	private void createFurtherSection(final IManagedForm managedForm, final String title, final String description,
			final int numColumns)
	{
		Section section = createSection(managedForm, title, description, FURTHER_SECTION_EXPANDED);
		FormToolkit toolkit = managedForm.getToolkit();
		Composite client = createClientComposite(toolkit, section, numColumns);

		Label label = toolkit.createLabel(client, "Geburtsdatum", SWT.NONE);
		label.setLayoutData(new GridData());

		GC gc = new GC(label.getDisplay());
		GridData gridData = new GridData();
		gridData.widthHint = gc.textExtent("00.00.0000.0000").x;

		this.birthdate = new CDateTime(client, CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		this.birthdate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.birthdate.setLayoutData(gridData);
		this.birthdate.setNullText("");
		this.birthdate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				setDirty(true);
				birthyear.setSelection(birthdate.getSelection());
			}
		});
		toolkit.adapt(this.birthdate);

		label = toolkit.createLabel(client, "oder -jahr", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = gc.textExtent("0000.0000").x;
		gc.dispose();

		this.birthyear = new CDateTime(client, CDT.SPINNER);
		this.birthyear.setPattern("yyyy");
		this.birthyear.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.birthyear.setLayoutData(gridData);
		this.birthyear.setNullText("");
		this.birthyear.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				setDirty(true);
			}
		});
		toolkit.adapt(this.birthyear);

		label = toolkit.createLabel(client, "Beruf", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		this.profession = toolkit.createText(client, "");
		this.profession.setLayoutData(gridData);
		this.profession.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
			}
		});
		this.profession.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		if (PersonSettings.getInstance().getPersonHasDomain())
		{
			label = toolkit.createLabel(client, "Domäne");
			label.setLayoutData(new GridData());

			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 3;

			CCombo combo = new CCombo(client, SWT.READ_ONLY | SWT.FLAT);
			combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
			combo.setLayoutData(gridData);
			toolkit.adapt(combo);

			this.domainViewer = new ComboViewer(combo);
			this.domainViewer.setContentProvider(new DomainComboContentProvider(PersonSettings.getInstance()
					.isPersonDomainMandatory()));
			this.domainViewer.setLabelProvider(new DomainComboLabelProvider());
			this.domainViewer.setSorter(new DomainComboSorter());
			this.domainViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
			this.domainViewer.setInput(selectDomains());
			this.domainViewer.addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(final SelectionChangedEvent event)
				{
					setDirty(true);
				}
			});

			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			tracker.open();
			ConnectionService service = (ConnectionService) tracker.getService();
			this.domainViewer.setInput(service);
			tracker.close();
		}

		toolkit.paintBordersFor(client);
	}

	private void createExtendedFieldsSection(final IManagedForm managedForm, final String title,
			final String description, final int numColumns)
	{
		Section section = createSection(managedForm, title, description, FURTHER_SECTION_EXPANDED);
		FormToolkit toolkit = managedForm.getToolkit();
		Composite client = createClientComposite(toolkit, section, numColumns);
		addExtendedFields(client, toolkit, numColumns);
		toolkit.paintBordersFor(client);
	}

	private void createIdentitySectionPart(final IManagedForm managedForm, final String title,
			final String description, final int numColumns)
	{
		Section section = createSection(managedForm, title, description, IDENTITY_SECTION_EXPANDED);
		FormToolkit toolkit = managedForm.getToolkit();
		Composite client = createClientComposite(toolkit, section, numColumns);

		Label label = toolkit.createLabel(client, "Geschlecht", SWT.NONE);
		label.setLayoutData(new GridData());

		RadioGroup radioGroup = RadioGroupFormToolkit.createRadioGroup(toolkit, client, SWT.HORIZONTAL);
		radioGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toolkit.adapt(radioGroup, true, false);

		this.sexRadioGroupViewer = new RadioGroupViewer(radioGroup);
		this.sexRadioGroupViewer.setContentProvider(new ArrayContentProvider());
		this.sexRadioGroupViewer.setLabelProvider(new PersonSexLabelProvider());
		this.sexRadioGroupViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			PersonSexQuery query = (PersonSexQuery) service.getQuery(PersonSex.class);
			Collection<PersonSex> sexes = query.selectAll();
			this.sexRadioGroupViewer.setInput(sexes.toArray(new PersonSex[0]));
		}
		tracker.close();
		Control[] controls = this.sexRadioGroupViewer.getRadioGroup().getChildren();
		for (Control control : controls)
		{
			control.setBackground(client.getBackground());
		}
		this.sexRadioGroupViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				PersonSex personSex = (PersonSex) ssel.getFirstElement();
				salutation.setText(personSex == null ? "" : personSex.getSalutation());
				notifyListeners(new PropertyChangeEvent(sexRadioGroupViewer, "sex", null, personSex));
				setDirty(true);
			}
		});

		label = toolkit.createLabel(client, "Anredeform", SWT.NONE);
		label.setLayoutData(new GridData());

		radioGroup = RadioGroupFormToolkit.createRadioGroup(toolkit, client, SWT.HORIZONTAL);
		radioGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		radioGroup.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				setDirty(true);
			}
		});
		toolkit.adapt(radioGroup, true, false);

		this.formRadioGroupViewer = new RadioGroupViewer(radioGroup);
		this.formRadioGroupViewer.setContentProvider(new ArrayContentProvider());
		this.formRadioGroupViewer.setLabelProvider(new PersonFormLabelProvider());
		this.formRadioGroupViewer.setInput(PersonForm.values());
		controls = this.formRadioGroupViewer.getRadioGroup().getChildren();
		for (Control control : controls)
		{
			control.setBackground(client.getBackground());
		}
		this.formRadioGroupViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				PersonForm personForm = (PersonForm) ssel.getFirstElement();
				notifyListeners(new PropertyChangeEvent(formRadioGroupViewer, "form", null, personForm));
				setDirty(true);
			}
		});

		label = toolkit.createLabel(client, "Anrede", SWT.NONE);
		label.setLayoutData(new GridData());

		this.salutation = toolkit.createText(client, "", SWT.SINGLE);
		this.salutation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.salutation.setEnabled(false);

		label = toolkit.createLabel(client, "Titel", SWT.NONE);
		label.setLayoutData(new GridData());

		final CCombo combo = new CCombo(client, SWT.FLAT | SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toolkit.adapt(combo);

		this.titleViewer = new ComboViewer(combo);
		this.titleViewer.setContentProvider(new TitleComboViewerContentProvider());
		this.titleViewer.setLabelProvider(new TitleComboViewerLabelProvider());
		this.titleViewer.setSorter(new PersonTitleSorter());
		this.titleViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.titleViewer.setInput(selectPersonTitles());
		this.titleViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				PersonTitle title = (PersonTitle) ssel.getFirstElement();
				notifyListeners(new PropertyChangeEvent(titleViewer, "title", null, title));
				setDirty(true);
			}
		});

		label = toolkit.createLabel(client, "Vorname", SWT.NONE);
		label.setLayoutData(new GridData());

		this.firstname = toolkit.createText(client, "");
		this.firstname.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.firstname.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				notifyListeners(new PropertyChangeEvent(firstname, "firstname", null, firstname.getText()));
				setDirty(true);
				updateLastnameDecoration();
			}
		});
		this.firstname.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		label = toolkit.createLabel(client, "Nachname", SWT.NONE);
		label.setLayoutData(new GridData());

		this.lastname = toolkit.createText(client, "");
		this.lastname.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.lastname.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				notifyListeners(new PropertyChangeEvent(sexRadioGroupViewer, "lastname", null, lastname.getText()));
				setDirty(true);
				updateLastnameDecoration();
			}
		});
		this.lastname.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		decoration = new ControlDecoration(this.lastname, SWT.LEFT | SWT.TOP);
		Image information = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage();
		decoration.setImage(information);
		decoration.hide();

		toolkit.paintBordersFor(client);
	}

	private void createNotesSection(final IManagedForm managedForm, final String title, final String description,
			final int numColumns)
	{
		notesSection = createSection(managedForm, title, description, NOTE_SECTION_EXPANDED);
		FormToolkit toolkit = managedForm.getToolkit();
		Composite client = createClientComposite(toolkit, notesSection, numColumns);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 128;

		this.notes = toolkit.createText(client, "", SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		this.notes.setLayoutData(gridData);
		this.notes.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
				Image image = ((Text) e.getSource()).getText().isEmpty() ? imageRegistry.get(Activator.KEY_OFF)
						: imageRegistry.get(Activator.KEY_ON);
				notesSelector.setImage(image);
				notesSelector.redraw();
				setDirty(true);
			}
		});
		this.notes.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(text.getText().length(), text.getText().length());
			}
		});

		toolkit.paintBordersFor(client);
	}

	private Section createSection(final IManagedForm managedForm, final String title, final String description,
			final String id)
	{
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();

		int style = 0;
		if (description == null || description.isEmpty())
		{
			style = Section.TWISTIE | Section.TITLE_BAR | Section.EXPANDED;
		}
		else
		{
			style = Section.TWISTIE | Section.TITLE_BAR | Section.DESCRIPTION | Section.EXPANDED;
		}
		Section section = toolkit.createSection(form.getBody(), style);
		section.setText(title);
		if (description != null && !description.isEmpty())
		{
			section.setDescription(description);
		}
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
	public Object getAdapter(final Class clazz)
	{
		if (clazz.equals(LinkPersonAddress.class))
		{
			return this.getEditor().getEditorInput().getAdapter(LinkPersonAddress.class);
		}
		return super.getAdapter(clazz);
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

	private Message getEmptyLastnameMessage()
	{
		Message msg = null;

		if (this.lastname.getText().isEmpty())
		{
			msg = new Message(this.lastname, "Fehler");
			msg.setMessage("Die Person muss einen Nachnamen haben.");
			FormToolkit.ensureVisible(this.lastname);
			this.lastname.setFocus();
		}

		return msg;
	}

	private Message getEmptyPhoneMessage()
	{
		Message msg = null;

		if (this.countryViewer.getSelection().isEmpty() && !this.phone.getText().isEmpty())
		{
			msg = new Message(this.countryViewer.getControl(), "Fehler");
			msg.setMessage("Sie haben die Telefonvorwahl nicht eingegeben.");
			FormToolkit.ensureVisible(this.countryViewer.getControl());
			this.countryViewer.getControl().setFocus();
		}

		return msg;
	}

	public String getFirstname()
	{
		if (firstname == null)
		{
			return getPerson().getFirstname();
		}
		return firstname.getText();
	}

	public String getLastname()
	{
		if (lastname == null)
		{
			return getPerson().getLastname();
		}
		return lastname.getText();
	}

	private String getNumValue(final String value)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < value.length(); i++)
		{
			if ("0123456789".contains(value.substring(i, i + 1)))
			{
				builder.append(value.substring(i, i + 1));
			}
		}
		return builder.toString();
	}

	private Person getPerson()
	{
		PersonEditorInput input = (PersonEditorInput) this.getEditor().getEditorInput();
		return input.getEntity();
	}

	public PersonForm getPersonForm()
	{
		if (formRadioGroupViewer == null)
		{
			return getPerson().getForm() == null ? null : getPerson().getForm();
		}
		StructuredSelection ssel = (StructuredSelection) formRadioGroupViewer.getSelection();
		return (PersonForm) ssel.getFirstElement();
	}

	public PersonSex getPersonSex()
	{
		if (sexRadioGroupViewer == null)
		{
			return getPerson().getSex() == null ? null : getPerson().getSex();
		}
		StructuredSelection ssel = (StructuredSelection) sexRadioGroupViewer.getSelection();
		return (PersonSex) ssel.getFirstElement();
	}

	public PersonTitle getPersonTitle()
	{
		if (titleViewer == null)
		{
			return getPerson().getTitle() == null ? null : getPerson().getTitle();
		}
		StructuredSelection ssel = (StructuredSelection) titleViewer.getSelection();
		return (PersonTitle) ssel.getFirstElement();
	}

	public String getText()
	{
		return PersonFormatter.getInstance().formatFirstnameLastname(getPerson());
	}

	@Override
	public void init(final IEditorSite site, final IEditorInput input)
	{
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

		};
		EntityMediator.addListener(Person.class, entityAdapter);
		EntityMediator.addListener(Domain.class, entityAdapter);
		EntityMediator.addListener(PersonTitle.class, entityAdapter);
		EntityMediator.addListener(LinkPersonAddress.class, entityAdapter);
		EntityMediator.addListener(Address.class, entityAdapter);
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
		return this.dirty;
	}

	private void loadContactsValues(final Person person)
	{
		if (person.getCountry() == null)
		{
			person.setCountry(PersonFormatter.getInstance().getCountry());
		}
		Country country = person.getCountry();
		if (country != null)
		{
			this.countryViewer.setSelection(new StructuredSelection(country));
			String phoneString = person.getPhone();
			if (!phoneString.isEmpty())
			{
				try
				{
					PhoneNumber phoneNumber = phoneUtil.parse(phoneString, country.getIso3166alpha2());
					phoneString = phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL);
					this.phone.setText(phoneString);
				}
				catch (NumberParseException e)
				{
					e.printStackTrace();
				}
			}

		}
		this.email.setText(person.getEmail());
		this.website.setText(person.getWebsite());
	}

	private void loadFurtherValues(final Person person)
	{
		Long birthdate = person.getBirthdate();
		if (birthdate != null)
		{
			long birth = birthdate.longValue();
			int year = GregorianCalendar.getInstance().get(Calendar.YEAR);
			if (birth < 1900 || year < birth)
			{
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(birth);
				this.birthdate.setSelection(calendar.getTime());
				this.birthyear.setSelection(calendar.getTime());
			}
			else
			{
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.DATE, 1);
				calendar.set(Calendar.MONTH, 0);
				calendar.set(Calendar.YEAR, (int) birth);
				this.birthdate.setSelection(null);
				this.birthyear.setSelection(calendar.getTime());
			}
		}
		else
		{
			this.birthdate.setSelection(null);
			this.birthyear.setSelection(null);
		}
		this.profession.setText(person.getProfession());
		if (PersonSettings.getInstance().getPersonHasDomain())
		{
			if (person.getDomain() != null)
				this.domainViewer.setSelection(new StructuredSelection(person.getDomain()));
			else if (PersonSettings.getInstance().isPersonDomainMandatory())
			{
				ConnectionService service = (ConnectionService) this.domainViewer.getInput();
				DomainQuery query = (DomainQuery) service.getQuery(Domain.class);
				List<Domain> domains = query.selectAll();
				if (domains.size() > 0)
				{
					this.domainViewer.setSelection(new StructuredSelection(domains.iterator().next()));
				}
			}
		}

		this.notes.setText(person.getNotes().replace('\r', '\n'));
	}

	private void loadIdentityValues(final Person person)
	{
		if (person.getSex() == null)
		{
			PersonSex[] sexes = (PersonSex[]) this.sexRadioGroupViewer.getInput();
			if (sexes.length > 0)
			{
				this.sexRadioGroupViewer.setSelection(new StructuredSelection(sexes[0]));
				person.setSex(sexes[0]);
			}
		}
		else
		{
			this.sexRadioGroupViewer.setSelection(new StructuredSelection(person.getSex()));
		}
		this.formRadioGroupViewer.setSelection(new StructuredSelection(person.getForm()));
		this.salutation.setText(person.getSex() == null ? "" : person.getSex().getSalutation());
		if (person.getTitle() != null)
		{
			this.titleViewer.setSelection(new StructuredSelection(person.getTitle()));
		}
		this.firstname.setText(person.getFirstname());
		this.lastname.setText(person.getLastname());
	}

	public void loadValues()
	{
		Person person = getPerson();
		this.loadIdentityValues(person);
		this.loadContactsValues(person);
		this.loadFurtherValues(person);
		this.loadExtendedFieldValues();
		this.setDirty(false);
	}

	private void saveExtendedFieldValues(final Person person)
	{
		for (ExtendedField field : extendedFields.values())
		{
			Control control = extendedFieldControls.get(field.getFieldExtension().getId());
			field.setValue(field.getFieldExtension().getType().getInput(control));
			if (field.getId() == null && !field.getValue().isEmpty())
			{
				if (field.getFieldExtension().getTarget().equals(FieldExtensionTarget.PERSON))
				{
					person.addExtendedFields((PersonExtendedField) field);
				}
			}
		}
	}

	public void notifyListeners(final PropertyChangeEvent event)
	{
		IPropertyChangeListener[] ls = this.listeners.toArray(new IPropertyChangeListener[0]);
		for (IPropertyChangeListener l : ls)
		{
			l.propertyChange(event);
		}
	}

	public void removeListener(final IPropertyChangeListener listener)
	{
		this.listeners.remove(listener);
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

	private void saveContactsValues(final Person person)
	{
		StructuredSelection ssel = (StructuredSelection) countryViewer.getSelection();
		person.setCountry((Country) ssel.getFirstElement());
		person.setPhone(removeSpaces(this.phone.getText()));
		person.setEmail(this.email.getText());
		person.setWebsite(this.website.getText());
	}

	private void saveFurtherValues(final Person person)
	{
		if (this.birthdate.getSelection() == null)
		{
			if (this.birthyear.getSelection() == null)
			{
				person.setBirthdate(null);
			}
			else
			{
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(this.birthyear.getSelection());
				person.setBirthdate(Long.valueOf(calendar.get(Calendar.YEAR)));
			}
		}
		else
		{
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(this.birthdate.getSelection());
			person.setBirthdate(Long.valueOf(calendar.getTimeInMillis()));
		}
		person.setProfession(this.profession.getText());
		if (PersonSettings.getInstance().getPersonHasDomain())
		{
			if (this.domainViewer != null)
			{
				StructuredSelection ssel = (StructuredSelection) this.domainViewer.getSelection();
				if (ssel.isEmpty())
				{
					person.setDomain(null);
				}
				else if (((Domain) ssel.getFirstElement()).getId() == null)
				{
					person.setDomain(null);
				}
				else
				{
					person.setDomain((Domain) ssel.getFirstElement());
				}
			}
		}

		person.setNotes(this.notes.getText().isEmpty() ? null : this.notes.getText());
	}

	private void saveIdentityValues(final Person person)
	{
		StructuredSelection ssel = (StructuredSelection) this.sexRadioGroupViewer.getSelection();
		PersonSex personSex = (PersonSex) ssel.getFirstElement();
		person.setSex(personSex);
		ssel = (StructuredSelection) this.formRadioGroupViewer.getSelection();
		PersonForm personForm = (PersonForm) ssel.getFirstElement();
		person.setForm(personForm);
		ssel = (StructuredSelection) this.titleViewer.getSelection();
		PersonTitle title = (PersonTitle) ssel.getFirstElement();
		person.setTitle(title == null ? null : title.getId() == null ? null : title);
		person.setFirstname(this.firstname.getText());
		person.setLastname(this.lastname.getText());
	}

	public void saveValues()
	{
		Person person = getPerson();
		this.saveIdentityValues(person);
		this.saveContactsValues(person);
		this.saveFurtherValues(person);
		saveExtendedFieldValues(person);
		this.setDirty(false);
	}

	private Domain[] selectDomains()
	{
		List<Domain> domains = null;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();

		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			DomainQuery query = (DomainQuery) service.getQuery(Domain.class);
			domains = query.selectAll();
		}
		tracker.close();
		return domains == null ? new Domain[0] : domains.toArray(new Domain[0]);
	}

	private PersonTitle[] selectPersonTitles()
	{
		Collection<PersonTitle> titles = null;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			PersonTitleQuery query = (PersonTitleQuery) service.getQuery(PersonTitle.class);
			titles = query.selectAll(false);
			titles.add(PersonTitle.newInstance());
		}
		tracker.close();
		return titles == null ? new PersonTitle[0] : titles.toArray(new PersonTitle[0]);
	}

	private Country[] selectPrefixes()
	{
		Collection<Country> prefixes = null;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			CountryQuery query = (CountryQuery) service.getQuery(Country.class);
			prefixes = query.selectPrefixes();
		}
		tracker.close();
		return prefixes == null ? new Country[0] : prefixes.toArray(new Country[0]);
	}

	@Override
	public void setFocus()
	{
		this.firstname.setFocus();
	}

	public void setFocus(final Control control)
	{
		control.setFocus();
	}

	protected int showMessage(final String title, final Image image, final String message, final int dialogType,
			final String[] buttonLabels, final int defaultButton)
	{
		MessageDialog dialog = new MessageDialog(this.getEditor().getEditorSite().getShell(), title, image, message,
				dialogType, buttonLabels, defaultButton);
		dialog.setBlockOnOpen(true);
		return dialog.open();
	}

	protected int showWarningMessage(final Message msg)
	{
		int result = this.showMessage(msg.getTitle(), Dialog.getImage(Dialog.DLG_IMG_MESSAGE_WARNING),
				msg.getMessage(), MessageDialog.WARNING, new String[] { "OK" }, 0);
		this.setFocus(msg.getControl());
		return result;
	}

	private void updateLastnameDecoration()
	{
		if (decoration != null)
		{
			long count = 0L;
			ConnectionService service = Activator.getDefault().getConnectionService();
			if (service != null)
			{
				if (this.lastname.getText().length() > 3
						&& this.lastname.getText().length() + this.firstname.getText().length() > 5)
				{
					Person person = getPerson();
					PersonQuery query = (PersonQuery) service.getQuery(Person.class);
					count = query.countByFirstnameAndLastnameAsLike(this.firstname.getText(), this.lastname.getText(),
							person.getId());
				}
			}
			if (count == 0L)
			{
				decoration.hide();
			}
			else
			{
				decoration.setDescriptionText(count + " Personen mit gleichem Namen vorhanden.");
				decoration.show();
			}
		}
	}

	public boolean validate()
	{
		Message msg = null;

		if (msg == null)
		{
			msg = this.getEmptyLastnameMessage();
		}

		if (msg == null)
		{
			msg = this.getEmptyPhoneMessage();
		}

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	private void initializeFields(Map<String, String> values)
	{
		Set<Entry<String, String>> entries = values.entrySet();
		for (Entry<String, String> entry : entries)
		{
			if (entry.getKey().equals("lastname"))
			{
				this.lastname.setText(entry.getValue());
			}
			else if (entry.getKey().equals("firstname"))
			{
				this.firstname.setText(entry.getValue());
			}
		}
		this.setDirty(values.size() > 0);
	}

	public void setDirty(boolean dirty)
	{
		this.dirty = dirty;
		this.firePropertyChange(PROP_DIRTY);
	}
}
