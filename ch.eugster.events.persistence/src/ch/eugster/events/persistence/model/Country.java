package ch.eugster.events.persistence.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "events_country")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "country_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "country_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "country_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "country_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "country_version")) })
public class Country extends AbstractEntity
{
	/*
	 * Data
	 */
	@Id
	@Column(name = "country_id")
	@GeneratedValue(generator = "events_country_id_seq")
	@TableGenerator(name = "events_country_id_seq", table = "events_sequence", initialValue = 1, allocationSize = 5)
	private Long id;

	@Basic
	@Column(name = "country_name")
	private String name;

	@Basic
	@Column(name = "country_iso_3166_alpha_2")
	private String iso3166alpha2;

	@Basic
	@Column(name = "country_iso_3166_alpha_3")
	private String iso3166alpha3;

	@Basic
	@Column(name = "country_iso_3166_numeric")
	private String iso31662numeric;

	@Basic
	@Column(name = "country_itu_code")
	private String ituCode;

	@Basic
	@Column(name = "country_phone_prefix")
	private String phonePrefix;

	@Basic
	@Column(name = "country_phone_pattern")
	private String phonePattern;

	@Basic
	@Column(name = "country_city_line_pattern")
	private String cityLinePattern;

	@Basic
	@Column(name = "country_address_line_pattern")
	private String addressLinePattern;

	@Basic
	@Convert("booleanConverter")
	@Column(name = "country_visible")
	private boolean visible;

	@Override
	public Long getId()
	{
		return id;
	}

	@Override
	public void setId(Long id)
	{
		this.id = id;
	}

	public void setItuCode(String code)
	{
		this.ituCode = code;
	}

	public String getItuCode()
	{
		return stringValueOf(ituCode);
	}

	public void setPhonePattern(String pattern)
	{
		this.phonePattern = pattern;
	}

	public String getPhonePattern()
	{
		String pattern = AbstractEntity.stringValueOf(phonePattern);
		return pattern.isEmpty() ? "#################" : pattern;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setIso3166alpha2(String iso3166alpha2)
	{
		this.iso3166alpha2 = iso3166alpha2;
	}

	public String getIso3166alpha2()
	{
		return stringValueOf(iso3166alpha2);
	}

	public void setIso3166alpha3(String iso3166alpha3)
	{
		this.iso3166alpha3 = iso3166alpha3;
	}

	public String getIso3166alpha3()
	{
		return stringValueOf(iso3166alpha3);
	}

	public void setIso31662numeric(String iso31662numeric)
	{
		this.iso31662numeric = iso31662numeric;
	}

	public String getIso31662numeric()
	{
		return stringValueOf(iso31662numeric);
	}

	public void setPhonePrefix(String prefix)
	{
		this.phonePrefix = prefix;
	}

	public String getPhonePrefix()
	{
		return AbstractEntity.stringValueOf(phonePrefix);
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public static Country newInstance()
	{
		return (Country) AbstractEntity.newInstance(new Country());
	}

	public String getCityLinePattern()
	{
		return stringValueOf(cityLinePattern);
	}

	public void setCityLinePattern(String cityLinePattern)
	{
		this.cityLinePattern = cityLinePattern;
	}

	public String getAddressLinePattern()
	{
		return stringValueOf(addressLinePattern);
	}

	public void setAddressLinePattern(String addressLinePattern)
	{
		this.addressLinePattern = addressLinePattern;
	}

}
