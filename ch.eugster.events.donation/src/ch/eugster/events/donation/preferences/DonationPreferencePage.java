package ch.eugster.events.donation.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.donation.Activator;


public class DonationPreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage
{

	public DonationPreferencePage()
	{
		this(GRID);
	}

	public DonationPreferencePage(int style)
	{
		super(style);
	}

	public DonationPreferencePage(String title, int style)
	{
		super(title, style);
	}

	public DonationPreferencePage(String title, ImageDescriptor image,
			int style)
	{
		super(title, image, style);
	}

	@Override
	protected void createFieldEditors()
	{
		FileFieldEditor bookingEditor = new FileFieldEditor(PreferenceInitializer.KEY_DONATION_TEMPLATE, "Vorlage Spendenbestätigung", this.getFieldEditorParent());
		bookingEditor.setChangeButtonText("...");
		bookingEditor.setFileExtensions(new String[] { "*.odt" });
		bookingEditor.setErrorMessage("Die Vorlage für die Spendenbestätigungen existiert nicht im angegebenen Pfad.");
		this.addField(bookingEditor);
	}

	@Override
	public void init(IWorkbench workbench)
	{
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		this.setPreferenceStore(store);
		this.setDescription("Vorlagen");
	}

}
