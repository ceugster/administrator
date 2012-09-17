/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.person.views;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.queries.PersonTitleQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class PersonTitleContentProvider implements IStructuredContentProvider
{
	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getElements(final Object object)
	{
		if (object instanceof ConnectionService)
		{
			ConnectionService service = (ConnectionService) object;
			PersonTitleQuery query = (PersonTitleQuery) service.getQuery(PersonTitle.class);
			Collection<PersonTitle> titles = query.selectAll();
			return titles.toArray(new PersonTitle[0]);
		}
		else if (object instanceof PersonTitle[])
		{
			return (PersonTitle[]) object;
		}

		return new PersonTitle[0];
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}

}
