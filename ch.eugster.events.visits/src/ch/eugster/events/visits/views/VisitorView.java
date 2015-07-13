package ch.eugster.events.visits.views;

import java.awt.Font;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.nebula.widgets.ganttchart.AdvancedTooltip;
import org.eclipse.nebula.widgets.ganttchart.GanttChart;
import org.eclipse.nebula.widgets.ganttchart.GanttComposite;
import org.eclipse.nebula.widgets.ganttchart.GanttEvent;
import org.eclipse.nebula.widgets.ganttchart.GanttGroup;
import org.eclipse.nebula.widgets.ganttchart.GanttPhase;
import org.eclipse.nebula.widgets.ganttchart.GanttSection;
import org.eclipse.nebula.widgets.ganttchart.GanttSpecialDateRange;
import org.eclipse.nebula.widgets.ganttchart.IGanttEventListener;
import org.eclipse.nebula.widgets.ganttchart.ISettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.util.tracker.ServiceTracker;

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
import ch.eugster.events.persistence.model.Visitor;
import ch.eugster.events.persistence.queries.VisitQuery;
import ch.eugster.events.persistence.queries.VisitThemeQuery;
import ch.eugster.events.persistence.queries.VisitorQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.visits.Activator;
import ch.eugster.events.visits.editors.VisitEditor;
import ch.eugster.events.visits.editors.VisitEditorInput;

public class VisitorView extends ViewPart implements IViewPart
{
	public static final String ID = "ch.eugster.events.visits.overview";

	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	private ThemeRoot themeRoot;
	
	private VisitorRoot visitorRoot;
	
	private GanttChart ganttChart;

	private IDialogSettings settings;

	private EntityListener themeListener;
	
	private EntityListener visitorListener;
	
	private EntityListener visitListener;

