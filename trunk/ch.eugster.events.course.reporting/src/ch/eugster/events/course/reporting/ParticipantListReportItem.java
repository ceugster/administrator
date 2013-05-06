package ch.eugster.events.course.reporting;

import java.text.DecimalFormat;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Participant;

public class ParticipantListReportItem implements Comparable<ParticipantListReportItem>
{
	private String id;

	private String name;

	private String address;

	private String city;

	private String phone;

	private String mobile;

	private String email;

	private String count;

	private String correspondant;

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
		loadData(participant);
	}

	@Override
	public int compareTo(final ParticipantListReportItem other)
	{
		ParticipantListReportItem participant = other;
		int comparison = this.getName().compareTo(participant.getName());
		if (comparison == 0)
		{
			return this.getId().compareTo(participant.getId());
		}
		return comparison;
	}

	public String getAddress()
	{
		return address;
	}

	public String getCity()
	{
		return city;
	}

	public String getCorrespondant()
	{
		return this.correspondant;
	}

	public String getCount()
	{
		return count;
	}

	public String getEmail()
	{
		return email;
	}

	public String getId()
	{
		return id;
	}

	public String getMobile()
	{
		return mobile;
	}

	public String getName()
	{
		return name;
	}

	public String getPhone()
	{
		return phone;
	}

	private void loadData(final Participant participant)
	{
		this.address = AddressFormatter.getInstance().formatAddressLine(participant.getLink().getAddress());
		this.city = AddressFormatter.getInstance().formatCityLine(participant.getLink().getAddress());
		this.correspondant = participant.getBooking().getParticipant().getId().equals(participant.getId()) ? "A" : "";
		this.count = DecimalFormat.getIntegerInstance().format(participant.getCount());
		this.email = participant.getLink().getEmail();
		this.id = PersonFormatter.getInstance().formatId(participant.getLink().getPerson());
		this.mobile = LinkPersonAddressFormatter.getInstance().formatPhoneWithOptionalPrefix(
				participant.getLink().getPerson().getCountry(), participant.getLink().getPhone());
		this.name = PersonFormatter.getInstance().formatLastnameFirstname(participant.getLink().getPerson());
		this.phone = PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(
				participant.getLink().getPerson().getCountry(), participant.getLink().getPhone());
	}

	public void setAddress(final String address)
	{
		this.address = address;
	}

	public void setCity(final String city)
	{
		this.city = city;
	}

	public void setCorrespondant(final String correspondant)
	{
		this.correspondant = correspondant;
	}

	public void setCount(final String count)
	{
		this.count = count;
	}

	public void setEmail(final String email)
	{
		this.email = email;
	}

	public void setId(final String id)
	{
		this.id = id;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public void setPhone(final String phone)
	{
		this.phone = phone;
	}
}
