package ch.eugster.events.importer.wizards;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;

import ch.eugster.events.persistence.model.Membership;

public class ImportWizard extends Wizard 
{
	private Sheet sheet;

	private Membership membership;
	
	private FormulaEvaluator formulaEvaluator;
	
	private boolean updateExistingAdresses;
	
	private boolean withHeader;
	
	public ImportWizard() 
	{
	}

	public void setSheet(Sheet sheet) 
	{
		this.sheet = sheet;
	}

	public Sheet getSheet() 
	{
		return this.sheet;
	}

	public void setMembership(Membership membership)
	{
		this.membership = membership;
	}
	
	public Membership getMembership()
	{
		return this.membership;
	}

	public void setUpdateExistingAddresses(boolean update)
	{
		this.updateExistingAdresses = update;
	}
	
	public boolean isUpdateExistingAddresses()
	{
		return this.updateExistingAdresses;
	}
	
	public void setWithHeader(boolean withHeader)
	{
		this.withHeader = withHeader;
	}
	
	public boolean isWithHeader()
	{
		return this.withHeader;
	}
	
	@Override
	public boolean performFinish() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPages() 
	{
		SelectSourceWizardPage selectSourcePage = new SelectSourceWizardPage(
				this);
		this.addPage(selectSourcePage);

		ShowSourceWizardPage showSourcePage = new ShowSourceWizardPage(this);
		this.addPage(showSourcePage);

		MapFieldsWizardPage mapFieldsPage = new MapFieldsWizardPage(this);
		this.addPage(mapFieldsPage);

		UpdatePersonWizardPage updatePersonPage = new UpdatePersonWizardPage(this);
		this.addPage(updatePersonPage);

		IWizardContainer wizardContainer = this.getContainer();
		if (wizardContainer instanceof IPageChangeProvider) {
			IPageChangeProvider pageChangeProvider = (IPageChangeProvider) wizardContainer;
			pageChangeProvider.addPageChangedListener(showSourcePage);
			pageChangeProvider.addPageChangedListener(mapFieldsPage);
			pageChangeProvider.addPageChangedListener(updatePersonPage);
		}
	}

	private FormulaEvaluator getEvaluator() 
	{
		if (this.formulaEvaluator == null) 
		{
			this.formulaEvaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
		}
		return this.formulaEvaluator;
	}

	public String evaluateCell(Cell cell) 
	{
		CellValue cellValue = getEvaluator().evaluate(cell);
		String value = null;
		if (cellValue != null)
		{
			switch (cellValue.getCellType()) 
			{
			case Cell.CELL_TYPE_BOOLEAN:
			{
				value = Boolean.toString(cellValue.getBooleanValue());
				break;
			}
			case Cell.CELL_TYPE_NUMERIC:
			{
				try 
				{
					value = Integer.valueOf(
							Double.valueOf(cellValue.getNumberValue()).intValue())
							.toString();
				} 
				catch (NumberFormatException e1) 
				{
					try 
					{
						value = Long.valueOf(Double.valueOf(cellValue.getNumberValue()).longValue()).toString();
					} 
					catch (NumberFormatException e2) 
					{
						try 
						{
							value = Double.valueOf(cellValue.getNumberValue()).toString();
						} 
						catch (NumberFormatException e) 
						{
						}
					}
				}
				break;
			}
			case Cell.CELL_TYPE_STRING:
			{
				value = cellValue.getStringValue();
				break;
			}
			default:
			{
				break;
			}
			}
		}
		return value;
	}

	public int evaluateAlignment(Cell cell) 
	{
		int alignment = SWT.LEFT;
		CellValue cellValue = getEvaluator().evaluate(cell);
		if (cellValue != null)
		{
			switch (cellValue.getCellType()) 
			{
				case Cell.CELL_TYPE_BOOLEAN:
				{
					alignment = SWT.CENTER;
					break;
				}
				case Cell.CELL_TYPE_NUMERIC:
				{
					try 
					{
						Double.valueOf(cellValue.getNumberValue()).toString();
						alignment = SWT.RIGHT;
					} 
					catch (NumberFormatException e) 
					{
					}
					break;
				}
				default:
					break;
			}
		}
		return alignment;
	}

    public boolean performCancel() 
    {
    	return super.performCancel();
    }
    
    public boolean canFinish()
    {
    	if (this.getContainer().getCurrentPage() instanceof UpdatePersonWizardPage)
    	{
        	return this.sheet != null && this.membership != null && this.membership.getFieldMapping() != null && !this.membership.getFieldMapping().isEmpty();
    	}
    	return false;
    }

    public enum MappingKey
    {
    	SOURCE, TARGET;
    }
}
