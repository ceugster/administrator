package ch.eugster.events.persistence.model;

public interface ISelectedPhoneProvider
{
	SelectedPhone getSelectedPhone();

	String getPhone(SelectedPhone selectedPhone);
}
