package ch.eugster.events.importer.wizards;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.eclipse.swt.SWT;

import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.model.PersonSex;
import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.queries.AddressSalutationQuery;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.queries.GlobalSettingsQuery;
import ch.eugster.events.persistence.queries.PersonSexQuery;
import ch.eugster.events.persistence.queries.PersonTitleQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public enum MappingNames 
{
	EMPTY, EXTERNAL_ID, SALUTATION, TITLE, FIRSTNAME, LASTNAME, ADDRESS, ADDRESS_NUMBER, 
	ADDRESS_SUB_NUMBER, POB, ADDITIONAL_LINE, COUNTRY, ZIP, CITY, STATE, PHONE_FIX, PHONE_MOBILE, BIRTH_YEAR, 
	BIRTH_MONTH, BIRTH_DAY, BIRTH_DATE, SEX;
	
	private static String[] labels = new String[] { "", "Externe Id", "Anrede", "Titel", "Vorname", "Nachname", "Strasse", "Hausnummer", 
		"Hausteilnummer", "Postfach", "Zusatz", "Land", "Postleitzahl", "Ort", "Kanton", "Telefon fest", "Telefon mobil", "Geburtsjahr", 
		"Geburtsmonat", "Geburtstag", "Geburtsdatum", "Geschlecht" };

	private static Country defaultCountry = null;

	public static MappingNames[] getTargetViewerColumns()
	{
		return new MappingNames[] { EXTERNAL_ID, SEX, SALUTATION, TITLE, FIRSTNAME, LASTNAME, ADDRESS, POB, ADDITIONAL_LINE, COUNTRY, ZIP, CITY, STATE, PHONE_FIX, PHONE_MOBILE, BIRTH_YEAR, BIRTH_DATE };
	}
	
	public static MappingNames getMappingNameFromLabel(String label)
	{
		if (label == null)
		{
			return MappingNames.EMPTY;
		}
		for (MappingNames mappingName : MappingNames.values())
		{
			if (mappingName.label().equals(label))
			{
				return mappingName;
			}
		}
		return MappingNames.EMPTY;
	}
	
	private static int getIndexOf(MappingNames[] mappingNames, MappingNames searchMappingName)
	{
		for (int i = 0; i < mappingNames.length; i++)
		{
			if (mappingNames[i].equals(searchMappingName))
			{
				return i;
			}
		}
		return -1;
	}

	public String getValue(Row row, Map<MappingNames, Integer> targetMapping, int index, ConnectionService connectionService)
	{
		Cell cell = row.getCell(index);
		if (cell == null)
		{
			return "";
		}
		String value = getCellValue(cell);
		
		switch(this)
		{
		case SALUTATION:
		{
			if (value == null || value.isEmpty())
			{
				return MappingNames.SALUTATION.getValue(row, targetMapping, targetMapping.get(MappingNames.SEX.label()).intValue(), connectionService);
			}
			PersonSexQuery query = (PersonSexQuery) connectionService.getQuery(PersonSex.class);
			List<PersonSex> sexes = query.selectBySalutation(value);
			if (sexes.size() == 1)
			{
				return sexes.get(0).getSalutation();
			}
			else
			{
				AddressSalutationQuery salutationQuery = (AddressSalutationQuery) connectionService.getQuery(AddressSalutation.class);
				List<AddressSalutation> salutations = salutationQuery.selectBySalutation(value);
				return salutations.get(0).getSalutation();
			}
		}
		case SEX:
		{
			if (value == null || value.isEmpty())
			{
				return MappingNames.SEX.getValue(row, targetMapping, targetMapping.get(MappingNames.SALUTATION.label()), connectionService);
			}
			if (value.equals("f"))
			{
				value = "w";
			}
			PersonSexQuery query = (PersonSexQuery) connectionService.getQuery(PersonSex.class);
			List<PersonSex> sexes = query.selectBySymbol(value);
			return sexes.get(0).getSymbol();
		}
		case COUNTRY:
		{
			if (defaultCountry == null)
			{
				GlobalSettingsQuery gsQuery = (GlobalSettingsQuery) connectionService.getQuery(GlobalSettings.class);
				GlobalSettings settings = gsQuery.find(GlobalSettings.class, Long.valueOf(1L));
				defaultCountry = settings.getCountry();
			}
			if (value == null || value.isEmpty())
			{
				value = defaultCountry == null ? "" : defaultCountry.getIso3166alpha2();
			}
			CountryQuery query = (CountryQuery) connectionService.getQuery(Country.class);
			List<Country> countries = query.selectByIso3166alpha2Code(value);
			return countries.isEmpty() ? defaultCountry.getIso3166alpha2() : countries.get(0).getIso3166alpha2();
		}
		case TITLE:
		{
			if (value == null || value.isEmpty())
			{
				return "";
			}
			PersonTitleQuery query = (PersonTitleQuery) connectionService.getQuery(PersonTitle.class);
			List<PersonTitle> titles = query.selectByTitle(value);
			return titles.isEmpty() ? "" : titles.get(0).getTitle();
		}
		case ADDRESS:
		{
			StringBuilder returnValue = new StringBuilder(value);
			Integer idx = targetMapping.get(MappingNames.ADDRESS_NUMBER.label());
			if (idx != null)
			{
				returnValue = returnValue.append(" " + getCellValue(row.getCell(idx.intValue())));
				idx = targetMapping.get(MappingNames.ADDRESS_SUB_NUMBER.label());
				if (idx != null)
				{
					returnValue = returnValue.append(getCellValue(row.getCell(idx.intValue())));
				}
			}
			else
			{
				idx = targetMapping.get(MappingNames.ADDRESS_SUB_NUMBER.label());
				if (idx != null)
				{
					returnValue = returnValue.append(" " + getCellValue(row.getCell(idx.intValue())));
				}
			}
			return returnValue.toString().trim();
		}
		case BIRTH_YEAR:
		{
			
		}
		default:
		{
			return value;
		}
		}
	}
	
	public static String getCellValue(Cell cell)
	{
		int type = cell.getCellType();
		if (type == Cell.CELL_TYPE_FORMULA)
		{
			type = cell.getCachedFormulaResultType();
		}
		switch (type)
		{
		case Cell.CELL_TYPE_BLANK:
		{
			return "";
		}
		case Cell.CELL_TYPE_BOOLEAN:
		{
			return Boolean.valueOf(cell.getBooleanCellValue()).toString();
		}
		case Cell.CELL_TYPE_ERROR:
		{
			return "";
		}
		case Cell.CELL_TYPE_NUMERIC:
		{
			Double value = Double.valueOf(cell.getNumericCellValue());
			if (value.intValue() == value.doubleValue())
			{
				return Integer.valueOf(value.intValue()).toString();
			}
			return value.toString();
		}
		case Cell.CELL_TYPE_STRING:
		{
			return cell.getStringCellValue();
		}
		default:
		{
			return "";
		}
		}
	}
	public String label()
	{
		return labels[this.ordinal()];
	}
	
	public int columnStyle()
	{
		return SWT.LEFT;
	}
	
	public String check(Row row, MappingNames[] targetNames, int index, ConnectionService connectionService)
	{
		Cell cell = row.getCell(index);
		if (cell == null)
		{
			return "";
		}
		String value = getCellValue(cell);
		
		switch(this)
		{
		case SALUTATION:
		{
			if (value == null || value.isEmpty())
			{
				int sexIndex = getIndexOf(targetNames, MappingNames.SEX);
				if (sexIndex == -1)
				{
					return "Der Wert für " + this.label() + " ist nicht gültig. Ändern Sie den Wert in der Importdatei auf einen gültigen Wert.";
				}
				else
				{
					return MappingNames.SEX.check(row, targetNames, sexIndex, connectionService);
				}
			}
			PersonSexQuery query = (PersonSexQuery) connectionService.getQuery(PersonSex.class);
			List<PersonSex> sexes = query.selectBySalutation(value);
			if (sexes.size() == 1)
			{
				return "";
			}
			else
			{
				AddressSalutationQuery salutationQuery = (AddressSalutationQuery) connectionService.getQuery(AddressSalutation.class);
				List<AddressSalutation> salutations = salutationQuery.selectBySalutation(value);
				if (salutations.size() == 1)
				{
					return "";
				}
				else
				{
					return "Der Wert '" + value + "' für " + this.label() + " ist nicht gültig. Ändern Sie den Wert in der Importdatei auf einen gültigen Wert.";
				}
			}
		}
		case SEX:
		{
			if (value == null || value.isEmpty())
			{
				int salutationIndex = getIndexOf(targetNames, MappingNames.SALUTATION);
				if (salutationIndex == -1)
				{
					return "Der Wert '" + value + "' für " + this.label() + " ist nicht gültig. Ändern Sie den Wert in der Importdatei auf einen gültigen Wert.";
				}
				else
				{
					return MappingNames.SALUTATION.check(row, targetNames, salutationIndex, connectionService);
				}
			}
			if (value.equals("f"))
			{
				value = "w";
			}
			PersonSexQuery query = (PersonSexQuery) connectionService.getQuery(PersonSex.class);
			List<PersonSex> sexes = query.selectBySymbol(value);
			if (sexes.size() == 1)
			{
				return "";
			}
			else
			{
				return "Der Wert '" + value + "' für " + this.label() + " ist nicht gültig. Ändern Sie den Wert in der Importdatei auf einen gültigen Wert.";
			}
		}
		case COUNTRY:
		{
			if (defaultCountry == null)
			{
				GlobalSettingsQuery gsQuery = (GlobalSettingsQuery) connectionService.getQuery(GlobalSettings.class);
				GlobalSettings settings = gsQuery.find(GlobalSettings.class, Long.valueOf(1L));
				defaultCountry = settings.getCountry();
			}
			if (value == null || value.isEmpty())
			{
				value = defaultCountry == null ? "" : defaultCountry.getIso3166alpha2();
			}
			CountryQuery query = (CountryQuery) connectionService.getQuery(Country.class);
			List<Country> countries = query.selectByIso3166alpha2Code(value);
			return countries.isEmpty() 
							? ("Der Wert '" + value + "' für " + this.label() + " ist nicht gültig. Ändern Sie den Wert in der Importdatei auf einen gültigen Wert.")
							: "";
		}
		case TITLE:
		{
			if (value == null || value.isEmpty())
			{
				return "";
			}
			PersonTitleQuery query = (PersonTitleQuery) connectionService.getQuery(PersonTitle.class);
			List<PersonTitle> titles = query.selectByTitle(value);
			if (titles.size() == 1)
			{
				return "";
			}
			else
			{
				return "Der Wert '" + value + "' für " + this.label() + " ist nicht gültig. Ändern Sie den Wert in der Importdatei auf einen gültigen Wert.";
			}
		}
		default:
		{
			return "";
		}
		}
	}
		
}
