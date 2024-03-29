package ch.eugster.events.person.editors;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.views.PersonFormEditorContentOutlinePage;
import ch.eugster.events.ui.editors.AbstractEntityFormEditor;

public class PersonFormEditor extends AbstractEntityFormEditor<Person> implements EventHandler
{
	public static final String ID = "ch.eugster.events.person.editors.personFormEditor";

	private PersonFormEditorContentOutlinePage contentOutlinePage;

	private ServiceRegistration<EventHandler> eventHandlerRegistration;
	
	public PersonFormEditor()
	{
		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, "ch/eugster/events/persistence/merge");		
		eventHandlerRegistration = Activator.getDefault().getBundle().getBundleContext().registerService(EventHandler.class, this, properties);
	}

	@Override
	public void close(boolean save)
	{
		eventHandlerRegistration.unregister();
		super.close(save);
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
			List<LinkPersonAddress> links = person.getLinks();
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
	public List<IFormPage> getPages()
	{
		return this.pages == null ? new ArrayList<IFormPage>() : this.pages;
	}

	@Override
	protected void setDirty(boolean dirty)
	{
		List<IFormPage> pages = this.getPages();
		for (IFormPage page : pages)
		{
			if (page instanceof IPersonFormEditorPage)
			{
				IPersonFormEditorPage editorPage = (IPersonFormEditorPage) page;
				editorPage.setDirty(dirty);
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
			linkPage.removePropertyListener(this);
			super.removePage(page);

			IFormPage[] pages = this.getPages().toArray(new IFormPage[0]);
			for (int i = 0; i < pages.length; i++)
			{
				if (pages[i] instanceof FormEditorPersonPage)
				{
					FormEditorPersonPage personPage = (FormEditorPersonPage) pages[i];
					personPage.setDirty(true);
				}
			}
		}
	}

	public void removePage(final String id)
	{
		IFormPage[] pages = this.getPages().toArray(new IFormPage[0]);
		for (int i = 0; i < pages.length; i++)
		{
			if (pages[i] instanceof FormEditorPersonPage)
			{
				FormEditorPersonPage personPage = (FormEditorPersonPage) pages[i];
				personPage.setDirty(true);
			}
			else if (pages[i] instanceof IFormPage)
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
		List<Object> pagesToRemove = new ArrayList<Object>();
		List<IFormPage> pages = this.getPages();
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
		List<IFormPage> pages = this.getPages();
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
		boolean hasNotes = !input.getEntity().getNotes().isEmpty();
		if (!hasNotes)
		{
			List<LinkPersonAddress> links = input.getEntity().getValidLinks();
			for (LinkPersonAddress link : links)
			{
				if (!link.getAddress().getNotes().isEmpty())
				{
					hasNotes = true;
					break;
				}
			}
		}
		
		for (IFormPage page : this.getPages())
		{
			if (page != null && page.getManagedForm() != null)
			{
				if (page instanceof FormEditorPersonPage)
				{
					FormEditorPersonPage fpage = (FormEditorPersonPage) page;
					fpage.getManagedForm().getForm().setText(fpage.getText());
					fpage.setNotesSelectorState(hasNotes);
				}
				else if (page instanceof FormEditorLinkPage)
				{
					FormEditorLinkPage fpage = (FormEditorLinkPage) page;
					fpage.deleteAddressIfNoLinks();
					fpage.getManagedForm().getForm().setText(fpage.getText());
				}
			}
		}
	}

	@Override
	protected boolean validate()
	{
		boolean valid = true;
		List<IFormPage> pages = this.getPages();
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

	@Override
	public void handleEvent(Event event) 
	{
		PersonEditorInput input = (PersonEditorInput) this.getEditorInput();
		Person person = (Person) input.getAdapter(Person.class);
		if (person.getId() == null)
		{
			return;
		}
		if (event.getTopic().equals("ch/eugster/events/persistence/merge"))
		{
			Object entity = event.getProperty("entity");
			if (entity instanceof Person)
			{
				Person updatedPerson = (Person) entity;
				if (person.getId().equals(updatedPerson.getId()))
				{
					input.setEntity(updatedPerson);
					reset();
				}
			}
			else if (entity instanceof AddressGroupMember)
			{
				AddressGroupMember member = (AddressGroupMember) entity;
				if (member.getLink() != null)
				{
					Person p = member.getLink().getPerson();
					if (person.getId().equals(p.getId()))
					{
						ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class, null);
						tracker.open();
						try
						{
							ConnectionService service = (ConnectionService) tracker.getService();
							if (service != null)
							{
								service.refresh(p);
								input.setEntity(p);
								this.reset();
								this.setActivePage(this.getCurrentPage());
							}
						}
						finally
						{
							tracker.close();
						}
					}
				}
			}
		}
	}

}
