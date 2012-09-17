package ch.eugster.events.person;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory
{
	public static final String ID = "ch.eugster.events.person.perspective";

	@Override
	public void createInitialLayout(IPageLayout layout)
	{
	}

}
