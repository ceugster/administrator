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

	private IContentOutlinePage contentOutlinePage;

	public PersonFormEditor()
	{
	}

	@Override
	public int addPage(final IFormPage page) throws PartInitException
	{
		page.addPropertyListener(this);
		int pagenum = super.addPage(page);
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
			this.setActivePage(this.getPageCount() > 1 ? 0 : 0);
		}
		catch (PartInitException e)
		{
		}
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

	@Override
	protected void setDirty(boolean dirty)
	{
		Collection<IFormPage> pages = this.getPages();
		for (IFormPage page : pages)
		{
			if (page instanceof IPersonFormEditorPage)
			{
				IPersonFormEditorPage editorPage = (IPersonFormEditorPage) page;
				editorPage.setDirty(dirty);
			}
		}
	}

	@Override
	protected void setWidgetsActive(boolean active)
	{
		Collection<IFormPage> pages = this.getPages();
		for (IFormPage page : pages)
		{
			if (page instanceof IPersonFormEditorPage)
			{
				IPersonFormEditorPage editorPage = (IPersonFormEditorPage) page;
				editorPage.setWidgetsActive(active);
			}
		}
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
	public void removePage(final int page)
	{
		Object object = pages.get(page);
		if (object instanceof FormEditorLinkPage)
		{
			FormEditorLinkPage linkPage = (FormEditorLinkPage) object;
			linkPage.getLink().setDeleted(true);
			linkPage.removePropertyListener(this);
			super.removePage(page);
		}
	}

	public void removePage(final String id)
	{
		IFormPage[] pages = this.getPages().toArray(new IFormPage[0]);
		for (int i = 0; i < pages.length; i++)
		{
			if (pages[i] instanceof IFormPage)
			{
				if (pages[i].getId().equals(id))
				{
					removePage(i);
				}
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
					if (page instanceof IPersonFormEditorPage)
					{
						IPersonFormEditorPage editorPage = (IPersonFormEditorPage) page;
						editorPage.loadValues();
					}
				}
			}
		}
		for (Object page : pagesToRemove)
		{
			int index = this.pages.indexOf(page);
			this.removePage(index);
		}
	}

	@Override
	public void saveValues()
	{
		Collection<IFormPage> pages = this.getPages();
		for (IFormPage page : pages)
		{
			if (page != null && page instanceof IPersonFormEditorPage && page.isDirty())
			{
				((IPersonFormEditorPage) page).saveValues();
			}
		}
	}

	@Override
	protected void updateControls()
	{
		PersonEditorInput input = (PersonEditorInput) this.getEditorInput();
		this.setPartName(input.getName());
		for (IFormPage page : this.getPages())
		{
			if (page != null && page.getManagedForm() != null)
			{
				if (page instanceof FormEditorPersonPage)
				{
					FormEditorPersonPage fpage = (FormEditorPersonPage) page;
					page.getManagedForm().getForm().setText(fpage.getText());
				}
				else if (page instanceof FormEditorLinkPage)
				{
					FormEditorLinkPage fpage = (FormEditorLinkPage) page;
					page.getManagedForm().getForm().setText(fpage.getText());
				}
			}
		}
	}

	@Override
	protected boolean validate()
	{
		boolean valid = true;
		Collection<IFormPage> pages = this.getPages();
		for (IFormPage page : pages)
		{
			if (page != null && page.isDirty() && page instanceof IPersonFormEditorPage)
			{
				IPersonFormEditorPage editorPage = (IPersonFormEditorPage) page;
				valid = editorPage.validate();
				if (!valid)
				{
					this.setActivePage(page.getId());
					return false;
				}
			}
		}
		return valid;
	}

}
