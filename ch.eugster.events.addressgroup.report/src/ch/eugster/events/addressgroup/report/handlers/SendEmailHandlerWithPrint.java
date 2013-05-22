package ch.eugster.events.addressgroup.report.handlers;

import ch.eugster.events.addressgroup.report.RecipientListFactory;
import ch.eugster.events.ui.helpers.EmailHelper;

public class SendEmailHandlerWithPrint extends PrintEmailRecipientsHandler
{
	@Override
	protected boolean execute()
	{
		boolean done = super.execute();
		if (done)
		{
			EmailHelper.getInstance().sendEmail(RecipientListFactory.getEmails());
		}
		return done;
	}
}
