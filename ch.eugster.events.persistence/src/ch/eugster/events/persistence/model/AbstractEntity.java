package ch.eugster.events.persistence.model;

import java.beans.PropertyChangeSupport;
import java.util.Calendar;

import javax.persistence.Basic;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.ConversionValue;
import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.ObjectTypeConverter;

import ch.eugster.events.persistence.events.EntityMediator;

@MappedSuperclass
@EntityListeners(EntityMediator.class)
@ObjectTypeConverter(name = "booleanConverter", dataType = java.lang.Short.class, objectType = java.lang.Boolean.class, conversionValues = {
		@ConversionValue(dataValue = "0", objectValue = "false"),
		@ConversionValue(dataValue = "1", objectValue = "true") }, defaultObjectValue = "false")
public abstract class AbstractEntity implements IEntity
{
	@Basic
	@Convert("booleanConverter")
	protected boolean deleted;

	@Basic
	protected int version;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	protected Calendar inserted;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	protected Calendar updated;

	/*
	 * References
	 */
	@ManyToOne(optional = true)
	@JoinColumn(referencedColumnName = "user_id")
	protected User user;

	@Transient
	protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	@Override
	public boolean equals(final Object other)
	{
		if (other == null)
		{
			return false;
		}
		if (this == other)
		{
			return true;
		}
		if (other.getClass().equals(this.getClass()))
		{
			final AbstractEntity entity = (AbstractEntity) other;
			if ((((AbstractEntity) other).getId() != null) && (this.getId() != null))
			{
				if (entity.getId().equals(this.getId()))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public abstract Long getId();

	public Calendar getInserted()
	{
		return inserted;
	}

	public PropertyChangeSupport getPropertyChangeSupport()
	{
		return this.propertyChangeSupport;
	}

	public Calendar getUpdated()
	{
		return updated;
	}

	public User getUser()
	{
		return user;
	}

	public int getVersion()
	{
		return this.version;
	}

	@Override
	public boolean isDeleted()
	{
		return this.deleted;
	}

	public void setDeleted(final boolean deleted)
	{
		if (this instanceof AddressGroupMember && deleted)
		{
			System.out.println();
		}
		this.propertyChangeSupport.firePropertyChange("deleted", this.deleted, this.deleted = deleted);
	}

	public abstract void setId(Long id);

	public void setInserted(final Calendar inserted)
	{
		this.propertyChangeSupport.firePropertyChange("inserted", this.inserted, this.inserted = inserted);
	}

	public void setUpdated(final Calendar updated)
	{
		this.propertyChangeSupport.firePropertyChange("updated", this.updated, this.updated = updated);
	}

	public void setUser(final User user)
	{
		this.propertyChangeSupport.firePropertyChange("user", this.user, this.user = user);
	}

	public void setVersion(final int version)
	{
		this.propertyChangeSupport.firePropertyChange("version", this.version, this.version = version);
	}
	
	public String getEntityName()
	{
		return this.getClass().getName();
	}
	
	public String getInstanceName()
	{
		return this.getClass().getName();
	}

	protected static AbstractEntity newInstance(final AbstractEntity entity)
	{
		entity.setId(null);
		entity.setDeleted(false);
		entity.setVersion(0);
		entity.setInserted(null);
		return entity;
	}

	public static String stringValueOf(final String value)
	{
		return value == null ? "" : value.trim();
	}

}
