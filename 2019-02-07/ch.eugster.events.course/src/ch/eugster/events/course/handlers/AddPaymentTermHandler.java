package ch.eugster.events.course.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.course.editors.PaymentTermEditor;
import ch.eugster.events.course.editors.PaymentTermEditorInput;
import ch.eugster.events.course.views.PaymentTermView;
import ch.eugster.events.persistence.model.PaymentTerm;

public class AddPaymentTermHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("activePart") instanceof PaymentTermView)
		{
			try
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.openEditor(new PaymentTermEditorInput(PaymentTerm.newInstance()), PaymentTermEditor.ID);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
		return Status.OK_STATUS;
	}
}
