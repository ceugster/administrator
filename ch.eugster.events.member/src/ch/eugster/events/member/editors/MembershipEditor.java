package ch.eugster.events.member.editors;

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

import ch.eugster.events.persistence.exceptions.PersistenceException.ErrorCode;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class MembershipEditor extends AbstractEntityEditor<Donation>
{
	public static final String ID = "ch.eugster.events.member.editor";

	private Section section;

	private Text code;

	private Text name;

	private Button memberCodeMandatory;

	@Override
	protected void createSections(ScrolledForm parent)
	{
		this.createSection(parent);
	}

	private void createSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.section.setLayoutData(layoutData);
		this.section.setLayout(sectionLayout);
		this.section.setText("Organisation");
		this.section.setClient(this.fillSection(this.section));
		this.section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				MembershipEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSection(Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Code", SWT.NONE);
		label.setLayoutData(new GridData());

		this.code = new Text(composite, SWT.BORDER);
		this.code.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.code.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent event)
			{
				MembershipEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.name = new Text(composite, SWT.BORDER);
		this.name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.name.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent event)
			{
				MembershipEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "", SWT.NONE);
		label.setLayoutData(new GridData());

		this.memberCodeMandatory = formToolkit.createButton(composite, "Eingabe der Mitgliedernummer erzwingen",
				SWT.CHECK);
		this.memberCodeMandatory.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.memberCodeMandatory.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				MembershipEditor.this.setDirty(true);
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

	@Override
	protected Message getMessage(ErrorCode errorCode)
	{
		return null;
	}

	protected Message getMessage()
	{
		if (this.name.getText().isEmpty())
		{
			return new Message(this.name);
		}
		return null;
	}

	@Override
	protected String getName()
	{
		MembershipEditorInput input = (MembershipEditorInput) this.getEditorInput();
		return input.getEntity().getId() == null ? "Neu" : "Mitgliedschaft: " + input.getEntity().getCode();
	}

	@Override
	protected String getText()
	{
		MembershipEditorInput input = (MembershipEditorInput) this.getEditorInput();
		return input.getEntity().getName().isEmpty() ? "Mitgliedschaft" : input.getEntity().getName();
	}

	@Override
	protected void initialize()
	{
	}

	@Override
	protected void loadValues()
	{
		MembershipEditorInput input = (MembershipEditorInput) this.getEditorInput();
		Membership membership = input.getEntity();
		this.code.setText(membership.getCode());
		this.name.setText(membership.getName());
		this.memberCodeMandatory.setSelection(membership.isMemberCodeMandatory());
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		MembershipEditorInput input = (MembershipEditorInput) this.getEditorInput();
		Membership membership = input.getEntity();
		membership.setCode(code.getText());
		membership.setName(name.getText());
		membership.setMemberCodeMandatory(memberCodeMandatory.getSelection());
	}

	@Override
	protected boolean validate()
	{
		if (this.name.getText().isEmpty())
		{
			return false;
		}

		return true;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<Donation> input)
	{
		return input.getEntity() instanceof Donation;
	}

	@Override
	public void setFocus()
	{
		this.code.setFocus();
	}

}
