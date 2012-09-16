package ch.eugster.events.course.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CoursePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	private BooleanFieldEditor useDomains;

	private BooleanFieldEditor domainsMandatory;

	private BooleanFieldEditor useCategories;

	private BooleanFieldEditor categoriesMandatory;

	private BooleanFieldEditor useRubrics;

	private BooleanFieldEditor rubricsMandatory;

	public CoursePreferencePage()
	{
		this(GRID);
	}

	public CoursePreferencePage(int style)
	{
		super(style);
	}

	public CoursePreferencePage(String title, int style)
	{
		super(title, style);
	}

	public CoursePreferencePage(String title, ImageDescriptor image, int style)
	{
		super(title, image, style);
	}

	@Override
	protected void createFieldEditors()
	{
		useDomains = new BooleanFieldEditor(PreferenceInitializer.KEY_USE_DOMAINS, "Domänen verwenden",
				getFieldEditorParent());
		useDomains.setPropertyChangeListener(this);
		addField(useDomains);

		domainsMandatory = new BooleanFieldEditor(PreferenceInitializer.KEY_MANDATORY_DOMAINS,
				"Verwendung der Domänen ist zwingend", getFieldEditorParent());
		domainsMandatory.setEnabled(this.getPreferenceStore().getBoolean(PreferenceInitializer.KEY_USE_DOMAINS),
				getFieldEditorParent());
		addField(useDomains);

		useCategories = new BooleanFieldEditor(PreferenceInitializer.KEY_USE_CATEGORIES, "Kategorien verwenden",
				getFieldEditorParent());
		useCategories.setPropertyChangeListener(this);
		addField(useCategories);

		categoriesMandatory = new BooleanFieldEditor(PreferenceInitializer.KEY_MANDATORY_CATEGORIES,
				"Verwendung der Kategorien ist zwingend", getFieldEditorParent());
		categoriesMandatory.setEnabled(this.getPreferenceStore().getBoolean(PreferenceInitializer.KEY_USE_CATEGORIES),
				getFieldEditorParent());
		addField(useCategories);

		useRubrics = new BooleanFieldEditor(PreferenceInitializer.KEY_USE_RUBRICS, "Rubriken verwenden",
				getFieldEditorParent());
		useRubrics.setPropertyChangeListener(this);
		addField(useRubrics);

		rubricsMandatory = new BooleanFieldEditor(PreferenceInitializer.KEY_MANDATORY_RUBRICS,
				"Verwendung der Rubriken ist zwingend", getFieldEditorParent());
		rubricsMandatory.setEnabled(this.getPreferenceStore().getBoolean(PreferenceInitializer.KEY_USE_RUBRICS),
				getFieldEditorParent());
		addField(useRubrics);

	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		if (event.getSource().equals(useDomains))
		{
			domainsMandatory.setEnabled(useDomains.getBooleanValue(), getFieldEditorParent());
		}
		else if (event.getSource().equals(useCategories))
		{
			categoriesMandatory.setEnabled(useCategories.getBooleanValue(), getFieldEditorParent());
		}
		else if (event.getSource().equals(useRubrics))
		{
			rubricsMandatory.setEnabled(useRubrics.getBooleanValue(), getFieldEditorParent());
		}
	}

	@Override
	public void init(IWorkbench workbench)
	{
		IPreferenceStore store = new CoursePreferenceStore();
		this.setPreferenceStore(store);
		this.setDescription("Optionen");
	}

}
