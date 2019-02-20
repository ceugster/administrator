package ch.eugster.events.persistence.model;

public interface DirtyMarkable
{
	void setDirty(boolean dirty);
}
