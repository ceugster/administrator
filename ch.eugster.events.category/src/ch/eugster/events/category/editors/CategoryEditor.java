package ch.eugster.events.category.editors;

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

import ch.eugster.events.category.Activator;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.Category;
import ch.eugster.events.persistence.queries.CategoryQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class CategoryEditor extends AbstractEntityEditor<Category>
{
	public static final String ID = "ch.eugster.events.category.editor";

	private Text code;

	private Text name;

	private Text description;

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(Category.class, this);
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
				CategoryEditor.this.scrolledForm.reflow(true);
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
				if (CategoryEditor.this.name.getText().isEmpty()
						|| CategoryEditor.this.name.getText().equals(
								CategoryEditor.this.code.getText().substring(0,
										CategoryEditor.this.code.getText().length() - 1)))
					CategoryEditor.this.name.setText(CategoryEditor.this.code.getText());
				CategoryEditor.this.setDirty(true);
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
				CategoryEditor.this.setDirty(true);
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
				CategoryEditor.this.setDirty(true);
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

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();

		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			CategoryEditorInput input = (CategoryEditorInput) this.getEditorInput();
			Category category = (Category) input.getAdapter(Category.class);
			String code = this.code.getText();
			CategoryQuery query = (CategoryQuery) service.getQuery(Category.class);
			if (!query.isCodeUnique(code, category.getId()))
			{
				msg = new Message(this.code, "Ungültiger Code");
				msg.setMessage("Der gewählte Code wird bereits verwendet.");
				return msg;
			}
		}
		tracker.close();

		return msg;
	}

	private Message getEmptyNameMessage()
	{
		Message msg = null;

		if (this.name.getText().isEmpty())
		{
			msg = new Message(this.name, "Fehler");
			msg.setMessage("Die Kategorie muss eine Bezeichnung haben.");
		}

		return msg;
	}

	@Override
	protected String getName()
	{
		CategoryEditorInput input = (CategoryEditorInput) this.getEditorInput();
		Category category = (Category) input.getAdapter(Category.class);
		return category.getId() == null ? "Neu" : (category.getCode().length() == 0 ? "???" : category.getCode());
	}

	@Override
	protected String getText()
	{
		CategoryEditorInput input = (CategoryEditorInput) this.getEditorInput();
		Category category = (Category) input.getAdapter(Category.class);
		return category.getId() == null ? "Neue Kategorie" : "Kategorie "
				+ (category.getCode().length() == 0 ? category.getName() : category.getCode());
	}

	@Override
	protected void loadValues()
	{
		CategoryEditorInput input = (CategoryEditorInput) this.getEditorInput();
		Category category = (Category) input.getAdapter(Category.class);
		if (category != null)
		{
			this.code.setText(category.getCode());
			this.name.setText(category.getName());
			this.description.setText(category.getDescription());
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		CategoryEditorInput input = (CategoryEditorInput) this.getEditorInput();
		Category category = (Category) input.getAdapter(Category.class);
		if (category != null)
		{
			category.setCode(this.code.getText());
			category.setName(this.name.getText());
			category.setDescription(this.description.getText());
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
	protected boolean validateType(AbstractEntityEditorInput<Category> input)
	{
		return input.getAdapter(Category.class) instanceof Category;
	}

	@Override
	public void setFocus()
	{
		this.code.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Category.class, this);
	}
}
