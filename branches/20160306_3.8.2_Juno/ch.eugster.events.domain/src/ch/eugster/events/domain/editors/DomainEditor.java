package ch.eugster.events.domain.editors;

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

import ch.eugster.events.domain.Activator;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class DomainEditor extends AbstractEntityEditor<Domain>
{
	public static final String ID = "ch.eugster.events.domain.editor";

	private Text code;

	private Text name;

	private Text description;

	private Text organization;

	private Text address;

	private Text city;

	private Text phone;

	private Text fax;

	private Text email;

	private Text website;

	private void createDomainSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Domäne");
		section.setClient(this.fillDomainSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				DomainEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private void createOrganizationSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Organisation");
		section.setClient(this.fillOrganizationSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				DomainEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	@Override
	protected void createSections(final ScrolledForm parent)
	{
		this.createDomainSection(parent);
		this.createOrganizationSection(parent);
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Domain.class, this);
	}

	private Control fillDomainSection(final Section parent)
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
			public void modifyText(final ModifyEvent e)
			{
				if (DomainEditor.this.name.getText().isEmpty()
						|| DomainEditor.this.name.getText().equals(
								DomainEditor.this.code.getText().substring(0,
										DomainEditor.this.code.getText().length() - 1)))
					DomainEditor.this.name.setText(DomainEditor.this.code.getText());
				DomainEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.name = this.formToolkit.createText(composite, "");
		this.name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.name.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DomainEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Beschreibung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.description = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL);
		this.description.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.description.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DomainEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillOrganizationSection(final Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Name", SWT.NONE);
		label.setLayoutData(new GridData());

		this.organization = this.formToolkit.createText(composite, "");
		this.organization.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.organization.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DomainEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Strasse", SWT.NONE);
		label.setLayoutData(new GridData());

		this.address = this.formToolkit.createText(composite, "");
		this.address.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.address.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DomainEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Ort", SWT.NONE);
		label.setLayoutData(new GridData());

		this.city = this.formToolkit.createText(composite, "");
		this.city.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.city.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DomainEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Telefon", SWT.NONE);
		label.setLayoutData(new GridData());

		this.phone = this.formToolkit.createText(composite, "");
		this.phone.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.phone.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DomainEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Fax", SWT.NONE);
		label.setLayoutData(new GridData());

		this.fax = this.formToolkit.createText(composite, "");
		this.fax.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.fax.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DomainEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Email", SWT.NONE);
		label.setLayoutData(new GridData());

		this.email = this.formToolkit.createText(composite, "");
		this.email.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.email.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DomainEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Website", SWT.NONE);
		label.setLayoutData(new GridData());

		this.website = this.formToolkit.createText(composite, "");
		this.website.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.website.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DomainEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
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
	protected Message getMessage(final PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		if (errorCode.equals(""))
		{
			msg = this.getUniqueCodeMessage();
		}
		return msg;
	}

	@Override
	protected String getName()
	{
		DomainEditorInput input = (DomainEditorInput) this.getEditorInput();
		Domain domain = (Domain) input.getAdapter(Domain.class);
		return domain.getId() == null ? "Neu" : (domain.getCode().length() == 0 ? "???" : domain.getCode());
	}

	@Override
	protected String getText()
	{
		DomainEditorInput input = (DomainEditorInput) this.getEditorInput();
		Domain domain = (Domain) input.getAdapter(Domain.class);
		return domain.getId() == null ? "Neue Domäne" : "Domäne "
				+ (domain.getCode().length() == 0 ? domain.getName() : domain.getCode());
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
			DomainEditorInput input = (DomainEditorInput) this.getEditorInput();
			Domain domain = (Domain) input.getAdapter(Domain.class);
			String code = this.code.getText();
			DomainQuery query = (DomainQuery) service.getQuery(Domain.class);
			if (!query.isCodeUnique(code, domain.getId()))
			{
				msg = new Message(this.code, "Ungültiger Code");
				msg.setMessage("Der gewählte Code wird bereits verwendet.");
				return msg;
			}
		}
		tracker.close();

		return msg;
	}

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(Domain.class, this);
	}

	@Override
	protected void loadValues()
	{
		DomainEditorInput input = (DomainEditorInput) this.getEditorInput();
		Domain domain = (Domain) input.getAdapter(Domain.class);
		if (domain != null)
		{
			this.code.setText(domain.getCode());
			this.name.setText(domain.getName());
			this.description.setText(domain.getDescription());
			this.organization.setText(domain.getOrganization());
			this.address.setText(domain.getAddress());
			this.city.setText(domain.getCity());
			this.phone.setText(domain.getPhone());
			this.fax.setText(domain.getFax());
			this.email.setText(domain.getEmail());
			this.website.setText(domain.getWebsite());
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		DomainEditorInput input = (DomainEditorInput) this.getEditorInput();
		Domain domain = (Domain) input.getAdapter(Domain.class);
		if (domain != null)
		{
			domain.setCode(this.code.getText());
			domain.setName(this.name.getText());
			domain.setDescription(this.description.getText());
			domain.setOrganization(this.organization.getText());
			domain.setAddress(this.address.getText());
			domain.setCity(this.city.getText());
			domain.setPhone(this.phone.getText());
			domain.setFax(this.fax.getText());
			domain.setEmail(this.email.getText());
			domain.setWebsite(this.website.getText());
		}
	}

	@Override
	public void setFocus()
	{
		this.code.setFocus();
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
	protected boolean validateType(final AbstractEntityEditorInput<Domain> input)
	{
		return input.getAdapter(Domain.class) instanceof Domain;
	}
}
