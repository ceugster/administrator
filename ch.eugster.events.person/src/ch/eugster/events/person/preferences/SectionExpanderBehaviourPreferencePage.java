package ch.eugster.events.person.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.eugster.events.person.editors.EditorSelector;

public class SectionExpanderBehaviourPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage
{
	private static final String message = "Personen - Editorenbereiche";

	public SectionExpanderBehaviourPreferencePage()
	{
		super(GRID);
	}

	public SectionExpanderBehaviourPreferencePage(final int style)
	{
		super(style);
	}

	public SectionExpanderBehaviourPreferencePage(final String title, final ImageDescriptor image, final int style)
	{
		super(title, image, style);
	}

	public SectionExpanderBehaviourPreferencePage(final String title, final int style)
	{
		super(title, style);
	}

	@Override
	protected void createFieldEditors()
	{
		this.setTitle("Einstellungen der Editorenbereiche");
		this.setMessage(message);

		String[][] labelsAndValues = new String[EditorSelector.values().length][2];
		for (int i = 0; i < EditorSelector.values().length; i++)
		{
			labelsAndValues[i][0] = EditorSelector.values()[i].label();
			labelsAndValues[i][1] = EditorSelector.values()[i].value();
		}
		RadioGroupFieldEditor selectEditor = new RadioGroupFieldEditor(PreferenceInitializer.KEY_EDITOR_SELECTOR,
				"Auswahl Personeneditor", 1, labelsAndValues, getFieldEditorParent());
		this.addField(selectEditor);

		labelsAndValues = new String[][] {
				{ "Auf Editorenebene (gilt für alle Editoren gleichen Typs)",
						PreferenceInitializer.EDITOR_SECTION_BEHAVIOUR_EDITOR },
				{ "Auf Objektebene (individuell für jedes Objekt)",
						PreferenceInitializer.EDITOR_SECTION_BEHAVIOUR_OBJECT } };
		selectEditor = new RadioGroupFieldEditor(PreferenceInitializer.KEY_EDITOR_SECTION_BEHAVIOUR,
				"Verhalten der Bereiche in den Editoren", 1, labelsAndValues, getFieldEditorParent());
		this.addField(selectEditor);

		BooleanFieldEditor cityEditor = new BooleanFieldEditor(
				PreferenceInitializer.KEY_EDITOR_ADD_BLANK_AFTER_DOT_IN_CITY, "Leerschlag nach . in Ort",
				getFieldEditorParent());
		this.addField(cityEditor);
	}

	@Override
	public void init(final IWorkbench workbench)
	{
		IPreferenceStore store = PersonPreferenceStore.getInstance();
		this.setPreferenceStore(store);
		this.setDescription("");
	}

}
