package ch.eugster.events.person.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.FieldExtensionQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.preferences.PreferenceInitializer;

public class LinkSearcher extends Composite
{
	private static final String LASTNAME = "lastname";

	private static final String FIRSTNAME = "firstname";

	private static final String ORGANISATION = "organization";

	private static final String ADDRESS = "address";

	private static final String CITY = "city";

	private static final String PHONE = "phone";

	private static final String EMAIL = "email";

	private static final String ID = "id";

	private UIJob updateListJob = null;

	private IDialogSettings dialogSettings;

	private final Map<String, Widget> widgets = new HashMap<String, Widget>();

	private final Map<String, FieldExtension> extensions = new HashMap<String, FieldExtension>();

	private boolean listen;

	private final boolean searchAddresses;

	private final List<ICriteriaChangedListener> listeners = new Vector<ICriteriaChangedListener>();

	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	public LinkSearcher(final Composite parent, final boolean searchAddresses, final int style)
	{
		super(parent, style);

		this.searchAddresses = searchAddresses;

		this.dialogSettings = Activator.getDefault().getDialogSettings().getSection("person.link.searcher");

		if (this.dialogSettings == null)
			this.dialogSettings = Activator.getDefault().getDialogSettings().addNewSection("person.link.searcher");
		if (this.dialogSettings.get("lastname.text") == null)
			this.dialogSettings.put("lastname.text", "");
		if (this.dialogSettings.get("firstname.text") == null)
			this.dialogSettings.put("firstname.text", "");
		if (this.dialogSettings.get("organization.text") == null)
			this.dialogSettings.put("organization.text", "");
		if (this.dialogSettings.get("address.text") == null)
			this.dialogSettings.put("address.text", "");
		if (this.dialogSettings.get("city.text") == null)
			this.dialogSettings.put("city.text", "");
		if (this.dialogSettings.get("phone.text") == null)
			this.dialogSettings.put("phone.text", "");
		if (this.dialogSettings.get("email.text") == null)
			this.dialogSettings.put("email.text", "");
		if (this.dialogSettings.get("id.text") == null)
			this.dialogSettings.put("id.text", "");

		this.setLayout(new GridLayout(2, false));

		Label label = new Label(this, SWT.NONE);
		label.setText("Nachname");
		label.setLayoutData(new GridData());

		Text widget = new Text(this, SWT.BORDER | SWT.SINGLE);
		widget.setText(this.dialogSettings.get("lastname.text"));
		widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		widget.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				Text widget = (Text) LinkSearcher.this.widgets.get(LASTNAME);
				LinkSearcher.this.dialogSettings.put("lastname.text", widget.getText());
				LinkSearcher.this.modifyText();
			}
		});
		widget.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				((Text) e.widget).selectAll();
			}
		});
		this.widgets.put(LASTNAME, widget);

		label = new Label(this, SWT.NONE);
		label.setText("Vorname");
		label.setLayoutData(new GridData());

		widget = new Text(this, SWT.BORDER | SWT.SINGLE);
		widget.setText(this.dialogSettings.get("firstname.text"));
		widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		widget.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				Text widget = (Text) LinkSearcher.this.widgets.get(FIRSTNAME);
				LinkSearcher.this.dialogSettings.put("firstname.text", widget.getText());
				LinkSearcher.this.modifyText();
			}
		});
		widget.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				((Text) e.widget).selectAll();
			}
		});
		this.widgets.put(FIRSTNAME, widget);

		label = new Label(this, SWT.NONE);
		label.setText("Organisation");
		label.setLayoutData(new GridData());

		widget = new Text(this, SWT.BORDER | SWT.SINGLE);
		widget.setText(this.dialogSettings.get("organization.text"));
		widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		widget.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				Text widget = (Text) LinkSearcher.this.widgets.get(ORGANISATION);
				LinkSearcher.this.dialogSettings.put("organization.text", widget.getText());
				LinkSearcher.this.modifyText();
			}
		});
		widget.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				((Text) e.widget).selectAll();
			}
		});
		this.widgets.put(ORGANISATION, widget);

		label = new Label(this, SWT.NONE);
		label.setText("Strasse/Postfach");
		label.setLayoutData(new GridData());

		widget = new Text(this, SWT.BORDER | SWT.SINGLE);
		widget.setText(this.dialogSettings.get("address.text"));
		widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		widget.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				Text widget = (Text) LinkSearcher.this.widgets.get(ADDRESS);
				LinkSearcher.this.dialogSettings.put("address.text", widget.getText());
				LinkSearcher.this.modifyText();
			}
		});
		widget.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				((Text) e.widget).selectAll();
			}
		});
		this.widgets.put(ADDRESS, widget);

		label = new Label(this, SWT.NONE);
		label.setText("Postleitzahl/Ort");
		label.setLayoutData(new GridData());

		widget = new Text(this, SWT.BORDER | SWT.SINGLE);
		widget.setText(this.dialogSettings.get("city.text"));
		widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		widget.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				Text widget = (Text) LinkSearcher.this.widgets.get(CITY);
				LinkSearcher.this.dialogSettings.put("city.text", widget.getText());
				LinkSearcher.this.modifyText();
			}
		});
		widget.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				((Text) e.widget).selectAll();
			}
		});
		this.widgets.put(CITY, widget);

		label = new Label(this, SWT.NONE);
		label.setText("Telefon/Fax");
		label.setLayoutData(new GridData());

		widget = new Text(this, SWT.BORDER | SWT.SINGLE);
		widget.setText(this.dialogSettings.get("phone.text"));
		widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		widget.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				Text widget = (Text) LinkSearcher.this.widgets.get(PHONE);
				LinkSearcher.this.dialogSettings.put("phone.text", widget.getText());
				LinkSearcher.this.modifyText();
			}
		});
		widget.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				((Text) e.widget).selectAll();
			}
		});
		this.widgets.put(PHONE, widget);

		label = new Label(this, SWT.NONE);
		label.setText("Email");
		label.setLayoutData(new GridData());

		widget = new Text(this, SWT.BORDER | SWT.SINGLE);
		widget.setText(this.dialogSettings.get("email.text"));
		widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		widget.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				Text widget = (Text) LinkSearcher.this.widgets.get(EMAIL);
				LinkSearcher.this.dialogSettings.put("email.text", widget.getText());
				LinkSearcher.this.modifyText();
			}
		});
		widget.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				((Text) e.widget).selectAll();
			}
		});
		this.widgets.put(EMAIL, widget);

		addExtendedWidgets();

		label = new Label(this, SWT.NONE);
		label.setText("Id");
		label.setLayoutData(new GridData());

		widget = new Text(this, SWT.BORDER | SWT.SINGLE);
		widget.setText(this.dialogSettings.get("id.text"));
		widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		widget.setData("dialog.setting", "id.text");
		widget.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				Text widget = (Text) LinkSearcher.this.widgets.get(ID);
				LinkSearcher.this.dialogSettings.put("id.text", widget.getText());
				LinkSearcher.this.modifyText();
			}
		});
		widget.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				((Text) e.widget).selectAll();
			}
		});
		this.widgets.put(ID, widget);

		this.startListening();

		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(final ServiceReference reference)
			{
				connectionService = (ConnectionService) super.addingService(reference);
				return connectionService;
			}
		};
		connectionServiceTracker.open();
	}

	public void addCriteriaChangedListener(final ICriteriaChangedListener listener)
	{
		if (!this.listeners.contains(listener))
			this.listeners.add(listener);
	}

	private void addExtendedWidgets()
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				FieldExtensionQuery extensionQuery = (FieldExtensionQuery) service.getQuery(FieldExtension.class);
				List<FieldExtension> extensions = extensionQuery.selectSearchables(false);
				for (final FieldExtension extension : extensions)
				{
					Label label = new Label(this, SWT.NONE);
					label.setText(extension.getLabel());
					label.setLayoutData(new GridData());

					Text widget = new Text(this, SWT.BORDER | SWT.SINGLE);
					String value = dialogSettings.get(extension.getId().toString() + ".text");
					if (value != null)
					{
						widget.setText(value);
					}
					widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
					widget.setData("dialog.setting", extension.getId().toString() + ".text");
					widget.addModifyListener(new ModifyListener()
					{
						@Override
						public void modifyText(final ModifyEvent event)
						{
							Text widget = (Text) LinkSearcher.this.widgets.get(ID);
							LinkSearcher.this.dialogSettings.put(extension.getId().toString() + ".text",
									widget.getText());
							LinkSearcher.this.modifyText();
						}
					});
					widget.addFocusListener(new FocusAdapter()
					{
						@Override
						public void focusGained(final FocusEvent e)
						{
							((Text) e.widget).selectAll();
						}
					});
					this.widgets.put(extension.getId().toString(), widget);
					this.extensions.put(extension.getId().toString(), extension);
				}
			}
		}
		catch (NumberFormatException e)
		{
		}
		finally
		{
			tracker.close();
		}
	}

	public void clearSearchFields()
	{
		this.stopListening();
		for (Widget widget : this.widgets.values())
		{
			if (widget instanceof Text)
			{
				Text text = (Text) widget;
				if (!text.getText().isEmpty())
					text.setText("");
			}
		}
		this.startListening();

		for (ICriteriaChangedListener listener : this.listeners)
		{
			listener.criteriaChanged(new AbstractEntity[0]);
		}
	}

	private Map<String, String> createCriteria()
	{
		final Map<String, String> criteria = new HashMap<String, String>();
		Text text = (Text) widgets.get(ID);
		String id = text.getText().trim();
		if (!id.isEmpty())
		{
			criteria.put(ID, id);
		}
		else
		{
			for (Entry<String, Widget> entry : widgets.entrySet())
			{
				text = (Text) entry.getValue();
				String value = text.getText().trim();
				if (entry.getKey().equals(ID))
				{
				}
				else if (entry.getKey().equals(LASTNAME))
				{
				}
				else if (entry.getKey().equals(PHONE))
				{
				}
				else if (entry.getKey().equals(CITY))
				{
					if (!value.isEmpty())
					{
						if (value.contains("."))
						{
							value = value.replace(".", ".%");
						}
						criteria.put(entry.getKey(), value);
					}
				}
				else
				{
					if (!value.isEmpty())
					{
						criteria.put(entry.getKey(), value);
					}
				}
			}
			if (criteria.isEmpty())
			{
				text = (Text) widgets.get(LASTNAME);
				String value = text.getText().trim();
				if (value.length() > 3)
				{
					criteria.put(LASTNAME, value);
				}
				text = (Text) widgets.get(PHONE);
				value = text.getText().trim();
				if (value.length() > 6)
				{
					criteria.put(PHONE, value);
				}
			}
			else
			{
				text = (Text) widgets.get(LASTNAME);
				String value = text.getText().trim();
				if (!value.isEmpty())
				{
					criteria.put(LASTNAME, value);
				}
				text = (Text) widgets.get(PHONE);
				value = text.getText().trim();
				if (!value.isEmpty())
				{
					criteria.put(PHONE, value);
				}
			}
		}
		return criteria;
	}

	@Override
	public void dispose()
	{
		int length = 0;
		length += this.dialogSettings.get("lastname.text").length();
		length += this.dialogSettings.get("firstname.text").length();
		length += this.dialogSettings.get("organization.text").length();
		length += this.dialogSettings.get("address.text").length();
		length += this.dialogSettings.get("city.text").length();
		length += this.dialogSettings.get("phone.text").length();
		length += this.dialogSettings.get("email.text").length();
		if (length < 3)
		{
			this.dialogSettings.put("lastname.text", "");
			this.dialogSettings.put("firstname.text", "");
			this.dialogSettings.put("organization.text", "");
			this.dialogSettings.put("address.text", "");
			this.dialogSettings.put("city.text", "");
			this.dialogSettings.put("phone.text", "");
			this.dialogSettings.put("email.text", "");
		}

		connectionServiceTracker.close();
	}

	public Map<String, String> getInitialValues()
	{
		Map<String, String> initialValues = new HashMap<String, String>();
		String value = dialogSettings.get("lastname.text");
		value = toUpperCase(value);
		if (value != null)
		{
			initialValues.put("lastname", value);
		}
		value = dialogSettings.get("firstname.text");
		value = toUpperCase(value);
		if (value != null)
		{
			initialValues.put("firstname", value);
		}
		value = dialogSettings.get("organization.text");
		value = toUpperCase(value);
		if (value != null)
		{
			initialValues.put("organization", value);
		}
		value = dialogSettings.get("address.text");
		value = toUpperCase(value);
		if (value != null)
		{
			try
			{
				value = Long.valueOf(value).toString();
				initialValues.put("pob", value);
			}
			catch (NumberFormatException e)
			{
				initialValues.put("address", value);
			}
		}
		value = dialogSettings.get("city.text");
		value = toUpperCase(value);
		if (value != null)
		{
			try
			{
				value = Integer.valueOf(value).toString();
				initialValues.put("zip", value);
			}
			catch (NumberFormatException e)
			{
				initialValues.put("city", value);
			}
		}
		value = dialogSettings.get("email.text");
		value = toUpperCase(value);
		if (value != null)
		{
			initialValues.put("email", value);
		}
		return initialValues;
	}

	private String toUpperCase(String value)
	{
		StringBuilder result = new StringBuilder();
		if (value == null || value.trim().isEmpty())
		{
			return null;
		}
		String[] vals = value.split(" ");
		for (String val : vals)
		{
			char[] chars = val.toCharArray();
			if (chars.length > 0)
			{
				chars[0] = Character.toUpperCase(chars[0]);
				result = result.append(new String(chars) + " ");
			}
		}
		return result.toString().trim();
	}

	private Collection<Person> getPersons(final List<LinkPersonAddress> links)
	{
		Map<Long, Person> persons = new HashMap<Long, Person>();
		for (LinkPersonAddress link : links)
		{
			if (persons.get(link.getPerson().getId()) == null)
			{
				persons.put(link.getPerson().getId(), link.getPerson());
			}
		}
		return persons.values();
	}

