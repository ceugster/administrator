package ch.eugster.events.visits.views;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.ganttchart.AdvancedTooltip;
import org.eclipse.nebula.widgets.ganttchart.GanttChart;
import org.eclipse.nebula.widgets.ganttchart.GanttComposite;
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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.visits.Activator;
import ch.eugster.events.visits.editors.VisitEditor;
import ch.eugster.events.visits.editors.VisitEditorInput;

public abstract class AbstractGanttView<T extends AbstractEntity> extends ViewPart implements IViewPart, IGanttEventListener, ISelectionProvider
{
	private List<Color> colors = new ArrayList<Color>();

	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	protected ConnectionService connectionService;

	protected GanttChart ganttChart;

	protected ISelection selection;
	
	protected IDialogSettings settings;

	private List<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
	
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
		this.getSite().setSelectionProvider(this);
	}

	protected abstract void initialize();
	
	protected abstract void clear();
	
	@Override
	public void eventDoubleClicked(final GanttEvent event, final MouseEvent mouseEvent)
	{
		if (event.getData() instanceof Visit)
		{
			Visit visit = (Visit) event.getData();
			if (visit.isValid())
			{
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
	}

	@Override
	public void eventHeaderSelected(final Calendar newlySelectedDate, final List allSelectedDates)
	{
		System.out.println();
	}

	@Override
	public void eventMovedToNewSection(final GanttEvent event, final GanttSection oldSection,
			final GanttSection newSection)
	{
		System.out.println();
	}

	@Override
	public void eventPropertiesSelected(final List events)
	{
		System.out.println();
	}

	@Override
	public void eventReordered(final GanttEvent event)
	{
		System.out.println();
	}

	@Override
	public void eventsDeleteRequest(final List events, final MouseEvent mouseEvent)
	{
		System.out.println();
	}

	@Override
	public void eventsDroppedOrResizedOntoUnallowedDateRange(final List events,
			final GanttSpecialDateRange range)
	{
		System.out.println();
	}

	@Override
	public void eventSelected(final GanttEvent event, final List allSelectedEvents, final MouseEvent mouseEvent)
	{
		Object[] events = allSelectedEvents.toArray();
		this.setSelection(new StructuredSelection(events));
	}

	@Override
	public void eventsMoved(final List events, final MouseEvent mouseEvent)
	{
		System.out.println();
		eventsMoved(events);
	}

	@Override
	public void eventsMoveFinished(final List events, final MouseEvent mouseEvent)
	{
		System.out.println();
	}

	@Override
	public void eventsResized(final List events, final MouseEvent mouseEvent)
	{
		System.out.println();
	}

	@Override
	public void eventsResizeFinished(final List events, final MouseEvent mouseEvent)
	{
		System.out.println();
	}

	@Override
	public void lastDraw(final GC gc)
	{
		System.out.println();
	}

	@Override
	public void phaseMoved(final GanttPhase phase, final MouseEvent mouseEvent)
	{
		System.out.println();
	}

	@Override
	public void phaseMoveFinished(final GanttPhase phase, final MouseEvent mouseEvent)
	{
		System.out.println();
	}

	@Override
	public void phaseResized(final GanttPhase phase, final MouseEvent mouseEvent)
	{
		System.out.println();
	}

	@Override
	public void phaseResizeFinished(final GanttPhase phase, final MouseEvent mouseEvent)
	{
		System.out.println();
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
	}

	protected abstract Menu extendMenu(final GanttChart ganttChart);

	@Override
	public void dispose()
	{
		
		if (connectionServiceTracker != null)
		{
			connectionServiceTracker.close();
		}
		for (Color color : colors)
		{
			color.dispose();
		}
		super.dispose();
	}

	protected abstract String getContent(final Object object);

	protected abstract AdvancedTooltip getTooltip(final Object object);

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

	protected void fireSelectionEvent(ISelection selection)
	{
		
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener selectionChangedListener) 
	{
		this.selectionChangedListeners.add(selectionChangedListener);
	}

	@Override
	public ISelection getSelection() 
	{
		return this.selection;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener selectionChangedListener) 
	{
		this.selectionChangedListeners.remove(selectionChangedListener);
	}

	@Override
	public void setSelection(ISelection selection) 
	{
		this.selection = selection;
	}

}
