package ch.eugster.events.course.wizards;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.BookingTypeProposition;

public class BookingTypePropositionLabelProvider extends LabelProvider implements IBaseLabelProvider
{
	private NumberFormat nf;

	public BookingTypePropositionLabelProvider()
	{
		nf = DecimalFormat.getCurrencyInstance();
	}
	
	@Override
	public String getText(Object element)
	{
		if (element instanceof BookingTypeProposition)
		{
			BookingTypeProposition proposition = (BookingTypeProposition) element;
			StringBuilder builder = new StringBuilder();
			if (!proposition.getCode().isEmpty() && !proposition.getName().isEmpty())
			{
				builder = builder.append(proposition.getCode() + " - " + proposition.getName());
			}
			else
			{
				builder = builder.append(proposition.getCode() + proposition.getName());
			}
			if (proposition.getPrice() != 0d || proposition.getMaxAge() != 0)
			{
				builder = builder.append(" (");
			}
			if (proposition.getPrice() != 0d)
			{
				builder = builder.append(nf.format(proposition.getPrice()));
			}
			if (proposition.getPrice() != 0d && proposition.getMaxAge() != 0)
			{
				builder = builder.append(", ");
			}
			if (proposition.getMaxAge() != 0)
			{
				builder = builder.append(DecimalFormat.getIntegerInstance().format(proposition.getMaxAge()));
			}
			if (proposition.getPrice() != 0d || proposition.getMaxAge() != 0)
			{
				builder = builder.append(")");
			}
			return builder.toString();
		}
		return "";
	}

}
