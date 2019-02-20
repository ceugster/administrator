package ch.eugster.events.charity;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ch.eugster.events.sponsorlauf"; //$NON-NLS-1$

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

	public BundleContext getBundleContext()
	{
		return super.getBundle().getBundleContext();
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
	protected void initializeImageRegistry(final ImageRegistry imageRegistry)
	{
		super.initializeImageRegistry(imageRegistry);
		imageRegistry.put("RED", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/red_dot.gif")));
		imageRegistry.put("GREEN",
				ImageDescriptor.createFromURL(getBundle().getEntry("/icons/green_dot.gif")));
		imageRegistry.put("YELLOW",
				ImageDescriptor.createFromURL(getBundle().getEntry("/icons/yellow_dot.gif")));
		imageRegistry.put("RUNNER",
				ImageDescriptor.createFromURL(getBundle().getEntry("/icons/runner_16.png")));
	}
}
