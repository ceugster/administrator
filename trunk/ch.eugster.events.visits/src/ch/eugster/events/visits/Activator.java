package ch.eugster.events.visits;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.VisitSettings;
import ch.eugster.events.persistence.queries.VisitSettingsQuery;
import ch.eugster.events.persistence.service.ConnectionService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{
	public static final String PLUGIN_ID = "ch.eugster.events.visits"; //$NON-NLS-1$

	private static Activator plugin;

	private VisitSettings settings;

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	/**
	 * The constructor
	 */
	public Activator()
	{
	}

	public VisitSettings getSettings()
	{
		return settings;
	}

	@Override
	protected void initializeImageRegistry(final ImageRegistry imageRegistry)
	{
		super.initializeImageRegistry(imageRegistry);
		imageRegistry.put("edit", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/edit_16.png")));
		imageRegistry.put("email", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/email_16.png")));
		imageRegistry.put("appliance", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/appliance_16.gif")));
		imageRegistry.put("at", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/at.png")));
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
		plugin = this;

		connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(context, ConnectionService.class, null)
		{

			@Override
			public ConnectionService addingService(final ServiceReference<ConnectionService> reference)
			{
				ConnectionService service = (ConnectionService) super.addingService(reference);
				if (service != null)
				{
					VisitSettingsQuery query = (VisitSettingsQuery) service.getQuery(VisitSettings.class);
					Activator.this.settings = query.select();
					if (Activator.this.settings == null)
					{
						Activator.this.settings = query.merge(new VisitSettings());
					}
				}
				return service;
			}

			@Override
			public void remove(final ServiceReference<ConnectionService> reference)
			{
			}
		};
		connectionServiceTracker.open();
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
		connectionServiceTracker.close();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault()
	{
		return plugin;
	}

}
