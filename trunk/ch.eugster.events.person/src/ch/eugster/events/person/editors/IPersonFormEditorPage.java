package ch.eugster.events.person.editors;

public interface IPersonFormEditorPage
{
	void setDirty(boolean dirty);

	void loadValues();

	void saveValues();

	boolean validate();

	void setWidgetsActive(boolean active);

	boolean isWidgetsActive();
}
