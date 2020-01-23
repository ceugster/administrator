package ch.eugster.events.ui.dnd;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import ch.eugster.events.persistence.model.AbstractEntity;

public class CourseDragSourceListener implements DragSourceListener
{
	private final Viewer sourceViewer;

	public CourseDragSourceListener(Viewer viewer)
	{
		this.sourceViewer = viewer;
	}

	@Override
	public void dragFinished(DragSourceEvent event)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void dragSetData(DragSourceEvent event)
	{
		IStructuredSelection ssel = (IStructuredSelection) this.sourceViewer.getSelection();
		Object[] objects = ssel.toArray();
		AbstractEntity[] entities = new AbstractEntity[objects.length];
		for (int i = 0; i < objects.length; i++)
		{
			entities[i] = (AbstractEntity) objects[i];
		}
		CourseTransfer.getTransfer().setData(event.detail, entities);
	}

	@Override
	public void dragStart(DragSourceEvent event)
	{
		// TODO Auto-generated method stub

	}

}
