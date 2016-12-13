package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.progress.UIJob;

import ch.eugster.events.addressgroup.views.AddressGroupMemberView;
import ch.eugster.events.addressgroup.views.AddressGroupView;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.persistence.queries.AddressGroupMemberQuery;
import ch.eugster.events.persistence.queries.AddressGroupQuery;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class DeleteHandler extends ConnectionServiceDependentAbstractHandler
{

	public boolean confirmed(final IViewPart view)
	{
		return MessageDialog.openQuestion(view.getViewSite().getShell(), "Löschbestätigung",
				"Sollen die ausgewählten Elemente entfernt werden?");
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (event.getApplicationContext() instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			Object activePart = context.getVariable("activePart");
			if (activePart instanceof AddressGroupView)
			{
				AddressGroupView view = (AddressGroupView) activePart;
				if (confirmed(view))
				{
					execute(view.getViewer());
				}
			}
			else if (activePart instanceof AddressGroupMemberView)
			{
				AddressGroupMemberView view = (AddressGroupMemberView) activePart;
				if (confirmed(view))
				{
					execute(view.getViewer());
				}
			}
		}
		return Status.OK_STATUS;
	}

	private void execute(final StructuredViewer viewer)
	{
		if (connectionService != null)
		{
			UIJob job = new UIJob("Entferne gewählte Elemente...")
			{
				@Override
				public IStatus runInUIThread(final IProgressMonitor monitor)
				{
					IStatus status = Status.OK_STATUS;
					StructuredSelection ssel = (StructuredSelection) viewer.getSelection();
					monitor.beginTask("Entferne ausgewählte Elemente...", ssel.size());
					Object[] elements = ssel.toArray();
					for (Object element : elements)
					{
						if (element instanceof AddressGroupCategory)
						{
							AddressGroupCategory category = (AddressGroupCategory) element;
							AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) connectionService.getQuery(category
									.getClass());
							query.delete(category);
						}
						else if (element instanceof AddressGroup)
						{
							AddressGroup group = (AddressGroup) element;
							AddressGroupQuery query = (AddressGroupQuery) connectionService.getQuery(group.getClass());
							query.delete(group);
						}
						// else if (element instanceof AddressGroupLink)
						// {
						// AddressGroupLink link = (AddressGroupLink)
						// element;
						// AddressGroupLinkQuery query =
						// (AddressGroupLinkQuery)
						// service.getQuery(link.getClass());
						// query.delete(link);
						// }
						else if (element instanceof AddressGroupMember)
						{
							AddressGroupMember member = (AddressGroupMember) element;
							AddressGroupMemberQuery query = (AddressGroupMemberQuery) connectionService.getQuery(member
									.getClass());
							query.delete(member);
						}
						monitor.worked(1);
					}
					monitor.done();
					return status;
				}
			};
			job.setUser(true);
			job.schedule();
		}
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getVariable("selection") instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) context.getVariable("selection");
			setBaseEnabled(ssel.size() > 0);
		}
	}
}
