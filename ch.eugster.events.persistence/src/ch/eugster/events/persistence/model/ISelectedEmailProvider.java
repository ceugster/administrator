package ch.eugster.events.persistence.model;

public interface ISelectedEmailProvider
{
	SelectedEmail getSelectedEmail();

	String getEmail(SelectedEmail selectedEmail);
}
