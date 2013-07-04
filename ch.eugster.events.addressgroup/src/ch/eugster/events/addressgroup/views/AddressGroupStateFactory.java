package ch.eugster.events.addressgroup.views;

import org.eclipse.core.commands.State;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

public class AddressGroupStateFactory extends SingletonStateWrapper
{

	@Override
	public State createSingleton()
	{
		return getViewState();
	}

	public static ViewState getViewState()
	{
		return ViewState.INSTANCE;
	}

	public static final class ViewState extends UIState implements IPartListener
	{
		static final ViewState INSTANCE = new ViewState();

		private ViewState()
		{

		}

		public void partActivated(IWorkbenchPart part)
		{
			if (part instanceof AddressGroupView)
			{
				AddressGroupView view = (AddressGroupView) part;
				this.setView(view);
			}
		}

		public void partBroughtToTop(IWorkbenchPart part)
		{
			partActivated(part);
		}

		public void partClosed(IWorkbenchPart part)
		{

		}

		public void partDeactivated(IWorkbenchPart part)
		{

		}

		public void partOpened(IWorkbenchPart part)
		{

		}

		public void setView(AddressGroupView view)
		{
			setValue(view);
		}
	}

}
