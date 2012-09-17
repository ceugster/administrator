package ch.eugster.events.member.dialog;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.Membership;

public class MemberSorter extends ViewerSorter
{
	private final MemberDialog dialog;

	public MemberSorter(MemberDialog dialog)
	{
		super();
		this.dialog = dialog;
	}

	@Override
	public int compare(Viewer viewer, Object element1, Object element2)
	{
		if (element1 instanceof Membership && element2 instanceof Membership)
		{
			Membership membership1 = (Membership) element1;
			Membership membership2 = (Membership) element2;

			boolean checked1 = dialog.isChecked(membership1);
			boolean checked2 = dialog.isChecked(membership2);

			if (checked1 && checked2)
				return membership1.getCode().compareTo(membership2.getCode());
			else if (checked1)
				return -1;
			else if (checked2)
				return 1;
			else
				return membership1.getCode().compareTo(membership2.getCode());
		}

		return 0;
	}
}
