package ch.eugster.events.person.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityListener;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.Teacher;
import ch.eugster.events.persistence.model.Visitor;
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

	private static final String ORGANISATION = "organisation";

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

	private EntityListener entityListener;

	private final boolean searchAddresses;

	private final Collection<ICriteriaChangedListener> listeners = new Vector<ICriteriaChangedListener>();

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
		if (this.dialogSettings.get("organisation.text") == null)
			this.dialogSettings.put("organisation.text", "");
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
		widget.setText(this.dialogSettings.get("organisation.text"));
		widget.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		widget.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				Text widget = (Text) LinkSearcher.this.widgets.get(ORGANISATION);
				LinkSearcher.this.dialogSettings.put("organisation.text", widget.getText());
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
				modifyText();
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
				Collection<FieldExtension> extensions = extensionQuery.selectSearchables(false);
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

		CriteriaChangedEvent event = new CriteriaChangedEvent(new AbstractEntity[0]);
		for (ICriteriaChangedListener listener : this.listeners)
			listener.criteriaChanged(event);
	}

	@Override
	public void dispose()
	{
		connectionServiceTracker.close();
		EntityMediator.removeListener(Address.class, entityListener);
		EntityMediator.removeListener(Person.class, entityListener);
		EntityMediator.removeListener(LinkPersonAddress.class, entityListener);
		EntityMediator.removeListener(Teacher.class, entityListener);
		EntityMediator.removeListener(Member.class, entityListener);
		EntityMediator.removeListener(Visitor.class, entityListener);
		EntityMediator.removeListener(Domain.class, entityListener);
	}

	public void fillAddress(final Address address)
	{
		String text = this.dialogSettings.get("organisation.text");
		address.setName(text);
		text = this.dialogSettings.get("address.text");
		address.setAddress(text);
	}

	public void fillPerson(final Person person)
	{
		String text = this.dialogSettings.get("lastname.text");
		person.setLastname(text);
		text = this.dialogSettings.get("firstname.text");
		person.setFirstname(text);
	}

	private Collection<Person> getPersons(final Collection<LinkPersonAddress> links)
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

	private String getText(final String key)
	{
		return ((Text) this.widgets.get(key)).getText();
	}

	private Text getTextWidget(final String key)
	{
		return (Text) this.widgets.get(key);
	}

	private boolean hasAddressCriteria(final Map<String, String> criteria)
	{
		Iterator<String> keys = criteria.keySet().iterator();
		while (keys.hasNext())
		{
			String key = keys.next();
			if (key.equals(ORGANISATION))
				return true;
			if (key.equals(ADDRESS))
				return true;
			if (key.equals(CITY))
				return true;
			if (key.equals(PHONE))
				return true;
			if (key.equals(EMAIL))
				return true;
		}
		return false;
	}

	public void initialize()
	{
		if (this.listen)
		{
			CriteriaChangedEvent event = new CriteriaChangedEvent(this.selectItems());
			for (ICriteriaChangedListener listener : this.listeners)
				listener.criteriaChanged(event);
		}

		entityListener = new EntityAdapter()
		{
			@Override
			public void postPersist(final AbstractEntity entity)
			{
				if (entity instanceof Address)
				{

					modifyText();
				}
				else if (entity instanceof Person)
				{
					modifyText();
				}
				else if (entity instanceof LinkPersonAddress)
				{
					modifyText();
				}
				else if (entity instanceof Teacher)
				{
					modifyText();
				}
				else if (entity instanceof Member)
				{
					modifyText();
				}
				else if (entity instanceof Visitor)
				{
					modifyText();
				}
				else if (entity instanceof Domain)
				{
					modifyText();
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof Address)
				{
					modifyText();
				}
				else if (entity instanceof Person)
				{
					modifyText();
				}
				else if (entity instanceof LinkPersonAddress)
				{
					modifyText();
				}
				else if (entity instanceof Teacher)
				{
					modifyText();
				}
				else if (entity instanceof Member)
				{
					modifyText();
				}
				else if (entity instanceof Visitor)
				{
					modifyText();
				}
				else if (entity instanceof Domain)
				{
					modifyText();
				}
			}
		};
		EntityMediator.addListener(Address.class, entityListener);
		EntityMediator.addListener(Person.class, entityListener);
		EntityMediator.addListener(LinkPersonAddress.class, entityListener);
		EntityMediator.addListener(Teacher.class, entityListener);
		EntityMediator.addListener(Member.class, entityListener);
		EntityMediator.addListener(Visitor.class, entityListener);
		EntityMediator.addListener(Domain.class, entityListener);
	}

	public void modifyText()
	{
		if (this.listen)
		{
			if (updateListJob == null || updateListJob.getState() == Job.NONE)
			{
				schedule();
			}
			else if (updateListJob.getState() == Job.SLEEPING)
			{
				System.out.println("SLEEPING: " + updateListJob.getState());
				updateListJob.cancel();
				System.out.println("CANCELLED: " + updateListJob.getState());
				schedule();
			}
			else
			{
				System.out.println("RUNNING: " + updateListJob.getState());
			}
		}
	}

	public void removeCriteriaChangedListener(final ICriteriaChangedListener listener)
	{
		if (this.listeners.contains(listener))
			this.listeners.remove(listener);
	}

	private void schedule()
	{
		System.out.println(updateListJob == null ? "FIRST" : "IF: " + updateListJob.getState());
		updateListJob = new UIJob("Starte Suchlauf...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				CriteriaChangedEvent event = new CriteriaChangedEvent(LinkSearcher.this.selectItems());
				for (ICriteriaChangedListener listener : LinkSearcher.this.listeners)
				{
					listener.criteriaChanged(event);
				}
				return Status.OK_STATUS;
			}
		};
		updateListJob.schedule(500L);
		System.out.println("SCHEDULED: " + updateListJob.getState());
	}

	private Collection<AbstractEntity> selectById(final String text)
	{
		Collection<AbstractEntity> entities = new ArrayList<AbstractEntity>();

		Person person = null;
		Address address = null;

		try
		{
			if (connectionService != null)
			{
				Long id = Long.valueOf(text);
				PersonQuery personQuery = (PersonQuery) connectionService.getQuery(Person.class);
				person = personQuery.find(Person.class, id);
				AddressQuery addressQuery = (AddressQuery) connectionService.getQuery(Address.class);
				address = addressQuery.find(Address.class, id);
			}
		}
		catch (NumberFormatException e)
		{
		}
		if (person != null)
		{
			entities.add(person);
		}
		if (address != null && address.getPersonLinks().size() == 0)
		{
			entities.add(address);
		}
		return entities;
	}

	private AbstractEntity[] selectItems()
	{
		Collection<AbstractEntity> entities = new ArrayList<AbstractEntity>();

		String value = this.getText(ID);
		if (!value.isEmpty())
		{
			entities = this.selectById(this.getTextWidget(ID).getText());
		}
		else
		{
			Map<String, String> criteria = new HashMap<String, String>();
			for (Entry<String, Widget> entry : this.widgets.entrySet())
			{
				if (!entry.getKey().equals("id"))
				{
					if (entry.getValue() instanceof Text)
					{
						Text text = (Text) entry.getValue();
						if (!text.getText().isEmpty())
						{
							criteria.put(entry.getKey(), text.getText());
						}
					}
				}
			}

			if (!criteria.isEmpty())
			{
				if (connectionService != null)
				{
					LinkPersonAddressQuery linkQuery = (LinkPersonAddressQuery) connectionService
							.getQuery(LinkPersonAddress.class);
					int maxRows = new InstanceScope().getNode(Activator.PLUGIN_ID).getInt(
							PreferenceInitializer.KEY_MAX_RECORDS, 0);
					entities.addAll(getPersons(linkQuery.selectByCriteria(criteria, extensions, maxRows)));

					if (this.searchAddresses)
					{
						if (hasAddressCriteria(criteria))
						{
							AddressQuery addressQuery = (AddressQuery) connectionService.getQuery(Address.class);
							entities.addAll(addressQuery.selectByCriteria(criteria, maxRows - entities.size()));
						}
					}
				}
			}
		}
		return entities.toArray(new AbstractEntity[0]);
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