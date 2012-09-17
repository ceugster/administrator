package ch.eugster.events.ui.helpers;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;

public class ClipboardHelper
{
	private static Clipboard clipboard;

	public static Clipboard getClipboard()
	{
		if (clipboard == null)
			clipboard = new Clipboard(Display.getCurrent());
		return clipboard;
	}
}
