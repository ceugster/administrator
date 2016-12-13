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

	public CoursePreferencePage(final int style)
	{
		super(style);
	}

	public CoursePreferencePage(final String title, final ImageDescriptor image, final int style)
	{
		super(title, image, style);
	}

	public CoursePreferencePage(final String title, final int style)
	{
		super(title, style);
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
		addField(domainsMandatory);

		useCategories = new BooleanFieldEditor(PreferenceInitializer.KEY_USE_CATEGORIES, "Kategorien verwenden",
				getFieldEditorParent());
		useCategories.setPropertyChangeListener(this);
		addField(useCategories);

		categoriesMandatory = new BooleanFieldEditor(PreferenceInitializer.KEY_MANDATORY_CATEGORIES,
				"Verwendung der Kategorien ist zwingend", getFieldEditorParent());
		categoriesMandatory.setEnabled(this.getPreferenceStore().getBoolean(PreferenceInitializer.KEY_USE_CATEGORIES),
				getFieldEditorParent());
		addField(categoriesMandatory);

		useRubrics = new BooleanFieldEditor(PreferenceInitializer.KEY_USE_RUBRICS, "Rubriken verwenden",
				getFieldEditorParent());
		useRubrics.setPropertyChangeListener(this);
		addField(useRubrics);

		rubricsMandatory = new BooleanFieldEditor(PreferenceInitializer.KEY_MANDATORY_RUBRICS,
				"Verwendung der Rubriken ist zwingend", getFieldEditorParent());
		rubricsMandatory.setEnabled(this.getPreferenceStore().getBoolean(PreferenceInitializer.KEY_USE_RUBRICS),
				getFieldEditorParent());
		addField(rubricsMandatory);

	}

	@Override
	public void init(final IWorkbench workbench)
	{
		IPreferenceStore store = CoursePreferenceStore.getInstance();
		this.setPreferenceStore(store);
		this.setDescription("Optionen");
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event)
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
		super.propertyChange(event);
	}
}
