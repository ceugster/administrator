package ch.eugster.events.ui.sorters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class StringSorter extends ViewerSorter
{
	private Order order = Order.ASCENDING;

	public StringSorter()
	{
	}

	public StringSorter(Order order)
	{
		this.order = order;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		String s1 = (String) e1;
		String s2 = (String) e2;

		if (this.order.equals(Order.ASCENDING))
			return s1.compareTo(s2);
		else if (this.order.equals(Order.DESCENDING))
			return s2.compareTo(s1);
		else
			return 0;
	}

	public enum Order
	{
		ASCENDING, DESCENDING;
	}

}
