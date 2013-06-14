package ch.eugster.events.course.wizards;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.course.Constants;
import ch.eugster.events.course.views.AnnulatedCoursesFilter;
import ch.eugster.events.course.views.CourseContentProvider;
import ch.eugster.events.course.views.CourseFilter;
import ch.eugster.events.course.views.CourseSorter;
import ch.eugster.events.course.views.DoneCoursesFilter;
import ch.eugster.events.course.views.SeasonFilter;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.Participant;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.service.ConnectionService;

public class CourseWizardPage extends WizardPage implements IDoubleClickListener, ISelectionChangedListener
{
	private final Booking booking;

	private Text filterText;

	private Button filterButton;

	private TreeViewer viewer;

	private Label imageLabel;

	private Label messageLabel;

	private IDialogSettings dialogSettings;

	private final DateFormat df = SimpleDateFormat.getDateInstance();

	private AlreadyParticipantFilter alreadyParticipantFilter = null;

	private final Collection<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();

	private ServiceTracker connectionServiceTracker;

	public CourseWizardPage(final String name, final Booking booking)
	{
		super(name);
		this.booking = booking;
		this.dialogSettings = Activator.getDefault().getDialogSettings()
				.getSection(Constants.DIALOG_SETTINGS_KEY_COURSE_VIEW_SECTION_ID);
		if (this.dialogSettings == null)
		{
			this.dialogSettings = Activator.getDefault().getDialogSettings()
					.addNewSection(Constants.DIALOG_SETTINGS_KEY_COURSE_VIEW_SECTION_ID);
			this.dialogSettings.put(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_BUTTON_SELECTED_ID, false);
			this.dialogSettings.put(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_TEXT_VALUE_ID, "");
		}
	}

	public void addSelectionChangedListener(final ISelectionChangedListener listener)
	{
		this.selectionChangedListeners.add(listener);
	}

