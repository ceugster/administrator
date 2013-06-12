package ch.eugster.events.person.editors;

public interface Saveable
{
	void loadValues();

	void saveValues();

	void setDirty(boolean dirty);
}
