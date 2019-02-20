package ch.eugster.events.todo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

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
		COURSE, RELOAD, TODO_DONE, TODO_OPEN;
		
		public String key()
		{
			switch (this)
			{
			case COURSE:
			{
				return "image.course";
			}
			case RELOAD:
			{
				return "image.reload";
			}
			case TODO_DONE:
			{
				return "image.todo.done";
			}
			case TODO_OPEN:
			{
				return "image.todo.open";
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
			case COURSE:
			{
				return "/icons/flag_green_16.png";
			}
			case RELOAD:
			{
				return "/icons/reload_16.png";
			}
			case TODO_DONE:
			{
				return "/icons/flag_green_16.png";
			}
			case TODO_OPEN:
			{
				return "/icons/flag_orange_16.png";
			}
			default:
			{
				return null;
			}
			}
		}
	}
}
