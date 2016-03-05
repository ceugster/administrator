package ch.eugster.events.course.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

import ch.eugster.events.persistence.model.Course;

public class CourseFilter extends ViewerFilter
{
	private Text filteredText;

	private Button filterButton;

	public CourseFilter(Text filteredText, Button filterButton)
	{
		this.filteredText = filteredText;
		this.filterButton = filterButton;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element)
	{
		if (filterButton.getSelection())
		{
			if (element instanceof Course)
			{
				Course course = (Course) element;
				if (course.getCode().toLowerCase().contains(filteredText.getText().toLowerCase()))
					return true;
				if (course.getTitle().toLowerCase().contains(filteredText.getText().toLowerCase()))
					return true;
				return false;
			}
			return true;
		}
		else
			return true;
	}

}
