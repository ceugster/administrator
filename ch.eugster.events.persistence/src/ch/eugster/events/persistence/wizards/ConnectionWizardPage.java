/*
 * Created on 13.02.2009
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.persistence.wizards;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.Preferences;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.preferences.PreferenceInitializer;

public class ConnectionWizardPage extends WizardPage implements ModifyListener, ISelectionChangedListener
{
	private Text url;

	private Label urlExample;

	private Text user;

	private Text password;

	private Text confirm;

	private ComboViewer drivers;

	private Button testConnection;

	private Label helpLabel;

	private static final String MESSAGE = "Legen Sie die Eigenschaften für die gewünschte Datenbankverbindung fest.";

	private static final String URL_ERROR = "Sie haben keine URL angegeben.";

	private static final String USER_ERROR = "Sie haben keinen Benutzernamen angegeben.";

	private static final String PASSWORD_ERROR = "Die Passwortbestätigung entspricht nicht dem Passwort.";

	private static final String DRIVER_ERROR = "Sie haben kein Datenbanksystem ausgewählt.";

	public ConnectionWizardPage(String name)
	{
		super(name);
	}

	@Override
	public void createControl(Composite parent)
	{
		setTitle("Verbindungseigenschaften");
		this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("WIZARD"));
		this.setMessage(ConnectionWizardPage.MESSAGE);
		setDescription("Geben Sie die benötigten Verbindungsdaten ein und prüfen Sie ggf. die Verbindung.");

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(2, false));

		Label label = new Label(composite, SWT.LEFT);
		label.setText("Datenbanksystem");
		label.setLayoutData(new GridData());

		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		drivers = new ComboViewer(combo);
		drivers.setContentProvider(new ArrayContentProvider());
		drivers.setLabelProvider(new DriverLabelProvider());
		drivers.setSorter(new ViewerSorter());
		drivers.setInput(DriverLabelProvider.SupportedDriver.values());
		drivers.addSelectionChangedListener(this);

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("URL");

		url = new Text(composite, SWT.BORDER | SWT.SINGLE);
		url.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		url.addModifyListener(this);

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Beispiel");

		urlExample = new Label(composite, SWT.NONE);
		urlExample.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Benutzername");

		user = new Text(composite, SWT.BORDER | SWT.SINGLE);
		user.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		user.addModifyListener(this);

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Passwort");

		password = new Text(composite, SWT.BORDER | SWT.SINGLE);
		password.setEchoChar('*');
		password.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		password.addModifyListener(this);

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Passwort bestätigen");

		confirm = new Text(composite, SWT.BORDER | SWT.SINGLE);
		confirm.setEchoChar('*');
		confirm.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		confirm.addModifyListener(this);

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("");

		testConnection = new Button(composite, SWT.PUSH);
		testConnection.setLayoutData(new GridData());
		testConnection.setText("Verbindung prüfen");
		testConnection.addSelectionListener(new SelectionListener()
		{
			public void widgetSelected(SelectionEvent e)
			{
				State state = ConnectionWizardPage.this.validateConnection();

				switch (state.ordinal())
				{
					case 0:
					{
						String message = "Die Verbindung zur Datenbank wurde erfolgreich hergestellt.";
						MessageDialog dialog = new MessageDialog(ConnectionWizardPage.this.getShell(),
								"Ungültige Verbindung", null, message, MessageDialog.INFORMATION,
								new String[] { "OK" }, 0);
						dialog.open();
						break;
					}
					case 1:
					{
						String message = "Der gewählte Treiber konnte nicht gefunden werden. Bitte setzen Sie sich mit dem Lieferanten in Verbindung.";
						MessageDialog dialog = new MessageDialog(ConnectionWizardPage.this.getShell(),
								"Treiber nicht gefunden", null, message, MessageDialog.ERROR, new String[] { "OK" }, 0);
						dialog.open();
						break;
					}
					case 2:
					{
						String message = "Entweder sind die Verbindungsangaben falsch oder die Datenbank ist nicht richtig konfiguriert. Bitte vergewissern Sie sich, dass Verbindungsdaten richtig sind und die Datenbank und der Benutzer existieren und dass der Benutzer die notwendigen Zugriffsberechtigungen besitzt.";
						MessageDialog dialog = new MessageDialog(ConnectionWizardPage.this.getShell(),
								"Ungültige Verbindung", null, message, MessageDialog.ERROR, new String[] { "OK" }, 0);
						dialog.open();
						break;
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;

		helpLabel = new Label(composite, SWT.WRAP);
		helpLabel.setLayoutData(layoutData);

		Preferences prefs = PreferenceInitializer.getServerNode();
		String selectedDriverName = prefs.get(PersistenceUnitProperties.JDBC_DRIVER,
				DriverLabelProvider.SupportedDriver.values()[0].getDriver());
		DriverLabelProvider.SupportedDriver selectedDriver = DriverLabelProvider.SupportedDriver.values()[0];
		for (DriverLabelProvider.SupportedDriver supportedDriver : DriverLabelProvider.SupportedDriver.values())
			if (supportedDriver.getDriver().equals(selectedDriverName))
			{
				selectedDriver = supportedDriver;
				break;
			}

		drivers.setSelection(new StructuredSelection(selectedDriver));
		url.setText(prefs.get(PersistenceUnitProperties.JDBC_URL, selectedDriver.getDefaultURL()));
		urlExample.setText(selectedDriver.getExampleURL());
		user.setText(prefs.get(PersistenceUnitProperties.JDBC_USER, "events"));
		password.setText(prefs.get(PersistenceUnitProperties.JDBC_PASSWORD, "events"));
		helpLabel.setText(selectedDriver.getDescription());
		checkInputs();

		setControl(composite);
	}

	private State validateConnection()
	{
		State state = State.OK;
		if (!drivers.getSelection().isEmpty())
		{
			Connection connection = null;
			try
			{
				StructuredSelection ssel = (StructuredSelection) drivers.getSelection();
				DriverLabelProvider.SupportedDriver driver = (DriverLabelProvider.SupportedDriver) ssel
						.getFirstElement();
				Class.forName(driver.getDriver());
				connection = DriverManager.getConnection(url.getText(), user.getText(), password.getText());
				if (connection != null)
					if (!connection.isClosed())
					{
						connection.close();
					}
			}
			catch (ClassNotFoundException e)
			{
				state = State.DRIVER_ERROR1;
			}
			catch (SQLException e)
			{
				state = State.CONNECTION_ERROR;
			}
			finally
			{
			}

		}
		setPageComplete(state.equals(State.OK));
		return state;
	}

	public String getUrl()
	{
		return url.getText();
	}

	public String getUser()
	{
		return user.getText();
	}

	public String getPassword()
	{
		return password.getText();
	}

	public String getDriverName()
	{
		StructuredSelection ssel = (StructuredSelection) drivers.getSelection();
		return ((DriverLabelProvider.SupportedDriver) ssel.getFirstElement()).getDriver();
	}

	public void handleEvent(Event event)
	{
		checkInputs();
	}

	private void checkInputs()
	{
		if (drivers.getSelection().isEmpty())
		{
			this.setMessage(ConnectionWizardPage.DRIVER_ERROR, IMessageProvider.ERROR);
			ConnectionWizardPage.this.setPageComplete(false);
		}
		else if (url.getText().isEmpty())
		{
			this.setMessage(ConnectionWizardPage.URL_ERROR, IMessageProvider.ERROR);
			ConnectionWizardPage.this.setPageComplete(false);
		}
		else if (user.getText().isEmpty())
		{
			this.setMessage(ConnectionWizardPage.USER_ERROR, IMessageProvider.ERROR);
			ConnectionWizardPage.this.setPageComplete(false);
		}
		else if (!password.getText().equals(confirm.getText()))
		{
			this.setMessage(ConnectionWizardPage.PASSWORD_ERROR, IMessageProvider.ERROR);
			ConnectionWizardPage.this.setPageComplete(false);
		}
		else
		{
			this.setMessage(ConnectionWizardPage.MESSAGE);
			ConnectionWizardPage.this.setPageComplete(true);
		}
	}

	@Override
	public void modifyText(ModifyEvent e)
	{
		checkInputs();
		if (isPageComplete())
		{
			validateConnection();
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event)
	{
		StructuredSelection ssel = (StructuredSelection) event.getSelection();
		if (ssel.getFirstElement() instanceof DriverLabelProvider.SupportedDriver)
		{
			DriverLabelProvider.SupportedDriver supportedDriver = (DriverLabelProvider.SupportedDriver) ssel
					.getFirstElement();
			url.setText(supportedDriver.getDefaultURL());
			urlExample.setText(supportedDriver.getExampleURL());
			setDescription(supportedDriver.getDescription());
			helpLabel.setText(getDescription());
			getShell().layout();
		}
		else
		{
			url.setText("");
			setDescription("");
			helpLabel.setText(getDescription());
		}

		checkInputs();
		if (isPageComplete())
		{
			validateConnection();
		}
	}

	private enum State
	{
		OK, DRIVER_ERROR1, CONNECTION_ERROR;
	}

}
