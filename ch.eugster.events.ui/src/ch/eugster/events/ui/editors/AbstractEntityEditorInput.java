/*
 * Created on 18.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.ui.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import ch.eugster.events.persistence.model.AbstractEntity;

public abstract class AbstractEntityEditorInput<T extends AbstractEntity> implements IEditorInput
{
	protected T entity;

	@Override
	public boolean exists()
	{
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return null;
	}

	@Override
	public IPersistableElement getPersistable()
	{
		return null;
	}

	public T getEntity()
	{
		return this.entity;
	}

	public void setEntity(T entity)
	{
		this.entity = entity;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter)
	{
		if (adapter.getName().equals(this.entity.getClass().getName()))
			return this.entity;

		return null;
	}

	public AbstractEntity getParent()
	{
		return null;
	}

	public abstract boolean hasParent();

	@Override
	public boolean equals(Object object)
	{
		if (object != null && object.getClass().equals(this.getClass()))
		{
			AbstractEntityEditorInput<?> input = (AbstractEntityEditorInput<?>) object;
			if (this.entity.getId() != null && input.getEntity().getId() != null)
				return this.entity.getId().equals(input.getEntity().getId());
		}
		return false;
	}
}
