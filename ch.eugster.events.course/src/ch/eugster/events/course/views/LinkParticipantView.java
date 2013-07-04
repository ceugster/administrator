package ch.eugster.events.course.views;

import java.text.DateFormat;

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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.course.Activator;
import ch.eugster.events.course.editors.BookingEditor;
import ch.eugster.events.course.editors.BookingEditorInput;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.person.views.PersonView;
import ch.eugster.events.ui.views.AbstractEntityView;

public class LinkParticipantView extends AbstractEntityView implements IDoubleClickListener, ISelectionListener
{
	public static final String ID = "ch.eugster.events.course.views.linkParticipantView";

	private TreeViewer viewer;

	private final DateFormat df = DateFormat.getDateInstance();

	public LinkParticipantView()
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

		Composite filterComposite = new Composite(parent, SWT.NONE);
		filterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterComposite.setLayout(new GridLayout(3, false));

		final Tree tree = new Tree(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setHeaderVisible(true);
		tree.addListener(SWT.Expand, new Listener()
		{
			@Override
			public void handleEvent(final Event event)
			{
				tree.getDisplay().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						if (tree.isDisposed())
							return;
						LinkParticipantView.this.packColumns();
					}
				});
			}
		});

		this.viewer = new TreeViewer(tree);
		this.viewer.setContentProvider(new LinkParticipantContentProvider());
		this.viewer.setSorter(new LinkParticipantSorter());
		ViewerFilter[] filters = new ViewerFilter[] { new DeletedEntityFilter() };
		this.viewer.setFilters(filters);
		this.viewer.addDoubleClickListener(this);

		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Booking)
				{
					Booking booking = (Booking) cell.getElement();
					cell.setImage(LinkParticipantView.this.getImage(booking.getCourse()));
					cell.setText(CourseFormatter.getInstance().formatBookingId(booking));
				}
				else if (cell.getElement() instanceof Participant)
				{
					Participant participant = (Participant) cell.getElement();
					cell.setImage(LinkParticipantView.this.getImage(participant));
					cell.setText(PersonFormatter.getInstance().formatId(participant.getLink().getPerson()));
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
					cell.setText(CourseFormatter.getInstance().formatComboEntry(booking.getCourse()));
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
		treeColumn.setText("Kurs/Teilnehmer");
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
					cell.setText(booking.getState().toString());
				}
				else if (cell.getElement() instanceof Participant)
				{
					Participant participant = (Participant) cell.getElement();
					cell.setText(participant.getBookingType().getName());
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Status");
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
					cell.setText(Integer.valueOf(booking.getParticipantCount()).toString());
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Teilnehmer");
		treeColumn.setResizable(true);

		this.createContextMenu();

		PersonView view = (PersonView) this.getSite().getPage().findView(PersonView.ID);
		if (view != null)
		{
			IStructuredSelection ssel = (IStructuredSelection) view.getViewer().getSelection();
			this.viewer.setInput(ssel.getFirstElement());
		}

		this.getSite().getPage().addSelectionListener(PersonView.ID, this);
		this.getSite().setSelectionProvider(this.viewer);
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Booking.class, this);
		EntityMediator.removeListener(LinkPersonAddress.class, this);
		EntityMediator.removeListener(Person.class, this);
		EntityMediator.removeListener(Address.class, this);
		EntityMediator.removeListener(Participant.class, this);

		this.getSite().getPage().removeSelectionListener(PersonView.ID, this);

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

	private Image getImage(final Course course)
	{
		if (course.getState().equals(CourseState.FORTHCOMING))
			return Activator.getDefault().getImageRegistry().get("COURSE_FORTHCOMING");
		else if (course.getState().equals(CourseState.DONE))
			return Activator.getDefault().getImageRegistry().get("COURSE_DONE");
		else if (course.getState().equals(CourseState.ANNULATED))
			return Activator.getDefault().getImageRegistry().get("COURSE_ANNULATED");
		else
			return null;
	}

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

		EntityMediator.addListener(Booking.class, this);
		EntityMediator.addListener(LinkPersonAddress.class, this);
		EntityMediator.addListener(Person.class, this);
		EntityMediator.addListener(Address.class, this);
		EntityMediator.addListener(Participant.class, this);
	}

	private void packColumns()
	{
		TreeColumn[] columns = this.viewer.getTree().getColumns();
		for (int i = 1; i < columns.length; i++)
			columns[i].pack();
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		if (this.getViewer().getInput() != null)
		{
			if (entity instanceof Booking)
				this.viewer.remove(entity);
			else if (entity instanceof Participant)
				this.viewer.remove(entity);
		}
	}

	@Override
	public void postPersist(final AbstractEntity entity)
	{
		if (entity instanceof Participant)
		{
			Participant participant = (Participant) entity;
			if (this.getViewer().getInput() instanceof Person)
			{
				Person person = (Person) this.getViewer().getInput();
				if (participant.getLink().getPerson().getId().equals(person.getId()))
				{
					if (person.getDefaultLink() != null)
					{
						person.getDefaultLink().addParticipant(participant);
					}
					this.getViewer().refresh();
					this.packColumns();
				}
			}
			else if (this.getViewer().getInput() instanceof LinkPersonAddress)
			{
				LinkPersonAddress link = (LinkPersonAddress) this.getViewer().getInput();
				if (participant.getLink().getId().equals(link.getId()))
				{
					link.addParticipant(participant);
					this.getViewer().refresh();
					this.packColumns();
				}
			}
		}
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
		if (entity instanceof Participant)
		{
			Participant participant = (Participant) entity;
			if (this.getViewer().getInput() instanceof Person)
			{
				Person person = (Person) this.getViewer().getInput();
				if (participant.getLink().getPerson().getId().equals(person.getId()))
				{
					this.getViewer().refresh(participant);
					this.packColumns();
				}
			}
			else if (this.getViewer().getInput() instanceof LinkPersonAddress)
			{
				LinkPersonAddress link = (LinkPersonAddress) this.getViewer().getInput();
				if (participant.getLink().getId().equals(link.getId()))
				{
					this.getViewer().refresh(participant);
					this.packColumns();
				}
			}
		}
		else if (entity instanceof Booking)
		{
			Booking booking = (Booking) entity;
			this.getViewer().refresh(booking);
			this.packColumns();
		}
	}

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection)
	{
		if (part instanceof PersonView)
		{
			PersonView view = (PersonView) part;
			if (view.getViewer().getSelection().isEmpty())
				this.setInput(null);
			else
			{
				StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
				if (!ssel.isEmpty())
				{
					this.setInput(ssel.getFirstElement());
				}
				else
					this.setInput(null);
			}
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

	public void setInput(final Object parent)
	{
		if (this.viewer != null)
		{
			this.viewer.setInput(parent);
			this.packColumns();
		}
	}

}