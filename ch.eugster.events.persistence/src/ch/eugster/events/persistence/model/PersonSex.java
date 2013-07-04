package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "events_person_sex")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "person_sex_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "person_sex_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "person_sex_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "person_sex_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "person_sex_version")) })
public class PersonSex extends AbstractEntity
{
	@Id
	@Column(name = "person_sex_id")
	@GeneratedValue(generator = "events_person_sex_id_seq")
	@TableGenerator(name = "events_person_sex_id_seq", table = "events_sequence", allocationSize = 1)
	private Long id;

	@Basic
	@Column(name = "person_sex_salutation")
	private String salutation;

	@Basic
	@Column(name = "person_sex_personal")
	private String personal;

	@Basic
	@Column(name = "person_sex_polite")
	private String polite;

	@Basic
	@Column(name = "person_sex_symbol")
	private String symbol;

	@Basic(fetch = FetchType.LAZY)
	@Lob
	@Column(name = "person_sex_image_data", columnDefinition = "BLOB")
	private byte[] image;

	@Override
	public Long getId()
	{
		return id;
	}

	@Override
	public void setId(Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setSalutation(String salutation)
	{
		this.propertyChangeSupport.firePropertyChange("salutation", this.salutation, this.salutation = salutation);
	}

	public String getSalutation()
	{
		return AbstractEntity.stringValueOf(salutation);
	}

	public void setPersonal(String personal)
	{
		this.propertyChangeSupport.firePropertyChange("personal", this.personal, this.personal = personal);
	}

	public String getPersonal()
	{
		return AbstractEntity.stringValueOf(personal);
	}

	public void setPolite(String polite)
	{
		this.propertyChangeSupport.firePropertyChange("polite", this.polite, this.polite = polite);
	}

	public String getPolite()
	{
		return AbstractEntity.stringValueOf(polite);
	}

	public void setImage(byte[] image)
	{
		this.propertyChangeSupport.firePropertyChange("image", this.image, this.image = image);
	}

	public byte[] getImage()
	{
		return image;
	}

	public void setSymbol(String symbol)
	{
		this.propertyChangeSupport.firePropertyChange("symbol", this.symbol, this.symbol = symbol);
	}

	public String getSymbol()
	{
		return AbstractEntity.stringValueOf(symbol);
	}

	public String getForm(PersonForm form)
	{
		switch (form)
		{
			case POLITE:
			{
				return this.getPolite();
			}
			case PERSONAL:
			{
				return this.getPersonal();
			}
			default:
			{
				return this.getPolite();
			}
		}
	}
}
