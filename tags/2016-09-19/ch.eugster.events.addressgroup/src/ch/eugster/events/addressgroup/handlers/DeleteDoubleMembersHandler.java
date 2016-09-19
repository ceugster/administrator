package ch.eugster.events.addressgroup.handlers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;

import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.queries.AddressGroupCategoryQuery;
import ch.eugster.events.persistence.queries.AddressGroupMemberQuery;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.handlers.ConnectionServiceDependentAbstractHandler;

public class DeleteDoubleMembersHandler extends ConnectionServiceDependentAbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		if (connectionService != null)
		{
			DomainQuery domainQuery = (DomainQuery) connectionService.getQuery(Domain.class);
			List<Domain> domains = domainQuery.selectAll();
			for (Domain domain : domains)
			{
				compute(connectionService, domain);
			}
		}
		return Status.OK_STATUS;
	}

	private void compute(ConnectionService service, Domain domain)
	{
		AddressGroupCategoryQuery query = (AddressGroupCategoryQuery) service.getQuery(AddressGroupCategory.class);
		List<AddressGroupCategory> categories = query.selectByDomain(domain);
		for (AddressGroupCategory category : categories)
		{
			compute(service, category);
		}
	}

	private void compute(ConnectionService service, AddressGroupCategory category)
	{
		Calendar calendar = GregorianCalendar.getInstance();
		List<AddressGroup> addressGroups = category.getAddressGroups();
		for (AddressGroup addressGroup : addressGroups)
		{
			Map<String, AddressGroupMember> members = new HashMap<String, AddressGroupMember>();
			List<AddressGroupMember> addressGroupMembers = addressGroup.getAddressGroupMembers();
			for (AddressGroupMember addressGroupMember : addressGroupMembers)
			{
				String id = addressGroupMember.getLink() == null || addressGroupMember.getLink().isDeleted()
						|| addressGroupMember.getLink().getPerson().isDeleted() ? "A"
						+ addressGroupMember.getAddress().getId() : "P" + addressGroupMember.getLink().getId();
				AddressGroupMember member = members.get(id);
				if (member == null)
				{
					members.put(id, addressGroupMember);
				}
				else
				{
					AddressGroupMemberQuery query = (AddressGroupMemberQuery) service
							.getQuery(AddressGroupMember.class);
					addressGroupMember.setUpdated(calendar);
					query.delete(addressGroupMember);
				}
			}
			members.clear();
		}
	}
}
