package ch.eugster.events.course.editors;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingAnnulatedState;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.PaymentTerm;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.PaymentTermQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class BookingEditor extends AbstractEntityEditor<Booking> implements PropertyChangeListener
{
	public static final String ID = "ch.eugster.events.course.editors.bookingEditor";

	private static final String BOOKING_EDITOR = "booking.editor";

	private static final String MAIN_SECTION_EXPANDED = "main.section.expanded";

	private static final String DATE_SECTION_EXPANDED = "date.section.expanded";

	private static final String PAYMENT_SECTION_EXPANDED = "payment.section.expanded";

	private CDateTime date;

	private ComboViewer stateViewer;

	private ComboViewer paymentTermViewer;

	private Text note;

	private CDateTime bookingConfirmationSentDate;

	private CDateTime invitationSentDate;

	private CDateTime participationConfirmationSentDate;

	private FormattedText bookingAmount;

	private CDateTime payDate;

	private FormattedText payAmount;

	private CDateTime payBackDate;

	private FormattedText payBackAmount;

	private Section mainSection;

	private Section dateSection;

	private Section paymentSection;

	private IDialogSettings dialogSettings;

	private void createDateSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.dateSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.dateSection.setLayoutData(layoutData);
		this.dateSection.setLayout(sectionLayout);
		this.dateSection.setText("Aktivitäten");
		this.dateSection.setClient(this.fillDateSection(this.dateSection));
		this.dateSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				BookingEditor.this.dialogSettings.put(BookingEditor.DATE_SECTION_EXPANDED, e.getState());
				BookingEditor.this.scrolledForm.reflow(true);
			}
		});
		this.dateSection.setExpanded(this.dialogSettings.getBoolean(BookingEditor.DATE_SECTION_EXPANDED));
	}

	private void createMainSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.mainSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.mainSection.setLayoutData(layoutData);
		this.mainSection.setLayout(sectionLayout);
		this.mainSection.setText("Buchungsdaten");
		this.mainSection.setClient(this.fillMainSection(this.mainSection));
		this.mainSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				BookingEditor.this.dialogSettings.put(BookingEditor.MAIN_SECTION_EXPANDED, e.getState());
				BookingEditor.this.scrolledForm.reflow(true);
			}
		});
		this.mainSection.setExpanded(this.dialogSettings.getBoolean(BookingEditor.MAIN_SECTION_EXPANDED));
	}

	private void createPaymentSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.paymentSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.paymentSection.setLayoutData(layoutData);
		this.paymentSection.setLayout(sectionLayout);
		this.paymentSection.setText("Zahlungen");
		this.paymentSection.setClient(this.fillPaymentSection(this.paymentSection));
		this.paymentSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				BookingEditor.this.dialogSettings.put(BookingEditor.PAYMENT_SECTION_EXPANDED, e.getState());
				BookingEditor.this.scrolledForm.reflow(true);
			}
		});
		this.paymentSection.setExpanded(this.dialogSettings.getBoolean(BookingEditor.PAYMENT_SECTION_EXPANDED));
	}

	@Override
	protected void createSections(final ScrolledForm parent)
	{
		this.createMainSection(parent);
		this.createDateSection(parent);
		this.createPaymentSection(parent);
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Course.class, this);
		EntityMediator.removeListener(Booking.class, this);
		super.dispose();
	}

	private Control fillDateSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Buchungsbestätigung verschickt", SWT.NONE);
		label.setLayoutData(new GridData());

		this.bookingConfirmationSentDate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		this.bookingConfirmationSentDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.bookingConfirmationSentDate.setLayoutData(new GridData());
		this.bookingConfirmationSentDate.setSelection(new Date());
		this.bookingConfirmationSentDate.setNullText("Ausstehend");
		this.bookingConfirmationSentDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				BookingEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Kursunterlagen verschickt", SWT.NONE);
		label.setLayoutData(new GridData());

		this.invitationSentDate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		this.invitationSentDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.invitationSentDate.setLayoutData(new GridData());
		this.invitationSentDate.setSelection(new Date());
		this.invitationSentDate.setNullText("Ausstehend");
		this.invitationSentDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				BookingEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Teilnahmebestätigung verschickt", SWT.NONE);
		label.setLayoutData(new GridData());

		this.participationConfirmationSentDate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		this.participationConfirmationSentDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.participationConfirmationSentDate.setLayoutData(new GridData());
		this.participationConfirmationSentDate.setSelection(new Date());
		this.participationConfirmationSentDate.setNullText("Ausstehend");
		this.participationConfirmationSentDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				BookingEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillMainSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Buchungsdatum", SWT.NONE);
		label.setLayoutData(new GridData());

		this.date = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		this.date.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.date.setLayoutData(new GridData());
		this.date.setSelection(new Date());
		this.date.setNullText("");
		this.date.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				BookingEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Status", SWT.NONE);
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.formToolkit.adapt(combo);

		this.stateViewer = new ComboViewer(combo);
		this.stateViewer.setContentProvider(new ArrayContentProvider());
		this.stateViewer.setLabelProvider(new BookingStateLabelProvider());
		this.stateViewer.setSorter(new BookingStateSorter());
		Booking booking = ((BookingEditorInput) this.getEditorInput()).getEntity();
		if (booking.getCourse().getState().equals(CourseState.FORTHCOMING))
			this.stateViewer.setInput(BookingForthcomingState.values());
		else if (booking.getCourse().getState().equals(CourseState.DONE))
			this.stateViewer.setInput(BookingDoneState.values());
		else if (booking.getCourse().getState().equals(CourseState.ANNULATED))
			this.stateViewer.setInput(BookingAnnulatedState.values());
		this.stateViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				BookingEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Zahlungsbedingungen", SWT.NONE);
		label.setLayoutData(new GridData());

		combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.formToolkit.adapt(combo);

		this.paymentTermViewer = new ComboViewer(combo);
		this.paymentTermViewer.setContentProvider(new ArrayContentProvider());
		this.paymentTermViewer.setLabelProvider(new PaymentTermLabelProvider());
		this.paymentTermViewer.setInput(getPaymentTerms());
		this.paymentTermViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				BookingEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Bemerkungen", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 72;

		this.note = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		this.note.setLayoutData(layoutData);
		this.note.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				BookingEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private List<PaymentTerm> getPaymentTerms()
	{
		List<PaymentTerm> paymentTerms = new ArrayList<PaymentTerm>();
		PaymentTerm term = PaymentTerm.newInstance();
		term.setId(Long.valueOf(0L));
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				PaymentTermQuery query = (PaymentTermQuery) service.getQuery(PaymentTerm.class);
				List<PaymentTerm> existingTerms = query.selectAll();
				paymentTerms.addAll(existingTerms);
			}
		}
		finally
		{
			tracker.close();
		}
		return paymentTerms;
	}

	private Control fillPaymentSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Gesamtbetrag", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData layoutData = new GridData();
		layoutData.widthHint = 64;

		Text text = this.formToolkit.createText(composite, "", SWT.RIGHT);
		text.setLayoutData(layoutData);
		text.setEnabled(false);

		this.bookingAmount = new FormattedText(text);
		this.bookingAmount.setFormatter(new NumberFormatter("#,###,##0.00", Locale.getDefault()));

		label = this.formToolkit.createLabel(composite, "Zahlungsdatum", SWT.NONE);
		label.setLayoutData(new GridData());

		this.payDate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.payDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.payDate.setLayoutData(new GridData());
		this.payDate.setNullText("");
		this.payDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				BookingEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Zahlungsbetrag", SWT.NONE);
		label.setLayoutData(new GridData());

		layoutData = new GridData();
		layoutData.widthHint = 64;

		text = this.formToolkit.createText(composite, "", SWT.RIGHT);
		text.setLayoutData(layoutData);

		this.payAmount = new FormattedText(text);
		this.payAmount.setFormatter(new NumberFormatter("#,###,##0.00", Locale.getDefault()));

		label = this.formToolkit.createLabel(composite, "Datum Rückzahlung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.payBackDate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.payBackDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.payBackDate.setLayoutData(new GridData());
		this.payBackDate.setNullText("");
		this.payBackDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				BookingEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Betrag Rückzahlung", SWT.NONE);
		label.setLayoutData(new GridData());

		layoutData = new GridData();
		layoutData.widthHint = 64;

		text = this.formToolkit.createText(composite, "", SWT.RIGHT);
		text.setLayoutData(layoutData);

		this.payBackAmount = new FormattedText(text);
		this.payBackAmount.setFormatter(new NumberFormatter("#,###,##0.00", Locale.getDefault()));

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Class adapter)
	{
		// if (IContentOutlinePage.class.equals(adapter))
		// {
		// this.contentOutlinePage = new BookingEditorContentOutlinePage(this);
		// return this.contentOutlinePage;
		// }
		return super.getAdapter(adapter);
	}

	@Override
	protected Message getMessage(final PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		return msg;
	}

	@Override
	protected String getName()
	{
		BookingEditorInput input = (BookingEditorInput) this.getEditorInput();
		Booking booking = (Booking) input.getAdapter(Booking.class);
		return booking.getId() == null ? "Neu" : "Buchung: " + booking.getId();
	}

	@Override
	protected String getText()
	{
		BookingEditorInput input = (BookingEditorInput) this.getEditorInput();
		Booking booking = (Booking) input.getAdapter(Booking.class);
		Person person = booking.getParticipant().getLink().getPerson();
		return (booking.getId() == null ? "Neue Buchung" : "Buchung " + booking.getId()) + " - "
				+ PersonFormatter.getInstance().formatLastnameFirstname(person) + " ("
				+ PersonFormatter.getInstance().formatId(person) + ")";
	}

	@Override
	protected void initialize()
	{
		Long id = ((BookingEditorInput) this.getEditorInput()).getEntity().getId();
		this.initializeDialogSettings(id == null ? BookingEditor.BOOKING_EDITOR : BookingEditor.BOOKING_EDITOR + "."
				+ id);

		EntityMediator.addListener(Course.class, this);
		EntityMediator.addListener(Booking.class, this);
	}

	private void initializeDialogSettings(final String section)
	{
		this.dialogSettings = Activator.getDefault().getDialogSettings().getSection(section);
		if (this.dialogSettings == null)
			this.dialogSettings = Activator.getDefault().getDialogSettings().addNewSection(section);
	}

	@Override
	protected void loadValues()
	{
		BookingEditorInput input = (BookingEditorInput) this.getEditorInput();
		Booking booking = input.getEntity();
		if (booking != null)
		{
			this.date.setSelection(booking.getDate() == null ? null : booking.getDate().getTime());

			if (booking.getCourse().getState().equals(CourseState.FORTHCOMING))
				this.stateViewer.setSelection(new StructuredSelection(booking.getForthcomingState()));
			else if (booking.getCourse().getState().equals(CourseState.DONE))
				this.stateViewer.setSelection(new StructuredSelection(booking.getDoneState()));
			else if (booking.getCourse().getState().equals(CourseState.ANNULATED))
				this.stateViewer.setSelection(new StructuredSelection(booking.getAnnulatedState()));

			PaymentTerm term = booking.getPaymentTerm();
			if (term == null)
			{
				term = PaymentTerm.newInstance();
				term.setId(Long.valueOf(0L));
			}
			this.paymentTermViewer.setSelection(new StructuredSelection(new PaymentTerm[] { term }));

			this.note.setText(booking.getNote());

			Date date = null;
			if (booking.getBookingConfirmationSentDate() != null)
			{
				date = booking.getBookingConfirmationSentDate().getTime();
			}
			this.bookingConfirmationSentDate.setSelection(date);

			date = null;
			if (booking.getInvitationSentDate() != null)
			{
				date = booking.getInvitationSentDate().getTime();
			}
			this.invitationSentDate.setSelection(date);

			date = null;
			if (booking.getParticipationConfirmationSentDate() != null)
			{
				date = booking.getParticipationConfirmationSentDate().getTime();
			}
			this.participationConfirmationSentDate.setSelection(date);

			this.bookingAmount.setValue(Double.valueOf(booking.getAmount()));

			date = null;
			if (booking.getPayDate() != null)
			{
				date = booking.getPayDate().getTime();
			}
			this.payDate.setSelection(date);

			this.payAmount.setValue(Double.valueOf(booking.getPayAmount()));

			date = null;
			if (booking.getPayBackDate() != null)
			{
				date = booking.getPayBackDate().getTime();
			}
			this.payBackDate.setSelection(date);

			this.payBackAmount.setValue(Double.valueOf(booking.getPayAmount()));

		}
		this.setDirty(false);
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (entity instanceof Course)
				{
					Course course = (Course) entity;
					Booking booking = ((BookingEditorInput) getEditorInput()).getEntity();
					if (course.equals(booking.getCourse()))
					{
						if (course.getState().equals(CourseState.FORTHCOMING))
						{
							if (!(stateViewer.getInput() instanceof BookingForthcomingState[]))
							{
								stateViewer.setInput(BookingForthcomingState.values());
								stateViewer.setSelection(new StructuredSelection(booking.getForthcomingState()));
							}
						}
						if (course.getState().equals(CourseState.DONE))
						{
							if (!(stateViewer.getInput() instanceof BookingDoneState[]))
							{
								stateViewer.setInput(BookingDoneState.values());
								stateViewer.setSelection(new StructuredSelection(booking.getDoneState()));
							}
						}
						if (course.getState().equals(CourseState.ANNULATED))
						{
							if (!(stateViewer.getInput() instanceof BookingAnnulatedState[]))
							{
								stateViewer.setInput(BookingAnnulatedState.values());
								stateViewer.setSelection(new StructuredSelection(booking.getAnnulatedState()));
							}
						}
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
		// if (entity instanceof CourseDetail)
		// {
		// this.courseDetailViewer.refresh();
		// this.packColumns(this.courseDetailViewer.getTable().getColumns());
		// }
		// else if (entity instanceof CourseGuide)
		// {
		// this.courseGuideViewer.refresh();
		// this.packColumns(this.courseGuideViewer.getTable().getColumns());
		// }
		// else if (entity instanceof BookingType)
		// {
		// this.bookingTypeViewer.refresh();
		// this.packColumns(this.bookingTypeViewer.getTable().getColumns());
		// }
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (entity instanceof Course)
				{
					Course course = (Course) entity;
					Booking booking = ((BookingEditorInput) getEditorInput()).getEntity();
					if (course.equals(booking.getCourse()))
					{
						if (course.getState().equals(CourseState.FORTHCOMING))
						{
							if (!(stateViewer.getInput() instanceof BookingForthcomingState[]))
							{
								stateViewer.setInput(BookingForthcomingState.values());
								stateViewer.setSelection(new StructuredSelection(booking.getForthcomingState()));
							}
						}
						if (course.getState().equals(CourseState.DONE))
						{
							if (!(stateViewer.getInput() instanceof BookingDoneState[]))
							{
								stateViewer.setInput(BookingDoneState.values());
								stateViewer.setSelection(new StructuredSelection(booking.getDoneState()));
							}
						}
						if (course.getState().equals(CourseState.ANNULATED))
						{
							if (!(stateViewer.getInput() instanceof BookingAnnulatedState[]))
							{
								stateViewer.setInput(BookingAnnulatedState.values());
								stateViewer.setSelection(new StructuredSelection(booking.getAnnulatedState()));
							}
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void propertyChange(final java.beans.PropertyChangeEvent event)
	{
		this.setDirty(true);
	}

	@Override
	protected void saveValues()
	{
		BookingEditorInput input = (BookingEditorInput) this.getEditorInput();
		Booking booking = input.getEntity();
		if (booking != null)
		{
			Calendar calendar = GregorianCalendar.getInstance();
			if (date.getSelection() != null)
			{
				calendar.setTime(date.getSelection());
				booking.setDate(calendar);
			}
			else
			{
				booking.setDate(null);
			}

			IStructuredSelection ssel = (IStructuredSelection) this.stateViewer.getSelection();
			if (ssel.getFirstElement() instanceof BookingForthcomingState)
				booking.setForthcomingState((BookingForthcomingState) ssel.getFirstElement());
			else if (ssel.getFirstElement() instanceof BookingDoneState)
				booking.setDoneState((BookingDoneState) ssel.getFirstElement());
			else if (ssel.getFirstElement() instanceof BookingAnnulatedState)
				booking.setAnnulatedState((BookingAnnulatedState) ssel.getFirstElement());

			ssel = (IStructuredSelection) this.paymentTermViewer.getSelection();
			PaymentTerm selectedPaymentTerm = (PaymentTerm) ssel.getFirstElement();
			if (selectedPaymentTerm != null && selectedPaymentTerm.getId().equals(Long.valueOf(0L)))
			{
				selectedPaymentTerm = null;
			}
			booking.setPaymentTerm(selectedPaymentTerm);

			booking.setNote(this.note.getText());

			if (this.bookingConfirmationSentDate.getSelection() == null)
			{
				calendar = null;
			}
			else
			{
				calendar.setTime(this.bookingConfirmationSentDate.getSelection());
			}
			booking.setBookingConfirmationSentDate(calendar);

			if (this.invitationSentDate.getSelection() == null)
			{
				calendar = null;
			}
			else
			{
				calendar.setTime(this.invitationSentDate.getSelection());
			}
			booking.setInvitationSentDate(calendar);

			if (this.participationConfirmationSentDate.getSelection() == null)
			{
				calendar = null;
			}
			else
			{
				calendar.setTime(this.participationConfirmationSentDate.getSelection());
			}
			booking.setParticipationConfirmationSentDate(calendar);

			if (this.payDate.getSelection() == null)
			{
				calendar = null;
			}
			else
			{
				calendar.setTime(this.payDate.getSelection());
			}
			booking.setPayDate(calendar);

			try
			{
				Double value = Double.valueOf(this.payAmount.getControl().getText());
				booking.setPayAmount(value.doubleValue());
			}
			catch (NumberFormatException e)
			{
				booking.setPayAmount(0d);
			}

			if (this.payBackDate.getSelection() == null)
			{
				calendar = null;
			}
			else
			{
				calendar.setTime(this.payBackDate.getSelection());
			}
			booking.setPayBackDate(calendar);

			try
			{
				Double value = Double.valueOf(this.payBackAmount.getControl().getText());
				booking.setPayBackAmount(value.doubleValue());
			}
			catch (NumberFormatException e)
			{
				booking.setPayBackAmount(0d);
			}
		}
	}

	@Override
	public void setFocus()
	{
		this.date.setFocus();
	}

	@Override
	protected boolean validate()
	{
		Message msg = null;

		// StructuredSelection ssel = (StructuredSelection)
		// this.stateViewer.getSelection();
		// if (ssel.isEmpty())
		// {
		// msg = new Message(this.code, "Kursstatus fehlt");
		// msg.setMessage("Sie haben den Kursstatus nicht festgelegt.");
		// FormToolkit.ensureVisible(this.stateViewer.getCCombo());
		// this.stateViewer.getCCombo().setFocus();
		// }
		//
		// if (msg == null)
		// {
		// if (GlobalSettings.getInstance().isCourseDomainMandatory())
		// {
		// ssel = (StructuredSelection) this.domainViewer.getSelection();
		// if (ssel.isEmpty() || ((Domain) ssel.getFirstElement()).getId() ==
		// null)
		// {
		// msg = new Message(this.code, "Fehlende Domäne");
		// msg.setMessage("Sie haben keine Domäne ausgewählt.");
		// FormToolkit.ensureVisible(this.domainViewer.getCCombo());
		// this.domainViewer.getCCombo().setFocus();
		// }
		// }
		// }
		//
		// if (msg == null)
		// {
		// if (GlobalSettings.getInstance().isCourseCategoryMandatory())
		// {
		// ssel = (StructuredSelection) this.categoryViewer.getSelection();
		// if (ssel.isEmpty() || ((Category) ssel.getFirstElement()).getId() ==
		// null)
		// {
		// msg = new Message(this.code, "Fehlende Kategorie");
		// msg.setMessage("Sie haben keine Kategorie ausgewählt.");
		// FormToolkit.ensureVisible(this.categoryViewer.getCCombo());
		// this.categoryViewer.getCCombo().setFocus();
		// }
		// }
		// }
		//
		// if (msg == null)
		// {
		// if (GlobalSettings.getInstance().isCourseRubricMandatory())
		// {
		// ssel = (StructuredSelection) this.rubricViewer.getSelection();
		// if (ssel.isEmpty() || ((Rubric) ssel.getFirstElement()).getId() ==
		// null)
		// {
		// msg = new Message(this.code, "Fehlende Rubrik");
		// msg.setMessage("Sie haben keine Rubrik ausgewählt.");
		// FormToolkit.ensureVisible(this.rubricViewer.getCCombo());
		// this.rubricViewer.getCCombo().setFocus();
		// }
		// }
		// }
		//
		// if (msg == null)
		// {
		// if (GlobalSettings.getInstance().isCourseResponsibleUserMandatory())
		// {
		// ssel = (StructuredSelection) this.userViewer.getSelection();
		// if (ssel.isEmpty() || ((User) ssel.getFirstElement()).getId() ==
		// null)
		// {
		// msg = new Message(this.code, "Fehlende kursverantwortliche Person");
		// msg.setMessage("Sie haben keine für den Kurs verantwortliche Person ausgewählt.");
		// FormToolkit.ensureVisible(this.userViewer.getCCombo());
		// this.userViewer.getCCombo().setFocus();
		// }
		// }
		// }
		//
		// if (msg == null)
		// {
		// msg = this.getEmptyCodeMessage();
		// }
		//
		// if (msg == null)
		// {
		// msg = this.getUniqueCodeMessage();
		// }
		//
		// if (msg != null) this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(final AbstractEntityEditorInput<Booking> input)
	{
		return input.getEntity() instanceof Booking;
	}

}