//	private boolean hasAddressCriteria(final Map<String, String> criteria)
//	{
//		Iterator<String> keys = criteria.keySet().iterator();
//		while (keys.hasNext())
//		{
//			String key = keys.next();
//			if (key.equals(ORGANISATION))
//			{
//				return true;
//			}
//			if (key.equals(ADDRESS))
//			{
//				return true;
//			}
//			if (key.equals(CITY))
//			{
//				return true;
//			}
//			if (key.equals(PHONE))
//			{
//				return true;
//			}
//			if (key.equals(EMAIL))
//			{
//				return true;
//			}
//		}
//		return false;
//	}

	private boolean hasPersonCriteria(final Map<String, String> criteria)
	{
		Iterator<String> keys = criteria.keySet().iterator();
		while (keys.hasNext())
		{
			String key = keys.next();
			if (key.equals(LASTNAME))
			{
				return true;
			}
			if (key.equals(FIRSTNAME))
			{
				return true;
			}
		}
		return false;
	}

	public void initialize()
	{
		if (this.listen)
		{
			Map<String, String> criteria = createCriteria();
			AbstractEntity[] entities = selectItems(criteria);
			if (entities.length > 0)
			{
				for (ICriteriaChangedListener listener : this.listeners)
				{
					listener.criteriaChanged(entities);
				}
			}
		}

	}

	public void modifyText()
	{
		if (this.listen)
		{
			schedule();
		}
	}

	public void removeCriteriaChangedListener(final ICriteriaChangedListener listener)
	{
		if (this.listeners.contains(listener))
			this.listeners.remove(listener);
	}

	private void schedule()
	{
		if (updateListJob == null)
		{
			updateListJob = new UIJob("Starte Suchlauf...")
			{
				@Override
				public IStatus runInUIThread(final IProgressMonitor monitor)
				{
					AbstractEntity[] entities = new AbstractEntity[0];
					final Map<String, String> criteria = createCriteria();
					if (!criteria.isEmpty())
					{
						entities = LinkSearcher.this.selectItems(criteria);
					}
					for (ICriteriaChangedListener listener : LinkSearcher.this.listeners)
					{
						listener.criteriaChanged(entities);
					}
					return Status.OK_STATUS;
				}
			};
		}
		else
		{
			updateListJob.cancel();
		}
		updateListJob.schedule(500L);
	}

	private AbstractEntity[] selectById(final String text)
	{
		List<AbstractEntity> entities = new ArrayList<AbstractEntity>();

		Person person = null;
		Address address = null;
		LinkPersonAddress link = null;

		try
		{
			if (connectionService != null)
			{
				Long id = Long.valueOf(text);
				PersonQuery personQuery = (PersonQuery) connectionService.getQuery(Person.class);
				person = personQuery.find(Person.class, id);
				AddressQuery addressQuery = (AddressQuery) connectionService.getQuery(Address.class);
				address = addressQuery.find(Address.class, id);
//				LinkPersonAddressQuery linkQuery = (LinkPersonAddressQuery) connectionService.getQuery(LinkPersonAddress.class);
//				link = linkQuery.find(LinkPersonAddress.class, id);
			}
		}
		catch (NumberFormatException e)
		{
		}
		if (person != null)
		{
			if (!person.isDeleted())
			{
				entities.add(person);
			}
		}
		if (address != null)
		{
			if (address.getValidLinks().size() == 0)
			{
				entities.add(address);
			}
			else
			{
				for (LinkPersonAddress child : address.getPersonLinks())
				{
					addIfNotAlreadyAdded(entities, child.getPerson());
				}
			}
		}
//		if (link != null)
//		{
//			addIfNotAlreadyAdded(entities, link.getPerson());
//		}
		return entities.toArray(new AbstractEntity[0]);
	}
	
	private void addIfNotAlreadyAdded(List<AbstractEntity> entities, Person person)
	{
		for (AbstractEntity entity : entities)
		{
			if (entity instanceof Person)
			{
				if (entity.getId().equals(person.getId()))
				{
					return;
				}
			}
		}
		entities.add(person);
	}

	private AbstractEntity[] selectItems(final Map<String, String> criteria)
	{
		AbstractEntity[] entities = new AbstractEntity[0];
		if (criteria != null && !criteria.isEmpty())
		{
			String id = criteria.get(ID);
			if (id != null && !id.trim().isEmpty())
			{
				entities = this.selectById(id.trim());
			}
			else
			{
				if (connectionService != null)
				{
					List<AbstractEntity> selected = new ArrayList<AbstractEntity>();
					LinkPersonAddressQuery linkQuery = (LinkPersonAddressQuery) connectionService
							.getQuery(LinkPersonAddress.class);
					int maxRows = new InstanceScope().getNode(Activator.PLUGIN_ID).getInt(
							PreferenceInitializer.KEY_MAX_RECORDS, 0);
					selected.addAll(getPersons(linkQuery.selectByCriteria(criteria, extensions, maxRows)));

					if (this.searchAddresses)
					{
						if (!hasPersonCriteria(criteria))
						{
							AddressQuery addressQuery = (AddressQuery) connectionService.getQuery(Address.class);
							List<Address> addresses = addressQuery.selectByCriteria(criteria,
									maxRows - selected.size());
							for (Address address : addresses)
							{
								if (address.getValidLinks().isEmpty())
								{
									selected.add(address);
								}
							}
						}
					}
					entities = selected.toArray(new AbstractEntity[0]);
				}
			}
		}
		return entities;
	}

	@Override
	public boolean setFocus()
	{
		return ((Text) this.widgets.get(LASTNAME)).setFocus();
	}

	public void startListening()
	{
		this.listen = true;
	}

	public void stopListening()
	{
		this.listen = false;
	}
}
