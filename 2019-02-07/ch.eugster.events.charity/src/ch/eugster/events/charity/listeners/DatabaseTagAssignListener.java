package ch.eugster.events.charity.listeners;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import ch.eugster.events.charity.Activator;
import ch.eugster.events.charity.TagReader;
import ch.eugster.events.charity.preferences.PreferenceInitializer;
import ch.eugster.events.persistence.model.CharityRunTagRead;
import ch.eugster.events.persistence.queries.CharityRunTagReadQuery;

import com.impinj.octane.Tag;

public class DatabaseTagAssignListener extends AbstractStartableTagReportListener implements IPropertyChangeListener
{
	private CharityRunTagReadQuery query;
	
	private Calendar calendar = GregorianCalendar.getInstance();
	
	private int timeBetweenReads;
	
	private boolean updated = false;
	
	public DatabaseTagAssignListener(CharityRunTagReadQuery query)
	{
		this.query = query;
	}
	
	public String label()
	{
		return "Datenbank aktiviert";
	}

	public void starting()
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(this);
		this.timeBetweenReads = store.getInt(PreferenceInitializer.KEY_TIME_BETWEEN_READS);
	}

	public void process(Tag tag) 
	{
		updated = false;
		CharityRunTagRead tagRead = query.selectLastRead(tag.getEpc().toHexString());
		if (tagRead == null)
		{
			tagRead = CharityRunTagRead.newInstance(TagReader.getCharityRun());
			tagRead.setAntennaPort(tag.getAntennaPortNumber());
			tagRead.setTagId(tag.getEpc().toHexString());
			tagRead.setFirstSeen(tag.getFirstSeenTime().getLocalDateTime());
			tagRead.setLastSeen(tag.getLastSeenTime().getLocalDateTime());
			updated = true;
		}
		else
		{
			calendar.setTime(tagRead.getLastSeen());
			calendar.add(Calendar.MILLISECOND, this.timeBetweenReads);
			if (calendar.getTime().before(tag.getLastSeenTime().getLocalDateTime()))
			{
				tagRead = CharityRunTagRead.newInstance(TagReader.getCharityRun());
				tagRead.setAntennaPort(tag.getAntennaPortNumber());
				tagRead.setTagId(tag.getEpc().toHexString());
				tagRead.setFirstSeen(tag.getFirstSeenTime().getLocalDateTime());
				tagRead.setLastSeen(tag.getLastSeenTime().getLocalDateTime());
				updated = true;
			}
			else
			{
				tagRead.setLastSeen(tag.getLastSeenTime().getLocalDateTime());
				updated = true;
			}
		}
		if (updated)
		{
			tagRead.incrementCount();
			query.merge(tagRead);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) 
	{
		if (event.getProperty().equals(PreferenceInitializer.KEY_TIME_BETWEEN_READS))
		{
			this.timeBetweenReads = ((Integer) event.getNewValue()).intValue();
		}
	}

	@Override
	protected void stopping() 
	{
	}
}
