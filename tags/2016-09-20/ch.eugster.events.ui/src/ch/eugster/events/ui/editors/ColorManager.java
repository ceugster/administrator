package ch.eugster.events.ui.editors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager
{

	protected Map<RGB, Color> colorTable = new HashMap<RGB, Color>(10);

	public void dispose()
	{
		Collection<Color> colors = colorTable.values();
		Iterator<Color> iterator = colors.iterator();
		while (iterator.hasNext())
			iterator.next().dispose();
	}

	public Color getColor(RGB rgb)
	{
		Color color = colorTable.get(rgb);
		if (color == null)
		{
			color = new Color(Display.getCurrent(), rgb);
			colorTable.put(rgb, color);
		}
		return color;
	}
}
