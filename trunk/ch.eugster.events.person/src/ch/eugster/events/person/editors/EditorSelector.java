package ch.eugster.events.person.editors;

import org.eclipse.ui.IEditorInput;

import ch.eugster.events.persistence.model.LinkPersonAddress;

public enum EditorSelector
{
	SINGLE_PAGE_EDITOR, MULTI_PAGE_EDITOR;

	public String getEditorId()
	{
		switch (this)
		{
			case SINGLE_PAGE_EDITOR:
			{
				return PersonEditor.ID;
			}
			case MULTI_PAGE_EDITOR:
			{
				return PersonFormEditor.ID;
			}
			default:
			{
				throw new RuntimeException("Invalid editor selector");
			}
		}
	}

	public IEditorInput getEditorInput(final LinkPersonAddress link)
	{
		switch (this)
		{
			case SINGLE_PAGE_EDITOR:
			{
				return new LinkPersonAddressEditorInput(link);
			}
			case MULTI_PAGE_EDITOR:
			{
				return new PersonEditorInput(link.getPerson());
			}
			default:
			{
				throw new RuntimeException("Invalid editor selector");
			}
		}
	}

	public String key()
	{
		switch (this)
		{
			case SINGLE_PAGE_EDITOR:
			{
				return "single.page";
			}
			case MULTI_PAGE_EDITOR:
			{
				return "multi.page";
			}
			default:
			{
				throw new RuntimeException("Invalid editor selector");
			}
		}
	}

	public String label()
	{
		switch (this)
		{
			case SINGLE_PAGE_EDITOR:
			{
				return "Einseitiger Editor (mit eingeschränkter Funktionalität)";
			}
			case MULTI_PAGE_EDITOR:
			{
				return "Mehrseitiger Editor (eine Seite Person, je eine Seite pro Adresse)";
			}
			default:
			{
				throw new RuntimeException("Invalid editor selector");
			}
		}
	}

	public String value()
	{
		switch (this)
		{
			case SINGLE_PAGE_EDITOR:
			{
				return "0";
			}
			case MULTI_PAGE_EDITOR:
			{
				return "1";
			}
			default:
			{
				throw new RuntimeException("Invalid editor selector");
			}
		}
	}
}
