package ch.eugster.events.member.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.member.editors.MembershipEditor;
import ch.eugster.events.member.editors.MembershipEditorInput;
import ch.eugster.events.persistence.model.Membership;

public class EditMembershipHandler extends AbstractHandler implements IHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		EvaluationContext context = (EvaluationContext) event.getApplicationContext();
		ISelection sel = (ISelection) context.getVariable("selection");
		{
			if (sel instanceof StructuredSelection)
			{
				StructuredSelection ssel = (StructuredSelection) sel;
				{
					if (!ssel.isEmpty() && ssel.getFirstElement() instanceof Membership)
					{
						Membership membership = (Membership) ssel.getFirstElement();
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
				}
			}
		}
		return null;
	}

}
