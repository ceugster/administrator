package ch.eugster.events.persistence.filters;

import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.AbstractEntity;

public class ExcludeListedEntitiesFilter extends AbstractEntityViewerFilter
{
	private AbstractEntity[] excludeEntities;

	public ExcludeListedEntitiesFilter(AbstractEntity[] excludeEntities)
	{
		this.excludeEntities = excludeEntities;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element)
	{
		if (this.excludeEntities == null || this.excludeEntities.length == 0) return true;

		if (element instanceof AbstractEntity)
			return this.select((AbstractEntity) element);

		return true;
	}

	public boolean select(AbstractEntity element)
	{
		if (this.excludeEntities == null || this.excludeEntities.length == 0) return true;

		for (AbstractEntity entity : this.excludeEntities)
		{
			if ((element).getId().equals(entity.getId()))
				return false;
		}
		return true;
	}

}
