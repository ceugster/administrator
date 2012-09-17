package ch.eugster.events.member.wizards;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;

public class ImportMemberWizard extends Wizard implements IWizard
{
	@Override
	public boolean performFinish()
	{
		return false;
	}

}
