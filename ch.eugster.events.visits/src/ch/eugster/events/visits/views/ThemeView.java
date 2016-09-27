package ch.eugster.events.visits.views;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.ganttchart.AdvancedTooltip;
import org.eclipse.nebula.widgets.ganttchart.GanttChart;
import org.eclipse.nebula.widgets.ganttchart.GanttEvent;
import org.eclipse.nebula.widgets.ganttchart.GanttSection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityListener;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.model.VisitTheme;
import ch.eugster.events.persistence.model.VisitVisitor;
import ch.eugster.events.persistence.queries.VisitQuery;
import ch.eugster.events.persistence.queries.VisitThemeQuery;

public class ThemeView extends AbstractGanttView<Visit>
{
	public static final String ID = "ch.eugster.events.visits.theme.view";

	private Map<Long, GanttSection> sections = new HashMap<Long, GanttSection>();
	
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
		this.themeListener = new EntityAdapter()
		{

			@Override
			public void postDelete(final AbstractEntity entity)
			{
				if (entity instanceof VisitTheme)
				{
					VisitTheme theme = (VisitTheme) entity;
					ThemeView.this.sections.remove(theme.getId());
				}
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				if (entity instanceof VisitTheme)
				{
					VisitTheme theme = (VisitTheme) entity;
					ThemeView.this.sections.put(theme.getId(), new GanttSection(ganttChart, theme.getName()));
				}
			}

			@Override
			public void postRemove(final AbstractEntity entity)
			{
				if (entity instanceof VisitTheme)
				{
					VisitTheme theme = (VisitTheme) entity;
					ThemeView.this.sections.remove(theme.getId());
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof VisitTheme)
				{
					VisitTheme theme = (VisitTheme) entity;
					GanttSection section = ThemeView.this.sections.get(theme.getId());
//					section.update(theme);
				}
			}
		};
		EntityMediator.addListener(VisitTheme.class, this.themeListener);
		this.visitListener = new EntityAdapter()
		{

			@Override
			public void postDelete(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
					if (visit.getTheme() != null)
					{
						GanttSection section = sections.get(visit.getTheme().getId());
//						GanttEvent event = section.removeGanttEvent(visit);
//						if (event != null)
//						{
//							ganttChart.redrawGanttChart();
//						}
					}
				}
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
//					if (visit.getTheme() != null)
//					{
//						GanttSection section = sections.get(visit.getTheme().getId());
//						section.getGroup().addEvent(visit);
//						ganttChart.redrawGanttChart();
//					}
				}
			}

			@Override
			public void postRemove(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
//					if (visit.getTheme() != null)
//					{
//						ThemeGanttSection section = sections.get(visit.getTheme().getId());
//						GanttEvent event = section.getGroup().removeEvent(visit);
//						if (event != null)
//						{
//							ganttChart.redrawGanttChart();
//						}
//					}
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
//					if (visit.getTheme() != null)
//					{
//						ThemeGanttSection section = sections.get(visit.getTheme().getId());
//						GanttEvent event = section.getGroup().getEvent(visit);
//						if (event != null)
//						{
////							event.update();
//							ganttChart.redrawGanttChart();
//						}
//					}
				}
			}
		};
		EntityMediator.addListener(Visit.class, this.visitListener);
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
		VisitThemeQuery themeQuery = (VisitThemeQuery) this.connectionService.getQuery(VisitTheme.class);
		List<VisitTheme> themes = themeQuery.selectAll(false);
		for (VisitTheme theme : themes)
		{
			GanttSection section = new GanttSection(ganttChart, theme.getName());
			List<Visit> visits = theme.getVisits(false);
			for (Visit visit : visits)
			{
				if (visit.getStart() != null && visit.getEnd() != null)
				{
					GanttEvent event = new GanttEvent(ganttChart, getLabelText(visit), visit.getStart(), visit.getEnd(), 0);
					section.addGanttEvent(event);
				}
			}
			this.sections.put(theme.getId(), section);
		}
	}

	private String getLabelText(final Visit visit)
	{
		if (!visit.getVisitors().isEmpty())
		{
			return visit.getVisitors().get(0).
		}
	}
	
	protected String getContent(final Visit visit)
	{
		/*
		 * Dates
		 */
		StringBuilder content = new StringBuilder(visit.getFormattedPeriod());
		if (content.length() > 0)
		{
			content = content.append("\n\n");
		}

		content = content.append("Thema: " + visit.getTheme().getName());
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
		if (visit.getSchoolClass() != null)
		{
			content = content.append("\n\n");
			content = content
					.append("Klasse: " + visit.getSchoolClass().getName())
					.append(Visit.stringValueOf(visit.getClassName()).isEmpty() ? " (" : ", " + visit.getClassName()
							+ " (")
					.append(NumberFormat.getIntegerInstance().format(visit.getPupils()) + " Schüler/innen)");
		}
		if (visit.getTeacher() != null)
		{
			content = content.append("\n\n");
			Address address = visit.getTeacher().getLink().getAddress();
			content = content
					.append("Schulhaus: ")
					.append(Address.stringValueOf(address.getName()).trim().isEmpty() ? "?, " : address.getName()
							+ ", ")
					.append("Stockwert: "
							+ (Visit.stringValueOf(visit.getFloor()).isEmpty() ? "?, " : visit.getFloor() + ", "))
					.append("Schulzimmer: "
							+ (Visit.stringValueOf(visit.getClassRoom()).trim().isEmpty() ? "?" : ", "
									+ visit.getClassRoom()));
			content = content.append("\n");
			content = content.append("Ort: " + AddressFormatter.getInstance().formatCityLine(address) + " ("
					+ address.getProvince() + ")");
			content = content.append("\n\n");
		}

		return content.toString();
	}

	protected AdvancedTooltip getTooltip(final Visit visit)
	{
		StringBuilder tooltipTitle = new StringBuilder(visit.getTheme() == null ? "" : visit.getTheme().getName());
		tooltipTitle = tooltipTitle.append(visit.getState() == null ? "" : " - " + visit.getState().label());
		return new AdvancedTooltip(tooltipTitle.toString(), getContent(visit));
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
			event.setAdvancedTooltip(AbstractGanttView.this.getTooltip(visit));
			VisitQuery query = (VisitQuery)connectionService.getQuery(Visit.class);
			event.setData(query.merge(visit));
		}
	}

	@Override
	protected void clear() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	protected <T> String getContent(T entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected <T> AdvancedTooltip getTooltip(T entity) {
		// TODO Auto-generated method stub
		return null;
	}
}
