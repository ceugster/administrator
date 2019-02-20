package ch.eugster.events.charity.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.formattedtext.DoubleFormatter;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.charity.Activator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.formatters.PhoneFormatter;
import ch.eugster.events.persistence.model.CharityPerson;
import ch.eugster.events.persistence.model.CharityRunner;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.queries.CharityRunnerQuery;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class CharityRunnerEditor extends AbstractEntityEditor<CharityRunner>
{
	public static final String ID = "ch.eugster.events.charity.runner.editor";

	private Country country;

	private Button leadership;

	private Text groupName;

	private Button[] sexes;

	private Text firstname;

	private Text lastname;

	private Text street;

	private Text zip;

	private Text city;

	private Text phone;

	private Text email;

	private Spinner rounds;
	
	private FormattedText variableAmount;
	
	private FormattedText fixAmount;
	
	private ComboViewer groupViewer;
	
	@Override
	protected void createSections(ScrolledForm parent)
	{
		this.createLeadershipSection(parent);
		this.createAddressSection(parent);
		this.createAmountSection(parent);
		this.createGroupSection(parent);
	}

	private void createLeadershipSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | Section.DESCRIPTION);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Gruppenleitung");
		section.setDescription("Dieser Bereich muss nur ausgefüllt werden, wenn die Person eine Gruppe (oder Schulklasse) führt.");
		section.setClient(this.fillLeadershipSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				CharityRunnerEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private void createAddressSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Adressinformationen");
		section.setClient(this.fillAddressSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				CharityRunnerEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private void createAmountSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Sponsorenbeträge");
		section.setClient(this.fillAmountSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				CharityRunnerEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private void createGroupSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Gruppeninformationen");
		section.setDescription("Dieser Bereich muss nur ausgefüllt werden, wenn die Person in einer Gruppe läuft, die Gruppe aber nicht leitet.");
		section.setClient(this.fillGroupSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				CharityRunnerEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillLeadershipSection(Section parent)
	{
		CharityRunnerEditorInput input = (CharityRunnerEditorInput) this.getEditorInput();
		final CharityRunner runner = input.getEntity();
		
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Gruppenleitung", SWT.NONE);
		label.setLayoutData(new GridData());

		leadership = this.formToolkit.createButton(composite, "", SWT.CHECK);
		leadership.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		leadership.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				boolean dirty = CharityRunnerEditor.this.isDirty();

				if (leadership.getSelection())
				{
					groupViewer.getCombo().setEnabled(false);
					groupViewer.setSelection(new StructuredSelection());
				}
				else
				{
					if (runner.getRunners().isEmpty())
					{
						groupViewer.getCombo().setEnabled(true);
						groupViewer.setSelection(runner.getLeader() == null ? new StructuredSelection() : new StructuredSelection(new CharityRunner[] { runner.getLeader() }));
					}
					else
					{
						MessageDialog.openConfirm(getSite().getShell(), "Fehler", "Die Gruppenleitung kann nicht deaktiviert werden, weil die zugehörige Gruppe nicht leer ist.");
						leadership.setSelection(true);
						dirty = dirty == false ? false : true;
					}
				}
				CharityRunnerEditor.this.setDirty(dirty);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				this.widgetSelected(e);
			}
		});
		leadership.setEnabled(runner.hasLeadership());
		
		label = this.formToolkit.createLabel(composite, "Gruppenbezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.groupName = this.formToolkit.createText(composite, "");
		this.groupName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.groupName.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunnerEditor.this.setDirty(true);
			}
		});
		this.groupName.setEnabled(runner.hasLeadership());
		
		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillAddressSection(Section parent)
	{
		GridLayout layout = new GridLayout(3, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		
		Group sexGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		sexGroup.setLayoutData(gridData);
		sexGroup.setLayout(new GridLayout(2, true));
		sexGroup.setText("Anrede");

		sexes = new Button[CharityPerson.Sex.values().length];
		for (int i = 0; i < CharityPerson.Sex.values().length; i++)
		{
			sexes[i] = this.formToolkit.createButton(sexGroup, CharityPerson.Sex.values()[i].salutation(), SWT.RADIO);
			sexes[i].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			sexes[i].setData(CharityPerson.Sex.values()[i]);
			sexes[i].addSelectionListener(new SelectionListener() 
			{
				@Override
				public void widgetSelected(SelectionEvent e) 
				{
					CharityRunnerEditor.this.setDirty(true);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) 
				{
					this.widgetSelected(e);
				}
			});
		}

		Label label = this.formToolkit.createLabel(composite, "Vorname", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		this.firstname = this.formToolkit.createText(composite, "");
		this.firstname.setLayoutData(gridData);
		this.firstname.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunnerEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Nachname", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		this.lastname = this.formToolkit.createText(composite, "");
		this.lastname.setLayoutData(gridData);
		this.lastname.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunnerEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Strasse", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		this.street = this.formToolkit.createText(composite, "");
		this.street.setLayoutData(gridData);
		this.street.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunnerEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "PLZ Ort", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 64;
		
		this.zip = this.formToolkit.createText(composite, "");
		this.zip.setLayoutData(gridData);
		this.zip.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunnerEditor.this.setDirty(true);
			}
		});

		this.city = this.formToolkit.createText(composite, "");
		this.city.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.city.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunnerEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Telefon", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		this.phone = this.formToolkit.createText(composite, "");
		this.phone.setLayoutData(gridData);
		this.phone.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunnerEditor.this.setDirty(true);
			}
		});
		this.phone.addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(FocusEvent e) 
			{
				phone.setText(phone.getText().replaceAll("\\s+",""));
			}

			@Override
			public void focusLost(FocusEvent e) 
			{
				phone.setText(PhoneFormatter.format(phone.getText(), country == null ? "CH" : country.getIso3166alpha2()));
			}
		});
		
		label = this.formToolkit.createLabel(composite, "Email", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		this.email = this.formToolkit.createText(composite, "");
		this.email.setLayoutData(gridData);
		this.email.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunnerEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillAmountSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Gelaufene Runden", SWT.NONE);
		label.setLayoutData(new GridData());

		this.rounds = new Spinner(composite, SWT.WRAP);
		this.rounds.setLayoutData(new GridData());
		this.rounds.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.rounds.setIncrement(1);
		this.rounds.setPageIncrement(10);
		this.rounds.setMaximum(Integer.MAX_VALUE);
		this.rounds.setMinimum(0);
		this.rounds.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunnerEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Betrag pro Runde", SWT.NONE);
		label.setLayoutData(new GridData());

		DoubleFormatter formatter = new DoubleFormatter();
	    formatter.setFixedLengths(false, true);

	    GridData gridData = new GridData();
	    gridData.widthHint = 100;
	    
	    this.variableAmount = new FormattedText(composite, SWT.BORDER | SWT.RIGHT);
		this.variableAmount.getControl().setLayoutData(gridData);
		this.variableAmount.getControl().setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
	    this.variableAmount.setFormatter(formatter);
	    this.variableAmount.getControl().addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunnerEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Fixer Betrag", SWT.NONE);
		label.setLayoutData(new GridData());

		formatter = new DoubleFormatter();
	    formatter.setFixedLengths(false, true);

	    gridData = new GridData();
	    gridData.widthHint = 100;
	    
	    this.fixAmount = new FormattedText(composite, SWT.BORDER | SWT.RIGHT);
		this.fixAmount.getControl().setLayoutData(gridData);
		this.fixAmount.getControl().setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
	    this.fixAmount.setFormatter(formatter);
	    this.fixAmount.getControl().addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunnerEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillGroupSection(Section parent)
	{
		CharityRunnerEditorInput input = (CharityRunnerEditorInput) this.getEditorInput();
		final CharityRunner runner = input.getEntity();
		
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Gruppenzugehörigkeit", SWT.NONE);
		label.setLayoutData(new GridData());

		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);

		groupViewer = new ComboViewer(combo);
		groupViewer.setContentProvider(new ArrayContentProvider());
		groupViewer.setLabelProvider(new LabelProvider() 
		{
			@Override
			public String getText(Object element) 
			{
				CharityRunner runner = (CharityRunner) element;
				return runner.getGroupName();
			}
		});
		groupViewer.addSelectionChangedListener(new ISelectionChangedListener() 
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				CharityRunner leader = (CharityRunner) ssel.getFirstElement();
				if (leader == null || leader.getId() == null)
				{
					leadership.setEnabled(true);
					groupName.setEnabled(true);
					groupName.setText(runner.getGroupName());
					groupViewer.getCombo().setEnabled(!leadership.getSelection());
				}
				else
				{
					leadership.setSelection(false);
					leadership.setEnabled(false);
					groupName.setText("");
					groupName.setEnabled(false);
					groupViewer.getCombo().setEnabled(true);
				}
				CharityRunnerEditor.this.setDirty(true);
			}
		});
		groupViewer.setInput(getLeaders());
		groupViewer.getCombo().setEnabled(!runner.hasLeadership());
		
		this.formToolkit.paintBordersFor(composite);

		return composite;
	}
	
	private CharityRunner[] getLeaders()
	{
		CharityRunnerEditorInput input = (CharityRunnerEditorInput) this.getEditorInput();
		CharityRunner runner = input.getEntity();
		
		List<CharityRunner> leaders = new ArrayList<CharityRunner>();
		leaders.add(CharityRunner.newInstance(runner.getCharityRun(), CharityPerson.newInstance()));
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundleContext(), ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = tracker.getService();
			if (service != null)
			{
				CharityRunnerQuery query = (CharityRunnerQuery) service.getQuery(CharityRunner.class);
				leaders.addAll(query.selectLeaders());
				leaders.remove(runner);
			}
		}
		finally
		{
			tracker.close();
		}
		return leaders.toArray(new CharityRunner[0]);
	}

	@Override
	protected Message getMessage(PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		if (errorCode.equals(""))
		{
			msg = this.getEmptyNameMessage();
		}
		return msg;
	}

	private Message getEmptyNameMessage()
	{
		Message msg = null;

		if (this.leadership.getSelection() &&  this.groupName.getText().isEmpty())
		{
			msg = new Message(this.groupName, "Fehler");
			msg.setMessage("Bitte geben Sie eine gültige Gruppenbezeichnung ein.");
		}

		return msg;
	}

	@Override
	protected String getName()
	{
		CharityRunnerEditorInput input = (CharityRunnerEditorInput) this.getEditorInput();
		CharityRunner charityRunner = input.getEntity();
		return charityRunner.getId() == null ? "Neu" : (charityRunner.getPerson().getLastname().length() == 0 ? "???" : charityRunner.getPerson().getLastname());
	}

	@Override
	protected String getText()
	{
		CharityRunnerEditorInput input = (CharityRunnerEditorInput) this.getEditorInput();
		CharityRunner charityRunner = input.getEntity();
		return charityRunner.getId() == null ? "Neuer Läufer" : "Läufer "
				+ (charityRunner.getPerson().getLastname().isEmpty() ? "???" : charityRunner.getPerson().getLastname());
	}

	@Override
	protected void loadValues()
	{
		CharityRunnerEditorInput input = (CharityRunnerEditorInput) this.getEditorInput();
		CharityRunner charityRunner = input.getEntity();
		this.leadership.setSelection(charityRunner.hasLeadership());
		this.groupName.setText(charityRunner.getGroupName());
		this.sexes[charityRunner.getPerson().getSex().ordinal()].setSelection(true);
		this.firstname.setText(charityRunner.getPerson().getFirstname());
		this.lastname.setText(charityRunner.getPerson().getLastname());
		this.street.setText(charityRunner.getPerson().getStreet());
		this.zip.setText(charityRunner.getPerson().getZip());
		this.city.setText(charityRunner.getPerson().getCity());
		this.phone.setText(charityRunner.getPerson().getPhone());
		this.email.setText(charityRunner.getPerson().getEmail());
		this.rounds.setSelection(charityRunner.getRounds());
		this.variableAmount.setValue(charityRunner.getVariableAmount());
		this.fixAmount.setValue(charityRunner.getFixAmount());
		this.groupViewer.setSelection(charityRunner.getLeader() == null ? new StructuredSelection() : new StructuredSelection( new CharityRunner[] { charityRunner.getLeader() }));
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		CharityRunnerEditorInput input = (CharityRunnerEditorInput) this.getEditorInput();
		CharityRunner charityRunner = input.getEntity();
		charityRunner.setLeadership(this.leadership.getSelection());
		charityRunner.setGroupName(this.leadership.getSelection() ? this.groupName.getText() : "");
		for (Button sex : sexes)
		{
			if (sex.getSelection())
			{
				charityRunner.getPerson().setSex((CharityPerson.Sex) sex.getData());
			}
		}
		charityRunner.getPerson().setFirstname(this.firstname.getText());
		charityRunner.getPerson().setLastname(this.lastname.getText());
		charityRunner.getPerson().setStreet(this.street.getText());
		charityRunner.getPerson().setZip(this.zip.getText());
		charityRunner.getPerson().setCity(this.city.getText());
		charityRunner.getPerson().setPhone(this.phone.getText());
		charityRunner.getPerson().setEmail(this.email.getText());
		charityRunner.setRounds(this.rounds.getSelection());
		charityRunner.setVariableAmount(((Double)this.variableAmount.getValue()).doubleValue());
		charityRunner.setFixAmount(((Double) this.fixAmount.getValue()).doubleValue());
		charityRunner.getPerson().setFirstname(this.firstname.getText());
		/*
		 * Be attentive that in the groupViewer there exists an empty CharityRunner. This runner
		 * has the only purpose to let the user deselect the currently edited CharityRunner from any leader.
		 * So pay attention to not add this empty CharityRunner, else there would one be created.
		 */
		IStructuredSelection ssel = (IStructuredSelection) groupViewer.getSelection();
		CharityRunner leader = (CharityRunner) ssel.getFirstElement();
		CharityRunner oldLeader = charityRunner.getLeader();
		if (oldLeader != null) 
		{
			oldLeader.removeRunner(charityRunner);
		}
		charityRunner.setLeader((ssel.isEmpty() || leader.getId() == null) ? null : leader);
	}

	@Override
	protected boolean validate()
	{
		Message msg = this.getEmptyNameMessage();

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<CharityRunner> input)
	{
		return input.getAdapter(CharityRunner.class) instanceof CharityRunner;
	}

	@Override
	public void setFocus()
	{
		this.firstname.setFocus();
	}

	@Override
	protected void initialize() 
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundleContext(), ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = tracker.getService();
			if (service != null)
			{
				CountryQuery query = (CountryQuery) service.getQuery(Country.class);
				this.country = query.selectDefault();
			}
		}
		finally
		{
			tracker.close();
		}
	}

	@Override
	public void dispose()
	{
	}
}
