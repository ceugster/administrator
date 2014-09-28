package ch.eugster.events.utilities.console;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.eugster.events.utilities.console.database.manipulator.DoubleAddressGroupMemberEntriesDeleter;
import ch.eugster.events.utilities.console.database.manipulator.LawFolderUpdater;
import ch.eugster.events.utilities.console.database.manipulator.PersonWithoutChildrenRemover;

public class Activator implements BundleActivator
{

	private static BundleContext context;

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
		Activator.context = bundleContext;

		context.registerService(CommandProvider.class.getName(), new LawFolderUpdater(), null);
		context.registerService(CommandProvider.class.getName(), new DoubleAddressGroupMemberEntriesDeleter(), null);
		context.registerService(CommandProvider.class.getName(), new PersonWithoutChildrenRemover(), null);
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
		Activator.context = null;
	}

	public static BundleContext getContext()
	{
		return context;
	}

}
