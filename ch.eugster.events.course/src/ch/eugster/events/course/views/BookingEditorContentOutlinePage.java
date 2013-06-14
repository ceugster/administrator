package ch.eugster.events.course.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import ch.eugster.events.course.Activator;
import ch.eugster.events.course.editors.BookingEditor;
import ch.eugster.events.course.editors.BookingEditorInput;
import ch.eugster.events.course.wizards.BookingWizard;
import ch.eugster.events.course.wizards.ParticipantWizardPage;
import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.ui.helpers.EmailHelper;
import ch.eugster.events.ui.views.IEntityEditorContentOutlinePage;
import ch.eugster.events.ui.wizards.WizardDialog;

public class BookingEditorContentOutlinePage extends ContentOutlinePage implements IEntityEditorContentOutlinePage,
		PropertyChangeListener
{
	private final BookingEditor editor;

	private final EntityAdapter entityAdapter;

	private ViewerRoot root;

	public BookingEditorContentOutlinePage(final BookingEditor editor)
	{
		this.editor = editor;
		this.root = new ViewerRoot(((BookingEditorInput) editor.getEditorInput()).getEntity());

		this.entityAdapter = new EntityAdapter()
		{
			@Override
			public void postDelete(final AbstractEntity entity)
			{
				if (entity instanceof Participant)
				{
					Participant participant = (Participant) entity;
					if (BookingEditorContentOutlinePage.this.root.getBooking().getId() != null
							&& BookingEditorContentOutlinePage.this.root.getBooking().getId()
									.equals(participant.getBooking().getId()))
					{
						BookingEditorContentOutlinePage.this.root.getParticipantGroup().removeParticipant(
								(Participant) entity);
						BookingEditorContentOutlinePage.this.getTreeViewer().refresh();
					}
				}
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				if (entity instanceof Participant)
				{
					Participant participant = (Participant) entity;
					if (BookingEditorContentOutlinePage.this.root.getBooking().getId() != null
							&& BookingEditorContentOutlinePage.this.root.getBooking().getId()
									.equals(participant.getBooking().getId()))
					{
						BookingEditorContentOutlinePage.this.root.getParticipantGroup().updateBooking(
								(Participant) entity);
					}
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof Participant)
				{
					Participant participant = (Participant) entity;
					if (BookingEditorContentOutlinePage.this.root.getBooking().getId() != null
							&& BookingEditorContentOutlinePage.this.root.getBooking().getId()
									.equals(participant.getBooking().getId()))
					{
					}
				}
			}
		};
		EntityMediator.addListener(Participant.class, this.entityAdapter);
	}

	private void createAddParticipantAction(final IMenuManager manager, final ParticipantGroup participantGroup)
	{
		Action action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) BookingEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof ParticipantGroup)
				{
					Participant participant = Participant.newInstance(BookingEditorContentOutlinePage.this.root
							.getBooking());
					participant.getPropertyChangeSupport().addPropertyChangeListener(
							BookingEditorContentOutlinePage.this);
					BookingWizard wizard = new BookingWizard(BookingEditorContentOutlinePage.this.root.getBooking());
					wizard.addPage(new ParticipantWizardPage("participantWizardPage", wizard));
					Shell shell = BookingEditorContentOutlinePage.this.getSite().getShell();
					WizardDialog dialog = new WizardDialog(shell, wizard);
					dialog.open();
					participant.getPropertyChangeSupport().removePropertyChangeListener(
							BookingEditorContentOutlinePage.this);
				}
			}
		};

		action.setText("Hinzufügen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("ADD"));
		manager.add(action);
	}

	private void createBookingTypeAction(final IMenuManager manager, final Participant participant,
			final BookingType bookingType)
	{
		Action action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) BookingEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof Participant)
				{
					Participant participant = (Participant) ssel.getFirstElement();
					participant.getPropertyChangeSupport().addPropertyChangeListener(
							BookingEditorContentOutlinePage.this);
					participant.setBookingType(bookingType);
					participant.getPropertyChangeSupport().removePropertyChangeListener(
							BookingEditorContentOutlinePage.this);
				}
			}
		};

		action.setText(CourseFormatter.getInstance().formatComboEntry(bookingType));
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("EDIT"));
		manager.add(action);
	}

	protected void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener()
		{
			@Override
			public void menuAboutToShow(final IMenuManager manager)
			{
				StructuredSelection ssel = (StructuredSelection) BookingEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				Object object = ssel.getFirstElement();
				if (object instanceof ParticipantGroup)
				{
					BookingEditorContentOutlinePage.this.createSendEmailAction(manager, (ParticipantGroup) object);
					manager.add(new Separator());
					BookingEditorContentOutlinePage.this.createAddParticipantAction(manager, (ParticipantGroup) object);
				}
				else if (object instanceof Participant)
				{
					Participant participant = (Participant) object;
					/*
					 * Menu: Email senden
					 */
					BookingEditorContentOutlinePage.this.createSendEmailAction(manager, (Participant) object);
					manager.add(new Separator());

					Course course = BookingEditorContentOutlinePage.this.root.getBooking().getCourse();
					if (course.getBookingTypes().size() > 1)
					{
						IMenuManager subMenu = new MenuManager("Buchungsart ändern");
						Collection<BookingType> bookingTypes = course.getBookingTypes();
						for (BookingType bookingType : bookingTypes)
						{
							if (participant.getBookingType() == null
									|| !bookingType.getId().equals(participant.getBookingType().getId()))
								BookingEditorContentOutlinePage.this.createBookingTypeAction(subMenu, participant,
										bookingType);
						}
						manager.add(subMenu);
						manager.add(new Separator());
					}

					BookingEditorContentOutlinePage.this.createDeleteParticipantAction(manager, (Participant) object);
				}
			}
		});

		Menu menu = menuManager.createContextMenu(this.getTreeViewer().getControl());
		this.getTreeViewer().getControl().setMenu(menu);

		this.getSite().registerContextMenu(ContentOutline.PREFIX, menuManager, this.getTreeViewer());
	}

	@Override
	public void createControl(final Composite parent)
	{
		super.createControl(parent);

		TreeViewer viewer = this.getTreeViewer();
		viewer.setContentProvider(new BookingEditorContentOutlineContentProvider());
		viewer.setLabelProvider(new BookingEditorContentOutlineLabelProvider());
		viewer.setSorter(new BookingEditorContentOutlineSorter());
		viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		viewer.setInput(this.root);
		this.createContextMenu();
	}

	private void createDeleteParticipantAction(final IMenuManager manager, final Participant participant)
	{
		Action action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) BookingEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof Participant)
				{
					Participant participant = (Participant) ssel.getFirstElement();
					participant.getPropertyChangeSupport().addPropertyChangeListener(
							BookingEditorContentOutlinePage.this);
					BookingEditorContentOutlinePage.this.root.getParticipantGroup().removeParticipant(participant);
					participant.getPropertyChangeSupport().removePropertyChangeListener(
							BookingEditorContentOutlinePage.this);
				}
			}
		};

		action.setText("Entfernen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("DELETE"));
		manager.add(action);
	}

	private void createSendEmailAction(final IMenuManager manager, final Participant participant)
	{
		this.createSendEmailAction(manager, new Participant[] { participant });
	}

	private void createSendEmailAction(final IMenuManager manager, final Participant[] participants)
	{
		Action action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				if (EmailHelper.getInstance().isEmailSupported())
				{
					Collection<String> emails = new ArrayList<String>();
					for (Participant participant : participants)
					{
						if (EmailHelper.getInstance().isValidAddress(participant.getLink().getPerson().getEmail()))
							emails.add(participant.getLink().getPerson().getEmail());
						else if (EmailHelper.getInstance().isValidAddress(participant.getLink().getEmail()))
							emails.add(participant.getLink().getEmail());
					}
					EmailHelper.getInstance().sendEmail(emails.toArray(new String[0]));
				}
			}
		};

		action.setEnabled(EmailHelper.getInstance().isEmailSupported());
		action.setText("Email senden");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("EMAIL"));
		manager.add(action);
	}

	private void createSendEmailAction(final IMenuManager manager, final ParticipantGroup participantGroup)
	{
		Collection<Participant> participants = new ArrayList<Participant>();
		for (Participant participant : participantGroup.getParticipants())
		{
			if (!participant.isDeleted())
				participants.add(participant);
		}
		this.createSendEmailAction(manager, participants.toArray(new Participant[0]));
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Participant.class, this.entityAdapter);
		super.dispose();
	}

	public BookingEditor getEditor()
	{
		return this.editor;
	}

	public ViewerRoot getRoot()
	{
		return this.root;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event)
	{
		this.editor.propertyChange(event);
		this.getTreeViewer().refresh(event.getSource());
	}

	private void refresh(final Object object)
	{
		UIJob job = new UIJob("Aktualisiere Outline...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				BookingEditorContentOutlinePage.this.getTreeViewer().refresh(object);
				return Status.OK_STATUS;
			}

		};
		job.setUser(true);
		job.schedule();
	}

	@Override
	public void update()
	{
		this.root.getParticipantGroup().updateParticipants();
	}

	private class BookingEditorContentOutlineContentProvider implements ITreeContentProvider
	{
		@Override
		public void dispose()
		{
		}

		@Override
		public Object[] getChildren(final Object parentElement)
		{
			if (parentElement instanceof ViewerRoot)
			{
				return ((ViewerRoot) parentElement).getGroups();
			}
			else if (parentElement instanceof Group)
			{
				if (parentElement instanceof ParticipantGroup)
					return BookingEditorContentOutlinePage.this.root.getParticipantGroup().getParticipants()
							.toArray(new Participant[0]);
			}
			return new String[0];
		}

		@Override
		public Object[] getElements(final Object inputElement)
		{
			return this.getChildren(inputElement);
		}

		@Override
		public Object getParent(final Object element)
		{
			if (element instanceof Participant)
				return BookingEditorContentOutlinePage.this.root.getParticipantGroup();
			return null;
		}

		@Override
		public boolean hasChildren(final Object element)
		{
			if (element.equals(BookingEditorContentOutlinePage.this.root.getParticipantGroup()))
			{
				return BookingEditorContentOutlinePage.this.root.getParticipantGroup().getParticipants().size() > 0;
			}
			return false;
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
		{
		}

	}

	private class BookingEditorContentOutlineLabelProvider extends LabelProvider
	{
		@Override
		public Image getImage(final Object element)
		{
			if (element instanceof ParticipantGroup)
				return Activator.getDefault().getImageRegistry().get("BOOKING_GREEN");
			else if (element instanceof Participant)
			{
				Participant defaultParticipant = BookingEditorContentOutlinePage.this.root.getBooking()
						.getParticipant();
				if (defaultParticipant != null)
				{
					Participant participant = (Participant) element;
					if (defaultParticipant.getId() != null && participant.getId() != null)
					{
						if (defaultParticipant.getId().equals(participant.getId()))
							return Activator.getDefault().getImageRegistry().get("BOOKING_RED");
					}
				}
				return Activator.getDefault().getImageRegistry().get("BOOKING_BLUE");
			}

			return super.getImage(element);
		}

		@Override
		public String getText(final Object element)
		{
			if (element instanceof Participant)
			{
				Participant participant = (Participant) element;
				String id = PersonFormatter.getInstance().formatId(participant.getLink().getPerson());
				String name = PersonFormatter.getInstance().formatLastnameFirstname(participant.getLink().getPerson());
				String address = participant.getLink().getAddress().getAddress();
				String bookingType = null;
				if (participant.getBookingType() != null)
					bookingType = " - " + CourseFormatter.getInstance().formatComboEntry(participant.getBookingType());
				return id + " - " + name + ", " + address + (bookingType == null ? "" : " - " + bookingType) + " ("
						+ DecimalFormat.getIntegerInstance().format(participant.getCount()) + ")";
			}

			return super.getText(element);
		}

	}

	private class BookingEditorContentOutlineSorter extends ViewerSorter
	{
		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2)
		{
			if (e1 instanceof Participant && e2 instanceof Participant)
			{
				Participant p1 = (Participant) e1;
				Participant p2 = (Participant) e2;
				String name1 = PersonFormatter.getInstance().formatLastnameFirstname(p1.getLink().getPerson());
				String name2 = PersonFormatter.getInstance().formatLastnameFirstname(p2.getLink().getPerson());
				return name1.compareTo(name2);
			}
			return super.compare(viewer, e1, e2);
		}

	}

	protected abstract class Group
	{
		protected ViewerRoot root;

		private final Collection<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();

		public Group(final ViewerRoot root)
		{
			this.root = root;
		}

		public void addPropertyChangeListener(final PropertyChangeListener listener)
		{
			this.propertyChangeListeners.add(listener);
		}

		protected void firePropertyChange(final PropertyChangeEvent event)
		{
			PropertyChangeListener[] listeners = this.propertyChangeListeners.toArray(new PropertyChangeListener[0]);
			for (PropertyChangeListener listener : listeners)
				listener.propertyChange(event);
		}

		public ViewerRoot getRoot()
		{
			return this.root;
		}

		@Override
		public abstract String toString();
	}

	public class ParticipantGroup extends Group
	{
		private Booking booking;

		private Collection<Participant> participants = new ArrayList<Participant>();

		public ParticipantGroup(final ViewerRoot root, final Booking booking)
		{
			super(root);
			this.booking = booking;
		}

		public Collection<Participant> getParticipants()
		{
			return this.participants;
		}

		public void refreshParticipant(final Participant participant)
		{
			for (Participant p : this.participants)
			{
				if (p.getId() != null && participant.getId().equals(p.getId()))
				{
					Participant.copy(participant, p);
					this.firePropertyChange(new PropertyChangeEvent(this, "participantRefreshed", this, this));
					return;
				}
			}
		}

		public void removeParticipant(final Participant participant)
		{
			if (participant.getId() == null)
				this.participants.remove(participant);
			else
			{
				participant.setDeleted(true);
			}
			this.firePropertyChange(new PropertyChangeEvent(this, "bookingTypeRemoved", this, this));
		}

		@Override
		public String toString()
		{
			return "Teilnehmer (" + DecimalFormat.getIntegerInstance().format(booking.getParticipantCount()) + ")";
		}

		public void updateBooking(final Participant participant)
		{
			this.participants.add(participant);
			this.firePropertyChange(new PropertyChangeEvent(this, "participantAdded", this, this));
		}

		public void updateParticipants()
		{
			Map<Long, Participant> map = new HashMap<Long, Participant>();
			for (Participant participants : this.root.getBooking().getParticipants())
			{
				map.put(participants.getId(), participants);
			}
			for (Participant participant : this.participants)
			{
				if (participant.getId() == null)
				{
					this.root.getBooking().addParticipant(participant);
				}
				else
				{
					Participant oldParticipant = map.get(participant.getId());
					Participant.copy(participant, oldParticipant);
				}
			}
		}

	}

	public class ViewerRoot
	{
		private final Booking booking;

		private final ParticipantGroup participantGroup;

		public ViewerRoot(final Booking booking)
		{
			this.booking = booking;
			this.participantGroup = new ParticipantGroup(this, booking);
			this.participantGroup.addPropertyChangeListener(BookingEditorContentOutlinePage.this);
		}

		public Booking getBooking()
		{
			return this.booking;
		}

		public Group[] getGroups()
		{
			return new Group[] { this.participantGroup };
		}

		public ParticipantGroup getParticipantGroup()
		{
			return this.participantGroup;
		}
	}
}
