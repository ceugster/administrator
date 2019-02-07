package ch.eugster.events.persistence.formatters;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class PhoneFormatter 
{
	private static PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

	public static String format(Person person)
	{
		if (person.getPhone().isEmpty())
		{
			return "";
		}
		return format(person.getPhone(), person.getCountry());
	}
	
	public static String format(Address address)
	{
		if (address.getPhone().isEmpty())
		{
			return "";
		}
		return format(address.getPhone(), address.getCountry());
	}
	
	public static String format(LinkPersonAddress link)
	{
		if (link.getPhone().isEmpty())
		{
			return "";
		}
		return format(link.getPhone(), link.getAddress().getCountry());
	}
	
	public static String format(String phoneNumber, Country country)
	{
		if (country == null)
		{
			return "";
		}
		return format(phoneNumber, country.getIso3166alpha2());
	}
	
	public static String format(String phoneNumber, String countryCode)
	{
		try 
		{
			PhoneNumber number = phoneUtil.parse(phoneNumber, countryCode);
			return phoneUtil.format(number, PhoneNumberFormat.NATIONAL);
		} 
		catch (NumberParseException e) 
		{
			return "";
		}
	}
}
