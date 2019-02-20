package ch.eugster.events.donation.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.donation.Activator;


public class DonationPreferencePage extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage
{
	private StringFieldEditor numberFieldEditor;
	
	private ScaleFieldEditor scaleFieldEditor;
	
	public DonationPreferencePage()
	{
		this(FieldEditorPreferencePage.GRID);
	}

	public DonationPreferencePage(final int style)
	{
		super(style);
	}

	public DonationPreferencePage(final String title, final int style)
	{
		super(title, style);
	}

	public DonationPreferencePage(final String title, final ImageDescriptor image,
			final int style)
	{
		super(title, image, style);
	}

	@Override
	protected void createFieldEditors()
	{
		final FileFieldEditor bookingEditor = new FileFieldEditor(PreferenceInitializer.KEY_DONATION_TEMPLATE, "Vorlage Spendenbestätigung", this.getFieldEditorParent());
		bookingEditor.setChangeButtonText("...");
		bookingEditor.setFileExtensions(new String[] { "*.odt" });
		bookingEditor.setErrorMessage("Die Vorlage für die Spendenbestätigungen existiert nicht im angegebenen Pfad.");
		this.addField(bookingEditor);
		
		this.numberFieldEditor = new StringFieldEditor(PreferenceInitializer.KEY_DONATION_INPUT_FOR_NUMBER_OF_YEARS_BACK_POSSIBLE, "Datumeingabe möglich bis", this.getFieldEditorParent());
		this.addField(this.numberFieldEditor);

		this.scaleFieldEditor = new ScaleFieldEditor(PreferenceInitializer.KEY_DONATION_INPUT_FOR_NUMBER_OF_YEARS_BACK_POSSIBLE, "Jahre zurück", this.getFieldEditorParent());
		this.scaleFieldEditor.setIncrement(1);
		this.scaleFieldEditor.setMaximum(50);
		this.scaleFieldEditor.setMinimum(0);
		this.scaleFieldEditor.setPageIncrement(5);
		this.addField(this.scaleFieldEditor);
	}

	@Override
	public void init(final IWorkbench workbench)
	{
		final IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		this.setPreferenceStore(store);
		this.setDescription("Vorlagen");
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event)
	{
		super.propertyChange(event);
		if (event.getProperty().equals("field_editor_value"))
		{
			if (event.getSource().equals(this.numberFieldEditor))
			{
				int value = 0;
				try
				{
					value = Integer.valueOf(event.getNewValue().toString()).intValue();
					this.scaleFieldEditor.getScaleControl().setSelection(value);
				}
				catch (final NumberFormatException e)
				{
					try
					{
						value = Integer.valueOf(event.getOldValue().toString()).intValue();
						this.scaleFieldEditor.getScaleControl().setSelection(value);
					}
					catch (final NumberFormatException e2)
					{
						this.numberFieldEditor.getTextControl(this.getFieldEditorParent()).setText("0");
					}
				}
			}
			else if (event.getSource().equals(this.scaleFieldEditor))
			{
				final int selection = this.scaleFieldEditor.getScaleControl().getSelection();
				this.numberFieldEditor.getTextControl(this.getFieldEditorParent()).setText(String.valueOf(selection));
			}
		}
	}
}
