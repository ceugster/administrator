package ch.eugster.events.course.wizards;

import org.eclipse.jface.wizard.Wizard;

import ch.eugster.events.course.views.CourseEditorContentOutlinePage.BookingTypeGroup;
import ch.eugster.events.persistence.model.BookingType;

public class BookingTypeWizard extends Wizard
{

	private final BookingTypeGroup bookingTypeGroup;

	private BookingType bookingType;

	public BookingTypeWizard(BookingTypeGroup bookingTypeGroup, BookingType bookingType)
	{
		this.bookingTypeGroup = bookingTypeGroup;
		this.bookingType = bookingType;
	}

	@Override
	public void addPages()
	{
		this.addPage(new BookingTypeWizardPage("bookingTypeWizardPage"));
	}

	@Override
	public boolean canFinish()
	{
		return true;
	}

	public BookingType getBookingType()
	{
		return this.bookingType;
	}

	@Override
	public boolean performFinish()
	{
		BookingTypeWizardPage wizardPage = (BookingTypeWizardPage) this.getPage("bookingTypeWizardPage");
		this.bookingType = wizardPage.updateBookingType(this.bookingType);
		if (!this.bookingTypeGroup.getBookingTypes().contains(this.bookingType))
			this.bookingTypeGroup.addBookingType(this.bookingType);
		return true;
	}

}
