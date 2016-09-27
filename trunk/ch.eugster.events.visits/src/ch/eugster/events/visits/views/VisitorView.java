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
import ch.eugster.events.persistence.model.VisitVisitor;
import ch.eugster.events.persistence.queries.VisitThemeQuery;

public class VisitorView extends AbstractGanttView
{
	public static final String ID = "ch.eugster.events.visits.visitor.view";

	private EntityListener visitorListener;
	
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
		EntityMediator.removeListener(Visit.class, this.visitorListener);
		EntityMediator.removeListener(Visit.class, this.visitListener);
		super.dispose();
	}

	protected void registerEntityListeners()
	{
		this.visitorListener = new EntityAdapter()
		{

			@Override
			public void postDelete(final AbstractEntity entity)
			{
				if (entity instanceof VisitVisitor)
				{
					((VisitorRoot)VisitorView.this.root).removeGanttGroup((VisitVisitor) entity);
				}
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				if (entity instanceof VisitVisitor)
				{
					VisitVisitor visitor = (VisitVisitor) entity;
					VisitGanttGroup group = new VisitVisitorGanttGroup(visitor, ganttChart);
					((VisitorRoot)VisitorView.this.root).addGanttGroup(group);
				}
			}

			@Override
			public void postRemove(final AbstractEntity entity)
			{
				if (entity instanceof VisitVisitor)
				{
					((VisitorRoot)VisitorView.this.root).removeGanttGroup((VisitVisitor) entity);
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof VisitVisitor)
				{
					VisitVisitor visitor = (VisitVisitor) entity;
					((VisitorRoot)VisitorView.this.root).updateGanttGroup(visitor);
				}
			}
		};
		EntityMediator.addListener(VisitVisitor.class, this.visitorListener);
		this.visitListener = new EntityAdapter()
		{

			@Override
			public void postDelete(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
//					VisitGanttGroup ganttGroup = ((VisitorRoot)VisitorView.this.root).getGanttGroup(visit.getvi.getVisitor());
//					VisitGanttEvent event = ganttGroup.removeEvent(visit);
//					if (event != null)
//					{
//						root.getScope().removeScopeEvent(event);
//						ganttChart.redrawGanttChart();
//					}
				}
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
//					VisitGanttGroup ganttGroup = ((VisitorRoot)VisitorView.this.root).getGanttGroup(visit.getTheme());
//					VisitGanttEvent event = ganttGroup.addEvent(visit);
//					if (event != null)
//					{
//						root.getScope().addScopeEvent(event);
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
//					VisitGanttGroup ganttGroup = ((VisitorRoot)VisitorView.this.root).getGanttGroup(visit.getTheme());
//					VisitGanttEvent event = ganttGroup.removeEvent(visit);
//					if (event != null)
//					{
//						root.getScope().removeScopeEvent(event);
//						ganttChart.redrawGanttChart();
//					}
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
//					VisitGanttGroup ganttGroup = ((VisitorRoot)VisitorView.this.root).getGanttGroup(visit.getTheme());
//					VisitGanttEvent event = ganttGroup.getEvent(visit);
//					if (event != null)
//					{
//						event.update();
//						ganttChart.redrawGanttChart();
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
	protected void initializeRoot()
	{
		this.root = new VisitorRoot(ganttChart);
		VisitThemeQuery themeQuery = (VisitThemeQuery) this.connectionService.getQuery(VisitVisitor.class);
		List<VisitTheme> themes = themeQuery.selectVisibles();
//		for (VisitTheme theme : themes)
//		{
//			VisitGanttGroup group = new VisitGanttGroup(theme, ganttChart);
//			this.root.addGanttGroup(group);
//		}
	}

	protected void clearRoot()
	{
		Set<VisitVisitor> themes = ((VisitorRoot) this.root).getThemes();
		for (VisitVisitor theme : themes)
		{
			((VisitorRoot) this.root).removeGanttGroup(theme);
		}
	}
	
	private class VisitorRoot extends Root
	{
		private Map<VisitVisitor, VisitGanttGroup> visitorGroups = new HashMap<VisitVisitor, VisitGanttGroup>();
		
		public VisitorRoot(GanttChart ganttChart)
		{
			super("Themen", ganttChart);
		}
		
		public Set<VisitVisitor> getThemes()
		{
			return visitorGroups.keySet();
		}
		
		public void addGanttGroup(VisitGanttGroup ganttGroup)
		{
//			visitorGroups.put(ganttGroup.getVisitor(), ganttGroup);
//			List<Visit> visits = ganttGroup.getVisitors().getVisits(false);
//			for (Visit visit : visits)
//			{
//				VisitGanttEvent event = new VisitThemeGanttEvent(visit, ganttChart);
//				ganttGroup.addEvent(event);
//				this.getScope().addScopeEvent(event);
//			}
			this.getScope().getParentChart().redrawGanttChart();
		}

		public VisitGanttGroup getGanttGroup(VisitVisitor visitor)
		{
			return this.visitorGroups.get(visitor);
		}

		public void updateGanttGroup(VisitVisitor theme)
		{
//			this.visitorGroups.get(theme).update(theme);
			this.getScope().getParentChart().redrawGanttChart();
		}

		public void removeGanttGroup(VisitVisitor theme)
		{
			VisitGanttGroup themeGroup = this.visitorGroups.remove(theme);
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
	
	private class VisitVisitorGanttGroup extends VisitGanttGroup
	{
		private VisitVisitor visitor;
		
		public VisitVisitorGanttGroup(VisitVisitor visitor, GanttChart parent) 
		{
			super(parent);
			this.visitor = visitor;
		}

		public VisitVisitor getVisitor()
		{
			return this.visitor;
		}
		
		public void update(VisitVisitor visitor)
		{
			@SuppressWarnings("unchecked")
			List<VisitGanttEvent> events = this.getEventMembers();
			for (VisitGanttEvent event : events)
			{
				event.getVisit().addVisitor(visitor);
				event.update(true);
			}
			ganttChart.redrawGanttChart();
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
