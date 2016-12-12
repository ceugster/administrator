package ch.eugster.events.addressgroup.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.addressgroup.Activator;

public class FormLetterFolderPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public FormLetterFolderPreferencePage()
	{
		this(GRID);
	}

	public FormLetterFolderPreferencePage(int style)
	{
		super(style);
	}

	public FormLetterFolderPreferencePage(String title, int style)
	{
		super(title, style);
	}

	public FormLetterFolderPreferencePage(String title, ImageDescriptor image, int style)
	{
		super(title, image, style);
	}

	@Override
	protected void createFieldEditors()
	{
		String userHome = System.getProperty("user.home");
		DirectoryFieldEditor formLetterFolderEditor = new DirectoryFieldEditor(
				PreferenceInitializer.KEY_FORM_LETTER_FOLDER, "Verzeichnis Serienbriefe", this.getFieldEditorParent());
		formLetterFolderEditor.setChangeButtonText("...");
		formLetterFolderEditor.setFilterPath(userHome == null ? new File("") : new File(userHome));
		formLetterFolderEditor.setErrorMessage("Das Verzeichnis ist ungültig.");
		this.addField(formLetterFolderEditor);

	}

	@Override
	public void init(IWorkbench workbench)
	{
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		this.setPreferenceStore(store);
		this.setDescription("Verzeichnis Serienbriefe");
	}

}
