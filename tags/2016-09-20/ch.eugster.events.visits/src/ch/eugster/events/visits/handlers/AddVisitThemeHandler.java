package ch.eugster.events.visits.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.persistence.model.VisitTheme;
import ch.eugster.events.visits.editors.VisitThemeEditor;
import ch.eugster.events.visits.editors.VisitThemeEditorInput;

public class AddVisitThemeHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		VisitTheme theme = VisitTheme.newInstance();
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new VisitThemeEditorInput(theme), VisitThemeEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
