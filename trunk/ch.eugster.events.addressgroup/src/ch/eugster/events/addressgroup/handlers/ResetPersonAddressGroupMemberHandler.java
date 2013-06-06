package ch.eugster.events.addressgroup.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import ch.eugster.events.addressgroup.views.PersonAddressGroupMemberView;

public class ResetPersonAddressGroupMemberHandler extends AbstractHandler implements IHandler, IPropertyChangeListener
{
	private PersonAddressGroupMemberView view;

	public ResetPersonAddressGroupMemberHandler()
	{
	}

	@Override
	public void dispose()
	{
		if (view != null)
		{
			view.removePartPropertyListener(this);
		}
		super.dispose();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (view != null)
		{
			view.reset();
		}
		return null;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event)
	{
		if (event.getProperty().equals("dirty"))
		{
			String dirty = (String) event.getNewValue();
			if (dirty != null)
			{
				setBaseEnabled(dirty.equals("true"));
			}
		}
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		EvaluationContext context = (EvaluationContext) evaluationContext;
		if (context.getParent().getVariable("activePart") instanceof PersonAddressGroupMemberView)
		{
			view = (PersonAddressGroupMemberView) context.getParent().getVariable("activePart");
			view.addPartPropertyListener(this);
			this.setBaseEnabled(view.isDirty());
		}
	}

}
