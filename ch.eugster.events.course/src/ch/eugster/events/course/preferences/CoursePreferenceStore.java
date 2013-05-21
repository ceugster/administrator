package ch.eugster.events.course.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.queries.GlobalSettingsQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class CoursePreferenceStore extends ScopedPreferenceStore
{
	public CoursePreferenceStore()
	{
		super(new InstanceScope(), Activator.PLUGIN_ID);
		this.setValue(PreferenceInitializer.KEY_BOOKING_ID_FORMAT, GlobalSettings.getInstance().getBookingIdFormat());
		this.setValue(PreferenceInitializer.KEY_USE_CATEGORIES, GlobalSettings.getInstance().getCourseHasCategory());
		this.setValue(PreferenceInitializer.KEY_USE_DOMAINS, GlobalSettings.getInstance().getCourseHasDomain());
		this.setValue(PreferenceInitializer.KEY_USE_RUBRICS, GlobalSettings.getInstance().getCourseHasRubric());
		this.setValue(PreferenceInitializer.KEY_MANDATORY_CATEGORIES, GlobalSettings.getInstance()
				.isCourseCategoryMandatory());
		this.setValue(PreferenceInitializer.KEY_MANDATORY_DOMAINS, GlobalSettings.getInstance()
				.isCourseDomainMandatory());
		this.setValue(PreferenceInitializer.KEY_MANDATORY_RUBRICS, GlobalSettings.getInstance()
				.isCourseRubricMandatory());
	}

	@Override
	public void save()
	{
		if (this.needsSaving())
		{
			GlobalSettings.getInstance()
					.setBookingIdFormat(this.getString(PreferenceInitializer.KEY_BOOKING_ID_FORMAT));
			GlobalSettings.getInstance()
					.setCourseHasCategory(this.getBoolean(PreferenceInitializer.KEY_USE_CATEGORIES));
			GlobalSettings.getInstance().setCourseHasDomain(this.getBoolean(PreferenceInitializer.KEY_USE_DOMAINS));
			GlobalSettings.getInstance().setCourseHasRubric(this.getBoolean(PreferenceInitializer.KEY_USE_RUBRICS));
			GlobalSettings.getInstance().setCourseCategoryMandatory(
					this.getBoolean(PreferenceInitializer.KEY_MANDATORY_CATEGORIES));
			GlobalSettings.getInstance().setCourseDomainMandatory(
					this.getBoolean(PreferenceInitializer.KEY_MANDATORY_DOMAINS));
			GlobalSettings.getInstance().setCourseRubricMandatory(
					this.getBoolean(PreferenceInitializer.KEY_MANDATORY_RUBRICS));
			try
			{
				ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
						ConnectionService.class.getName(), null);
				tracker.open();
				ConnectionService service = (ConnectionService) tracker.getService();
				if (service != null)
				{
					GlobalSettingsQuery query = (GlobalSettingsQuery) service.getQuery(GlobalSettings.class);
					GlobalSettings.setInstance(query.merge(GlobalSettings.getInstance()));
				}
				tracker.close();

				super.save();
			}
			catch (Exception e)
			{
			}
		}
	}
}