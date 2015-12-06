package ch.eugster.events.todo;

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
	public static final String PLUGIN_ID = "ch.eugster.events.todo"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry imageRegistry)
	{
		super.initializeImageRegistry(imageRegistry);
		for (Image image : Image.values())
		{
			imageRegistry.put(image.key(),
					ImageDescriptor.createFromURL(this.getBundle().getEntry(image.value())));
		}
	}

	public enum Image
	{
		IMAGE_COURSE, IMAGE_RELOAD;
		
		public String key()
		{
			switch (this)
			{
			case IMAGE_COURSE:
			{
				return "image.course";
			}
			case IMAGE_RELOAD:
			{
				return "image.reload";
			}
			default:
			{
				return null;
			}
			}
		}

		public String value()
		{
			switch (this)
			{
			case IMAGE_COURSE:
			{
				return "/icons/flag_green_16.png";
			}
			case IMAGE_RELOAD:
			{
				return "/icons/reload_16.png";
			}
			default:
			{
				return null;
			}
			}
		}
	}
}
