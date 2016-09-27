package ch.eugster.events.visits.editors;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;

import ch.eugster.events.persistence.model.SelectedEmail;
import ch.eugster.events.persistence.model.Visitor;

public class VisitorEmailLabelProvider extends LabelProvider
{
//	private final ComboViewer viewer;

	private Visitor visitor;

	public void setVisitor(Visitor visitor)
	{
		this.visitor = visitor;
	}

	public VisitorEmailLabelProvider(ComboViewer viewer)
	{
//		this.viewer = viewer;
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
			if (element instanceof SelectedEmail)
			{
				return visitor.getEmail((SelectedEmail) element);
			}
			return "";
		}
	}

}
