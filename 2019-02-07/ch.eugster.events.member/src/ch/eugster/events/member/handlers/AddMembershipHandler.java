package ch.eugster.events.member.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.member.editors.MembershipEditor;
import ch.eugster.events.member.editors.MembershipEditorInput;
import ch.eugster.events.member.views.MembershipView;
import ch.eugster.events.persistence.model.Membership;

public class AddMembershipHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		IViewPart viewPart = (IViewPart) context.getParent().getVariable("activePart");
		if (viewPart instanceof MembershipView)
		{
			Membership membership = Membership.newInstance();
			try
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.openEditor(new MembershipEditorInput(membership), MembershipEditor.ID);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

}
