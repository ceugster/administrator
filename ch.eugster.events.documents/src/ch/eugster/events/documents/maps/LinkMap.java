package ch.eugster.events.documents.maps;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.documents.Activator;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.FieldExtensionTarget;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.LinkPersonAddressExtendedField;
import ch.eugster.events.persistence.model.PersonExtendedField;
import ch.eugster.events.persistence.queries.FieldExtensionQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class LinkMap extends AbstractDataMap
{
	private static NumberFormat amountFormatter = null;

	public LinkMap(final LinkPersonAddress link)
	{
		this(link, null);
	}

	public LinkMap(final LinkPersonAddress link, final Integer year)
	{
		if (amountFormatter == null)
		{
			amountFormatter = DecimalFormat.getNumberInstance();
			amountFormatter.setMinimumFractionDigits(Currency.getInstance(Locale.getDefault())
					.getDefaultFractionDigits());
			amountFormatter.setMaximumFractionDigits(Currency.getInstance(Locale.getDefault())
					.getDefaultFractionDigits());
			amountFormatter.setGroupingUsed(true);
		}

		this.setProperties(new AddressMap(link.getAddress()).getProperties());
		this.setProperties(new PersonMap(link.getPerson()).getProperties());

		for (Key key : Key.values())
		{
			if (year == null)
			{
				this.setProperty(key.getKey(), key.getValue(link));
			}
			else
			{
				this.setProperty(key.getKey(), key.getValue(link, year.intValue()));
			}
		}

		for (TableKey key : TableKey.values())
		{
			this.addTableMaps(key.getKey(), key.getTableMaps(link, year));
		}

		Collection<LinkPersonAddressExtendedField> extendedFields = link.getExtendedFields();
		for (LinkPersonAddressExtendedField extendedField : extendedFields)
		{
			ExtendedFieldKey key = new ExtendedFieldKey(extendedField.getFieldExtension());
			if (!extendedField.getFieldExtension().isDeleted())
			{
				String value = extendedField.isDeleted() ? PersonExtendedField.stringValueOf(extendedField
						.getFieldExtension().getDefaultValue()) : PersonExtendedField.stringValueOf(extendedField
						.getValue());
				this.setProperty(key.getKey(), value);
			}
		}
	}

	public static Collection<DataMapKey> getExtendedFieldKeys()
	{
		Collection<DataMapKey> keys = new ArrayList<DataMapKey>();
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		Object service = tracker.getService();
		if (service instanceof ConnectionService)
		{
			ConnectionService connectionService = (ConnectionService) service;
			FieldExtensionQuery query = (FieldExtensionQuery) connectionService.getQuery(FieldExtension.class);
			Collection<FieldExtension> fieldExtensions = query.selectByTarget(FieldExtensionTarget.PA_LINK, false);
			for (FieldExtension fieldExtension : fieldExtensions)
			{
				keys.add(new ExtendedFieldKey(fieldExtension));
			}
		}
		tracker.close();
		return keys;
	}

	public enum Key implements DataMapKey
	{
		PHONE, FUNCTION, MAILING_ADDRESS, TOTAL_DONATIONS;

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case PHONE:
				{
					return "Telefon";
				}
				case FUNCTION:
				{
					return "Funktion";
				}
				case MAILING_ADDRESS:
				{
					return "Anschrift (Person)";
				}
				case TOTAL_DONATIONS:
				{
					return "Gesamter Spendenbetrag";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		@Override
		public String getKey()
		{
			switch (this)
			{
				case PHONE:
				{
					return "link_phone";
				}
				case FUNCTION:
				{
					return "link_function";
				}
				case MAILING_ADDRESS:
				{
					return "mailing_address";
				}
				case TOTAL_DONATIONS:
				{
					return "total_donations";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		@Override
		public String getName()
		{
			switch (this)
			{
				case PHONE:
				{
					return "Telefon";
				}
				case FUNCTION:
				{
					return "Funktion";
				}
				case MAILING_ADDRESS:
				{
					return "Anschrift";
				}
				case TOTAL_DONATIONS:
				{
					return "Spendenbetrag";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final LinkPersonAddress link)
		{
			switch (this)
			{
				case PHONE:
				{
					return LinkPersonAddressFormatter.getInstance().formatPhoneWithOptionalPrefix(
							link.getAddress().getCountry(), link.getPhone());
				}
				case FUNCTION:
				{
					return link.getFunction();
				}
				case MAILING_ADDRESS:
				{
					return LinkPersonAddressFormatter.getInstance().getLabel(link);
				}
				case TOTAL_DONATIONS:
				{
					double totalAmount = 0D;
					Collection<Donation> donations = link.getPerson().getDonations();
					for (Donation donation : donations)
					{
						if (!donation.isDeleted())
						{
							totalAmount += donation.getAmount();
						}
					}
					return amountFormatter.format(totalAmount);
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final LinkPersonAddress link, final int year)
		{
			switch (this)
			{
				case PHONE:
				{
					return LinkPersonAddressFormatter.getInstance().formatPhoneWithOptionalPrefix(
							link.getAddress().getCountry(), link.getPhone());
				}
				case FUNCTION:
				{
					return link.getFunction();
				}
				case MAILING_ADDRESS:
				{
					return LinkPersonAddressFormatter.getInstance().getLabel(link);
				}
				case TOTAL_DONATIONS:
				{
					double totalAmount = 0D;
					Collection<Donation> donations = link.getPerson().getDonations();
					for (Donation donation : donations)
					{
						if (!donation.isDeleted())
						{
							if (year == donation.getDonationYear())
							{
								totalAmount += donation.getAmount();
							}
						}
					}
					return amountFormatter.format(totalAmount);
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}

	public enum TableKey
	{
		DONATION;

		public String getDescription()
		{
			switch (this)
			{
				case DONATION:
				{
					return "Spenden";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getKey()
		{
			switch (this)
			{
				case DONATION:
				{
					return "donations";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public List<DataMap> getTableMaps(final LinkPersonAddress link)
		{
			switch (this)
			{
				case DONATION:
				{
					return this.getTableMaps(link, null);
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public List<DataMap> getTableMaps(final LinkPersonAddress link, final Integer year)
		{
			switch (this)
			{
				case DONATION:
				{
					List<DataMap> tableMaps = new ArrayList<DataMap>();
					Collection<Donation> donations = link.getPerson().getDonations();
					for (Donation donation : donations)
					{
						if (!donation.isDeleted())
						{
							if (year == null)
							{
								tableMaps.add(new DonationMap(donation));
							}
							else
							{
								if (donation.getDonationYear() == year.intValue())
								{
									tableMaps.add(new DonationMap(donation));
								}
							}
						}
					}
					return tableMaps;
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}
}
