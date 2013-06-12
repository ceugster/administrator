package ch.eugster.events.person.editors;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.person.views.PersonFormEditorContentOutlinePage;
import ch.eugster.events.ui.editors.AbstractEntityFormEditor;

public class PersonFormEditor extends AbstractEntityFormEditor<Person>
{
	public static final String ID = "ch.eugster.events.person.editors.personFormEditor";

	private boolean dirty;

	private IContentOutlinePage contentOutlinePage;

	@Override
	public int addPage(final IFormPage page) throws PartInitException
	{
		page.addPropertyListener(this);
		int pagenum = super.addPage(page);
		setDirty(true);
		return pagenum;
	}

	@Override
	protected void addPages()
	{
		try
		{
			FormEditorPersonPage personPage = new FormEditorPersonPage(this, "person.page", "Person");
			this.addPage(personPage);

			Person person = getPerson();
			Collection<LinkPersonAddress> links = person.getLinks();
			for (LinkPersonAddress link : links)
			{
				if (!link.isDeleted())
				{
					String id = "link.page." + link.getAddressType().getId().toString();
					IFormPage page = new FormEditorLinkPage(this, personPage, id, link);
					this.addPage(page);
				}
			}
		}
		catch (PartInitException e)
		{
		}
		this.setActivePage(this.getPageCount() > 1 ? 0 : 0);
		setDirty(false);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Class adapter)
	{
		if (IContentOutlinePage.class.equals(adapter))
		{
			if (this.contentOutlinePage == null)
				this.contentOutlinePage = new PersonFormEditorContentOutlinePage((FormPage) this.getSelectedPage());
			return this.contentOutlinePage;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Collection<IFormPage> getPages()
	{
		return this.pages;
	}

	protected Person getPerson()
	{
		return ((PersonEditorInput) this.getEditorInput()).getEntity();
	}

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException
	{
		super.init(site, input);
		Person person = getPerson();
		setPartName(person.getId() == null ? "Neu" : PersonFormatter.getInstance().formatFirstnameLastname(person));
	}

	@Override
	public boolean isDirty()
	{
		Collection<IFormPage> pages = this.getPages();
		for (IFormPage page : pages)
		{
			if (page != null && page.isDirty())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void removePage(final int page)
	{
		setDirty(true);
		Object object = pages.get(page);
		if (object instanceof IFormPage)
		{
			((IFormPage) object).removePropertyListener(this);
		}
		super.removePage(page);
	}

	public void removePage(final String id)
	{
		IFormPage[] pages = this.getPages().toArray(new IFormPage[0]);
		for (int i = 0; i < pages.length; i++)
		{
			if (pages[i].getId().equals(id))
			{
				pages[i].removePropertyListener(this);
				super.removePage(i);
				setDirty(true);
				break;
			}
		}
	}

	@Override
	protected void reset()
	{
		Collection<Object> pagesToRemove = new ArrayList<Object>();
		Collection<IFormPage> pages = this.getPages();
		for (IFormPage page : pages)
		{
			if (page != null)
			{
				LinkPersonAddress link = (LinkPersonAddress) page.getAdapter(LinkPersonAddress.class);
				if (link != null && link.getId() == null)
				{
					pagesToRemove.add(page);
				}
				else if (page.getManagedForm() != null)
				{
					page.getManagedForm().getForm().setText(this.getEditorInput().getName());
					if (page instanceof Saveable)
					{
						Saveable saveable = (Saveable) page;
						saveable.loadValues();
						saveable.setDirty(false);
					}
				}
			}
		}
		for (Object page : pagesToRemove)
		{
			int index = this.pages.indexOf(page);
			this.removePage(index);
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		Collection<IFormPage> pages = this.getPages();
		for (IFormPage page : pages)
		{
			if (page != null && page.isDirty())
			{
				if (page instanceof Saveable)
				{
					((Saveable) page).saveValues();
				}
			}
		}
	}

	@Override
	public void setDirty(final boolean dirty)
	{
		this.dirty = dirty;
		this.firePropertyChange(PROP_DIRTY);
	}

	@Override
	protected boolean validate()
	{
		boolean valid = true;
		Collection<IFormPage> pages = this.getPages();
		for (IFormPage page : pages)
		{
			if (page != null && page.isDirty())
			{
				if (page instanceof Validateable)
				{
					valid = ((Validateable) page).validate();
					if (!valid)
					{
						this.setActivePage(page.getId());
						return false;
					}
				}
			}
		}
		return valid;
	}

}
