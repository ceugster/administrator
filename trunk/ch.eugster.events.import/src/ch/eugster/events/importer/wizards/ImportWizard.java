package ch.eugster.events.importer.wizards;

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;

public class ImportWizard extends Wizard
{
	public ImportWizard()
	{
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
		SelectSourceWizardPage selectSourcePage = new SelectSourceWizardPage(this);
		this.addPage(selectSourcePage);

		ShowSourceWizardPage sourcePropertyPage = new ShowSourceWizardPage(this);
		this.addPage(sourcePropertyPage);

		MapFieldsWizardPage mapFieldsPage = new MapFieldsWizardPage(this);
		this.addPage(mapFieldsPage);

		IWizardContainer wizardContainer = this.getContainer();
		if (wizardContainer instanceof IPageChangeProvider)
		{
			IPageChangeProvider pageChangeProvider = (IPageChangeProvider) wizardContainer;
			pageChangeProvider.addPageChangedListener(sourcePropertyPage);
			pageChangeProvider.addPageChangedListener(mapFieldsPage);
		}
	}

	// private FormulaEvaluator getEvaluator()
	// {
	// if (formulaEvaluator == null)
	// {
	// formulaEvaluator =
	// sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
	// }
	// return formulaEvaluator;
	// }

	// public String evaluateCell(Cell cell)
	// {
	// CellValue cellValue = getEvaluator().evaluate(cell);
	// String value = null;
	// switch (cellValue.getCellType())
	// {
	// case Cell.CELL_TYPE_BOOLEAN:
	// value = Boolean.toString(cellValue.getBooleanValue());
	// break;
	// case Cell.CELL_TYPE_NUMERIC:
	// try
	// {
	// value =
	// Integer.valueOf(Double.valueOf(cellValue.getNumberValue()).intValue()).toString();
	// }
	// catch (NumberFormatException e1)
	// {
	// try
	// {
	// value =
	// Long.valueOf(Double.valueOf(cellValue.getNumberValue()).longValue()).toString();
	// }
	// catch (NumberFormatException e2)
	// {
	// try
	// {
	// value = Double.valueOf(cellValue.getNumberValue()).toString();
	// }
	// catch (NumberFormatException e)
	// {
	// }
	// }
	// }
	// break;
	// case Cell.CELL_TYPE_STRING:
	// value = cellValue.getStringValue();
	// break;
	// default:
	// break;
	// }
	// return value;
	// }

	// public int evaluateAlignment(Cell cell)
	// {
	// CellValue cellValue = getEvaluator().evaluate(cell);
	// int alignment = SWT.LEFT;
	// switch (cellValue.getCellType())
	// {
	// case Cell.CELL_TYPE_BOOLEAN:
	// alignment = SWT.CENTER;
	// break;
	// case Cell.CELL_TYPE_NUMERIC:
	// try
	// {
	// Double.valueOf(cellValue.getNumberValue()).toString();
	// alignment = SWT.RIGHT;
	// }
	// catch (NumberFormatException e)
	// {
	// }
	// break;
	// default:
	// break;
	// }
	// return alignment;
	// }

}
