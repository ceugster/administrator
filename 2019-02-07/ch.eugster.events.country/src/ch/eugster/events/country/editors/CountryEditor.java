package ch.eugster.events.country.editors;

import org.eclipse.nebula.widgets.formattedtext.MaskFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.country.Activator;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class CountryEditor extends AbstractEntityEditor<Country>
{
	public static final String ID = "ch.eugster.events.country.editor";

	private Text name;

	private Text iso3166alpha2;

	private Text iso3166alpha3;

	private Text iso31662numeric;

	private Text ituCode;

	private Text phonePrefix;

	private Text phonePattern;

	private Text cityLinePattern;

	// private Text addressLinePattern;

	private Button visible;

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(Country.class, this);
	}

	@Override
	protected void createSections(ScrolledForm parent)
	{
		this.createCountrySection(parent);
		this.createCodeSection(parent);
		this.createPatternSection(parent);
	}

	private void createCountrySection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Land");
		section.setClient(this.fillLandSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				CountryEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private void createCodeSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Codes");
		section.setClient(this.fillCodeSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				CountryEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private void createPatternSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Einstellungen");
		section.setClient(this.fillPatternSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				CountryEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillLandSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.name = this.formToolkit.createText(composite, "");
		this.name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.name.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				CountryEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "", SWT.NONE);
		label.setLayoutData(new GridData());

		visible = this.formToolkit.createButton(composite, "In Auswahllisten zeigen", SWT.CHECK);
		visible.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		visible.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				CountryEditor.this.setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillCodeSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "ISO 3166 zweistellig", SWT.NONE);
		label.setLayoutData(new GridData());

		this.iso3166alpha2 = this.formToolkit.createText(composite, "");
		this.iso3166alpha2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.iso3166alpha2.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				CountryEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "ISO 3166 dreistellig", SWT.NONE);
		label.setLayoutData(new GridData());

		this.iso3166alpha3 = this.formToolkit.createText(composite, "");
		this.iso3166alpha3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.iso3166alpha3.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				CountryEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "ISO 3166 numerisch", SWT.NONE);
		label.setLayoutData(new GridData());

		this.iso31662numeric = this.formToolkit.createText(composite, "");
		this.iso31662numeric.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.iso31662numeric.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				CountryEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "ITU Code", SWT.NONE);
		label.setLayoutData(new GridData());

		this.ituCode = this.formToolkit.createText(composite, "");
		this.ituCode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.ituCode.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				CountryEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillPatternSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Landesvorwahl", SWT.NONE);
		label.setLayoutData(new GridData());

		this.phonePrefix = this.formToolkit.createText(composite, "");
		this.phonePrefix.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.phonePrefix.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				CountryEditor.this.setDirty(true);
			}
		});

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		label = this.formToolkit.createLabel(composite, "", SWT.NONE);
		label.setLayoutData(gridData);

		label = this.formToolkit.createLabel(composite, "Muster Telefonnummer", SWT.NONE);
		label.setLayoutData(new GridData());

		this.phonePattern = this.formToolkit.createText(composite, "");
		this.phonePattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.phonePattern.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				CountryEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "", SWT.NONE);
		label.setLayoutData(new GridData());

		label = this.formToolkit.createLabel(composite, "Beliebige Ziffer = #", SWT.WRAP);
		label.setLayoutData(new GridData());

		label = this.formToolkit.createLabel(composite, "Muster prüfen", SWT.NONE);
		label.setLayoutData(new GridData());

		final Text text = this.formToolkit.createText(composite, "", SWT.FLAT);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = this.formToolkit.createLabel(composite, "Resultat", SWT.NONE);
		label.setLayoutData(new GridData());

		final Label testlabel = this.formToolkit.createLabel(composite, "", SWT.WRAP);
		testlabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				try
				{
					MaskFormatter formatter = new MaskFormatter(phonePattern.getText());
					formatter.setValue(text.getText());
					testlabel.setText(formatter.getDisplayString());
				}
				catch (Exception ex)
				{
					testlabel.setText(ex.getLocalizedMessage());
				}
			}
		});

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		label = this.formToolkit.createLabel(composite, "", SWT.NONE);
		label.setLayoutData(gridData);

		label = this.formToolkit.createLabel(composite, "Ortsadresszeile", SWT.NONE);
		label.setLayoutData(new GridData());

		this.cityLinePattern = this.formToolkit.createText(composite, "");
		this.cityLinePattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.cityLinePattern.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				CountryEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "", SWT.NONE);
		label.setLayoutData(new GridData());

		label = this.formToolkit.createLabel(composite, "L (Land), P (PLZ), O (Ort) (z.B. Schweiz: L-P O)", SWT.NONE);
		label.setLayoutData(new GridData());

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	@Override
	protected Message getMessage(PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		if (errorCode.equals(""))
		{
			msg = this.getUniqueCodeMessage();
		}
		return msg;
	}

	private Message getUniqueCodeMessage()
	{
		Message msg = null;

		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = tracker.getService();
			if (service != null)
			{
				CountryEditorInput input = (CountryEditorInput) this.getEditorInput();
				Country country = (Country) input.getAdapter(Country.class);
				String code = this.iso3166alpha2.getText();
	
				CountryQuery query = (CountryQuery) service.getQuery(Country.class);
				if (!query.isIso3166alpha2Unique(code, country.getId()))
				{
					msg = new Message(this.iso3166alpha2, "Ungültiger Code");
					msg.setMessage("Der gewählte ISO 3166 Code wird bereits verwendet.");
					return msg;
				}
	
				if (!query.isIso3166alpha3Unique(code, country.getId()))
				{
					msg = new Message(this.iso3166alpha3, "Ungültiger Code");
					msg.setMessage("Der gewählte ISO 3166 Code wird bereits verwendet.");
					return msg;
				}
	
				if (!query.isIso31662numericUnique(code, country.getId()))
				{
					msg = new Message(this.iso31662numeric, "Ungültiger Code");
					msg.setMessage("Der gewählte numerische ISO 31662 Code wird bereits verwendet.");
					return msg;
				}
			}
		}
		finally
		{
			tracker.close();
		}
		return msg;
	}

	private Message getEmptyNameMessage()
	{
		Message msg = null;

		if (this.name.getText().isEmpty())
		{
			msg = new Message(this.name, "Fehler");
			msg.setMessage("Die Domäne muss eine Bezeichnung haben.");
		}

		return msg;
	}

	@Override
	protected String getName()
	{
		CountryEditorInput input = (CountryEditorInput) this.getEditorInput();
		Country country = (Country) input.getAdapter(Country.class);
		return country.getId() == null ? "Neu" : (country.getIso3166alpha2().length() == 0 ? "???" : country
				.getIso3166alpha2());
	}

	@Override
	protected String getText()
	{
		CountryEditorInput input = (CountryEditorInput) this.getEditorInput();
		Country country = (Country) input.getAdapter(Country.class);
		return country.getId() == null ? "Neues Land" : country.getName();
	}

	@Override
	protected void loadValues()
	{
		CountryEditorInput input = (CountryEditorInput) this.getEditorInput();
		Country country = (Country) input.getAdapter(Country.class);

		this.iso31662numeric.setText(country.getIso31662numeric());
		this.iso3166alpha2.setText(country.getIso3166alpha2());
		this.iso3166alpha3.setText(country.getIso3166alpha3());
		this.name.setText(country.getName());
		this.ituCode.setText(country.getItuCode());
		this.phonePrefix.setText(country.getPhonePrefix());
		this.phonePattern.setText(country.getPhonePattern());
		this.cityLinePattern.setText(country.getCityLinePattern());
		this.visible.setSelection(country.isVisible());

		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		CountryEditorInput input = (CountryEditorInput) this.getEditorInput();
		Country country = (Country) input.getAdapter(Country.class);

		country.setCityLinePattern(cityLinePattern.getText());
		country.setIso31662numeric(iso31662numeric.getText());
		country.setIso3166alpha2(iso3166alpha2.getText());
		country.setIso3166alpha3(iso3166alpha3.getText());
		country.setItuCode(ituCode.getText());
		country.setName(name.getText());
		country.setPhonePattern(phonePattern.getText());
		country.setPhonePrefix(phonePrefix.getText());
		country.setVisible(visible.getSelection());

	}

	@Override
	protected boolean validate()
	{
		Message msg = this.getUniqueCodeMessage();

		if (msg == null)
			msg = this.getEmptyNameMessage();

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<Country> input)
	{
		return input.getAdapter(Country.class) instanceof Country;
	}

	@Override
	public void setFocus()
	{
		this.iso3166alpha2.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Country.class, this);
	}
}
