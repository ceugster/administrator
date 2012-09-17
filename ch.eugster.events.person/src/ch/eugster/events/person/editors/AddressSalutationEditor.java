package ch.eugster.events.person.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException.ErrorCode;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class AddressSalutationEditor extends AbstractEntityEditor<AddressSalutation>
{

	public static final String ID = "ch.eugster.events.person.salutation.editor";

	private Section salutationSection;

	private Text salutation;

	private Text polite;

	public AddressSalutationEditor()
	{
		EntityMediator.addListener(AddressSalutation.class, this);
	}

	@Override
	protected void createSections(ScrolledForm parent)
	{
		this.createSalutationSection();
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
		this.salutationSection.setText("Anrede");
		this.salutationSection.setClient(this.fillTitleSection(this.salutationSection));
		this.salutationSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				AddressSalutationEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillTitleSection(Section parent)
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
			public void modifyText(ModifyEvent e)
			{
				AddressSalutationEditor.this.setDirty(true);
			}
		});
		this.salutation.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		label = this.formToolkit.createLabel(composite, "Briefanrede", SWT.NONE);
		label.setLayoutData(new GridData());

		this.polite = this.formToolkit.createText(composite, "");
		this.polite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.polite.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				AddressSalutationEditor.this.setDirty(true);
			}
		});
		this.polite.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	@Override
	protected Message getMessage(ErrorCode errorCode)
	{
		return null;
	}

	@Override
	protected String getName()
	{
		AddressSalutation salutation = ((AddressSalutationEditorInput) this.getEditorInput()).getEntity();
		return salutation.getId() == null ? "Neu" : salutation.getSalutation();
	}

	@Override
	protected String getText()
	{
		AddressSalutation salutation = ((AddressSalutationEditorInput) this.getEditorInput()).getEntity();
		return salutation.getId() == null ? "Neue Anrede" : salutation.getSalutation();
	}

	@Override
	protected void initialize()
	{
	}

	@Override
	protected void loadValues()
	{
		AddressSalutationEditorInput input = (AddressSalutationEditorInput) this.getEditorInput();
		AddressSalutation salutation = input.getEntity();
		this.salutation.setText(salutation.getSalutation());
		this.polite.setText(salutation.getPolite());
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		AddressSalutationEditorInput input = (AddressSalutationEditorInput) this.getEditorInput();
		AddressSalutation salutation = input.getEntity();
		salutation.setSalutation(this.salutation.getText());
		salutation.setPolite(this.polite.getText());
	}

	@Override
	protected boolean validate()
	{
		return true;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<AddressSalutation> input)
	{
		return input.getEntity() instanceof AddressSalutation;
	}

	@Override
	public void setFocus()
	{
		this.salutation.setFocus();
	}

	@Override
	public void postDelete(AbstractEntity entity)
	{
		AddressSalutationEditorInput input = (AddressSalutationEditorInput) this.getEditorInput();
		AddressSalutation salutation = input.getEntity();
		if (salutation.getId() != null && salutation.getId().equals(entity.getId()))
		{
			this.getEditorSite().getPage().closeEditor(this, false);
		}
	}
}
