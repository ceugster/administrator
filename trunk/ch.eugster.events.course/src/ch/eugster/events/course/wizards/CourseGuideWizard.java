package ch.eugster.events.course.wizards;

import org.eclipse.jface.wizard.Wizard;

import ch.eugster.events.course.views.CourseEditorContentOutlinePage.CourseGuideGroup;
import ch.eugster.events.persistence.model.CourseGuide;

public class CourseGuideWizard extends Wizard {

	private CourseGuideGroup courseGuideGroup;

	private CourseGuide courseGuide;

	public CourseGuideWizard(CourseGuideGroup courseGuideGroup, CourseGuide courseGuide) 
	{
		this.courseGuideGroup = courseGuideGroup;
		this.courseGuide = courseGuide;
	}

	@Override
	public void addPages() 
	{
		this.addPage(new CourseGuideWizardPage("courseGuideWizardPage"));
	}

	@Override
	public boolean canFinish() {
		return this.getPage("courseGuideWizardPage").isPageComplete();
	}

	public CourseGuide getCourseGuide() 
	{
		return this.courseGuide;
	}

	@Override
	public boolean performFinish() 
	{
		CourseGuideWizardPage wizardPage = (CourseGuideWizardPage) this.getPage("courseGuideWizardPage");
		this.courseGuide = wizardPage.updateCourseGuide();
		if (!this.courseGuideGroup.getCourseGuides().contains(this.courseGuide))
			this.courseGuideGroup.addCourseGuide(this.courseGuide);
		return true;
	}

}
