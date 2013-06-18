package ch.eugster.events.ui.editors;

public interface Dirtyable
{
	boolean isDirty();

	void setDirty();

	void setDirty(boolean dirty);
}
