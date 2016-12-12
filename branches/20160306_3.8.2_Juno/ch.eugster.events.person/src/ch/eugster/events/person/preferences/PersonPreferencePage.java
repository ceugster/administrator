package ch.eugster.events.person.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PersonPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	private BooleanFieldEditor mandatoryEditor;

	public PersonPreferencePage()
	{
		this(GRID);
	}

	public PersonPreferencePage(final int style)
	{
		super(style);
	}

	public PersonPreferencePage(final String title, final ImageDescriptor image, final int style)
	{
		super(title, image, style);
	}

	public PersonPreferencePage(final String title, final int style)
	{
		super(title, style);
	}

	@Override
	protected void createFieldEditors()
	{
		BooleanFieldEditor domainEditor = new BooleanFieldEditor(PreferenceInitializer.KEY_USE_DOMAIN, "Mit Domäne",
				SWT.CHECK, this.getFieldEditorParent());
		domainEditor.setPropertyChangeListener(this);
		this.addField(domainEditor);

		this.mandatoryEditor = new BooleanFieldEditor(PreferenceInitializer.KEY_DOMAIN_MANDATORY, "Domäne zwingend",
				SWT.CHECK, this.getFieldEditorParent());
		this.addField(mandatoryEditor);
	}

	@Override
	public void init(final IWorkbench workbench)
	{
		IPreferenceStore store = PersonPreferenceStore.getInstance();
		this.setPreferenceStore(store);
		this.setDescription("");
	}

	/**
	 * The field editor preference page implementation of this
	 * <code>IPreferencePage</code> (and <code>IPropertyChangeListener</code>)
	 * method intercepts <code>IS_VALID</code> events but passes other events on
	 * to its superclass.
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent event)
	{
		if (event.getProperty().equals("field_editor_value"))
		{
			boolean value = ((Boolean) event.getNewValue()).booleanValue();
			this.mandatoryEditor.setEnabled(value, this.getFieldEditorParent());
		}
	}

}
