package ch.eugster.events.persistence.formatters;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSettings;

public class PersonFormatter extends AbstractFormatter
{
	private NumberFormat nf;

	private static PersonFormatter formatter = null;

	public PersonFormatter()
	{
		super();
		String ft = GlobalSettings.getInstance() == null ? "" : PersonSettings.getInstance().getIdFormat();
		if (ft == null)
			this.nf = NumberFormat.getIntegerInstance();
		else
			this.nf = new DecimalFormat(ft);
	}

	public String convertPersonLabelToStored(final String visible)
	{
		String stored = visible;
		String[][] variables = getPersonLabelVariablesToConvertToStored();
		for (int i = 0; i < variables.length; i++)
		{
			stored = stored.replace(variables[i][0], variables[i][1]);
		}
		return stored;
	}

	public String convertPersonLabelToVisible(final String stored)
	{
		String visible = stored;
		String[][] variables = getPersonLabelVariablesToConvertToVisible();
		for (int i = 0; i < variables.length; i++)
		{
			visible = visible.replace(variables[i][0], variables[i][1]);
		}
		return visible;
	}

	public String convertPoliteToStored(final String visible)
	{
		String stored = visible;
		String[][] variables = getPersonVariablesToConvertToStored();
		for (int i = 0; i < variables.length; i++)
		{
			stored = stored.replace(variables[i][0], variables[i][1]);
		}
		return stored;
	}

	public String convertPoliteToVisible(final String stored)
	{
		String visible = stored;
		String[][] variables = getPersonVariablesToConvertToVisible();
		for (int i = 0; i < variables.length; i++)
		{
			visible.replace(variables[i][0], variables[i][1]);
		}
		return visible;
	}

	public String createFemalePersonalVisible()
	{
		return "Liebe ${Vorname}";
	}

	public String createFemalePoliteVisible()
	{
		return "Sehr geehrte Frau ${Titel} ${Nachname}";
	}

	public String createMalePersonalVisible()
	{
		return "Lieber ${Vorname}";
	}

	public String createMalePoliteVisible()
	{
		return "Sehr geehrter Herr ${Titel} ${Nachname}";
	}

	public String createVisiblePersonLabel()
	{
		StringBuilder builder = new StringBuilder("${Organisation}");
		builder = builder.append("|${Anrede} ${Titel} ${Vorname} ${Nachname}");
		builder = builder.append("|${Funktion}");
		builder = builder.append("|${Zusatz}");
		builder = builder.append("|${Strasse}");
		builder = builder.append("|${Postfach}");
		builder = builder.append("|${Land}-${Plz} ${Ort}");
		return builder.toString();
	}

	public String formatBirthdate(final Person person)
	{
		Long value = person.getBirthdate();
		if (value == null)
			return "";
		if (value.longValue() > 0 && value.longValue() < 10000)
			return value.toString();
		else
		{
			DateFormat formatter = SimpleDateFormat.getDateInstance();
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTimeInMillis(value.longValue());
			return formatter.format(calendar.getTime());
		}
	}

	public String formatFirstnameLastname(final Person person)
	{
		StringBuilder sb = new StringBuilder("");
		if (!Person.stringValueOf(person.getFirstname().trim()).equals(""))
		{
			sb.append(person.getFirstname().trim());
		}
		if (!Person.stringValueOf(person.getLastname().trim()).equals(""))
		{
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(person.getLastname().trim());
		}
		return sb.toString();
	}

	public String formatId(final Person person)
	{
		return (person != null && person.getId() != null) ? this.nf.format(person.getId()) : ""; //$NON-NLS-1$
	}

	public String formatLastnameFirstname(final Person person)
	{
		StringBuilder sb = new StringBuilder("");
		if (!person.getLastname().equals(""))
		{
			sb.append(person.getLastname().trim());
		}
		if (!person.getFirstname().equals(""))
		{
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(person.getFirstname());
		}
		return sb.toString();
	}

	public String formatLastnameFirstnameInitial(final Person person)
	{
		StringBuilder sb = new StringBuilder("");
		if (!person.getLastname().isEmpty())
		{
			sb.append(person.getLastname().trim());
		}
		if (!person.getFirstname().isEmpty())
		{
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(person.getFirstnameInitial());
		}
		return sb.toString();
	}

	public String[] getPersonLabelStoredVariables()
	{
		return new String[] { "${organization}", "${salutation}", "${title}", "${firstname}", "${lastname}",
				"${function}", "${anotherline}", "${address}", "${pob}", "${country}", "${zip}", "${city}" };
	}

	public String[][] getPersonLabelVariablesToConvertToStored()
	{
		String[] visibleVariables = getPersonLabelVisibleVariables();
		String[] storedVariables = getPersonLabelStoredVariables();

		String[][] variables = new String[Math.min(storedVariables.length, visibleVariables.length)][2];
		for (int i = 0; i < variables.length; i++)
		{
			variables[i][0] = visibleVariables[i];
			variables[i][1] = storedVariables[i];
		}
		return variables;
	}

