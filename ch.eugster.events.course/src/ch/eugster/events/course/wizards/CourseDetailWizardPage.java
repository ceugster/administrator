package ch.eugster.events.course.wizards;

import java.util.Calendar;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.model.CourseDetail;

public class CourseDetailWizardPage extends WizardPage implements SelectionListener
{

	private CDateTime start;

	private CDateTime end;

	private Button withSubstituteDate;

	private CDateTime substituteStart;

	private Label ersatzStartLabel;

	private Label ersatzEndLabel;

	private CDateTime substituteEnd;

	private Text location;

	private Text meetingPoint;

	private Text journey;

	public CourseDetailWizardPage(final String pageName)
	{
		super(pageName);
	}

	public CourseDetailWizardPage(final String pageName, final String title, final ImageDescriptor titleImage,
			final CourseDetailWizard wizard)
	{
		super(pageName, title, titleImage);
	}

	@Override
	public void createControl(final Composite parent)
	{
		ImageDescriptor image = ImageDescriptor.createFromImage(Activator.getDefault().getImageRegistry()
				.get("NEW_WIZARD"));
		this.setImageDescriptor(image);

		CourseDetailWizard wizard = (CourseDetailWizard) this.getWizard();
		String course = CourseFormatter.getInstance().formatComboEntry(wizard.getCourseDetail().getCourse());
		if (wizard.getCourseDetail().getId() == null)
		{
			this.setTitle("Kursdaten hinzufügen");
			this.setDescription("Hinzufügen von Kursdaten zu '" + course + "'.");
		}
		else
		{
			this.setTitle("Kursdaten bearbeiten");
			this.setDescription("Bearbeiten der Kursdaten zu '" + course + "'.");
		}

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Beginn");

		this.start = new CDateTime(composite, CDT.BORDER | CDT.SPINNER | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.start.setLayoutData(new GridData());
		this.start.addSelectionListener(this);

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Beginn");

		this.end = new CDateTime(composite, CDT.BORDER | CDT.SPINNER | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.end.setLayoutData(new GridData());
		this.end.addSelectionListener(this);

		label = new Label(composite, SWT.NONE);
		label.setText("Kursort");
		label.setLayoutData(new GridData());

		this.location = new Text(composite, SWT.SINGLE | SWT.BORDER);
		this.location.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(composite, SWT.NONE);
		label.setText("Treffpunkt");
		label.setLayoutData(new GridData());

		this.meetingPoint = new Text(composite, SWT.SINGLE | SWT.BORDER);
		this.meetingPoint.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(composite, SWT.NONE);
		label.setText("An- und Rückreise");
		label.setLayoutData(new GridData());

		this.journey = new Text(composite, SWT.SINGLE | SWT.BORDER);
		this.journey.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridData layoutData = new GridData();
		layoutData.horizontalSpan = 2;

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(layoutData);

		layoutData = new GridData();
		layoutData.horizontalSpan = 2;

		this.withSubstituteDate = new Button(composite, SWT.CHECK);
		this.withSubstituteDate.setText("Ersatzdatum");
		this.withSubstituteDate.setLayoutData(layoutData);
		this.withSubstituteDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				boolean visible = CourseDetailWizardPage.this.withSubstituteDate.getSelection();
				CourseDetailWizardPage.this.ersatzStartLabel.setVisible(visible);
				CourseDetailWizardPage.this.substituteStart.setVisible(visible);
				CourseDetailWizardPage.this.ersatzEndLabel.setVisible(visible);
				CourseDetailWizardPage.this.substituteEnd.setVisible(visible);
			}
		});

		this.ersatzStartLabel = new Label(composite, SWT.NONE);
		this.ersatzStartLabel.setLayoutData(new GridData());
		this.ersatzStartLabel.setText("Beginn");

