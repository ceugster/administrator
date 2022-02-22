package ch.eugster.events.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.splash.EclipseSplashHandler;

@SuppressWarnings("restriction")
public class EventsSplashHandler extends EclipseSplashHandler
{
	@Override
	public void init(Shell splash)
	{
		splash.setLayout(new GridLayout());
		Label label = new Label(splash, SWT.None);
		label.setLayoutData(new GridData(GridData.FILL_BOTH));
		label.setText("XXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		splash.layout();

		final Image image = Activator.getDefault().getImageRegistry().get("EXIT");
		if (image != null)
		{
			final int xposition = splash.getSize().x - image.getImageData().width - 10;
			final int yposition = 10;
			getContent().addPaintListener(new PaintListener()
			{
				public void paintControl(PaintEvent e)
				{
					e.gc.drawImage(image, xposition, yposition);
				}
			});
		}
	}
}
