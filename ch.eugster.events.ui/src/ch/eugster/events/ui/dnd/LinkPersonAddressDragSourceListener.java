package ch.eugster.events.ui.dnd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.IEntity;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;

public class LinkPersonAddressDragSourceListener implements DragSourceListener
{
	private final Viewer sourceViewer;

	public LinkPersonAddressDragSourceListener(Viewer viewer)
	{
		this.sourceViewer = viewer;
	}

	@Override
	public void dragFinished(DragSourceEvent event)
	{
	}

	@SuppressWarnings("unchecked")
	@Override
	public void dragSetData(DragSourceEvent event)
	{
		IStructuredSelection ssel = (IStructuredSelection) this.sourceViewer.getSelection();
		Collection<IEntity> links = new ArrayList<IEntity>();
		List<AbstractEntity> entities = ssel.toList();
		for (AbstractEntity entity : entities)
		{
			if (entity instanceof LinkPersonAddress)
			{
				links.add(entity);
			}
			else if (entity instanceof Person)
			{
				links.add(((Person) entity).getDefaultLink());
			}
			else if (entity instanceof Address)
			{
				links.add(entity);
			}
		}
		EntityTransfer.getTransfer().setData(event.detail, links.toArray(new IEntity[0]));
	}

	@Override
	public void dragStart(DragSourceEvent event)
	{
	}
}
