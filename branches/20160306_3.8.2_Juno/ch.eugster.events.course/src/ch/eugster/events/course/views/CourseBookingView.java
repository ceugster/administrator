package ch.eugster.events.course.views;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
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
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.course.editors.BookingEditor;
import ch.eugster.events.course.editors.BookingEditorInput;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingAnnulatedState;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.IBookingState;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.queries.BookingQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.views.AbstractEntityView;

public class CourseBookingView extends AbstractEntityView implements IDoubleClickListener, ISelectionListener
{
	public static final String ID = "ch.eugster.events.course.views.courseBookingView";

	private CheckboxTreeViewer viewer;

	private Label bookingViewLabelMinMaxParticipants;

	private Label bookingViewLabelBookingStates;

	private Label bookingViewLabelBookingTypes;

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

		final Tree tree = new Tree(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
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
		tree.addListener(SWT.Selection, new Listener() 
		{
			@Override
			public void handleEvent(Event event) 
			{
				if (event.detail == SWT.CHECK)
				{
					TreeItem item = (TreeItem) event.item;
					Object data = item.getData();
					if (data instanceof Booking)
					{
						Booking booking = (Booking) data;
						booking.setPayAmount(item.getChecked() ? booking.getAmount() : 0D);
						ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class, null);
						tracker.open();
						try
						{
							ConnectionService service = tracker.getService();
							if (service != null)
							{
								BookingQuery query = (BookingQuery) service.getQuery(Booking.class);
								setInput(query.merge(booking).getCourse());
							}
						}
						finally
						{
							tracker.close();
						}
					}
					else if (data instanceof Participant)
					{
						event.doit = false;
						try
						{
							tree.setRedraw(false);
							item.setChecked(! item.getChecked() );
						} 
						finally 
						{
							tree.setRedraw(true);
						}
					}
					System.out.println();
				}
			}
		});
		
