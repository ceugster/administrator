package ch.eugster.events.documents.maps;

import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.documents.Activator;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.FieldExtensionTarget;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.LinkPersonAddressExtendedField;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.queries.FieldExtensionQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class LinkMap extends AbstractDataMap<LinkPersonAddress>
{
	protected LinkMap() {
		super();
	}

	public LinkMap(final LinkPersonAddress link)
	{
		this(link, null, null, null, null, false);
	}

	public LinkMap(final LinkPersonAddress link, final boolean isGroup)
	{
		this(link, null, null, null, null, isGroup);
	}
	
	public LinkMap(Donation donation)
	{
		this.setProperties(new AddressMap(donation.getLink().getAddress()).getProperties());
		this.setProperties(new PersonMap(donation.getLink().getPerson()).getProperties());

		for (final Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(donation.getLink()));
		}

		this.addTableMaps(TableKey.DONATIONS.getKey(), TableKey.DONATIONS.getTableMaps(donation));

		final List<LinkPersonAddressExtendedField> extendedFields = donation.getLink().getExtendedFields();
		for (final LinkPersonAddressExtendedField extendedField : extendedFields)
		{
			final ExtendedFieldKey key = new ExtendedFieldKey(extendedField.getFieldExtension());
			if (!extendedField.getFieldExtension().isDeleted())
			{
				final String value = extendedField.isDeleted() ? AbstractEntity.stringValueOf(extendedField
						.getFieldExtension().getDefaultValue()) : AbstractEntity.stringValueOf(extendedField
						.getValue());
				this.setProperty(key.getKey(), value);
			}
		}
	}
	
	public LinkMap(final LinkPersonAddress link, final DonationYear selectedFromDonationYear, final DonationYear selectedToDonationYear, final DonationPurpose purpose, final Domain domain, final boolean isGroup)
	{
		this.setProperties(new AddressMap(link.getAddress(), isGroup).getProperties());
		this.setProperties(new PersonMap(link.getPerson()).getProperties());

		for (final Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(link));
		}

		for (final TableKey key : TableKey.values())
		{
			this.addTableMaps(key.getKey(), key.getTableMaps(link, selectedFromDonationYear, selectedToDonationYear, purpose, domain));
		}

		final List<LinkPersonAddressExtendedField> extendedFields = link.getExtendedFields();
		for (final LinkPersonAddressExtendedField extendedField : extendedFields)
		{
			final ExtendedFieldKey key = new ExtendedFieldKey(extendedField.getFieldExtension());
			if (!extendedField.getFieldExtension().isDeleted())
			{
				final String value = extendedField.isDeleted() ? AbstractEntity.stringValueOf(extendedField
						.getFieldExtension().getDefaultValue()) : AbstractEntity.stringValueOf(extendedField
						.getValue());
				this.setProperty(key.getKey(), value);
			}
		}
	}

	public static List<DataMapKey> getExtendedFieldKeys()
	{
		final List<DataMapKey> keys = new ArrayList<DataMapKey>();
		final ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			final Object service = tracker.getService();
			if (service instanceof ConnectionService)
			{
				final ConnectionService connectionService = (ConnectionService) service;
				final FieldExtensionQuery query = (FieldExtensionQuery) connectionService.getQuery(FieldExtension.class);
				final List<FieldExtension> fieldExtensions = query.selectByTarget(FieldExtensionTarget.PA_LINK, false);
				for (final FieldExtension fieldExtension : fieldExtensions)
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

	@Override
	protected void printReferences(final Writer writer)
	{
		this.printHeader(writer, 2, "Referenzen");
		this.startTable(writer, 0);
		this.startTableRow(writer);
		this.printCell(writer, "#person", "Person");
		this.endTableRow(writer);
		this.startTableRow(writer);
		this.printCell(writer, "#address", "Adresse");
		this.endTableRow(writer);
		this.endTable(writer);
	}

	@Override
	protected void printTables(final Writer writer)
	{
		this.printHeader(writer, 2, "Tabellen");
		this.startTable(writer, 0);
		this.startTableRow(writer);
		this.printCell(writer, null, TableKey.DONATIONS.getKey());
		this.printCell(writer, "#donation", "Spenden");
		this.endTableRow(writer);
		this.endTable(writer);
	}

	public enum Key implements DataMapKey
	{
		PHONE, EMAIL, FUNCTION, MAILING_ADDRESS, TOTAL_DONATIONS, MEMBER, COURSE_VISITS;
		
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
					return "0";
				}
				case MEMBER:
				{
					StringBuilder builder = new StringBuilder();
					final Member[] members = link.getMembers().toArray(new Member[0]);
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
					final List<Participant> participants = link.getParticipants();
					final Map<Long, String> courses = new HashMap<Long, String>();
					for (final Participant participant: participants)
					{
						if (!participant.isDeleted() && !participant.getBooking().isDeleted() && !participant.getBooking().getCourse().isDeleted())
						{
							if (participant.getBooking().getBookingState(participant.getBooking().getCourse().getState()).equals(BookingForthcomingState.BOOKED))
							{
								final Course course = participant.getBooking().getCourse();
								final List<CourseDetail> details = course.getCourseDetails();
								final Calendar start = details.isEmpty() ? null : details.get(0).getStart();
								final String title = course.getTitle();
								final String date = start == null ? "ohne Datum" : DateFormat.getInstance().format(start.getTime());
								courses.put(participant.getBooking().getCourse().getId(), title + " (" + date + ", angemelded)\n");
							}
							else if (participant.getBooking().getBookingState(participant.getBooking().getCourse().getState()).equals(BookingDoneState.PARTICIPATED))
							{
								final Course course = participant.getBooking().getCourse();
								final List<CourseDetail> details = course.getCourseDetails();
								final Calendar start = details.isEmpty() ? null : details.get(0).getStart();
								final String title = course.getTitle();
								final String date = start == null ? "ohne Datum" : DateFormat.getInstance().format(start.getTime());
								courses.put(participant.getBooking().getCourse().getId(), title + " (" + date + ", teilgenommen)\n");
							}
						}
					}
					final Collection<String> values = courses.values();
					StringBuilder builder = new StringBuilder();
					for (final String value : values)
					{
						builder = builder.append(value);
					}
					final String visits = builder.toString();
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

		public List<DataMap<?>> getTableMaps(Donation donation)
		{
			final List<DataMap<?>> tableMaps = new ArrayList<DataMap<?>>();
			tableMaps.add(new DonationMap(donation));
			return tableMaps;
		}

		public List<DataMap<?>> getTableMaps(final LinkPersonAddress link, final DonationYear fromYear, final DonationYear toYear, final DonationPurpose purpose,
				final Domain domain)
		{
			switch (this)
			{
				case DONATIONS:
				{
					final List<DataMap<?>> tableMaps = new ArrayList<DataMap<?>>();
					final List<Donation> donations = link.getDonations();
					for (final Donation donation : donations)
					{
						if (domain == null || (donation.getDomain() != null && domain.equals(donation.getDomain())))
						{
							if (purpose == null || (donation.getPurpose() != null && purpose.equals(donation.getPurpose())))
							{
								this.addDonation(donation, tableMaps, fromYear, toYear, purpose, domain);
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

		private void addDonation(final Donation donation, final List<DataMap<?>> tableMaps, final DonationYear fromYear, final DonationYear toYear, final DonationPurpose purpose,
				final Domain domain)
		{
			if (this.printDonation(donation, purpose, domain))
			{
				if (fromYear == null || donation.getDonationYear() >= fromYear.getYear())
				{
					if (toYear == null || donation.getDonationYear() <= toYear.getYear())
					{
						tableMaps.add(new DonationMap(donation));
					}
				}
			}
		}

		private boolean printDonation(final Donation donation, final DonationPurpose purpose, final Domain domain)
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
