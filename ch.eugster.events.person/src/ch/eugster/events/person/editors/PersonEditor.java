package ch.eugster.events.person.editors;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
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
import org.eclipse.nebula.jface.viewer.radiogroup.RadioGroupViewer;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.MaskFormatter;
import org.eclipse.nebula.widgets.radiogroup.RadioGroup;
import org.eclipse.nebula.widgets.radiogroup.forms.RadioGroupFormToolkit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.ExtendedField;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.FieldExtensionTarget;
import ch.eugster.events.persistence.model.FieldExtensionType;
import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.LinkPersonAddressExtendedField;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonExtendedField;
import ch.eugster.events.persistence.model.PersonForm;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.model.PersonSex;
import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.queries.FieldExtensionQuery;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.persistence.queries.PersonSexQuery;
import ch.eugster.events.persistence.queries.PersonTitleQuery;
import ch.eugster.events.persistence.queries.ZipCodeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.preferences.PreferenceInitializer;
import ch.eugster.events.person.views.PersonEditorOutlinePage;
import ch.eugster.events.person.views.PersonTitleSorter;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;
import ch.eugster.events.ui.helpers.BrowseHelper;
import ch.eugster.events.ui.helpers.EmailHelper;

public class PersonEditor extends AbstractEntityEditor<Address> implements PropertyChangeListener,
		IContentProposalListener
{
	public static final String ID = "ch.eugster.events.person.editors.simplePersonEditor";

	private static final String EMAIL_LABEL = "Email (senden)";

	private static final String EMAIL_LINK = "Email (<a>senden</a>)";

	private static final String WEBSITE_LABEL = "Webseite (öffnen)";

	private static final String WEBSITE_LINK = "Webseite (<a>öffnen</a>)";

	private static final String CONTACT_SECTION_EXPANDED = "contacts.section.expanded";

	private static final String ADDRESS_SECTION_EXPANDED = "address.section.expanded";

	private static final String FURTHER_SECTION_EXPANDED = "further.section.expanded";

	private static final String NOTE_SECTION_EXPANDED = "note.section.expanded";

	private static final String LABEL_SECTION_EXPANDED = "label.section.expanded";

	private final Collection<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();

	private EntityAdapter entityAdapter;

	private IDialogSettings dialogSettings;

	private RadioGroupViewer sexRadioGroupViewer;

	private RadioGroupViewer formRadioGroupViewer;

	private Text salutation;

	private ComboViewer titleViewer;

	private Text firstname;

	private ControlDecoration decoration;

	private Text lastname;

	// private ImageHyperlink notesSelector;

	private CDateTime birthdate;

	private Text birthyear;

	private Text profession;

	private Text notes;

	private ComboViewer domainViewer;

	private ComboViewer mobilePrefixViewer;

	private FormattedText mobile;

	private Link sendPersonalEmail;

	private Text personalEmail;

	private Link browsePersonalWebsite;

	private Text personalWebsite;

	private Link browseBusinessWebsite;

	private Text businessWebsite;

	private Text addressLabel;

	private Text salutationLabel;

	private Text businessEmail;

	private Link sendBusinessEmail;

	private Text privatePrefix;

	private Text businessPrefix;

	private Text faxPrefix;

	private FormattedText businessPhone;

	private FormattedText privatePhone;

	private FormattedText fax;

	private Text organisation;

	private Text anotherLine;

	private Text address;

	private Text pob;

	private Text zip;

	private Text city;

	private ComboViewer addressCountryViewer;

	private ComboViewer provinceViewer;

	private Section addressSection;

	private Section contactSection;

	private Section furtherSection;

	private Section noteSection;

	private Section labelSection;

	private Map<Long, ExtendedField> extendedFields = new HashMap<Long, ExtendedField>();

	private Map<Long, Control> extendedFieldControls = new HashMap<Long, Control>();

	public PersonEditor()
	{
		super();
	}

	private void addExtendedFields(final Composite parent)
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
				Collection<FieldExtension> extensions = query.selectAll(false);
				if (extensions.size() > 0)
				{
					LinkPersonAddress link = (LinkPersonAddress) this.getEditorInput().getAdapter(
							LinkPersonAddress.class);
					for (LinkPersonAddressExtendedField field : link.getExtendedFields())
					{
						this.extendedFields.put(field.getFieldExtension().getId(), field);
					}
					for (PersonExtendedField field : link.getPerson().getExtendedFields())
					{
						this.extendedFields.put(field.getFieldExtension().getId(), field);
					}
					for (FieldExtension extension : extensions)
					{
						ExtendedField field = this.extendedFields.get(extension.getId());
						if (field == null)
						{
							if (extension.getTarget().equals(FieldExtensionTarget.PA_LINK))
							{
								field = LinkPersonAddressExtendedField.newInstance(link, extension);
								field.setValue(extension.getDefaultValue());
								extendedFields.put(field.getFieldExtension().getId(), field);
							}
							else if (extension.getTarget().equals(FieldExtensionTarget.PERSON))
							{
								field = PersonExtendedField.newInstance(link.getPerson(), extension);
								field.setValue(extension.getDefaultValue());
								extendedFields.put(field.getFieldExtension().getId(), field);
							}
						}
						Label label = formToolkit.createLabel(parent, extension.getLabel());
						label.setLayoutData(new GridData());

						GridData gridData = extension.getWidthHint() == 0 ? new GridData(GridData.FILL_HORIZONTAL)
								: new GridData();
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
							Text text = formToolkit.createText(parent, "");
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

	private void createAddressSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.addressSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.addressSection.setLayoutData(layoutData);
		this.addressSection.setLayout(sectionLayout);
		this.addressSection.setText("Adresse");
		this.addressSection.setClient(this.fillAddressSection(this.addressSection));
		this.addressSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				PersonEditor.this.dialogSettings.put(PersonEditor.ADDRESS_SECTION_EXPANDED, e.getState());
				PersonEditor.this.scrolledForm.reflow(true);
			}
		});
		this.addressSection.setExpanded(this.dialogSettings.getBoolean(PersonEditor.ADDRESS_SECTION_EXPANDED));
	}

	private void createContactSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.contactSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.contactSection.setLayoutData(layoutData);
		this.contactSection.setLayout(sectionLayout);
		this.contactSection.setText("Kontakt");
		this.contactSection.setClient(this.fillContactSection(this.contactSection));
		this.contactSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				PersonEditor.this.dialogSettings.put(PersonEditor.CONTACT_SECTION_EXPANDED, e.getState());
				PersonEditor.this.scrolledForm.reflow(true);
			}
		});
		this.contactSection.setExpanded(this.dialogSettings.getBoolean(PersonEditor.CONTACT_SECTION_EXPANDED));
	}

	private void createFurtherSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.furtherSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.furtherSection.setLayoutData(layoutData);
		this.furtherSection.setLayout(sectionLayout);
		this.furtherSection.setText("Weitere Angaben");
		this.furtherSection.setClient(this.fillFurtherSection(this.furtherSection));
		this.furtherSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				PersonEditor.this.dialogSettings.put(PersonEditor.FURTHER_SECTION_EXPANDED, e.getState());
				PersonEditor.this.scrolledForm.reflow(true);
			}
		});
		this.furtherSection.setExpanded(this.dialogSettings.getBoolean(PersonEditor.FURTHER_SECTION_EXPANDED));
	}

	private void createLabelSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.labelSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.labelSection.setLayoutData(layoutData);
		this.labelSection.setLayout(sectionLayout);
		this.labelSection.setText("Vorschau");
		this.labelSection.setClient(this.fillLabelSection(this.labelSection));
		this.labelSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				PersonEditor.this.dialogSettings.put(PersonEditor.LABEL_SECTION_EXPANDED, e.getState());
				PersonEditor.this.scrolledForm.reflow(true);
			}
		});
		this.labelSection.setExpanded(this.dialogSettings.getBoolean(PersonEditor.LABEL_SECTION_EXPANDED));
	}

	private void createNoteSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.noteSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.noteSection.setLayoutData(layoutData);
		this.noteSection.setLayout(sectionLayout);
		this.noteSection.setText("Bemerkungen");
		this.noteSection.setClient(this.fillNoteSection(this.noteSection));
		this.noteSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				PersonEditor.this.dialogSettings.put(PersonEditor.NOTE_SECTION_EXPANDED, e.getState());
				PersonEditor.this.scrolledForm.reflow(true);
			}
		});
		this.noteSection.setExpanded(this.dialogSettings.getBoolean(PersonEditor.NOTE_SECTION_EXPANDED));
	}

	@Override
	protected void createSections(final ScrolledForm parent)
	{
		createAddressSection(parent);
		createContactSection(parent);
		createFurtherSection(parent);
		createNoteSection(parent);
		createLabelSection(parent);
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Course.class, this);
		super.dispose();
	}

	private Composite fillAddressSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(5, false));

		Label label = this.formToolkit.createLabel(composite, "Geschlecht", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		RadioGroup radioGroup = RadioGroupFormToolkit.createRadioGroup(this.formToolkit, composite, SWT.HORIZONTAL);
		radioGroup.setLayoutData(gridData);
		this.formToolkit.adapt(radioGroup, true, false);

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
			control.setBackground(composite.getBackground());
		}

		this.sexRadioGroupViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				setDirty(true);
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				PersonSex personSex = (PersonSex) ssel.getFirstElement();
				salutation.setText(personSex == null ? "" : personSex.getSalutation());
				updateSingleLabel();
				notifyListeners(new PropertyChangeEvent(sexRadioGroupViewer, "sex", null, personSex));
			}
		});

		label = this.formToolkit.createLabel(composite, "Anredeform", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		radioGroup = RadioGroupFormToolkit.createRadioGroup(this.formToolkit, composite, SWT.HORIZONTAL);
		radioGroup.setLayoutData(gridData);
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
				updateSingleLabel();
				setDirty(true);
			}
		});
		this.formToolkit.adapt(radioGroup, true, false);

		this.formRadioGroupViewer = new RadioGroupViewer(radioGroup);
		this.formRadioGroupViewer.setContentProvider(new ArrayContentProvider());
		this.formRadioGroupViewer.setLabelProvider(new PersonFormLabelProvider());
		this.formRadioGroupViewer.setInput(PersonForm.values());
		controls = this.formRadioGroupViewer.getRadioGroup().getChildren();
		for (Control control : controls)
		{
			control.setBackground(composite.getBackground());
		}

		label = this.formToolkit.createLabel(composite, "Anrede", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		this.salutation = this.formToolkit.createText(composite, "", SWT.SINGLE);
		this.salutation.setLayoutData(gridData);
		this.salutation.setEnabled(false);

		label = this.formToolkit.createLabel(composite, "Titel", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		CCombo combo = new CCombo(composite, SWT.FLAT | SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(gridData);
		this.formToolkit.adapt(combo);

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
				updateSingleLabel();
				notifyListeners(new PropertyChangeEvent(titleViewer, "title", null, title));
				setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Vorname", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		this.firstname = this.formToolkit.createText(composite, "");
		this.firstname.setLayoutData(gridData);
		this.firstname.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateSingleLabel();
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

		label = this.formToolkit.createLabel(composite, "Nachname", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		this.lastname = this.formToolkit.createText(composite, "");
		this.lastname.setLayoutData(gridData);
		this.lastname.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateSingleLabel();
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

		label = formToolkit.createLabel(composite, "Organisation", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		this.organisation = formToolkit.createText(composite, "");
		this.organisation.setLayoutData(gridData);
		this.organisation.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateSingleLabel();
				setDirty(true);
			}
		});
		this.organisation.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		label = formToolkit.createLabel(composite, "Zusatz", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		this.anotherLine = formToolkit.createText(composite, "");
		this.anotherLine.setLayoutData(gridData);
		this.anotherLine.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateSingleLabel();
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

		label = formToolkit.createLabel(composite, "Strasse", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		this.address = formToolkit.createText(composite, "");
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
			}

		});
		this.address.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateSingleLabel();
				setDirty(true);
			}
		});

		label = formToolkit.createLabel(composite, "Postfach", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		this.pob = formToolkit.createText(composite, "");
		this.pob.setLayoutData(gridData);
		this.pob.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateSingleLabel();
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

		label = formToolkit.createLabel(composite, "Land PLZ Ort Kanton", SWT.NONE);
		label.setLayoutData(new GridData());

		combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(new GridData());
		formToolkit.adapt(combo);

		this.addressCountryViewer = new ComboViewer(combo);
		this.addressCountryViewer.setContentProvider(new ArrayContentProvider());
		this.addressCountryViewer.setLabelProvider(new CountryIso3166Alpha2LabelProvider());
		this.addressCountryViewer.setSorter(new CountryIso3166Alpha2Sorter());
		this.addressCountryViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.addressCountryViewer.setInput(selectCountries());
		this.addressCountryViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				updateSingleLabel();
				setDirty(true);
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				if (ssel.getFirstElement() instanceof Country)
				{
					Country country = (Country) ssel.getFirstElement();
					addressCountryViewer.setData("country", country);
					// linkPhonePrefix.setText(country.getPhonePrefix());
					privatePrefix.setText(country.getPhonePrefix());
					businessPrefix.setText(country.getPhonePrefix());
					faxPrefix.setText(country.getPhonePrefix());
					if (country.getPhonePattern().isEmpty())
					{
						privatePhone.setFormatter(null);
						businessPhone.setFormatter(null);
						fax.setFormatter(null);
					}
					else
					{
						privatePhone.setFormatter(new MaskFormatter(country.getPhonePattern()));
						businessPhone.setFormatter(new MaskFormatter(country.getPhonePattern()));
						fax.setFormatter(new MaskFormatter(country.getPhonePattern()));
					}
					String[] states = selectProvinceCodes(country);
					provinceViewer.setInput(states);
					if (zip.getData("zipCode") instanceof ZipCode)
					{
						ZipCode zipCode = (ZipCode) zip.getData("zipCode");
						provinceViewer.setSelection(new StructuredSelection(zipCode.getState()));
					}
				}
			}
		});

		gridData = new GridData();
		gridData.widthHint = 48;

		this.zip = formToolkit.createText(composite, "");
		this.zip.setLayoutData(gridData);
		this.zip.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				zipSelected(zip.getText());
				updateSingleLabel();
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
				// zipSelected(zip.getText());
				// updateSingleLabel();
			}
		});

		// label = this.formToolkit.createLabel(composite, "Ort", SWT.NONE);
		// label.setLayoutData(new GridData());

		this.city = formToolkit.createText(composite, "");
		this.city.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.city.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
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
		try
		{
			char[] autoActivationCharacters = new char[] { '.', '#' };
			KeyStroke keyStroke = KeyStroke.getInstance("Ctrl+Space");

			ContentProposalAdapter proposalAdapter = new ContentProposalAdapter(this.city, new TextContentAdapter(),
					new CityContentProposalProvider(addressCountryViewer, zip), keyStroke, autoActivationCharacters);
			proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			proposalAdapter.addContentProposalListener(this);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

		// label = this.formToolkit.createLabel(composite, "Kanton", SWT.NONE);
		// label.setLayoutData(new GridData());

		gridData = new GridData();
		// gridData.horizontalSpan = 3;

		combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(gridData);
		formToolkit.adapt(combo);

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

		formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Composite fillContactSection(final Section parent)
	{
		final Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(3, false));

		Label label = this.formToolkit.createLabel(composite, "Mobiltelefon", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData();
		gridData.widthHint = 48;

		CCombo combo = new CCombo(composite, SWT.FLAT | SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(gridData);
		this.formToolkit.adapt(combo);

		this.mobilePrefixViewer = new ComboViewer(combo);
		this.mobilePrefixViewer.setContentProvider(new ArrayContentProvider());
		this.mobilePrefixViewer.setLabelProvider(new CountryPrefixLabelProvider());
		this.mobilePrefixViewer.setSorter(new CountryPrefixSorter());
		this.mobilePrefixViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter(), new CountryPrefixFilter() });
		this.mobilePrefixViewer.setInput(selectPrefixes());
		this.mobilePrefixViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				if (ssel.getFirstElement() instanceof Country)
				{
					Country country = (Country) ssel.getFirstElement();
					String numValue = getNumValue(mobile.getControl().getText());
					mobile.setFormatter(new MaskFormatter(country.getPhonePattern()));
					mobile.setValue(numValue);
				}
				setDirty(true);
				mobile.getControl().setFocus();
			}
		});

		Text phone = this.formToolkit.createText(composite, "");
		phone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		phone.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
			}
		});
		phone.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.mobile = new FormattedText(phone);

		label = this.formToolkit.createLabel(composite, "Telefon Privat", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 48;

		privatePrefix = formToolkit.createText(composite, "");
		privatePrefix.setLayoutData(gridData);
		privatePrefix.setEnabled(false);

		phone = this.formToolkit.createText(composite, "");
		phone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		phone.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
			}
		});
		phone.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.privatePhone = new FormattedText(phone);

		label = this.formToolkit.createLabel(composite, "Telefon Arbeit", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 48;

		businessPrefix = formToolkit.createText(composite, "");
		businessPrefix.setLayoutData(gridData);
		businessPrefix.setEnabled(false);

		phone = this.formToolkit.createText(composite, "");
		phone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		phone.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
			}
		});
		phone.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.businessPhone = new FormattedText(phone);

		label = this.formToolkit.createLabel(composite, "Fax", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 48;

		faxPrefix = formToolkit.createText(composite, "");
		faxPrefix.setLayoutData(gridData);
		faxPrefix.setEnabled(false);

		phone = this.formToolkit.createText(composite, "");
		phone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		phone.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
			}
		});
		phone.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.fax = new FormattedText(phone);

		this.sendPersonalEmail = new Link(composite, SWT.NONE);
		this.sendPersonalEmail.setText(PersonEditor.EMAIL_LABEL);
		this.sendPersonalEmail.setToolTipText("Um ein Email zu senden, klicken Sie hier");
		this.sendPersonalEmail.setLayoutData(new GridData());
		this.sendPersonalEmail.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				EmailHelper.getInstance().sendEmail(PersonEditor.this.personalEmail.getText());
			}
		});

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.personalEmail = this.formToolkit.createText(composite, "");
		this.personalEmail.setLayoutData(gridData);
		this.personalEmail.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
				boolean enabled = EmailHelper.getInstance().isEmailSupported()
						&& EmailHelper.getInstance().isValidAddress(PersonEditor.this.personalEmail.getText());
				if (enabled)
				{
					PersonEditor.this.sendPersonalEmail.setText(PersonEditor.EMAIL_LINK);
					composite.layout(true, true);
				}
				else
					PersonEditor.this.sendPersonalEmail.setText(PersonEditor.EMAIL_LABEL);
			}
		});
		this.personalEmail.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.sendBusinessEmail = new Link(composite, SWT.NONE);
		this.sendBusinessEmail.setText(PersonEditor.EMAIL_LABEL);
		this.sendBusinessEmail.setToolTipText("Um ein Email zu senden, klicken Sie hier");
		this.sendBusinessEmail.setLayoutData(new GridData());
		this.sendBusinessEmail.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				EmailHelper.getInstance().sendEmail(PersonEditor.this.personalEmail.getText());
			}
		});

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.businessEmail = this.formToolkit.createText(composite, "");
		this.businessEmail.setLayoutData(gridData);
		this.businessEmail.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
				boolean enabled = EmailHelper.getInstance().isEmailSupported()
						&& EmailHelper.getInstance().isValidAddress(PersonEditor.this.personalEmail.getText());
				if (enabled)
				{
					PersonEditor.this.sendBusinessEmail.setText(PersonEditor.EMAIL_LINK);
					composite.layout(true, true);
				}
				else
					PersonEditor.this.sendBusinessEmail.setText(PersonEditor.EMAIL_LABEL);
			}
		});
		this.businessEmail.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.browsePersonalWebsite = new Link(composite, SWT.NONE);
		this.browsePersonalWebsite.setText(PersonEditor.WEBSITE_LABEL);
		this.browsePersonalWebsite.setLayoutData(new GridData());
		this.browsePersonalWebsite.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				BrowseHelper.getInstance().browse(PersonEditor.this.personalWebsite.getText());
			}
		});
		GC gc = new GC(this.browsePersonalWebsite);
		FontMetrics fm = gc.getFontMetrics();
		int width = PersonEditor.WEBSITE_LINK.length() * fm.getAverageCharWidth();
		int height = fm.getHeight();
		this.browsePersonalWebsite.setSize(this.browsePersonalWebsite.computeSize(width, height));

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.personalWebsite = this.formToolkit.createText(composite, "");
		this.personalWebsite.setLayoutData(gridData);
		this.personalWebsite.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
				boolean enabled = BrowseHelper.getInstance().isBrowsingSupported()
						&& BrowseHelper.getInstance().isValidAddress(PersonEditor.this.personalWebsite.getText());
				if (enabled)
				{
					PersonEditor.this.browsePersonalWebsite.setText(PersonEditor.WEBSITE_LINK);
				}
				else
				{
					PersonEditor.this.browsePersonalWebsite.setText(PersonEditor.WEBSITE_LABEL);
				}
				composite.layout(true, true);
			}
		});
		this.personalWebsite.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.browseBusinessWebsite = new Link(composite, SWT.NONE);
		this.browseBusinessWebsite.setText(PersonEditor.WEBSITE_LABEL);
		this.browseBusinessWebsite.setLayoutData(new GridData());
		this.browseBusinessWebsite.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				BrowseHelper.getInstance().browse(PersonEditor.this.businessWebsite.getText());
			}
		});
		width = PersonEditor.WEBSITE_LINK.length() * fm.getAverageCharWidth();
		height = fm.getHeight();
		gc.dispose();
		this.browseBusinessWebsite.setSize(this.browseBusinessWebsite.computeSize(width, height));

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.businessWebsite = this.formToolkit.createText(composite, "");
		this.businessWebsite.setLayoutData(gridData);
		this.businessWebsite.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
				boolean enabled = BrowseHelper.getInstance().isBrowsingSupported()
						&& BrowseHelper.getInstance().isValidAddress(PersonEditor.this.businessWebsite.getText());
				if (enabled)
				{
					PersonEditor.this.browseBusinessWebsite.setText(PersonEditor.WEBSITE_LINK);
				}
				else
				{
					PersonEditor.this.browseBusinessWebsite.setText(PersonEditor.WEBSITE_LABEL);
				}
				composite.layout(true, true);
			}
		});
		this.businessWebsite.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Composite fillFurtherSection(final Section parent)
	{
		final Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = formToolkit.createLabel(composite, "Geburtsdatum", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData();
		gridData.widthHint = 84;

		this.birthdate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		this.birthdate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.birthdate.setLayoutData(gridData);
		this.birthdate.setNullText("");
		this.birthdate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				setDirty(true);
				Date date = birthdate.getSelection();
				if (date == null)
				{
					birthyear.setText("");
				}
				else
				{
					Calendar calendar = GregorianCalendar.getInstance();
					calendar.setTime(date);
					birthyear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
				}
			}
		});
		formToolkit.adapt(this.birthdate);

		label = formToolkit.createLabel(composite, "oder -jahr", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 64;

		birthyear = this.formToolkit.createText(composite, "");
		birthyear.setLayoutData(gridData);
		birthyear.setLayoutData(gridData);
		birthyear.addVerifyListener(new VerifyListener()
		{
			@Override
			public void verifyText(final VerifyEvent evt)
			{
				evt.doit = (evt.text.matches("^\\d+$") || (evt.character == '\b') || (evt.character == '\000'));
			}
		});
		birthyear.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
			}
		});

		label = formToolkit.createLabel(composite, "Beruf", SWT.NONE);
		label.setLayoutData(new GridData());

		this.profession = formToolkit.createText(composite, "");
		this.profession.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
			label = formToolkit.createLabel(composite, "Domäne");
			label.setLayoutData(new GridData());

			CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
			combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
			combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			formToolkit.adapt(combo);

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

		addExtendedFields(composite);

		formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Composite fillLabelSection(final Section parent)
	{
		final Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout());

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 96;
		// gridData.widthHint = 128;

		this.addressLabel = formToolkit.createText(composite, "", SWT.MULTI | SWT.WRAP);
		this.addressLabel.setLayoutData(gridData);
		this.addressLabel.setEditable(false);

		this.salutationLabel = formToolkit.createText(composite, "");
		this.salutationLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.salutationLabel.setEditable(false);

		formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Composite fillNoteSection(final Section parent)
	{
		final Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(3, false));

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 128;

		this.notes = formToolkit.createText(composite, "", SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		this.notes.setLayoutData(gridData);
		this.notes.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				// ImageRegistry imageRegistry =
				// Activator.getDefault().getImageRegistry();
				// Image image = ((Text) e.getSource()).getText().isEmpty() ?
				// imageRegistry.get(Activator.KEY_OFF)
				// : imageRegistry.get(Activator.KEY_ON);
				// notesSelector.setImage(image);
				// notesSelector.redraw();
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

		formToolkit.paintBordersFor(composite);

		return composite;
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

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Class adapter)
	{
		if (IContentOutlinePage.class.equals(adapter))
		{
			if (this.contentOutlinePage == null)
				this.contentOutlinePage = new PersonEditorOutlinePage(this);
			return this.contentOutlinePage;
		}
		return null;
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

		if (this.addressCountryViewer.getSelection().isEmpty())
		{
			msg = new Message(this.city, "Fehler");
			msg.setMessage("Der Ländercode fehlt.");
			FormToolkit.ensureVisible(this.addressCountryViewer.getControl());
			this.addressCountryViewer.getControl().setFocus();
		}

		return msg;
	}

	private Message getEmptyZipMessage()
	{
		Message msg = null;

		if (this.zip.getText().isEmpty())
		{
			msg = new Message(this.zip, "Fehler");
			msg.setMessage("Sie haben keine Postleitzahl eingegeben.");
			FormToolkit.ensureVisible(this.zip);
			this.zip.setFocus();
		}

		return msg;
	}

	protected Message getMessage()
	{
		Message msg = null;

		return msg;
	}

	@Override
	protected Message getMessage(final PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		return msg;
	}

	@Override
	protected String getName()
	{
		LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) this.getEditorInput();
		LinkPersonAddress link = (LinkPersonAddress) input.getAdapter(LinkPersonAddress.class);
		return link.getId() == null ? "Neu" : "A" + PersonFormatter.getInstance().formatId(link.getPerson());
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

	@Override
	protected String getText()
	{
		LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) this.getEditorInput();
		LinkPersonAddress link = (LinkPersonAddress) input.getAdapter(LinkPersonAddress.class);
		return link.getId() == null ? "Neue Person" : "Person "
				+ PersonFormatter.getInstance().formatId(link.getPerson());
	}

	@Override
	protected void initialize()
	{
		String value = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceInitializer.KEY_EDITOR_SECTION_BEHAVIOUR);

		Long id = ((LinkPersonAddressEditorInput) this.getEditorInput()).getEntity().getId();
		if (id == null || value.equals(PreferenceInitializer.EDITOR_SECTION_BEHAVIOUR_EDITOR))
		{
			id = Long.valueOf(0L);
		}
		this.initializeDialogSettings(id == null ? ID : ID + "." + id);

		entityAdapter = new EntityAdapter()
		{
		};
		EntityMediator.addListener(Address.class, entityAdapter);
	}

	private void initializeDialogSettings(final String section)
	{
		this.dialogSettings = Activator.getDefault().getDialogSettings().getSection(section);
		if (this.dialogSettings == null)
			this.dialogSettings = Activator.getDefault().getDialogSettings().addNewSection(section);
	}

	private void loadContactValues(final LinkPersonAddress link)
	{
		Country country = link.getPerson().getCountry();
		if (country != null)
		{
			StructuredSelection ssel = new StructuredSelection(new Country[] { country });
			this.mobilePrefixViewer.setSelection(ssel);
		}

		this.mobile.setValue(link.getPerson().getPhone());
		this.privatePrefix.setText(link.getAddress().getCountry().getPhonePrefix());
		this.privatePhone.setValue(link.getPhone());
		this.businessPrefix.setText(link.getAddress().getCountry().getPhonePrefix());
		this.businessPhone.setValue(link.getAddress().getPhone());
		this.faxPrefix.setText(link.getAddress().getCountry().getPhonePrefix());
		this.fax.setValue(link.getAddress().getFax());
		this.personalEmail.setText(link.getPerson().getEmail());
		this.businessEmail.setText(link.getEmail());
		this.personalWebsite.setText(link.getPerson().getWebsite());
		this.businessWebsite.setText(link.getAddress().getWebsite());
	}

	private void loadExtendedFieldValues()
	{
		for (ExtendedField field : extendedFields.values())
		{
			Control control = extendedFieldControls.get(field.getFieldExtension().getId());
			field.getFieldExtension().getType().setInput(control, AbstractEntity.stringValueOf(field.getValue()));
		}
	}

	private void loadFurtherValues(final LinkPersonAddress link)
	{
		Long birthdate = link.getPerson().getBirthdate();
		if (birthdate != null)
		{
			long birth = birthdate.longValue();
			int year = GregorianCalendar.getInstance().get(Calendar.YEAR);
			if (birth < 1900 || year < birth)
			{
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(birth);
				this.birthdate.setSelection(calendar.getTime());
				this.birthyear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
			}
			else
			{
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.DATE, 1);
				calendar.set(Calendar.MONTH, 0);
				calendar.set(Calendar.YEAR, (int) birth);
				this.birthdate.setSelection(null);
				this.birthyear.setText(String.valueOf(calendar.get(Calendar.YEAR)));
			}
		}
		else
		{
			this.birthdate.setSelection(null);
			this.birthyear.setText("");
		}
		this.profession.setText(link.getPerson().getProfession());
		if (PersonSettings.getInstance().getPersonHasDomain())
		{
			if (link.getPerson().getDomain() != null)
			{
				if (this.domainViewer != null)
				{
					this.domainViewer.setSelection(new StructuredSelection(link.getPerson().getDomain()));
				}
			}
			else if (PersonSettings.getInstance().isPersonDomainMandatory())
			{
				Domain[] domains = (Domain[]) this.domainViewer.getInput();
				if (domains.length > 0)
					this.domainViewer.setSelection(new StructuredSelection(domains[0]));
			}
		}

		this.notes.setText(link.getPerson().getNotes().replace('\r', '\n'));
	}

	private void loadPersonAddressValues(final LinkPersonAddress link)
	{
		PersonSex sex = link.getPerson().getSex();
		if (sex == null)
		{
			PersonSex[] sexes = (PersonSex[]) this.sexRadioGroupViewer.getInput();
			if (sexes.length > 0)
			{
				sex = sexes[0];
			}
		}
		StructuredSelection ssel = new StructuredSelection(new PersonSex[] { sex });
		this.sexRadioGroupViewer.setSelection(ssel);

		PersonForm form = link.getPerson().getForm();
		if (form == null)
		{
			form = PersonForm.POLITE;
		}
		ssel = new StructuredSelection(new PersonForm[] { form });
		this.formRadioGroupViewer.setSelection(ssel);

		PersonTitle title = link.getPerson().getTitle();
		ssel = new StructuredSelection(new PersonTitle[] { title });
		this.titleViewer.setSelection(ssel);

		this.firstname.setText(link.getPerson().getFirstname());
		this.lastname.setText(link.getPerson().getLastname());
		this.organisation.setText(link.getAddress().getName());
		this.anotherLine.setText(link.getPerson().getAnotherLine());
		this.address.setText(link.getAddress().getAddress());
		this.pob.setText(link.getAddress().getPob());

		Country country = link.getAddress().getCountry();
		ssel = new StructuredSelection(new Country[] { country });
		this.addressCountryViewer.setSelection(ssel);

		this.zip.setText(link.getAddress().getZip());
		this.city.setText(link.getAddress().getCity());
		if (link.getAddress().getProvince() != null)
		{
			this.provinceViewer.setSelection(new StructuredSelection(new String[] { link.getAddress().getProvince() }));
		}
	}

	@Override
	public void loadValues()
	{
		LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) this.getEditorInput();
		LinkPersonAddress link = input.getEntity();

		loadPersonAddressValues(link);
		loadContactValues(link);
		loadFurtherValues(link);

		loadExtendedFieldValues();

		setDirty(false);
	}

	public void notifyListeners(final PropertyChangeEvent event)
	{
		IPropertyChangeListener[] ls = this.listeners.toArray(new IPropertyChangeListener[0]);
		for (IPropertyChangeListener l : ls)
		{
			l.propertyChange(event);
		}
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		if (entity instanceof Address)
		{
			LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) this.getEditorInput();
			if (input.getEntity().getId().equals(entity.getId()))
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(this, false);
		}

	}

	// private Message getEmptyAddressAndPobMessage()
	// {
	// Message msg = null;
	//
	// if (this.address.getText().isEmpty() && this.pob.getText().isEmpty())
	// {
	// msg = new Message(this.city, "Fehler");
	// msg.setMessage("Sie müssen entweder eine Strasse oder ein Postfach erfassen.");
	// FormToolkit.ensureVisible(this.address);
	// this.address.setFocus();
	// }
	//
	// return msg;
	// }

	@Override
	public void postUpdate(final AbstractEntity entity)
	{

	}

	@Override
	public void propertyChange(final java.beans.PropertyChangeEvent event)
	{
		this.setDirty(true);
	}

	@Override
	public void proposalAccepted(final IContentProposal contentProposal)
	{
		if (contentProposal instanceof CityContentProposal)
		{
			CityContentProposal proposal = (CityContentProposal) contentProposal;
			city.setText(proposal.getZipCode().getCity());
		}
	}

	public void removeListener(final IPropertyChangeListener listener)
	{
		this.listeners.remove(listener);
	}

	private void saveContactValues(final LinkPersonAddress link)
	{
		StructuredSelection ssel = (StructuredSelection) this.mobilePrefixViewer.getSelection();
		Country country = (Country) ssel.getFirstElement();
		link.getPerson().setCountry(country);
		if (link.getPerson().getCountry() == null)
		{
			link.getPerson().setCountry(link.getAddress().getCountry());
		}
		link.getPerson().setPhone(this.mobile.getValue().toString());
		link.setPhone(this.privatePhone.getValue().toString());
		link.getAddress().setPhone(this.businessPhone.getValue().toString());
		link.getAddress().setFax(this.fax.getValue().toString());
		link.getPerson().setEmail(this.personalEmail.getText());
		link.getPerson().setWebsite(this.personalWebsite.getText());
		link.setEmail(this.businessEmail.getText());
		link.getAddress().setWebsite(this.businessWebsite.getText());
	}

	private void saveExtendedFieldValues(final LinkPersonAddress link)
	{
		for (ExtendedField field : extendedFields.values())
		{
			Control control = extendedFieldControls.get(field.getFieldExtension().getId());
			field.setValue(field.getFieldExtension().getType().getInput(control));
			if (field.getId() == null && !field.getValue().isEmpty())
			{
				if (field.getFieldExtension().getTarget().equals(FieldExtensionTarget.PA_LINK))
				{
					link.addExtendedFields((LinkPersonAddressExtendedField) field);
				}
				else if (field.getFieldExtension().getTarget().equals(FieldExtensionTarget.PERSON))
				{
					link.getPerson().addExtendedFields((PersonExtendedField) field);
				}
			}
		}
	}

	private void saveFurtherValues(final LinkPersonAddress link)
	{
		Calendar calendar = GregorianCalendar.getInstance();
		if (this.birthdate.getSelection() == null)
		{
			if (this.birthyear.getText().isEmpty())
			{
				link.getPerson().setBirthdate(null);
			}
			else
			{
				link.getPerson().setBirthdate(Long.valueOf(this.birthyear.getText()));
			}
		}
		else
		{
			calendar.setTime(this.birthdate.getSelection());
			link.getPerson().setBirthdate(Long.valueOf(calendar.getTimeInMillis()));
		}
		link.getPerson().setProfession(this.profession.getText());
		if (PersonSettings.getInstance().getPersonHasDomain())
		{
			if (this.domainViewer != null)
			{
				StructuredSelection ssel = (StructuredSelection) this.domainViewer.getSelection();
				if (ssel.isEmpty())
				{
					link.getPerson().setDomain(null);
				}
				else if (((Domain) ssel.getFirstElement()).getId() == null)
				{
					link.getPerson().setDomain(null);
				}
				else
				{
					link.getPerson().setDomain((Domain) ssel.getFirstElement());
				}
			}
		}

		link.getPerson().setNotes(this.notes.getText().isEmpty() ? null : this.notes.getText());
	}

	private void savePersonAddressValues(final LinkPersonAddress link)
	{
		StructuredSelection ssel = (StructuredSelection) this.sexRadioGroupViewer.getSelection();
		PersonSex sex = (PersonSex) ssel.getFirstElement();
		link.getPerson().setSex(sex);

		ssel = (StructuredSelection) this.formRadioGroupViewer.getSelection();
		PersonForm form = (PersonForm) ssel.getFirstElement();
		link.getPerson().setForm(form);

		ssel = (StructuredSelection) this.titleViewer.getSelection();
		PersonTitle title = (PersonTitle) ssel.getFirstElement();
		link.getPerson().setTitle(title);

		link.getPerson().setFirstname(firstname.getText());
		link.getPerson().setLastname(lastname.getText());
		link.getAddress().setName(organisation.getText());
		link.getPerson().setAnotherLine(anotherLine.getText());
		link.getAddress().setAddress(address.getText());
		link.getAddress().setPob(pob.getText());

		ssel = (StructuredSelection) this.addressCountryViewer.getSelection();
		Country country = (Country) ssel.getFirstElement();
		link.getAddress().setCountry(country);

		link.getAddress().setZip(zip.getText());
		link.getAddress().setCity(city.getText());

		ssel = (StructuredSelection) provinceViewer.getSelection();
		String province = (String) ssel.getFirstElement();
		link.getAddress().setProvince(province);
	}

	@Override
	public void saveValues()
	{
		LinkPersonAddressEditorInput input = (LinkPersonAddressEditorInput) this.getEditorInput();
		LinkPersonAddress link = input.getEntity();

		savePersonAddressValues(link);
		saveContactValues(link);
		saveFurtherValues(link);

		saveExtendedFieldValues(link);
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

	private Domain[] selectDomains()
	{
		Collection<Domain> domains = null;
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
	public void setFocus()
	{
		this.organisation.setFocus();
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
					LinkPersonAddress link = (LinkPersonAddress) this.getEditorInput().getAdapter(
							LinkPersonAddress.class);
					PersonQuery query = (PersonQuery) service.getQuery(Person.class);
					count = query.countByFirstnameAndLastnameAsLike(this.firstname.getText(), this.lastname.getText(),
							link.getPerson().getId());
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
							StructuredSelection ssel = (StructuredSelection) this.sexRadioGroupViewer.getSelection();
							if (ssel.getFirstElement() instanceof PersonSex)
							{
								PersonSex sex = (PersonSex) ssel.getFirstElement();
								line = line.replace(variable, sex.getSalutation());
							}
							else
							{
								line = line.replace(variable, "");
							}
						}
						else if (variable.equals("${title}"))
						{
							StructuredSelection ssel = (StructuredSelection) this.titleViewer.getSelection();
							if (ssel.getFirstElement() instanceof PersonTitle)
							{
								PersonTitle title = (PersonTitle) ssel.getFirstElement();
								line = line.replace(variable, title.getTitle());
							}
							else
							{
								line = line.replace(variable, "");
							}
						}
						else if (variable.equals("${organisation}"))
						{
							line = line.replace(variable, this.organisation.getText());
						}
						else if (variable.equals("${firstname}"))
						{
							line = line.replace(variable, firstname.getText());
						}
						else if (variable.equals("${lastname}"))
						{
							line = line.replace(variable, lastname.getText());
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
							StructuredSelection ssel = (StructuredSelection) this.addressCountryViewer.getSelection();
							if (ssel.getFirstElement() instanceof Country)
							{
								Country country = (Country) ssel.getFirstElement();
								if (GlobalSettings.getInstance().getCountry() != null)
								{
									if (GlobalSettings.getInstance().getCountry().getId().equals(country.getId()))
									{
										c = "";
									}
									else
									{
										c = country.getIso3166alpha2();
									}
								}
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
		if (!addressLabel.isDisposed())
		{
			this.addressLabel.setText(label.toString());
		}
		if (!salutationLabel.isDisposed())
		{
			StructuredSelection ssel = (StructuredSelection) this.sexRadioGroupViewer.getSelection();
			PersonSex sex = (PersonSex) ssel.getFirstElement();
			ssel = (StructuredSelection) this.formRadioGroupViewer.getSelection();
			PersonForm form = (PersonForm) ssel.getFirstElement();
			if (sex == null || form == null)
			{
				this.salutationLabel.setText("");
			}
			else
			{
				ssel = (StructuredSelection) this.titleViewer.getSelection();
				PersonTitle title = (PersonTitle) ssel.getFirstElement();
				String letter = sex.getForm(form);

				letter = letter.replace("${title}", title == null ? "" : title.getTitle());
				letter = letter.replace("${firstname}", firstname.getText());
				letter = letter.replace("${lastname}", lastname.getText());
				letter = letter.replaceAll("  ", " ");
				this.salutationLabel.setText(letter.trim());
			}
		}
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

	@Override
	protected boolean validateType(final AbstractEntityEditorInput<Address> input)
	{
		return input.getEntity() instanceof Address;
	}

	private void zipSelected(final String code)
	{
		if (addressCountryViewer.getData("country") instanceof Country && !code.isEmpty())
		{
			Country country = (Country) addressCountryViewer.getData("country");
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
