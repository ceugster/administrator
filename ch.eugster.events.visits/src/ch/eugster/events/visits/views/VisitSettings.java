package ch.eugster.events.visits.views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.nebula.widgets.ganttchart.AbstractSettings;

public class VisitSettings extends AbstractSettings
{
	@Override
	public String getWeekHeaderTextDisplayFormatTop()
	{
		SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateInstance(DateFormat.MEDIUM);
		return formatter.toPattern();
	}

	@Override
	public String getDayHeaderTextDisplayFormatTop()
	{
		SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateInstance(DateFormat.MEDIUM);
		return formatter.toPattern();
	}

	@Override
	public String getMonthHeaderTextDisplayFormatTop()
	{
		return "MMMMM ''yy";
	}

}
