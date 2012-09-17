package ch.eugster.events.visits.views;

import java.awt.Font;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.nebula.widgets.ganttchart.AdvancedTooltip;
import org.eclipse.nebula.widgets.ganttchart.GanttChart;
import org.eclipse.nebula.widgets.ganttchart.GanttEvent;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityListener;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.model.VisitTheme;
import ch.eugster.events.persistence.model.VisitVisitor;
import ch.eugster.events.persistence.queries.VisitQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.formatters.AddressFormatter;
import ch.eugster.events.ui.formatters.PersonFormatter;
import ch.eugster.events.visits.Activator;
import ch.eugster.events.visits.editors.VisitEditor;
import ch.eugster.events.visits.editors.VisitEditorInput;

public class OverviewView extends ViewPart implements IViewPart
{
	public static final String ID = "ch.eugster.events.visits.overview";

	private ServiceTracker connectionServiceTracker;

	private GanttChart ganttChart;

	private IDialogSettings settings;

	private EntityListener entityListener;

	public OverviewView()
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

	// public GanttChart getGanttChart()
	// {
	// return ganttChart;
	// }

	private GanttEvent createEvent(final Visit visit)
	{
		GanttEvent event = new GanttEvent(ganttChart, visit, visit.getTheme().getName(), visit.getStart(),
				visit.getEnd(), 0);
		event.setResizable(false);
		event.setMoveable(false);
		Map<Long, GanttEvent> events = getEventsMap();
		events.get(key).put(visit.getId(), event);
		return doUpdateEvent(visit, event);
	}

	private Map<Long, Map<Long, GanttEvent>> createEventsMap()
	{
		Map<Long, Map<Long, GanttEvent>> events = new HashMap<Long, Map<Long, GanttEvent>>();
		ganttChart.setData("events", events);
		return events;
	}

	private Menu createMenu(final GanttChart ganttChart)
	{
		Menu menu = new Menu(ganttChart.getGanttComposite());
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("MenuItem");
		return menu;
	}

