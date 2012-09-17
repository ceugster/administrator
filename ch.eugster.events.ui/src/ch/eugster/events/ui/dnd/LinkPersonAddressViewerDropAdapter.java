package ch.eugster.events.ui.dnd;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

import ch.eugster.events.persistence.model.IEntity;

public class LinkPersonAddressViewerDropAdapter extends ViewerDropAdapter
{
	public LinkPersonAddressViewerDropAdapter(Viewer viewer)
	{
		super(viewer);
	}

	@Override
	public boolean performDrop(Object data)
	{
		if (data instanceof IEntity[])
		{
			IEntity[] entities = (IEntity[]) data;
			if (this.getViewer() instanceof TableViewer)
			{
				TableViewer viewer = (TableViewer) this.getViewer();
				for (IEntity entity : entities)
					viewer.add(entity);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType)
	{
		if (EntityTransfer.getTransfer().isSupportedType(transferType))
		{
			return true;
		}
		return false;
	}

}
