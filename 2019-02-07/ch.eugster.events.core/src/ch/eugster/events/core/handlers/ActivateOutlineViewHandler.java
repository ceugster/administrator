package ch.eugster.events.core.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

public class ActivateOutlineViewHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object ctx = event.getApplicationContext();
		if (ctx instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) ctx;
			IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
			try
			{
				window.getActivePage().showView(IPageLayout.ID_OUTLINE);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

}
