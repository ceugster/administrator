package ch.eugster.events.donation.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.TableItem;

import ch.eugster.events.donation.views.DonationView;
import ch.eugster.events.persistence.model.Donation;

public class SelectAllHandler extends AbstractHandler implements IHandler
{
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			Object activePart = context.getVariable("activePart");
			if (activePart instanceof DonationView)
			{
				DonationView view = (DonationView) activePart;
				TableItem[] items = view.getViewer().getTable().getItems();
				List<Donation> donations = new ArrayList<Donation>();
				for (TableItem item : items)
				{
					Donation donation = (Donation) item.getData();
					donations.add(donation);
				}
				StructuredSelection ssel = new StructuredSelection(donations.toArray(new Donation[0]));
				view.getViewer().setSelection(ssel);
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		setBaseEnabled(true);
	}

}
