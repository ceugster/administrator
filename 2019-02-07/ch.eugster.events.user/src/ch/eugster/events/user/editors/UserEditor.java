package ch.eugster.events.user.editors;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
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

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;
import ch.eugster.events.user.Activator;

public class UserEditor extends AbstractEntityEditor<User>
{
	public static final String ID = "ch.eugster.events.user.editor";

	private Text fullname;

	private Text username;

	private Text password;

	private ComboViewer stateViewer;

	private ComboViewer domainViewer;

	private Spinner minEditorColumns;

	private Spinner maxEditorColumns;

	private void createSection(final ScrolledForm parent)
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
			public void expansionStateChanged(final ExpansionEvent e)
			{
				UserEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	@Override
	protected void createSections(final ScrolledForm parent)
	{
		this.createSection(parent);
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(User.class, this);
	}

	private Control fillSection(final Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Benutzer", SWT.NONE);
		label.setLayoutData(new GridData());

		this.fullname = this.formToolkit.createText(composite, "");
		this.fullname.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.fullname.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				UserEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Anmeldename", SWT.NONE);
		label.setLayoutData(new GridData());

		this.username = this.formToolkit.createText(composite, "");
		this.username.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.username.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				UserEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Passwort", SWT.NONE);
		label.setLayoutData(new GridData());

		this.password = this.formToolkit.createText(composite, "");
		this.password.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.password.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				UserEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Status", SWT.NONE);
		label.setLayoutData(new GridData());

		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.FLAT);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.stateViewer = new ComboViewer(combo);
		this.stateViewer.setContentProvider(new ArrayContentProvider());
		this.stateViewer.setLabelProvider(new StateLabelProvider());
		this.stateViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				UserEditor.this.setDirty(true);
			}
		});
		this.stateViewer.setInput(User.UserStatus.values());

		label = this.formToolkit.createLabel(composite, "Standarddomäne", SWT.NONE);
		label.setLayoutData(new GridData());

		combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.FLAT);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.domainViewer = new ComboViewer(combo);
		this.domainViewer.setContentProvider(new ArrayContentProvider());
		this.domainViewer.setLabelProvider(new DomainLabelProvider());
		this.domainViewer.setSorter(new DomainSorter());
		this.domainViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				UserEditor.this.setDirty(true);
			}
		});

		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				DomainQuery query = (DomainQuery) service.getQuery(Domain.class);
				List<Domain> domains = query.selectAll();
				domains.add(Domain.newInstance());
				this.domainViewer.setInput(domains.toArray(new Domain[0]));
			}
		}
		finally
		{
			tracker.close();
		}
		label = this.formToolkit.createLabel(parent, "Mindestzahl Spalten im Editor");
		label.setLayoutData(new GridData());

		minEditorColumns = new Spinner(parent, SWT.NONE);
		minEditorColumns.setDigits(0);
		minEditorColumns.setIncrement(1);
		minEditorColumns.setMaximum(9);
		minEditorColumns.setMinimum(1);
		minEditorColumns.setPageIncrement(3);
		minEditorColumns.setLayoutData(new GridData());
		minEditorColumns.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.formToolkit.adapt(minEditorColumns);

		label = this.formToolkit.createLabel(parent, "Maximalzahl Spalten im Editor");
		label.setLayoutData(new GridData());

		maxEditorColumns = new Spinner(parent, SWT.NONE);
		maxEditorColumns.setDigits(0);
		maxEditorColumns.setIncrement(1);
		maxEditorColumns.setMaximum(9);
		maxEditorColumns.setMinimum(1);
		maxEditorColumns.setPageIncrement(3);
		maxEditorColumns.setLayoutData(new GridData());
		maxEditorColumns.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.formToolkit.adapt(maxEditorColumns);

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Message getEmptyUsernameMessage()
	{
		Message msg = null;

		if (this.username.getText().isEmpty())
		{
			msg = new Message(this.username, "Ungültiger Anmeldename");
			msg.setMessage("Sie müssen einen Anmeldenamen festlegen.");
			return msg;
		}

		return msg;
	}

	@Override
	protected Message getMessage(final PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		if (errorCode.equals(""))
		{
		}
		return msg;
	}

	@Override
	protected String getName()
	{
		UserEditorInput input = (UserEditorInput) this.getEditorInput();
		User user = (User) input.getAdapter(User.class);
		return user.getId() == null ? "Neu" : (user.getUsername().length() == 0 ? "???" : user.getUsername());
	}

	@Override
	protected String getText()
	{
		UserEditorInput input = (UserEditorInput) this.getEditorInput();
		User user = (User) input.getAdapter(User.class);
		return user.getId() == null ? "Neuer Benutzer" : "Benutzer "
				+ (user.getFullname().length() == 0 ? user.getUsername() : user.getFullname());
	}

	private Message getUniqueUsernameMessage()
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
				UserEditorInput input = (UserEditorInput) this.getEditorInput();
				User user = (User) input.getAdapter(User.class);
				String code = this.username.getText();
				UserQuery query = (UserQuery) service.getQuery(User.class);
				if (!query.isUsernameUnique(code, user.getId()))
				{
					msg = new Message(this.username, "Ungültiger Benutzername");
					msg.setMessage("Der gewählte Benutzername wird bereits verwendet.");
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

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(User.class, this);
	}

	@Override
	protected void loadValues()
	{
		UserEditorInput input = (UserEditorInput) this.getEditorInput();
		User user = input.getEntity();
		this.fullname.setText(user.getFullname());
		this.username.setText(user.getUsername());
		this.password.setText(user.getPassword());
		this.stateViewer.setSelection(new StructuredSelection(user.getState()));
		if (user.getDomain() == null)
			this.domainViewer.setSelection(new StructuredSelection(Domain.newInstance()));
		else
			this.domainViewer.setSelection(new StructuredSelection(user.getDomain()));
		minEditorColumns.setSelection(input.getEntity().getMinEditorColumns());
		maxEditorColumns.setSelection(input.getEntity().getMaxEditorColumns());
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		UserEditorInput input = (UserEditorInput) this.getEditorInput();
		User user = input.getEntity();
		user.setFullname(this.fullname.getText());
		user.setUsername(this.username.getText());
		user.setPassword(this.password.getText());
		StructuredSelection ssel = (StructuredSelection) this.stateViewer.getSelection();
		user.setState((User.UserStatus) ssel.getFirstElement());
		ssel = (StructuredSelection) this.domainViewer.getSelection();
		Domain domain = ssel.isEmpty() ? null : (Domain) ssel.getFirstElement();
		user.setDomain(domain == null || domain.getId() == null ? null : (Domain) ssel.getFirstElement());
		user.setMinEditorColumns(minEditorColumns.getSelection());
		user.setMaxEditorColumns(maxEditorColumns.getSelection());
	}

	@Override
	public void setFocus()
	{
		this.fullname.setFocus();
	}

	@Override
	protected boolean validate()
	{
		Message msg = this.getUniqueUsernameMessage();

		if (msg == null)
			msg = this.getEmptyUsernameMessage();

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(final AbstractEntityEditorInput<User> input)
	{
		return input.getAdapter(User.class) instanceof User;
	}
}
