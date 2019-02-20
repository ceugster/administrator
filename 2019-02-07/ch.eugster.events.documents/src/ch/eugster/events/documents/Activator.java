package ch.eugster.events.documents;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator
{
	private static Activator activator;

	private BundleContext context;

	public BundleContext getBundleContext()
	{
		return context;
	}

	@Override
	public void start(final BundleContext context) throws Exception
	{
		this.context = context;
		Activator.activator = this;
	}

	@Override
	public void stop(final BundleContext context) throws Exception
	{
		this.context = null;
	}

	public static Activator getDefault()
	{
		return activator;
	}

}
