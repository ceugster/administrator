package ch.eugster.events.persistence.model;

public interface ExtendedField
{
	FieldExtension getFieldExtension();

	Long getId();

	String getValue();

	void setValue(String value);
}
