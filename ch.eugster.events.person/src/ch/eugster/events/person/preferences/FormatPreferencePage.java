package ch.eugster.events.person.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;

public class FormatPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	public FormatPreferencePage()
	{
		super(GRID);
	}

	public FormatPreferencePage(final int style)
	{
		super(style);
	}

	public FormatPreferencePage(final String title, final ImageDescriptor image, final int style)
	{
		super(title, image, style);
	}

	public FormatPreferencePage(final String title, final int style)
	{
		super(title, style);
	}

	@Override
	protected void createFieldEditors()
	{
		StringFieldEditor idFormatEditor = new StringFieldEditor(PreferenceInitializer.KEY_ID_FORMAT,
				"Formatierung Id", this.getFieldEditorParent());
		this.addField(idFormatEditor);

		StringBuilder description = new StringBuilder("Mögliche Variablen:\n");
		String[] variables = PersonFormatter.getInstance().getPersonLabelVisibleVariables();
		for (String variable : variables)
		{
			description = description.append("\n" + variable);
		}
		AddressLinesListFieldEditor personFormatEditor = new AddressLinesListFieldEditor(
				PreferenceInitializer.KEY_PERSON_LABEL_FORMAT, "Formatierung Etikette Person", description.toString(),
				variables, this.getFieldEditorParent());
		this.addField(personFormatEditor);

		description = new StringBuilder("Mögliche Variablen:\n");
		variables = AddressFormatter.getInstance().getAddressLabelVisibleVariables();
		for (String variable : variables)
		{
			description = description.append("\n" + variable);
		}
		AddressLinesListFieldEditor addressFormatEditor = new AddressLinesListFieldEditor(
				PreferenceInitializer.KEY_ADDRESS_LABEL_FORMAT, "Formatierung Etikette Adresse",
				description.toString(), variables, this.getFieldEditorParent());
		this.addField(addressFormatEditor);

	}

	@Override
	public void init(final IWorkbench workbench)
	{
		IPreferenceStore store = PersonPreferenceStore.getInstance();
		this.setPreferenceStore(store);
		this.setDescription("");
	}

}
