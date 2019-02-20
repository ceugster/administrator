package ch.eugster.events.charity.listeners;

import com.impinj.octane.Tag;

public interface StartableTagReportListener 
{
	String label();
	
	boolean isActive();
	
	void setActive(boolean active);
	
	boolean started();
	
	void start();
	
	void stop();
	
	void process(Tag tag);
}
