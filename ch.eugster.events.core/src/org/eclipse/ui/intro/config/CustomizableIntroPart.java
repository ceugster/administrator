package org.eclipse.ui.intro.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;

import ch.eugster.events.core.Activator;

public class CustomizableIntroPart implements IIntroPart
{

	@Override
	public void addPropertyListener(IPropertyListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void createPartControl(Composite parent)
	{
		Composite outerContainer = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		outerContainer.setLayout(gridLayout);
		outerContainer.setBackground(outerContainer.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
		Label label = new Label(outerContainer, SWT.CENTER);
		label.setText("Willkommen beim Administrator");
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gd.horizontalAlignment = GridData.CENTER;
		gd.verticalAlignment = GridData.CENTER;
		label.setLayoutData(gd);
		label.setBackground(outerContainer.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public IIntroSite getIntroSite()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle()
	{
		return "Willkommen";
	}

	@Override
	public Image getTitleImage()
	{
		return Activator.getDefault().getImageRegistry().get("EXIT");
	}

	@Override
	public void init(IIntroSite site, IMemento memento) throws PartInitException
	{
	}

	@Override
	public void removePropertyListener(IPropertyListener listener)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void saveState(IMemento memento)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void standbyStateChanged(boolean standby)
	{
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
