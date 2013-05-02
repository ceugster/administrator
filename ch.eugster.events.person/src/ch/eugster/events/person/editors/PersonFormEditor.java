package ch.eugster.events.person.editors;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
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
		return ((PersonEditorInput) this.getEditorInput()).getEntity().getPerson();
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
		IFormPage page = this.findPage("person.page");
		if (page != null && page.isDirty())
		{
			return true;
		}

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			AddressTypeQuery query = (AddressTypeQuery) service.getQuery(AddressType.class);
			Collection<AddressType> addressTypes = query.selectAll(true);
			for (AddressType addressType : addressTypes)
			{
				page = this.findPage("link.page." + addressType.getId().toString());
				if (page != null && page.isDirty())
				{
					return true;
				}
			}
		}
		tracker.close();
		return dirty;
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
		for (Object page : this.pages)
		{
			if (page instanceof FormEditorPersonPage)
			{
				FormEditorPersonPage formPage = (FormEditorPersonPage) page;
				if (formPage.getManagedForm() != null)
				{
					formPage.getManagedForm().getForm().setText(this.getEditorInput().getName());
					formPage.loadValues();
					formPage.setDirty(false);
				}
			}
			else if (page instanceof FormEditorLinkPage)
			{
				FormEditorLinkPage formPage = (FormEditorLinkPage) page;
				if (formPage.getLink().getId() == null)
				{
					pagesToRemove.add(page);
				}
				else
				{
					if (formPage.getManagedForm() != null)
					{
						formPage.getManagedForm().getForm().setText(this.getEditorInput().getName());
						formPage.loadValues();
						formPage.setDirty(false);
					}
				}
			}
			else if (page == null)
			{
				pagesToRemove.add(page);
			}
		}
		for (Object page : pagesToRemove)
		{
			if (page != null)
			{
				int index = this.pages.indexOf(page);
				this.removePage(index);
			}
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		Person person = getPerson();
		IFormPage page = this.findPage("person.page");
		if (page != null && page.isDirty())
		{
			((FormEditorPersonPage) page).saveValues();
		}

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			AddressTypeQuery query = (AddressTypeQuery) service.getQuery(AddressType.class);
			Collection<AddressType> addressTypes = query.selectAll(true);
			for (AddressType addressType : addressTypes)
			{
				page = this.findPage("link.page." + addressType.getId().toString());
				if (page == null)
				{
					Collection<LinkPersonAddress> links = person.getLinks();
					for (LinkPersonAddress link : links)
					{
						if (link.getAddressType().getId().equals(addressType.getId()))
						{
							link.setDeleted(true);
						}
					}
				}
				else
				{
					if (page.isDirty() && page instanceof FormEditorLinkPage)
					{
						FormEditorLinkPage linkPage = (FormEditorLinkPage) page;
						linkPage.saveValues();
						if (!person.getLinks().contains(linkPage.getLink()))
						{
							person.getLinks().add(linkPage.getLink());
						}
					}
				}
			}
		}
		tracker.close();
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
		IFormPage page = this.findPage("person.page");
		if (page != null && page.isDirty())
		{
			valid = ((FormEditorPersonPage) page).validate();
			if (!valid)
			{
				this.setActivePage(page.getId());
			}
		}

		if (valid)
		{
			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			tracker.open();
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				AddressTypeQuery query = (AddressTypeQuery) service.getQuery(AddressType.class);
				Collection<AddressType> addressTypes = query.selectAll(true);
				for (AddressType addressType : addressTypes)
				{
					page = this.findPage("link.page." + addressType.getId().toString());
					if (page != null && page.isDirty())
					{
						if (valid)
						{
							valid = ((FormEditorLinkPage) page).validate();
							if (!valid)
							{
								this.setActivePage(page.getId());
							}
						}
					}
				}
			}
			tracker.close();
		}
		return valid;
	}

}