	private void createOrUpdateEvent(final GanttEvent event, final Visit visit, final VisitVisitor visitor)
	{
		if (event == null)
		{
			createEvent(visit, visitor);
		}
		else
		{
			updateEvent(visit, visitor, event);
		}
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		GridLayout gridLayout = new GridLayout();
		parent.setLayout(gridLayout);

		ganttChart = new GanttChart(parent, SWT.None, new VisitSettings());
		if (PlatformUI.getWorkbench().getDisplay().getActiveShell().getFont() != null)
		{
			ganttChart.setFont(PlatformUI.getWorkbench().getDisplay().getActiveShell().getFont());
		}

		ganttChart.setLayoutData(new GridData(GridData.FILL_BOTH));
		ganttChart.getGanttComposite().setMenu(this.createMenu(ganttChart));
		ganttChart.getGanttComposite().setView(settings.getInt("view"));
		ganttChart.getGanttComposite().setZoomLevel(settings.getInt("zoom"));
		ganttChart.getGanttComposite().setDate(GregorianCalendar.getInstance(), SWT.CENTER);
		ganttChart.addGanttEventListener(new IGanttEventListener()
		{

			@Override
			public void eventDoubleClicked(final GanttEvent event, final MouseEvent mouseEvent)
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
				// TODO Auto-generated method stub

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
		connectionServiceTracker.open();
		registerEntityListeners();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Visit.class, this.entityListener);
		if (connectionServiceTracker != null)
		{
			connectionServiceTracker.close();
		}
	}

	private GanttEvent doUpdateEvent(final Visit visit, final GanttEvent event)
	{
		int fontStyle = Font.BOLD;
		if (visit.getState() != null)
		{
			if (visit.getState().equals(Visit.State.PROVISORILY))
			{
				fontStyle = Font.ITALIC;
			}
		}
		if (event.getTextFont() == null)
		{
			event.setTextFont(ganttChart.getFont());
		}
		event.getTextFont().getFontData()[0].setStyle(fontStyle);
		event.setStartDate(visit.getStart());
		event.setRevisedStart(visit.getStart());
		event.setEndDate(visit.getEnd());
		event.setRevisedEnd(visit.getEnd());
		event.setData(visit);
		event.setAutomaticRowHeight();
		Color newColor = getColor(visit.getTheme());
		Color oldColor = event.getGradientStatusColor();
		if (oldColor != null && (newColor == null || !event.getGradientStatusColor().equals(newColor)))
		{
			event.getGradientStatusColor().dispose();
		}
		if (newColor != null)
		{
			event.setGradientStatusColor(newColor);
		}
		event.setAdvancedTooltip(getTooltip(visit));
		return event;
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

	private GanttEvent getEvent(final Visit visit, final VisitVisitor visitor)
	{
		Map<Long, GanttEvent> events = getEventsMap().get(visit.getId());
		if (visitor == null)
		{
			return events.get(visit.getId());
		}
		return events.get(visitor.getId());
	}

	private Map<Long, Map<Long, GanttEvent>> getEventsMap()
	{
		@SuppressWarnings("unchecked")
		Map<Long, Map<Long, GanttEvent>> events = (Map<Long, Map<Long, GanttEvent>>) ganttChart.getData("events");
		if (events == null)
		{
			events = createEventsMap();
		}
		return events;
	}

	private AdvancedTooltip getTooltip(final Visit visit)
	{
		StringBuilder tooltipTitle = new StringBuilder(visit.getTheme() == null ? "" : visit.getTheme().getName());
		tooltipTitle = tooltipTitle.append(visit.getState() == null ? "" : " - " + visit.getState().label());
		return new AdvancedTooltip(tooltipTitle.toString(), getContent(visit));
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);

		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(final ServiceReference reference)
			{
				ConnectionService service = (ConnectionService) super.addingService(reference);
				VisitQuery query = (VisitQuery) service.getQuery(Visit.class);
				loadVisits(query.selectAppointed());
				return service;
			}

			@Override
			public void remove(final ServiceReference reference)
			{
				loadVisits(new ArrayList<Visit>());
				super.remove(reference);
			}

		};
	}

	private void loadVisits(final Collection<Visit> visits)
	{
		for (Visit visit : visits)
		{
			if (visit.getVisitors().isEmpty())
			{
				createOrUpdateEvent(getEvent(visit, null), visit);
			}
			else
			{
				for (VisitVisitor visitor : visit.getVisitors())
				{
					createOrUpdateEvent(getEvent(visit, visitor), visit);
				}
			}
		}
	}

	private void registerEntityListeners()
	{
		this.entityListener = new EntityAdapter()
		{

			@Override
			public void postDelete(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
					removeEvent(visit);
				}
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
					createEvent(visit);
				}
			}

			@Override
			public void postRemove(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
					removeEvent(visit);
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof Visit)
				{
					Visit visit = (Visit) entity;
					GanttEvent event = getEvent(visit);
					if (visit.getStart() == null || visit.getEnd() == null)
					{
						if (event != null)
						{
							removeEvent(visit);
						}
					}
					else
					{
						if (event == null)
						{
							createEvent(visit);
						}
						else
						{
							updateEvent(visit, event);
						}
					}
				}
			}
		};
		EntityMediator.addListener(Visit.class, this.entityListener);
	}

	private void removeEvent(final Visit visit)
	{
		GanttEvent event = getEventsMap().get(visit.getId());
		if (event != null)
		{
			getEventsMap().remove(visit.getId());
			if (event.getGradientStatusColor() != null)
			{
				event.getGradientStatusColor().dispose();
			}
			event.dispose();
		}
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

	private GanttEvent updateEvent(final Visit visit, final GanttEvent event)
	{
		doUpdateEvent(visit, event);
		event.update(true);
		return event;
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
}
