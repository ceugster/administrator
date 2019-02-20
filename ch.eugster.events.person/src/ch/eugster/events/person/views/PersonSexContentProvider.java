/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.person.views;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.PersonSex;
import ch.eugster.events.persistence.queries.PersonSexQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class PersonSexContentProvider implements IStructuredContentProvider
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
			PersonSexQuery query = (PersonSexQuery) service.getQuery(PersonSex.class);
			List<PersonSex> sexes = query.selectAll();
			return sexes.toArray(new PersonSex[0]);
		}
		else if (object instanceof PersonSex[])
		{
			return (PersonSex[]) object;
		}

		return new PersonSex[0];
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}

}
