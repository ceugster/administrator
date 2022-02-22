package ch.eugster.events.member.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;

import ch.eugster.events.member.Activator;
import ch.eugster.events.persistence.model.Membership;

public class ImportMemberWizard extends Wizard implements IWizard
{
	private Workbook workbook;
	
	private Sheet selectedSheet;
	
	private Membership membership;
	
	public ImportMemberWizard(Membership membership)
	{
		this.membership = membership;
		IDialogSettings settings = Activator.getDefault().getDialogSettings()
				.getSection("import.members.wizard");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("import.members.wizard");
		}
		this.setDialogSettings(settings);
	}

	public void buildWorkbook(File source) throws EncryptedDocumentException, InvalidFormatException, IOException
	{
		InputStream is = null;
		try
		{
			is = new FileInputStream(source);
			this.workbook = WorkbookFactory.create(is);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (InvalidFormatException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (is != null)
			{
				is.close();
			}
		}
	}
	
	public boolean isAutomaticUpdate()
	{
		ImportMemberSelectSourceFileWizardPage page = (ImportMemberSelectSourceFileWizardPage) this.getPage("import.member.select.source.file.wizard.page");
		return page.isAutomaticUpdate();
	}
	
	public boolean isSkipExistingMembers()
	{
		ImportMemberSelectSourceFileWizardPage page = (ImportMemberSelectSourceFileWizardPage) this.getPage("import.member.select.source.file.wizard.page");
		return page.isSkipExistingMembers();
	}
	
	public boolean doSilentInsertIfSingleResult()
	{
		ImportMemberSelectSourceFileWizardPage page = (ImportMemberSelectSourceFileWizardPage) this.getPage("import.member.select.source.file.wizard.page");
		return page.doSilentInsertIfSingleResult();
	}
	
	public void clearWorkbook()
	{
		try 
		{
			if (this.workbook != null)
			{
				this.workbook.close();
			}
		} 
		catch (IOException e) 
		{
		}
		finally
		{
			this.workbook = null;
		}
	}
	
	public Workbook getWorkbook()
	{
		return this.workbook;
	}
	
	public void setSelectedSheet(Sheet sheet)
	{
		this.selectedSheet = sheet;
	}

	public Sheet getSelectedSheet()
	{
		return this.selectedSheet;
	}
	
	public Membership getMembership()
	{
		return this.membership;
	}
	
	@Override
	public boolean performFinish()
	{
		return true;
	}

}
