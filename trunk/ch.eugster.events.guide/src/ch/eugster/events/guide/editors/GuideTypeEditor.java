package ch.eugster.events.guide.editors;

import java.io.File;

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
import org.eclipse.swt.widgets.FileDialog;
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

import ch.eugster.events.guide.Activator;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.GuideType;
import ch.eugster.events.persistence.queries.GuideTypeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class GuideTypeEditor extends AbstractEntityEditor<GuideType>
{
	public static final String ID = "ch.eugster.events.guide.editor.guidetype";

	private Text code;

	private Text name;

	private Text description;

	private Text template;
	
	private Button templateSelector;
	
	@Override
	protected void initialize()
	{
		EntityMediator.addListener(GuideType.class, this);
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
				GuideTypeEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSection(Section parent)
	{
		GridLayout layout = new GridLayout(3, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Code", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		this.code = this.formToolkit.createText(composite, "");
		this.code.setLayoutData(gridData);
		this.code.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				if (GuideTypeEditor.this.name.getText().isEmpty()
						|| GuideTypeEditor.this.name.getText().equals(
								GuideTypeEditor.this.code.getText().substring(0,
										GuideTypeEditor.this.code.getText().length() - 1)))
					GuideTypeEditor.this.name.setText(GuideTypeEditor.this.code.getText());
				GuideTypeEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		this.name = this.formToolkit.createText(composite, "");
		this.name.setLayoutData(gridData);
		this.name.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				GuideTypeEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Beschreibung", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.heightHint = 48;
		
		this.description = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL);
		this.description.setLayoutData(gridData);
		this.description.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				GuideTypeEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Vertragsvorlage", SWT.NONE);
		label.setLayoutData(new GridData());

		this.template = this.formToolkit.createText(composite, "");
		this.template.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.template.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				GuideTypeEditor.this.setDirty(true);
			}
		});

		this.templateSelector = this.formToolkit.createButton(composite, "...", SWT.PUSH);
		this.templateSelector.setLayoutData(new GridData());
		this.templateSelector.addSelectionListener(new SelectionListener()
		{
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(GuideTypeEditor.this.getSite().getShell());
				dialog.setFilterExtensions(new String[] { "*.odt"});
				dialog.setFilterIndex(0);
				dialog.setText("Vertragsvorlage wählen");
				String path = dialog.open();
				if (path != null)
				{
					GuideTypeEditor.this.template.setText(path);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
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
			GuideTypeEditorInput input = (GuideTypeEditorInput) this.getEditorInput();
			GuideType guideType = (GuideType) input.getAdapter(GuideType.class);
			String code = this.code.getText();
			GuideTypeQuery query = (GuideTypeQuery) service.getQuery(GuideType.class);
			if (!query.isCodeUnique(code, guideType.getId()))
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

	private Message getInvalidTemplatePathMessage()
	{
		Message msg = null;

		if (!this.template.getText().isEmpty())
		{
			File path = new File(this.template.getText());
			if (!path.isFile() || !path.getName().endsWith(".odt"))
			{
				msg = new Message(this.template, "Fehler");
				msg.setMessage("Die angegebene Vertragsvorlage ist ungültig.");
			}
		}

		return msg;
	}

	@Override
	protected String getName()
	{
		GuideTypeEditorInput input = (GuideTypeEditorInput) this.getEditorInput();
		GuideType guideType = (GuideType) input.getAdapter(GuideType.class);
		return guideType.getId() == null ? "Neu" : (guideType.getCode().length() == 0 ? "???" : guideType.getCode());
	}

	@Override
	protected String getText()
	{
		GuideTypeEditorInput input = (GuideTypeEditorInput) this.getEditorInput();
		GuideType guideType = (GuideType) input.getAdapter(GuideType.class);
		return guideType.getId() == null ? "Neue Kategorie" : "Kategorie "
				+ (guideType.getCode().length() == 0 ? guideType.getName() : guideType.getCode());
	}

	@Override
	protected void loadValues()
	{
		GuideTypeEditorInput input = (GuideTypeEditorInput) this.getEditorInput();
		GuideType guideType = (GuideType) input.getAdapter(GuideType.class);
		if (guideType != null)
		{
			this.code.setText(guideType.getCode());
			this.name.setText(guideType.getName());
			this.description.setText(guideType.getDescription());
			this.template.setText(guideType.getTemplate() == null ? "" : guideType.getTemplate());
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		GuideTypeEditorInput input = (GuideTypeEditorInput) this.getEditorInput();
		GuideType guideType = (GuideType) input.getAdapter(GuideType.class);
		if (guideType != null)
		{
			guideType.setCode(this.code.getText());
			guideType.setName(this.name.getText());
			guideType.setDescription(this.description.getText());
			guideType.setTemplate(this.template.getText().isEmpty() ? null : this.template.getText());
		}
	}

	@Override
	protected boolean validate()
	{
		Message msg = this.getUniqueCodeMessage();

		if (msg == null)
			msg = this.getEmptyNameMessage();

		if (msg == null)
			msg = this.getInvalidTemplatePathMessage();

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<GuideType> input)
	{
		return input.getAdapter(GuideType.class) instanceof GuideType;
	}

	@Override
	public void setFocus()
	{
		this.code.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(GuideType.class, this);
	}
}
