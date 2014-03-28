package ch.eugster.events.donation.handlers;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;

import ch.eugster.events.donation.views.DonationView;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.ui.dnd.DonationTransfer;
import ch.eugster.events.ui.helpers.ClipboardHelper;

public class CopyHandler extends AbstractHandler implements IHandler
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
				TableViewer viewer = view.getViewer();
				StructuredSelection ssel = (StructuredSelection) viewer.getSelection();
				Object[] elements = ssel.toArray();

				Collection<Donation> donations = new ArrayList<Donation>();
				for (Object element : elements)
				{
					if (element instanceof Donation)
					{
						donations.add((Donation) element);
					}
				}
				setContent(donations.toArray(new Donation[0]));
			}
		}
		return Status.OK_STATUS;
	}

	private boolean isValidSelection(final DonationView view)
	{
		TableViewer viewer = view.getViewer();
		if (viewer.getSelection() instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) viewer.getSelection();
			Object[] elements = ssel.toArray();

			for (Object element : elements)
			{
				if (!(element instanceof Donation))
				{
					return false;
				}
			}
		}
		return true;
	}

	private void setContent(final Donation[] donations)
	{
		Transfer[] transfers = new Transfer[donations.length];
		for (int i = 0; i < donations.length; i++)
		{
			DonationTransfer transfer = DonationTransfer.getTransfer();
			transfer.setData(DND.DROP_COPY, donations);
			transfers[i] = transfer;
		}
		ClipboardHelper.getClipboard().setContents(donations, transfers);
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		boolean enabled = false;
		if (evaluationContext instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) evaluationContext;
			Object activePart = context.getVariable("activePart");
			if (activePart instanceof DonationView)
			{
				enabled = isValidSelection((DonationView) activePart);
			}
		}
		setBaseEnabled(enabled);
	}

}
