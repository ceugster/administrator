package ch.eugster.events.utilities.console.database.manipulator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.FieldExtensionTarget;
import ch.eugster.events.persistence.model.FieldExtensionType;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonExtendedField;
import ch.eugster.events.persistence.queries.FieldExtensionQuery;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.utilities.console.Activator;

public class LawFolderUpdater implements CommandProvider
{
	public void _go(final CommandInterpreter commandInterpreter)
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getContext(), ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service == null)
			{
				System.out.println("Database connection not established.");
				return;
			}

			FieldExtensionQuery extensionQuery = (FieldExtensionQuery) service.getQuery(FieldExtension.class);
			Collection<FieldExtension> extensions = extensionQuery.selectByTarget(FieldExtensionTarget.PERSON, false);
			Map<String, FieldExtension> existingExtensions = new HashMap<String, FieldExtension>();
			String[] keys = new String[] { "Gesetzesordner", "Funktion", "Ressort" };

			insertExisting(keys, extensions, existingExtensions, extensionQuery);
			addAbsentee(keys, existingExtensions, extensionQuery);

			updateLawFolders(service, existingExtensions.get(keys[0]));
		}
		finally
		{
			tracker.close();
		}
	}

	private void addAbsentee(final String[] keys, final Map<String, FieldExtension> extensions,
			final FieldExtensionQuery query)
	{
		for (int i = 0; i < keys.length; i++)
		{
			FieldExtension extension = extensions.get(keys[i]);
			if (extension == null)
			{
				if (keys[i].equals(keys[0]))
				{
					insertFieldExtension(keys[i], true, query);
				}
				else if (keys[i].equals(keys[1]))
				{
					insertFieldExtension(keys[i], false, query);
				}
				else if (keys[i].equals(keys[2]))
				{
					insertFieldExtension(keys[i], false, query);
				}
			}
		}
	}

	private String checkNumber(final String note)
	{
		try
		{
			Integer.valueOf(note);
			return note;
		}
		catch (NumberFormatException nfe)
		{
			return null;
		}
	}

	@Override
	public String getHelp()
	{
		StringBuilder help = new StringBuilder();
		return help.append("\tgo - inserts a new field extension to person named 'gesetzesordner'\n").toString();
	}

	private void insertExisting(final String[] keys, final Collection<FieldExtension> collection,
			final Map<String, FieldExtension> map, final FieldExtensionQuery query)
	{
		for (FieldExtension extension : collection)
		{
			if (extension.getLabel().equals(keys[0]))
			{
				map.put(keys[0], extension);
			}
			else if (extension.getLabel().equals(keys[1]))
			{
				map.put(keys[1], extension);
			}
			else if (extension.getLabel().equals(keys[2]))
			{
				map.put(keys[2], extension);
			}
		}
	}

	private void insertFieldExtension(final String key, final boolean searchable, final FieldExtensionQuery query)
	{
		FieldExtension extension = FieldExtension.newInstance();
		extension.setLabel(key);
		extension.setSearchable(searchable);
		extension.setTarget(FieldExtensionTarget.PERSON);
		extension.setType(FieldExtensionType.TEXT);
		extension = query.merge(extension);
		System.out.println("- merging field extension " + key);
	}

	private void updateLawFolders(final ConnectionService service, final FieldExtension extension)
	{
		PersonQuery personQuery = (PersonQuery) service.getQuery(Person.class);
		Collection<Person> persons = personQuery.selectAll(false);
		for (Person person : persons)
		{
			String fullNotes = person.getNotes();
			String[] noteLines = fullNotes.split("\n");
			for (String noteLine : noteLines)
			{
				String[] notes = noteLine.split(" ");
				for (String note : notes)
				{
					if (note.length() == 3)
					{
						String value = checkNumber(note);
						if (value != null && value.equals(note))
						{
							boolean found = false;
							for (PersonExtendedField field : person.getExtendedFields())
							{
								if (field.getValue().equals(note))
								{
									found = true;
								}
							}
							if (!found)
							{
								PersonExtendedField field = PersonExtendedField.newInstance(person, extension);
								field.setValue(note);
								person.addExtendedFields(field);
								person = personQuery.merge(person);
								System.out.println("- merging extended person field " + note);
							}
						}
					}
				}
			}
		}
	}
}
