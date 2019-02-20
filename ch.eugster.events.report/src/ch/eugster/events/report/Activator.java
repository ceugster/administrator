package ch.eugster.events.report;

import java.util.Hashtable;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.internal.engine.ReportServiceComponent;

public class Activator extends AbstractUIPlugin
{
	private static Activator activator;

	private ServiceRegistration<ReportService> reportServiceRegistration;
	
	public ServiceRegistration<ReportService> getReportServiceRegistration()
	{
		return reportServiceRegistration;
	}
	
	@Override
	protected void initializeImageRegistry(final ImageRegistry imageRegistry)
	{
		super.initializeImageRegistry(imageRegistry);
		imageRegistry.put("first", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/first.gif")));
		imageRegistry.put("firstd", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/firstd.gif")));
		imageRegistry.put("last", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/last.gif")));
		imageRegistry.put("lastd", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/lastd.gif")));
		imageRegistry.put("next", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/next.gif")));
		imageRegistry.put("nextd", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/nextd.gif")));
		imageRegistry.put("previous", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/previous.gif")));
		imageRegistry.put("previousd", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/previousd.gif")));
		imageRegistry.put("print", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/print.gif")));
		imageRegistry.put("printd", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/printd.gif")));
		imageRegistry.put("reload", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/reload.gif")));
		imageRegistry.put("reloadd", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/reloadd.gif")));
		imageRegistry.put("save", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/save.gif")));
		imageRegistry.put("saved", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/saved.gif")));
		imageRegistry.put("zoomactual", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/zoomactual.gif")));
		imageRegistry.put("zoomactuald", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/zoomactuald.gif")));
		imageRegistry.put("zoomfitpage", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/zoomfitpage.gif")));
		imageRegistry.put("zoomfitpaged",
				ImageDescriptor.createFromURL(getBundle().getEntry("/icons/zoomfitpaged.gif")));
		imageRegistry.put("zoomfitwidth",
				ImageDescriptor.createFromURL(getBundle().getEntry("/icons/zoomfitwidth.gif")));
		imageRegistry.put("zoomfitwidthd",
				ImageDescriptor.createFromURL(getBundle().getEntry("/icons/zoomfitwidthd.gif")));
		imageRegistry.put("zoomminus", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/zoomminus.gif")));
		imageRegistry.put("zoomminusd", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/zoomminusd.gif")));
		imageRegistry.put("zoomplus", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/zoomplus.gif")));
		imageRegistry.put("zoomplusd", ImageDescriptor.createFromURL(getBundle().getEntry("/icons/zoomplusd.gif")));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(final BundleContext bundleContext) throws Exception
	{
		super.start(bundleContext);
		Activator.activator = this;
		reportServiceRegistration = bundleContext.registerService(ReportService.class, new ReportServiceComponent(),
				new Hashtable<String, Object>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext bundleContext) throws Exception
	{
		reportServiceRegistration.unregister();
		super.stop(bundleContext);
	}

	public static Activator getDefault()
	{
		return activator;
	}

}
