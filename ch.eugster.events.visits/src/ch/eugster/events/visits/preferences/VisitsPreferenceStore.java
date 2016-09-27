package ch.eugster.events.visits.preferences;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.persistence.queries.GlobalSettingsQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.visits.Activator;

public class VisitsPreferenceStore extends ScopedPreferenceStore
{
	private AddressType[] availableAddressTypes;

	public VisitsPreferenceStore()
	{
		super(InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		setAvailableAddressTypes();

		AddressType addressType = Activator.getDefault().getSettings().getDefaultAddressType();
		if (addressType == null)
		{
			this.setValue(PreferenceInitializer.KEY_DEFAULT_ADDRESS_TYPE, 0);
		}
		else
		{
			if (availableAddressTypes.length == 1)
			{
				this.setValue(PreferenceInitializer.KEY_DEFAULT_ADDRESS_TYPE, 0);
			}
			else
			{
				for (int i = 1; i < availableAddressTypes.length; i++)
				{
					if (availableAddressTypes[i].getId().equals(addressType.getId()))
					{
						this.setValue(PreferenceInitializer.KEY_DEFAULT_ADDRESS_TYPE, i);
					}
				}
			}
		}
	}

	public AddressType[] getAvailableAddressTypes()
	{
		return availableAddressTypes;
	}

	@Override
	public void save()
	{
		if (this.needsSaving())
		{
			Activator
					.getDefault()
					.getSettings()
					.setDefaultAddressType(
							availableAddressTypes[this.getInt(PreferenceInitializer.KEY_DEFAULT_ADDRESS_TYPE)]);

			ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class, null);
			tracker.open();
			try
			{
				ConnectionService service = (ConnectionService) tracker.getService();
				if (service != null)
				{
					GlobalSettingsQuery query = (GlobalSettingsQuery) service.getQuery(GlobalSettings.class);
					GlobalSettings.setInstance(query.merge(GlobalSettings.getInstance()));
				}

				super.save();
			}
			catch (Exception e)
			{
			}
			finally
			{
				tracker.close();
			}
		}
	}

	private void setAvailableAddressTypes()
	{
		Collection<AddressType> addressTypes = new ArrayList<AddressType>();
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				AddressTypeQuery query = (AddressTypeQuery) service.getQuery(AddressType.class);
				addressTypes.addAll(query.selectAll(false));
			}
		}
		finally
		{
			tracker.close();
		}
		availableAddressTypes = addressTypes.toArray(new AddressType[0]);
	}

}
