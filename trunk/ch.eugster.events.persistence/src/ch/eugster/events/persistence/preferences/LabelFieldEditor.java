package ch.eugster.events.persistence.preferences;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LabelFieldEditor extends FieldEditor
{
	private Label label;
	
	private String errorMessage;
	
	public LabelFieldEditor(String name, String labelText, Composite parent)
	{
		init(name, labelText);
		this.errorMessage = JFaceResources.getString("StringFieldEditor.errorMessage");//$NON-NLS-1$
		createControl(parent);
	}
	
	@Override
	protected void adjustForNumColumns(int numColumns)
	{
		GridData gd = (GridData) getLabelControl().getLayoutData();
		gd.horizontalSpan = numColumns;
		gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
	}
	
	/**
	 * Fills this field editor's basic controls into the given parent.
	 * <p>
	 * The string field implementation of this <code>FieldEditor</code>
	 * framework method contributes the text field. Subclasses may override but
	 * must call <code>super.doFillIntoGrid</code>.
	 * </p>
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns)
	{
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = numColumns;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		getLabelControl(parent).setLayoutData(gd);
	}
	
	/**
	 * Returns this field editor's label component.
	 * <p>
	 * The label is created if it does not already exist
	 * </p>
	 * 
	 * @param parent
	 *            the parent
	 * @return the label control
	 */
	@Override
	public Label getLabelControl(Composite parent)
	{
		if (this.label == null)
		{
			this.label = new Label(parent, SWT.LEFT | SWT.WRAP);
			this.label.setFont(parent.getFont());
			String text = getLabelText();
			if (text != null)
			{
				this.label.setText(text);
			}
			this.label.addDisposeListener(new DisposeListener()
			{
				public void widgetDisposed(DisposeEvent event)
				{
					LabelFieldEditor.this.label = null;
				}
			});
		}
		else
		{
			checkParent(this.label, parent);
		}
		return this.label;
	}
	
	/**
	 * Returns the label control.
	 * 
	 * @return the label control, or <code>null</code> if no label control has
	 *         been created
	 */
	@Override
	protected Label getLabelControl()
	{
		return this.label;
	}
	
	/**
	 * Sets this field editor's label text. The label is typically presented to
	 * the left of the entry field.
	 * 
	 * @param text
	 *            the label text
	 */
	@Override
	public void setLabelText(String text)
	{
		Assert.isNotNull(text);
		super.setLabelText(text);
		if (this.label != null)
		{
			this.label.setText(text);
		}
	}
	
	@Override
	protected void doLoad()
	{
	}
	
	@Override
	protected void doLoadDefault()
	{
	}
	
	@Override
	protected void doStore()
	{
	}
	
	@Override
	public int getNumberOfControls()
	{
		return 1;
	}
	
	/**
	 * Returns the error message that will be displayed when and if an error
	 * occurs.
	 * 
	 * @return the error message, or <code>null</code> if none
	 */
	public String getErrorMessage()
	{
		return this.errorMessage;
	}
}
