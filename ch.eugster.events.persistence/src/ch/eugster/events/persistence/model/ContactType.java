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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.Activator;
import ch.eugster.events.persistence.formatters.PhoneFormatter;

@Entity
@Table(name = "events_contact_type")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "contact_type_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "contact_type_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "contact_type_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "contact_type_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "contact_type_version")) })
public class ContactType extends AbstractEntity
{

	@Id
	@Column(name = "contact_type_id")
	@GeneratedValue(generator = "events_contact_type_id_seq")
	@TableGenerator(name = "events_contact_type_id_seq", table = "events_sequence", allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "contact_type_code")
	private String code;

	@Basic
	@Column(name = "contact_type_name")
	private String name;

	@Basic
	@Column(name = "contact_type_description")
	private String description;

	@Basic
	@Column(name = "contact_type_protocol")
	@Enumerated(EnumType.ORDINAL)
	private Protocol protocol;

	@Basic(fetch = FetchType.LAZY)
	@Lob
	@Column(name = "contact_type_icon", columnDefinition = "BLOB")
	private byte[] icon;

	private ContactType()
	{
	}

	public void copy(final ContactType source)
	{
		this.setCode(source.getCode());
		this.setDescription(source.getDescription());
		this.setName(source.getName());
	}

	public String getCode()
	{
		return AbstractEntity.stringValueOf(this.code);
	}

	public String getDescription()
	{
		return AbstractEntity.stringValueOf(this.description);
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public String getName()
	{
		return AbstractEntity.stringValueOf(this.name);
	}

	public Protocol getProtocol()
	{
		return protocol;
	}

	public void setCode(final String code)
	{
		this.propertyChangeSupport.firePropertyChange("code", this.code, this.code = code);
	}

	public void setDescription(final String description)
	{
		this.propertyChangeSupport.firePropertyChange("description", this.description, this.description = description);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setName(final String name)
	{
		this.propertyChangeSupport.firePropertyChange("name", this.name, this.name = name);
	}

	public void setProtocol(final Protocol protocol)
	{
		this.propertyChangeSupport.firePropertyChange("protocol", this.protocol, this.protocol = protocol);
	}

	public static ContactType newInstance()
	{
		return (ContactType) AbstractEntity.newInstance(new ContactType());
	}

	public enum Protocol
	{
		PHONE, MOBILE, EMAIL, HTTP, OTHER;

		public boolean check(final String address, Country country)
		{
			switch (this)
			{
				case PHONE:
				{
					return !PhoneFormatter.format(address, country).isEmpty();
				}
				case MOBILE:
				{
					return !PhoneFormatter.format(address, country).isEmpty();
				}
				case EMAIL:
				{
					return address.indexOf("@") > 0 && address.indexOf("@") < address.length()
							&& address.lastIndexOf("@") == address.indexOf("@");
				}
				case HTTP:
				{
					return address.length() > 0;
				}
				case OTHER:
				{
					return true;
				}
				default:
				{
					throw new RuntimeException("No such Protocol");
				}
			}
		}

		public String label()
		{
			switch (this)
			{
				case PHONE:
				{
					return "Telefon";
				}
				case MOBILE:
				{
					return "Mobile";
				}
				case EMAIL:
				{
					return "Email";
				}
				case HTTP:
				{
					return "Webadresse";
				}
				case OTHER:
				{
					
				}
				default:
				{
					throw new RuntimeException("No such Protocol");
				}
			}
		}

		public Image icon()
		{
			switch (this)
			{
				case PHONE:
				{
					return Activator.getDefault().getImageRegistry().get("phone");
				}
				case MOBILE:
				{
					return Activator.getDefault().getImageRegistry().get("mobile");
				}
				case EMAIL:
				{
					return Activator.getDefault().getImageRegistry().get("email");
				}
				case HTTP:
				{
					return Activator.getDefault().getImageRegistry().get("browse");
				}
				default:
				{
					return null;
				}
			}
		}

		public boolean useCountry()
		{
			switch (this)
			{
				case PHONE:
				{
					return true;
				}
				case MOBILE:
				{
					return true;
				}
				case EMAIL:
				{
					return false;
				}
				case HTTP:
				{
					return false;
				}
				case OTHER:
				{
					return false;
				}
				default:
				{
					throw new RuntimeException("No such Protocol");
				}
			}
		}
	}
}
