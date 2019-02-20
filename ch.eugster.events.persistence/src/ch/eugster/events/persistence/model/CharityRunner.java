package ch.eugster.events.persistence.model;

import static javax.persistence.CascadeType.ALL;

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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "events_charity_runner")
@AssociationOverrides({ @AssociationOverride(name = "user", joinColumns = @JoinColumn(name = "charity_runner_user_id")) })
@AttributeOverrides({ @AttributeOverride(name = "inserted", column = @Column(name = "charity_runner_inserted")),
		@AttributeOverride(name = "updated", column = @Column(name = "charity_runner_updated")),
		@AttributeOverride(name = "deleted", column = @Column(name = "charity_runner_deleted")),
		@AttributeOverride(name = "version", column = @Column(name = "charity_runner_version")) })
public class CharityRunner extends AbstractEntity
{
	@Id
	@Column(name = "charity_runner_id")
	@GeneratedValue(generator = "events_charity_runner_id_seq")
	@TableGenerator(name = "events_charity_runner_id_seq", table = "events_sequence")
	private Long id;

	/**
	 * References
	 */
	@ManyToOne(optional = false)
	@JoinColumn(name = "charity_runner_charity_run_id", referencedColumnName = "charity_run_id")
	private CharityRun charityRun;

	@ManyToOne(optional = false, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "charity_runner_charity_person_id", referencedColumnName = "charity_person_id")
	private CharityPerson person;

	@ManyToOne(optional = true, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "charity_runner_charity_tag_id", referencedColumnName = "charity_tag_id")
	private CharityTag tag;

	@ManyToOne(optional = true, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "charity_runner_leader_id", referencedColumnName = "charity_runner_id")
	private CharityRunner leader;

	@Basic
	@Column(name = "charity_runner_leadership")
	@Convert("booleanConverter")
	private boolean leadership;

	@Basic
	@Column(name = "charity_runner_group_name")
	private String groupName;

	@Basic
	@Column(name = "charity_runner_rounds")
	private int rounds;

	@Basic
	@Column(name = "charity_runner_variable_amount")
	private double variableAmount;

	@Basic
	@Column(name = "charity_runner_fix_amount")
	private double fixAmount;
	/*
	 * Runners
	 */
	@OneToMany(cascade = ALL, mappedBy = "leader")
	private List<CharityRunner> runners = new Vector<CharityRunner>();

	private CharityRunner()
	{
		super();
	}

	private CharityRunner(CharityRun charityRun, CharityPerson charityPerson)
	{
		this.charityRun = charityRun;
		this.person = charityPerson;
	}
	
	private CharityRunner(CharityRun charityRun, CharityPerson charityPerson, CharityRunner leader)
	{
		this.charityRun = charityRun;
		this.person = charityPerson;
		this.leader = leader;
	}
	
	@PostPersist
	private void postPersist()
	{
		if (this.getLeader() != null)
		{
			this.getLeader().addRunner(this);
		}
		this.getCharityRun().addRunner(this);
	}
	
	@PostUpdate
	private void postUpdate()
	{
		if (this.getLeader() != null && !this.getLeader().getRunners().contains(this))
		{
			this.getLeader().addRunner(this);
		}
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

	public void setPerson(CharityPerson person)
	{
		this.propertyChangeSupport.firePropertyChange("person", this.person, this.person = person);
	}

	public CharityPerson getPerson()
	{
		return this.person;
	}
	
	public void setLeader(CharityRunner leader)
	{
		this.propertyChangeSupport.firePropertyChange("leader", this.leader, this.leader = leader);
	}

	public CharityRunner getLeader()
	{
		return this.leader;
	}
	
	public void setCharityRun(CharityRun charity_run)
	{
		this.propertyChangeSupport.firePropertyChange("charityRun", this.charityRun, this.charityRun = charity_run);
	}

	public CharityRun getCharityRun()
	{
		return this.charityRun;
	}
	
	public void setTag(CharityTag tag)
	{
		this.propertyChangeSupport.firePropertyChange("tag", this.tag, this.tag = tag);
	}

	public CharityTag getTag()
	{
		return this.tag;
	}
	
	public void addRunner(CharityRunner runner)
	{
		if (runner.getLeader() != null && runner.getLeader().getId().equals(this.getId()) && !this.getRunners().contains(runner))
		{
			this.propertyChangeSupport.firePropertyChange("addRrunner", this.runners, this.runners.add(runner));
		}
	}
	
	public void removeRunner(CharityRunner runner)
	{
		this.propertyChangeSupport.firePropertyChange("removeRrunner", this.runners, this.runners.remove(runner));
	}
	
	public boolean hasLeadership() 
	{
		return leadership;
	}

	public void setLeadership(boolean leadership) 
	{
		this.propertyChangeSupport.firePropertyChange("leadership", this.leadership, this.leadership = leadership);
	}

	public String getGroupName() 
	{
		return stringValueOf(groupName);
	}

	public void setGroupName(String groupName) 
	{
		this.propertyChangeSupport.firePropertyChange("groupName", this.groupName, this.groupName = (groupName == null || groupName.isEmpty()) ? null : groupName);
	}

	public List<CharityRunner> getRunners()
	{
		List<CharityRunner> activeRunners = new Vector<CharityRunner>();
		for (CharityRunner runner : this.runners)
		{
			if (runner.isValid())
			{
				activeRunners.add(runner);
			}
		}
		return activeRunners;
	}
	
	public int getRounds() 
	{
		return this.rounds;
	}

	public void setRounds(int rounds) 
	{
		this.propertyChangeSupport.firePropertyChange("rounds", this.rounds, this.rounds = rounds);
	}

	public double getVariableAmount() 
	{
		return this.variableAmount;
	}

	public void setVariableAmount(double variableAmount) 
	{
		this.propertyChangeSupport.firePropertyChange("variableAmount", this.variableAmount, this.variableAmount = variableAmount);
	}

	public double getFixAmount() 
	{
		return this.fixAmount;
	}

	public void setFixAmount(double fixAmount) 
	{
		this.propertyChangeSupport.firePropertyChange("fixAmount", this.fixAmount, this.fixAmount = fixAmount);
	}

	public static CharityRunner newInstance(CharityRun charityRun, CharityPerson charityPerson)
	{
		return (CharityRunner) AbstractEntity.newInstance(new CharityRunner(charityRun, charityPerson));
	}

	public static CharityRunner newInstance(CharityRun charityRun, CharityPerson charityPerson, CharityRunner leader)
	{
		return (CharityRunner) AbstractEntity.newInstance(new CharityRunner(charityRun, charityPerson, leader));
	}
}
