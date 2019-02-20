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

import ch.eugster.events.course.editors.BasicBookingTypeEditor;
import ch.eugster.events.course.editors.BasicBookingTypeEditorInput;
import ch.eugster.events.course.views.BasicBookingTypeView;
import ch.eugster.events.persistence.model.BookingTypeProposition;

public class EditBasicBookingTypeHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		if (context.getVariable("activePart") instanceof BasicBookingTypeView)
		{
			BasicBookingTypeView view = (BasicBookingTypeView) context.getVariable("activePart");
			StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
			if (!ssel.isEmpty() && ssel.getFirstElement() instanceof BookingTypeProposition)
			{
				BookingTypeProposition basicBookingType = (BookingTypeProposition) ssel.getFirstElement();
				try
				{
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.openEditor(new BasicBookingTypeEditorInput(basicBookingType), BasicBookingTypeEditor.ID);
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
			this.setBaseEnabled(ssel.getFirstElement() instanceof BookingTypeProposition);
		}
	}

}
