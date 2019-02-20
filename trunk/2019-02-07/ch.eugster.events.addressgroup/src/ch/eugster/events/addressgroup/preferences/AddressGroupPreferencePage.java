package ch.eugster.events.addressgroup.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.addressgroup.Activator;

public class AddressGroupPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

	public AddressGroupPreferencePage()
	{
		this(GRID);
	}

	public AddressGroupPreferencePage(int style)
	{
		super(style);
	}

	public AddressGroupPreferencePage(String title, int style)
	{
		super(title, style);
	}

	public AddressGroupPreferencePage(String title, ImageDescriptor image, int style)
	{
		super(title, image, style);
	}

	@Override
	protected void createFieldEditors()
	{
	}

	@Override
	public void init(IWorkbench workbench)
	{
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		this.setPreferenceStore(store);
		this.setDescription("Adressgruppen");
	}

}
