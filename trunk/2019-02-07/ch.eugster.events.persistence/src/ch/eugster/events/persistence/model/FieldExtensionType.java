package ch.eugster.events.persistence.model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public enum FieldExtensionType
{
	TEXT, SPINNER, BOOLEAN;

	public void addListeners(final Control control, final DirtyMarkable markable)
	{
		switch (this)
		{
			case TEXT:
			{
				((Text) control).addModifyListener(new ModifyListener()
				{
					@Override
					public void modifyText(final ModifyEvent e)
					{
						markable.setDirty(true);
					}
				});
				break;
			}
			case SPINNER:
			{
				((Spinner) control).addModifyListener(new ModifyListener()
				{
					@Override
					public void modifyText(final ModifyEvent e)
					{
						markable.setDirty(true);
					}
				});
				((Spinner) control).addSelectionListener(new SelectionListener()
				{
					@Override
					public void widgetDefaultSelected(final SelectionEvent e)
					{
						widgetSelected(e);
					}

					@Override
					public void widgetSelected(final SelectionEvent e)
					{
						markable.setDirty(true);
					}
				});
				break;
			}
			case BOOLEAN:
			{
				((Button) control).addSelectionListener(new SelectionListener()
				{
					@Override
					public void widgetDefaultSelected(final SelectionEvent e)
					{
						widgetSelected(e);
					}

					@Override
					public void widgetSelected(final SelectionEvent e)
					{
						markable.setDirty(true);
					}
				});
				break;
			}
			default:
			{
				throw new RuntimeException("Invalid extension type");
			}
		}
	}

	public Control createControl(final Composite parent, final int style)
	{
		switch (this)
		{
			case TEXT:
			{
				return new Text(parent, style);
			}
			case SPINNER:
			{
				return new Spinner(parent, style);
			}
			case BOOLEAN:
			{
				return new Button(parent, style);
			}
			default:
			{
				throw new RuntimeException("Invalid extension type");
			}
		}
	}

	public int defaultStyle()
	{
		switch (this)
		{
			case TEXT:
			{
				return 0;
			}
			case SPINNER:
			{
				return 0;
			}
			case BOOLEAN:
			{
				return SWT.CHECK;
			}
			default:
			{
				throw new RuntimeException("Invalid extension type");
			}
		}
	}

	public String getInput(final Control control)
	{
		switch (this)
		{
			case TEXT:
			{
				return ((Text) control).getText();
			}
			case SPINNER:
			{
				return Integer.valueOf(((Spinner) control).getSelection()).toString();
			}
			case BOOLEAN:
			{
				return Boolean.valueOf(((Button) control).getSelection()).toString();
			}
			default:
			{
				throw new RuntimeException("Invalid extension type");
			}
		}
	}

	public String label()
	{
		switch (this)
		{
			case TEXT:
			{
				return "Text";
			}
			case SPINNER:
			{
				return "Ganze Zahl";
			}
			case BOOLEAN:
			{
				return "Ja/Nein";
			}
			default:
			{
				throw new RuntimeException("Invalid extension type");
			}
		}
	}

	public void setInput(final Control control, final String value)
	{
		switch (this)
		{
			case TEXT:
			{
				((Text) control).setText(value);
				break;
			}
			case SPINNER:
			{
				((Spinner) control).setSelection(Integer.valueOf(value).intValue());
				break;
			}
			case BOOLEAN:
			{
				((Button) control).setSelection(Boolean.valueOf(value).booleanValue());
				break;
			}
			default:
			{
				throw new RuntimeException("Invalid extension type");
			}
		}
	}
}
