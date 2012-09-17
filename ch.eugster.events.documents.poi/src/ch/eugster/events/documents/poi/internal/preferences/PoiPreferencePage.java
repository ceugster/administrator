package ch.eugster.events.documents.poi.internal.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.documents.poi.internal.Activator;

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

public class PoiPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	private FileFieldEditor writerEditor;

	public PoiPreferencePage()
	{
		this(GRID);
	}

	public PoiPreferencePage(final int style)
	{
		super(style);
	}

	public PoiPreferencePage(final String title, final ImageDescriptor image, final int style)
	{
		super(title, image, style);
	}

	public PoiPreferencePage(final String title, final int style)
	{
		super(title, style);
	}

	@Override
	protected void createFieldEditors()
	{
		FileFieldEditor spreadsheetEditor = new FileFieldEditor(PreferenceConstants.KEY_SPREADSHEET_PATH,
				"Tabellenkalkutation", this.getFieldEditorParent());
		spreadsheetEditor.setEmptyStringAllowed(true);
		spreadsheetEditor.setFileExtensions(new String[] { "*.exe" });
		spreadsheetEditor.setChangeButtonText("...");
		spreadsheetEditor.setErrorMessage("Der Pfad ist ungültig.");
		this.addField(spreadsheetEditor);

	}

	@Override
	public void init(final IWorkbench workbench)
	{
		IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), Activator.getDefault().getBundle()
				.getSymbolicName());
		this.setPreferenceStore(store);
		this.setDescription("Einstellungen Microsoft Office");
	}

}