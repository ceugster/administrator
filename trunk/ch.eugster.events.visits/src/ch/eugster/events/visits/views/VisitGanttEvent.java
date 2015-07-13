package ch.eugster.events.visits.views;

import org.eclipse.nebula.widgets.ganttchart.GanttChart;
import org.eclipse.nebula.widgets.ganttchart.GanttEvent;

import ch.eugster.events.persistence.model.Visit;

public class VisitGanttEvent extends GanttEvent 
{
	public VisitGanttEvent(Visit visit, GanttChart ganttChart) 
	{
		super(ganttChart,visit.get);
	}
}
