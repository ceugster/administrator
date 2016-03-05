/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
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
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "version_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "version_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "version_updated")),
		@AttributeOverride(name = "version", column = @Column(name = "version_version")),
		@AttributeOverride(name = "deleted", column = @Column(name = "version_deleted")) })
@Table(name = "events_version")
@NamedQuery(name = "findVersion", query = "SELECT OBJECT(version) FROM Version version WHERE version.id = 1")
public class Version extends AbstractEntity
{
	public static final int DATA_VERSION = 3;

	public static final int STRUCTURE_VERSION = 38;

	@Id
	@Column(name = "version_id")
	@GeneratedValue(generator = "events_version_id_seq")
	@TableGenerator(name = "events_version_id_seq", table = "events_sequence", initialValue = 2, allocationSize = 5)
	protected Long id;

	@Column(name = "version_data")
	private int dataVersion;

	@Basic
	@Column(name = "version_structure")
	private int structureVersion;

	private Version()
	{
		super();
	}

	public int getDataVersion()
	{
		return this.dataVersion;
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	public int getStructureVersion()
	{
		return this.structureVersion;
	}

	public void setDataVersion(final int dataVersion)
	{
		this.propertyChangeSupport.firePropertyChange("dataVersion", this.dataVersion, this.dataVersion = dataVersion);
	}

	@Override
	public void setId(final Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void setStructureVersion(final int structureVersion)
	{
		this.propertyChangeSupport.firePropertyChange("structureVersion", this.structureVersion,
				this.structureVersion = structureVersion);
	}

	public static Version newInstance()
	{
		return (Version) AbstractEntity.newInstance(new Version());
	}

}
