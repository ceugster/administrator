package ch.eugster.events.addressgroup;

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
	public static final String PLUGIN_ID = "ch.eugster.events.addressgroup";

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
		imageRegistry.put("CATEGORY", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/category_16.png")));
		imageRegistry.put("ADDRESS_GROUP",
				ImageDescriptor.createFromURL(getBundle().getEntry("/icons/addressgroup_16.png")));
		imageRegistry.put("MEMBER", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/member_blue_16.png")));
		imageRegistry.put("ADDRESS", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/home_16.png")));
	}

}
