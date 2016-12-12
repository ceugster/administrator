package ch.eugster.events.documents.odfdom.internal.preferences;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.documents.odfdom.internal.Activator;

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

public class OdfDomPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	private FileFieldEditor writerEditor;

	public OdfDomPreferencePage()
	{
		this(GRID);
	}

	public OdfDomPreferencePage(final int style)
	{
		super(style);
	}

	public OdfDomPreferencePage(final String title, final ImageDescriptor image, final int style)
	{
		super(title, image, style);
	}

	public OdfDomPreferencePage(final String title, final int style)
	{
		super(title, style);
	}

	@Override
	protected void createFieldEditors()
	{
		String[][] officePackages = new String[2][2];
		officePackages[0][0] = "OpenOffice";
		officePackages[0][1] = "open.office";
		officePackages[1][0] = "LibreOffice";
		officePackages[1][1] = "libre.office";

		final Map<String, String> values = new HashMap<String, String>();
		values.put(officePackages[0][1] + ".writer.path", "C:/Programme/OpenOffice.org 3/program/swriter.exe");
		values.put(officePackages[1][1] + ".writer.path", "C:/Programme/LibreOffice/program/swriter.exe");
		values.put(officePackages[0][1] + ".writer.name", "swriter.exe");
		values.put(officePackages[1][1] + ".writer.name", "swriter.exe");

		ComboFieldEditor officeEditor = new ComboFieldEditor(PreferenceInitializer.KEY_OFFICE_PACKAGE,
				"Installiertes Office Paket", officePackages, this.getFieldEditorParent())
		{
			@Override
			protected void fireValueChanged(final String property, final Object oldValue, final Object newValue)
			{
				String path = values.get(newValue + ".writer.path");
				String name = values.get(newValue + ".writer.name");
				writerEditor.setFilterPath(new File(path));
				writerEditor.setFileExtensions(new String[] { name });
				super.fireValueChanged(property, oldValue, newValue);
			}
		};
		this.addField(officeEditor);

		// LabelFieldEditor editor = new LabelFieldEditor("Textverarbeitung",
		// this.getFieldEditorParent());
		// this.addField(editor);
		//
		writerEditor = new FileFieldEditor(PreferenceInitializer.KEY_OFFICE_WRITER_PATH, "Textverarbeitung",
				this.getFieldEditorParent());
		writerEditor.setEmptyStringAllowed(true);
		writerEditor.setFileExtensions(new String[] { "*.exe" });
		writerEditor.setChangeButtonText("...");
		writerEditor.setErrorMessage("Der Pfad ist ungültig.");
		this.addField(writerEditor);

	}

	@Override
	public void init(final IWorkbench workbench)
	{
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.getDefault().getBundle()
				.getSymbolicName());
		this.setPreferenceStore(store);
		this.setDescription("Einstellungen Open Document Format");
	}

}