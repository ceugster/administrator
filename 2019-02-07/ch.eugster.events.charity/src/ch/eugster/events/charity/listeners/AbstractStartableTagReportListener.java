package ch.eugster.events.charity.listeners;

import ch.eugster.events.charity.TagReader;

import com.impinj.octane.Tag;

public abstract class AbstractStartableTagReportListener implements
		StartableTagReportListener
{
	private boolean started = false;
	
	private boolean active = false;
	
	public boolean isActive()
	{
		return this.active;
	}
	
	public void setActive(boolean active)
	{
		this.active = active;
		if (this.active)
		{
			if (TagReader.isStarted())
			{
				this.start();
			}
		}
		else
		{
			if (this.started)
			{
				this.stop();
			}
		}
	}

	@Override
	public abstract void process(Tag tag);

	@Override
	public abstract String label();

	protected abstract void starting();
	
	protected abstract void stopping();
	
	@Override
	public boolean started() 
	{
		return this.started;
	}

	@Override
	public void start() 
	{
		if (this.active)
		{
			this.starting();
			this.started = true;
		}
	}

	@Override
	public void stop() 
	{
		if (this.started)
		{
			this.started = false;
			this.stopping();
		}
	}
}
