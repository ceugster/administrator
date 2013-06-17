package ch.eugster.events.course.wizards;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.model.BookingAnnulatedState;
import ch.eugster.events.persistence.model.BookingDoneState;
import ch.eugster.events.persistence.model.BookingForthcomingState;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseState;

public class BookingWizardPage extends WizardPage implements ISelectionChangedListener, IBookingWizardPage
{
	private CDateTime bookingDate;

	private ComboViewer bookingState;

	private Button printBookingConfirmation;

	private CDateTime bookingConfirmationSentDate;

	private Button printInvitation;

	private CDateTime invitationSentDate;

	private CDateTime participationConfirmationSentDate;

	private CDateTime payDate;

	private FormattedText payAmount;

	private CDateTime payBackDate;

	private FormattedText payBackAmount;

	private Text bookingConfirmationTemplatePath;

	private Button bookingConfirmationTemplatePathSelector;

	private Text invitationTemplatePath;

	private Button invitationTemplatePathSelector;

	private IDialogSettings settings;

	public BookingWizardPage(final String name, final IBookingWizard wizard)
	{
		super(name);
		Assert.isTrue(wizard instanceof BookingWizard);
		settings = Activator.getDefault().getDialogSettings().getSection("booking.wizard");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("booking.wizard");
		}
	}

	@Override
	public void createControl(final Composite parent)
	{
		this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("BOOKING_48"));
		this.setTitle("Buchungsdaten");
		this.setMessage("Erfassen und Bearbeiten der Buchungsdaten");

		Composite composite = new Composite(parent, SWT.None);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Buchungsdatum");

		GridData layoutData = new GridData();
		layoutData.horizontalSpan = 2;

		this.bookingDate = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM | CDT.SPINNER);
		this.bookingDate.setLayoutData(layoutData);
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
				BookingWizardPage.this.updatePageState();
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Status");

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;

		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(layoutData);

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
				BookingWizardPage.this.updatePageState();
			}

		});

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Versand Buchungsbestätigung");

		this.bookingConfirmationSentDate = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM | CDT.SPINNER);
		this.bookingConfirmationSentDate.setLayoutData(new GridData());
		this.bookingConfirmationSentDate.setNullText("");

		this.printBookingConfirmation = new Button(composite, SWT.CHECK);
		this.printBookingConfirmation.setText("Buchungsbestätigung jetzt drucken");
		this.printBookingConfirmation.setLayoutData(new GridData());
		this.printBookingConfirmation.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				updatePageState();
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Versand Kursunterlagen");

		this.invitationSentDate = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM | CDT.SPINNER);
		this.invitationSentDate.setLayoutData(new GridData());
		this.invitationSentDate.setNullText("");

		this.printInvitation = new Button(composite, SWT.CHECK);
		this.printInvitation.setText("Kurseinladung jetzt drucken");
		this.printInvitation.setLayoutData(new GridData());
		this.printInvitation.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				updatePageState();
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Versand Teilnahmebestätigung");

		layoutData = new GridData();
		layoutData.horizontalSpan = 2;

		this.participationConfirmationSentDate = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM | CDT.SPINNER);
		this.participationConfirmationSentDate.setLayoutData(layoutData);
		this.participationConfirmationSentDate.setNullText("");

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Zahlungsdatum");

		layoutData = new GridData();
		layoutData.horizontalSpan = 2;

		this.payDate = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM | CDT.SPINNER);
		this.payDate.setLayoutData(layoutData);
		this.payDate.setNullText("");

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Zahlungsbetrag");

		layoutData = new GridData();
		layoutData.widthHint = 64;
		layoutData.horizontalSpan = 2;

		Text text = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		text.setLayoutData(layoutData);
		text.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.payAmount = new FormattedText(text);
		this.payAmount.setFormatter(new NumberFormatter("#,###,###.00", Locale.getDefault()));

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Rückzahlungsdatum");

		layoutData = new GridData();
		layoutData.horizontalSpan = 2;

		this.payBackDate = new CDateTime(composite, CDT.BORDER | CDT.DATE_MEDIUM | CDT.SPINNER);
		this.payBackDate.setLayoutData(layoutData);
		this.payBackDate.setNullText("");

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Rückzahlungsbetrag");

		layoutData = new GridData();
		layoutData.widthHint = 64;
		layoutData.horizontalSpan = 2;

		text = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		text.setLayoutData(layoutData);
		text.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent e)
			{
				Text text = (Text) e.getSource();
				text.setSelection(0, text.getText().length());
			}
		});

		this.payBackAmount = new FormattedText(text);
		this.payBackAmount.setFormatter(new NumberFormatter("#,###,###.00", Locale.getDefault()));

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Vorlage Buchungsbestätigung");

		bookingConfirmationTemplatePath = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		bookingConfirmationTemplatePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		String path = settings.get(".booking.confirmation.template.path");
		bookingConfirmationTemplatePath.setText(path == null ? "" : path);

		bookingConfirmationTemplatePathSelector = new Button(composite, SWT.PUSH);
		bookingConfirmationTemplatePathSelector.setLayoutData(new GridData());
		bookingConfirmationTemplatePathSelector.setText("...");
		bookingConfirmationTemplatePathSelector.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				String path = bookingConfirmationTemplatePath.getText();
				File file = new File(path);
				if (!file.isFile())
				{
					path = "";
				}
				FileDialog dialog = new FileDialog(BookingWizardPage.this.getShell());
				dialog.setFilterPath(path.isEmpty() ? System.getProperty("user.home") : path);
				path = dialog.open();
				if (path != null)
				{
					file = new File(path);
					settings.put("booking.confirmation.template.path", file.getAbsolutePath());
					bookingConfirmationTemplatePath.setText(file.getAbsolutePath());
					updatePageState();
				}
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Vorlage Kurseinladung");

		invitationTemplatePath = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		invitationTemplatePath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		path = settings.get("template.path");
		invitationTemplatePath.setText(path == null ? "" : path);

		invitationTemplatePathSelector = new Button(composite, SWT.PUSH);
		invitationTemplatePathSelector.setLayoutData(new GridData());
		invitationTemplatePathSelector.setText("...");
		invitationTemplatePathSelector.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				String path = invitationTemplatePath.getText();
				File file = new File(path);
				if (!file.isFile())
				{
					path = "";
				}
				FileDialog dialog = new FileDialog(BookingWizardPage.this.getShell());
				dialog.setFilterPath(path.isEmpty() ? System.getProperty("user.home") : path);
				path = dialog.open();
				if (path != null)
				{
					file = new File(path);
					settings.put("invitation.template.path", file.getAbsolutePath());
					invitationTemplatePath.setText(file.getAbsolutePath());
					updatePageState();
				}
			}
		});

		this.setValues();

		this.updatePageState();

		this.setControl(composite);
	}

	private Booking getBooking()
	{
		BookingWizard wizard = (BookingWizard) this.getWizard();
		return wizard.getBooking();
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

	public String getBookingConfirmationTemplatePath()
	{
		return bookingConfirmationTemplatePath.getText();
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

	public String getInvitationTemplatePath()
	{
		return invitationTemplatePath.getText();
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

	public boolean printBookingConfirmation()
	{
		return this.printBookingConfirmation.getSelection();
	}

	public boolean printInvitation()
	{
		return this.printInvitation.getSelection();
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
			BookingForthcomingState[] availableBookingStates = null;
			if (courseState.equals(CourseState.FORTHCOMING))
			{
				if (course.getParticipantsCount() + 1 < course.getMaxParticipants())
				{
					availableBookingStates = BookingForthcomingState.values();
				}
				else
				{
					availableBookingStates = new BookingForthcomingState[] { BookingForthcomingState.WAITING_LIST };
				}
			}
			this.bookingState.setInput(availableBookingStates);
			BookingForthcomingState selectedBookingState = availableBookingStates[0];
			this.getBooking().setForthcomingState(selectedBookingState);
			this.bookingState.setSelection(new StructuredSelection(
					new BookingForthcomingState[] { selectedBookingState }));
		}
	}

	private void setValues()
	{
		Date date = null;
		if (this.getBooking().getDate() == null)
		{
			date = GregorianCalendar.getInstance().getTime();
		}
		else
		{
			date = this.getBooking().getDate().getTime();
		}
		this.bookingDate.setSelection(date);

		if (this.getBooking().getCourse() != null)
		{
			this.setBookingStateInput(this.getBooking().getCourse());
		}

		if (this.getBooking().getBookingConfirmationSentDate() == null)
		{
			date = null;
		}
		else
		{
			date = this.getBooking().getBookingConfirmationSentDate().getTime();
		}
		this.bookingConfirmationSentDate.setSelection(date);

		if (this.getBooking().getInvitationSentDate() == null)
		{
			date = null;
		}
		else
		{
			date = this.getBooking().getInvitationSentDate().getTime();
		}
		this.invitationSentDate.setSelection(date);

		if (this.getBooking().getParticipationConfirmationSentDate() == null)
		{
			date = null;
		}
		else
		{
			date = this.getBooking().getParticipationConfirmationSentDate().getTime();
		}
		this.participationConfirmationSentDate.setSelection(date);

		if (this.getBooking().getPayDate() == null)
		{
			date = null;
		}
		else
		{
			date = this.getBooking().getPayDate().getTime();
		}
		this.payDate.setSelection(date);

		this.payAmount.setValue(Double.valueOf(this.getBooking().getPayAmount()));

		if (this.getBooking().getPayBackDate() == null)
		{
			date = null;
		}
		else
		{
			date = this.getBooking().getPayBackDate().getTime();
		}
		this.payBackDate.setSelection(date);

		this.payBackAmount.setValue(Double.valueOf(this.getBooking().getPayBackAmount()));

		updatePageState();
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
		Number amount = (Number) payAmount.getValue();
		booking.setPayAmount(amount.doubleValue());
		booking.setPayBackDate(this.getPayBackDate());
		amount = (Number) this.payBackAmount.getValue();
		booking.setPayBackAmount(amount.doubleValue());
	}

	private void updatePageState()
	{
		if (this.bookingDate.getSelection() == null)
		{
			super.setPageComplete(false);
			return;
		}
		IStructuredSelection ssel = (IStructuredSelection) this.bookingState.getSelection();
		if (!(ssel.getFirstElement() instanceof BookingForthcomingState))
		{
			super.setPageComplete(false);
			return;
		}
		if (this.printBookingConfirmation())
		{
			File file = new File(bookingConfirmationTemplatePath.getText());
			if (!file.isFile())
			{
				super.setPageComplete(false);
				return;
			}
		}
		if (this.printInvitation())
		{
			File file = new File(invitationTemplatePath.getText());
			if (!file.isFile())
			{
				super.setPageComplete(false);
				return;
			}
		}
		super.setPageComplete(true);
	}

}
