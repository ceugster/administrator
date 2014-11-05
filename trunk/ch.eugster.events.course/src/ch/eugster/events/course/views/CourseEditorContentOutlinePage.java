package ch.eugster.events.course.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
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
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import ch.eugster.events.course.Activator;
import ch.eugster.events.course.dialogs.CommitmentContractDialog;
import ch.eugster.events.course.editors.CourseEditor;
import ch.eugster.events.course.editors.CourseEditorInput;
import ch.eugster.events.course.wizards.BookingTypeWizard;
import ch.eugster.events.course.wizards.CourseDetailWizard;
import ch.eugster.events.course.wizards.CourseGuideWizard;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseDetail;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.ui.helpers.EmailHelper;
import ch.eugster.events.ui.views.IEntityEditorContentOutlinePage;
import ch.eugster.events.ui.wizards.WizardDialog;

public class CourseEditorContentOutlinePage extends ContentOutlinePage implements IEntityEditorContentOutlinePage,
		PropertyChangeListener, IDoubleClickListener
{
	private final CourseEditor editor;

	private final ViewerRoot root;

	public CourseEditorContentOutlinePage(final CourseEditor editor)
	{
		this.editor = editor;
		this.root = new ViewerRoot(editor);
	}

//	private void copy(final BookingType source, final BookingType target)
//	{
//		target.setAnnulationCharges(source.getAnnulationCharges());
//		target.setInserted(source.getInserted());
//		target.setCode(source.getCode());
//		target.setCourse(source.getCourse());
//		target.setDeleted(source.isDeleted());
//		target.setId(source.getId());
//		target.setMaxAge(source.getMaxAge());
//		target.setMembership(source.getMembership());
//		target.setName(source.getName());
//		target.setPrice(source.getPrice());
//		target.setUpdated(source.getUpdated());
//		target.setUser(source.getUser());
//		target.setVersion(source.getVersion());
//	}
//
//	private void copy(final CourseDetail source, final CourseDetail target)
//	{
//		target.setDeleted(source.isDeleted());
//		target.setEnd(source.getEnd());
//		target.setId(source.getId());
//		target.setInserted(source.getInserted());
//		target.setJourney(source.getJourney());
//		target.setLocation(source.getLocation());
//		target.setMeetingPoint(source.getMeetingPoint());
//		target.setStart(source.getStart());
//		target.setSubstituteEnd(source.getSubstituteEnd());
//		target.setSubstituteStart(source.getSubstituteStart());
//		target.setUpdated(source.getUpdated());
//		target.setUser(source.getUser());
//		target.setVersion(source.getVersion());
//		target.setWithSubstituteDate(source.isWithSubstituteDate());
//	}
//
//	private void copy(final CourseGuide source, final CourseGuide target)
//	{
//		target.setDeleted(source.isDeleted());
//		target.setDescription(source.getDescription());
//		target.setGuide(source.getGuide());
//		target.setGuideType(source.getGuideType());
//		target.setId(source.getId());
//		target.setInserted(source.getInserted());
//		target.setNote(source.getNote());
//		target.setPhone(source.getPhone());
//		target.setUpdated(source.getUpdated());
//		target.setUser(source.getUser());
//		target.setVersion(source.getVersion());
//		for (Compensation compensation : source.getCompensations())
//		{
//			if (!compensation.isDeleted())
//			{
//				target.addCompensation(copy(compensation, Compensation.newInstance(compensation.getCourseGuide())));
//			}
//		}
//	}
//	
//	private Compensation copy(final Compensation source, Compensation target)
//	{
//		target.setAmount(source.getAmount());
//		target.setCompensationType(source.getCompensationType());
//		target.setDeleted(source.isDeleted());
//		target.setId(source.getId());
//		target.setInserted(source.getInserted());
//		target.setUpdated(source.getUpdated());
//		target.setUser(source.getUser());
//		return target;
//	}

//	private void update(final CourseGuide source, final CourseGuide target)
//	{
//		target.setDeleted(source.isDeleted());
//		target.setDescription(source.getDescription());
//		target.setGuide(source.getGuide());
//		target.setGuideType(source.getGuideType());
//		target.setId(source.getId());
//		target.setInserted(source.getInserted());
//		target.setNote(source.getNote());
//		target.setPhone(source.getPhone());
//		target.setUpdated(source.getUpdated());
//		target.setUser(source.getUser());
//		target.setVersion(source.getVersion());
//		for (Compensation compensation : source.getCompensations())
//		{
//			if (compensation.getId() == null)
//			{
//				if (!compensation.isDeleted())
//				{
//					target.addCompensation(compensation);
//				}
//			}
//			else
//			{
//				for (Compensation targetCompensation : target.getCompensations())
//				{
//					if (targetCompensation.getId().equals(compensation.getId()))
//					{
//						copy(compensation, targetCompensation);
//					}
//				}
//			}
//		}
//	}

	private IAction createAddBookingTypeAction()
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) CourseEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof BookingTypeGroup)
				{
					BookingTypeGroup bookingTypeGroup = (BookingTypeGroup) ssel.getFirstElement();
					BookingType bookingType = BookingType.newInstance(CourseEditorContentOutlinePage.this.root
							.getCourse());
					bookingType.getPropertyChangeSupport().addPropertyChangeListener(
							CourseEditorContentOutlinePage.this);
					BookingTypeWizard wizard = new BookingTypeWizard(bookingTypeGroup, bookingType);
					Shell shell = CourseEditorContentOutlinePage.this.getSite().getShell();
					WizardDialog dialog = new WizardDialog(shell, wizard);
					dialog.open();
					bookingType.getPropertyChangeSupport().removePropertyChangeListener(
							CourseEditorContentOutlinePage.this);
				}
			}
		};

		action.setText("Hinzufügen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("ADD"));
		return action;
	}

	private IAction createAddCourseDetailAction()
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) CourseEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof CourseDetailGroup)
				{
					CourseDetailGroup courseDetailGroup = (CourseDetailGroup) ssel.getFirstElement();
					CourseDetail courseDetail = CourseDetail.newInstance(CourseEditorContentOutlinePage.this.root
							.getCourse());
					courseDetail.getPropertyChangeSupport().addPropertyChangeListener(
							CourseEditorContentOutlinePage.this);
					CourseDetailWizard wizard = new CourseDetailWizard(courseDetailGroup, courseDetail);
					Shell shell = CourseEditorContentOutlinePage.this.getSite().getShell();
					WizardDialog dialog = new WizardDialog(shell, wizard);
					dialog.open();
					courseDetail.getPropertyChangeSupport().removePropertyChangeListener(
							CourseEditorContentOutlinePage.this);
				}
			}
		};

		action.setText("Hinzufügen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("ADD"));
		return action;
	}

	private IAction createAddCourseGuideAction()
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) CourseEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof CourseGuideGroup)
				{
					CourseGuideGroup courseGuideGroup = (CourseGuideGroup) ssel.getFirstElement();
					CourseGuide courseGuide = CourseGuide.newInstance(CourseEditorContentOutlinePage.this.root
							.getCourse());
					courseGuide.getPropertyChangeSupport().addPropertyChangeListener(
							CourseEditorContentOutlinePage.this);
					CourseGuideWizard wizard = new CourseGuideWizard(courseGuideGroup, courseGuide);
					Shell shell = CourseEditorContentOutlinePage.this.getSite().getShell();
					WizardDialog dialog = new WizardDialog(shell, wizard);
					dialog.open();
					courseGuide.getPropertyChangeSupport().removePropertyChangeListener(
							CourseEditorContentOutlinePage.this);
				}
			}
		};

		action.setText("Hinzufügen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("ADD"));
		return action;
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
				StructuredSelection ssel = (StructuredSelection) CourseEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				Object object = ssel.getFirstElement();
				if (object instanceof BookingTypeGroup)
				{
					manager.add(CourseEditorContentOutlinePage.this.createAddBookingTypeAction());
				}
				else if (object instanceof BookingType)
				{
					manager.add(CourseEditorContentOutlinePage.this.createEditBookingTypeAction());
					manager.add(CourseEditorContentOutlinePage.this.createDeleteBookingTypeAction((BookingType) object));
				}
				else if (object instanceof CourseDetailGroup)
				{
					manager.add(CourseEditorContentOutlinePage.this.createAddCourseDetailAction());
				}
				else if (object instanceof CourseDetail)
				{
					manager.add(CourseEditorContentOutlinePage.this.createEditCourseDetailAction());
					manager.add(CourseEditorContentOutlinePage.this.createDeleteCourseDetailAction());
				}
				if (object instanceof CourseGuideGroup)
				{
					manager.add(CourseEditorContentOutlinePage.this.createSendEmailAction((CourseGuideGroup) object));
					manager.add(new Separator());
					manager.add(CourseEditorContentOutlinePage.this.createAddCourseGuideAction());
				}
				else if (object instanceof CourseGuide)
				{
					CourseEditorContentOutlinePage.this.createSendEmailAction((CourseGuide) object);
					manager.add(new Separator());
					manager.add(CourseEditorContentOutlinePage.this.createEditCourseGuideAction());
					manager.add(CourseEditorContentOutlinePage.this.createDeleteCourseGuideAction());
					manager.add(new Separator());
					manager.add(CourseEditorContentOutlinePage.this.createCommitmentContractAction());
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
		viewer.setContentProvider(new CourseEditorContentOutlineContentProvider());
		viewer.setLabelProvider(new CourseEditorContentOutlineLabelProvider());
		viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		viewer.setSorter(new CourseEditorContentOutlineSorter());
		viewer.setInput(this.root);
		viewer.addDoubleClickListener(this);
		this.createContextMenu();
	}

	public void setInput(ViewerRoot root)
	{
		this.getTreeViewer().setInput(root);
	}

	private IAction createDeleteBookingTypeAction(final BookingType bookingType)
	{
		Action action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) CourseEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof BookingType)
				{
					BookingType bookingType = (BookingType) ssel.getFirstElement();
					bookingType.getPropertyChangeSupport().addPropertyChangeListener(
							CourseEditorContentOutlinePage.this);
					CourseEditorContentOutlinePage.this.root.getBookingTypeGroup().removeBookingType(bookingType);
					bookingType.getPropertyChangeSupport().removePropertyChangeListener(
							CourseEditorContentOutlinePage.this);
				}
			}
		};

		Collection<Booking> bookings = bookingType.getCourse().getBookings();
		{
			for (Booking booking : bookings)
			{
				Collection<Participant> participants = booking.getParticipants();
				for (Participant participant : participants)
				{
					if (participant.getBookingType() != null
							&& participant.getBookingType().getId().equals(bookingType.getId()))
						action.setEnabled(false);
				}
			}
		}
		action.setText("Entfernen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("DELETE"));
		return action;
	}

	private IAction createDeleteCourseDetailAction()
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) CourseEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof CourseDetail)
				{
					CourseDetail courseDetail = (CourseDetail) ssel.getFirstElement();
					courseDetail.getPropertyChangeSupport().addPropertyChangeListener(
							CourseEditorContentOutlinePage.this);
					CourseEditorContentOutlinePage.this.root.getCourseDetailGroup().removeCourseDetail(courseDetail);
					courseDetail.getPropertyChangeSupport().removePropertyChangeListener(
							CourseEditorContentOutlinePage.this);
				}
			}
		};

		action.setText("Entfernen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("DELETE"));
		return action;
	}

	private IAction createDeleteCourseGuideAction()
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) CourseEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof CourseGuide)
				{
					CourseGuide courseGuide = (CourseGuide) ssel.getFirstElement();
					courseGuide.getPropertyChangeSupport().addPropertyChangeListener(
							CourseEditorContentOutlinePage.this);
					CourseEditorContentOutlinePage.this.root.getCourseGuideGroup().removeCourseGuide(courseGuide);
					courseGuide.getPropertyChangeSupport().removePropertyChangeListener(
							CourseEditorContentOutlinePage.this);
				}
			}
		};

		action.setText("Entfernen");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("DELETE"));
		return action;
	}

	private IAction createCommitmentContractAction()
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) CourseEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof CourseGuide)
				{
					CommitmentContractDialog dialog = new CommitmentContractDialog(CourseEditorContentOutlinePage.this.getSite().getShell(), ssel);
					dialog.open();
				}
			}
		};

		action.setText("Einsatzvertrag generieren");
