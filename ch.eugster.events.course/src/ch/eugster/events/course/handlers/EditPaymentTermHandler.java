package ch.eugster.events.course.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.course.editors.PaymentTermEditor;
import ch.eugster.events.course.editors.PaymentTermEditorInput;
import ch.eugster.events.course.views.PaymentTermView;
import ch.eugster.events.persistence.model.PaymentTerm;

public class EditPaymentTermHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("activePart") instanceof PaymentTermView)
		{
			PaymentTermView view = (PaymentTermView) context.getVariable("activePart");
			StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
			if (!ssel.isEmpty() && ssel.getFirstElement() instanceof PaymentTerm)
			{
				PaymentTerm paymentTerm = (PaymentTerm) ssel.getFirstElement();
				try
				{
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.openEditor(new PaymentTermEditorInput(paymentTerm), PaymentTermEditor.ID);
				}
				catch (PartInitException e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getParent().getVariable("selection") instanceof IStructuredSelection)
		{
			IStructuredSelection ssel = (IStructuredSelection) context.getVariable("selection");
			this.setBaseEnabled(ssel.getFirstElement() instanceof PaymentTerm);
		}
	}

}
