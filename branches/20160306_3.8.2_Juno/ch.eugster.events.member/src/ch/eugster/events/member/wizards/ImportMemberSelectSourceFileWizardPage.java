package ch.eugster.events.member.wizards;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

public class ImportMemberSelectSourceFileWizardPage extends WizardPage implements IWizardPage
{
	private Text path;
	
	private ListViewer sheetList;

	private Button automaticUpdate;
	
	private Button silentInsertIfSingleResult;
	
	private Button skipExistingMembers;
	
	public ImportMemberSelectSourceFileWizardPage()
	{
		super("import.member.select.source.file.wizard.page");
	}

	@Override
	public void createControl(final Composite parent)
	{
		this.setTitle("Mitglieder abgleichen");
		this.setDescription("Gleichen Sie die Mitglieder der ausgewählten Mitgliedschaft mit einer Excel Arbeitsmappe ab.");
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.NONE);
		label.setText("Quelldatei (Excel)");
		label.setLayoutData(new GridData());

		path = new Text(composite, SWT.BORDER | SWT.SINGLE);
		path.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button button = new Button(composite, SWT.PUSH);
		button.setText("...");
		button.setLayoutData(new GridData());
		button.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(ImportMemberSelectSourceFileWizardPage.this.getShell(), SWT.NONE);
				dialog.setText("Datei für den Mitgliedernummernabgleich");
				dialog.setFilterExtensions(new String[] { "*.xls;*.xlsx" });
				dialog.setFilterIndex(0);
				dialog.setFilterNames(new String[] { "Excel Arbeitsmappe (*.xls, *.xlsx)" });
				String path = getWizard().getDialogSettings().get("file.path");
				path = dialog.open();
				if (path != null)
				{
					File file = new File(path);
					if (file.isFile() && !file.getAbsolutePath().equals(ImportMemberSelectSourceFileWizardPage.this.path.getText()))
					{
						ImportMemberSelectSourceFileWizardPage.this.initializeWorkbook(file);
					}
				}
			}
		});

		label = new Label(composite, SWT.None);
		label.setText("Auswahl Seite");
		label.setLayoutData(new GridData());
		
		List list = new List(composite, SWT.SINGLE | SWT.BORDER);
		list.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.sheetList = new ListViewer(list);
		this.sheetList.setContentProvider(new ArrayContentProvider());
		this.sheetList.setLabelProvider(new LabelProvider() 
		{
			@Override
			public String getText(Object element) 
			{
				Sheet sheet = (Sheet) element;
				return sheet.getSheetName();
			}
		});
		this.sheetList.addSelectionChangedListener(new ISelectionChangedListener() 
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				Sheet sheet = (Sheet) ssel.getFirstElement();
				ImportMemberWizard wizard = (ImportMemberWizard) getWizard();
				wizard.setSelectedSheet(sheet);
				
			}
		});
	
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		
		this.skipExistingMembers = new Button(composite, SWT.CHECK);
		this.skipExistingMembers.setText("Bereits vorhandene Personen/Adressen überspringen");
		this.skipExistingMembers.setLayoutData(gridData);
		
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		
		this.automaticUpdate = new Button(composite, SWT.CHECK);
		this.automaticUpdate.setText("Daten von bereits vorhandenen Personen/Adressen automatisch aktualisieren");
		this.automaticUpdate.setLayoutData(gridData);
		
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		
		this.silentInsertIfSingleResult = new Button(composite, SWT.CHECK);
		this.silentInsertIfSingleResult.setText("Wenn die Suche eine einzige Person/Adresse ergibt, die noch nicht Mitglied ist, automatisch als Mitglied setzen");
		this.silentInsertIfSingleResult.setLayoutData(gridData);
		
		String path = getWizard().getDialogSettings().get("file.path");
		if (path != null && !path.isEmpty())
		{
			File file = new File(path);
			if (file.isFile())
			{
				this.initializeWorkbook(file);
			}
		}
		
		this.setControl(composite);
	}

	public boolean isSkipExistingMembers()
	{
		return this.skipExistingMembers == null ? false : this.skipExistingMembers.getSelection();
	}
	
	public boolean isAutomaticUpdate()
	{
		return this.automaticUpdate == null ? false : this.automaticUpdate.getSelection();
	}
	
	public boolean doSilentInsertIfSingleResult()
	{
		return this.silentInsertIfSingleResult.getSelection();
	}
	
	private void initializeWorkbook(File file)
	{
		getWizard().getDialogSettings().put("file.path", file.getAbsolutePath());
		IWizard wizard = ImportMemberSelectSourceFileWizardPage.this.getWizard();
		if (wizard instanceof ImportMemberWizard)
		{
			ImportMemberWizard importMemberWizard = (ImportMemberWizard) wizard;
			try 
			{
				importMemberWizard.buildWorkbook(file);
				this.path.setText(file.getAbsolutePath());
				Workbook workbook = importMemberWizard.getWorkbook();
				java.util.List<Sheet> sheets = new ArrayList<Sheet>();
				int count = workbook.getNumberOfSheets();
				for (int i = 0; i < count; i++)
				{
					sheets.add(workbook.getSheetAt(i));
				}
				this.sheetList.setInput(sheets.toArray(new Sheet[0]));
				if (sheets.size() > 0)
				{
					IStructuredSelection ssel = new StructuredSelection(new Sheet[] { sheets.get(0) });
					ImportMemberSelectSourceFileWizardPage.this.sheetList.setSelection(ssel);
				}
			} 
			catch (EncryptedDocumentException e1) 
			{
				MessageDialog.openError(importMemberWizard.getShell(), "Verschlüsselte Datei", "Die Datei kann nicht geöffnet werden, da sie verschlüsselt ist.");
			} 
			catch (InvalidFormatException e1) 
			{
				MessageDialog.openError(importMemberWizard.getShell(), "Ungültiges Dateiformat", "Die Datei kann nicht geöffnet werden. Das Format der Datei ist ungültig.");
			} 
			catch (IOException e1) 
			{
				MessageDialog.openError(importMemberWizard.getShell(), "Zugriff verweigert", "Auf die Datei kann nicht zugegriffen werden. Möglicherweise wird sie von einem anderen Prozess verwendet.");
			}
		}
	}
	
	@Override
	public boolean isPageComplete() 
	{
		return this.path != null && (this.path.getText().endsWith(".xls") || this.path.getText().endsWith(".xlsx")) && new File(this.path.getText()).exists();
	}
}
