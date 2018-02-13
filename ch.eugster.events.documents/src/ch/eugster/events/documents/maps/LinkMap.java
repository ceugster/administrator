package ch.eugster.events.documents.maps;

import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.documents.Activator;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.FieldExtensionTarget;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.LinkPersonAddressExtendedField;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.PersonExtendedField;
import ch.eugster.events.persistence.queries.FieldExtensionQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class LinkMap extends AbstractDataMap<LinkPersonAddress>
{
	private static NumberFormat amountFormatter = null;
	
	private Integer year;
	
	private Domain domain;
	
	protected LinkMap() {
		super();
	}

	public LinkMap(final LinkPersonAddress link)
	{
		this(link, null, null, null, false);
	}

	public LinkMap(final LinkPersonAddress link, boolean isGroup)
	{
		this(link, null, null, null, isGroup);
	}

	public LinkMap(final LinkPersonAddress link, final Integer year, DonationPurpose purpose, Domain domain, boolean isGroup)
	{
		this.year = year;
		this.domain = domain;
		
		if (amountFormatter == null)
		{
			amountFormatter = DecimalFormat.getNumberInstance();
			amountFormatter.setMinimumFractionDigits(Currency.getInstance(Locale.getDefault())
					.getDefaultFractionDigits());
			amountFormatter.setMaximumFractionDigits(Currency.getInstance(Locale.getDefault())
					.getDefaultFractionDigits());
			amountFormatter.setGroupingUsed(true);
		}

		this.setProperties(new AddressMap(link.getAddress(), isGroup).getProperties());
		this.setProperties(new PersonMap(link.getPerson()).getProperties());

		Key.domain = this.domain;
		Key.year = this.year;
		
		for (Key key : Key.values())
		{
			if (year == null)
			{
				this.setProperty(key.getKey(), key.getValue(link));
			}
			else
			{
				this.setProperty(key.getKey(), key.getValue(link));
			}
		}

		for (TableKey key : TableKey.values())
		{
			this.addTableMaps(key.getKey(), key.getTableMaps(link, year, purpose, domain));
		}

		List<LinkPersonAddressExtendedField> extendedFields = link.getExtendedFields();
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

	public static List<DataMapKey> getExtendedFieldKeys()
	{
		List<DataMapKey> keys = new ArrayList<DataMapKey>();
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			Object service = tracker.getService();
			if (service instanceof ConnectionService)
			{
				ConnectionService connectionService = (ConnectionService) service;
				FieldExtensionQuery query = (FieldExtensionQuery) connectionService.getQuery(FieldExtension.class);
				List<FieldExtension> fieldExtensions = query.selectByTarget(FieldExtensionTarget.PA_LINK, false);
				for (FieldExtension fieldExtension : fieldExtensions)
				{
					keys.add(new ExtendedFieldKey(fieldExtension));
				}
			}
		}
		finally
		{
			tracker.close();
		}
		return keys;
	}

	protected void printReferences(Writer writer)
	{
		printHeader(writer, 2, "Referenzen");
		startTable(writer, 0);
		startTableRow(writer);
		printCell(writer, "#person", "Person");
		endTableRow(writer);
		startTableRow(writer);
		printCell(writer, "#address", "Adresse");
		endTableRow(writer);
		endTable(writer);
	}

	protected void printTables(Writer writer)
	{
		printHeader(writer, 2, "Tabellen");
		startTable(writer, 0);
		startTableRow(writer);
		printCell(writer, null, TableKey.DONATIONS.getKey());
		printCell(writer, "#donation", "Spenden");
		endTableRow(writer);
		endTable(writer);
	}

	public enum Key implements DataMapKey
	{
		PHONE, EMAIL, FUNCTION, MAILING_ADDRESS, TOTAL_DONATIONS, MEMBER, COURSE_VISITS;
		
		public static Domain domain;
		
		public static Integer year;

		@Override
		public Class<?> getType()
		{
			return String.class;
		}

		@Override
		public String getDescription()
		{
			switch (this)
			{
				case PHONE:
				{
					return "Telefon";
				}
				case EMAIL:
				{
					return "Email";
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
				case MEMBER:
				{
					return "Mitglied";
				}
				case COURSE_VISITS:
				{
					return "Kursbesuche";
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
				case EMAIL:
				{
					return "link_email";
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
				case MEMBER:
				{
					return "member";
				}
				case COURSE_VISITS:
				{
					return "course_visits";
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
				case EMAIL:
				{
					return "Email";
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
				case MEMBER:
				{
					return "Mitglied";
				}
				case COURSE_VISITS:
				{
					return "Kursbesuche";
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
				case EMAIL:
				{
					return link.getEmail();
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
					List<Donation> donations = link.getPerson().getDonations();
					for (Donation donation : donations)
					{
						if (!donation.isDeleted())
						{
							if (domain == null || (donation.getDomain() != null && domain.equals(donation.getDomain())))
							{
								totalAmount += donation.getAmount();
							}
						}
					}
					return amountFormatter.format(totalAmount);
				}
				case MEMBER:
				{
					StringBuilder builder = new StringBuilder();
					Member[] members = link.getMembers().toArray(new Member[0]);
					for (int i = 0; i < members.length; i++)
					{
						builder = builder.append(members[i].getMembership().getName());
						if (i < members.length - 1)
						{
							builder = builder.append(", ");
						}
					}
					return builder.toString();
				}
				case COURSE_VISITS:
				{
					List<Participant> participants = link.getParticipants();
					Map<Long, String> courses = new HashMap<Long, String>();
					for (Participant participant: participants)
					{
						if (!participant.isDeleted() && !participant.getBooking().isDeleted() && !participant.getBooking().getCourse().isDeleted())
						{
							if (participant.getBooking().getBookingState(participant.getBooking().getCourse().getState()).equals(BookingForthcomingState.BOOKED))
							{
								Course course = participant.getBooking().getCourse();
								List<CourseDetail> details = course.getCourseDetails();
								Calendar start = details.isEmpty() ? null : details.get(0).getStart();
								String title = course.getTitle();
								String date = start == null ? "ohne Datum" : SimpleDateFormat.getInstance().format(start.getTime());
								courses.put(participant.getBooking().getCourse().getId(), title + " (" + date + ", angemelded)\n");
							}
							else if (participant.getBooking().getBookingState(participant.getBooking().getCourse().getState()).equals(BookingDoneState.PARTICIPATED))
							{
								Course course = participant.getBooking().getCourse();
								List<CourseDetail> details = course.getCourseDetails();
								Calendar start = details.isEmpty() ? null : details.get(0).getStart();
								String title = course.getTitle();
								String date = start == null ? "ohne Datum" : SimpleDateFormat.getInstance().format(start.getTime());
								courses.put(participant.getBooking().getCourse().getId(), title + " (" + date + ", teilgenommen)\n");
							}
						}
					}
					Collection<String> values = courses.values();
					StringBuilder builder = new StringBuilder();
					for (String value : values)
					{
						builder = builder.append(value);
					}
					String visits = builder.toString();
					return visits.substring(0, visits.isEmpty() ? 0 : visits.length()  - 2);
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
		DONATIONS;

		public String getDescription()
		{
			switch (this)
			{
				case DONATIONS:
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
				case DONATIONS:
				{
					return "table_donations";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public List<DataMap<?>> getTableMaps(final LinkPersonAddress link, final Integer year, DonationPurpose purpose,
				Domain domain)
		{
			switch (this)
			{
				case DONATIONS:
				{
					List<DataMap<?>> tableMaps = new ArrayList<DataMap<?>>();
					List<Donation> donations = link.getPerson().getDonations();
					for (Donation donation : donations)
					{
						if (domain == null || (donation.getDomain() != null && domain.equals(donation.getDomain())))
						{
							if (year == null || year.intValue() == donation.getDonationYear())
							{
								addDonation(donation, tableMaps, year, purpose, domain);
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

		private void addDonation(Donation donation, List<DataMap<?>> tableMaps, Integer year, DonationPurpose purpose,
				Domain domain)
		{
			if (printDonation(donation, purpose, domain))
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

		private boolean printDonation(Donation donation, DonationPurpose purpose, Domain domain)
		{
			if (donation.isDeleted())
			{
				return false;
			}
			if (purpose == null && domain == null)
			{
				return true;
			}

			if (purpose == null)
			{
				if (domain.getId() != null)
				{
					if (donation.getDomain() == null)
					{
						return false;
					}
					else
					{
						return domain.getId().equals(donation.getDomain().getId());
					}
				}
			}
			if (domain == null)
			{
				return donation.getPurpose().getId().equals(purpose.getId());
			}
			if (donation.getDomain() == null || donation.getPurpose() == null)
			{
				return false;
			}
			if (donation.getDomain() != null && domain.getId().equals(donation.getDomain().getId()))
			{
				if (donation.getPurpose().getId().equals(purpose.getId()))
				{
					return true;
				}
			}
			return false;
		}

	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}
}
