package ch.eugster.events.course.editors;

import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.PaymentTerm;

public class PaymentTermLabelProvider extends LabelProvider
{

	@Override
	public String getText(Object element)
	{
		if (element instanceof PaymentTerm)
		{
			return ((PaymentTerm) element).getText();
		}
		return "";
	}

}
