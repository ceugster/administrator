package ch.eugster.events.donation.editors;

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

import ch.eugster.events.donation.Activator;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.queries.DonationPurposeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class DonationPurposeEditor extends AbstractEntityEditor<DonationPurpose>
{
	public static final String ID = "ch.eugster.events.donation.purpose.editor";

	private Text code;

	private Text name;

	private Text description;

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(DonationPurpose.class, this);
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
				DonationPurposeEditor.this.scrolledForm.reflow(true);
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
			@Override
			public void modifyText(ModifyEvent e)
			{
				DonationPurposeEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.name = this.formToolkit.createText(composite, "");
		this.name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.name.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				DonationPurposeEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Beschreibung", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 124;

		this.description = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL);
		this.description.setLayoutData(gridData);
		this.description.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				DonationPurposeEditor.this.setDirty(true);
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
				DonationPurposeEditorInput input = (DonationPurposeEditorInput) this.getEditorInput();
				DonationPurpose purpose = (DonationPurpose) input.getAdapter(DonationPurpose.class);
				String code = this.code.getText();
				DonationPurposeQuery query = (DonationPurposeQuery) service.getQuery(DonationPurpose.class);
				if (!query.isCodeUnique(code, purpose.getId()))
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
			msg.setMessage("Die Domäne muss eine Bezeichnung haben.");
		}

		return msg;
	}

	@Override
	protected String getName()
	{
		DonationPurposeEditorInput input = (DonationPurposeEditorInput) this.getEditorInput();
		DonationPurpose purpose = (DonationPurpose) input.getAdapter(DonationPurpose.class);
		return purpose.getId() == null ? "Neu" : (purpose.getCode().length() == 0 ? "???" : purpose.getCode());
	}

	@Override
	protected String getText()
	{
		DonationPurposeEditorInput input = (DonationPurposeEditorInput) this.getEditorInput();
		DonationPurpose purpose = (DonationPurpose) input.getAdapter(DonationPurpose.class);
		return purpose.getId() == null ? "Neuer Spendenzweck" : "Spendenzweck "
				+ (purpose.getCode().length() == 0 ? purpose.getName() : purpose.getCode());
	}

	@Override
	protected void loadValues()
	{
		DonationPurposeEditorInput input = (DonationPurposeEditorInput) this.getEditorInput();
		DonationPurpose purpose = (DonationPurpose) input.getAdapter(DonationPurpose.class);
		if (purpose != null)
		{
			this.code.setText(purpose.getCode());
			this.name.setText(purpose.getName());
			this.description.setText(purpose.getDescription());
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		DonationPurposeEditorInput input = (DonationPurposeEditorInput) this.getEditorInput();
		DonationPurpose purpose = (DonationPurpose) input.getAdapter(DonationPurpose.class);
		if (purpose != null)
		{
			purpose.setCode(this.code.getText());
			purpose.setName(this.name.getText());
			purpose.setDescription(this.description.getText());
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
	protected boolean validateType(AbstractEntityEditorInput<DonationPurpose> input)
	{
		return input.getAdapter(DonationPurpose.class) instanceof DonationPurpose;
	}

	@Override
	public void setFocus()
	{
		this.code.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(DonationPurpose.class, this);
	}
}
