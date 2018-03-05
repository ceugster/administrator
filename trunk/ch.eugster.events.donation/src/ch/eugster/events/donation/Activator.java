package ch.eugster.events.donation;

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
	public static final String PLUGIN_ID = "ch.eugster.events.donation";

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
	public void start(final BundleContext context) throws Exception
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
	public void stop(final BundleContext context) throws Exception
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
	protected void initializeImageRegistry(final ImageRegistry imageRegistry)
	{
		super.initializeImageRegistry(imageRegistry);
		imageRegistry.put("MONEY", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/money.png")));
		imageRegistry.put("CLEAR", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/clear_16.png")));
		imageRegistry.put("PRINT", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/print_16.png")));
		imageRegistry.put("LIST", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/list_16.png")));
		imageRegistry.put("LOAD_LETTER", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/page_16.png")));
	}

}
