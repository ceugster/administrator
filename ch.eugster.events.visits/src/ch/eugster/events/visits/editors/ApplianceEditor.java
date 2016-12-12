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
import ch.eugster.events.persistence.model.Appliance;
import ch.eugster.events.persistence.queries.ApplianceQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;
import ch.eugster.events.visits.Activator;

public class ApplianceEditor extends AbstractEntityEditor<Appliance>
{
	public static final String ID = "ch.eugster.events.visits.appliance.editor";

	private Text name;

	private Text description;

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
		section.setText("Gerät");
		section.setClient(this.fillSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				ApplianceEditor.this.scrolledForm.reflow(true);
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
				ApplianceEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Beschreibung", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 64;

		this.description = this.formToolkit.createText(composite, "", SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		this.description.setLayoutData(gridData);
		this.description.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				ApplianceEditor.this.setDirty(true);
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
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				ApplianceEditorInput input = (ApplianceEditorInput) this.getEditorInput();
				Appliance appliance = (Appliance) input.getAdapter(Appliance.class);
				ApplianceQuery query = (ApplianceQuery) service.getQuery(Appliance.class);
				if (!query.isNameUnique(name.getText(), appliance.getId()))
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

	@Override
	protected String getName()
	{
		ApplianceEditorInput input = (ApplianceEditorInput) this.getEditorInput();
		return input.getName();
	}

	@Override
	protected String getText()
	{
		ApplianceEditorInput input = (ApplianceEditorInput) this.getEditorInput();
		return input.getToolTipText();
	}

	@Override
	protected void loadValues()
	{
		ApplianceEditorInput input = (ApplianceEditorInput) this.getEditorInput();
		Appliance appliance = (Appliance) input.getAdapter(Appliance.class);
		this.name.setText(Appliance.stringValueOf(appliance.getName()));
		this.description.setText(Appliance.stringValueOf(appliance.getDescription()));
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		ApplianceEditorInput input = (ApplianceEditorInput) this.getEditorInput();
		Appliance appliance = (Appliance) input.getAdapter(Appliance.class);
		appliance.setName(this.name.getText().isEmpty() ? null : this.name.getText());
		appliance.setDescription(this.description.getText().isEmpty() ? null : this.description.getText());
	}

	@Override
	protected boolean validate()
	{
		Message msg = this.getUniqueNameMessage();

		if (msg == null)
			msg = this.getEmptyNameMessage();

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<Appliance> input)
	{
		return input.getAdapter(Appliance.class) instanceof Appliance;
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
