package ch.eugster.events.guide.editors;

import ch.eugster.events.persistence.model.GuideType;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class GuideTypeEditorInput extends AbstractEntityEditorInput<GuideType>
{
	public GuideTypeEditorInput(GuideType guideType)
	{
		this.entity = guideType;
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
