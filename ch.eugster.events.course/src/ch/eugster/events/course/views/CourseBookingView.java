package ch.eugster.events.course.views;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.course.Activator;
import ch.eugster.events.course.editors.BookingEditor;
import ch.eugster.events.course.editors.BookingEditorInput;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.filters.NotBookingParticipantFilter;
import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingAnnulatedState;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.IBookingState;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.ui.views.AbstractEntityView;

public class CourseBookingView extends AbstractEntityView implements IDoubleClickListener, ISelectionListener
{
	public static final String ID = "ch.eugster.events.course.views.courseBookingView";

	private TreeViewer viewer;

	private Label bookingStateLabelColumn1;

	private Label bookingStateLabelColumn2;

	public CourseBookingView()
	{
	}

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(this.viewer.getControl());

		this.viewer.getControl().setMenu(menu);

		this.getSite().registerContextMenu(menuManager, this.viewer);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(final Composite parent)
	{
		parent.setLayout(new GridLayout());

		final Tree tree = new Tree(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setHeaderVisible(true);
		tree.addListener(SWT.Expand, new Listener()
		{
			@Override
			public void handleEvent(final Event event)
			{
				if (tree != null && !tree.isDisposed())
				{
					tree.getDisplay().asyncExec(new Runnable()
					{
						@Override
						public void run()
						{
							if (tree.isDisposed())
								return;
							CourseBookingView.this.packColumns();
						}
					});
				}
			}
		});

		this.viewer = new TreeViewer(tree);
		this.viewer.setContentProvider(new CourseBookingContentProvider());
		this.viewer.setSorter(new CourseBookingSorter());
		this.viewer.addDoubleClickListener(this);
		ViewerFilter[] filters = new ViewerFilter[] { new NotBookingParticipantFilter(), new DeletedEntityFilter() };

		this.viewer.setFilters(filters);

		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Booking)
				{
					Booking booking = (Booking) cell.getElement();
					cell.setImage(CourseBookingView.this.getImage(booking));
					cell.setText(CourseFormatter.getInstance().formatBookingId(booking) + " "
							+ booking.getState().toString() + " ("
							+ Integer.valueOf(booking.getParticipantCount()).toString() + ")");
				}
				else if (cell.getElement() instanceof Participant)
				{
					Participant participant = (Participant) cell.getElement();
					cell.setImage(CourseBookingView.this.getImage(participant));
					cell.setText(PersonFormatter.getInstance().formatId(participant.getLink().getPerson()) + " ("
							+ participant.getCount() + ")");
				}
			}

		});
		GC gc = new GC(tree);
		FontMetrics fm = gc.getFontMetrics();
		int charWidth = fm.getAverageCharWidth();
		int columnWidth = charWidth * 30;

		TreeColumn treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Buchung");
		treeColumn.setWidth(columnWidth);
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Booking)
				{
					Booking booking = (Booking) cell.getElement();
					cell.setText(PersonFormatter.getInstance().formatLastnameFirstname(
							booking.getParticipant().getLink().getPerson()));
				}
				else if (cell.getElement() instanceof Participant)
				{
					Participant participant = (Participant) cell.getElement();
					cell.setText(PersonFormatter.getInstance().formatLastnameFirstname(
							participant.getLink().getPerson()));
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Teilnehmer");
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Booking)
				{
					Booking booking = (Booking) cell.getElement();
					Participant participant = booking.getParticipant();
					cell.setText(AddressFormatter.getInstance().formatCityLine(participant.getLink().getAddress()));
				}
				else if (cell.getElement() instanceof Participant)
				{
					Participant participant = (Participant) cell.getElement();
					cell.setText(AddressFormatter.getInstance().formatCityLine(participant.getLink().getAddress()));
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Adresse");
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Booking)
				{
					Booking booking = (Booking) cell.getElement();
					Participant participant = booking.getParticipant();
					if (participant.getBookingType() != null)
						cell.setText(CourseFormatter.getInstance().formatComboEntry(participant.getBookingType()));
				}
				else if (cell.getElement() instanceof Participant)
				{
					Participant participant = (Participant) cell.getElement();
					if (participant.getBookingType() != null)
						cell.setText(CourseFormatter.getInstance().formatComboEntry(participant.getBookingType()));
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Buchungsart");
		treeColumn.setResizable(true);

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.viewer);

		CourseView courseView = null;
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null)
		{
			courseView = (CourseView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.findView(CourseView.ID);
		}
		if (courseView == null)
		{
			IWorkbenchPage[] pages = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages();
			for (IWorkbenchPage page : pages)
			{
				courseView = (CourseView) page.findView(CourseView.ID);
				if (courseView != null)
					break;
			}
		}
		if (courseView == null || courseView.getViewer() == null)
			this.setInput(null);
		else
			this.selectionChanged(courseView, courseView.getViewer().getSelection());

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(new GridLayout(2, true));

		this.bookingStateLabelColumn1 = new Label(composite, SWT.NONE);
		this.bookingStateLabelColumn1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.bookingStateLabelColumn2 = new Label(composite, SWT.NONE);
		this.bookingStateLabelColumn2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Course.class, this);
		EntityMediator.removeListener(Booking.class, this);
		EntityMediator.removeListener(Person.class, this);
		EntityMediator.removeListener(LinkPersonAddress.class, this);
		EntityMediator.removeListener(Participant.class, this);

		this.getSite().getPage().removeSelectionListener(CourseView.ID, this);

		super.dispose();
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof Booking)
		{
			this.editBooking((Booking) object);
		}
	}

	private void editBooking(final Booking booking)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new BookingEditorInput(booking), BookingEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}

	private Image getImage(final Booking booking)
	{
		if (booking.getState() instanceof BookingForthcomingState)
		{
			if (booking.getState().equals(BookingForthcomingState.BOOKED))
				return Activator.getDefault().getImageRegistry().get("BOOKING_GREEN");
			else if (booking.getState().equals(BookingForthcomingState.PROVISIONAL_BOOKED))
				return Activator.getDefault().getImageRegistry().get("BOOKING_BLUE");
			else if (booking.getState().equals(BookingForthcomingState.WAITING_LIST))
				return Activator.getDefault().getImageRegistry().get("BOOKING_RED");
			else if (booking.getState().equals(BookingForthcomingState.BOOKING_CANCELED))
				return Activator.getDefault().getImageRegistry().get("BOOKING_GREY");
		}
		else if (booking.getState() instanceof BookingDoneState)
		{
			if (booking.getState().equals(BookingDoneState.PARTICIPATED))
				return Activator.getDefault().getImageRegistry().get("BOOKING_GREEN");
			else if (booking.getState().equals(BookingDoneState.PARTICIPATION_BROKE_OFF))
				return Activator.getDefault().getImageRegistry().get("BOOKING_BLUE");
			else if (booking.getState().equals(BookingDoneState.NOT_PARTICIPATED))
				return Activator.getDefault().getImageRegistry().get("BOOKING_RED");
		}
		else if (booking.getState() instanceof BookingAnnulatedState)
		{
			if (booking.getState().equals(BookingAnnulatedState.ANNULATED))
				return Activator.getDefault().getImageRegistry().get("BOOKING_BLUE");
			else if (booking.getState().equals(BookingAnnulatedState.COURSE_CANCELED))
				return Activator.getDefault().getImageRegistry().get("BOOKING_RED");
		}
		return null;
	}

	// private IMenuManager createGeneratorSubmenu(IMenuManager menuManager,
	// final StructuredSelection ssel)
	// {
	// IMenuManager subMenuManager = new MenuManager("Generieren");
	// subMenuManager.addMenuListener(new IMenuListener()
	// {
	// @Override
	// public void menuAboutToShow(IMenuManager manager)
	// {
	// }
	// });
	// subMenuManager.add(CourseBookingView.this.createBookingConfirmationAction(subMenuManager,
	// ssel));
	// subMenuManager.add(CourseBookingView.this.createInvitationAction(subMenuManager,
	// ssel));
	// subMenuManager.add(CourseBookingView.this.createParticipationConfirmationAction(subMenuManager,
	// ssel));
	// subMenuManager.setVisible(ssel.getFirstElement() instanceof Booking);
	//
	// return subMenuManager;
	// }
	//
	// private IContributionItem createBookingConfirmationAction(IMenuManager
	// manager, final StructuredSelection ssel)
	// {
	// IAction action = new Action()
	// {
	// @Override
	// public void run()
	// {
	// BookingConfirmationGenerator.getInstance().execute(ssel);
	// }
	// };
	// action.setText("Anmeldebestätigung");
	// action.setAccelerator(SWT.MOD1 | 'A');
	// action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("GENERATE"));
	// action.setEnabled(BookingConfirmationGenerator.getInstance().getTemplate()
	// != null);
	//
	// IContributionItem item = new ActionContributionItem(action);
	// item.setParent(manager);
	// item.setVisible(true);
	// return item;
	// }
	//
	// private IAction createInvitationAction(IMenuManager manager, final
	// StructuredSelection ssel)
	// {
	// IAction action = new Action()
	// {
	// @Override
	// public void run()
	// {
	// InvitationGenerator.getInstance().execute(ssel);
	// }
	// };
	// action.setText("Kurseinladung");
	// action.setAccelerator(SWT.MOD1 | 'K');
	// action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("GENERATE"));
	// action.setEnabled(InvitationGenerator.getInstance().getTemplate() !=
	// null);
	// return action;
	// }
	//
	// private IAction createParticipationConfirmationAction(IMenuManager
	// manager, final StructuredSelection ssel)
	// {
	// IAction action = new Action()
	// {
	// @Override
	// public void run()
	// {
	// ParticipationConfirmationGenerator.getInstance().execute(ssel);
	// }
	// };
	// action.setText("Teilnahmebestätigung");
	// action.setAccelerator(SWT.MOD1 | 'T');
	// action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("GENERATE"));
	// action.setEnabled(ParticipationConfirmationGenerator.getInstance().getTemplate()
	// != null);
	// return action;
	// }

	private Image getImage(final Participant participant)
	{
		if (participant.getId().equals(participant.getBooking().getParticipant().getId()))
			return Activator.getDefault().getImageRegistry().get("BOOKING_RED");
		else
			return Activator.getDefault().getImageRegistry().get("BOOKING_BLUE");
	}

	public TreeViewer getViewer()
	{
		return this.viewer;
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);

		EntityMediator.addListener(Course.class, this);
		EntityMediator.addListener(Booking.class, this);
		EntityMediator.addListener(Person.class, this);
		EntityMediator.addListener(LinkPersonAddress.class, this);
		EntityMediator.addListener(Participant.class, this);
		site.getPage().addSelectionListener(CourseView.ID, this);
	}

	private void packColumns()
	{
		if (this.viewer != null)
		{
			TreeColumn[] columns = this.viewer.getTree().getColumns();
			for (TreeColumn column : columns)
			{
				column.pack();
			}
		}
	}

	@Override
	public void postDelete(AbstractEntity entity)
	{
		entity = refreshEntity(entity);
		if (this.viewer.getInput() != null)
		{
			if (entity instanceof Season)
				this.viewer.refresh();
			else if (entity instanceof Course)
				this.viewer.refresh();
			else if (entity instanceof Booking)
				this.viewer.refresh(((Booking) entity).getCourse());
			else if (entity instanceof Participant)
				this.viewer.refresh(((Participant) entity).getBooking());
		}
	}

	@Override
	public void postPersist(AbstractEntity entity)
	{
		entity = refreshEntity(entity);
		if (this.viewer.getInput() != null)
		{
			if (entity instanceof Season)
			{
				this.viewer.add(this, entity);
			}
			else if (entity instanceof Course)
			{
				Course course = (Course) entity;
				this.viewer.add(course.getSeason(), course);
			}
			else if (entity instanceof Booking)
			{
				Booking booking = (Booking) entity;
				this.viewer.add(booking.getCourse(), booking);
			}
			else if (entity instanceof Participant)
			{
				Participant participant = (Participant) entity;
				this.viewer.refresh(participant.getBooking());
			}
			this.packColumns();
		}
	}

	@Override
	public void postUpdate(AbstractEntity entity)
	{
		entity = refreshEntity(entity);
		if (this.viewer.getInput() != null)
		{
			if (entity instanceof Season)
			{
				this.viewer.refresh();
			}
			else if (entity instanceof Course)
			{
				this.viewer.refresh(entity);
			}
			else if (entity instanceof Booking)
			{
				Booking booking = (Booking) entity;
				Course course = booking.getCourse();
				if (this.viewer.getInput().equals(course))
				{
					this.viewer.refresh(entity);
					this.setSummaryLabels(course);
				}
			}
			else if (entity instanceof Participant)
			{
				Participant participant = (Participant) entity;
				Booking booking = participant.getBooking();
				Course course = booking.getCourse();
				if (this.viewer.getInput().equals(course))
				{
					this.viewer.refresh(booking);
					this.setSummaryLabels(course);
				}
			}

			this.packColumns();
		}
	}

	private AbstractEntity refreshEntity(AbstractEntity entity)
	{
		// ServiceTracker tracker = new
		// ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
		// ConnectionService.class.getName(), null);
		// tracker.open();
		// try
		// {
		// ConnectionService service = (ConnectionService) tracker.getService();
		// return service.refresh(entity);
		// }
		// finally
		// {
		// tracker.close();
		// }
		return entity;
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection)
	{
		if (part instanceof CourseView)
		{
			Job job = new Job("Lade Buchungen...")
			{
				@Override
				public IStatus run(final IProgressMonitor monitor)
				{
					monitor.beginTask("Buchungen werden geladen...", IProgressMonitor.UNKNOWN);
					Display.getDefault().syncExec(new Runnable()
					{
						@Override
						public void run()
						{
							CourseView view = (CourseView) part;
							if (!view.getViewer().getSelection().isEmpty())
							{
								StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
								setInput(ssel.getFirstElement());
							}
						}
					});
					monitor.done();
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus()
	{
		if (this.viewer != null)
			this.viewer.getTree().setFocus();
	}

	public void setInput(final Object object)
	{
		if (this.viewer != null)
		{
			viewer.setInput(object);
			packColumns();
			setSummaryLabels(object);
		}
	}

	private void setSummaryLabels(final Object object)
	{
		int rows = Math.max(BookingForthcomingState.values().length, BookingDoneState.values().length);
		rows = Math.max(rows, BookingAnnulatedState.values().length);
		rows = (rows / 2) + (rows % 2);

		if (this.bookingStateLabelColumn1 != null)
		{
			if (object == null)
			{
				for (int i = 0; i < rows; i++)
				{
					this.bookingStateLabelColumn1.setText(this.bookingStateLabelColumn1.getText() + "\n");
					this.bookingStateLabelColumn2.setText(this.bookingStateLabelColumn2.getText() + "\n");
				}
			}
			else if (object instanceof Course)
			{
				int[] count = null;
				IBookingState[] states = null;
				this.bookingStateLabelColumn1.setText("");
				this.bookingStateLabelColumn2.setText("");

				Course course = (Course) object;
				Collection<Booking> bookings = course.getBookings();

				if (course.getState().equals(CourseState.FORTHCOMING))
				{
					states = BookingForthcomingState.values();
					count = new int[states.length];
					for (Booking booking : bookings)
					{
						count[booking.getForthcomingState().ordinal()] = count[booking.getForthcomingState().ordinal()]
								+ booking.getParticipantCount();
					}
				}
				else if (course.getState().equals(CourseState.DONE))
				{
					states = BookingDoneState.values();
					count = new int[states.length];
					for (Booking booking : bookings)
					{
						count[booking.getDoneState().ordinal()] = count[booking.getDoneState().ordinal()]
								+ booking.getParticipantCount();
					}
				}
				if (course.getState().equals(CourseState.ANNULATED))
				{
					states = BookingAnnulatedState.values();
					count = new int[states.length];
					for (Booking booking : bookings)
					{
						count[booking.getAnnulatedState().ordinal()] = count[booking.getAnnulatedState().ordinal()]
								+ booking.getParticipantCount();
					}
				}
				for (int i = 0; i < rows; i++)
				{
					this.bookingStateLabelColumn1.setText(this.bookingStateLabelColumn1.getText()
							+ states[i].toString() + ": " + count[i]);
					if (!this.bookingStateLabelColumn1.getText().isEmpty())
						this.bookingStateLabelColumn1.setText(this.bookingStateLabelColumn1.getText() + "\n");
				}
				for (int i = rows; i < count.length; i++)
				{
					this.bookingStateLabelColumn2.setText(this.bookingStateLabelColumn2.getText()
							+ states[i].toString() + ": " + count[i]);
					if (!this.bookingStateLabelColumn2.getText().isEmpty())
						this.bookingStateLabelColumn2.setText(this.bookingStateLabelColumn2.getText() + "\n");
				}
			}
			this.bookingStateLabelColumn1.getParent().getParent().layout();
		}
	}

}