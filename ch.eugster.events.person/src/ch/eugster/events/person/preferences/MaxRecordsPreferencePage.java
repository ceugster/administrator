package ch.eugster.events.person.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MaxRecordsPreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage
{

	public MaxRecordsPreferencePage()
	{
		super(GRID);
	}

	public MaxRecordsPreferencePage(int style)
	{
		super(style);
	}

	public MaxRecordsPreferencePage(String title, int style)
	{
		super(title, style);
	}

	public MaxRecordsPreferencePage(String title, ImageDescriptor image,
			int style)
	{
		super(title, image, style);
	}

	@Override
	protected void createFieldEditors()
	{
		IntegerFieldEditor maxRowsEditor = new IntegerFieldEditor(PreferenceInitializer.KEY_MAX_RECORDS, "Maximale Anzahl Datensätze laden", this.getFieldEditorParent());
		maxRowsEditor.setEmptyStringAllowed(false);
		maxRowsEditor.setValidRange(-1, 100000);
		this.addField(maxRowsEditor);

	}

	@Override
	public void init(IWorkbench workbench)
	{
		IPreferenceStore store = new PersonPreferenceStore();
		this.setPreferenceStore(store);
		this.setDescription("");
	}

}
