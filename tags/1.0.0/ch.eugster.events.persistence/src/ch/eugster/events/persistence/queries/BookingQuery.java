package ch.eugster.events.persistence.queries;

import ch.eugster.events.persistence.model.Booking;
import ch.eugster.events.persistence.service.ConnectionService;

public class BookingQuery extends AbstractEntityQuery<Booking>
{
	public BookingQuery(final ConnectionService service)
	{
		super(service);
	}
}
