package ch.eugster.events.visits.preferences;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.eugster.events.persistence.model.AddressType;

public class VisitsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	public static final String ID = "ch.eugster.events.visits.preference.page";

	private ComboFieldEditor addressTypeEditor;

	private VisitsPreferenceStore preferenceStore;

	public VisitsPreferencePage()
	{
		this(GRID);
	}

	public VisitsPreferencePage(int style)
	{
		super(style);
	}

	public VisitsPreferencePage(String title, int style)
	{
		super(title, style);
	}

	public VisitsPreferencePage(String title, ImageDescriptor image, int style)
	{
		super(title, image, style);
	}

	@Override
	protected void createFieldEditors()
	{
		AddressType[] addressTypes = preferenceStore.getAvailableAddressTypes();
		if (addressTypes != null && addressTypes.length > 0)
		{
			String[][] entries = new String[addressTypes.length][2];
			for (int i = 0; i < addressTypes.length; i++)
			{
				entries[i][0] = addressTypes[i].getName();
				entries[i][1] = Integer.valueOf(i).toString();
			}
			addressTypeEditor = new ComboFieldEditor(PreferenceInitializer.KEY_DEFAULT_ADDRESS_TYPE,
					"Vorschlag Addresstyp", entries, this.getFieldEditorParent());
			this.addField(addressTypeEditor);
		}
	}

	@Override
	public void init(IWorkbench workbench)
	{
		preferenceStore = new VisitsPreferenceStore();
		this.setPreferenceStore(preferenceStore);
		this.setDescription("");
	}

}
