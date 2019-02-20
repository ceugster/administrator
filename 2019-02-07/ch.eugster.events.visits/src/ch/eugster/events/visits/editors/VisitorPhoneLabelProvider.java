package ch.eugster.events.visits.editors;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.formattedtext.MaskFormatter;

import ch.eugster.events.persistence.model.SelectedPhone;
import ch.eugster.events.persistence.model.Visitor;

public class VisitorPhoneLabelProvider extends LabelProvider
{
	private Visitor visitor;

	public void setVisitor(Visitor visitor)
	{
		this.visitor = visitor;
	}

	@Override
	public String getText(Object element)
	{
		if (visitor == null)
		{
			return "";
		}
		else
		{
			if (element instanceof SelectedPhone)
			{
				SelectedPhone phone = (SelectedPhone) element;
				return getFormattedPhone(phone);
			}
			return "";
		}
	}

	private String getFormattedPhone(SelectedPhone selectedPhone)
	{
		String phoneNumber = visitor.getPhone(selectedPhone);
		if (phoneNumber.isEmpty())
		{
			return phoneNumber;
		}
		try
		{
			MaskFormatter formatter = new MaskFormatter(visitor.getLink().getAddress().getCountry().getPhonePattern());
			formatter.setValue(phoneNumber);
			return formatter.getDisplayString();
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		return "";
	}

}
