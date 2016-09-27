package ch.eugster.events.persistence.formatters;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import ch.eugster.events.persistence.model.Visit;

public class DateFormatter 
{
	public static DateFormatter getInstance()
	{
		return new DateFormatter();
	}
	
	public String formatDateRange(Calendar start, Calendar end)
	{
		StringBuilder daterange = new StringBuilder();
		if (start != null && end != null)
		{
			if (start.get(Calendar.YEAR) == end.get(Calendar.YEAR))
			{
				if (start.get(Calendar.MONTH) == end.get(Calendar.MONTH))
				{
					if (start.get(Calendar.DATE) == end.get(Calendar.DATE))
					{
						daterange = daterange.append(SimpleDateFormat.getDateInstance().format(start.getTime()));
						daterange = daterange.append(" " + new SimpleDateFormat("HH:mm").format(start.getTime()));
						daterange = daterange.append("-" + new SimpleDateFormat("HH:mm").format(end.getTime()));
					}
				}
			}
			if (daterange.length() == 0)
			{
				daterange = daterange.append(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(start.getTime()));
				daterange = daterange.append("-" + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(end.getTime()));
			}
		}
		else if (end == null)
		{
			daterange = daterange.append(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(start.getTime()));
		}
		else if (start == null)
		{
			daterange = daterange.append(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(end.getTime()));
		}
		return daterange.toString();
	}
}
