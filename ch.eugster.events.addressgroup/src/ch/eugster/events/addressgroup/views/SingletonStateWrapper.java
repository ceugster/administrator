package ch.eugster.events.addressgroup.views;

import org.eclipse.core.commands.IStateListener;
import org.eclipse.core.commands.State;

public abstract class SingletonStateWrapper extends State
{
	public abstract State createSingleton();

	private final State singleton;

	public SingletonStateWrapper()
	{
		this.singleton = createSingleton();
	}

	@Override
	public void addListener(IStateListener listener)
	{
		singleton.addListener(listener);
	}

	@Override
	public void dispose()
	{
		singleton.dispose();
	}

	@Override
	public boolean equals(Object other)
	{
		return singleton.equals(other);
	}

	@Override
	public Object getValue()
	{
		return singleton.getValue();
	}

	@Override
	public int hashCode()
	{
		return singleton.hashCode();
	}

	@Override
	public void removeListener(IStateListener listener)
	{
		singleton.removeListener(listener);
	}

	@Override
	public void setId(String id)
	{
		singleton.setId(id);
	}

	@Override
	public void setValue(Object value)
	{
		singleton.setValue(value);
	}

	@Override
	public String toString()
	{
		return singleton.toString();
	}
}
