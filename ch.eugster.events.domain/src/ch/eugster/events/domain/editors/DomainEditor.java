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

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(Domain.class, this);
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
				DomainEditor.this.scrolledForm.reflow(true);
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
			public void modifyText(ModifyEvent e)
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
			public void modifyText(ModifyEvent e)
			{
				DomainEditor.this.setDirty(true);
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
	protected boolean validateType(AbstractEntityEditorInput<Domain> input)
	{
		return input.getAdapter(Domain.class) instanceof Domain;
	}

	@Override
	public void setFocus()
	{
		this.code.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Domain.class, this);
	}
}
