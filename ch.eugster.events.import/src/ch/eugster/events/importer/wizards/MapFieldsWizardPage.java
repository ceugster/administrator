package ch.eugster.events.importer.wizards;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import ch.eugster.events.importer.Activator;

public class MapFieldsWizardPage extends WizardPage implements IPageChangedListener
{
	private final ImportWizard wizard;

	private IDialogSettings settings;

	private final String[] targetNames = new String[] { "", "Externe Id", "Vorname", "Nachname", "Strasse", "Postfach",
			"Zusatz", "Land", "Postleitzahl", "Ort", "Kanton", "Telefon", "Handy", "Geburtsjahr", "Geburtsmonat",
			"Geburtstag", "Geburtsdatum", "Titel" };

	private Composite composite;

	private Text[] sourceNames;

	private ComboViewer[] targetViewers;

	private Sheet sheet;

	public MapFieldsWizardPage(ImportWizard wizard)
	{
		super(MapFieldsWizardPage.class.getName());
		this.wizard = wizard;
		init();
	}

	private void init()
	{
		settings = Activator.getDefault().getDialogSettings().getSection("import.wizard.table");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("import.wizard.table");
		}
	}

	@Override
	public void createControl(Composite parent)
	{
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(2, true));
		setControl(composite);
	}

	public void updatePage(Sheet sheet)
	{
		if (composite != null)
		{
			Control[] controls = composite.getChildren();
			for (Control control : controls)
			{
				if (!control.isDisposed())
				{
					control.dispose();
				}
			}

			int firstColumn = 0;
			int lastColumn = 0;
			Iterator<Cell> iterator = sheet.getRow(sheet.getFirstRowNum()).cellIterator();
			while (iterator.hasNext())
			{
				Cell cell = iterator.next();
				if (firstColumn > cell.getColumnIndex())
				{
					firstColumn = cell.getColumnIndex();
				}
				if (lastColumn < cell.getColumnIndex())
				{
					lastColumn = cell.getColumnIndex();
				}
			}
			int columns = lastColumn - firstColumn + 1;

			sourceNames = new Text[columns];
			targetViewers = new ComboViewer[columns];

			String[] targetNameSettings = settings.getArray("target.combo.selection");
			if (targetNameSettings == null || targetNameSettings.length < columns)
			{
				String[] selectedTargets = new String[columns];
				for (int i = 0; i < columns; i++)
				{
					if (targetNameSettings == null || targetNameSettings.length < (i + 1))
					{
						selectedTargets[i] = targetNames[0];
					}
					else
					{
						selectedTargets[i] = targetNameSettings[i];
					}
				}
				targetNameSettings = selectedTargets;
			}

			iterator = sheet.getRow(sheet.getFirstRowNum()).cellIterator();
			while (iterator.hasNext())
			{
				Cell cell = iterator.next();
				if (cell.getColumnIndex() == 33)
				{
					System.out.println();
				}
				sourceNames[cell.getColumnIndex() - firstColumn] = new Text(composite, SWT.BORDER
						| wizard.evaluateAlignment(cell));
				sourceNames[cell.getColumnIndex() - firstColumn].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				sourceNames[cell.getColumnIndex() - firstColumn].setEditable(false);
				sourceNames[cell.getColumnIndex() - firstColumn].setText(this.wizard.evaluateCell(cell));

				Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
				combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				targetViewers[cell.getColumnIndex() - firstColumn] = new ComboViewer(combo);
				targetViewers[cell.getColumnIndex() - firstColumn].setContentProvider(new ArrayContentProvider());
				targetViewers[cell.getColumnIndex() - firstColumn].setLabelProvider(new LabelProvider()
				{
					@Override
					public Image getImage(Object element)
					{
						return super.getImage(element);
					}

					@Override
					public String getText(Object element)
					{
						return element.toString();
					}
				});
				targetViewers[cell.getColumnIndex() - firstColumn].setInput(targetNames);
				targetViewers[cell.getColumnIndex() - firstColumn].setSelection(new StructuredSelection(
						new String[] { targetNameSettings[cell.getColumnIndex() - firstColumn] }));

			}
			composite.pack();
		}
	}

	@Override
	public void pageChanged(PageChangedEvent event)
	{
		if (event.getSelectedPage() == this)
		{
			if (wizard.getSheet() != sheet)
			{
				this.sheet = wizard.getSheet();
				updatePage(sheet);
			}
		}
	}

}
