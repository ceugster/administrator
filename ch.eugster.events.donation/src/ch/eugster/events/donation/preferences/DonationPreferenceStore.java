package ch.eugster.events.donation.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.donation.Activator;

public class DonationPreferenceStore extends ScopedPreferenceStore
{
	public DonationPreferenceStore()
	{
		super(new InstanceScope(), Activator.PLUGIN_ID);
	}

	//	@Override
	//	public void save()
	//	{
	//		if (this.needsSaving())
	//		{
	//			GlobalSettings.getInstance().setBookingIdFormat(this.getString(PreferenceInitializer.KEY_ID_FORMAT));
	//			GlobalSettings.getInstance().setCourseHasCategory(this.getBoolean(PreferenceInitializer.KEY_USE_CATEGORIES));
	//			GlobalSettings.getInstance().setCourseHasDomain(this.getBoolean(PreferenceInitializer.KEY_USE_DOMAINS));
	//			GlobalSettings.getInstance().setCourseHasRubric(this.getBoolean(PreferenceInitializer.KEY_USE_RUBRICS));
	//			GlobalSettings.getInstance().setCourseCategoryMandatory(this.getBoolean(PreferenceInitializer.KEY_MANDATORY_CATEGORIES));
	//			GlobalSettings.getInstance().setCourseDomainMandatory(this.getBoolean(PreferenceInitializer.KEY_MANDATORY_DOMAINS));
	//			GlobalSettings.getInstance().setCourseRubricMandatory(this.getBoolean(PreferenceInitializer.KEY_MANDATORY_RUBRICS));
	//			try
	//			{
	//				GlobalSettings.getInstance().merge();
	//
	//				super.save();
	//			}
	//			catch (Exception e)
	//			{
	//				System.out.println();
	//			}
	//		}
	//	}
}
