package ch.eugster.events.documents.poi.internal.services;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;
import ch.eugster.events.documents.poi.internal.Activator;
import ch.eugster.events.documents.poi.internal.preferences.PreferenceConstants;
import ch.eugster.events.documents.services.DocumentBuilderService;

public class SpreadsheetBuilderService implements DocumentBuilderService
{
	private void addHeader(final HSSFSheet sheet)
	{
		DateFormat dateFormat = DateFormat.getDateInstance();
		DateFormat timeFormat = DateFormat.getTimeInstance();
		this.setHeader(sheet, dateFormat.format(Calendar.getInstance().getTime()), Header.LEFT);
		this.setHeader(sheet, "Adressliste", Header.CENTER);
		this.setHeader(sheet, timeFormat.format(Calendar.getInstance().getTime()), Header.RIGHT);
	}

	private void addRow(final DataMapKey[] keys, final DataMap dataMap, final HSSFSheet sheet, final int rowIndex,
			final HSSFCellStyle style, final HSSFFont font)
	{
		HSSFRow row = this.createRow(sheet, (short) rowIndex);
		for (int i = 0; i < keys.length; i++)
		{
			this.createCell(row, (short) i, dataMap.getProperty(keys[i].getKey()), font, style);
		}
	}

	private void addTitles(final DataMapKey[] keys, final HSSFSheet sheet, final int rowIndex,
			final HSSFCellStyle style, final HSSFFont font)
	{
		HSSFRow row = this.createRow(sheet, (short) rowIndex);

		for (int i = 0; i < keys.length; i++)
		{
			this.createCell(row, (short) i, keys[i].getName(), font, style);
		}
	}

	protected void autoSizeColumn(final HSSFSheet sheet, final int columnIndex)
	{
		sheet.autoSizeColumn(columnIndex);
	}

	@Override
	public IStatus buildDocument(final DataMapKey[] keys, final Collection<DataMap> maps)
	{
		IStatus status = Status.OK_STATUS;
		try
		{
			HSSFWorkbook workbook = this.createWorkbook();
			HSSFCellStyle style = this.createStyle(workbook, new short[] { CellStyle.BORDER_NONE,
					CellStyle.BORDER_NONE, CellStyle.BORDER_NONE, CellStyle.BORDER_NONE });
			HSSFFont normal = this.createFont(workbook, "Verdana", Font.BOLDWEIGHT_NORMAL, (short) 8);
			HSSFFont bold = this.createFont(workbook, "Verdana", Font.BOLDWEIGHT_BOLD, (short) 8);

			int counter = 0;
			HSSFSheet sheet = null;
			for (DataMap map : maps)
			{
				if (counter == 0)
				{
					sheet = this.createSheet(workbook, "Adressen");
					this.addHeader(sheet);
					this.addTitles(keys, sheet, counter, style, bold);
				}
				this.addRow(keys, map, sheet, ++counter, style, normal);
			}
			this.packColumns(sheet, 0, keys.length);
			if (maps.size() > 0)
			{
				this.showDocument(workbook);
			}
		}
		catch (Exception e)
		{
			status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
					"Beim Aufbereiten der Dokumente ist ein Fehler aufgetreten.", e);
		}
		return status;
	}

	@Override
	public IStatus buildDocument(final File file, final Collection<DataMap> maps)
	{
		return Status.CANCEL_STATUS;
	}

	@Override
	public IStatus buildDocument(final File file, final DataMap map)
	{
		return Status.CANCEL_STATUS;
	}

	protected void createCell(final HSSFRow row, final int col, final Double value, final HSSFFont font,
			final HSSFCellStyle style)
	{
		HSSFCell cell = row.createCell(col);
		cell.setCellStyle(style);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		cell.setCellValue(value);
	}

	protected void createCell(final HSSFRow row, final int col, final String value, final HSSFFont font,
			final HSSFCellStyle style)
	{
		try
		{
			double doubleValue = Double.valueOf(value);
			createCell(row, col, doubleValue, font, style);
		}
		catch (NumberFormatException e)
		{
			HSSFRichTextString string = new HSSFRichTextString(value);
			string.applyFont(font);
			HSSFCell cell = row.createCell(col);
			cell.setCellStyle(style);
			cell.setCellValue(string);
		}
	}

	protected HSSFFont createFont(final HSSFWorkbook workbook, final String name, final short type, final short height)
	{
		HSSFFont font = workbook.createFont();
		font.setBoldweight(type);
		font.setFontName(name);
		font.setFontHeightInPoints(height);
		return font;
	}

	protected HSSFRow createRow(final HSSFSheet sheet, final int index)
	{
		return sheet.createRow(index);
	}

	protected HSSFSheet createSheet(final HSSFWorkbook workbook, final String name)
	{
		return workbook.createSheet(name);
	}

	protected HSSFCellStyle createStyle(final HSSFWorkbook workbook, final short[] borders)
	{
		HSSFCellStyle style = workbook.createCellStyle();
		style.setBorderTop(borders[0]);
		style.setBorderBottom(borders[0]);
		style.setBorderLeft(borders[0]);
		style.setBorderRight(borders[0]);
		return style;
	}

	protected HSSFRichTextString createText(final HSSFFont font)
	{
		HSSFRichTextString text = new HSSFRichTextString();
		text.applyFont(font);
		return text;
	}

	protected HSSFWorkbook createWorkbook()
	{
		return new HSSFWorkbook();
	}

	protected int getLastRowNumber(final HSSFSheet sheet)
	{
		return sheet.getLastRowNum();
	}

	protected void packColumns(final HSSFSheet sheet, final int start, final int end)
	{
		for (int i = start; i < end; i++)
			sheet.autoSizeColumn(i);
	}

	protected void setHeader(final HSSFSheet sheet, final String value, final Header header)
	{
		if (header.equals(Header.LEFT))
			sheet.getHeader().setLeft(value);
		else if (header.equals(Header.CENTER))
			sheet.getHeader().setCenter(value);
		else if (header.equals(Header.RIGHT))
			sheet.getHeader().setRight(value);
	}

	private IStatus showDocument(final HSSFWorkbook workbook) throws Exception
	{
		IStatus status = Status.OK_STATUS;

		IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), Activator.getDefault().getBundle()
				.getSymbolicName());
		String path = store.getString(PreferenceConstants.KEY_SPREADSHEET_PATH);

		try
		{
			File tmpFile = File.createTempFile("tmp", ".xls");
			tmpFile.deleteOnExit();
			FileOutputStream fileOut = new FileOutputStream(tmpFile);
			workbook.write(fileOut);
			fileOut.close();

			if (Desktop.isDesktopSupported() && path.isEmpty())
			{
				if (Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
				{
					Desktop.getDesktop().open(tmpFile);
				}
				else
				{
					status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
							"Das generierte Dokument kann nicht angezeigt werden (Das System unterstützt die Anzeige nicht).");
				}
			}
			else
			{
				Runtime.getRuntime().exec(path + " " + tmpFile);
			}
		}
		catch (Exception e)
		{
			status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
					"Beim Aufbereiten der Dokumente ist ein Fehler aufgetreten.", e);
		}
		return status;
	}

	public enum Header
	{
		LEFT, CENTER, RIGHT
	}
}