		this.substituteStart = new CDateTime(composite, CDT.BORDER | CDT.SPINNER | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.substituteStart.setLayoutData(new GridData());
		this.substituteStart.addSelectionListener(this);

		this.ersatzEndLabel = new Label(composite, SWT.NONE);
		this.ersatzEndLabel.setLayoutData(new GridData());
		this.ersatzEndLabel.setText("Ende");

		this.substituteEnd = new CDateTime(composite, CDT.BORDER | CDT.SPINNER | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.substituteEnd.setLayoutData(new GridData());
		this.substituteEnd.addSelectionListener(this);

		this.setValues();

		this.setControl(composite);
	}

	private Calendar getEnd()
	{
		if (this.end.getSelection() == null)
		{
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(this.end.getSelection());
		return calendar;
	}

	private String getJourney()
	{
		return this.journey.getText();
	}

	private String getLocation()
	{
		return this.location.getText();
	}

	private String getMeetingPoint()
	{
		return this.meetingPoint.getText();
	}

	private Calendar getStart()
	{
		if (this.start.getSelection() == null)
		{
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(this.start.getSelection());
		return calendar;
	}

	private Calendar getSubstituteEnd()
	{
		if (this.substituteEnd.getSelection() == null)
		{
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(this.substituteEnd.getSelection());
		return calendar;
	}

	private Calendar getSubstituteStart()
	{
		if (this.substituteStart.getSelection() == null)
			return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(this.substituteStart.getSelection());
		return calendar;
	}

	@Override
	public boolean isPageComplete()
	{
		return true;
	}

	private void setEndDate()
	{
		CourseDetailWizard wizard = (CourseDetailWizard) this.getWizard();
		if (wizard.getCourseDetail().getEnd() != null)
		{
			this.end.setSelection(wizard.getCourseDetail().getEnd().getTime());
		}
	}

	private void setJourney()
	{
		CourseDetailWizard wizard = (CourseDetailWizard) this.getWizard();
		this.journey.setText(wizard.getCourseDetail().getJourney());
	}

	private void setLocation()
	{
		CourseDetailWizard wizard = (CourseDetailWizard) this.getWizard();
		this.location.setText(wizard.getCourseDetail().getLocation());
	}

	private void setMeetingPoint()
	{
		CourseDetailWizard wizard = (CourseDetailWizard) this.getWizard();
		this.meetingPoint.setText(wizard.getCourseDetail().getMeetingPoint());
	}

	private void setStartDate()
	{
		CourseDetailWizard wizard = (CourseDetailWizard) this.getWizard();
		if (wizard.getCourseDetail().getStart() != null)
		{
			this.start.setSelection(wizard.getCourseDetail().getStart().getTime());
		}
	}

	private void setSubstituteEnd()
	{
		CourseDetailWizard wizard = (CourseDetailWizard) this.getWizard();
		if (wizard.getCourseDetail().getSubstituteEnd() != null)
		{
			this.substituteEnd.setSelection(wizard.getCourseDetail().getSubstituteEnd().getTime());
		}
	}

	private void setSubstituteStart()
	{
		CourseDetailWizard wizard = (CourseDetailWizard) this.getWizard();
		if (wizard.getCourseDetail().getSubstituteStart() != null)
		{
			this.substituteStart.setSelection(wizard.getCourseDetail().getSubstituteStart().getTime());
		}
	}

	private void setValues()
	{
		this.setStartDate();
		this.setEndDate();
		this.setWithSubstituteDate();
		this.setSubstituteStart();
		this.setSubstituteEnd();
		this.setLocation();
		this.setMeetingPoint();
		this.setJourney();
	}

	private void setWithSubstituteDate()
	{
		CourseDetailWizard wizard = (CourseDetailWizard) this.getWizard();
		this.withSubstituteDate.setSelection(wizard.getCourseDetail().isWithSubstituteDate());

		boolean visible = this.withSubstituteDate.getSelection();
		this.ersatzStartLabel.setVisible(visible);
		this.substituteStart.setVisible(visible);
		this.ersatzEndLabel.setVisible(visible);
		this.substituteEnd.setVisible(visible);
	}

	public CourseDetail updateCourseDetail(final CourseDetail courseDetail)
	{
		courseDetail.setStart(this.getStart());
		courseDetail.setEnd(this.getEnd());
		courseDetail.setWithSubstituteDate(this.withSubstituteDate.getSelection());
		courseDetail.setSubstituteStart(this.getSubstituteStart());
		courseDetail.setSubstituteEnd(this.getSubstituteEnd());
		courseDetail.setLocation(this.getLocation());
		courseDetail.setMeetingPoint(this.getMeetingPoint());
		courseDetail.setJourney(this.getJourney());
		return courseDetail;
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event)
	{
		this.widgetSelected(event);
	}

	@Override
	public void widgetSelected(final SelectionEvent event)
	{
		if (event.widget.equals(this.start))
		{
			// this.handleStartEvent();
		}
		else if (event.widget.equals(this.end))
		{
			// this.handleEndEvent();
		}
		else if (event.widget.equals(this.substituteStart))
		{
			// this.handleSubstituteStartEvent();
		}
		else if (event.widget.equals(this.substituteEnd))
		{
			// this.handleSubstituteEndEvent();
		}
	}

}
