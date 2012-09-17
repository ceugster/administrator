/*
 * Created on 08.01.2009
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.ui.dialogs;

import org.eclipse.swt.widgets.Control;

public class Message
{
	Control control;

	private String title = ""; //$NON-NLS-1$

	private String message = ""; //$NON-NLS-1$

	public Message()
	{
		this(null);
	}

	public Message(Control control)
	{
		this(control, ""); //$NON-NLS-1$
	}

	public Message(Control control, String title)
	{
		this(control, title, ""); //$NON-NLS-1$
	}

	public Message(Control control, String title, String message)
	{
		this.control = control;
		this.title = title;
		this.message = message;
	}

	public Control getControl()
	{
		return control;
	}

	public void setControl(Control control)
	{
		this.control = control;
	}

	public String getTitle()
	{
		return title == null ? "" : title; //$NON-NLS-1$
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getMessage()
	{
		return message == null ? "" : message; //$NON-NLS-1$
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
