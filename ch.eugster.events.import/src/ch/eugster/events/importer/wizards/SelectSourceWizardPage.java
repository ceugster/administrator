package ch.eugster.events.importer.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.eugster.events.importer.Activator;

public class SelectSourceWizardPage extends WizardPage implements ISelectionProvider, ISelectionChangedListener
{
	private final ImportWizard wizard;

	private IDialogSettings settings;

	private Text sourceFilename;

	private Button sourceSelector;

	private ComboViewer sheetSelector;

	private static final String[] FILE_EXTENSIONS = new String[] { ".xls", ".xlsx" };

	private static final String[] FILE_VERSIONS = new String[] { "Excel-Arbeitsmappe (bis 2003)",
			"Excel-Arbeitsmappe (ab 2007)" };

	private Workbook workbook;

	private final Collection<ISelectionChangedListener> sheetSelectorListeners = new ArrayList<ISelectionChangedListener>();

	public SelectSourceWizardPage(ImportWizard wizard)
	{
		super(SelectSourceWizardPage.class.getName());
		this.wizard = wizard;
		init();
	}

	public Sheet getSelectedSheet()
	{
		StructuredSelection ssel = (StructuredSelection) this.sheetSelector.getSelection();
		return ssel.isEmpty() ? null : (Sheet) ssel.getFirstElement();
	}

	private void init()
	{
		settings = Activator.getDefault().getDialogSettings().getSection("import.wizard.source");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("import.wizard.source");
		}
		if (settings.get("directory") == null)
		{
			settings.put("directory", "");
		}
		if (settings.get("file") == null)
		{
			settings.put("file", "");
		}
		if (settings.get("selected.type") == null)
		{
			settings.put("selected.type", 0);
		}
	}

	@Override
	public void createControl(Composite parent)
	{
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = 400;

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Importdatei");

		sourceFilename = new Text(composite, SWT.BORDER | SWT.SINGLE);
		sourceFilename.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sourceFilename.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				File file = new File(sourceFilename.getText());
				workbook = checkFileType(checkFile(file));
				if (workbook != null)
				{
					settings.put("file", file.getAbsolutePath());
					if (file.getParentFile() != null)
					{
						settings.put("directory", file.getParentFile().getAbsolutePath());
					}

					sheetSelector.setInput(workbook);
					if (sheetSelector.getElementAt(0) != null)
					{
						StructuredSelection ssel = new StructuredSelection(
								new Object[] { sheetSelector.getElementAt(0) });
						sheetSelector.setSelection(ssel);
					}
					setPageComplete(!sheetSelector.getSelection().isEmpty());
				}
			}
		});

		sourceSelector = new Button(composite, SWT.PUSH);
		sourceSelector.setText("...");
		sourceSelector.setLayoutData(new GridData());
		sourceSelector.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(SelectSourceWizardPage.this.getShell(), SWT.OPEN);
				dialog.setFileName(sourceFilename.getText());
				int count = Math.min(FILE_EXTENSIONS.length, FILE_VERSIONS.length);
				String[] extensions = new String[count];
				String[] versions = new String[count];
				for (int i = 0; i < count; i++)
				{
					extensions[i] = "*" + FILE_EXTENSIONS[i];
					versions[i] = FILE_VERSIONS[i];
				}
				dialog.setFilterExtensions(extensions);
				dialog.setFilterIndex(settings.getInt("selected.type"));
				dialog.setFilterNames(versions);
				String path = settings.get("directory");
				if (path != null)
					dialog.setFilterPath(path);
				dialog.setText("Einzulesende Datei");
				path = dialog.open();
				SelectSourceWizardPage.this.sourceFilename.setText(path == null ? "" : path);

			}
		});

		label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Arbeitsblatt");

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(gridData);

		sheetSelector = new ComboViewer(combo);
		sheetSelector.setContentProvider(new SheetSelectorContentProvider());
		sheetSelector.setLabelProvider(new LabelProvider()
		{

			@Override
			public String getText(Object element)
			{
				if (element instanceof Sheet)
				{
					Sheet sheet = (Sheet) element;
					return sheet.getSheetName();
				}
				return super.getText(element);
			}
		});
		sheetSelector.addSelectionChangedListener(this);

		if (this.getWizard() instanceof ImportWizard)
		{
			ImportWizard wizard = (ImportWizard) this.getWizard();
			ShowSourceWizardPage page = (ShowSourceWizardPage) wizard.getPage(ShowSourceWizardPage.class.getName());
			sheetSelector.addSelectionChangedListener(page);
		}
		sourceFilename.setText(settings.get("file"));

		this.setControl(composite);
	}

	private File checkFile(File file)
	{
		return (file.exists() && file.isFile() && file.canRead()) ? file : null;
	}

	private Workbook checkFileType(File file)
	{
		if (file == null)
		{
			return null;
		}

		if (file.exists() && file.isFile() && file.canRead())
		{
			for (String extension : FILE_EXTENSIONS)
			{
				if (file.getName().toLowerCase().endsWith(extension))
				{
					try
					{
						InputStream in = new FileInputStream(file.getAbsolutePath());
						Workbook workbook = WorkbookFactory.create(in);
						in.close();
						return workbook;
					}
					catch (FileNotFoundException e2)
					{
						e2.printStackTrace();
					}
					catch (InvalidFormatException e1)
					{
						e1.printStackTrace();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	private class SheetSelectorContentProvider implements IStructuredContentProvider
	{

		@Override
		public void dispose()
		{
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
		}

		@Override
		public Object[] getElements(Object inputElement)
		{
			if (inputElement instanceof Workbook)
			{
				Workbook workbook = (Workbook) inputElement;
				Collection<Sheet> sheets = new ArrayList<Sheet>();
				int numberOfSheets = workbook.getNumberOfSheets();
				for (int i = 0; i < numberOfSheets; i++)
				{
					sheets.add(workbook.getSheetAt(i));
				}
				return sheets.toArray(new Sheet[0]);
			}
			return new Sheet[0];
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		this.sheetSelectorListeners.add(listener);
	}

	@Override
	public ISelection getSelection()
	{
		return null;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		this.sheetSelectorListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection)
	{

	}

	@Override
	public void selectionChanged(SelectionChangedEvent event)
	{
		ISelectionChangedListener[] listeners = this.sheetSelectorListeners.toArray(new ISelectionChangedListener[0]);
		for (ISelectionChangedListener listener : listeners)
		{
			listener.selectionChanged(event);
		}
	}
}
