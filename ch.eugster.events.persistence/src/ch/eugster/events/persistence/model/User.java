package ch.eugster.events.persistence.model;

import java.util.List;
import java.util.Vector;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;

@Entity
@Table(name = "events_user")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "user_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "user_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "user_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "user_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "user_version")) })
public class User extends AbstractEntity
{
	@Transient
	private static User current;

	/**
	 * References
	 */
	@ManyToOne
	@JoinColumn(name = "user_default_domain_id", referencedColumnName = "domain_id")
	private Domain defaultDomain;

	@Id
	@Column(name = "user_id")
	@GeneratedValue(generator = "events_user_id_seq")
	@TableGenerator(name = "events_user_id_seq", table = "events_sequence", initialValue = 50, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "user_fullname")
	private String fullname;

	@Basic
	@Column(name = "user_username")
	private String username;

	@Basic
	@Column(name = "user_password")
	private String password;

	@Basic
	@Column(name = "user_state")
	@Enumerated
	private UserStatus state;

	@Basic
	@Column(name = "user_min_editor_columns")
	private int minEditorColumns = 1;

	@Basic
	@Column(name = "user_max_editor_columns")
	private int maxEditorColumns = 1;

	@Basic
	@Column(name = "user_template_directory")
	private String templateDirectory;

	@Basic
	@Column(name = "user_last_used_form_letter")
	private String lastUsedFormLetter;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private final List<UserProperty> userProperties = new Vector<UserProperty>();

	private User()
	{
		super();
	}

	public Domain getDomain()
	{
		return this.defaultDomain;
	}

	public String getFullname()
	{
		return AbstractEntity.stringValueOf(this.fullname);
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getLastUsedFormLetter()
	{
		return stringValueOf(lastUsedFormLetter);
	}

	public int getMaxEditorColumns()
	{
		return maxEditorColumns;
	}

	public int getMinEditorColumns()
	{
		return minEditorColumns;
	}

	public String getPassword()
	{
		return AbstractEntity.stringValueOf(this.password);
	}

	public List<UserProperty> getProperties()
	{
		return userProperties;
	}

	public UserProperty getProperty(final String key)
	{
		for (UserProperty property : userProperties)
		{
			if (property.getKey().equals(key))
			{
				return property;
			}
		}
		UserProperty property = UserProperty.newInstance(this);
		property.setKey(key);
		return property;
	}

	public UserStatus getState()
	{
		return this.state;
	}

	public String getTemplateDirectory()
	{
		return stringValueOf(this.templateDirectory);
	}

	public String getUsername()
	{
		return AbstractEntity.stringValueOf(this.username);
	}

	public void setDomain(final Domain defaultDomain)
	{
		this.propertyChangeSupport.firePropertyChange("defaultDomain", this.defaultDomain,
				this.defaultDomain = defaultDomain);
	}

	public void setFullname(final String fullname)
	{
		this.propertyChangeSupport.firePropertyChange("fullname", this.fullname, this.fullname = fullname);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setLastUsedFormLetter(final String lastUsedFormLetter)
	{
		this.propertyChangeSupport.firePropertyChange("lastUsedFormLetter", this.lastUsedFormLetter,
				this.lastUsedFormLetter = lastUsedFormLetter);
	}

	public void setMaxEditorColumns(final int maxEditorColumns)
	{
		this.propertyChangeSupport.firePropertyChange("maxEditorColumns", this.maxEditorColumns,
				this.maxEditorColumns = maxEditorColumns);
	}

	public void setMinEditorColumns(final int minEditorColumns)
	{
		this.propertyChangeSupport.firePropertyChange("minEditorColumns", this.minEditorColumns,
				this.minEditorColumns = minEditorColumns);
	}

	public void setPassword(final String password)
	{
		this.propertyChangeSupport.firePropertyChange("password", this.password, this.password = password);
	}

	public void setProperty(final UserProperty property)
	{
		for (UserProperty userProperty : this.userProperties)
		{
			if (userProperty.getKey().equals(property.getKey()))
			{
				userProperty.setValue(property.getValue());
				return;
			}
		}
		this.userProperties.add(property);
	}

	public void setState(final UserStatus state)
	{
		this.propertyChangeSupport.firePropertyChange("state", this.state, this.state = state);
		this.state = state;
	}

	public void setTemplateDirectory(final String templateDirectory)
	{
		this.propertyChangeSupport.firePropertyChange("templateDirectory", this.templateDirectory,
				this.templateDirectory = templateDirectory);
	}

	public void setUsername(final String username)
	{
		this.propertyChangeSupport.firePropertyChange("username", this.username, this.username = username);
	}

	public static User getCurrent()
	{
		if (current == null)
		{
			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			tracker.open();

			String username = System.getProperty("user.name");
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				UserQuery query = (UserQuery) service.getQuery(User.class);
				User user = query.selectByUsername(username);
				if (user == null)
				{
					user = User.newInstance();
					user.setUsername(username);
					user.setState(UserStatus.USER);
					user = query.merge(user);
					if (user != null)
					{
						User.setCurrent(user);
					}
				}
				else
				{
					User.setCurrent(user);
				}
			}
			tracker.close();
		}

		return User.current;
	}

	public static boolean isCurrentUserAdministrator()
	{
		return (current != null && current.getState().equals(User.UserStatus.ADMINISTRATOR));
	}

	public static User newInstance()
	{
		return (User) AbstractEntity.newInstance(new User());
	}

	public static void setCurrent(final User current)
	{
		User.current = current;
	}

	public static enum UserStatus
	{
		ADMINISTRATOR, USER;

		String[] states = new String[] { "Administrator", "Benutzer" };

		@Override
		public String toString()
		{
			return this.states[this.ordinal()];
		}
	}

}
