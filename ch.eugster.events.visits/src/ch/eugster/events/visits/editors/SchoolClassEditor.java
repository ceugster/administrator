package ch.eugster.events.visits.editors;

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
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.SchoolClass;
import ch.eugster.events.persistence.model.SchoolClass.Level;
import ch.eugster.events.persistence.queries.SchoolClassQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;
import ch.eugster.events.visits.Activator;

public class SchoolClassEditor extends AbstractEntityEditor<SchoolClass>
{
	public static final String ID = "ch.eugster.events.visits.schoolclass.editor";

	private ComboViewer levelViewer;

	private Text name;

	@Override
	protected void initialize()
	{
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
		section.setText("Schulklasse");
		section.setClient(this.fillSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				SchoolClassEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.name = this.formToolkit.createText(composite, "");
		this.name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.name.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				SchoolClassEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Auswertungsstufe", SWT.NONE);
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

		this.levelViewer = new ComboViewer(combo);
		this.levelViewer.setContentProvider(new ArrayContentProvider());
		this.levelViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof Level)
				{
					Level level = (Level) element;
					return level.label();
				}
				return "";
			}
		});
		this.levelViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				SchoolClassEditor.this.setDirty(true);
			}
		});
		this.levelViewer.setInput(SchoolClass.Level.values());

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	@Override
	protected Message getMessage(PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		if (errorCode.equals(""))
		{
			msg = this.getUniqueNameMessage();
		}
		return msg;
	}

	private Message getUniqueNameMessage()
	{
		Message msg = null;

		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				SchoolClassEditorInput input = (SchoolClassEditorInput) this.getEditorInput();
				SchoolClass schoolClass = (SchoolClass) input.getAdapter(SchoolClass.class);
				SchoolClassQuery query = (SchoolClassQuery) service.getQuery(SchoolClass.class);
				if (!query.isNameUnique(name.getText(), schoolClass.getId()))
				{
					msg = new Message(this.name, "Bezeichnung vorhanden", "Die Bezeichnung " + name.getText()
							+ " ist bereits vorhanden. Bitte wählen Sie eine Bezeichnung, die noch nicht vorhanden ist.");
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

		if (name.getText().isEmpty())
		{
			msg = new Message(this.name, "Bezeichnung fehlt", "Die Bezeichnung darf nicht leer sein.");
		}
		return msg;
	}

	private Message getMissingLevelMessage()
	{
		Message msg = null;

		if (levelViewer.getSelection().isEmpty())
		{
			msg = new Message(this.levelViewer.getControl(), "Auswertungsstufe fehlt",
					"Die Auswertungsstufe darf nicht leer sein.");
		}
		return msg;
	}

	@Override
	protected String getName()
	{
		SchoolClassEditorInput input = (SchoolClassEditorInput) this.getEditorInput();
		SchoolClass schoolClass = (SchoolClass) input.getAdapter(SchoolClass.class);
		return SchoolClass.stringValueOf(schoolClass.getName()).isEmpty() ? "Neu" : schoolClass.getName();
	}

	@Override
	protected String getText()
	{
		SchoolClassEditorInput input = (SchoolClassEditorInput) this.getEditorInput();
		SchoolClass schoolClass = (SchoolClass) input.getAdapter(SchoolClass.class);
		return SchoolClass.stringValueOf(schoolClass.getName()).isEmpty() ? "Neu" : schoolClass.getName();
	}

	@Override
	protected void loadValues()
	{
		SchoolClassEditorInput input = (SchoolClassEditorInput) this.getEditorInput();
		SchoolClass schoolClass = (SchoolClass) input.getAdapter(SchoolClass.class);
		this.name.setText(SchoolClass.stringValueOf(schoolClass.getName()));
		if (schoolClass.getLevel() != null)
		{
			this.levelViewer.setSelection(new StructuredSelection(new Level[] { schoolClass.getLevel() }));
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		SchoolClassEditorInput input = (SchoolClassEditorInput) this.getEditorInput();
		SchoolClass schoolClass = (SchoolClass) input.getAdapter(SchoolClass.class);
		schoolClass.setName(this.name.getText().isEmpty() ? null : this.name.getText());
		StructuredSelection ssel = (StructuredSelection) this.levelViewer.getSelection();
		schoolClass.setLevel(ssel.isEmpty() ? null : (Level) ssel.getFirstElement());
	}

	@Override
	protected boolean validate()
	{
		Message msg = this.getUniqueNameMessage();

		if (msg == null)
			msg = this.getEmptyNameMessage();

		if (msg == null)
			msg = this.getMissingLevelMessage();

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<SchoolClass> input)
	{
		return input.getAdapter(SchoolClass.class) instanceof SchoolClass;
	}

	@Override
	public void setFocus()
	{
		this.name.setFocus();
	}

	@Override
	public void dispose()
	{
	}
}
