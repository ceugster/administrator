package ch.eugster.events.course.reporting;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.IBookingState;
import ch.eugster.events.persistence.model.Participant;

public class ParticipantListReportItem implements Comparable<ParticipantListReportItem>
{
	private Participant participant;

	public ParticipantListReportItem()
	{
		super();
	}

	/**
	 * Load Address
	 * 
	 * @param member
	 */
	public ParticipantListReportItem(final Participant participant)
	{
		this.participant = participant;
	}

	@Override
	public int compareTo(final ParticipantListReportItem other)
	{
		int comparison = 0;
		ParticipantListReportItem participant = other;
		if (this.getState().ordinal() < other.getState().ordinal())
		{
			comparison = -1;
		}
		else if (this.getState().ordinal() > other.getState().ordinal())
		{
			comparison = 1;
		}
		else
		{
			comparison = this.getName().compareTo(participant.getName());
			if (comparison == 0)
			{
				return this.getId().compareTo(participant.getId());
			}
		}
		return comparison;
	}

	public String getAddress()
	{
		return AddressFormatter.getInstance().formatAddressLine(participant.getLink().getAddress());
	}

	public String getCity()
	{
		return AddressFormatter.getInstance().formatCityLine(participant.getLink().getAddress());
	}

	public String getCorrespondant()
	{
		return participant.getBooking().getParticipant().getId().equals(participant.getId()) ? "A" : "";
	}

	public int getCount()
	{
		return participant.getCount();
	}

	public String getEmail()
	{
		String email = participant.getLink().getEmail();
		if (email.isEmpty())
		{
			email = participant.getLink().getPerson().getEmail();
		}
		if (email.isEmpty())
		{
			email = participant.getLink().getAddress().getEmail();
		}
		return email;
	}

	public String getId()
	{
		return PersonFormatter.getInstance().formatId(participant.getLink().getPerson());
	}

	public String getMobile()
	{
		return LinkPersonAddressFormatter.getInstance().formatPhoneWithOptionalPrefix(
				participant.getLink().getPerson().getCountry(), participant.getLink().getPerson().getPhone());
	}

	public String getName()
	{
		return PersonFormatter.getInstance().formatLastnameFirstname(participant.getLink().getPerson());
	}

	public Participant getParticipant()
	{
		return this.participant;
	}

	public String getPhone()
	{
		String phone = null;
		phone = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(
				participant.getLink().getPerson().getCountry(), participant.getLink().getPhone());
		if (phone == null || phone.isEmpty())
		{
			phone = LinkPersonAddressFormatter.getInstance().formatPhoneWithOptionalPrefix(
					participant.getLink().getAddress().getCountry(), participant.getLink().getAddress().getPhone());
		}
		return phone;
	}

	private IBookingState getState()
	{
		return participant.getBooking().getBookingState(participant.getBooking().getCourse().getState());
	}

	public String getStatus()
	{
		return participant.getBooking().getBookingState(participant.getBooking().getCourse().getState()).shortName();
	}

	public String getBookingType()
	{
		if (participant.getBookingType() == null)
		{
			return "";
		}
		return participant.getBookingType().getName();
	}
}
