package ch.eugster.events.person.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.progress.UIJob;

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException.ErrorCode;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.PersonSex;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class PersonSexEditor extends AbstractEntityEditor<PersonSex>
{

	public static final String ID = "ch.eugster.events.person.sex.editor";

	private Section sexSection;

	private Section salutationSection;

	private Text salutation;

	private Text polite;

	private Text personal;

	private Text symbol;

	public PersonSexEditor()
	{
		EntityMediator.addListener(PersonSex.class, this);
	}

	private void createSalutationSection()
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.salutationSection = this.formToolkit.createSection(this.scrolledForm.getBody(),
				ExpandableComposite.EXPANDED | ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR
						| ExpandableComposite.TWISTIE);
		this.salutationSection.setLayoutData(layoutData);
		this.salutationSection.setLayout(sectionLayout);
		this.salutationSection.setText("Allgemein");
		this.salutationSection.setClient(this.fillSalutationSection(this.salutationSection));
		this.salutationSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				PersonSexEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	@Override
	protected void createSections(final ScrolledForm parent)
	{
		this.createSexSection();
		this.createSalutationSection();
	}

	private void createSexSection()
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.sexSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.sexSection.setLayoutData(layoutData);
		this.sexSection.setLayout(sectionLayout);
		this.sexSection.setText("Allgemein");
		this.sexSection.setClient(this.fillSexSection(this.sexSection));
		this.sexSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				PersonSexEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSalutationSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		Label label = this.formToolkit.createLabel(composite,
				"Sie können folgende Variablen verwenden, um die Anreden zu personalisieren:", SWT.NONE);
		label.setLayoutData(gridData);

		String[] variables = PersonFormatter.getInstance().getPersonVisibleVariables();
		for (String variable : variables)
		{
			label = this.formToolkit.createLabel(composite, "", SWT.NONE);
			label.setLayoutData(new GridData());

			Link link = new Link(composite, SWT.None);
			link.setLayoutData(new GridData());
			link.setText("<a>" + variable + "</a>");
			link.addListener(SWT.Selection, new Listener()
			{
				@Override
				public void handleEvent(final Event event)
				{
					if (PersonSexEditor.this.polite.getCaretPosition() > 0)
					{
						int pos = PersonSexEditor.this.polite.getCaretPosition();
						String text = PersonSexEditor.this.polite.getText();
						String newText = text.substring(0, pos) + event.text + text.substring(pos);
						PersonSexEditor.this.polite.setText(newText);
					}
					else if (PersonSexEditor.this.personal.getCaretPosition() > 0)
					{
						int pos = PersonSexEditor.this.personal.getCaretPosition();
						String text = PersonSexEditor.this.personal.getText();
						String newText = text.substring(0, pos) + event.text + text.substring(pos);
						PersonSexEditor.this.personal.setText(newText);
					}
				}
			});
		}

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		label = this.formToolkit.createLabel(composite, "", SWT.NONE);
		label.setLayoutData(gridData);

		label = this.formToolkit.createLabel(composite, "Briefanrede höflich", SWT.NONE);
		label.setLayoutData(new GridData());

		this.polite = this.formToolkit.createText(composite, "");
		this.polite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.polite.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				PersonSexEditor.this.setDirty(true);
			}
		});
		this.polite.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		label = this.formToolkit.createLabel(composite, "Briefanrede persönlich", SWT.NONE);
		label.setLayoutData(new GridData());

		this.personal = this.formToolkit.createText(composite, "");
		this.personal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.personal.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				PersonSexEditor.this.setDirty(true);
			}
		});
		this.personal.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillSexSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Anrede", SWT.NONE);
		label.setLayoutData(new GridData());

		this.salutation = this.formToolkit.createText(composite, "");
		this.salutation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.salutation.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				PersonSexEditor.this.setDirty(true);
			}
		});
		this.salutation.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		label = this.formToolkit.createLabel(composite, "Symbol", SWT.NONE);
		label.setLayoutData(new GridData());

		this.symbol = this.formToolkit.createText(composite, "");
		this.symbol.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.symbol.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				PersonSexEditor.this.setDirty(true);
			}
		});
		this.symbol.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	@Override
	protected Message getMessage(final ErrorCode errorCode)
	{
		return null;
	}

	@Override
	protected String getName()
	{
		PersonSex salutation = ((PersonSexEditorInput) this.getEditorInput()).getEntity();
		return salutation.getId() == null ? "Neu" : salutation.getSalutation();
	}

	@Override
	protected String getText()
	{
		PersonSex personSex = ((PersonSexEditorInput) this.getEditorInput()).getEntity();
		return personSex.getId() == null ? "Neue Anrede" : personSex.getSalutation();
	}

	@Override
	protected void initialize()
	{
	}

	@Override
	protected void loadValues()
	{
		PersonSexEditorInput input = (PersonSexEditorInput) this.getEditorInput();
		PersonSex personSex = input.getEntity();
		this.salutation.setText(personSex.getSalutation());
		this.symbol.setText(personSex.getSymbol());
		this.polite.setText(PersonFormatter.getInstance().convertPoliteToVisible(personSex.getPolite()));
		this.personal.setText(PersonFormatter.getInstance().convertPoliteToVisible(personSex.getPersonal()));
		this.setDirty(false);
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				PersonSexEditorInput input = (PersonSexEditorInput) getEditorInput();
				PersonSex personSex = input.getEntity();
				if (personSex.getId() != null && personSex.getId().equals(entity.getId()))
				{
					getEditorSite().getPage().closeEditor(PersonSexEditor.this, false);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	protected void saveValues()
	{
		PersonSexEditorInput input = (PersonSexEditorInput) this.getEditorInput();
		PersonSex personSex = input.getEntity();
		personSex.setSalutation(this.salutation.getText());
		personSex.setSymbol(this.symbol.getText());
		personSex.setPolite(PersonFormatter.getInstance().convertPoliteToStored(this.polite.getText()));
		personSex.setPersonal(PersonFormatter.getInstance().convertPoliteToStored(this.personal.getText()));
	}

	@Override
	public void setFocus()
	{
		this.salutation.setFocus();
	}

	@Override
	protected boolean validate()
	{
		return true;
	}

	@Override
	protected boolean validateType(final AbstractEntityEditorInput<PersonSex> input)
	{
		return input.getEntity() instanceof PersonSex;
	}
}
