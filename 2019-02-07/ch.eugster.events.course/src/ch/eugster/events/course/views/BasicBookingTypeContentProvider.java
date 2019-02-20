/*
 * Created on 17.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.course.views;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.eugster.events.persistence.model.BookingTypeProposition;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.queries.BookingTypePropositionQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class BasicBookingTypeContentProvider implements IStructuredContentProvider
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
			BookingTypePropositionQuery query = (BookingTypePropositionQuery) service.getQuery(BookingTypeProposition.class);
			List<BookingTypeProposition> basicBookingTypes = query.selectAll();
			return basicBookingTypes.toArray(new BookingTypeProposition[0]);
		}

		return (Season[]) object;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
	}
}
