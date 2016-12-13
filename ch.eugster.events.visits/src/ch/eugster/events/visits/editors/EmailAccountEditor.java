package ch.eugster.events.visits.editors;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import org.eclipse.ui.progress.UIJob;

import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.EmailAccount;
import ch.eugster.events.ui.Activator;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class EmailAccountEditor extends AbstractEntityEditor<EmailAccount>
{
	public static final String ID = "ch.eugster.events.visits.email.editor";

	private ComboViewer typeViewer;

	private Text username;

	private Text password;

	private Text host;

	private Spinner port;

	private Button auth;

	private Button starttls;

	private Button ssl;

	private void checkAccount()
	{
		if (this.username.getText().isEmpty())
		{
			MessageDialog.openWarning(this.getSite().getShell(), "Benutzername fehlt",
					"Sie haben keinen Benutzernamen eingegeben.");
			return;
		}
		if (typeViewer.getSelection().isEmpty())
		{
			MessageDialog.openWarning(this.getSite().getShell(), "Angabe des Kontotyps fehlt",
					"Sie haben keinen Kontotyp ausgewählt.");
			return;
		}
		if (this.host.getText().isEmpty())
		{
			MessageDialog.openWarning(this.getSite().getShell(), "Host fehlt", "Sie haben keinen Host eingegeben.");
			return;
		}
		if (this.port.getSelection() == 0)
		{
			MessageDialog.openWarning(this.getSite().getShell(), "Port fehlt", "Sie haben keinen Port eingegeben.");
			return;
		}

		UIJob job = new UIJob("Stelle Verbindung mit " + username.getText() + " her...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				monitor.beginTask("Stelle Verbindung mit " + username.getText() + " her...", IProgressMonitor.UNKNOWN);
				IStatus status = Status.OK_STATUS;
				StructuredSelection ssel = (StructuredSelection) typeViewer.getSelection();
				EmailAccount.Type type = (EmailAccount.Type) ssel.getFirstElement();
				Properties props = new Properties();
				props.put(type.hostKey(), host.getText());
				props.put(type.authKey(), auth.getSelection());
				props.put(type.portKey(), String.valueOf(port.getSelection()));
				if (starttls.getSelection())
				{
					props.put(type.starttlsKey(), String.valueOf(starttls.getSelection()));
				}
				if (ssl.getSelection())
				{
					props.put(type.sslKey(), String.valueOf(ssl.getSelection()));
				}

				Authenticator authenticator = new Authenticator()
				{

					@Override
					protected PasswordAuthentication getPasswordAuthentication()
					{
						return new PasswordAuthentication(username.getText(), password.getText());
					}

				};
				Session session = Session.getInstance(props, authenticator);

				try
				{
					Store store = session.getStore(type.storeKey());
					store.connect(props.getProperty(type.hostKey()), username.getText(), password.getText());
					store.close();
				}
				catch (NoSuchProviderException e)
				{
					e.printStackTrace();
					status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
				}
				catch (MessagingException e)
				{
					e.printStackTrace();
					status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
				}
				finally
				{
					monitor.done();
				}
				return status;
			}
		};
		job.addJobChangeListener(new JobChangeAdapter()
		{
			@Override
			public void done(final IJobChangeEvent event)
			{
				if (event.getResult().isOK())
				{
					MessageDialog.openInformation(getSite().getShell(), "Verbindung hergestellt",
							"Die Verbindung konnte erfolgreich hergestellt werden.");
				}
				else
				{
					ErrorDialog
							.openError(
									getSite().getShell(),
									"Fehler",
									"Beim Versuch, eine Verbindung mit dem Emailkonto herzustellen, ist ein Fehler aufgetreten.",
									event.getResult());
				}
			}
		});
		job.setSystem(true);
		job.schedule();
	}

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
		section.setText("Eigenschaften");
		section.setClient(this.fillSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				EmailAccountEditor.this.scrolledForm.reflow(true);
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
	}

	private Control fillSection(final Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		final Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Benutzername", SWT.NONE);
		label.setLayoutData(new GridData());

		this.username = this.formToolkit.createText(composite, "");
		this.username.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.username.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				EmailAccountEditor.this.setDirty(true);
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
				EmailAccountEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Typ", SWT.NONE);
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

		this.typeViewer = new ComboViewer(combo);
		this.typeViewer.setContentProvider(new ArrayContentProvider());
		this.typeViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(final Object element)
			{
				if (element instanceof EmailAccount.Type)
				{
					EmailAccount.Type type = (EmailAccount.Type) element;
					return type.label();
				}
				return "";
			}
		});
		this.typeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				EmailAccountEditor.this.setDirty(true);
			}
		});
		this.typeViewer.setInput(EmailAccount.Type.values());

		label = this.formToolkit.createLabel(composite, "Host", SWT.NONE);
		label.setLayoutData(new GridData());

		this.host = this.formToolkit.createText(composite, "");
		this.host.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.host.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				EmailAccountEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Port", SWT.NONE);
		label.setLayoutData(new GridData());

		this.port = new Spinner(composite, SWT.WRAP);
		this.port.setLayoutData(new GridData());
		this.port.setMaximum(Integer.MAX_VALUE);
		this.port.setMinimum(0);
		this.port.setIncrement(1);
		this.port.setPageIncrement(10);
		this.port.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				EmailAccountEditor.this.setDirty(true);
			}
		});
		this.port.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				EmailAccountEditor.this.setDirty(true);
			}
		});
		this.port.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		this.formToolkit.adapt(port);

		label = this.formToolkit.createLabel(composite, "");
		label.setLayoutData(new GridData());

		auth = this.formToolkit.createButton(composite, "Authentifizierung erforderlich", SWT.CHECK);
		auth.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		auth.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				EmailAccountEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "");
		label.setLayoutData(new GridData());

		starttls = this.formToolkit.createButton(composite, "STARTTLS", SWT.CHECK);
		starttls.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		starttls.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				EmailAccountEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "");
		label.setLayoutData(new GridData());

		ssl = this.formToolkit.createButton(composite, "SSL/TLS", SWT.CHECK);
		ssl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ssl.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				EmailAccountEditor.this.setDirty(true);
			}
		});

		Button test = this.formToolkit.createButton(composite, "Verbindung testen", SWT.PUSH);
		test.setLayoutData(new GridData());
		test.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				checkAccount();
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	@Override
	protected Message getMessage(final PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		return msg;
	}

	@Override
	protected String getName()
	{
		EmailAccountEditorInput input = (EmailAccountEditorInput) this.getEditorInput();
		EmailAccount account = (EmailAccount) input.getAdapter(EmailAccount.class);
		return EmailAccount.stringValueOf(account.getUsername()).isEmpty() ? "Neu" : account.getUsername();
	}

	@Override
	protected String getText()
	{
		EmailAccountEditorInput input = (EmailAccountEditorInput) this.getEditorInput();
		EmailAccount account = (EmailAccount) input.getAdapter(EmailAccount.class);
		return EmailAccount.stringValueOf(account.getUsername()).isEmpty() ? "Neu" : account.getUsername();
	}

	@Override
	protected void initialize()
	{
	}

	@Override
	protected void loadValues()
	{
		EmailAccountEditorInput input = (EmailAccountEditorInput) this.getEditorInput();
		EmailAccount account = (EmailAccount) input.getAdapter(EmailAccount.class);
		this.username.setText(EmailAccount.stringValueOf(account.getUsername()));
		this.password.setText(EmailAccount.stringValueOf(account.getPassword()));
		this.host.setText(EmailAccount.stringValueOf(account.getHost()));
		this.port.setSelection(account.getPort());
		this.auth.setSelection(account.isAuth());
		this.starttls.setSelection(account.isStarttlsEnable());
		this.ssl.setSelection(account.isSslEnable());
		if (account.getType() != null)
		{
			this.typeViewer.setSelection(new StructuredSelection(new EmailAccount.Type[] { account.getType() }));
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		EmailAccountEditorInput input = (EmailAccountEditorInput) this.getEditorInput();
		EmailAccount account = (EmailAccount) input.getAdapter(EmailAccount.class);
		account.setUsername(this.username.getText().isEmpty() ? null : this.username.getText());
		account.setPassword(this.password.getText().isEmpty() ? null : this.password.getText());
		account.setHost(this.host.getText().isEmpty() ? null : this.host.getText());
		account.setPort(this.port.getSelection());
		account.setAuth(this.auth.getSelection());
		account.setStarttlsEnable(this.starttls.getSelection());
		account.setSslEnable(this.ssl.getSelection());
		StructuredSelection ssel = (StructuredSelection) this.typeViewer.getSelection();
		account.setType(ssel.isEmpty() ? null : (EmailAccount.Type) ssel.getFirstElement());
	}

	@Override
	public void setFocus()
	{
		this.username.setFocus();
	}

	@Override
	protected boolean validate()
	{
		Message msg = null;

		return msg == null;
	}

	@Override
	protected boolean validateType(final AbstractEntityEditorInput<EmailAccount> input)
	{
		return input.getAdapter(EmailAccount.class) instanceof EmailAccount;
	}
}
