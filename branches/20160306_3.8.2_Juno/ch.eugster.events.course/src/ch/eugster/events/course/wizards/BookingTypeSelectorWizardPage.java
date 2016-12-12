package ch.eugster.events.course.wizards;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingAnnulatedState;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;

public class BookingTypeSelectorWizardPage extends WizardPage implements ISelectionChangedListener
{
	private final Booking booking;

	private CDateTime bookingDate;

	private ComboViewer bookingState;

	private CDateTime bookingConfirmationSentDate;

	private CDateTime invitationSentDate;

	private CDateTime participationConfirmationSentDate;

	private CDateTime payDate;

	private FormattedText payAmount;

	private CDateTime payBackDate;

	private FormattedText payBackAmount;

	public BookingTypeSelectorWizardPage(final String name, final Booking booking)
	{
		super(name);
		this.booking = booking;
	}

	@Override
	public void createControl(final Composite parent)
	{
		this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("BOOKING_48"));
		this.setTitle("Buchungsdaten");
		this.setMessage("Erfassen und Bearbeiten der Buchungsdaten");

		Composite composite = new Composite(parent, SWT.None);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(2, false));

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Buchungsdatum");

		this.bookingDate = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM | CDT.DROP_DOWN);
		this.bookingDate.setLayoutData(new GridData());
		this.bookingDate.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				BookingTypeSelectorWizardPage.this.updatePageState();
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Status");

		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.bookingState = new ComboViewer(combo);
		this.bookingState.setContentProvider(new ArrayContentProvider());
		this.bookingState.setLabelProvider(new LabelProvider()
		{

			@Override
			public Image getImage(final Object element)
			{
				return super.getImage(element);
			}

			@Override
			public String getText(final Object element)
			{
				return super.getText(element);
			}
		});
		this.bookingState.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				BookingTypeSelectorWizardPage.this.updatePageState();
			}

		});

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Buchungsbestätigung verschickt");

		this.bookingConfirmationSentDate = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM | CDT.DROP_DOWN);
		this.bookingConfirmationSentDate.setLayoutData(new GridData());
		this.bookingConfirmationSentDate.setNullText("<Noch nicht verschickt>");

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Kurseinladung und -unterlagen verschickt");

		this.invitationSentDate = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM | CDT.DROP_DOWN);
		this.invitationSentDate.setLayoutData(new GridData());
		this.invitationSentDate.setNullText("<Noch nicht verschickt>");

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Teilnahmebestätigung verschickt");

		this.participationConfirmationSentDate = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM | CDT.DROP_DOWN);
		this.participationConfirmationSentDate.setLayoutData(new GridData());
		this.participationConfirmationSentDate.setNullText("<Noch nicht verschickt>");

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Zahlungsdatum");

		this.payDate = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM | CDT.DROP_DOWN);
		this.payDate.setLayoutData(new GridData());
		this.payDate.setNullText("<Keine Zahlung>");

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Zahlungsbetrag");

		GridData layoutData = new GridData();
		layoutData.widthHint = 64;

		Text text = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		text.setLayoutData(layoutData);

		this.payAmount = new FormattedText(text);
		this.payAmount.setFormatter(new NumberFormatter("#,###,##0.00", Locale.getDefault()));

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Rückzahlungsdatum");

		this.payBackDate = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM | CDT.DROP_DOWN);
		this.payBackDate.setLayoutData(new GridData());
		this.payBackDate.setNullText("<Keine Zahlung>");

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Rückzahlungsbetrag");

		layoutData = new GridData();
		layoutData.widthHint = 64;

		text = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		text.setLayoutData(layoutData);

		this.payBackAmount = new FormattedText(text);
		this.payBackAmount.setFormatter(new NumberFormatter("#,###,##0.00", Locale.getDefault()));

		this.setValues();

		this.setControl(composite);
	}

	private Calendar getBookingConfirmationSentDate()
	{
		if (this.bookingConfirmationSentDate.getSelection() == null)
		{
			return null;
		}
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(this.bookingConfirmationSentDate.getSelection());
		return calendar;
	}

	private Calendar getBookingDate()
	{
		if (this.bookingDate.getSelection() == null)
		{
			return null;
		}
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(this.bookingDate.getSelection());
		return calendar;
	}

	private Calendar getInvitationSentDate()
	{
		if (this.invitationSentDate.getSelection() == null)
		{
			return null;
		}
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(this.invitationSentDate.getSelection());
		return calendar;
	}

	private Calendar getParticipationConfirmationSentDate()
	{
		if (this.participationConfirmationSentDate.getSelection() == null)
		{
			return null;
		}
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(this.participationConfirmationSentDate.getSelection());
		return calendar;
	}

	private Calendar getPayBackDate()
	{
		if (this.payBackDate.getSelection() == null)
		{
			return null;
		}
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(this.payBackDate.getSelection());
		return calendar;
	}

	private Calendar getPayDate()
	{
		if (this.payDate.getSelection() == null)
		{
			return null;
		}
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(this.payDate.getSelection());
		return calendar;
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event)
	{
		if (!event.getSelection().isEmpty() && event.getSelection() instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) event.getSelection();
			{
				if (ssel.getFirstElement() instanceof Course)
				{
					this.setBookingStateInput((Course) ssel.getFirstElement());
				}
			}
		}
	}

	private void setBookingStateInput(final Course course)
	{
		if (course == null || course.getState() == null)
		{
			this.bookingState.setInput(null);
			this.bookingState.setSelection(null);
		}
		else
		{
			CourseState courseState = course.getState();
			if (courseState.equals(CourseState.FORTHCOMING))
			{
				this.bookingState.setInput(BookingForthcomingState.values());
				if (this.booking.getState() == null)
					this.booking.setForthcomingState(BookingForthcomingState.BOOKED);
			}
			else if (courseState.equals(CourseState.DONE))
			{
				this.bookingState.setInput(BookingDoneState.values());
				if (this.booking.getState() == null)
					this.booking.setDoneState(BookingDoneState.PARTICIPATED);
			}
			else if (courseState.equals(CourseState.ANNULATED))
			{
				this.bookingState.setInput(BookingAnnulatedState.values());
				if (this.booking.getState() == null)
					this.booking.setAnnulatedState(BookingAnnulatedState.ANNULATED);
			}
			this.bookingState.setSelection(new StructuredSelection(this.booking.getBookingState(courseState)));
		}
	}

	private void setValues()
	{
		Date date = null;
		if (this.booking.getDate() == null)
		{
			date = GregorianCalendar.getInstance().getTime();
		}
		else
		{
			date = this.booking.getDate().getTime();
		}
		this.bookingDate.setSelection(date);

		if (this.booking.getBookingConfirmationSentDate() == null)
		{
			date = null;
		}
		else
		{
			date = this.booking.getBookingConfirmationSentDate().getTime();
		}
		this.bookingConfirmationSentDate.setSelection(date);

		if (this.booking.getInvitationSentDate() == null)
		{
			date = null;
		}
		else
		{
			date = this.booking.getInvitationSentDate().getTime();
		}
		this.invitationSentDate.setSelection(date);

		if (this.booking.getParticipationConfirmationSentDate() == null)
		{
			date = null;
		}
		else
		{
			date = this.booking.getParticipationConfirmationSentDate().getTime();
		}
		this.participationConfirmationSentDate.setSelection(date);

		if (this.booking.getPayDate() == null)
		{
			date = null;
		}
		else
		{
			date = this.booking.getPayDate().getTime();
		}
		this.payDate.setSelection(date);

		this.payAmount.setValue(Double.valueOf(this.booking.getPayAmount()));

		if (this.booking.getPayBackDate() == null)
		{
			date = null;
		}
		else
		{
			date = this.booking.getPayBackDate().getTime();
		}
		this.payBackDate.setSelection(date);

		this.payBackAmount.setValue(Double.valueOf(this.booking.getPayBackAmount()));
	}

	public void update(final Booking booking)
	{
		StructuredSelection ssel = (StructuredSelection) this.bookingState.getSelection();
		if (!ssel.isEmpty())
		{
			if (ssel.getFirstElement() instanceof BookingForthcomingState)
			{
				booking.setForthcomingState((BookingForthcomingState) ssel.getFirstElement());
			}
			else if (ssel.getFirstElement() instanceof BookingDoneState)
			{
				booking.setDoneState((BookingDoneState) ssel.getFirstElement());
			}
			else if (ssel.getFirstElement() instanceof BookingAnnulatedState)
			{
				booking.setAnnulatedState((BookingAnnulatedState) ssel.getFirstElement());
			}
		}
		booking.setBookingConfirmationSentDate(this.getBookingConfirmationSentDate());
		booking.setDate(this.getBookingDate());
		booking.setInvitationSentDate(this.getInvitationSentDate());
		booking.setNote(this.getDescription());
		booking.setParticipationConfirmationSentDate(this.getParticipationConfirmationSentDate());
		booking.setPayDate(this.getPayDate());
		Number amount = (Number) this.payAmount.getValue();
		booking.setPayAmount(amount.doubleValue());
		booking.setPayBackDate(this.getPayBackDate());
		amount = (Number) this.payBackAmount.getValue();
		booking.setPayBackAmount(amount.doubleValue());
	}

	private void updatePageState()
	{
		if (this.bookingDate.getSelection() == null)
			this.setPageComplete(false);
		if (this.bookingState.getSelection().isEmpty())
			this.setPageComplete(false);
		if (this.bookingState.getSelection().isEmpty())
			this.setPageComplete(false);
		this.setPageComplete(true);
	}
}
