package ch.eugster.events.documents.maps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.documents.Activator;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.FieldExtensionTarget;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonExtendedField;
import ch.eugster.events.persistence.queries.FieldExtensionQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class PersonMap extends AbstractDataMap<Person>
{
	private static final DateFormat dateFormatter = SimpleDateFormat.getDateInstance();

	protected PersonMap()
	{
		super();
	}
	
	public PersonMap(final Person person)
	{
		for (Key key : Key.values())
		{
			this.setProperty(key.getKey(), key.getValue(person));
		}
		List<PersonExtendedField> extendedFields = person.getExtendedFields();
		for (PersonExtendedField extendedField : extendedFields)
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
				List<FieldExtension> fieldExtensions = query.selectByTarget(FieldExtensionTarget.PERSON, false);
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

	public enum Key implements DataMapKey
	{
		ID, SALUTATION, TITLE, FIRSTNAME, LASTNAME, PROFESSION, PHONE, EMAIL, WEBSITE, SEX, BIRTHDATE, FORM, POLITE, NOTE;

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
				case ID:
				{
					return "Identifikationsnummer der Person";
				}
				case SALUTATION:
				{
					return "Anrede";
				}
				case TITLE:
				{
					return "Titel";
				}
				case FIRSTNAME:
				{
					return "Vorname";
				}
				case LASTNAME:
				{
					return "Nachname";
				}
				case PROFESSION:
				{
					return "Beruf";
				}
				case PHONE:
				{
					return "Mobile";
				}
				case EMAIL:
				{
					return "Email (Person)";
				}
				case WEBSITE:
				{
					return "Website (Person)";
				}
				case SEX:
				{
					return "Geschlecht";
				}
				case BIRTHDATE:
				{
					return "Geburtsdatum oder Geburtsjahr";
				}
				case FORM:
				{
					return "Anredeform (persönlich oder höflich)";
				}
				case POLITE:
				{
					return "Briefanrede";
				}
				case NOTE:
				{
					return "Bemerkungen";
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
				case ID:
				{
					return "person_id";
				}
				case SALUTATION:
				{
					return "salutation";
				}
				case TITLE:
				{
					return "person_title";
				}
				case FIRSTNAME:
				{
					return "person_firstname";
				}
				case LASTNAME:
				{
					return "person_lastname";
				}
				case PROFESSION:
				{
					return "person_profession";
				}
				case PHONE:
				{
					return "person_phone";
				}
				case EMAIL:
				{
					return "person_email";
				}
				case WEBSITE:
				{
					return "person_website";
				}
				case SEX:
				{
					return "person_sex";
				}
				case BIRTHDATE:
				{
					return "person_birthdate";
				}
				case FORM:
				{
					return "person_form";
				}
				case POLITE:
				{
					return "polite";
				}
				case NOTE:
				{
					return "person_note";
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
				case ID:
				{
					return "Id";
				}
				case SALUTATION:
				{
					return "Anrede";
				}
				case TITLE:
				{
					return "Titel";
				}
				case FIRSTNAME:
				{
					return "Vorname";
				}
				case LASTNAME:
				{
					return "Nachname";
				}
				case PROFESSION:
				{
					return "Beruf";
				}
				case PHONE:
				{
					return "Mobile";
				}
				case EMAIL:
				{
					return "Email";
				}
				case WEBSITE:
				{
					return "Website";
				}
				case SEX:
				{
					return "Geschlecht";
				}
				case BIRTHDATE:
				{
					return "Geburtsdatum";
				}
				case FORM:
				{
					return "Anredeform";
				}
				case POLITE:
				{
					return "Briefanrede";
				}
				case NOTE:
				{
					return "Bemerkungen";
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}

		public String getValue(final Person person)
		{
			switch (this)
			{
				case ID:
				{
					return person.getId().toString();
				}
				case SALUTATION:
				{
					return person.getSex() == null ? "Fehler!" : person.getSex().getSalutation();
				}
				case TITLE:
				{
					return person.getTitle() == null ? "" : person.getTitle().getTitle();
				}
				case FIRSTNAME:
				{
					return person.getFirstname();
				}
				case LASTNAME:
				{
					return person.getLastname();
				}
				case PROFESSION:
				{
					return person.getProfession();
				}
				case PHONE:
				{
					return PersonFormatter.getInstance().formatPhoneWithOptionalPrefix(person.getCountry(),
							person.getPhone());
				}
				case EMAIL:
				{
					return person.getEmail();
				}
				case WEBSITE:
				{
					return person.getWebsite();
				}
				case SEX:
				{
					return person.getSex() == null ? "Fehler!" : person.getSex().getSymbol();
				}
				case BIRTHDATE:
				{
					if (person.getBirthday() == null)
					{
						if (person.getBirthyear() == null)
						{
							return "";
						}
						else
						{
							return person.getBirthyear().toString();
						}
					}
					else
					{
						return dateFormatter.format(person.getBirthday());
					}
				}
				case FORM:
				{
					return person.getForm().toString();
				}
				case POLITE:
				{
					return person.getSex() == null ? "Fehler!" : PersonFormatter.getInstance()
							.replaceSalutationVariables(person, person.getSex().getForm(person.getForm()));
				}
				case NOTE:
				{
					return person.getNotes();
				}
				default:
				{
					throw new RuntimeException("Invalid key");
				}
			}
		}
	}

	@Override
	protected DataMapKey[] getKeys() 
	{
		return Key.values();
	}
}
