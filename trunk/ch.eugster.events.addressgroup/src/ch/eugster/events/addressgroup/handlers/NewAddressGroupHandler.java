package ch.eugster.events.addressgroup.handlers;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.menus.UIElement;

import ch.eugster.events.addressgroup.editors.AddressGroupEditor;
import ch.eugster.events.addressgroup.editors.AddressGroupEditorInput;
import ch.eugster.events.addressgroup.views.AddressGroupView;
import ch.eugster.events.addressgroup.views.UpdateableRightCommandHandler;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;

public class NewAddressGroupHandler extends UpdateableRightCommandHandler
{
	public static final String COMMAND_ID = "ch.eugster.events.addressgroup.add";

	public static final String STATE_ID = "addressgroup.state";

	private AddressGroupView view;

	public NewAddressGroupHandler()
	{
		super();
	}

	public String getCommandId()
	{
		return COMMAND_ID;
	}

	@Override
	public Collection<String> getAvailableStateIds()
	{
		return Collections.singleton(STATE_ID);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		Object ctx = event.getApplicationContext();
		if (ctx instanceof EvaluationContext)
		{
			EvaluationContext context = (EvaluationContext) ctx;
			Object object = context.getVariable("activePart");
			if (object instanceof AddressGroupView)
			{
				AddressGroupView view = (AddressGroupView) object;
				StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
				if (ssel.size() == 1 && ssel.getFirstElement() instanceof AddressGroupCategory)
				{
					AddressGroupCategory category = (AddressGroupCategory) ssel.getFirstElement();
					AddressGroup group = AddressGroup.newInstance(category);
					AddressGroupEditorInput input = new AddressGroupEditorInput(group);
					try
					{
						IWorkbenchWindow window = (IWorkbenchWindow) context.getVariable("activeWorkbenchWindow");
						window.getActivePage().openEditor(input, AddressGroupEditor.ID);
					}
					catch (PartInitException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public void updateElement(UIElement element, Map parameters)
	{
		if (view instanceof AddressGroupView)
		{
			IStructuredSelection ssel = (IStructuredSelection) view.getViewer().getSelection();
			if (ssel.getFirstElement() instanceof AddressGroupCategory)
			{
				AddressGroupCategory category = (AddressGroupCategory) ssel.getFirstElement();
				String name = category.getName();
				if (name.length() > 1 && name.endsWith("n"))
				{
					name = name.substring(0, name.length() - 1);
				}

				element.setText("Neue " + name);
				return;
			}
		}
		element.setText("Neue Gruppe");
	}

	public void handleStateChange(State state, Object oldValue)
	{
		Object value = state.getValue();
		if (value instanceof AddressGroupView)
		{
			this.view = (AddressGroupView) value;
		}
	}
}
