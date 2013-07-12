package ch.eugster.events.person.editors;

import ch.eugster.events.persistence.model.DirtyMarkable;

public interface IPersonFormEditorPage extends DirtyMarkable
{
	void setDirty(boolean dirty);

	void loadValues();

	void saveValues();

	boolean validate();
}
