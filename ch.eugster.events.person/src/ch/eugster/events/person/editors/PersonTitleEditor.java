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
import org.eclipse.ui.progress.UIJob;

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException.ErrorCode;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class PersonTitleEditor extends AbstractEntityEditor<PersonTitle>
{

	public static final String ID = "ch.eugster.events.person.title.editor";

	private Section titleSection;

	private Text title;

	public PersonTitleEditor()
	{
		EntityMediator.addListener(PersonTitle.class, this);
	}

	@Override
	protected void createSections(ScrolledForm parent)
	{
		this.createTitleSection();
	}

	private void createTitleSection()
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.titleSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.titleSection.setLayoutData(layoutData);
		this.titleSection.setLayout(sectionLayout);
		this.titleSection.setText("Personentitel");
		this.titleSection.setClient(this.fillTitleSection(this.titleSection));
		this.titleSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				PersonTitleEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillTitleSection(Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Titel", SWT.NONE);
		label.setLayoutData(new GridData());

		this.title = this.formToolkit.createText(composite, "");
		this.title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.title.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				PersonTitleEditor.this.setDirty(true);
			}
		});
		this.title.addFocusListener(new FocusAdapter()
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
		PersonTitle title = ((PersonTitleEditorInput) this.getEditorInput()).getEntity();
		return title.getId() == null ? "Neu" : title.getTitle();
	}

	@Override
	protected String getText()
	{
		PersonTitle title = ((PersonTitleEditorInput) this.getEditorInput()).getEntity();
		return title.getId() == null ? "Neuer Titel" : title.getTitle();
	}

	@Override
	protected void initialize()
	{
	}

	@Override
	protected void loadValues()
	{
		PersonTitleEditorInput input = (PersonTitleEditorInput) this.getEditorInput();
		PersonTitle personTitle = input.getEntity();
		this.title.setText(personTitle.getTitle());
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		PersonTitleEditorInput input = (PersonTitleEditorInput) this.getEditorInput();
		PersonTitle personTitle = input.getEntity();
		personTitle.setTitle(this.title.getText());
	}

	@Override
	protected boolean validate()
	{
		return true;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<PersonTitle> input)
	{
		return input.getEntity() instanceof PersonTitle;
	}

	@Override
	public void setFocus()
	{
		this.title.setFocus();
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				PersonTitleEditorInput input = (PersonTitleEditorInput) getEditorInput();
				PersonTitle personTitle = input.getEntity();
				if (personTitle.getId() != null && personTitle.getId().equals(entity.getId()))
				{
					getEditorSite().getPage().closeEditor(PersonTitleEditor.this, false);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
}
