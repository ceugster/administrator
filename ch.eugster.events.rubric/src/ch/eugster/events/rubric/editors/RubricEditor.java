package ch.eugster.events.rubric.editors;

import org.eclipse.swt.SWT;
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
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.Rubric;
import ch.eugster.events.persistence.queries.RubricQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.rubric.Activator;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class RubricEditor extends AbstractEntityEditor<Rubric>
{
	public static final String ID = "ch.eugster.events.rubric.editor";

	private Text code;

	private Text name;

	private Text description;

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(Rubric.class, this);
	}

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

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Beschreibung");
		section.setClient(this.fillSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				RubricEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Code", SWT.NONE);
		label.setLayoutData(new GridData());

		this.code = this.formToolkit.createText(composite, "");
		this.code.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.code.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				if (RubricEditor.this.name.getText().isEmpty()
						|| RubricEditor.this.name.getText().equals(
								RubricEditor.this.code.getText().substring(0,
										RubricEditor.this.code.getText().length() - 1)))
					RubricEditor.this.name.setText(RubricEditor.this.code.getText());
				RubricEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.name = this.formToolkit.createText(composite, "");
		this.name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.name.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				RubricEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Beschreibung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.description = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL);
		this.description.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.description.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				RubricEditor.this.setDirty(true);
			}
		});

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
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				RubricEditorInput input = (RubricEditorInput) this.getEditorInput();
				Rubric rubric = (Rubric) input.getAdapter(Rubric.class);
				String code = this.code.getText();
				RubricQuery query = (RubricQuery) service.getQuery(Rubric.class);
				if (!query.isCodeUnique(code, rubric.getId()))
				{
					msg = new Message(this.code, "Ungültiger Code");
					msg.setMessage("Der gewählte Code wird bereits verwendet.");
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
			msg.setMessage("Die Rubrik muss eine Bezeichnung haben.");
		}

		return msg;
	}

	@Override
	protected String getName()
	{
		RubricEditorInput input = (RubricEditorInput) this.getEditorInput();
		Rubric rubric = (Rubric) input.getAdapter(Rubric.class);
		return rubric.getId() == null ? "Neu" : (rubric.getCode().length() == 0 ? "???" : rubric.getCode());
	}

	@Override
	protected String getText()
	{
		RubricEditorInput input = (RubricEditorInput) this.getEditorInput();
		Rubric rubric = (Rubric) input.getAdapter(Rubric.class);
		return rubric.getId() == null ? "Neue Rubrik" : "Rubrik "
				+ (rubric.getCode().length() == 0 ? rubric.getName() : rubric.getCode());
	}

	@Override
	protected void loadValues()
	{
		RubricEditorInput input = (RubricEditorInput) this.getEditorInput();
		Rubric rubric = (Rubric) input.getAdapter(Rubric.class);
		if (rubric != null)
		{
			this.code.setText(rubric.getCode());
			this.name.setText(rubric.getName());
			this.description.setText(rubric.getDescription());
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		RubricEditorInput input = (RubricEditorInput) this.getEditorInput();
		Rubric rubric = (Rubric) input.getAdapter(Rubric.class);
		if (rubric != null)
		{
			rubric.setCode(this.code.getText());
			rubric.setName(this.name.getText());
			rubric.setDescription(this.description.getText());
		}
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
	protected boolean validateType(AbstractEntityEditorInput<Rubric> input)
	{
		return input.getAdapter(Rubric.class) instanceof Rubric;
	}

	@Override
	public void setFocus()
	{
		this.code.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Rubric.class, this);
	}
}
