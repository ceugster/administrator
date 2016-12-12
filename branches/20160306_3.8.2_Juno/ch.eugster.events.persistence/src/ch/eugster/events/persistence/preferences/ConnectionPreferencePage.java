package ch.eugster.events.persistence.preferences;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.persistence.wizards.DriverLabelProvider;

public class ConnectionPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	private static final String HELP_MESSAGE = "Die Änderungen werden erst nach einem Neustart aktiviert.\n\n";
	
	private ComboFieldEditor databaseEditor;
	
	private StringFieldEditor urlEditor;
	
	private StringFieldEditor userEditor;
	
	private StringFieldEditor passwordEditor;
	
	private LabelFieldEditor helpEditor;
	
	public ConnectionPreferencePage()
	{
		super(1);
	}
	
	public ConnectionPreferencePage(int style)
	{
		super(style);
	}
	
	public ConnectionPreferencePage(String title, int style)
	{
		super(title, style);
	}
	
	public ConnectionPreferencePage(String title, ImageDescriptor image, int style)
	{
		super(title, image, style);
	}
	
	/**
	 * Returns the preference store of this preference page.
	 * <p>
	 * This is a framework hook method for subclasses to return a page-specific
	 * preference store. The default implementation returns <code>null</code>.
	 * </p>
	 * 
	 * @return the preference store, or <code>null</code> if none
	 */
	@Override
	protected IPreferenceStore doGetPreferenceStore()
	{
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, PreferenceInitializer.getServerQualifier());
	}
	
	@Override
	protected void createFieldEditors()
	{
		String[][] drivers = new String[DriverLabelProvider.SupportedDriver.values().length][2];
		for (DriverLabelProvider.SupportedDriver driver : DriverLabelProvider.SupportedDriver.values())
		{
			drivers[driver.ordinal()][0] = driver.getPlatform();
			drivers[driver.ordinal()][1] = driver.getDriver();
		}
		
		this.databaseEditor = new ComboFieldEditor(PersistenceUnitProperties.JDBC_DRIVER, "Datenbank", drivers, this
						.getFieldEditorParent());
		this.addField(this.databaseEditor);
		
		this.urlEditor = new StringFieldEditor(PersistenceUnitProperties.JDBC_URL, "URL", this.getFieldEditorParent());
		this.urlEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
		this.urlEditor.setErrorMessage("Die URL muss einen gültige Form aufweisen.");
		this.urlEditor.setEmptyStringAllowed(false);
		this.addField(this.urlEditor);
		
		this.userEditor = new StringFieldEditor(PersistenceUnitProperties.JDBC_USER, "Benutzer", this
						.getFieldEditorParent());
		this.userEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_FOCUS_LOST);
		this.addField(this.userEditor);
		
		this.passwordEditor = new StringFieldEditor(PersistenceUnitProperties.JDBC_PASSWORD, "Passwort", this
						.getFieldEditorParent());
		this.passwordEditor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		this.addField(this.passwordEditor);
		
		ButtonFieldEditor testEditor = new ButtonFieldEditor("", "Verbindung prüfen", SWT.PUSH, this
						.getFieldEditorParent());
		testEditor.getButtonControl().addSelectionListener(new SelectionListener()
		{
			public void widgetSelected(SelectionEvent event)
			{
				try
				{
					Class.forName("com.mysql.jdbc.Driver");
					String url = ConnectionPreferencePage.this.urlEditor.getStringValue();
					String user = ConnectionPreferencePage.this.userEditor.getStringValue();
					String password = ConnectionPreferencePage.this.passwordEditor.getStringValue();
					Connection connection = DriverManager.getConnection(url, user, password);
					connection.close();
				}
				catch (ClassNotFoundException e)
				{
					MessageDialog
									.openError(ConnectionPreferencePage.this.getShell(), "Ungültiger Datenbanktreiber",
													"Die Verbindung konnte nicht hergestellt werden. Für die gewählte Datenbank fehlt der notwendige Treiber.");
					return;
				}
				catch (SQLException e)
				{
					MessageDialog
									.openError(ConnectionPreferencePage.this.getShell(), "Fehler",
													"Die Verbindung konnte nicht hergestellt werden. Stellen Sie sicher, dass die Verbindungsparameter richtig sind.");
					return;
				}
				MessageDialog.openInformation(ConnectionPreferencePage.this.getShell(), "Verbindung hergestellt",
								"Die Verbindung wurde erfolgreich hergestellt.");
				
			}
			
			public void widgetDefaultSelected(SelectionEvent event)
			{
				this.widgetSelected(event);
			}
		});
		this.addField(testEditor);
		
		this.helpEditor = new LabelFieldEditor("", "", this.getFieldEditorParent());
		this.helpEditor.setLabelText(ConnectionPreferencePage.HELP_MESSAGE);
		this.addField(this.helpEditor);
		
	}
	
	@Override
	/**
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getProperty().equals("field_editor_value"))
		{
			if (event.getNewValue() != null && !event.getNewValue().equals(event.getOldValue()))
			{
				if (event.getSource().equals(this.databaseEditor))
				{
					for (DriverLabelProvider.SupportedDriver supportedDriver : DriverLabelProvider.SupportedDriver
									.values())
					{
						if (supportedDriver.getDriver().equals(event.getNewValue()))
						{
							this.urlEditor.setStringValue(supportedDriver.getExampleURL());
							this.setDescription(supportedDriver.getDescription());
							this.helpEditor.setLabelText(ConnectionPreferencePage.HELP_MESSAGE + this.getDescription());
							this.getShell().layout();
							break;
						}
					}
				}
			}
		}
		else
			super.propertyChange(event);
	}
	
	@Override
	public void init(IWorkbench workbench)
	{
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, PreferenceInitializer
						.getServerQualifier());
		store.getString(PersistenceUnitProperties.JDBC_USER);
		this.setPreferenceStore(store);
		this.setDescription("Eigenschaften der Datenbankverbindung");
	}
	
}
