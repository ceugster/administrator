package ch.eugster.events.guide.editors;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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

import ch.eugster.events.guide.Activator;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.CompensationType;
import ch.eugster.events.persistence.model.CompensationType.Type;
import ch.eugster.events.persistence.queries.CompensationTypeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class CompensationTypeEditor extends AbstractEntityEditor<CompensationType>
{
	public static final String ID = "ch.eugster.events.guide.editor.compensationtype";

	private ComboViewer type;
	
	private Text code;

	private Text name;

	private Text description;
	
	private Cursor arrowCursor;

	@Override
	protected void initialize()
	{
		arrowCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_ARROW);
		EntityMediator.addListener(CompensationType.class, this);
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
				CompensationTypeEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Entschädigungsart", SWT.NONE);
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setCursor(arrowCursor);
		this.formToolkit.adapt(combo);

		this.type = new ComboViewer(combo);
		this.type.setContentProvider(new ArrayContentProvider());
		this.type.setLabelProvider(new LabelProvider() 
		{
			@Override
			public String getText(Object element) 
			{
				Type type = (Type) element;
				return type.label();
			}
		});
		this.type.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				CompensationTypeEditor.this.setDirty(true);
			}
		});
		this.type.setInput(CompensationType.Type.values());

		label = this.formToolkit.createLabel(composite, "Code", SWT.NONE);
		label.setLayoutData(new GridData());

		this.code = this.formToolkit.createText(composite, "");
		this.code.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.code.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CompensationTypeEditor.this.setDirty(true);
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
				CompensationTypeEditor.this.setDirty(true);
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
				CompensationTypeEditor.this.setDirty(true);
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
			CompensationTypeEditorInput input = (CompensationTypeEditorInput) this.getEditorInput();
			CompensationType compensationType = (CompensationType) input.getAdapter(CompensationType.class);
			String code = this.code.getText();
			CompensationTypeQuery query = (CompensationTypeQuery) service.getQuery(CompensationType.class);
			if (!query.isCodeUnique(code, compensationType.getId()))
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

	private Message verifyCompensationTypeSelected()
	{
		Message msg = null;

		IStructuredSelection ssel = (IStructuredSelection) type.getSelection();
		if (!(ssel.getFirstElement() instanceof CompensationType.Type))
		{
			msg = new Message(this.name, "Fehler");
			msg.setMessage("Es muss eine Entschädigungsart festgelegt werden.");
		}

		return msg;
	}

	@Override
	protected String getName()
	{
		CompensationTypeEditorInput input = (CompensationTypeEditorInput) this.getEditorInput();
		CompensationType compensationType = (CompensationType) input.getAdapter(CompensationType.class);
		return compensationType.getId() == null ? "Neu" : (compensationType.getCode().length() == 0 ? "???"
				: compensationType.getCode());
	}

	@Override
	protected String getText()
	{
		CompensationTypeEditorInput input = (CompensationTypeEditorInput) this.getEditorInput();
		CompensationType compensationType = (CompensationType) input.getAdapter(CompensationType.class);
		return compensationType.getId() == null ? "Neue Kategorie" : "Kategorie "
				+ (compensationType.getCode().length() == 0 ? compensationType.getName() : compensationType.getCode());
	}

	@Override
	protected void loadValues()
	{
		CompensationTypeEditorInput input = (CompensationTypeEditorInput) this.getEditorInput();
		CompensationType compensationType = (CompensationType) input.getAdapter(CompensationType.class);
		if (compensationType != null)
		{
			CompensationType.Type selectedType = compensationType.getType();
			if (selectedType == null)
			{
				selectedType = CompensationType.Type.SALARY;
			}
			this.type.setSelection(new StructuredSelection(new Type[] { selectedType }));
			this.code.setText(compensationType.getCode());
			this.name.setText(compensationType.getName());
			this.description.setText(compensationType.getDescription());
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		CompensationTypeEditorInput input = (CompensationTypeEditorInput) this.getEditorInput();
		CompensationType compensationType = (CompensationType) input.getAdapter(CompensationType.class);
		if (compensationType != null)
		{
			IStructuredSelection ssel = (IStructuredSelection) type.getSelection();
			compensationType.setType((CompensationType.Type) ssel.getFirstElement());
			compensationType.setCode(this.code.getText());
			compensationType.setName(this.name.getText());
			compensationType.setDescription(this.description.getText());
		}
	}

	@Override
	protected boolean validate()
	{
		Message msg = this.getUniqueCodeMessage();

		if (msg == null)
			msg = this.verifyCompensationTypeSelected();

		if (msg == null)
			msg = this.getEmptyNameMessage();

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<CompensationType> input)
	{
		return input.getAdapter(CompensationType.class) instanceof CompensationType;
	}

	@Override
	public void setFocus()
	{
		this.code.setFocus();
	}

	@Override
	public void dispose()
	{	arrowCursor.dispose();
		EntityMediator.removeListener(CompensationType.class, this);
		super.dispose();
	}
}
