package ch.eugster.events.course.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

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
		StringFieldEditor idFormatEditor = new StringFieldEditor(PreferenceInitializer.KEY_BOOKING_ID_FORMAT,
				"Format Buchungsnummer", this.getFieldEditorParent());
		this.addField(idFormatEditor);
	}

	@Override
	public void init(final IWorkbench workbench)
	{
		IPreferenceStore store = CoursePreferenceStore.getInstance();
		this.setPreferenceStore(store);
		this.setDescription("Formatierungen");
	}

}
