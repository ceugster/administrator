package ch.eugster.events.persistence.formatters;

import java.util.ArrayList;
import java.util.List;

import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.PersonSettings;

public class LinkPersonAddressFormatter extends AbstractFormatter
{
	private static LinkPersonAddressFormatter instance;

	private String formatLabel(final LinkPersonAddress link)
	{
		String labelPattern = PersonSettings.getInstance().getPersonLabelFormat();
		if (labelPattern.isEmpty())
		{
			labelPattern = PersonFormatter.getInstance().createVisiblePersonLabel();
			labelPattern = PersonFormatter.getInstance().convertPersonLabelToStored(labelPattern);
		}
		return formatPatternLabel(link, labelPattern);
	}

	private String formatPatternLabel(final LinkPersonAddress link, final String label)
	{
		List<String> lines = new ArrayList<String>();
		String[] addressLabelFormat = label.split("[|]");
		String[] variables = PersonFormatter.getInstance().getPersonLabelStoredVariables();
		for (String labelFormat : addressLabelFormat)
		{
			for (String variable : variables)
			{
				if (variable.equals("${salutation}"))
				{
					String salutation = link.getPerson().getSex() == null ? "" : link.getPerson().getSex()
							.getSalutation();
					labelFormat = labelFormat.replace(variable, salutation);
				}
				else if (variable.equals("${title}"))
				{
					labelFormat = labelFormat.replace(variable, link.getPerson().getTitle() == null ? "" : link
							.getPerson().getTitle().getTitle());
				}
				else if (variable.equals("${organization}"))
				{
					if (link.getAddress().getSalutation() == null
							|| link.getAddress().getSalutation().isShowAddressNameForPersons())
					{
						labelFormat = labelFormat.replace(variable, link.getAddress().getName());
					}
					else
					{
						labelFormat = labelFormat.replace(variable, "");
					}
				}
				else if (variable.equals("${firstname}"))
				{
					labelFormat = labelFormat.replace(variable, link.getPerson().getFirstname());
				}
				else if (variable.equals("${lastname}"))
				{
					labelFormat = labelFormat.replace(variable, link.getPerson().getLastname());
				}
				else if (variable.equals("${function}"))
				{
					labelFormat = labelFormat.replace(variable, link.getFunction() == null ? "" : link.getFunction());
				}
				else if (variable.equals("${anotherline}"))
				{
					labelFormat = labelFormat.replace(variable, link.getAddress().getAnotherLine());
				}
				else if (variable.equals("${address}"))
				{
					labelFormat = labelFormat.replace(variable, link.getAddress().getAddress());
				}
				else if (variable.equals("${pob}"))
				{
					labelFormat = labelFormat.replace(variable, link.getAddress().getPob());
				}
				else if (variable.equals("${country}"))
				{
					labelFormat = labelFormat.replace(variable, link.getAddress().getCountry() == null ? "" : link
							.getAddress().getCountry().getIso3166alpha2());
				}
				else if (variable.equals("${zip}"))
				{
					labelFormat = labelFormat.replace(variable, link.getAddress().getZip());
				}
				else if (variable.equals("${city}"))
				{
					labelFormat = labelFormat.replace(variable, link.getAddress().getCity());
				}
			}
			labelFormat = labelFormat.trim();
			if (!labelFormat.isEmpty())
			{
				lines.add(labelFormat);
			}
		}

		StringBuilder newLabel = new StringBuilder();
		for (String line : lines)
		{
			if (newLabel.length() > 0)
			{
				newLabel = newLabel.append("\n");
			}
			newLabel = newLabel.append(line);
		}
		return newLabel.toString().replace("  ", " ");
	}

	public String getLabel(final LinkPersonAddress link)
	{
		return this.formatLabel(link);
	}

	public static LinkPersonAddressFormatter getInstance()
	{
		if (instance == null)
		{
			instance = new LinkPersonAddressFormatter();
		}
		return instance;
	}
}
