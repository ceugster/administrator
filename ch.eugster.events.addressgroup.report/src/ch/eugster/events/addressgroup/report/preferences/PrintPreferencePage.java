package ch.eugster.events.addressgroup.report.preferences;

import java.io.File;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.eugster.events.addressgroup.report.Activator;
import ch.eugster.events.persistence.preferences.LabelFieldEditor;
import ch.eugster.events.report.engine.ReportService.Destination;
import ch.eugster.events.report.engine.ReportService.Format;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class PrintPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public PrintPreferencePage()
	{
		super(FieldEditorPreferencePage.GRID);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors()
	{
		this.setTitle("Einstellungen Empfängerlisten");

		BooleanFieldEditor printRecipientListEditor = new BooleanFieldEditor(
				PreferenceConstants.P_PRINT_RECIPIENT_LIST_AUTOMATICALLY,
				"Empfängerliste bei Emailversand aus Adressgruppen automatisch erstellen", getFieldEditorParent());
		addField(printRecipientListEditor);

		String[][] values = new String[Destination.values().length][2];
		for (Destination destination : Destination.values())
		{
			values[destination.ordinal()][0] = destination.label();
			values[destination.ordinal()][1] = String.valueOf(destination.ordinal());
		}
		ComboFieldEditor printTargetEditor = new ComboFieldEditor(PreferenceConstants.P_DESTINATION, "Ausgabe", values,
				getFieldEditorParent());
		addField(printTargetEditor);

		BooleanFieldEditor useDefaultPrinterEditor = new BooleanFieldEditor(PreferenceConstants.P_USE_STANDARD_PRINTER,
				"Standarddrucker verwenden", getFieldEditorParent());
		addField(useDefaultPrinterEditor);

		LabelFieldEditor labelEditor = new LabelFieldEditor("", "", getFieldEditorParent());
		addField(labelEditor);

		labelEditor = new LabelFieldEditor("", "Voreinstellungen Export", getFieldEditorParent());
		addField(labelEditor);

		values = new String[Format.values().length][2];
		for (Format format : Format.values())
		{
			values[format.ordinal()][0] = format.label();
			values[format.ordinal()][1] = String.valueOf(format.ordinal());
		}
		ComboFieldEditor fileFormatEditor = new ComboFieldEditor(PreferenceConstants.P_DEFAULT_FILE_FORMAT,
				"Dateiformat", values, getFieldEditorParent());
		addField(fileFormatEditor);

		DirectoryFieldEditor directoryEditor = new DirectoryFieldEditor(
				PreferenceConstants.P_DEFAULT_EXPORT_FILE_DIRECTORY, "Zielverzeichnis", getFieldEditorParent());
		directoryEditor.setChangeButtonText("...");
		directoryEditor.setEmptyStringAllowed(false);
		directoryEditor.setFilterPath(new File(System.getProperty("user.home")));
		addField(directoryEditor);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(final IWorkbench workbench)
	{
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Hier legen Sie die Einstellungen für die Erstellung von Empfängerlisten fest.");
	}

}