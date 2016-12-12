package ch.eugster.events.person.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.ui.progress.UIJob;

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException.ErrorCode;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.FieldExtensionTarget;
import ch.eugster.events.persistence.model.FieldExtensionType;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class FieldExtensionEditor extends AbstractEntityEditor<FieldExtension>
{

	public static final String ID = "ch.eugster.events.person.field.extension.editor";

	private Section section;

	private ComboViewer typeViewer;

	private ComboViewer targetViewer;

	private Text label;

	private Spinner minValue;

	private Spinner maxValue;

	private Spinner decimal;

	private Spinner widthHint;

	private Spinner heightHint;

	private Spinner style;

	private Text defaultValue;

	private Button searchable;

	public FieldExtensionEditor()
	{
		EntityMediator.addListener(FieldExtension.class, this);
	}

	private boolean checkLabel()
	{
		if (label.getText().isEmpty())
		{
			this.showWarningMessage("Eingabefehler", "Sie haben keine Bezeichnung eingegeben.",
					targetViewer.getControl());
			return false;
		}
		return true;
	}

	private boolean checkTarget()
	{
		if (targetViewer.getSelection().isEmpty())
		{
			this.showWarningMessage("Eingabefehler", "Sie haben keine Zieltabelle ausgewählt.",
					targetViewer.getControl());
			return false;
		}
		return true;
	}

	private boolean checkType()
	{
		if (typeViewer.getSelection().isEmpty())
		{
			this.showWarningMessage("Eingabefehler", "Sie haben keinen Feldtyp ausgewählt.", targetViewer.getControl());
			return false;
		}
		return true;
	}

	private void createNameSection()
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.section.setLayoutData(layoutData);
		this.section.setLayout(sectionLayout);
		this.section.setText("Bezeichnung");
		this.section.setClient(this.fillNameSection(this.section));
		this.section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				FieldExtensionEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private void createNumericSection()
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.section.setLayoutData(layoutData);
		this.section.setLayout(sectionLayout);
		this.section.setText("Eigenschaften für numerisches Feld");
		this.section.setClient(this.fillNumericSection(this.section));
		this.section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				FieldExtensionEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	@Override
	protected void createSections(final ScrolledForm parent)
	{
		this.createNameSection();
		this.createWidgetSection();
		this.createNumericSection();
	}

	private void createWidgetSection()
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.section.setLayoutData(layoutData);
		this.section.setLayout(sectionLayout);
		this.section.setText("Feldeigenschaften");
		this.section.setClient(this.fillWidgetSection(this.section));
		this.section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				FieldExtensionEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillNameSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Zieltabelle", SWT.NONE);
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		formToolkit.adapt(combo);

		targetViewer = new ComboViewer(combo);
		targetViewer.setContentProvider(new ArrayContentProvider());
		targetViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(final Object element)
			{
				if (element instanceof FieldExtensionTarget)
				{
					FieldExtensionTarget target = (FieldExtensionTarget) element;
					return target.label();
				}
				return "";
			}
		});
		targetViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				FieldExtensionEditor.this.setDirty(true);
			}
		});
		targetViewer.setInput(FieldExtensionTarget.values());

		label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.label = this.formToolkit.createText(composite, "");
		this.label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.label.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				FieldExtensionEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Vorschlagswert", SWT.NONE);
		label.setLayoutData(new GridData());

		this.defaultValue = this.formToolkit.createText(composite, "");
		this.defaultValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.defaultValue.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				FieldExtensionEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "", SWT.NONE);
		label.setLayoutData(new GridData());

		this.searchable = this.formToolkit.createButton(composite, "Durchsuchbar", SWT.CHECK);
		this.searchable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.searchable.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				FieldExtensionEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillNumericSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Minimalwert", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData();
		gridData.widthHint = 64;

		this.minValue = new Spinner(composite, SWT.NONE);
		this.minValue.setLayoutData(gridData);
		this.minValue.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				FieldExtensionEditor.this.setDirty(true);
			}
		});
		this.minValue.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		formToolkit.adapt(minValue);

		label = this.formToolkit.createLabel(composite, "Maximalwert", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 64;

		this.maxValue = new Spinner(composite, SWT.NONE);
		this.maxValue.setLayoutData(gridData);
		this.maxValue.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				FieldExtensionEditor.this.setDirty(true);
			}
		});
		this.maxValue.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		formToolkit.adapt(maxValue);

		label = this.formToolkit.createLabel(composite, "Dezimalstellen", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 64;

		this.decimal = new Spinner(composite, SWT.NONE);
		this.decimal.setLayoutData(gridData);
		this.decimal.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				FieldExtensionEditor.this.setDirty(true);
			}
		});
		this.decimal.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		formToolkit.adapt(decimal);

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillWidgetSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Feldtyp", SWT.NONE);
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		formToolkit.adapt(combo);

		typeViewer = new ComboViewer(combo);
		typeViewer.setContentProvider(new ArrayContentProvider());
		typeViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(final Object element)
			{
				if (element instanceof FieldExtensionType)
				{
					FieldExtensionType type = (FieldExtensionType) element;
					return type.label();
				}
				return "";
			}
		});
		typeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				FieldExtensionEditor.this.setDirty(true);
			}
		});
		typeViewer.setInput(FieldExtensionType.values());

		label = this.formToolkit.createLabel(composite, "Stil", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData();
		gridData.widthHint = 64;

		this.style = new Spinner(composite, SWT.NONE);
		this.style.setLayoutData(gridData);
		this.style.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				FieldExtensionEditor.this.setDirty(true);
			}
		});
		this.style.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		formToolkit.adapt(style);

		label = this.formToolkit.createLabel(composite, "Feldbreite", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 64;

		this.widthHint = new Spinner(composite, SWT.NONE);
		this.widthHint.setLayoutData(gridData);
		this.widthHint.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				FieldExtensionEditor.this.setDirty(true);
			}
		});
		this.widthHint.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		formToolkit.adapt(widthHint);

		label = this.formToolkit.createLabel(composite, "Feldhöhe", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 64;

		this.heightHint = new Spinner(composite, SWT.NONE);
		this.heightHint.setLayoutData(gridData);
		this.heightHint.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				FieldExtensionEditor.this.setDirty(true);
			}
		});
		this.heightHint.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		formToolkit.adapt(heightHint);

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
		FieldExtension extension = ((FieldExtensionEditorInput) this.getEditorInput()).getEntity();
		return extension.getId() == null ? "Neu" : extension.getLabel();
	}

	@Override
	protected String getText()
	{
		FieldExtension extension = ((FieldExtensionEditorInput) this.getEditorInput()).getEntity();
		return extension.getId() == null ? "Neu" : extension.getLabel();
	}

	@Override
	protected void initialize()
	{
	}

	@Override
	protected void loadValues()
	{
		FieldExtensionEditorInput input = (FieldExtensionEditorInput) this.getEditorInput();
		FieldExtension extension = input.getEntity();

		this.typeViewer.setSelection(new StructuredSelection(new FieldExtensionType[] { extension.getType() }));

		this.targetViewer.setSelection(new StructuredSelection(new FieldExtensionTarget[] { extension.getTarget() }));

		this.label.setText(FieldExtension.stringValueOf(extension.getLabel()).isEmpty() ? "" : extension.getLabel());
		this.defaultValue.setText(FieldExtension.stringValueOf(extension.getDefaultValue()).isEmpty() ? "" : extension
				.getDefaultValue());

		this.maxValue.setSelection(extension.getMaxValue());
		this.minValue.setSelection(extension.getMinValue());
		this.decimal.setSelection(extension.getDecimal());

		this.searchable.setSelection(extension.isSearchable());

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
				FieldExtensionEditorInput input = (FieldExtensionEditorInput) getEditorInput();
				FieldExtension extension = input.getEntity();

				if (extension.getId() != null && extension.getId().equals(entity.getId()))
				{
					getEditorSite().getPage().closeEditor(FieldExtensionEditor.this, false);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	protected void saveValues()
	{
		FieldExtensionEditorInput input = (FieldExtensionEditorInput) this.getEditorInput();
		FieldExtension extension = input.getEntity();

		StructuredSelection ssel = (StructuredSelection) typeViewer.getSelection();
		extension.setType((FieldExtensionType) ssel.getFirstElement());

		ssel = (StructuredSelection) targetViewer.getSelection();
		extension.setTarget((FieldExtensionTarget) ssel.getFirstElement());

		extension.setLabel(label.getText());

		extension.setDefaultValue(defaultValue.getText());

		extension.setMinValue(minValue.getSelection());

		extension.setMaxValue(maxValue.getSelection());

		extension.setDecimal(decimal.getSelection());

		extension.setSearchable(searchable.getSelection());
	}

	@Override
	public void setFocus()
	{
		this.typeViewer.getCCombo().setFocus();
	}

	@Override
	protected boolean validate()
	{
		boolean valid = checkTarget();
		if (valid)
		{
			valid = checkLabel();
			if (valid)
			{
				valid = checkType();
			}
		}
		return valid;
	}

	@Override
	protected boolean validateType(final AbstractEntityEditorInput<FieldExtension> input)
	{
		return input.getEntity() instanceof FieldExtension;
	}
}
