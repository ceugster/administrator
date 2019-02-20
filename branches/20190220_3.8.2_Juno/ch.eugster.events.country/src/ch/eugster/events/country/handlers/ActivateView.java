package ch.eugster.events.country.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.country.views.CountryView;

public class ActivateView extends AbstractHandler implements IHandler
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
				IWorkbenchPage page = window.getActivePage();
				page.showView(CountryView.ID);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

}
