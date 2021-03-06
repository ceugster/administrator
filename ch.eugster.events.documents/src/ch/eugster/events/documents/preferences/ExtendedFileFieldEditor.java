/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                 01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2010.
 */

package ch.eugster.events.documents.preferences;

import java.io.File;

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

/**
 * File field editor that allows setting the file dialog style and filter names
 * or {@link ContentType}s
 */
public class ExtendedFileFieldEditor extends FileFieldEditor
{

	private String[] extensions;

	private String[] names;

	private int style;

	/**
	 * Create a file field editor
	 * 
	 * @param style
	 *            the file dialog style
	 */
	protected ExtendedFileFieldEditor(int style)
	{
		super();

		this.style = style;
	}

	/**
	 * Create a file field editor
	 * 
	 * @param name
	 *            the preference name
	 * @param labelText
	 *            the label text
	 * @param enforceAbsolute
	 *            <code>true</code> if the file path must be absolute, and
	 *            <code>false</code> otherwise
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            the file dialog style
	 * 
	 * @see FileFieldEditor#FileFieldEditor(String, String, boolean, Composite)
	 */
	public ExtendedFileFieldEditor(String name, String labelText, boolean enforceAbsolute, Composite parent, int style)
	{
		super(name, labelText, enforceAbsolute, parent);

		this.style = style;
	}

	/**
	 * Create a file field editor
	 * 
	 * @param name
	 *            the preference name
	 * @param labelText
	 *            the label text
	 * @param enforceAbsolute
	 *            <code>true</code> if the file path must be absolute, and
	 *            <code>false</code> otherwise
	 * @param validationStrategy
	 *            the validation strategy
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            the file dialog style
	 * 
	 * @see FileFieldEditor#FileFieldEditor(String, String, boolean, int,
	 *      Composite)
	 */
	public ExtendedFileFieldEditor(String name, String labelText, boolean enforceAbsolute, int validationStrategy,
			Composite parent, int style)
	{
		super(name, labelText, enforceAbsolute, validationStrategy, parent);

		this.style = style;
	}

	/**
	 * Create a file field editor
	 * 
	 * @param name
	 *            the preference name
	 * @param labelText
	 *            the label text
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            the file dialog style
	 * 
	 * @see FileFieldEditor#FileFieldEditor(String, String, Composite)
	 */
	public ExtendedFileFieldEditor(String name, String labelText, Composite parent, int style)
	{
		super(name, labelText, parent);

		this.style = style;
	}

	/**
	 * @see FileFieldEditor#changePressed()
	 */
	@Override
	protected String changePressed()
	{
		File f = new File(getTextControl().getText());
		if (!f.exists())
		{
			f = null;
		}
		File d = getFile(f);
		if (d == null)
		{
			return null;
		}

		return d.getAbsolutePath();
	}

	/**
	 * Helper to open the file chooser dialog.
	 * 
	 * @param startingDirectory
	 *            the directory to open the dialog on.
	 * @return File The File the user selected or <code>null</code> if they do
	 *         not.
	 */
	protected File getFile(File startingDirectory)
	{
		FileDialog dialog = new FileDialog(getShell(), style);
		if (startingDirectory != null)
		{
			dialog.setFileName(startingDirectory.getPath());
		}
		if (extensions != null)
		{
			dialog.setFilterExtensions(extensions);
		}
		if (names != null)
		{
			dialog.setFilterNames(names);
		}
		String file = dialog.open();
		if (file != null)
		{
			file = file.trim();
			if (file.length() > 0)
			{
				return new File(file);
			}
		}

		return null;
	}

	/**
	 * Sets this file field editor's file extension filter.
	 * 
	 * @param extensions
	 *            a list of file extension, or <code>null</code> to set the
	 *            filter to the system's default value
	 */
	@Override
	public void setFileExtensions(String[] extensions)
	{
		this.extensions = extensions;
	}

	/**
	 * Sets this file field editor's file extension filter names.
	 * 
	 * @param names
	 *            a list of filter names, must correspond with the extensions
	 *            set using {@link #setFileExtensions(String[])}
	 */
	public void setFilterNames(String[] names)
	{
		this.names = names;
	}

}
