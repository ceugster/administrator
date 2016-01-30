package ch.eugster.events.course;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.Season;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "ch.eugster.events.course";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		Activator.plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception
	{
		Activator.plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault()
	{
		return Activator.plugin;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry imageRegistry)
	{
		super.initializeImageRegistry(imageRegistry);
		imageRegistry.put(Season.IMAGE_ACTIVE,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/season_green_16.png")));
		imageRegistry.put(Season.IMAGE_CLOSED,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/season_orange_16.png")));
		imageRegistry.put(CourseState.FORTHCOMING.imageKey(),
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/flag_green_16.png")));
		imageRegistry.put(CourseState.DONE.imageKey(),
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/flag_orange_16.png")));
		imageRegistry.put(CourseState.ANNULATED.imageKey(),
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/flag_gray_16.png")));
		imageRegistry.put("FILTER", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/filter_16.png")));
		imageRegistry.put("NEW_WIZARD", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/new_wiz.png")));
		imageRegistry.put("BOOKING_BLUE",
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/user_blue_16.png")));
		imageRegistry.put("BOOKING_GREEN",
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/user_green_16.png")));
		imageRegistry.put("BOOKING_GREEN_EXCLAMATION",
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/user_green_exclamation_16.png")));
		imageRegistry.put("BOOKING_RED",
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/user_red_16.png")));
		imageRegistry.put("BOOKING_GREY",
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/user_grey_16.png")));
		imageRegistry.put("ADD", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/add_16.gif")));
		imageRegistry.put("EDIT", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/edit_16.png")));
		imageRegistry.put("DELETE", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/delete_16.png")));
		imageRegistry.put("EMAIL", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/email_16.png")));
		imageRegistry.put("BOOKING_48",
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/booking_48.png")));
		imageRegistry.put("BOOKING_TYPE",
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/type_16.png")));
		imageRegistry.put("COURSE_DETAIL",
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/calendar_16.gif")));
		imageRegistry.put("COURSE_GUIDE",
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/crown_16.png")));
		imageRegistry.put("GENERATE", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/work_16.png")));
		imageRegistry.put("CLEAR", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/clear_16.png")));
		imageRegistry.put("error", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/exclamation.png")));
		imageRegistry.put("ok", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/ok_16.gif")));
	}

}