	public VisitorView()
	{
		settings = Activator.getDefault().getDialogSettings().getSection("gantt.view");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("gantt.view");
		}
		if (settings.get("zoom") == null)
		{
			settings.put("zoom", 3);
		}
		if (settings.get("view") == null)
		{
			settings.put("view", ISettings.VIEW_WEEK);
		}
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);
		this.connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		this.connectionServiceTracker.open();
		this.connectionService = (ConnectionService) this.connectionServiceTracker.getService();
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		final int oneRowHeight = 28;
		final int spacer = 6;

		parent.setLayout(new FillLayout());

		ganttChart = new GanttChart(parent, SWT.None, new VisitSettings());

		final GanttComposite ganttComposite = ganttChart.getGanttComposite();
		ganttComposite.setFixedRowHeightOverride(oneRowHeight - spacer);
		ganttComposite.setEventSpacerOverride(spacer);
		ganttComposite.setMenu(this.createMenu(ganttChart));
		ganttComposite.setView(settings.getInt("view"));
		ganttComposite.setZoomLevel(settings.getInt("zoom"));
		ganttComposite.setDate(GregorianCalendar.getInstance(), SWT.CENTER);
		
		if (PlatformUI.getWorkbench().getDisplay().getActiveShell().getFont() != null)
		{
			ganttChart.setFont(PlatformUI.getWorkbench().getDisplay().getActiveShell().getFont());
		}

		ganttChart.addGanttEventListener(new IGanttEventListener()
		{

			@Override
			public void eventDoubleClicked(final GanttEvent event, final MouseEvent mouseEvent)
			{
//				if (event.equals(themeRoot.getScope()) || event.equals(visitorRoot.getScope()))
//				{
//					return;
//				}
				Visit visit = (Visit) event.getData();
				try
				{
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.openEditor(new VisitEditorInput(visit), VisitEditor.ID);
				}
				catch (PartInitException e)
				{
					e.printStackTrace();
				}
			}

			@Override
			public void eventHeaderSelected(final Calendar newlySelectedDate, final List allSelectedDates)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void eventMovedToNewSection(final GanttEvent event, final GanttSection oldSection,
					final GanttSection newSection)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void eventPropertiesSelected(final List events)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void eventReordered(final GanttEvent event)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void eventsDeleteRequest(final List events, final MouseEvent mouseEvent)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void eventsDroppedOrResizedOntoUnallowedDateRange(final List events,
					final GanttSpecialDateRange range)
			{
				// TODO Auto-generated method stub
				System.out.println();
			}

			@Override
			public void eventSelected(final GanttEvent event, final List allSelectedEvents, final MouseEvent mouseEvent)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void eventsMoved(final List events, final MouseEvent mouseEvent)
			{
				for (Object evt : events)
				{
					GanttEvent event = (GanttEvent) evt;
					event.setStartDate(event.getActualStartDate());
					event.setEndDate(event.getActualEndDate());
					Visit visit = (Visit) event.getData();
					visit.setStart(event.getStartDate());
					visit.setEnd(event.getEndDate());
					event.setAdvancedTooltip(VisitorView.this.getTooltip(visit));
					VisitQuery query = (VisitQuery)connectionService.getQuery(Visit.class);
					event.setData(query.merge(visit));
				}
			}

			@Override
			public void eventsMoveFinished(final List events, final MouseEvent mouseEvent)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void eventsResized(final List events, final MouseEvent mouseEvent)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void eventsResizeFinished(final List events, final MouseEvent mouseEvent)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void lastDraw(final GC gc)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void phaseMoved(final GanttPhase phase, final MouseEvent mouseEvent)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void phaseMoveFinished(final GanttPhase phase, final MouseEvent mouseEvent)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void phaseResized(final GanttPhase phase, final MouseEvent mouseEvent)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void phaseResizeFinished(final GanttPhase phase, final MouseEvent mouseEvent)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void zoomedIn(final int newZoomLevel)
			{
			}

			@Override
			public void zoomedOut(final int newZoomLevel)
			{
			}

			@Override
			public void zoomReset()
			{
				// TODO Auto-generated method stub

			}
		});
		
		themeRoot = new ThemeRoot(ganttChart);
		VisitThemeQuery themeQuery = (VisitThemeQuery) this.connectionService.getQuery(VisitTheme.class);
		addThemes(themeQuery.selectAll(false));

		visitorRoot = new VisitorRoot(ganttChart);
		VisitorQuery visitorQuery = (VisitorQuery) this.connectionService.getQuery(Visitor.class);
		addVisitors(visitorQuery.selectAll(false));

		registerEntityListeners();
	}

	private Menu createMenu(final GanttChart ganttChart)
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
		EntityMediator.removeListener(Visit.class, this.visitorListener);
		EntityMediator.removeListener(Visit.class, this.visitListener);
		if (connectionServiceTracker != null)
		{
			connectionServiceTracker.close();
		}
		super.dispose();
	}

	private Color getColor(final VisitTheme visitTheme)
	{
		if (visitTheme == null || visitTheme.getColor() == null)
		{
			return null;
		}
		java.awt.Color c = new java.awt.Color(visitTheme.getColor().intValue());
		return new Color(ganttChart.getDisplay(), new RGB(c.getRed(), c.getGreen(), c.getBlue()));
	}

	private String getContent(final Visit visit)
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

	private AdvancedTooltip getTooltip(final Visit visit)
	{
		StringBuilder tooltipTitle = new StringBuilder(visit.getTheme() == null ? "" : visit.getTheme().getName());
		tooltipTitle = tooltipTitle.append(visit.getState() == null ? "" : " - " + visit.getState().label());
		return new AdvancedTooltip(tooltipTitle.toString(), getContent(visit));
	}

	private void addThemes(final List<VisitTheme> themes)
	{
		for (VisitTheme theme : themes)
		{
			themeRoot.addChild(theme);
		}
	}

	private void addVisitors(final List<Visitor> visitors)
	{
		for (Visitor visitor : visitors)
		{
			visitorRoot.addChild(visitor);
		}
	}

	private void registerEntityListeners()
	{
		this.themeListener = new EntityAdapter()
		{

			@Override
			public void postDelete(final AbstractEntity entity)
			{
				themeRoot.removeChild((VisitTheme) entity);
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				themeRoot.addChild((VisitTheme)entity);
			}

			@Override
			public void postRemove(final AbstractEntity entity)
			{
				themeRoot.removeChild((VisitTheme) entity);
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				themeRoot.updateChild((VisitTheme) entity);
			}
		};
		EntityMediator.addListener(VisitTheme.class, this.themeListener);
		this.visitorListener = new EntityAdapter()
		{

			@Override
			public void postDelete(final AbstractEntity entity)
			{
				visitorRoot.removeChild((Visitor) entity);
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				visitorRoot.addChild((Visitor) entity);
			}

			@Override
			public void postRemove(final AbstractEntity entity)
			{
				visitorRoot.removeChild((Visitor) entity);
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				visitorRoot.updateChild((Visitor) entity);
			}
		};
		EntityMediator.addListener(Visitor.class, this.visitorListener);
		this.visitListener = new EntityAdapter()
		{

			@Override
			public void postDelete(final AbstractEntity entity)
			{
				Visit visit = (Visit) entity;
				ThemeItem themeItem = themeRoot.getChild(visit.getTheme());
				themeItem.removeVisit(visit);
				List<VisitVisitor> visitors = visit.getVisitors();
				for (VisitVisitor visitor : visitors)
				{
					VisitorItem visitorItem = visitorRoot.getChild(visitor.getVisitor());
					visitorItem.removeVisit(visit);
				}
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				Visit visit = (Visit) entity;
				ThemeItem themeItem = themeRoot.getChild(visit.getTheme());
				themeItem.addVisit(visit);
				List<VisitVisitor> visitors = visit.getVisitors();
				for (VisitVisitor visitor : visitors)
				{
					VisitorItem visitorItem = visitorRoot.getChild(visitor.getVisitor());
					visitorItem.addVisit(visit);
				}
			}

			@Override
			public void postRemove(final AbstractEntity entity)
			{
				Visit visit = (Visit) entity;
				ThemeItem themeItem = themeRoot.getChild(visit.getTheme());
				themeItem.removeVisit(visit);
				List<VisitVisitor> visitors = visit.getVisitors();
				for (VisitVisitor visitor : visitors)
				{
					VisitorItem visitorItem = visitorRoot.getChild(visitor.getVisitor());
					visitorItem.removeVisit(visit);
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				Visit visit = (Visit) entity;
				ThemeItem themeItem = themeRoot.getChild(visit.getTheme());
				themeItem.getVisit(visit).updateEvent();
				List<VisitVisitor> visitors = visit.getVisitors();
				for (VisitVisitor visitor : visitors)
				{
					VisitorItem visitorItem = visitorRoot.getChild(visitor.getVisitor());
					visitorItem.getVisit(visit).updateEvent();
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

	public void setTodayCentered()
	{
		ganttChart.getGanttComposite().setDate(GregorianCalendar.getInstance(), SWT.CENTER);
		ganttChart.getGanttComposite().setFocus();
	}

	public void zoomIn()
	{
		int zoomLevel = settings.getInt("zoom");
		if (zoomLevel > ISettings.MIN_ZOOM_LEVEL)
		{
			settings.put("zoom", --zoomLevel);
		}
		ganttChart.getGanttComposite().zoomIn();
		settings.put("view", ganttChart.getGanttComposite().getCurrentView());
	}

	public void zoomOut()
	{
		int zoomLevel = settings.getInt("zoom");
		if (zoomLevel < ISettings.MAX_ZOOM_LEVEL)
		{
			settings.put("zoom", ++zoomLevel);
		}
		ganttChart.getGanttComposite().zoomOut();
		settings.put("view", ganttChart.getGanttComposite().getCurrentView());
	}
	
	private abstract class Root
	{
		private GanttEvent scope;
	
		public Root(String name, GanttChart ganttChart)
		{
			this.scope = new GanttEvent(ganttChart, name);
			this.scope.setVerticalEventAlignment(SWT.CENTER);
			this.scope.setStartDate(GregorianCalendar.getInstance());
			this.scope.setEndDate(GregorianCalendar.getInstance());
			this.scope.setData(this);
			
//			GanttSection section = new GanttSection(ganttChart, name);
//			section.addGanttEvent(this.scope);
		}

		public GanttEvent getScope()
		{
			return this.scope;
		}
//		public abstract int getLayer();
	}
	
	private class ThemeRoot extends Root
	{
		private Map<VisitTheme, ThemeItem> children = new HashMap<VisitTheme, ThemeItem>();
		
		public ThemeRoot(GanttChart ganttChart)
		{
			super("Themen", ganttChart);
		}
		
		public Map<VisitTheme, ThemeItem> getChildren()
		{
			return this.children;
		}

		public void addChild(VisitTheme theme)
		{
			ThemeItem child = new ThemeItem(theme);
			children.put(theme, child);
			@SuppressWarnings("unchecked")
			List<GanttEvent> members = child.getGanttGroup().getEventMembers();
			for (GanttEvent member : members)
			{
//				member.setLayer(Layer.THEME.ordinal());
				this.getScope().addScopeEvent(member);
			}
		}

		public ThemeItem getChild(VisitTheme theme)
		{
			return this.children.get(theme);
		}

		public void updateChild(VisitTheme theme)
		{
			ThemeItem item  = this.children.get(theme);
			item.update(theme);
		}

		public void removeChild(VisitTheme theme)
		{
			ThemeItem item  = this.children.remove(theme);
			@SuppressWarnings("unchecked")
			List<GanttEvent> members = item.getGanttGroup().getEventMembers();
			for (GanttEvent member : members)
			{
				this.getScope().removeScopeEvent(member);
			}
			item.getGanttGroup().dispose();
			ganttChart.redrawGanttChart();
		}
		
//		public int getLayer()
//		{
//			return Layer.THEME.ordinal();
//		}
	}
	
	private class VisitorRoot extends Root
	{
		private Map<Visitor, VisitorItem> children = new HashMap<Visitor, VisitorItem>();
		
		public VisitorRoot(GanttChart ganttChart)
		{
			super("Besucher", ganttChart);
		}
		
		public Map<Visitor, VisitorItem> getChildren()
		{
			return this.children;
		}

		public void addChild(Visitor visitor)
		{
			VisitorItem child = new VisitorItem(visitor);
			children.put(visitor, child);
			@SuppressWarnings("unchecked")
			List<GanttEvent> members = child.getGanttGroup().getEventMembers();
			for (GanttEvent member : members)
			{
//				member.setLayer(Layer.VISITOR.ordinal());
				this.getScope().addScopeEvent(member);
			}
		}

		public VisitorItem getChild(Visitor visitor)
		{
			return this.children.get(visitor);
		}

		public void updateChild(Visitor visitor)
		{
			VisitorItem item  = this.children.get(visitor);
			item.update(visitor);
		}

		public void removeChild(Visitor visitor)
		{
			VisitorItem item  = this.children.remove(visitor);
			@SuppressWarnings("unchecked")
			List<GanttEvent> members = item.getGanttGroup().getEventMembers();
			for (GanttEvent member : members)
			{
				this.getScope().removeScopeEvent(member);
			}
			item.getGanttGroup().dispose();
			ganttChart.redrawGanttChart();
		}
		
//		public int getLayer()
//		{
//			return Layer.VISITOR.ordinal();
//		}
	}
	
	private abstract class Item
	{
		protected GanttGroup ganttGroup;
		
		private Map<Long, VisitItem> visits = new HashMap<Long, VisitItem>();
		
		public GanttGroup getGanttGroup()
		{
			return this.ganttGroup;
		}

		public void addVisit(Visit visit)
		{
			this.visits.put(visit.getId(), new VisitItem(visit, this));
		}

		public VisitItem getVisit(Visit visit)
		{
			return this.visits.get(visit.getId());
		}
		
		public void removeVisit(Visit visit)
		{
			VisitItem item = this.visits.remove(visit.getId());
			this.ganttGroup.removeEvent(item.getGanttEvent());
			item.getGanttEvent().dispose();
		}
		
		public abstract String getVisitText(Visit visit);
	}
	
	private class ThemeItem extends Item
	{
		public ThemeItem(VisitTheme theme)
		{
			this.ganttGroup = new GanttGroup(ganttChart);
			update(theme);
			List<Visit> visits = theme.getVisits(false);
			for (Visit visit : visits)
			{
				this.addVisit(visit);
			}
		}
		
		public void update(VisitTheme theme)
		{
		}

		@Override
		public String getVisitText(Visit visit) 
		{
			StringBuilder name = new StringBuilder();
			List<VisitVisitor> visitors = visit.getVisitors();
			for (VisitVisitor visitor : visitors)
			{
				if (name.length() == 0)
				{
					name = name.append(PersonFormatter.getInstance().formatLastnameFirstnameInitial(visitor.getVisitor().getLink().getPerson()));
				}
				else
				{
					name = name.append(", " + PersonFormatter.getInstance().formatLastnameFirstnameInitial(visitor.getVisitor().getLink().getPerson()));
				}
			}
			return name.toString();
		}
	}
	
	private class VisitorItem extends Item
	{
		public VisitorItem(Visitor visitor)
		{
			ganttGroup = new GanttGroup(ganttChart);
			update(visitor);
			List<VisitVisitor> visits = visitor.getVisitorVisits(false);
			for (VisitVisitor visit : visits)
			{
				this.addVisit(visit.getVisit());
			}
		}

		public void update(Visitor visitor)
		{
		}

		@Override
		public String getVisitText(Visit visit)
		{
			return visit.getTheme().getName();
		}
	}

	private class VisitItem
	{
		private Item item;
		
		private Visit visit;
		
		private GanttEvent event;
		
		public VisitItem(Visit visit, Item item)
		{
			this.visit = visit;
			this.item = item;
			int percent = 0;
			Calendar now = GregorianCalendar.getInstance();
			if (visit.getEnd() != null && visit.getEnd().before(now))
			{
				percent = 100;
			}
			else if (visit.getStart() != null && visit.getStart().after(now))
			{
				percent = 0;
			}
			else if (visit.getEnd() != null && visit.getStart() != null)
			{
				percent = (int) (1 / visit.getEnd().getTimeInMillis() * visit.getEnd().compareTo(visit.getStart()));
			}
			this.event = new GanttEvent(ganttChart, this, item.getVisitText(visit), visit.getStart(), visit.getEnd(), percent);
			item.getGanttGroup().addEvent(event);
			update();
		}
		
		public Visit getVisit()
		{
			return this.visit;
		}
		
		public GanttEvent getGanttEvent()
		{
			return this.event;
		}

		public void updateEvent()
		{
			update();
			this.event.update(true);
		}
		
		private void update()
		{
			int fontStyle = Font.BOLD;
			if (this.visit.getState() != null)
			{
				if (this.visit.getState().equals(Visit.State.PROVISORILY))
				{
					fontStyle = Font.ITALIC;
				}
			}
			if (this.event.getTextFont() == null)
			{
				this.event.setTextFont(ganttChart.getFont());
			}
			this.event.getTextFont().getFontData()[0].setStyle(fontStyle);
			this.event.setStartDate(visit.getStart());
			this.event.setRevisedStart(visit.getStart());
			this.event.setEndDate(visit.getEnd());
			this.event.setRevisedEnd(visit.getEnd());
			this.event.setData(visit);
			this.event.setAutomaticRowHeight();
			Color newColor = getColor(visit.getTheme());
			Color oldColor = this.event.getGradientStatusColor();
			if (oldColor != null && (newColor == null || !this.event.getGradientStatusColor().equals(newColor)))
			{
				this.event.getGradientStatusColor().dispose();
			}
			if (newColor != null)
			{
				this.event.setGradientStatusColor(newColor);
			}
			this.event.setAdvancedTooltip(getTooltip(visit));
		}
	}

//	private enum Layer
//	{
//		THEME, VISITOR;
//	}
//	private class MyGanttHeaderSpacedLayout extends GanttHeaderSpacedLayout
//	{
//		
//	}
}
