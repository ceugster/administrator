package ch.eugster.events.person.editors;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.TextContentAdapter;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.persistence.queries.AddressSalutationQuery;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.queries.ZipCodeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.preferences.PreferenceInitializer;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;
import ch.eugster.events.ui.helpers.BrowseHelper;
import ch.eugster.events.ui.helpers.EmailHelper;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class AddressEditor extends AbstractEntityEditor<Address> implements PropertyChangeListener,
		IContentProposalListener
{
	public static final String ID = "ch.eugster.events.person.editors.addressEditor";

	private static final String EMAIL_LABEL = "Email (senden)";

	private static final String EMAIL_LINK = "Email (<a>senden</a>)";

	private static final String WEBSITE_LABEL = "Webseite (öffnen)";

	private static final String WEBSITE_LINK = "Webseite (<a>öffnen</a>)";

	private static final String CONTACT_SECTION_EXPANDED = "contacts.section.expanded";

	private static final String ADDRESS_SECTION_EXPANDED = "address.section.expanded";

	private Text addressLabel;

	private ComboViewer salutationViewer;

	private Text email;

	private Link sendEmail;

	private Text website;

	private Text phonePrefix;

	private Text phone;

	private Text faxPrefix;

	private Text fax;

	private Link browseWebsite;

	private Text name;

	private Text anotherLine;

	private Text address;

	private Text pob;

	private ComboViewer countryViewer;

	private Text zip;

	private Text city;

	private ComboViewer provinceViewer;

	private EntityAdapter entityAdapter;

	private IDialogSettings dialogSettings;

	private Section addressSection;

	private Section contactSection;

	private PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

	private AddressSalutation emptySalutation;

	public AddressEditor()
	{
		super();
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
				AddressEditor.this.dialogSettings.put(AddressEditor.ADDRESS_SECTION_EXPANDED, e.getState());
				AddressEditor.this.scrolledForm.reflow(true);
			}
		});
		this.addressSection.setExpanded(this.dialogSettings.getBoolean(AddressEditor.ADDRESS_SECTION_EXPANDED));
	}

	// private void updateAddressLabel()
	// {
	// Collection<String> labelLines = new ArrayList<String>();
	// String pattern = GlobalSettings.getInstance().getAddressLabelFormat();
	// if (pattern.isEmpty())
	// {
	// pattern = AddressFormatter.getInstance().createVisibleAddressLabel();
	// pattern =
	// AddressFormatter.getInstance().convertAddressLabelToStored(pattern);
	// }
	// String[] lines = pattern.split("[|]");
	// for (String line : lines)
	// {
	// if (line.contains("${"))
	// {
	// String[] variables =
	// AddressFormatter.getInstance().getAddressLabelStoredVariables();
	// for (String variable : variables)
	// {
	// if (line.contains("${"))
	// {
	// if (variable.equals("${salutation}"))
	// {
	// StructuredSelection ssel = (StructuredSelection)
	// this.salutationViewer.getSelection();
	// String salutation = "";
	// if (ssel.getFirstElement() instanceof AddressSalutation)
	// {
	// AddressSalutation as = (AddressSalutation) ssel.getFirstElement();
	// salutation = as.getSalutation();
	// }
	// line = line.replace(variable, salutation);
	// }
	// else if (variable.equals("${organization}"))
	// {
	// line = line.replace(variable, this.name.getText());
	// }
	// else if (variable.equals("${anotherline}"))
	// {
	// line = line.replace(variable, this.anotherLine.getText());
	// }
	// else if (variable.equals("${address}"))
	// {
	// line = line.replace(variable, this.address.getText());
	// }
	// else if (variable.equals("${pob}"))
	// {
	// line = line.replace(variable, this.pob.getText());
	// }
	// else if (variable.equals("${country}"))
	// {
	// String c = null;
	// StructuredSelection ssel = (StructuredSelection)
	// this.countryViewer.getSelection();
	// if (ssel.getFirstElement() instanceof Country)
	// {
	// Country country = (Country) ssel.getFirstElement();
	// c = country.getIso3166alpha2();
	// }
	// else
	// {
	// c = "";
	// }
	// line = line.replace(variable, c);
	// if (c.equals(""))
	// {
	// line = line.replace("-", "");
	// }
	// }
	// else if (variable.equals("${zip}"))
	// {
	// line = line.replace(variable, this.zip.getText());
	// }
	// else if (variable.equals("${city}"))
	// {
	// line = line.replace(variable, this.city.getText());
	// }
	// }
	// }
	// }
	// if (!line.trim().isEmpty())
	// {
	// labelLines.add(line.trim());
	// }
	// }
	// StringBuilder label = new StringBuilder();
	// for (String labelLine : labelLines)
	// {
	// if (label.length() > 0)
	// {
	// label = label.append("\n");
	// }
	// label = label.append(labelLine);
	// }
	// this.addressLabel.setText(label.toString());
	// }

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
				AddressEditor.this.dialogSettings.put(AddressEditor.CONTACT_SECTION_EXPANDED, e.getState());
				AddressEditor.this.scrolledForm.reflow(true);
			}
		});
		this.contactSection.setExpanded(this.dialogSettings.getBoolean(AddressEditor.CONTACT_SECTION_EXPANDED));
	}

	private void createLabelSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.addressSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.addressSection.setLayoutData(layoutData);
		this.addressSection.setLayout(sectionLayout);
		this.addressSection.setText("Etikette");
		this.addressSection.setClient(this.fillLabelSection(this.addressSection));
		this.addressSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				AddressEditor.this.dialogSettings.put(AddressEditor.ADDRESS_SECTION_EXPANDED, e.getState());
				AddressEditor.this.scrolledForm.reflow(true);
			}
		});
		this.addressSection.setExpanded(this.dialogSettings.getBoolean(AddressEditor.ADDRESS_SECTION_EXPANDED));
	}

	@Override
	protected void createSections(final ScrolledForm parent)
	{
		createAddressSection(parent);
		createContactSection(parent);
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

		Label label = formToolkit.createLabel(composite, "Anrede", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(gridData);
		formToolkit.adapt(combo);

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
				updateAddressLabel();
				setDirty(true);
			}
		});

		label = formToolkit.createLabel(composite, "Name Organisation", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;

		this.name = formToolkit.createText(composite, "");
		this.name.setLayoutData(gridData);
		this.name.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateAddressLabel();
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
				updateAddressLabel();
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
				updateAddressLabel();
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
				updateAddressLabel();
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
					// linkPhonePrefix.setText(country.getPhonePrefix());
					phonePrefix.setText(country.getPhonePrefix());
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
					faxPrefix.setText(country.getPhonePrefix());
					numValue = getNumValue(fax.getText());
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
					String[] states = selectProvinceCodes(country);
					provinceViewer.setInput(states);
					if (zip.getData("zipCode") instanceof ZipCode)
					{
						ZipCode zipCode = (ZipCode) zip.getData("zipCode");
						provinceViewer.setSelection(new StructuredSelection(zipCode.getState()));
					}
				}
				updateAddressLabel();
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
				updateAddressLabel();
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

		// label = toolkit.createLabel(client, "Ort", SWT.NONE);
		// label.setLayoutData(new GridData());

		this.city = formToolkit.createText(composite, "");
		this.city.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.city.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				updateAddressLabel();
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
					new CityContentProposalProvider(countryViewer, zip), keyStroke, autoActivationCharacters);
			proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
			proposalAdapter.addContentProposalListener(this);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

		// label = toolkit.createLabel(client, "Kanton", SWT.NONE);
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

	private Composite fillContactSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(3, false));

		Label label = formToolkit.createLabel(composite, "Telefon", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData();
		gridData.widthHint = 48;

		phonePrefix = formToolkit.createText(composite, "");
		phonePrefix.setLayoutData(gridData);
		phonePrefix.setEnabled(false);

		this.phone = formToolkit.createText(composite, "");
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
				IStructuredSelection ssel = (IStructuredSelection) AddressEditor.this.countryViewer.getSelection();
				if (ssel.getFirstElement() instanceof Country)
				{
					String value = AddressEditor.this.phone.getText().trim();
					value = removeSpaces(value);
					boolean dirty = isDirty();
					AddressEditor.this.phone.setText(value);
					AddressEditor.this.phone.setSelection(0, AddressEditor.this.phone.getText().length());
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
				if (!AddressEditor.this.countryViewer.getSelection().isEmpty())
				{
					IStructuredSelection ssel = (IStructuredSelection) AddressEditor.this.countryViewer.getSelection();
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

		label = formToolkit.createLabel(composite, "Fax", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 48;

		faxPrefix = formToolkit.createText(composite, "");
		faxPrefix.setLayoutData(gridData);
		faxPrefix.setEnabled(false);

		this.fax = formToolkit.createText(composite, "");
		this.fax.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.fax.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
			}
		});
		this.fax.addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				IStructuredSelection ssel = (IStructuredSelection) AddressEditor.this.countryViewer.getSelection();
				if (ssel.getFirstElement() instanceof Country)
				{
					String value = AddressEditor.this.fax.getText().trim();
					value = removeSpaces(value);
					boolean dirty = isDirty();
					AddressEditor.this.fax.setText(value);
					AddressEditor.this.fax.setSelection(0, AddressEditor.this.fax.getText().length());
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
				if (!AddressEditor.this.countryViewer.getSelection().isEmpty())
				{
					IStructuredSelection ssel = (IStructuredSelection) AddressEditor.this.countryViewer.getSelection();
					if (ssel.getFirstElement() instanceof Country)
					{
						String faxString = fax.getText();
						if (!faxString.isEmpty())
						{
							Country country = (Country) ssel.getFirstElement();
							try
							{
								PhoneNumber faxNumber = phoneUtil.parse(faxString, country.getIso3166alpha2());
								faxString = phoneUtil.format(faxNumber, PhoneNumberFormat.NATIONAL);
								boolean dirty = isDirty();
								fax.setText(faxString);
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

		sendEmail = new Link(composite, SWT.NONE);
		sendEmail.setText(AddressEditor.EMAIL_LABEL);
		sendEmail.setToolTipText("Klicken Sie hier, um ein Email zu schreiben");
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
				EmailHelper.getInstance().sendEmail(AddressEditor.this.email.getText());
			}
		});

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.email = formToolkit.createText(composite, "");
		this.email.setLayoutData(gridData);
		this.email.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
				if (EmailHelper.getInstance().isValidAddress(email.getText()))
				{
					sendEmail.setText(AddressEditor.EMAIL_LINK);
				}
				else
				{
					sendEmail.setText(AddressEditor.EMAIL_LABEL);
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

		browseWebsite = new Link(composite, SWT.NONE);
		browseWebsite.setText(AddressEditor.WEBSITE_LABEL);
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
				BrowseHelper.getInstance().browse(AddressEditor.this.website.getText());
			}
		});

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.website = formToolkit.createText(composite, "");
		this.website.setLayoutData(gridData);
		this.website.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setDirty(true);
				if (website.getText().length() > 11 && website.getText().startsWith("http://"))
				{
					browseWebsite.setText(AddressEditor.WEBSITE_LINK);
				}
				else
				{
					browseWebsite.setText(AddressEditor.WEBSITE_LABEL);
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

		formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Composite fillLabelSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 96;

		addressLabel = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.WRAP);
		addressLabel.setLayoutData(gridData);
		addressLabel.setEditable(false);

		this.formToolkit.paintBordersFor(composite);

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

	@Override
	protected Message getMessage(final PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		return msg;
	}

	@Override
	protected String getName()
	{
		AddressEditorInput input = (AddressEditorInput) this.getEditorInput();
		Address address = (Address) input.getAdapter(Address.class);
		return address.getId() == null ? "Neu" : "A" + AddressFormatter.getInstance().formatId(address);
	}

	@Override
	protected String getText()
	{
		AddressEditorInput input = (AddressEditorInput) this.getEditorInput();
		Address address = (Address) input.getAdapter(Address.class);
		return address.getId() == null ? "Neue Adresse" : "Adresse " + AddressFormatter.getInstance().formatId(address);
	}

	@Override
	protected void initialize()
	{
		String value = Activator.getDefault().getPreferenceStore()
				.getString(PreferenceInitializer.KEY_EDITOR_SECTION_BEHAVIOUR);

		Long id = ((AddressEditorInput) this.getEditorInput()).getEntity().getId();
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

	private void loadAddressValues(final Address address)
	{
		this.salutationViewer
				.setSelection(new StructuredSelection(
						new AddressSalutation[] { address.getSalutation() == null ? emptySalutation : address
								.getSalutation() }));
		this.name.setText(address.getName());
		this.anotherLine.setText(address.getAnotherLine());
		this.address.setText(address.getAddress());
		this.pob.setText(address.getPob());
		if (address.getCountry() == null)
		{
			address.setCountry(AddressFormatter.getInstance().getCountry());
		}
		this.countryViewer.setSelection(new StructuredSelection(address.getCountry()));
		this.zip.setText(address.getZip());
		this.city.setText(address.getCity());
		if (address.getProvince() != null)
		{
			this.provinceViewer.setSelection(new StructuredSelection(new String[] { address.getProvince() }));
		}
	}

	private void loadContactValues(final Address address)
	{
		this.phone.setText(address.getPhone());
		this.fax.setText(address.getFax());
		this.email.setText(address.getEmail());
		this.website.setText(address.getWebsite());
	}

	private void loadSalutationValues(final Address address)
	{
		// this.collectionName.setText(address.getCollectionName());
		// this.collectionAnotherLine.setText(address.getCollectionAnotherLine());
	}

	@Override
	public void loadValues()
	{
		AddressEditorInput input = (AddressEditorInput) this.getEditorInput();
		Address address = input.getEntity();

		loadAddressValues(address);
		loadContactValues(address);
		loadSalutationValues(address);

		setDirty(false);
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (entity instanceof Address)
				{
					AddressEditorInput input = (AddressEditorInput) getEditorInput();
					if (input.getEntity().getId().equals(entity.getId()))
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.closeEditor(AddressEditor.this, false);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

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

	private void saveAddressValues(final Address address)
	{
		StructuredSelection ssel = (StructuredSelection) this.salutationViewer.getSelection();
		if (ssel.getFirstElement() == null || ssel.getFirstElement().equals(emptySalutation))
		{
			address.setSalutation(null);
		}
		else
		{
			address.setSalutation((AddressSalutation) ssel.getFirstElement());
		}
		address.setName(name.getText());
		address.setAnotherLine(anotherLine.getText());
		address.setAddress(this.address.getText());
		address.setPob(this.pob.getText());
		ssel = (StructuredSelection) countryViewer.getSelection();
		if (ssel.getFirstElement() instanceof Country)
		{
			address.setCountry((Country) ssel.getFirstElement());
		}
		else
		{
			address.setCountry(null);
		}
		address.setZip(this.zip.getText());
		address.setCity(this.city.getText());
		if (this.provinceViewer.getSelection().isEmpty())
		{
			address.setProvince(null);
		}
		else
		{
			ssel = (StructuredSelection) this.provinceViewer.getSelection();
			address.setProvince((String) ssel.getFirstElement());
		}
	}

	private void saveContactValues(final Address address)
	{
		address.setPhone(removeSpaces(this.phone.getText()));
		address.setFax(removeSpaces(this.fax.getText()));
		address.setEmail(this.email.getText());
		address.setWebsite(this.website.getText());
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

	private void saveSalutationValues(final Address address)
	{
		// address.setCollectionName(this.collectionName.getText());
		// address.setCollectionAnotherLine(this.collectionAnotherLine.getText());
	}

	@Override
	public void saveValues()
	{
		AddressEditorInput input = (AddressEditorInput) this.getEditorInput();
		Address address = input.getEntity();

		saveAddressValues(address);
		saveContactValues(address);
		saveSalutationValues(address);
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

	private AddressSalutation[] selectSalutations()
	{
		Collection<AddressSalutation> salutations = new ArrayList<AddressSalutation>();
		emptySalutation = AddressSalutation.newInstance();
		salutations.add(emptySalutation);
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			AddressSalutationQuery query = (AddressSalutationQuery) service.getQuery(AddressSalutation.class);
			salutations = query.selectAll();
		}
		tracker.close();
		return salutations == null ? new AddressSalutation[0] : salutations.toArray(new AddressSalutation[0]);
	}

	@Override
	public void setFocus()
	{
		this.name.setFocus();
	}

	private void updateAddressLabel()
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
				String[] variables = PersonFormatter.getInstance().getPersonLabelStoredVariables();
				for (String variable : variables)
				{
					if (line.contains("${"))
					{
						if (variable.equals("${salutation}"))
						{
							StructuredSelection ssel = (StructuredSelection) this.salutationViewer.getSelection();
							if (ssel.getFirstElement() instanceof AddressSalutation)
							{
								AddressSalutation salutation = (AddressSalutation) ssel.getFirstElement();
								line = line.replace(variable, salutation.getSalutation());
							}
							else
							{
								line = line.replace(variable, "");
							}
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