	@Override
	public boolean canFlipToNextPage()
	{
		return this.isPageComplete();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createControl(final Composite parent)
	{
		this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("BOOKING_48"));
		this.setTitle("Auswahl Kurse");
		this.setMessage("Auswahl der noch nicht abgeschlossenen Kurse");

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		Composite filterComposite = new Composite(composite, SWT.NONE);
		filterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterComposite.setLayout(new GridLayout(3, false));

		Label label = new Label(filterComposite, SWT.NONE);
		label.setText("Filter");
		label.setLayoutData(new GridData());

		this.filterText = new Text(filterComposite, SWT.BORDER);
		this.filterText.setText(this.dialogSettings.get(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_TEXT_VALUE_ID));
		this.filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.filterText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				CourseWizardPage.this.dialogSettings.put(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_TEXT_VALUE_ID,
						CourseWizardPage.this.filterText.getText());
				CourseWizardPage.this.viewer.refresh();
			}
		});

		this.filterButton = new Button(filterComposite, SWT.TOGGLE);
		this.filterButton.setImage(Activator.getDefault().getImageRegistry().get("FILTER"));
		this.filterButton.setSelection(this.dialogSettings
				.getBoolean(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_BUTTON_SELECTED_ID));
		this.filterButton.setLayoutData(new GridData());
		this.filterButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseWizardPage.this.dialogSettings.put(
						Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_BUTTON_SELECTED_ID,
						CourseWizardPage.this.filterButton.getSelection());
				CourseWizardPage.this.viewer.refresh();
			}
		});

		final Tree tree = new Tree(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setHeaderVisible(true);
		tree.addListener(SWT.Expand, new Listener()
		{
			@Override
			public void handleEvent(final Event event)
			{
				CourseWizardPage.this.getControl().getDisplay().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						if (tree.isDisposed())
							return;
						CourseWizardPage.this.packColumns();
					}
				});
			}
		});

		this.viewer = new TreeViewer(tree);
		this.viewer.setContentProvider(new CourseContentProvider());
		this.viewer.setSorter(new CourseSorter());

		SeasonFilter seasonFilter = new SeasonFilter(this.viewer);
		seasonFilter.doFilter(false);
		DeletedEntityFilter deletedFilter = new DeletedEntityFilter();
		CourseFilter courseFilter = new CourseFilter(this.filterText, this.filterButton);
		DoneCoursesFilter doneCoursesFilter = new DoneCoursesFilter(this.viewer);
		doneCoursesFilter.doFilter(false);
		AnnulatedCoursesFilter annulatedCoursesFilter = new AnnulatedCoursesFilter(this.viewer);
		annulatedCoursesFilter.doFilter(false);
		this.alreadyParticipantFilter = new AlreadyParticipantFilter(this.viewer, this.booking);

		ViewerFilter[] filters = new ViewerFilter[] { seasonFilter, deletedFilter, courseFilter, doneCoursesFilter,
				annulatedCoursesFilter, this.alreadyParticipantFilter };

		this.viewer.setFilters(filters);
		this.viewer.addDoubleClickListener(this);
		this.viewer.addSelectionChangedListener(this);

		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Season)
				{
					Season season = (Season) cell.getElement();
					cell.setImage(Activator.getDefault().getImageRegistry().get(season.getImageKey()));
					cell.setText(season.getCode());
				}
				else if (cell.getElement() instanceof Course)
				{
					Course course = (Course) cell.getElement();
					cell.setImage(Activator.getDefault().getImageRegistry().get(course.getState().imageKey()));
					cell.setText(course.getCode());
				}
			}

		});
		GC gc = new GC(tree);
		FontMetrics fm = gc.getFontMetrics();
		int charWidth = fm.getAverageCharWidth();
		int columnWidth = charWidth * 30;

		TreeColumn treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Code");
		treeColumn.setWidth(columnWidth);
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Course)
				{
					Course course = (Course) cell.getElement();
					cell.setText(course.getTitle());
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Bezeichnung");
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.CENTER);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Course)
				{
					Course course = (Course) cell.getElement();
					if (course.getFirstDate() == null)
					{
						cell.setText("?");
					}
					else
					{
						cell.setText(CourseWizardPage.this.df.format(course.getFirstDate().getTime()));
					}
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Beginn");
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.CENTER);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Course)
				{
					Course course = (Course) cell.getElement();
					if (course.getLastDate() == null)
					{
						cell.setText("?");
					}
					else
					{
						cell.setText(CourseWizardPage.this.df.format(course.getLastDate().getTime()));
					}
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Ende");
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Course)
				{
					Course course = (Course) cell.getElement();
					cell.setText(course.getState().toString());
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Status");
		treeColumn.setResizable(true);

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = 32;

		Composite messageComposite = new Composite(composite, SWT.None);
		messageComposite.setLayoutData(gridData);
		messageComposite.setLayout(new GridLayout(2, false));

		gridData = new GridData(GridData.FILL_VERTICAL);
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = 20;

		imageLabel = new Label(messageComposite, SWT.NONE);
		imageLabel.setLayoutData(gridData);

		gridData = new GridData(GridData.FILL_BOTH);
		gridData.verticalAlignment = SWT.CENTER;

		messageLabel = new Label(messageComposite, SWT.LEFT);
		messageLabel.setLayoutData(gridData);

		this.filterText.setText(this.dialogSettings.get(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_TEXT_VALUE_ID));
		this.filterButton.setSelection(this.dialogSettings
				.getBoolean(Constants.DIALOG_SETTINGS_KEY_COURSE_FILTER_BUTTON_SELECTED_ID));

		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(final ServiceReference reference)
			{
				final ConnectionService connectionService = (ConnectionService) super.addingService(reference);
				Display display = Display.getCurrent();
				if (display == null)
				{
					display = Display.getDefault();
				}
				display.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						viewer.setInput(connectionService);
						viewer.expandAll();
						packColumns();
						if (booking.getCourse() != null)
						{
							viewer.setSelection(new StructuredSelection(booking.getCourse()), true);
						}
					}
				});
				return connectionService;
			}

			@Override
			public void removedService(final ServiceReference reference, final Object service)
			{
				Display display = Display.getCurrent();
				if (display == null)
				{
					display = Display.getDefault();
				}
				display.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						viewer.setInput(null);
					}
				});
				super.removedService(reference, service);
			}

		};
		connectionServiceTracker.open();

		this.setControl(composite);

		this.updatePageState();
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		StructuredSelection ssel = (StructuredSelection) event.getSelection();
		if (!ssel.isEmpty() && ssel.getFirstElement() instanceof Course)
		{
			if (this.getWizard().canFinish())
				this.getWizard().performFinish();
		}
	}

	public Course getCourse()
	{
		StructuredSelection ssel = (StructuredSelection) this.viewer.getSelection();
		return (Course) ssel.getFirstElement();
	}

	private void packColumns()
	{
		TreeColumn[] columns = this.viewer.getTree().getColumns();
		for (int i = 1; i < columns.length; i++)
			columns[i].pack();
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event)
	{
		if (event.getSource().equals(viewer))
		{
			this.updatePageState();
			ISelectionChangedListener[] listeners = this.selectionChangedListeners
					.toArray(new ISelectionChangedListener[0]);
			for (ISelectionChangedListener listener : listeners)
			{
				listener.selectionChanged(event);
			}
		}
		else if (event.getSource() instanceof TableViewer && !event.getSelection().isEmpty()
				&& event.getSelection() instanceof StructuredSelection)
		{
			this.alreadyParticipantFilter.selectionChanged(event);
		}
	}

	public void update(final Booking booking)
	{
		StructuredSelection ssel = (StructuredSelection) this.viewer.getSelection();
		booking.setCourse((Course) ssel.getFirstElement());
	}

	private void updatePageState()
	{
		IStructuredSelection ssel = (IStructuredSelection) viewer.getSelection();
		if (ssel.getFirstElement() instanceof Course)
		{
			Course course = (Course) ssel.getFirstElement();
			if (course.getParticipantsCount() + 1 > course.getMaxParticipants())
			{
				messageLabel.setText("Die maximale Anzahl der Buchungen ist bereits erreicht.");
				imageLabel.setImage(Activator.getDefault().getImageRegistry().get("error"));
				this.setPageComplete(false);
				return;
			}
			if (course.getBookingTypes().size() == 0)
			{
				messageLabel.setText("Für den Kurs sind keine Buchungsarten definiert.");
				imageLabel.setImage(Activator.getDefault().getImageRegistry().get("error"));
				this.setPageComplete(false);
				return;
			}
			else
			{
				messageLabel.setText("");
				imageLabel.setImage(null);
				this.setPageComplete(true);
				return;
			}
		}
		else
		{
			messageLabel.setText("Kein Kurs ausgewählt.");
			imageLabel.setImage(null);
			this.setPageComplete(false);
			return;
		}
	}

	private class AlreadyParticipantFilter extends ViewerFilter implements ISelectionChangedListener
	{
		private final TreeViewer viewer;

		private final Collection<Course> courses = new ArrayList<Course>();

		public AlreadyParticipantFilter(final TreeViewer viewer, final Booking booking)
		{
			this.viewer = viewer;

			Collection<Participant> participants = booking.getParticipants();
			for (Participant participant : participants)
			{
				if (!participant.isDeleted() && !participant.getLink().isDeleted())
				{
					Collection<Participant> otherCourseParticipants = participant.getLink().getParticipants();
					for (Participant otherCourseParticipant : otherCourseParticipants)
					{
						if (!otherCourseParticipant.isDeleted() && !otherCourseParticipant.getLink().isDeleted())
						{
							if (!this.courses.contains(otherCourseParticipant.getBooking().getCourse()))
								this.courses.add(otherCourseParticipant.getBooking().getCourse());
						}
					}
				}
			}
		}

		@Override
		public boolean select(final Viewer viewer, final Object parentElement, final Object element)
		{
			if (element instanceof Course)
			{
				for (Course course : this.courses)
				{
					if (course.getId().equals(((Course) element).getId()))
						return false;
				}
			}
			return true;
		}

		@Override
		public void selectionChanged(final SelectionChangedEvent event)
		{
			this.courses.clear();
			if (!event.getSelection().isEmpty() && event.getSelection() instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				Iterator<?> iterator = ssel.iterator();
				while (iterator.hasNext())
				{
					Object object = iterator.next();
					if (object instanceof Participant)
					{
						Participant participant = (Participant) object;
						if (!participant.isDeleted() && !participant.getLink().isDeleted())
						{
							Collection<Participant> otherCourseParticipants = participant.getLink().getParticipants();
							for (Participant otherCourseParticipant : otherCourseParticipants)
							{
								if (!otherCourseParticipant.isDeleted()
										&& !otherCourseParticipant.getLink().isDeleted())
								{
									if (!this.courses.contains(otherCourseParticipant.getBooking().getCourse()))
										this.courses.add(otherCourseParticipant.getBooking().getCourse());
								}
							}
						}
					}
				}
				this.viewer.refresh();
			}
		}

	}
}
