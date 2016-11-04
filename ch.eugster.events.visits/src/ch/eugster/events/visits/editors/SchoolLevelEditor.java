package ch.eugster.events.visits.editors;

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

import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.SchoolLevel;
import ch.eugster.events.persistence.queries.SchoolLevelQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;
import ch.eugster.events.visits.Activator;

public class SchoolLevelEditor extends AbstractEntityEditor<SchoolLevel>
{
	public static final String ID = "ch.eugster.events.visits.schoollevel.editor";

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
				SchoolLevelEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Auswertungsstufe", SWT.NONE);
		label.setLayoutData(new GridData());

		this.name = this.formToolkit.createText(composite, "");
		this.name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.name.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				SchoolLevelEditor.this.setDirty(true);
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
				SchoolLevelEditorInput input = (SchoolLevelEditorInput) this.getEditorInput();
				SchoolLevel schoolLevel = (SchoolLevel) input.getAdapter(SchoolLevel.class);
				SchoolLevelQuery query = (SchoolLevelQuery) service.getQuery(SchoolLevel.class);
				if (!query.isNameUnique(name.getText(), schoolLevel.getId()))
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

		return msg;
	}

	@Override
	protected String getName()
	{
		SchoolLevelEditorInput input = (SchoolLevelEditorInput) this.getEditorInput();
		SchoolLevel schoolLevel = (SchoolLevel) input.getAdapter(SchoolLevel.class);
		return SchoolLevel.stringValueOf(schoolLevel.getName()).isEmpty() ? "Neu" : schoolLevel.getName();
	}

	@Override
	protected String getText()
	{
		SchoolLevelEditorInput input = (SchoolLevelEditorInput) this.getEditorInput();
		SchoolLevel schoolLevel = (SchoolLevel) input.getAdapter(SchoolLevel.class);
		return SchoolLevel.stringValueOf(schoolLevel.getName()).isEmpty() ? "Neu" : schoolLevel.getName();
	}

	@Override
	protected void loadValues()
	{
		SchoolLevelEditorInput input = (SchoolLevelEditorInput) this.getEditorInput();
		SchoolLevel schoolLevel = (SchoolLevel) input.getAdapter(SchoolLevel.class);
		this.name.setText(SchoolLevel.stringValueOf(schoolLevel.getName()));
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		SchoolLevelEditorInput input = (SchoolLevelEditorInput) this.getEditorInput();
		SchoolLevel schoolLevel = (SchoolLevel) input.getAdapter(SchoolLevel.class);
		schoolLevel.setName(this.name.getText().isEmpty() ? null : this.name.getText());
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
	protected boolean validateType(AbstractEntityEditorInput<SchoolLevel> input)
	{
		return input.getAdapter(SchoolLevel.class) instanceof SchoolLevel;
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
