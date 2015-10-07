package ch.eugster.events.visits.views;

import java.awt.Font;
import java.text.NumberFormat;
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
import org.eclipse.swt.graphics.Color;
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
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.model.VisitVisitor;
import ch.eugster.events.persistence.queries.VisitQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.visits.Activator;
import ch.eugster.events.visits.editors.VisitEditor;
import ch.eugster.events.visits.editors.VisitEditorInput;

public abstract class AbstractGanttView extends ViewPart implements IViewPart, IGanttEventListener
{
	private ServiceTracker connectionServiceTracker;

	protected ConnectionService connectionService;

	protected Root root;
	
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
		
		if (PlatformUI.getWorkbench().getDisplay().getActiveShell().getFont() != null)
		{
			ganttChart.setFont(PlatformUI.getWorkbench().getDisplay().getActiveShell().getFont());
		}

		ganttChart.addGanttEventListener(this);

		this.connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		this.connectionServiceTracker.open();
		this.connectionService = (ConnectionService) this.connectionServiceTracker.getService();
		this.initializeRoot();
		
		registerEntityListeners();
	}

	protected abstract void initializeRoot();
	
	protected abstract void clearRoot();
	
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
	
	protected abstract class Root
	{
		private GanttEvent scope;
	
		public Root(String name, GanttChart ganttChart)
		{
			this.scope = new GanttEvent(ganttChart, name);
			this.scope.setVerticalEventAlignment(SWT.CENTER);
			this.scope.setStartDate(GregorianCalendar.getInstance());
			this.scope.setEndDate(GregorianCalendar.getInstance());
			this.scope.setData(this);
		}

		public GanttEvent getScope()
		{
			return this.scope;
		}

		public abstract void addGanttGroup(VisitGanttGroup group);
	}

	public abstract class VisitGanttGroup extends GanttGroup
	{
		public VisitGanttGroup(GanttChart parent) 
		{
			super(parent);
		}
		
		public abstract VisitGanttEvent createVisitGanttEvent(Visit visit, GanttChart ganttChart);
		
		public VisitGanttEvent addEvent(Visit visit)
		{
			VisitGanttEvent event = createVisitGanttEvent(visit, ganttChart);
			this.addEvent(event);
			return event;
		}

		public VisitGanttEvent getEvent(Visit visit)
		{
			@SuppressWarnings("unchecked")
			List<VisitGanttEvent> events = this.getEventMembers();
			for (VisitGanttEvent event : events)
			{
				if (event.getVisit().getId().equals(visit.getId()))
				{
					return event;
				}
			}
			return null;
		}

		public VisitGanttEvent removeEvent(Visit visit)
		{
			@SuppressWarnings("unchecked")
			List<VisitGanttEvent> events = this.getEventMembers();
			for (VisitGanttEvent event : events)
			{
				if (event.getVisit().getId().equals(visit.getId()))
				{
					this.removeEvent(event);
					return event;
				}
			}
			return null;
		}
	}
	
	public abstract class VisitGanttEvent extends GanttEvent
	{
		public VisitGanttEvent(Visit visit, GanttChart ganttChart) 
		{
			super(ganttChart, visit.getTheme().getName());
			this.setData(visit);
		}
		
		public Visit getVisit()
		{
			return (Visit) this.getData();
		}

		public abstract Color getColor();

		public void update()
		{
			int fontStyle = Font.BOLD;
			if (this.getVisit().getState() != null)
			{
				if (this.getVisit().getState().equals(Visit.State.PROVISORILY))
				{
					fontStyle = Font.ITALIC;
				}
			}
			if (this.getTextFont() == null)
			{
				this.setTextFont(ganttChart.getFont());
			}
			this.getTextFont().getFontData()[0].setStyle(fontStyle);
			this.setStartDate(this.getVisit().getStart());
			this.setRevisedStart(this.getVisit().getStart());
			this.setEndDate(this.getVisit().getEnd());
			this.setRevisedEnd(this.getVisit().getEnd());
			this.setData(this.getVisit());
			this.setAutomaticRowHeight();
			Color newColor = getColor();
			Color oldColor = this.getGradientStatusColor();
			if (oldColor != null && (newColor == null || !this.getGradientStatusColor().equals(newColor)))
			{
				this.getGradientStatusColor().dispose();
			}
			if (newColor != null)
			{
				this.setGradientStatusColor(newColor);
			}
			this.setAdvancedTooltip(getTooltip(this.getVisit()));
		}
	}

}
