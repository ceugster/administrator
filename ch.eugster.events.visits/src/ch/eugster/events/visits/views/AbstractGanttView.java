package ch.eugster.events.visits.views;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.model.VisitVisitor;
import ch.eugster.events.persistence.queries.VisitQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.visits.Activator;
import ch.eugster.events.visits.editors.VisitEditor;
import ch.eugster.events.visits.editors.VisitEditorInput;

public abstract class AbstractGanttView<T extends AbstractEntity> extends ViewPart implements IViewPart, IGanttEventListener
{
	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	protected ConnectionService connectionService;

	protected GanttChart ganttChart;

	protected IDialogSettings settings;

	public AbstractGanttView()
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
		ganttComposite.setMenu(this.extendMenu(ganttChart));
		ganttComposite.setView(settings.getInt("view"));
		ganttComposite.setZoomLevel(settings.getInt("zoom"));
		ganttComposite.setDate(GregorianCalendar.getInstance(), SWT.CENTER);
		
//		if (PlatformUI.getWorkbench().getDisplay().getActiveShell().getFont() != null)
//		{
//			ganttChart.setFont(PlatformUI.getWorkbench().getDisplay().getActiveShell().getFont());
//		}

		ganttChart.addGanttEventListener(this);

		this.connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		this.connectionServiceTracker.open();
		this.connectionService = (ConnectionService) this.connectionServiceTracker.getService();
		this.initialize();
		
		registerEntityListeners();
	}

	protected abstract void initialize();
	
	protected abstract void clear();
	
	@Override
	public void eventDoubleClicked(final GanttEvent event, final MouseEvent mouseEvent)
	{
		if (event.getData() instanceof Visit)
		{
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
//			GanttEvent event = (GanttEvent) evt;
//			event.setStartDate(event.getActualStartDate());
//			event.setEndDate(event.getActualEndDate());
//			Visit visit = (Visit) event.getData();
//			visit.setStart(event.getStartDate());
//			visit.setEnd(event.getEndDate());
//			event.setAdvancedTooltip(getTooltip(visit));
//			VisitQuery query = (VisitQuery)connectionService.getQuery(Visit.class);
//			event.setData(query.merge(visit));
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

	protected abstract Menu extendMenu(final GanttChart ganttChart);

	@Override
	public void dispose()
	{
		if (connectionServiceTracker != null)
		{
			connectionServiceTracker.close();
		}
		super.dispose();
	}

	protected abstract <T> String getContent(final T entity);

	protected abstract <T> AdvancedTooltip getTooltip(final T entity);

	protected abstract void eventsMoved(final List events);
	
	protected abstract void registerEntityListeners();

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
	
//	protected abstract class Root
//	{
//		private GanttEvent scope;
//	
//		public Root(String name, GanttChart ganttChart)
//		{
//			this.scope = new GanttEvent(ganttChart, name);
//			this.scope.setVerticalEventAlignment(SWT.CENTER);
//			this.scope.setStartDate(GregorianCalendar.getInstance());
//			this.scope.setEndDate(GregorianCalendar.getInstance());
//			this.scope.setData(this);
//		}
//
//		public GanttEvent getScope()
//		{
//			return this.scope;
//		}
//
//		public abstract void addGanttGroup(VisitGanttGroup group);
//	}

//	public class VisitGanttGroup extends GanttGroup
//	{
//		public VisitGanttGroup(GanttChart parent) 
//		{
//			super(parent);
//		}
//		
//		private GanttEvent createVisitGanttEvent(Visit visit, GanttChart ganttChart) 
//		{
//			GanttEvent event = new GanttEvent(ganttChart, SimpleDateFormat.getDateInstance().format(visit.getStart().getTime()));
//			event.setStartDate(visit.getStart());
//			event.setEndDate(visit.getEnd());
//			return event;
//		}
//		
//		public GanttEvent addEvent(Visit visit)
//		{
//			GanttEvent event = createVisitGanttEvent(visit, ganttChart);
//			this.addEvent(event);
//			return event;
//		}
//
//		public GanttEvent getEvent(Visit visit)
//		{
//			@SuppressWarnings("unchecked")
//			List<GanttEvent> events = this.getEventMembers();
//			for (GanttEvent event : events)
//			{
////				if (event.getVisit().getId().equals(visit.getId()))
////				{
//					return event;
////				}
//			}
//			return null;
//		}
//
//		public GanttEvent removeEvent(Visit visit)
//		{
//			@SuppressWarnings("unchecked")
//			List<GanttEvent> events = this.getEventMembers();
//			for (GanttEvent event : events)
//			{
////				if (event.getVisit().getId().equals(visit.getId()))
////				{
//					this.removeEvent(event);
//					return event;
////				}
//			}
//			return null;
//		}
//	}
	
//	public class VisitGanttEvent extends GanttEvent
//	{
//		public VisitGanttEvent(Visit visit, GanttChart ganttChart) 
//		{
//			super(ganttChart, visit.getTheme().getName());
//			this.setData(visit);
//			this.update();
//		}
//		
//		public Visit getVisit()
//		{
//			return (Visit) this.getData();
//		}
//
//		public Color getColor() 
//		{
//			java.awt.Color c = new java.awt.Color(this.getVisit().getTheme().getColor().intValue());
//			return new Color(this.getParentChart().getDisplay(), new RGB(c.getRed(), c.getGreen(), c.getBlue()));
//		}
//
//		public void update()
//		{
////			int fontStyle = Font.BOLD;
//			Visit visit = getVisit();
////			if (visit.getState() != null)
////			{
////				if (visit.getState().equals(Visit.State.PROVISORILY))
////				{
////					fontStyle = Font.ITALIC;
////				}
////			}
////			if (this.getTextFont() == null)
////			{
////				this.setTextFont(ganttChart.getFont());
////			}
////			this.getTextFont().getFontData()[0].setStyle(fontStyle);
//			this.setStartDate(visit.getStart());
//			this.setRevisedStart(visit.getStart());
//			this.setEndDate(visit.getEnd());
//			this.setRevisedEnd(visit.getEnd());
//			this.setStatusColor(ColorCache.getColor(255, 104, 145));
//			this.setGradientStatusColor(ColorCache.getColor(168, 185, 216));
////			this.setAutomaticRowHeight();
////			this.setStatusColor(ColorCache.getColor(81, 104, 145));
////			this.setGradientStatusColor(ColorCache.getColor(81, 104, 145));
//			this.setAdvancedTooltip(getTooltip(this.getVisit()));
//		}
//	}
//
}
