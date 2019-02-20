package ch.eugster.events.person.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.PersonSex;

public class PersonSexSorter extends ViewerSorter
{
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof PersonSex && e2 instanceof PersonSex)
		{
			PersonSex sex1 = (PersonSex) e1;
			PersonSex sex2 = (PersonSex) e2;
			return sex1.getSalutation().compareTo(sex2.getSalutation());
		}
		else if (e1 instanceof String && e2 instanceof String)
		{
			String sex1 = (String) e1;
			String sex2 = (String) e2;
			return sex1.compareTo(sex2);
		}
		return 0;
	}
}
