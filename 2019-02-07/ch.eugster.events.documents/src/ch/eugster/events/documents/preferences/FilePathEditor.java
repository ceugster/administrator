/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package ch.eugster.events.documents.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

/**
 * A field editor to edit directory paths.
 */
public class FilePathEditor extends ListEditor
{

	/**
	 * The last path, or <code>null</code> if none.
	 */
	private String lastPath;

	/**
	 * The special label text for directory chooser, or <code>null</code> if
	 * none.
	 */
	private String dirChooserLabelText;

	/**
	 * Creates a new path field editor
	 */
	protected FilePathEditor()
	{
	}

	/**
	 * Creates a path field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param dirChooserLabelText
	 *            the label text displayed for the directory chooser
	 * @param parent
	 *            the parent of the field editor's control
	 */
	public FilePathEditor(final String name, final String labelText, final String dirChooserLabelText,
			final Composite parent)
	{
		this.init(name, labelText);
		this.dirChooserLabelText = dirChooserLabelText;
		this.createControl(parent);
		this.getAddButton().setText("Hinzufügen");
		this.getRemoveButton().setText("Entfernen");
		this.getUpButton().setText("Auf");
		this.getDownButton().setText("Ab");
	}

	/*
	 * (non-Javadoc) Method declared on ListEditor. Creates a single string from
	 * the given array by separating each string with the appropriate
	 * OS-specific path separator.
	 */
	@Override
	protected String createList(final String[] items)
	{
		StringBuffer path = new StringBuffer("");//$NON-NLS-1$

		for (int i = 0; i < items.length; i++)
		{
			path.append(items[i]);
			path.append(File.pathSeparator);
		}
		return path.toString();
	}

	/*
	 * (non-Javadoc) Method declared on ListEditor. Creates a new path element
	 * by means of a directory dialog.
	 */
	@Override
	protected String getNewInputObject()
	{

		FileDialog dialog = new FileDialog(this.getShell(), SWT.SHEET);
		if (this.dirChooserLabelText != null)
		{
			dialog.setText(this.dirChooserLabelText);
		}
		if (this.lastPath != null)
		{
			if (new File(this.lastPath).exists())
			{
				dialog.setFilterPath(this.lastPath);
			}
		}

		dialog.setFilterExtensions(new String[] { "*.od*" });
		dialog.setFilterIndex(0);
		dialog.setFilterPath("");

		String dir = dialog.open();
		if (dir != null)
		{
			dir = dir.trim();
			if (dir.length() == 0)
			{
				return null;
			}
			this.lastPath = dir;
		}
		return dir;
	}

	/*
	 * (non-Javadoc) Method declared on ListEditor.
	 */
	@Override
	protected String[] parseString(final String stringList)
	{
		StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator + "\n\r");//$NON-NLS-1$
		ArrayList<String> v = new ArrayList<String>();
		while (st.hasMoreElements())
		{
			v.add((String) st.nextElement());
		}
		return v.toArray(new String[v.size()]);
	}
}
