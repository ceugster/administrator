package ch.eugster.events.person;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.service.ConnectionService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "ch.eugster.events.person";

	public static final String KEY_PERSON_BLUE = "person.blue";

	public static final String KEY_PERSON_RED = "person.red";

	public static final String KEY_PERSON_GREY = "person.grey";

	public static final String KEY_PERSON_BLUE_WITH_STAR = "person.blue.with.star";

	public static final String KEY_PERSON_GREY_WITH_STAR = "person.grey.with.star";

	public static final String KEY_EDIT = "edit";

	public static final String KEY_ADDRESS = "address";

	public static final String KEY_CHANGE_ADDRESS = "change.address";

	public static final String KEY_ADDRESS_MAIN = "address.main";

	public static final String KEY_ADDRESS_GREY = "address.grey";

	public static final String KEY_ADDRESS_48 = "address.48";

	public static final String KEY_EMAIL = "email";

	public static final String KEY_DONATION = "donation";

	public static final String KEY_PARTICIPANT = "participator";

	public static final String KEY_ADDRESS_GROUP_MEMBER = "address.group.member";

	public static final String KEY_FORM_EDITOR_BG = "form.editor.bg";

	public static final String KEY_DELETE = "delete";

	public static final String KEY_DELETE_INACTIVE = "delete.inactive";

	public static final String KEY_ON = "bulp.on";

	public static final String KEY_OFF = "bulp.off";

	public static final String KEY_BANK_CARD = "bank.card";
	
	public static final String KEY_WARN = "warn";

	public static final String KEY_OK = "ok";
	
	public static final String KEY_CLEAR = "clear";
	
	public static final String KEY_CATEGORY = "category";
	
	public static final String KEY_ADDRESS_GROUP = "address.group";
	
	public static final String KEY_CONTACTS = "contacts";
	
	// The shared instance
	private static Activator plugin;

	private FormColors formColors;

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	/**
	 * The constructor
	 */
	public Activator()
	{
	}

	public ConnectionService getConnectionService()
	{
		return connectionServiceTracker.getService();
	}

	public FormColors getFormColors(final Display display)
	{
		if (formColors == null)
		{
			formColors = new FormColors(display);
			formColors.markShared();
		}
		return formColors;
	}

	@Override
	protected void initializeImageRegistry(final ImageRegistry imageRegistry)
	{
		super.initializeImageRegistry(imageRegistry);
		imageRegistry.put(KEY_PERSON_BLUE,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/person_blue_16.png")));
		imageRegistry.put(KEY_PERSON_GREY,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/person_grey_16.png")));
		imageRegistry.put(KEY_PERSON_RED,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/person_red_16.png")));
		imageRegistry.put(KEY_EDIT, ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/edit_16.png")));
		imageRegistry.put(KEY_ADDRESS,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/address_16x16.png")));
		imageRegistry.put(KEY_EMAIL, ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/address_16.png")));
		imageRegistry.put(KEY_PERSON_BLUE_WITH_STAR,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/person_blue_with_star_16.png")));
		imageRegistry.put(KEY_PERSON_GREY_WITH_STAR,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/person_grey_with_star_16.png")));
		imageRegistry.put(KEY_ADDRESS_48,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/address_48x48.png")));
		imageRegistry.put(KEY_DONATION, ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/money.png")));
		imageRegistry.put(KEY_PARTICIPANT,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/person_red_16.png")));
		imageRegistry.put(KEY_FORM_EDITOR_BG,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/form_banner.gif")));
		imageRegistry.put(KEY_DELETE, ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/delete_16.png")));
		imageRegistry.put(KEY_DELETE_INACTIVE,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/delete_grey_16.png")));
		imageRegistry.put(KEY_ON, ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/bulp_on.png")));
		imageRegistry.put(KEY_OFF, ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/bulp_off.png")));
		imageRegistry.put(KEY_ADDRESS_MAIN,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/address_16x16.png")));
		imageRegistry.put(KEY_ADDRESS_GREY,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/home_grey_16.png")));
		imageRegistry.put(KEY_CHANGE_ADDRESS,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/change_16.png")));
		imageRegistry.put(KEY_BANK_CARD, ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/bank-cards-16.png")));
		imageRegistry.put(KEY_WARN,
				ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/warn_16.gif")));
		imageRegistry.put(KEY_OK, ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/ok_16.gif")));
		imageRegistry.put(KEY_CLEAR, ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/clear_16.png")));
		imageRegistry.put(KEY_CATEGORY, ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/category_16.png")));
		imageRegistry.put(KEY_ADDRESS_GROUP, ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/addressgroup_16.png")));
		imageRegistry.put(KEY_CONTACTS, ImageDescriptor.createFromURL(this.getBundle().getEntry("/icons/contact_16.png")));
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

		this.connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(this.getBundle().getBundleContext(),
				ConnectionService.class, null);
		this.connectionServiceTracker.open();

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
		this.connectionServiceTracker.close();
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
}
