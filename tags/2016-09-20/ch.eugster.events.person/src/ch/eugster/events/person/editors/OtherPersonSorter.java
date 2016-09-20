package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TableColumn;

import ch.eugster.events.persistence.model.LinkPersonAddress;

public class OtherPersonSorter extends ViewerSorter implements SelectionListener
{
	private Viewer viewer;

	private int currentColumn;

	private boolean asc = true;

	public OtherPersonSorter(Viewer viewer)
	{
		super();
		this.viewer = viewer;
	}

	@Override
	public int compare(Viewer viewer, Object element1, Object element2)
	{

		LinkPersonAddress link1 = (LinkPersonAddress) element1;
		LinkPersonAddress link2 = (LinkPersonAddress) element2;

		switch (this.currentColumn)
		{
		case 0:
			return this.compare(link1.getPerson().getId(), link2.getPerson().getId());
		case 1:
			return this.compare(link1.getPerson().getLastname(), link2.getPerson().getLastname());
		case 2:
			return this.compare(link1.getPerson().getFirstname(), link2.getPerson().getFirstname());
		default:
			return 0;
		}
	}

	public int compare(Long d1, Long d2)
	{
		if (this.asc)
			return d1.compareTo(d2);
		else
			return d2.compareTo(d1);
	}

	public int compare(String s1, String s2)
	{
		if (this.asc)
			return s1.compareTo(s2);
		else
			return s2.compareTo(s1);
	}

	public void widgetSelected(SelectionEvent event)
	{
		if (event.widget instanceof TableColumn)
		{
			if (event.widget.getData() instanceof Integer)
			{
				int newColumn = ((Integer) event.widget.getData()).intValue();
				if (newColumn == this.currentColumn)
					this.asc = !this.asc;
				else
					this.currentColumn = newColumn;
				this.viewer.refresh();
			}
		}
	}

	public void widgetDefaultSelected(SelectionEvent event)
	{
		this.widgetSelected(event);
	}
}
