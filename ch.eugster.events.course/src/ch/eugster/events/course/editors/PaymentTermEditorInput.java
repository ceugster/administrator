package ch.eugster.events.course.editors;

import ch.eugster.events.persistence.model.PaymentTerm;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class PaymentTermEditorInput extends AbstractEntityEditorInput<PaymentTerm>
{
	public PaymentTermEditorInput(PaymentTerm paymentTerm)
	{
		entity = paymentTerm;
	}

	@Override
	public boolean hasParent()
	{
		return false;
	}

	@Override
	public String getName()
	{
		return "TTT";
	}

	@Override
	public String getToolTipText()
	{

		return "FFF";
	}

}
