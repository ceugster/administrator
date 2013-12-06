package ch.eugster.events.persistence.formatters;

import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.GlobalSettings;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public abstract class AbstractFormatter
{
	private PhoneNumberUtil phoneFormatter = PhoneNumberUtil.getInstance();

	private final Country defaultCountry;

	protected AbstractFormatter()
	{
		defaultCountry = GlobalSettings.getInstance() == null ? null : GlobalSettings.getInstance().getCountry();
	}

	public String formatPhone(final Country country, final String phone)
	{
		String number = "";
		if (phone != null && !phone.isEmpty())
		{
			try
			{
				PhoneNumber phoneNumber = null;
				if (country == null)
				{
					phoneNumber = phoneFormatter.parse(phone, "CH");
				}
				else
				{
					phoneNumber = phoneFormatter.parse(phone, country.getIso3166alpha2());
				}
				number = phoneFormatter.format(phoneNumber, PhoneNumberFormat.NATIONAL);
			}
			catch (NumberParseException e)
			{
				return "";
			}
		}
		return number;
	}

	public String formatPhoneWithOptionalPrefix(final Country country, final String phone)
	{
		String number = "";
		if (phone != null && !phone.isEmpty())
		{
			try
			{
				PhoneNumber phoneNumber = null;
				if (country == null)
				{
					phoneNumber = phoneFormatter.parse(phone, "CH");
				}
				else
				{
					phoneNumber = phoneFormatter.parse(phone, country.getIso3166alpha2());
				}
				if (country.getId().equals(defaultCountry.getId()))
				{
					number = phoneFormatter.format(phoneNumber, PhoneNumberFormat.NATIONAL);
				}
				else
				{
					number = phoneFormatter.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL);
				}
			}
			catch (NumberParseException e)
			{
				return "";
			}
		}
		return number;
	}

	public String formatPhoneWithPrefix(final Country country, final String phone)
	{
		String number = "";
		if (phone != null && !phone.isEmpty())
		{
			try
			{
				PhoneNumber phoneNumber = null;
				if (country == null)
				{
					phoneNumber = phoneFormatter.parse(phone, "CH");
				}
				else
				{
					phoneNumber = phoneFormatter.parse(phone, country.getIso3166alpha2());
				}
				number = phoneFormatter.format(phoneNumber, PhoneNumberFormat.INTERNATIONAL);
			}
			catch (NumberParseException e)
			{
				return "";
			}
		}
		return number;
	}

	public Country getCountry()
	{
		return defaultCountry;
	}

}
