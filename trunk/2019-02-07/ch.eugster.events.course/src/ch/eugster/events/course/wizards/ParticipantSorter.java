package ch.eugster.events.course.wizards;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TableColumn;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.model.Participant;

public class ParticipantSorter extends ViewerSorter implements SelectionListener
{
	private final Viewer viewer;

	private int currentColumn;

	private boolean asc = true;

	public ParticipantSorter(Viewer viewer)
	{
		super();
		this.viewer = viewer;
	}

	@Override
	public int compare(Viewer viewer, Object element1, Object element2)
	{

		Participant p1 = (Participant) element1;
		Participant p2 = (Participant) element2;

		switch (this.currentColumn)
		{
			case 0:
				return this.compare(p1.getLink().getPerson().getId(), p2.getLink().getPerson().getId());
			case 1:
				return this.compare(p1.getLink().getPerson().getLastname(), p2.getLink().getPerson().getLastname());
			case 2:
				return this.compare(p1.getLink().getPerson().getFirstname(), p2.getLink().getPerson().getFirstname());
			case 3:
				return this.compare(p1.getLink().getAddress().getAddress(), p2.getLink().getAddress().getAddress());
			case 4:
			{
				String city1 = AddressFormatter.getInstance().formatCityLine(p1.getLink().getAddress());
				String city2 = AddressFormatter.getInstance().formatCityLine(p2.getLink().getAddress());
				return this.compare(city1, city2);
			}
			case 5:
				return this.compare(p1.getLink().getPhone(), p2.getLink().getPhone());
			case 6:
				return this.compare(p1.getLink().getPerson().getPhone(), p2.getLink().getPerson().getPhone());
			case 7:
				return this.compare(p1.getLink().getPerson().getEmail(), p2.getLink().getPerson().getEmail());
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

	@Override
	public void widgetSelected(SelectionEvent event)
	{
		if (event.widget instanceof TableColumn)
		{
			if (event.widget.getData() instanceof Integer)
			{
				int col = ((Integer) event.widget.getData()).intValue();
				if (col == this.currentColumn)
					this.asc = !this.asc;
				else
					this.currentColumn = col;
				this.viewer.refresh();
			}
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent event)
	{
		this.widgetSelected(event);
	}
}
