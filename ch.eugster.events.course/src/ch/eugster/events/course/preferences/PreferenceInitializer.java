package ch.eugster.events.course.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.model.GlobalSettings;

public class PreferenceInitializer extends AbstractPreferenceInitializer
{
	public static final String KEY_BOOKING_ID_FORMAT = "booking.id.format";

	public static final String KEY_USE_CATEGORIES = "course.category";

	public static final String KEY_USE_DOMAINS = "course.domain";

	public static final String KEY_USE_RUBRICS = "course.rubric";

	public static final String KEY_MANDATORY_CATEGORIES = "course.category";

	public static final String KEY_MANDATORY_DOMAINS = "course.domain";

	public static final String KEY_MANDATORY_RUBRICS = "course.rubric";

	public static final String KEY_BOOKING_TEMPLATE = "booking.confirmation.template";

	public static final String KEY_INVITATION_TEMPLATE = "invitation.template";

	public static final String KEY_PARTICIPATION_TEMPLATE = "participation.confirmation.template";

	public static final String KEY_TEMPLATE_PATHS = "template.paths";

	public PreferenceInitializer()
	{
	}

	@Override
	public void initializeDefaultPreferences()
	{
		IEclipsePreferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
		prefs.put(KEY_BOOKING_ID_FORMAT, GlobalSettings.getInstance().getBookingIdFormat());
		prefs.put(KEY_USE_CATEGORIES, Boolean.toString(GlobalSettings.getInstance().getCourseHasCategory()));
		prefs.put(KEY_USE_DOMAINS, Boolean.toString(GlobalSettings.getInstance().getCourseHasDomain()));
		prefs.put(KEY_USE_RUBRICS, Boolean.toString(GlobalSettings.getInstance().getCourseHasRubric()));
		prefs.put(KEY_MANDATORY_CATEGORIES, Boolean.toString(GlobalSettings.getInstance().isCourseCategoryMandatory()));
		prefs.put(KEY_MANDATORY_DOMAINS, Boolean.toString(GlobalSettings.getInstance().isCourseDomainMandatory()));
		prefs.put(KEY_MANDATORY_RUBRICS, Boolean.toString(GlobalSettings.getInstance().isCourseRubricMandatory()));
		prefs.put(KEY_BOOKING_TEMPLATE, "");
		prefs.put(KEY_INVITATION_TEMPLATE, "");
		prefs.put(KEY_PARTICIPATION_TEMPLATE, "");
		prefs.put(KEY_TEMPLATE_PATHS, "");
	}

}
