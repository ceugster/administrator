package ch.eugster.events.member.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Member;

public class MemberSorter extends ViewerSorter
{
	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2)
	{
		Member d1 = (Member) e1;
		Member d2 = (Member) e2;

		if (d1.getLinkPersonAddress() == null && d2.getLinkPersonAddress() == null)
		{
			return d1.getAddress().getName().compareTo(d2.getAddress().getName());
		}
		else if (d1.getLinkPersonAddress() == null)
		{
			return d1.getAddress().getName().compareTo(d2.getLinkPersonAddress().getPerson().getLastname());
		}
		else if (d2.getLinkPersonAddress() == null)
		{
			String name1 = PersonFormatter.getInstance().formatLastnameFirstname(d1.getLinkPersonAddress().getPerson());
			String name2 = d2.getAddress().getName();
			return name1.compareTo(name2);
		}
		else
		{
			String name1 = PersonFormatter.getInstance().formatLastnameFirstname(d1.getLinkPersonAddress().getPerson());
			String name2 = PersonFormatter.getInstance().formatLastnameFirstname(d2.getLinkPersonAddress().getPerson());
			return name1.compareTo(name2);
		}
	}
}
