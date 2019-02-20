package ch.eugster.events.person.editors;

import org.eclipse.jface.fieldassist.IContentProposal;

import ch.eugster.events.persistence.model.ZipCode;

public class CityContentProposal implements IContentProposal
{
	private final ZipCode zipCode;

	public CityContentProposal(ZipCode zipCode)
	{
		this.zipCode = zipCode;
	}

	public ZipCode getZipCode()
	{
		return this.zipCode;
	}

	@Override
	public String getContent()
	{
		if (this.zipCode == null)
		{
			return "";
		}
		return this.zipCode.getCity();
	}

	@Override
	public int getCursorPosition()
	{
		return 0;
	}

	@Override
	public String getDescription()
	{
		return "Auswahl der gefundenen Ortsbezeichnungen";
	}

	@Override
	public String getLabel()
	{
		if (this.zipCode == null)
		{
			return "";
		}
		return this.zipCode.getCity() + " (" + zipCode.getCountry().getIso3166alpha2() + ")";
	}

}
