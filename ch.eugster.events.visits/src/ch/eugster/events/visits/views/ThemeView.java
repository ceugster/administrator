package ch.eugster.events.visits.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.nebula.widgets.ganttchart.GanttChart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityListener;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.model.VisitTheme;
import ch.eugster.events.persistence.queries.VisitThemeQuery;

public class ThemeView extends AbstractGanttView
{
	public static final String ID = "ch.eugster.events.visits.theme.view";

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
					((ThemeRoot)ThemeView.this.root).removeGanttGroup((VisitTheme) entity);
				}
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				if (entity instanceof VisitTheme)
				{
					VisitTheme theme = (VisitTheme) entity;
					VisitGanttGroup group = new VisitThemeGanttGroup(theme, ganttChart);
					((ThemeRoot)ThemeView.this.root).addGanttGroup(group);
				}
			}

			@Override
			public void postRemove(final AbstractEntity entity)
			{
				if (entity instanceof VisitTheme)
				{
					((ThemeRoot)ThemeView.this.root).removeGanttGroup((VisitTheme) entity);
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof VisitTheme)
				{
					VisitTheme theme = (VisitTheme) entity;
					((ThemeRoot)ThemeView.this.root).updateGanttGroup(theme);
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
					VisitGanttGroup ganttGroup = ((ThemeRoot)ThemeView.this.root).getGanttGroup(visit.getTheme());
					VisitGanttEvent event = ganttGroup.removeEvent(visit);
					if (event != null)
					{
						root.getScope().removeScopeEvent(event);
						ganttChart.redrawGanttChart();
					}
				}
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
					VisitGanttGroup ganttGroup = ((ThemeRoot)ThemeView.this.root).getGanttGroup(visit.getTheme());
					VisitGanttEvent event = ganttGroup.addEvent(visit);
					if (event != null)
					{
						root.getScope().addScopeEvent(event);
						ganttChart.redrawGanttChart();
					}
				}
			}

			@Override
			public void postRemove(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
					VisitGanttGroup ganttGroup = ((ThemeRoot)ThemeView.this.root).getGanttGroup(visit.getTheme());
					VisitGanttEvent event = ganttGroup.removeEvent(visit);
					if (event != null)
					{
						root.getScope().removeScopeEvent(event);
						ganttChart.redrawGanttChart();
					}
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
					VisitGanttGroup ganttGroup = ((ThemeRoot)ThemeView.this.root).getGanttGroup(visit.getTheme());
					VisitGanttEvent event = ganttGroup.getEvent(visit);
					if (event != null)
					{
						event.update();
						ganttChart.redrawGanttChart();
					}
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
	protected void initializeRoot()
	{
		this.root = new ThemeRoot(ganttChart);
		VisitThemeQuery themeQuery = (VisitThemeQuery) this.connectionService.getQuery(VisitTheme.class);
		List<VisitTheme> themes = themeQuery.selectAll(false);
		for (VisitTheme theme : themes)
		{
			VisitGanttGroup group = new VisitThemeGanttGroup(theme, ganttChart);
			this.root.addGanttGroup(group);
		}
	}

	protected void clearRoot()
	{
		Set<VisitTheme> themes = ((ThemeRoot) this.root).getThemes();
		for (VisitTheme theme : themes)
		{
			((ThemeRoot) this.root).removeGanttGroup(theme);
		}
	}
	
	private class ThemeRoot extends Root
	{
		private Map<VisitTheme, VisitGanttGroup> themeGroups = new HashMap<VisitTheme, VisitGanttGroup>();
		
		public ThemeRoot(GanttChart ganttChart)
		{
			super("Themen", ganttChart);
		}
		
		public Set<VisitTheme> getThemes()
		{
			return themeGroups.keySet();
		}
		
		public void addGanttGroup(VisitGanttGroup ganttGroup)
		{
			themeGroups.put(ganttGroup.getTheme(), ganttGroup);
			List<Visit> visits = ganttGroup.getTheme().getVisits(false);
			for (Visit visit : visits)
			{
				VisitGanttEvent event = new VisitThemeGanttEvent(visit, ganttChart);
				ganttGroup.addEvent(event);
				this.getScope().addScopeEvent(event);
			}
			this.getScope().getParentChart().redrawGanttChart();
		}

		public VisitGanttGroup getGanttGroup(VisitTheme theme)
		{
			return this.themeGroups.get(theme);
		}

		public void updateGanttGroup(VisitTheme theme)
		{
			this.themeGroups.get(theme).update(theme);
			this.getScope().getParentChart().redrawGanttChart();
		}

		public void removeGanttGroup(VisitTheme theme)
		{
			VisitGanttGroup themeGroup = this.themeGroups.remove(theme);
			@SuppressWarnings("unchecked")
			List<VisitGanttEvent> events = themeGroup.getEventMembers();
			for (VisitGanttEvent event : events)
			{
				this.getScope().removeScopeEvent(event);
			}
			themeGroup.dispose();
			this.getScope().getParentChart().redrawGanttChart();
		}
	}
	
	private class VisitThemeGanttGroup extends VisitGanttGroup
	{
		public VisitThemeGanttGroup(VisitTheme theme, GanttChart parent) 
		{
			super(theme, parent);
		}

		@Override
		public VisitGanttEvent createVisitGanttEvent(Visit visit,
				GanttChart ganttChart) 
		{
			return new VisitThemeGanttEvent(visit, ganttChart);
		}
	}

	private class VisitThemeGanttEvent extends VisitGanttEvent
	{
		public VisitThemeGanttEvent(Visit visit, GanttChart ganttChart) 
		{
			super(visit, ganttChart);
		}

		@Override
		public Color getColor() 
		{
			java.awt.Color c = new java.awt.Color(this.getVisit().getTheme().getColor().intValue());
			return new Color(this.getParentChart().getDisplay(), new RGB(c.getRed(), c.getGreen(), c.getBlue()));
		}
	}
}
