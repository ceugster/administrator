package ch.eugster.events.visits.views;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.ganttchart.AdvancedTooltip;
import org.eclipse.nebula.widgets.ganttchart.GanttChart;
import org.eclipse.nebula.widgets.ganttchart.GanttEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityListener;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.DateFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.model.VisitVisitor;
import ch.eugster.events.persistence.queries.VisitQuery;

public class ThemeView extends AbstractGanttView<Visit>
{
	public static final String ID = "ch.eugster.events.visits.theme.view";

	private Map<Long, GanttEvent> events = new HashMap<Long, GanttEvent>();
	
	private EntityListener themeListener;
	
	private EntityListener visitListener;

	protected Menu extendMenu(final GanttChart ganttChart)
	{
		Menu menu = new Menu(ganttChart.getGanttComposite());
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("MenuItem");
		return menu;
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Visit.class, this.themeListener);
		EntityMediator.removeListener(Visit.class, this.visitListener);
		super.dispose();
	}

	protected void registerEntityListeners()
	{
		this.visitListener = new EntityAdapter()
		{
			@Override
			public void postDelete(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
					GanttEvent event = ThemeView.this.events.remove(visit.getId());
					if (event != null)
					{
						event.dispose();
					}
//					ganttChart.redrawGanttChart();
				}
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
					if (visit.getStart() != null && visit.getEnd() != null)
					{
						GanttEvent event = new GanttEvent(ganttChart, getLabelText(visit), visit.getStart(), visit.getEnd(), 0);
						updateEvent(event, visit);
						events.put(visit.getId(), event);
					}
				}
			}

			@Override
			public void postRemove(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
					GanttEvent event = ThemeView.this.events.remove(visit.getId());
					if (event != null)
					{
						event.dispose();
					}
//					ganttChart.redrawGanttChart();
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
					if (visit.getStart() != null && visit.getEnd() != null)
					{
						GanttEvent event = events.get(visit.getId());
						if (event == null && visit.getStart() != null && visit.getEnd() != null)
						{
							event = new GanttEvent(ganttChart, getLabelText(visit), visit.getStart(), visit.getEnd(), 0);
							events.put(visit.getId(), event);
						}
						updateEvent(event, visit);
					}
				}
			}
		};
		EntityMediator.addListener(Visit.class, this.visitListener);
	}

	private void updateEvent(GanttEvent event, Visit visit)
	{
		event.setData(visit);
		event.setAdvancedTooltip(getTooltip(visit));
		event.setEndDate(visit.getEnd());
		event.setHorizontalTextLocation(SWT.RIGHT);
		event.setName(getLabelText(visit));
		event.setShowBoldText(true);
		event.setStartDate(visit.getStart());
		if (visit.getTheme() != null && visit.getTheme().getColor() != null)
		{
			java.awt.Color awtColor = new java.awt.Color(visit.getTheme().getColor().intValue());
			org.eclipse.swt.graphics.Color swtColor =new Color(Display.getCurrent(), awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
			event.setGradientStatusColor(swtColor);
		}
		event.setShowBoldText(true);
		event.setTextDisplayFormat("#name#");
	}
	
	@Override
	public void setFocus()
	{
		ganttChart.setFocus();
	}

	public void setLevel(final int zoomLevel, final int view)
	{
		ganttChart.getGanttComposite().setView(view);
		ganttChart.getGanttComposite().setZoomLevel(zoomLevel);
		settings.put("view", view);
		settings.put("zoom", zoomLevel);
	}

	@Override
	protected void initialize()
	{
		if (connectionService != null)
		{
			VisitQuery query = (VisitQuery) connectionService.getQuery(Visit.class);
			List<Visit> visits = query.selectAll();
			for (Visit visit : visits)
			{
				if (visit.getStart() != null && visit.getEnd() != null)
				{
					GanttEvent event = new GanttEvent(ganttChart, getLabelText(visit), visit.getStart(), visit.getEnd(), 0);
					updateEvent(event, visit);
					events.put(visit.getId(), event);
				}
			}
		}
	}

	private String getLabelText(final Object object)
	{
		StringBuilder builder = new StringBuilder();
		if (object instanceof Visit)
		{
			Visit visit = (Visit) object;
			if (visit.getTheme() != null)
			{
				builder = builder.append(visit.getTheme().getName());
			}
			builder = builder.append(" (" + DateFormatter.getInstance().formatDateRange(visit.getStart(), visit.getEnd()) + ")");
		}
		return builder.toString();
	}
	
	protected String getContent(final Object object)
	{
		StringBuilder content = new StringBuilder();
		if (object instanceof Visit)
		{
			Visit visit = (Visit) object;
			/*
			 * Dates
			 */
			content = content.append(visit.getFormattedPeriod());
			if (content.length() > 0)
			{
				content = content.append("\n\n");
			}

			content = content.append("Thema: " + (visit.getTheme() == null ? "<nicht definiert>" : visit.getTheme().getName()));
			content = content.append("\n");
			for (VisitVisitor visitor : visit.getVisitors())
			{
				if (!visitor.isDeleted())
				{
					content = content.append("\n");
					content = content.append(visitor.getType().label() + ": ");
					content = content.append(PersonFormatter.getInstance().formatLastnameFirstname(
							visitor.getVisitor().getLink().getPerson()));
				}
			}
			content = content.append("\n");
			if (visit.getTeacher() != null)
			{
				content = content.append("Lehrperson: "
						+ PersonFormatter.getInstance().formatFirstnameLastname(visit.getTeacher().getLink().getPerson()));
			}
			if (!visit.getClassName().isEmpty())
			{
				content = content.append("\n\n");
				content = content
						.append("Klasse: " + visit.getClassName() + " (").append(NumberFormat.getIntegerInstance().format(visit.getPupils()) + " Schüler/innen)");
			}
			if (visit.getTeacher() != null)
			{
				content = content.append("\n\n");
				Address address = visit.getTeacher().getLink().getAddress();
				content = content
						.append("Schulhaus: ")
						.append(Address.stringValueOf(address.getName()).trim().isEmpty() ? "?, " : address.getName()
								+ ", ")
						.append("Stockwerk: "
								+ (Visit.stringValueOf(visit.getFloor()).isEmpty() ? "?, " : visit.getFloor() + ", "))
						.append("Schulzimmer: "
								+ (Visit.stringValueOf(visit.getClassRoom()).trim().isEmpty() ? "?" : ", "
										+ visit.getClassRoom()));
				content = content.append("\n");
				content = content.append("Ort: " + AddressFormatter.getInstance().formatCityLine(address) + " ("
						+ address.getProvince() + ")");
				content = content.append("\n\n");
			}

		}
		return content.toString();
	}

	protected AdvancedTooltip getTooltip(final Object object)
	{
		StringBuilder tooltipTitle = new StringBuilder();
		if (object instanceof Visit)
		{
			Visit visit = (Visit) object;
			tooltipTitle = tooltipTitle.append(visit.getTheme() == null ? "Thema nicht definiert" : visit.getTheme().getName());
			tooltipTitle = tooltipTitle.append(visit.getState() == null ? "" : " - " + visit.getState().label());
		}
		return new AdvancedTooltip(tooltipTitle.toString(), getContent(object));
	}

	protected void eventsMoved(List events)
	{
		for (Object evt : events)
		{
			GanttEvent event = (GanttEvent) evt;
			event.setStartDate(event.getActualStartDate());
			event.setEndDate(event.getActualEndDate());
			Visit visit = (Visit) event.getData();
			visit.setStart(event.getStartDate());
			visit.setEnd(event.getEndDate());
			event.setAdvancedTooltip(this.getTooltip(visit));
			VisitQuery query = (VisitQuery)connectionService.getQuery(Visit.class);
			event.setData(query.merge(visit));
		}
	}

	@Override
	protected void clear() 
	{
		// TODO Auto-generated method stub
		
	}
}