	public String[][] getPersonLabelVariablesToConvertToVisible()
	{
		String[] storedVariables = getPersonLabelStoredVariables();
		String[] visibleVariables = getPersonLabelVisibleVariables();

		String[][] variables = new String[Math.min(storedVariables.length, visibleVariables.length)][2];
		for (int i = 0; i < variables.length; i++)
		{
			variables[i][0] = storedVariables[i];
			variables[i][1] = visibleVariables[i];
		}
		return variables;
	}

	public String[] getPersonLabelVisibleVariables()
	{
		return new String[] { "${Organisation}", "${Anrede}", "${Titel}", "${Vorname}", "${Nachname}", "${Funktion}",
				"${Zusatz}", "${Strasse}", "${Postfach}", "${Land}", "${Plz}", "${Ort}" };
	}

	public String[] getPersonStoredVariables()
	{
		return new String[] { "${lastname}", "${title}", "${firstname}" };
	}

	public String[][] getPersonVariablesToConvertToStored()
	{
		String[] visibleVariables = getPersonVisibleVariables();
		String[] storedVariables = getPersonStoredVariables();

		String[][] variables = new String[Math.min(storedVariables.length, visibleVariables.length)][2];
		for (int i = 0; i < variables.length; i++)
		{
			variables[i][0] = visibleVariables[i];
			variables[i][1] = storedVariables[i];
		}
		return variables;
	}

	public String[][] getPersonVariablesToConvertToVisible()
	{
		String[] storedVariables = getPersonStoredVariables();
		String[] visibleVariables = getPersonVisibleVariables();

		String[][] variables = new String[Math.min(storedVariables.length, visibleVariables.length)][2];
		for (int i = 0; i < variables.length; i++)
		{
			variables[i][0] = storedVariables[i];
			variables[i][1] = visibleVariables[i];
		}
		return variables;
	}

	public String[] getPersonVisibleVariables()
	{
		return new String[] { "${Nachname}", "${Titel}", "${Vorname}" };
	}

	public String replaceSalutationVariables(final Person person, final String pattern)
	{
		String[] parts = pattern.split("[ ]");
		StringBuilder builder = new StringBuilder();
		for (String part : parts)
		{
			if (part.contains("${lastname}"))
			{
				part = person.getLastname();
			}
			if (part.contains("${firstname}"))
			{
				part = person.getFirstname();
			}
			if (part.contains("${title}"))
			{
				part = person.getTitle() == null ? "" : person.getTitle().getTitle();
			}
			builder = builder.append(part.isEmpty() ? "" : part + " ");
		}
		return builder.toString().trim();
	}

	public String replaceSalutationVariables(String pattern, Map<String, String> map)
	{
		String[] variables = this.getPersonLabelStoredVariables();
		for (String variable : variables)
		{
			String replacement = map.get(variable);
			if (replacement == null)
			{
				replacement = "";
			}
			pattern = pattern.replace(variable, replacement);
		}
		return pattern.trim();
	}

	public String replaceAddressLabelVariables(Map<String, String> map)
	{
		String format = PersonSettings.getInstance().getAddressLabelFormat();
		String[] lines = format == null ? new String[0] : format.split("[|]");
		String[] variables = this.getPersonLabelStoredVariables();
		for (String variable : variables)
		{
			String replacement = map.get(variable);
			if (replacement == null)
			{
				replacement = "";
			}
			for (int i = 0; i < lines.length; i++)
			{
				lines[i] = lines[i].replace(variable, replacement);
			}
		}
		StringBuilder builder = new StringBuilder();
		for (String line : lines)
		{
			if (!line.trim().isEmpty())
			{
				builder = builder.append(line.trim() + "\n");
			}
		}
		String value = builder.toString().trim();
		if (value.endsWith("\n"))
		{
			value = builder.substring(0, builder.toString().length() - 1).toString();
		}
		return value;
	}

	public String replacePersonLabelVariables(Map<String, String> map)
	{
		String format = PersonSettings.getInstance().getPersonLabelFormat();
		String[] lines = format == null ? new String[0] : format.split("[|]");
		String[] variables = this.getPersonLabelStoredVariables();
		for (String variable : variables)
		{
			String replacement = map.get(variable);
			if (replacement == null)
			{
				replacement = "";
			}
			for (int i = 0; i < lines.length; i++)
			{
				lines[i] = lines[i].replace(variable, replacement);
			}
		}
		StringBuilder builder = new StringBuilder();
		for (String line : lines)
		{
			if (!line.trim().isEmpty())
			{
				builder = builder.append(line.trim() + "\n");
			}
		}
		return builder.substring(0, builder.toString().length() - 1).toString();
	}

	public static PersonFormatter getInstance()
	{
		if (PersonFormatter.formatter == null)
			PersonFormatter.formatter = new PersonFormatter();

		return PersonFormatter.formatter;
	}

}
