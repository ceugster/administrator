package ch.eugster.events.category.editors;

import ch.eugster.events.persistence.model.Category;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class CategoryEditorInput extends AbstractEntityEditorInput<Category>
{
	public CategoryEditorInput(Category category)
	{
		entity = category;
	}

	@Override
	public boolean hasParent()
	{
		return false;
	}

	@Override
	public String getName()
	{
		return "TTT";
	}

	@Override
	public String getToolTipText()
	{

		return "FFF";
	}
}
