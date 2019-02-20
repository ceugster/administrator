/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package ch.eugster.events.persistence.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * A field editor for a string type preference.
 * <p>
 * This class may be used as is, or subclassed as required.
 * </p>
 */
public class ButtonFieldEditor extends FieldEditor
{
	private String buttonText;
	
	private int style;
	
	/**
	 * The button field, or <code>null</code> if none.
	 */
	Button button;
	
	/**
	 * Creates a new button field editor
	 */
	protected ButtonFieldEditor()
	{
	}
	
	/**
	 * Creates a string field editor. Use the method <code>setTextLimit</code>
	 * to limit the text.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param width
	 *            the width of the text input field in characters, or
	 *            <code>UNLIMITED</code> for no limit
	 * @param strategy
	 *            either <code>VALIDATE_ON_KEY_STROKE</code> to perform on the
	 *            fly checking (the default), or
	 *            <code>VALIDATE_ON_FOCUS_LOST</code> to perform validation only
	 *            after the text has been typed in
	 * @param parent
	 *            the parent of the field editor's control
	 * @since 2.0
	 */
	public ButtonFieldEditor(String name, String labelText, int style, Composite parent)
	{
		init(name, "");
		this.buttonText = labelText;
		this.style = style;
		createControl(parent);
	}
	
	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void adjustForNumColumns(int numColumns)
	{
		GridData gd = (GridData) this.button.getLayoutData();
		gd.horizontalSpan = numColumns - 1;
		// We only grab excess space if we have to
		// If another field editor has more columns then
		// we assume it is setting the width.
		gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
	}
	
	/**
	 * Checks whether the text input field contains a valid value or not.
	 * 
	 * @return <code>true</code> if the field value is valid, and
	 *         <code>false</code> if invalid
	 */
	protected boolean checkState()
	{
		boolean result = false;
		if (this.button == null)
		{
			result = false;
		}
		// call hook for subclasses
		result = result && doCheckState();
		
		if (result)
		{
			clearErrorMessage();
		}
		else
		{
			showErrorMessage(null);
		}
		
		return result;
	}
	
	/**
	 * Hook for subclasses to do specific state checks.
	 * <p>
	 * The default implementation of this framework method does nothing and
	 * returns <code>true</code>. Subclasses should override this method to
	 * specific state checks.
	 * </p>
	 * 
	 * @return <code>true</code> if the field value is valid, and
	 *         <code>false</code> if invalid
	 */
	protected boolean doCheckState()
	{
		return true;
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
		getLabelControl(parent);
		
		this.button = getButtonControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns - 1;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		this.button.setLayoutData(gd);
	}
	
	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void doLoad()
	{
	}
	
	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void doLoadDefault()
	{
	}
	
	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void doStore()
	{
	}
	
	/**
	 * Returns the error message that will be displayed when and if an error
	 * occurs.
	 * 
	 * @return the error message, or <code>null</code> if none
	 */
	public String getErrorMessage()
	{
		return null;
	}
	
	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	public int getNumberOfControls()
	{
		return 2;
	}
	
	/**
	 * Returns this field editor's text control.
	 * 
	 * @return the text control, or <code>null</code> if no text field is
	 *         created yet
	 */
	protected Button getButtonControl()
	{
		return this.button;
	}
	
	/**
	 * Returns this field editor's text control.
	 * <p>
	 * The control is created if it does not yet exist
	 * </p>
	 * 
	 * @param parent
	 *            the parent
	 * @return the text control
	 */
	public Button getButtonControl(Composite parent)
	{
		if (this.button == null)
		{
			this.button = new Button(parent, this.style);
			this.button.setText(this.buttonText);
			this.button.setFont(parent.getFont());
			this.button.addDisposeListener(new DisposeListener()
			{
				public void widgetDisposed(DisposeEvent event)
				{
					ButtonFieldEditor.this.button = null;
				}
			});
		}
		else
		{
			checkParent(this.button, parent);
		}
		return this.button;
	}
	
	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void refreshValidState()
	{
	}
	
	// /**
	// * Sets the error message that will be displayed when and if an error
	// * occurs.
	// *
	// * @param message
	// * the error message
	// */
	// public void setErrorMessage(String message)
	// {
	// }
	
	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	public void setFocus()
	{
		if (this.button != null)
		{
			this.button.setFocus();
		}
	}
	
	/**
	 * Shows the error message set via <code>setErrorMessage</code>.
	 */
	public void showErrorMessage()
	{
		showErrorMessage(null);
	}
	
	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
	@Override
	public void setEnabled(boolean enabled, Composite parent)
	{
		super.setEnabled(enabled, parent);
		getButtonControl(parent).setEnabled(enabled);
	}
}
