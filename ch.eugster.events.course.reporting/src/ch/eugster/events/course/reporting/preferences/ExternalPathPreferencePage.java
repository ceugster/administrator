package ch.eugster.events.course.reporting.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.course.reporting.Activator;

public class ExternalPathPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public ExternalPathPreferencePage()
	{
		this(GRID);
	}

	public ExternalPathPreferencePage(final int style)
	{
		super(style);
	}

	public ExternalPathPreferencePage(final String title, final ImageDescriptor image, final int style)
	{
		super(title, image, style);
	}

	public ExternalPathPreferencePage(final String title, final int style)
	{
		super(title, style);
	}

	@Override
	protected void createFieldEditors()
	{
		FileFieldEditor bookingEditor = new FileFieldEditor(PreferenceInitializer.KEY_BOOKING_TEMPLATE,
				"Vorlage Anmeldebestätigung", this.getFieldEditorParent());
		bookingEditor.setChangeButtonText("...");
		bookingEditor.setFileExtensions(new String[] { "*.odt" });
		bookingEditor.setErrorMessage("Die Vorlage für die Anmeldebestätigungen existiert nicht im angegebenen Pfad.");
		this.addField(bookingEditor);

		FileFieldEditor invitationEditor = new FileFieldEditor(PreferenceInitializer.KEY_INVITATION_TEMPLATE,
				"Vorlage Kurseinladung", this.getFieldEditorParent());
		invitationEditor.setChangeButtonText("...");
		invitationEditor.setFileExtensions(new String[] { "*.odt" });
		invitationEditor.setErrorMessage("Die Vorlage für die Kurseinladungen existiert nicht im angegebenen Pfad.");
		this.addField(invitationEditor);

		FileFieldEditor participationEditor = new FileFieldEditor(PreferenceInitializer.KEY_PARTICIPATION_TEMPLATE,
				"Vorlage Teilnahmebestätigung", this.getFieldEditorParent());
		participationEditor.setChangeButtonText("...");
		participationEditor.setFileExtensions(new String[] { "*.odt" });
		participationEditor
				.setErrorMessage("Die Vorlage für die Teilnahmebestätigungen existiert nicht im angegebenen Pfad.");
		this.addField(participationEditor);
	}

	@Override
	public void init(final IWorkbench workbench)
	{
		IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), Activator.PLUGIN_ID);
		this.setPreferenceStore(store);
		this.setDescription("Vorlagen");
	}

}
