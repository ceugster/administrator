package ch.eugster.events.persistence.model;

import static javax.persistence.CascadeType.ALL;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "events_charity_run")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "charity_run_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "charity_run_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "charity_run_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "charity_run_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "charity_run_version")) })
public class CharityRun extends AbstractEntity
{
	@Id
	@Column(name = "charity_run_id")
	@GeneratedValue(generator = "events_charity_run_id_seq")
	@TableGenerator(name = "events_charity_run_id_seq", table = "events_sequence")
	private Long id;

	@Basic
	@Column(name = "charity_run_name")
	private String name;

	@Basic
	@Column(name = "charity_run_description")
	private String description;

	@Basic
	@Column(name = "charity_run_place")
	private String place;

	@Basic
	@Column(name = "charity_run_state")
	private CharityRunState state;

	@Basic
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "charity_run_date")
	private Calendar date;

	@Basic
	@Column(name = "charity_run_round_length")
	private double roundLength;
	/*
	 * Runners
	 */
	@OneToMany(cascade = ALL, mappedBy = "charityRun")
	private List<CharityRunner> runners = new Vector<CharityRunner>();

	private CharityRun()
	{
		super();
	}

	@Override
	public Long getId()
	{
		return this.id;
	}

	@Override
	public void setId(Long id)
	{
		this.propertyChangeSupport.firePropertyChange("id", this.id, this.id = id);
	}

	public void copy(CharityRun schoolClass)
	{
	}

	public void setName(String name)
	{
		this.propertyChangeSupport.firePropertyChange("name", this.name,
				this.name = (name == null || name.isEmpty()) ? null : name);
	}

	public String getName()
	{
		return stringValueOf(this.name);
	}
	
	public void setDescription(String description)
	{
		this.propertyChangeSupport.firePropertyChange("description", this.description, this.description = (description == null || description.isEmpty()) ? null : description);
	}

	public String getDescription()
	{
		return stringValueOf(this.description);
	}
	
	public void setPlace(String place)
	{
		this.propertyChangeSupport.firePropertyChange("place", this.place, this.place = (place == null || place.isEmpty()) ? null : place);
	}

	public String getPlace()
	{
		return stringValueOf(this.place);
	}
	
	public void setState(CharityRunState state)
	{
		this.propertyChangeSupport.firePropertyChange("state", this.state, this.state = state);
	}

	public CharityRunState getState()
	{
		return this.state;
	}
	
	public void setDate(Calendar date)
	{
		this.propertyChangeSupport.firePropertyChange("date", this.date, this.date = date);
	}

	public Calendar getDate()
	{
		return this.date;
	}
	
	public void addRunner(CharityRunner runner)
	{
		if (!this.getRunners().contains(runner))
		{
			this.propertyChangeSupport.firePropertyChange("addRrunner", this.runners, this.runners.add(runner));
		}
	}
	
	public void removeRunner(CharityRunner runner)
	{
		this.propertyChangeSupport.firePropertyChange("removeRrunner", this.runners, this.runners.remove(runner));
	}
	
	public List<CharityRunner> getRunners()
	{
		List<CharityRunner> activeRunners = new Vector<CharityRunner>();
		for (CharityRunner runner : this.runners)
		{
			if (runner.isValid() && (runner.getRunners().size() > 0 || runner.getLeader() == null))
			{
				activeRunners.add(runner);
			}
		}
		return activeRunners;
	}

	public double getRoundLength() 
	{
		return roundLength;
	}

	public void setRoundLength(double roundLength) 
	{
		this.propertyChangeSupport.firePropertyChange("roundLength", this.roundLength, this.roundLength = roundLength);
	}

	public static CharityRun newInstance()
	{
		return (CharityRun) AbstractEntity.newInstance(new CharityRun());
	}
	
	public enum CharityRunState
	{
		ACTIVE, CLOSED;
	
		public String label()
		{
			switch (this)
			{
			case ACTIVE:
			{
				return "Aktiv";
			}
			case CLOSED:
			{
				return "Geschlossen";
			}
			default:
			{
				throw new RuntimeException("Invalid charity run state");
			}
			}
		}
	}
}
