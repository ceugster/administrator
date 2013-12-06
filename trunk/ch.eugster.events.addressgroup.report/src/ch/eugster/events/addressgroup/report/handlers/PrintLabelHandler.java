package ch.eugster.events.addressgroup.report.handlers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.report.Activator;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.engine.ReportService.Destination;
import ch.eugster.events.report.entries.LabelFactory;
import ch.eugster.events.ui.helpers.EmailHelper;

public class PrintLabelHandler extends AbstractHandler implements IHandler
{
	private int buildLabelList(final LabelFactory factory, final IStructuredSelection ssel)
	{
		Iterator<?> iterator = ssel.iterator();
		while (iterator.hasNext())
		{
			Object element = iterator.next();
			if (element instanceof AddressGroupCategory)
			{
				AddressGroupCategory category = (AddressGroupCategory) element;
				this.extract(factory, category);
			}
			else if (element instanceof AddressGroup)
			{
				AddressGroup group = (AddressGroup) element;
				this.extract(factory, group);
			}
			// else if (element instanceof AddressGroupLink)
			// {
			// this.extract(((AddressGroupLink)
			// element).getChild());
			// }
			else if (element instanceof AddressGroupMember)
			{
				AddressGroupMember member = (AddressGroupMember) element;
				this.extract(factory, member);
			}
		}
		return factory.size();
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		if (EmailHelper.getInstance().isEmailSupported())
		{
			if (event.getApplicationContext() instanceof EvaluationContext)
			{
				ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
						ReportService.class.getName(), null);
				tracker.open();
				ReportService service = (ReportService) tracker.getService();
				if (service != null)
				{
					LabelFactory factory = new LabelFactory();
					EvaluationContext context = (EvaluationContext) event.getApplicationContext();
					ISelection sel = (ISelection) context.getParent().getVariable("selection");
					IStructuredSelection ssel = (IStructuredSelection) sel;
					if (buildLabelList(factory, ssel) > 0)
					{
						Destination[] destinations = new Destination[] { Destination.PREVIEW, Destination.PRINTER };
						service.processLabels(factory.getEntries(), new HashMap<String, Object>(), destinations);
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	private void extract(final LabelFactory factory, final AddressGroup addressGroup)
	{
		if (!addressGroup.isDeleted())
		{
			Collection<AddressGroupMember> addressGroupMembers = addressGroup.getAddressGroupMembers();
			for (AddressGroupMember addressGroupMember : addressGroupMembers)
			{
				this.extract(factory, addressGroupMember);
			}
			// for (AddressGroupLink link : addressGroup.getChildren())
			// {
			// if (!link.isDeleted() && !link.getChild().isDeleted())
			// {
			// extract(link.getChild());
			// }
			// }
		}
	}

	private void extract(final LabelFactory factory, final AddressGroupCategory category)
	{
		if (!category.isDeleted())
		{
			Collection<AddressGroup> addressGroups = category.getAddressGroups();
			for (AddressGroup addressGroup : addressGroups)
			{
				if (!addressGroup.isDeleted())
				{
					this.extract(factory, addressGroup);
				}
			}
		}
	}

	private void extract(final LabelFactory factory, final AddressGroupMember member)
	{
		if (!member.isDeleted())
		{
			if (member.getLink() == null)
			{
				if (!member.getAddress().isDeleted())
				{
					factory.addEntry(member.getAddress());
				}
			}
			else
			{
				if (!member.getLink().isDeleted())
				{
					factory.addEntry(member.getLink());
				}
			}
		}
	}

	@Override
	public void setEnabled(final Object evaluationContext)
	{
		setBaseEnabled(true);
	}
}
