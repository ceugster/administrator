package ch.eugster.events.course.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ch.eugster.events.persistence.model.BookingTypeProposition;

public class BasicBookingTypeSorter extends ViewerSorter
{
	private int columnIndex = 0;

	private Order order = Order.ASCENDING;

	public int getColumnIndex()
	{
		return this.columnIndex;
	}
	
	public void setColumnIndex(int index)
	{
		this.columnIndex = index;
	}
	
	public void changeOrder()
	{
		this.order = this.order.equals(Order.ASCENDING) ? Order.DESCENDING : Order.ASCENDING;
	}
	
	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2)
	{
		if (e1 instanceof BookingTypeProposition && e2 instanceof BookingTypeProposition)
		{
			BookingTypeProposition basicBookingType1 = (BookingTypeProposition) e1;
			BookingTypeProposition basicBookingType2 = (BookingTypeProposition) e2;
			return this.compare(basicBookingType1, basicBookingType2);
		}
		return 0;
	}

	private int compare(final BookingTypeProposition basicBookingType1, final BookingTypeProposition basicBookingType2)
	{
		switch (this.columnIndex)
		{
			case 0:
			{
				if (order.equals(Order.ASCENDING))
				{
					int result = basicBookingType1.getCode().compareTo(basicBookingType2.getCode());
					if (result == 0)
					{
						Double price1 = Double.valueOf(basicBookingType1.getPrice());
						Double price2 = Double.valueOf(basicBookingType2.getPrice());
						return price1.compareTo(price2);
					}
					return result;
				}
				else
				{
					int result = basicBookingType2.getCode().compareTo(basicBookingType1.getCode());
					if (result == 0)
					{
						Double price1 = Double.valueOf(basicBookingType1.getPrice());
						Double price2 = Double.valueOf(basicBookingType2.getPrice());
						return price2.compareTo(price1);
					}
					return result;
				}
			}
			case 1:
			{
				if (order.equals(Order.ASCENDING))
				{
					int result = basicBookingType1.getName().compareTo(basicBookingType2.getName());
					if (result == 0)
					{
						Double price1 = Double.valueOf(basicBookingType1.getPrice());
						Double price2 = Double.valueOf(basicBookingType2.getPrice());
						return price1.compareTo(price2);
					}
					return result;
				}
				else
				{
					int result = basicBookingType2.getName().compareTo(basicBookingType1.getName());
					if (result == 0)
					{
						Double price1 = Double.valueOf(basicBookingType1.getPrice());
						Double price2 = Double.valueOf(basicBookingType2.getPrice());
						return price2.compareTo(price1);
					}
					return result;
				}
			}
		}
		return 0;
	}

	private enum Order
	{
		ASCENDING, DESCENDING;
	}
}