//		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("DELETE"));
		return action;
	}

	private IAction createEditBookingTypeAction()
	{
		Action action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) CourseEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof BookingType)
				{
					BookingTypeGroup bookingTypeGroup = CourseEditorContentOutlinePage.this.root.getBookingTypeGroup();
					BookingType bookingType = (BookingType) ssel.getFirstElement();
					bookingType.getPropertyChangeSupport().addPropertyChangeListener(
							CourseEditorContentOutlinePage.this);
					BookingTypeWizard wizard = new BookingTypeWizard(bookingTypeGroup, bookingType);
					Shell shell = CourseEditorContentOutlinePage.this.getSite().getShell();
					WizardDialog dialog = new WizardDialog(shell, wizard);
					dialog.open();
					bookingType.getPropertyChangeSupport().removePropertyChangeListener(
							CourseEditorContentOutlinePage.this);
				}
			}
		};

		action.setText("Bearbeiten");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("EDIT"));
		return action;
	}

	private IAction createEditCourseDetailAction()
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) CourseEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof CourseDetail)
				{
					CourseDetailGroup courseDetailGroup = CourseEditorContentOutlinePage.this.root
							.getCourseDetailGroup();
					CourseDetail courseDetail = (CourseDetail) ssel.getFirstElement();
					courseDetail.getPropertyChangeSupport().addPropertyChangeListener(
							CourseEditorContentOutlinePage.this);
					CourseDetailWizard wizard = new CourseDetailWizard(courseDetailGroup, courseDetail);
					Shell shell = CourseEditorContentOutlinePage.this.getSite().getShell();
					WizardDialog dialog = new WizardDialog(shell, wizard);
					dialog.open();
					courseDetail.getPropertyChangeSupport().removePropertyChangeListener(
							CourseEditorContentOutlinePage.this);
				}
			}
		};

		action.setText("Bearbeiten");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("EDIT"));
		return action;
	}

	private IAction createEditCourseGuideAction()
	{
		IAction action = new Action()
		{
			@Override
			public void run()
			{
				super.run();
				StructuredSelection ssel = (StructuredSelection) CourseEditorContentOutlinePage.this.getTreeViewer()
						.getSelection();
				if (!ssel.isEmpty() && ssel.getFirstElement() instanceof CourseGuide)
				{
					CourseGuideGroup courseGuideGroup = CourseEditorContentOutlinePage.this.root.getCourseGuideGroup();
					CourseGuide courseGuide = (CourseGuide) ssel.getFirstElement();
					courseGuide.getPropertyChangeSupport().addPropertyChangeListener(
							CourseEditorContentOutlinePage.this);
					CourseGuideWizard wizard = new CourseGuideWizard(courseGuideGroup, courseGuide);
					Shell shell = CourseEditorContentOutlinePage.this.getSite().getShell();
					WizardDialog dialog = new WizardDialog(shell, wizard);
					int result = dialog.open();
					courseGuide.getPropertyChangeSupport().removePropertyChangeListener(
							CourseEditorContentOutlinePage.this);
					if (result == IDialogConstants.OK_ID)
					{
						CourseEditorContentOutlinePage.this.editor.setDirty(true);
					}
				}
			}
		};

		action.setText("Bearbeiten");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("EDIT"));
		return action;
	}

	private IAction createSendEmailAction(final CourseGuide courseGuide)
	{
		return this.createSendEmailAction(new CourseGuide[] { courseGuide });
	}

	private IAction createSendEmailAction(final CourseGuide[] courseGuides)
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
					for (CourseGuide courseGuide : courseGuides)
					{
						if (EmailHelper.getInstance().isValidAddress(
								courseGuide.getGuide().getLink().getPerson().getEmail()))
							emails.add(courseGuide.getGuide().getLink().getPerson().getEmail());
						else if (EmailHelper.getInstance().isValidAddress(courseGuide.getGuide().getLink().getEmail()))
							emails.add(courseGuide.getGuide().getLink().getEmail());
					}
					EmailHelper.getInstance().sendEmail(emails.toArray(new String[0]));
				}
			}
		};

		action.setEnabled(EmailHelper.getInstance().isEmailSupported());
		action.setText("Email senden");
		action.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("EMAIL"));
		return action;
	}

	private IAction createSendEmailAction(final CourseGuideGroup courseGuideGroup)
	{
		Collection<CourseGuide> courseGuides = new ArrayList<CourseGuide>();
		for (CourseGuide courseGuide : courseGuideGroup.getCourseGuides())
		{
			if (!courseGuide.isDeleted())
				courseGuides.add(courseGuide);
		}
		return this.createSendEmailAction(courseGuides.toArray(new CourseGuide[0]));
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		if (!event.getSelection().isEmpty())
		{
			StructuredSelection ssel = (StructuredSelection) event.getSelection();
			if (ssel.getFirstElement() instanceof BookingType)
			{
				this.createEditBookingTypeAction().run();
			}
			else if (ssel.getFirstElement() instanceof CourseDetail)
			{
				this.createEditCourseDetailAction().run();
			}
			else if (ssel.getFirstElement() instanceof CourseGuide)
			{
				this.createEditCourseGuideAction().run();
			}
		}
	}

	public CourseEditor getEditor()
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
		this.getTreeViewer().refresh();
	}

	@Override
	public void update()
	{
//		this.root.getBookingTypeGroup().updateBookingTypes();
//		this.root.getCourseDetailGroup().updateCourseDetails();
//		this.root.getCourseGuideGroup().updateCourseGuides();
	}

	public class BookingTypeGroup extends Group
	{
//		private final Collection<BookingType> bookingTypes = new Vector<BookingType>();

		public BookingTypeGroup(final ViewerRoot root)
		{
			super(root);
//			bookin= root.getCourse().getBookingTypes();
//			for (BookingType source : sources)
//			{
//				if (!source.isDeleted())
//				{
//					BookingType target = BookingType.newInstance();
////					CourseEditorContentOutlinePage.this.copy(source, target);
////					this.bookingTypes.add(target);
//				}
//			}
		}

		public void addBookingType(final BookingType bookingType)
		{
			this.root.getCourse().addBookingType(bookingType);
//			this.bookingTypes.add(bookingType);
			this.firePropertyChange(new PropertyChangeEvent(this, "bookingTypeAdded", this, this));
		}

		public Collection<BookingType> getBookingTypes()
		{
			return this.root.getCourse().getBookingTypes();
		}

		public void removeBookingType(final BookingType bookingType)
		{
			if (bookingType.getId() == null)
				this.root.getCourse().getBookingTypes().remove(bookingType);
			else
			{
				bookingType.setDeleted(true);
			}
			this.firePropertyChange(new PropertyChangeEvent(this, "bookingTypeRemoved", this, this));
		}

		@Override
		public String toString()
		{
			return "Buchungsarten (" + this.root.getCourse().getBookingTypes().size() + ")";
		}

//		public void updateBookingTypes()
//		{
//			Map<Long, BookingType> map = new HashMap<Long, BookingType>();
//			for (BookingType bookingType : this.root.getCourse().getBookingTypes())
//			{
//				map.put(bookingType.getId(), bookingType);
//			}
//			for (BookingType bookingType : this.bookingTypes)
//			{
//				if (bookingType.getId() == null)
//				{
//					this.root.getCourse().addBookingType(bookingType);
//				}
//				else
//				{
//					BookingType oldBookingType = map.get(bookingType.getId());
//					copy(bookingType, oldBookingType);
//				}
//			}
//		}

	}

	public class CourseDetailGroup extends Group
	{
//		private final Collection<CourseDetail> courseDetails = new Vector<CourseDetail>();

		public CourseDetailGroup(final ViewerRoot root)
		{
			super(root);
//			Collection<CourseDetail> sources = root.getCourse().getCourseDetails();
//			for (CourseDetail source : sources)
//			{
//				if (!source.isDeleted())
//				{
//					CourseDetail target = CourseDetail.newInstance(source.getCourse());
//					CourseEditorContentOutlinePage.this.copy(source, target);
//					this.courseDetails.add(target);
//				}
//			}
		}

		public void addCourseDetail(final CourseDetail courseDetail)
		{
			this.root.getCourse().addCourseDetail(courseDetail);
			this.firePropertyChange(new PropertyChangeEvent(this, "courseDetailAdded", this, this));
		}

		public Collection<CourseDetail> getCourseDetails()
		{
			return this.root.getCourse().getCourseDetails();
		}

		public void removeCourseDetail(final CourseDetail courseDetail)
		{
			if (courseDetail.getId() == null)
				this.root.getCourse().getCourseDetails().remove(courseDetail);
			else
			{
				courseDetail.setDeleted(true);
			}
			this.firePropertyChange(new PropertyChangeEvent(this, "courseDetailRemoved", this, this));
		}

		@Override
		public String toString()
		{
			return "Kursdaten (" + this.root.getCourse().getCourseDetails().size() + ")";
		}

//		public void updateCourseDetails()
//		{
//			Map<Long, CourseDetail> map = new HashMap<Long, CourseDetail>();
//			for (CourseDetail courseDetail : this.root.getCourse().getCourseDetails())
//			{
//				map.put(courseDetail.getId(), courseDetail);
//			}
//			for (CourseDetail courseDetail : this.courseDetails)
//			{
//				if (courseDetail.getId() == null)
//				{
//					this.root.getCourse().addCourseDetail(courseDetail);
//				}
//				else
//				{
//					CourseDetail oldCourseDetail = map.get(courseDetail.getId());
//					copy(courseDetail, oldCourseDetail);
//				}
//			}
//		}

	}

	private class CourseEditorContentOutlineContentProvider implements ITreeContentProvider
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
				if (parentElement instanceof BookingTypeGroup)
					return CourseEditorContentOutlinePage.this.root.getBookingTypeGroup().getBookingTypes()
							.toArray(new BookingType[0]);
				else if (parentElement instanceof CourseDetailGroup)
					return CourseEditorContentOutlinePage.this.root.getCourseDetailGroup().getCourseDetails()
							.toArray(new CourseDetail[0]);
				else if (parentElement instanceof CourseGuideGroup)
					return CourseEditorContentOutlinePage.this.root.getCourseGuideGroup().getCourseGuides()
							.toArray(new CourseGuide[0]);
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
			if (element instanceof BookingType)
				return CourseEditorContentOutlinePage.this.root.getBookingTypeGroup();
			else if (element instanceof CourseDetail)
				return CourseEditorContentOutlinePage.this.root.getCourseDetailGroup();
			else if (element instanceof CourseGuide)
				return CourseEditorContentOutlinePage.this.root.getCourseGuideGroup();
			return null;
		}

		@Override
		public boolean hasChildren(final Object element)
		{
			if (element.equals(CourseEditorContentOutlinePage.this.root.getBookingTypeGroup()))
			{
				return CourseEditorContentOutlinePage.this.root.getBookingTypeGroup().getBookingTypes().size() > 0;
			}
			else if (element.equals(CourseEditorContentOutlinePage.this.root.getCourseDetailGroup()))
			{
				return CourseEditorContentOutlinePage.this.root.getCourseDetailGroup().getCourseDetails().size() > 0;
			}
			else if (element.equals(CourseEditorContentOutlinePage.this.root.getCourseGuideGroup()))
			{
				return CourseEditorContentOutlinePage.this.root.getCourseGuideGroup().getCourseGuides().size() > 0;
			}
			return false;
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
		{
		}

	}

	private class CourseEditorContentOutlineLabelProvider extends LabelProvider
	{
		@Override
		public Image getImage(final Object element)
		{
			if (element instanceof BookingTypeGroup)
				return Activator.getDefault().getImageRegistry().get("BOOKING_TYPE");
			else if (element instanceof CourseDetailGroup)
				return Activator.getDefault().getImageRegistry().get("COURSE_DETAIL");
			else if (element instanceof CourseGuideGroup)
				return Activator.getDefault().getImageRegistry().get("COURSE_GUIDE");
			else if (element instanceof BookingType)
				return Activator.getDefault().getImageRegistry().get("BOOKING_TYPE");
			else if (element instanceof CourseDetail)
				return Activator.getDefault().getImageRegistry().get("COURSE_DETAIL");
			else if (element instanceof CourseGuide)
				return Activator.getDefault().getImageRegistry().get("COURSE_GUIDE");

			return super.getImage(element);
		}

		@Override
		public String getText(final Object element)
		{
			if (element instanceof BookingType)
				return CourseFormatter.getInstance().formatBookingType((BookingType) element);
			else if (element instanceof CourseDetail)
				return CourseFormatter.getInstance().formatComboEntry((CourseDetail) element);
			else if (element instanceof CourseGuide)
				return CourseFormatter.getInstance().formatComboEntry((CourseGuide) element);

			return super.getText(element);
		}

	}

	private class CourseEditorContentOutlineSorter extends ViewerSorter
	{
		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2)
		{
			if (e1 instanceof BookingType && e2 instanceof BookingType)
			{
				BookingType b1 = (BookingType) e1;
				BookingType b2 = (BookingType) e2;
				return Double.valueOf(b1.getPrice()).compareTo(Double.valueOf(b2.getPrice()));
			}
			else if (e1 instanceof CourseDetail && e2 instanceof CourseDetail)
			{
				CourseDetail c1 = (CourseDetail) e1;
				CourseDetail c2 = (CourseDetail) e2;
				if (c1.getStart() != null && c2.getStart() != null)
				{
					return c1.getStart().compareTo(c2.getStart());
				}
				else if (c1.getStart() == null)
				{
					if (c2.getStart() == null)
						return 0;
					else
						return -1;
				}
				else if (c2.getStart() == null)
				{
					if (c1.getStart() == null)
						return 0;
					else
						return 1;
				}
				else
					return 0;
			}
			else if (e1 instanceof CourseGuide && e2 instanceof CourseGuide)
			{
				CourseGuide g1 = (CourseGuide) e1;
				CourseGuide g2 = (CourseGuide) e2;
				String p1 = PersonFormatter.getInstance().formatLastnameFirstname(g1.getGuide().getLink().getPerson());
				String p2 = PersonFormatter.getInstance().formatLastnameFirstname(g2.getGuide().getLink().getPerson());
				return p1.compareTo(p2);

			}
			return super.compare(viewer, e1, e2);
		}

	}

	public class CourseGuideGroup extends Group
	{
//		private final Collection<CourseGuide> courseGuides = new Vector<CourseGuide>();

		public CourseGuideGroup(final ViewerRoot root)
		{
			super(root);
//			Collection<CourseGuide> sources = root.getCourse().getCourseGuides();
//			for (CourseGuide source : sources)
//			{
//				if (!source.isDeleted())
//				{
//					CourseGuide target = CourseGuide.newInstance(source.getCourse());
//					CourseEditorContentOutlinePage.this.copy(source, target);
//					this.courseGuides.add(target);
//				}
//			}
		}

		public void addCourseGuide(final CourseGuide courseGuide)
		{
			this.root.getCourse().addCourseGuide(courseGuide);
			this.firePropertyChange(new PropertyChangeEvent(this, "courseGuideAdded", this, this));
		}

		public Collection<CourseGuide> getCourseGuides()
		{
			return this.root.getCourse().getCourseGuides();
		}

		public void removeCourseGuide(final CourseGuide courseGuide)
		{
			if (courseGuide.getId() == null)
				this.root.getCourse().getCourseGuides().remove(courseGuide);
			else
			{
				courseGuide.setDeleted(true);
			}
			this.firePropertyChange(new PropertyChangeEvent(this, "courseGuideRemoved", this, this));
		}

		@Override
		public String toString()
		{
			return "Kursleitung (" + this.root.getCourse().getCourseGuides().size() + ")";
		}

//		public void updateCourseGuides()
//		{
//			Map<Long, CourseGuide> map = new HashMap<Long, CourseGuide>();
//			for (CourseGuide courseGuide : this.root.getCourse().getCourseGuides())
//			{
//				map.put(courseGuide.getId(), courseGuide);
//			}
//			for (CourseGuide courseGuide : this.courseGuides)
//			{
//				if (courseGuide.getId() == null)
//				{
//					this.root.getCourse().addCourseGuide(courseGuide);
//				}
//				else
//				{
//					CourseGuide oldCourseGuide = map.get(courseGuide.getId());
//					update(courseGuide, oldCourseGuide);
//				}
//			}
//		}
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

	public class ViewerRoot
	{
		private CourseEditor editor;

		private BookingTypeGroup bookingTypeGroup;

		private CourseDetailGroup courseDetailGroup;

		private CourseGuideGroup courseGuideGroup;

		public ViewerRoot(final CourseEditor editor)
		{
			this.editor = editor;
			this.bookingTypeGroup = new BookingTypeGroup(this);
			this.bookingTypeGroup.addPropertyChangeListener(CourseEditorContentOutlinePage.this);
			this.courseDetailGroup = new CourseDetailGroup(this);
			this.courseDetailGroup.addPropertyChangeListener(CourseEditorContentOutlinePage.this);
			this.courseGuideGroup = new CourseGuideGroup(this);
			this.courseGuideGroup.addPropertyChangeListener(CourseEditorContentOutlinePage.this);
		}

		public BookingTypeGroup getBookingTypeGroup()
		{
			return this.bookingTypeGroup;
		}

		public Course getCourse()
		{
			CourseEditorInput input = (CourseEditorInput) editor.getEditorInput();
			return input.getEntity();
		}

		public CourseDetailGroup getCourseDetailGroup()
		{
			return this.courseDetailGroup;
		}

		public CourseGuideGroup getCourseGuideGroup()
		{
			return this.courseGuideGroup;
		}

		public Group[] getGroups()
		{
			return new Group[] { this.bookingTypeGroup, this.courseDetailGroup, this.courseGuideGroup };
		}

	}
}
