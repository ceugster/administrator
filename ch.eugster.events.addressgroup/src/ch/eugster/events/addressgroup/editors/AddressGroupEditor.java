package ch.eugster.events.addressgroup.editors;

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

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.queries.AddressGroupQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class AddressGroupEditor extends AbstractEntityEditor<AddressGroup>
{
	public static final String ID = "ch.eugster.events.addressgroup.editor.group";

	private Text code;

	private Text name;

	private Text description;

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(AddressGroupCategory.class, this);
		EntityMediator.addListener(AddressGroup.class, this);
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
				AddressGroupEditor.this.scrolledForm.reflow(true);
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
				String name = AddressGroupEditor.this.name.getText();
				String code = AddressGroupEditor.this.code.getText();
				int min = Math.min(name.length(), code.length());
				if (name.length() == 0 || (min > 0 && name.substring(0, min - 1).equals(code.substring(0, min - 1))))
				{
					AddressGroupEditor.this.name.setText(AddressGroupEditor.this.code.getText());
				}
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
				AddressGroupEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Beschreibung", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.minimumHeight = 126;

		this.description = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL);
		this.description.setLayoutData(layoutData);
		this.description.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				AddressGroupEditor.this.setDirty(true);
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

		ServiceTracker connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle()
				.getBundleContext(), ConnectionService.class.getName(), null);
		connectionServiceTracker.open();

		ConnectionService con = (ConnectionService) connectionServiceTracker.getService();
		if (con != null)
		{
			AddressGroupEditorInput input = (AddressGroupEditorInput) this.getEditorInput();
			AddressGroup addressGroup = input.getEntity();
			String code = this.code.getText();
			AddressGroupQuery query = (AddressGroupQuery) con.getQuery(AddressGroup.class);
			if (!query.isCodeUnique(code, addressGroup.getId()))
			{
				msg = new Message(this.name, "Ungültiger Code");
				msg.setMessage("Der gewählte Code wird bereits verwendet.");
				return msg;
			}
		}
		connectionServiceTracker.close();

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
		AddressGroupEditorInput input = (AddressGroupEditorInput) this.getEditorInput();
		AddressGroup addressGroup = input.getEntity();
		return addressGroup.getId() == null ? "Neu" : (addressGroup.getCode().length() == 0 ? "???" : addressGroup
				.getCode());
	}

	@Override
	protected String getText()
	{
		AddressGroupEditorInput input = (AddressGroupEditorInput) this.getEditorInput();
		AddressGroup addressGroup = input.getEntity();
		return addressGroup.getId() == null ? "Neue Adressgruppe" : "Adressgruppe "
				+ (addressGroup.getCode().length() == 0 ? addressGroup.getName() : addressGroup.getCode());
	}

	@Override
	protected void loadValues()
	{
		AddressGroupEditorInput input = (AddressGroupEditorInput) this.getEditorInput();
		AddressGroup addressGroup = input.getEntity();
		if (addressGroup != null)
		{
			this.code.setText(addressGroup.getCode());
			this.name.setText(addressGroup.getName());
			this.description.setText(addressGroup.getDescription());
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		AddressGroupEditorInput input = (AddressGroupEditorInput) this.getEditorInput();
		AddressGroup addressGroup = input.getEntity();
		if (addressGroup != null)
		{
			addressGroup.setCode(this.code.getText());
			addressGroup.setName(this.name.getText());
			addressGroup.setDescription(this.description.getText());
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
	protected boolean validateType(AbstractEntityEditorInput<AddressGroup> input)
	{
		return input.getAdapter(AddressGroup.class) instanceof AddressGroup;
	}

	@Override
	public void setFocus()
	{
		this.code.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(AddressGroupCategory.class, this);
		EntityMediator.removeListener(AddressGroup.class, this);
	}
}
