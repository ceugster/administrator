package ch.eugster.events.persistence.formatters;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.PersonSettings;

public class AddressFormatter extends AbstractFormatter
{
	private NumberFormat nf;

	private static AddressFormatter instance;

	private AddressFormatter()
	{
		super();
		String ft = PersonSettings.getInstance().getIdFormat();
		if (ft == null)
			this.nf = NumberFormat.getIntegerInstance();
		else
			this.nf = new DecimalFormat(ft);
	}

	public String convertAddressLabelToStored(final String visible)
	{
		String stored = visible;
		String[][] variables = getAddressLabelVariablesToConvertToStored();
		for (int i = 0; i < variables.length; i++)
		{
			stored = stored.replace(variables[i][0], variables[i][1]);
		}
		return stored;
	}

	public String convertAddressLabelToVisible(final String stored)
	{
		String visible = stored;
		String[][] variables = getAddressLabelVariablesToConvertToVisible();
		for (int i = 0; i < variables.length; i++)
		{
			visible = visible.replace(variables[i][0], variables[i][1]);
		}
		return visible;
	}

	// public String getAddressLabel(Address address)
	// {
	// if (address.isManualMailingAddress())
	// return address.getMailingAddress();
	// else
	// return AddressMap.getMailingAddress(address);
	// }

	public String createVisibleAddressLabel()
	{
		StringBuilder builder = new StringBuilder("${Anrede}");
		builder = builder.append("|${Organisation}");
		builder = builder.append("|${Zusatz}");
		builder = builder.append("|${Strasse}");
		builder = builder.append("|${Postfach}");
		builder = builder.append("|${Land}-${Plz} ${Ort}");
		return builder.toString();
	}

	public String getLabel(final Address address)
	{
		return this.formatAddressLabel(address);
	}

	public String formatAddressLabel(final Address address)
	{
		String addressLabelPattern = PersonSettings.getInstance().getAddressLabelFormat();
		if (addressLabelPattern.isEmpty())
		{
			addressLabelPattern = createVisibleAddressLabel();
			addressLabelPattern = convertAddressLabelToStored(addressLabelPattern);
		}
		return formatPatternAddressLabel(address, addressLabelPattern);
	}

	public String formatAddressLine(final Address address)
	{
		StringBuilder addressLine = new StringBuilder(address.getAddress());
		if (!address.getPob().isEmpty())
		{
			if (addressLine.length() > 0)
			{
				addressLine = addressLine.append(", ");
			}
			addressLine = addressLine.append(address.getPob());
		}
		return addressLine.toString().trim();
	}

	public String formatCityLine(final Address address)
	{
		String pattern = null;
		String country = "";
		if (address.getCountry() == null || address.getCountry().getCityLinePattern().isEmpty())
		{
			pattern = getDefaultCityLine();
		}
		else
		{
			country = address.getCountry().getIso3166alpha2();
			pattern = address.getCountry().getCityLinePattern();
		}

		pattern = pattern.replace("L", country.isEmpty() ? "" : country);
		if (country.isEmpty())
		{
			pattern = pattern.replace("-", "");
		}
		pattern = pattern.replace("P", address.getZip());
		pattern = pattern.replace("O", address.getCity());
		return pattern;
	}

	public String formatId(final Address address)
	{
		return (address != null && address.getId() != null) ? this.nf.format(address.getId()) : ""; //$NON-NLS-1$
	}

	private String formatPatternAddressLabel(final Address address, final String label)
	{
		StringBuilder newLabel = new StringBuilder();
		List<String> lines = new ArrayList<String>();
		String[] addressLabelFormat = label.split("[|]");
		String[] variables = getAddressLabelStoredVariables();
		for (String labelFormat : addressLabelFormat)
		{
			for (String variable : variables)
			{
				if (variable.equals("${salutation}"))
				{
					labelFormat = labelFormat.replace(variable, address.getSalutation() == null ? "" : address
							.getSalutation().getSalutation());
				}
				else if (variable.equals("${organization}"))
				{
					labelFormat = labelFormat.replace(variable, address.getName());
				}
				else if (variable.equals("${anotherline}"))
				{
					labelFormat = labelFormat.replace(variable, address.getAnotherLine());
				}
				else if (variable.equals("${address}"))
				{
					labelFormat = labelFormat.replace(variable, address.getAddress());
				}
				else if (variable.equals("${pob}"))
				{
					labelFormat = labelFormat.replace(variable, address.getPob());
				}
				else if (variable.equals("${country}"))
				{
					labelFormat = labelFormat.replace(variable, address.getCountry() == null ? "" : address
							.getCountry().getIso3166alpha2());
				}
				else if (variable.equals("${zip}"))
				{
					labelFormat = labelFormat.replace(variable, address.getZip());
				}
				else if (variable.equals("${city}"))
				{
					labelFormat = labelFormat.replace(variable, address.getCity());
				}
			}
			labelFormat = labelFormat.trim();
			if (!labelFormat.isEmpty())
			{
				lines.add(labelFormat);
			}
		}
		for (String line : lines)
		{
			if (newLabel.length() > 0)
			{
				newLabel = newLabel.append("\n");
			}
			newLabel = newLabel.append(line);
		}

		return newLabel.toString();
	}

	public String[] getAddressLabelStoredVariables()
	{
		return new String[] { "${salutation}", "${organization}", "${anotherline}", "${address}", "${pob}",
				"${country}", "${zip}", "${city}" };
	}

	public String[][] getAddressLabelVariablesToConvertToStored()
	{
		String[] visibleVariables = getAddressLabelVisibleVariables();
		String[] storedVariables = getAddressLabelStoredVariables();

		String[][] variables = new String[Math.min(storedVariables.length, visibleVariables.length)][2];
		for (int i = 0; i < variables.length; i++)
		{
			variables[i][0] = visibleVariables[i];
			variables[i][1] = storedVariables[i];
		}
		return variables;
	}

	public String[][] getAddressLabelVariablesToConvertToVisible()
	{
		String[] storedVariables = getAddressLabelStoredVariables();
		String[] visibleVariables = getAddressLabelVisibleVariables();

		String[][] variables = new String[Math.min(storedVariables.length, visibleVariables.length)][2];
		for (int i = 0; i < variables.length; i++)
		{
			variables[i][0] = storedVariables[i];
			variables[i][1] = visibleVariables[i];
		}
		return variables;
	}

	public String[] getAddressLabelVisibleVariables()
	{
		return new String[] { "${Anrede}", "${Organisation}", "${Zusatz}", "${Strasse}", "${Postfach}", "${Land}",
				"${Plz}", "${Ort}" };
	}

	public String getDefaultCityLine()
	{
		return "L-P O";
	}

	public static AddressFormatter getInstance()
	{
		if (instance == null)
		{
			instance = new AddressFormatter();
		}
		return instance;
	}
}
