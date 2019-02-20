package ch.eugster.events.persistence.model;

import java.util.Date;

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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "events_charity_run_tag_read")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "charity_run_tag_read_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "charity_run_tag_read_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "charity_run_tag_read_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "charity_run_tag_read_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "charity_run_tag_read_version")) })
public class CharityRunTagRead extends AbstractEntity
{
	/**
	 * Data
	 */
	@Id
	@Column(name = "charity_run_tag_read_id")
	@GeneratedValue(generator = "events_charity_run_tag_read_id_seq")
	@TableGenerator(name = "events_charity_run_tag_read_id_seq", table = "events_sequence")
	private Long id;

	/**
	 * References
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "charity_run_tag_read_charity_run_id", referencedColumnName = "charity_run_id")
	private CharityRun charityRun;

	@Basic
	@Column(name = "charity_run_tag_read_antenna_port")
	private short antennaPort;

	@Basic
	@Column(name = "charity_run_tag_read_tag_id")
	private String tagId;

	@Basic
	@Column(name = "charity_run_tag_read_tag_count")
	private int count;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "charity_run_tag_read_first_seen")
	private Date firstSeen;
	
	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "charity_run_tag_read_last_seen")
	private Date lastSeen;
	
	private CharityRunTagRead()
	{
		super();
	}

	private CharityRunTagRead(CharityRun charityRun)
	{
		super();
		this.charityRun = charityRun;
	}
	
	public Long getId()
	{
		return this.id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setAntennaPort(Short antennaPort)
	{
		this.antennaPort = antennaPort;
	}

	public Short getAntennaPort()
	{
		return this.antennaPort;
	}
	
	public void setTagId(String tagId)
	{
		this.tagId = tagId;
	}

	public String getTagId()
	{
		return this.tagId;
	}
	
	public void setCount(int count)
	{
		this.count = count;
	}

	public int getCount()
	{
		return this.count;
	}
	
	public void incrementCount()
	{
		this.count++;
	}
	
	public void setFirstSeen(Date firstSeen)
	{
		this.firstSeen = firstSeen;
	}

	public Date getFirstSeen()
	{
		return this.firstSeen;
	}
	
	public void setLastSeen(Date lastSeen)
	{
		this.lastSeen = lastSeen;
	}

	public Date getLastSeen()
	{
		return this.lastSeen;
	}
	
	public void setCharityRun(CharityRun charityRun)
	{
		this.charityRun = charityRun;
	}

	public CharityRun getCharityRun()
	{
		return this.charityRun;
	}
	
	public static CharityRunTagRead newInstance(CharityRun charityRun)
	{
		return (CharityRunTagRead) AbstractEntity.newInstance(new CharityRunTagRead(charityRun));
	}
}
