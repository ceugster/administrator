package ch.eugster.events.course.wizards;

import java.util.Calendar;

import org.eclipse.jface.wizard.Wizard;

import ch.eugster.events.course.views.CourseEditorContentOutlinePage.CourseDetailGroup;
import ch.eugster.events.persistence.model.CourseDetail;

public class CourseDetailWizard extends Wizard
{

	private final CourseDetailGroup courseDetailGroup;

	private CourseDetail courseDetail;

	public CourseDetailWizard(CourseDetailGroup courseDetailGroup, CourseDetail courseDetail)
	{
		this.courseDetailGroup = courseDetailGroup;
		this.courseDetail = courseDetail;
		if (courseDetail.getId() == null)
		{
			this.setDefault();
		}
	}

	private void setDefault()
	{
		this.courseDetail.setStart(Calendar.getInstance());
		this.courseDetail.setEnd(Calendar.getInstance());
		this.courseDetail.setLocation("");
	}

	@Override
	public void addPages()
	{
		this.addPage(new CourseDetailWizardPage("courseDetailWizardPage"));
	}

	@Override
	public boolean canFinish()
	{
		return true;
	}

	public CourseDetail getCourseDetail()
	{
		return this.courseDetail;
	}

	@Override
	public boolean performFinish()
	{
		CourseDetailWizardPage wizardPage = (CourseDetailWizardPage) this.getPage("courseDetailWizardPage");
		this.courseDetail = wizardPage.updateCourseDetail(this.courseDetail);
		if (!this.courseDetailGroup.getCourseDetails().contains(this.courseDetail))
		{
			this.courseDetailGroup.addCourseDetail(this.courseDetail);
		}
		return true;
	}

}