		this.viewer = new CheckboxTreeViewer(tree);
		this.viewer.setContentProvider(new CourseBookingContentProvider());
		this.viewer.setSorter(new CourseBookingSorter());
		this.viewer.addDoubleClickListener(this);
		ViewerFilter[] filters = new ViewerFilter[] { new DeletedEntityFilter() };

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
					cell.setText(CourseFormatter.getInstance().formatBookingId(booking)
							+ " - " + booking.getState().toString() + " ("
							+ Integer.valueOf(booking.getParticipantCount()).toString() + ")");
//					TreeItem item = (TreeItem) cell.getItem();
//					item.setGrayed(booking.getPayAmount() > 0D);
//					item.setChecked(booking.getAmount() <= booking.getPayAmount());
				}
				else if (cell.getElement() instanceof Participant)
				{
					Participant participant = (Participant) cell.getElement();
					cell.setImage(CourseBookingView.this.getImage(participant));
					cell.setText(PersonFormatter.getInstance().formatId(participant.getLink().getPerson()) + " - " +
							PersonFormatter.getInstance().formatLastnameFirstname(
							participant.getLink().getPerson()));
				}
			}

		});
		GC gc = new GC(tree);
		FontMetrics fm = gc.getFontMetrics();
		int charWidth = fm.getAverageCharWidth();
		int columnWidth = charWidth * 30;
		gc.dispose();

		TreeColumn treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Buchung Status");
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
					Participant participant = booking.getParticipant();
					cell.setText(PersonFormatter.getInstance().formatLastnameFirstname(
							participant.getLink().getPerson()));
				}
				else if (cell.getElement() instanceof Participant)
				{
					Participant participant = (Participant) cell.getElement();
					if (participant.getBookingType() != null)
					{
						cell.setText(CourseFormatter.getInstance().formatComboEntryBookingType(participant));
					}
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Name/Buchungsart");
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
					cell.setText(NumberFormat.getCurrencyInstance().format(booking.getAmount()));
				}
				else if (cell.getElement() instanceof Participant)
				{
					Participant participant = (Participant) cell.getElement();
					cell.setText(NumberFormat.getCurrencyInstance().format(participant.getAmount()));
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Betrag");
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
				String email = "";
				if (cell.getElement() instanceof Booking)
				{
					Booking booking = (Booking) cell.getElement();
					Participant participant = booking.getParticipant();
					email = participant.getEmail();
				}
				else if (cell.getElement() instanceof Participant)
				{
					Participant participant = (Participant) cell.getElement();
					email = participant.getEmail();
				}
				cell.setText(email);
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Email");
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
		composite.setLayout(new GridLayout());

		this.bookingViewLabelMinMaxParticipants = new Label(composite, SWT.NONE);
		this.bookingViewLabelMinMaxParticipants.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.bookingViewLabelBookingStates = new Label(composite, SWT.NONE);
		this.bookingViewLabelBookingStates.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.bookingViewLabelBookingTypes = new Label(composite, SWT.NONE);
		this.bookingViewLabelBookingTypes.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
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
			return getForthcomingStateImage(booking);
		}
		else if (booking.getState() instanceof BookingDoneState)
		{
			return getDoneStateImage(booking);
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
	
	private Image getForthcomingStateImage(Booking booking)
	{
		if (booking.getState().equals(BookingForthcomingState.BOOKED))
			return selectImageForPayedOrUnpayed(booking, "BOOKING_GREEN", "BOOKING_GREEN_EXCLAMATION");
		else if (booking.getState().equals(BookingForthcomingState.PROVISIONAL_BOOKED))
			return Activator.getDefault().getImageRegistry().get("BOOKING_BLUE");
		else if (booking.getState().equals(BookingForthcomingState.WAITING_LIST))
			return Activator.getDefault().getImageRegistry().get("BOOKING_RED");
		else if (booking.getState().equals(BookingForthcomingState.BOOKING_CANCELED))
			return Activator.getDefault().getImageRegistry().get("BOOKING_GREY");
		else return null;
	}
	
	private Image getDoneStateImage(Booking booking)
	{
		if (booking.getState().equals(BookingForthcomingState.BOOKED))
			return selectImageForPayedOrUnpayed(booking, "BOOKING_GREEN", "BOOKING_GREEN_EXCLAMATION");
		else if (booking.getState().equals(BookingForthcomingState.PROVISIONAL_BOOKED))
			return Activator.getDefault().getImageRegistry().get("BOOKING_BLUE");
		else if (booking.getState().equals(BookingForthcomingState.WAITING_LIST))
			return Activator.getDefault().getImageRegistry().get("BOOKING_RED");
		else if (booking.getState().equals(BookingForthcomingState.BOOKING_CANCELED))
			return Activator.getDefault().getImageRegistry().get("BOOKING_GREY");
		else return null;
	}
	
	private Image selectImageForPayedOrUnpayed(Booking booking, String keyPayed, String keyUnpayed)
	{
		if (booking.getAmount() <= (booking.getPayAmount() - booking.getPayBackAmount()))
		{
			return Activator.getDefault().getImageRegistry().get(keyPayed);
		}
		return Activator.getDefault().getImageRegistry().get(keyUnpayed);
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
	public void postDelete(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (viewer.getInput() != null)
				{
					if (entity instanceof Season)
					{
						viewer.refresh();
						setSummaryLabels((Course)viewer.getInput());
					}
					else if (entity instanceof Course)
					{
						if (viewer.getInput().equals(entity))
						{
							viewer.refresh();
							setSummaryLabels((Course)entity);
						}
					}
					else if (entity instanceof Booking)
					{
						Booking booking = (Booking) entity;
						viewer.refresh(booking);
						setSummaryLabels(booking.getCourse());
					}
					else if (entity instanceof Participant)
					{
						Participant participant = (Participant) entity;
						viewer.refresh(participant.getBooking());
						setSummaryLabels(participant.getBooking().getCourse());
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void postPersist(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (viewer.getInput() != null)
				{
					if (entity instanceof Season)
					{
						viewer.add(this, entity);
					}
					else if (entity instanceof Course)
					{
						Course course = (Course) entity;
						viewer.add(course.getSeason(), course);
					}
					else if (entity instanceof Booking)
					{
						Booking booking = (Booking) entity;
						viewer.add(booking.getCourse(), booking);
					}
					else if (entity instanceof Participant)
					{
						Participant participant = (Participant) entity;
						viewer.refresh(participant.getBooking());
					}
					packColumns();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (viewer.getInput() != null)
				{
					if (entity instanceof Season)
					{
						viewer.refresh();
						Course course = (Course) viewer.getInput();
						setSummaryLabels(course);
					}
					else if (entity instanceof Course)
					{
						viewer.refresh(entity);
						setSummaryLabels((Course)entity);
					}
					else if (entity instanceof Booking)
					{
						Booking booking = (Booking) entity;
						Course course = booking.getCourse();
						if (viewer.getInput().equals(course))
						{
							viewer.refresh(booking);
							setSummaryLabels(course);
						}
					}
					else if (entity instanceof Participant)
					{
						Participant participant = (Participant) entity;
						Booking booking = participant.getBooking();
						Course course = booking.getCourse();
						if (viewer.getInput().equals(course))
						{
							viewer.refresh(booking);
							setSummaryLabels(course);
						}
					}
					packColumns();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
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

		if (this.bookingViewLabelMinMaxParticipants != null)
		{
			if (object == null)
			{
				for (int i = 0; i < rows; i++)
				{
					this.bookingViewLabelMinMaxParticipants.setText(this.bookingViewLabelMinMaxParticipants.getText() + "\n");
					// this.bookingStateLabelColumn2.setText(this.bookingStateLabelColumn2.getText()
					// + "\n");
				}
			}
			else if (object instanceof Course)
			{
				Map<IBookingState, BookingStateCount> states = new HashMap<IBookingState, BookingStateCount>();
				Map<BookingType, BookingTypeCount> types = new HashMap<BookingType, BookingTypeCount>();
				Course course = (Course) object;
				this.bookingViewLabelMinMaxParticipants.setText("Minimale Teilnehmerzahl: " + course.getMinParticipants()
						+ " | Maximale Teilnehmerzahl: " + course.getMaxParticipants());
				List<Booking> bookings = course.getBookings();
				for (Booking booking : bookings)
				{
					BookingStateCount state = states.get(booking.getState());
					if (state == null)
					{
						state = new BookingStateCount(booking.getState(), booking.getParticipantCount());
						states.put(booking.getState(), state);
					}
					else
					{
						state.addCount(booking.getParticipantCount());
					}
					List<Participant> participants = booking.getParticipants();
					for (Participant participant : participants)
					{
						IBookingState bookingState = participant.getBooking().getState();
						if (bookingState.equals(BookingForthcomingState.BOOKED) || bookingState.equals(BookingDoneState.PARTICIPATED))
						{
							BookingType bookingType = participant.getBookingType();
							BookingTypeCount type = types.get(bookingType);
							if (type == null)
							{
								type = new BookingTypeCount(bookingType, participant.getCount());
								types.put(bookingType, type);
							}
							else
							{
								type.addCount(participant.getCount());
							}
						}
					}
				}
				StringBuilder text = new StringBuilder();
				int total = 0;
				for (BookingStateCount state : states.values())
				{
					if (text.length() == 0)
					{
						text = text.append("Buchungsstatus: " + state.getBookingStateName() + ": " + state.getCount());
					}
					else
					{
						text = text.append(" | " + state.getBookingStateName() + ": " + state.getCount());
					}
//					total += state.getCount();
				}
//				text = text.append(" | Total: " + total);
				this.bookingViewLabelBookingStates.setText(text.toString().trim());

				text = new StringBuilder();
				total = 0;
				for (BookingTypeCount type : types.values())
				{
					if (text.length() == 0)
					{
						text = text.append("Buchungsarten: " + type.getBookingTypeName() + ": " + type.getCount());
					}
					else
					{
						text = text.append(" | " + type.getBookingTypeName() + ": " + type.getCount());
					}
					total += type.getCount();
				}
				text = text.append(" | Total: " + total);
				this.bookingViewLabelBookingTypes.setText(text.toString().trim());
			}
			this.bookingViewLabelMinMaxParticipants.getParent().getParent().layout();
		}
	}

	private class BookingStateCount
	{
		int count;

		IBookingState state;

		public BookingStateCount(IBookingState state, int count)
		{
			this.count = count;
			this.state = state;
		}

		public String getBookingStateName()
		{
			return state.toString();
		}

		public int getCount()
		{
			return this.count;
		}

		public void addCount(int count)
		{
			this.count += count;
		}
	}

	private class BookingTypeCount
	{
		int count;

		BookingType type;

		public BookingTypeCount(BookingType type, int count)
		{
			this.count = count;
			this.type = type;
		}

		public String getBookingTypeName()
		{
			return type == null ? "" : type.getName();
		}

		public int getCount()
		{
			return this.count;
		}

		public void addCount(int count)
		{
			this.count += count;
		}
	}
}