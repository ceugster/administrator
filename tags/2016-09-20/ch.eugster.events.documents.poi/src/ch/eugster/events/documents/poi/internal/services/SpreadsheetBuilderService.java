package ch.eugster.events.documents.poi.internal.services;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Calendar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.runtime.IProgressMonitor;
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
	private void addHeader(final Sheet sheet)
	{
		DateFormat dateFormat = DateFormat.getDateInstance();
		DateFormat timeFormat = DateFormat.getTimeInstance();
		this.setHeader(sheet, dateFormat.format(Calendar.getInstance().getTime()), Header.LEFT);
		this.setHeader(sheet, "Adressliste", Header.CENTER);
		this.setHeader(sheet, timeFormat.format(Calendar.getInstance().getTime()), Header.RIGHT);
	}

	private void addRow(final DataMapKey[] keys, final DataMap dataMap, final Sheet sheet, final int rowIndex,
			final CellStyle style, final Font font)
	{
		Row row = this.createRow(sheet, (short) rowIndex);
		for (int i = 0; i < keys.length; i++)
		{
			this.createCell(row, (short) i, dataMap.getProperty(keys[i].getKey()), font, style);
		}
	}

	private void addTitles(final DataMapKey[] keys, final Sheet sheet, final int rowIndex,
			final CellStyle style, final Font font)
	{
		Row row = this.createRow(sheet, (short) rowIndex);

		for (int i = 0; i < keys.length; i++)
		{
			this.createCell(row, (short) i, keys[i].getName(), font, style);
		}
	}

	protected void autoSizeColumn(final Sheet sheet, final int columnIndex)
	{
		sheet.autoSizeColumn(columnIndex);
	}

//	@Override
//	public IStatus buildDocument(IProgressMonitor monitor, final DataMapKey[] keys, final List<DataMap> maps)
//	{
//		IStatus status = Status.OK_STATUS;
//		try
//		{
//			monitor.beginTask("Dokument wird erstellt...", 1);
//			status = buildDocument(new SubProgressMonitor(monitor, maps.size()), keys, maps.toArray(new DataMap[0]));
//			monitor.worked(1);
//		}
//		finally
//		{
//			monitor.done();
//		}
//		return status;
//	}

	@Override
	public IStatus buildDocument(IProgressMonitor monitor, final DataMapKey[] keys, final DataMap[] maps)
	{
		IStatus status = Status.OK_STATUS;
		try
		{
			monitor.beginTask("Dokument wird erstellt...", maps.length);
			Workbook workbook = this.createWorkbook();
			CellStyle style = this.createStyle(workbook, new short[] { CellStyle.BORDER_NONE,
					CellStyle.BORDER_NONE, CellStyle.BORDER_NONE, CellStyle.BORDER_NONE });
			Font normal = this.createFont(workbook, "Verdana", Font.BOLDWEIGHT_NORMAL, (short) 8);
			Font bold = this.createFont(workbook, "Verdana", Font.BOLDWEIGHT_BOLD, (short) 8);

			int counter = 0;
			Sheet sheet = null;
			for (DataMap map : maps)
			{
				if (counter == 0)
				{
					sheet = this.createSheet(workbook, "Adressen");
					sheet.getPrintSetup().setLandscape(false);
					sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
					this.addHeader(sheet);
					this.addTitles(keys, sheet, counter, style, bold);
				}
				this.addRow(keys, map, sheet, ++counter, style, normal);
				monitor.worked(1);
			}
			if (maps.length > 0)
			{
				this.packColumns(sheet, 0, keys.length);
				this.showDocument(workbook);
			}
		}
		catch (Exception e)
		{
			status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
					"Beim Aufbereiten der Dokumente ist ein Fehler aufgetreten.", e);
		}
		finally
		{
			monitor.done();
		}
		return status;
	}

	@Override
	public IStatus buildDocument(IProgressMonitor monitor, File file, DataMap[] map)
	{
		return Status.CANCEL_STATUS;
	}

//	@Override
//	public IStatus buildDocument(IProgressMonitor monitor, final File file, final List<DataMap> maps)
//	{
//		return Status.CANCEL_STATUS;
//	}

	@Override
	public IStatus buildDocument(IProgressMonitor monitor, final File file, final DataMap map)
	{
		return Status.CANCEL_STATUS;
	}

	protected void createCell(final Row row, final int col, final Double value, final Font font,
			final CellStyle style)
	{
		Cell cell = row.createCell(col);
		cell.setCellStyle(style);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		cell.setCellValue(value);
	}

	protected void createCell(final Row row, final int col, String value, final Font font,
			final CellStyle style)
	{
		if (value == null)
		{
			value = "";
		}
		RichTextString string = new XSSFRichTextString(value);
		string.applyFont(font);
		Cell cell = row.createCell(col);
		if (value.contains("\n"))
			style.setWrapText(true);
		cell.setCellStyle(style);
		cell.setCellValue(string);
	}

	protected Font createFont(final Workbook workbook, final String name, final short type, final short height)
	{
		Font font = workbook.createFont();
		font.setBoldweight(type);
		font.setFontName(name);
		font.setFontHeightInPoints(height);
		return font;
	}

	protected Row createRow(final Sheet sheet, final int index)
	{
		return sheet.createRow(index);
	}

	protected Sheet createSheet(final Workbook workbook, final String name)
	{
		return workbook.createSheet(name);
	}

	protected CellStyle createStyle(final Workbook workbook, final short[] borders)
	{
		CellStyle style = workbook.createCellStyle();
		style.setBorderTop(borders[0]);
		style.setBorderBottom(borders[0]);
		style.setBorderLeft(borders[0]);
		style.setBorderRight(borders[0]);
		return style;
	}

	protected RichTextString createText(final Font font)
	{
		RichTextString text = new XSSFRichTextString();
		text.applyFont(font);
		return text;
	}

	protected Workbook createWorkbook()
	{
		return new XSSFWorkbook();
	}

	protected int getLastRowNumber(final Sheet sheet)
	{
		return sheet.getLastRowNum();
	}

	protected void packColumns(final Sheet sheet, final int start, final int end)
	{
		for (int i = start; i < end; i++)
			sheet.autoSizeColumn(i);
	}

	protected void setHeader(final Sheet sheet, final String value, final Header header)
	{
		if (header.equals(Header.LEFT))
			sheet.getHeader().setLeft(value);
		else if (header.equals(Header.CENTER))
			sheet.getHeader().setCenter(value);
		else if (header.equals(Header.RIGHT))
			sheet.getHeader().setRight(value);
	}

	private IStatus showDocument(final Workbook workbook) throws Exception
	{
		IStatus status = Status.OK_STATUS;

		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, Activator.getDefault().getBundle()
				.getSymbolicName());
		String path = store.getString(PreferenceConstants.KEY_SPREADSHEET_PATH);

		try
		{
			File tmpFile = File.createTempFile("tmp", ".xlsx");
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
