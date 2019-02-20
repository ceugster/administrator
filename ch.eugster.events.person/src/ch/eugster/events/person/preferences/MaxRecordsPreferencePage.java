package ch.eugster.events.person.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MaxRecordsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public MaxRecordsPreferencePage()
	{
		super(GRID);
	}

	public MaxRecordsPreferencePage(final int style)
	{
		super(style);
	}

	public MaxRecordsPreferencePage(final String title, final ImageDescriptor image, final int style)
	{
		super(title, image, style);
	}

	public MaxRecordsPreferencePage(final String title, final int style)
	{
		super(title, style);
	}

	@Override
	protected void createFieldEditors()
	{
		IntegerFieldEditor maxRowsEditor = new IntegerFieldEditor(PreferenceInitializer.KEY_MAX_RECORDS,
				"Maximale Anzahl Datensätze laden", this.getFieldEditorParent());
		maxRowsEditor.setEmptyStringAllowed(false);
		maxRowsEditor.setValidRange(-1, 100000);
		this.addField(maxRowsEditor);

		IntegerFieldEditor criteriaMinLengthEditor = new IntegerFieldEditor(PreferenceInitializer.KEY_CRITERIA_MIN_LENGTH,
				"Mindestlänge Kriterien", this.getFieldEditorParent());
		criteriaMinLengthEditor.setEmptyStringAllowed(false);
		criteriaMinLengthEditor.setValidRange(-1, 100000);
		this.addField(criteriaMinLengthEditor);
	}

	@Override
	public void init(final IWorkbench workbench)
	{
		IPreferenceStore store = PersonPreferenceStore.getInstance();
		this.setPreferenceStore(store);
		this.setDescription("");
	}

}
