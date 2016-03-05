package ch.eugster.events.importer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.service.ConnectionService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private static Activator instance;

	private ServiceTracker connectionServiceTracker;
	
	public ConnectionService getConnectionService()
	{
		return (ConnectionService) connectionServiceTracker.getService();
	}

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
		
		connectionServiceTracker = new ServiceTracker(context, ConnectionService.class.getName(), null);
		connectionServiceTracker.open();
		instance = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception 
	{
		connectionServiceTracker.close();
		instance = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return instance;
	}

	@Override
	protected void initializeImageRegistry(final ImageRegistry imageRegistry)
	{
		super.initializeImageRegistry(imageRegistry);
		imageRegistry.put("selected", ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/add_16.gif")));
	}

}
