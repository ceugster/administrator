package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "events_email_account")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "email_account_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "email_account_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "email_account_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "email_account_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "email_account_version")) })
public class EmailAccount extends AbstractEntity
{
	/**
	 * Data
	 */
	@Id
	@Column(name = "email_account_id")
	@GeneratedValue(generator = "events_email_account_id_seq")
	@TableGenerator(name = "events_email_account_id_seq", table = "events_sequence")
	private Long id;

	@Basic
	@Column(name = "email_account_type")
	@Enumerated(EnumType.ORDINAL)
	private Type type;

	@Basic
	@Column(name = "email_account_host")
	private String host;

	@Basic
	@Column(name = "email_account_port")
	private int port;

	@Basic
	@Column(name = "email_account_auth")
	@Convert("booleanConverter")
	private boolean auth;

	@Basic
	@Column(name = "email_account_username")
	private String username;

	@Basic
	@Column(name = "email_account_password")
	private String password;

	@Basic
	@Column(name = "email_account_starttls_enable")
	@Convert("booleanConverter")
	private boolean starttlsEnable;

	@Basic
	@Column(name = "email_account_ssl_enable")
	@Convert("booleanConverter")
	private boolean sslEnable;

	private EmailAccount()
	{
		super();
	}

	public void copy(final EmailAccount account)
	{
	}

	public String getHost()
	{
		return host;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getPassword()
	{
		return password;
	}

	public int getPort()
	{
		return port;
	}

	public Type getType()
	{
		return type;
	}

	public String getUsername()
	{
		return username;
	}

	public boolean isAuth()
	{
		return auth;
	}

	public boolean isSslEnable()
	{
		return sslEnable;
	}

	public boolean isStarttlsEnable()
	{
		return starttlsEnable;
	}

	public void setAuth(final boolean auth)
	{
		this.auth = auth;
	}

	public void setHost(final String host)
	{
		this.host = host;
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setPassword(final String password)
	{
		this.propertyChangeSupport.firePropertyChange("password", this.password, this.password = password);
	}

	public void setPort(final int port)
	{
		this.propertyChangeSupport.firePropertyChange("port", this.port, this.port = port);
	}

	public void setSslEnable(final boolean sslEnable)
	{
		this.sslEnable = sslEnable;
	}

	public void setStarttlsEnable(final boolean starttlsEnable)
	{
		this.starttlsEnable = starttlsEnable;
	}

	public void setType(final Type type)
	{
		this.propertyChangeSupport.firePropertyChange("type", this.type, this.type = type);
	}

	public void setUsername(final String username)
	{
		this.propertyChangeSupport.firePropertyChange("username", this.username, this.username = username);
	}

	public static EmailAccount newInstance()
	{
		return (EmailAccount) AbstractEntity.newInstance(new EmailAccount());
	}

	public enum Type
	{
		SMTP, IMAP, POP3;

		public String authKey()
		{
			switch (this)
			{
				case SMTP:
				{
					return "mail.smtp.auth";
				}
				case IMAP:
				{
					return "mail.imap.auth";
				}
				case POP3:
				{
					return "mail.pop3.auth";
				}
				default:
				{
					return null;
				}
			}
		}

		public String hostKey()
		{
			switch (this)
			{
				case SMTP:
				{
					return "mail.smtp.host";
				}
				case IMAP:
				{
					return "mail.imap.host";
				}
				case POP3:
				{
					return "mail.pop3.host";
				}
				default:
				{
					return null;
				}
			}
		}

		public String label()
		{
			switch (this)
			{
				case SMTP:
				{
					return "SMTP";
				}
				case IMAP:
				{
					return "IMAP";
				}
				case POP3:
				{
					return "POP3";
				}
				default:
				{
					return null;
				}
			}
		}

		public String portKey()
		{
			switch (this)
			{
				case SMTP:
				{
					return "mail.smtp.port";
				}
				case IMAP:
				{
					return "mail.imap.port";
				}
				case POP3:
				{
					return "mail.pop3.port";
				}
				default:
				{
					return null;
				}
			}
		}

		public String sslKey()
		{
			switch (this)
			{
				case SMTP:
				{
					return "mail.smtp.ssl.enable";
				}
				case IMAP:
				{
					return "mail.imap.ssl.enable";
				}
				case POP3:
				{
					return "mail.pop3.ssl.enable";
				}
				default:
				{
					return null;
				}
			}
		}

		public String starttlsKey()
		{
			switch (this)
			{
				case SMTP:
				{
					return "mail.smtp.starttls.enable";
				}
				case IMAP:
				{
					return "mail.imap.starttls.enable";
				}
				case POP3:
				{
					return "mail.pop3.starttls.enable";
				}
				default:
				{
					return null;
				}
			}
		}

		public String storeKey()
		{
			switch (this)
			{
				case SMTP:
				{
					return "";
				}
				case IMAP:
				{
					return "imap";
				}
				case POP3:
				{
					return "pop3";
				}
				default:
				{
					return null;
				}
			}
		}
	}
}
