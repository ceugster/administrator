package ch.eugster.events.documents.poi.internal.services;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.documents.maps.AddressGroupMap;
import ch.eugster.events.documents.maps.AddressGroupMemberMap;
import ch.eugster.events.documents.maps.AddressMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;
import ch.eugster.events.documents.maps.LinkMap;
import ch.eugster.events.documents.maps.PersonMap;
import ch.eugster.events.documents.poi.internal.Activator;
import ch.eugster.events.documents.poi.internal.preferences.PreferenceConstants;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.queries.AddressGroupQuery;
import ch.eugster.events.persistence.service.ConnectionService;

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

	private void addRow(final DataMapKey[] keys, final DataMap<?> dataMap, final Sheet sheet, final int rowIndex,
			final CellStyle style, final Font font)
	{
		// This is a data row
		Row row = this.createRow(sheet, (short) rowIndex);
		for (int i = 0; i < keys.length; i++)
		{
			
			this.createCell(row, (short) i, keys[i].getType(), dataMap.getProperty(keys[i].getKey()), font, style);
		}
	}

	private void addTitles(final DataMapKey[] keys, final Sheet sheet, final int rowIndex,
			final CellStyle style, final Font font)
	{
		Row row = this.createRow(sheet, (short) rowIndex);
		// This is the title row
		for (int i = 0; i < keys.length; i++)
		{
			this.createCell(row, (short) i, String.class, keys[i].getName(), font, style);
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
	public IStatus buildDocument(IProgressMonitor monitor, final DataMapKey[] keys, final DataMap<?>[] maps)
	{
		IStatus status = Status.OK_STATUS;
		try
		{
			monitor.beginTask("Dokument wird erstellt...", maps.length);
			Workbook workbook = this.createWorkbook();
			CellStyle style = this.createStyle(workbook, new BorderStyle[] { BorderStyle.NONE,
					BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE });
			Font normal = this.createFont(workbook, "Verdana", false, (short) 8);
			Font bold = this.createFont(workbook, "Verdana", true, (short) 8);

			int counter = 0;
			Sheet sheet = null;
			for (DataMap<?> map : maps)
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
	public IStatus buildDocument(IProgressMonitor monitor, ConnectionService connectionService, Shell shell)
	{
		IStatus status = Status.OK_STATUS;

		final DataMapKey[] keys = getKeys();

		final AddressGroupQuery addressGroupQuery = (AddressGroupQuery) connectionService.getQuery(AddressGroup.class);
		List<AddressGroup> addressGroups = addressGroupQuery.selectValids();
		
		int groupCount = addressGroups.size();
		monitor.beginTask("Verarbeite Adressgruppen...", groupCount);

		for (AddressGroup addressGroup : addressGroups)
		{
			monitor.setTaskName("Verarbeite Adressgruppe " + addressGroup.getName() + "...");
			
			Workbook workbook = this.createWorkbook();
			CellStyle style = this.createStyle(workbook, new BorderStyle[] { BorderStyle.NONE,
					BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE });
			Font normal = this.createFont(workbook, "Verdana", false, (short) 8);
			Font bold = this.createFont(workbook, "Verdana", true, (short) 8);

			DataMap<?> dataMap = null;
			List<AddressGroupMember> addressGroupMembers = addressGroup.getValidAddressGroupMembers();
			
			Sheet sheet = createSheet(workbook, addressGroup);
			String filename = addressGroup.getAddressGroupCategory().getDomain().getCode() + " " + addressGroup.getAddressGroupCategory().getName() + " " + sheet.getSheetName() + ".xlsx";

			sheet.getPrintSetup().setLandscape(true);
			sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
			this.addHeader(sheet);

			int counter = 0;
			this.addTitles(getKeys(), sheet, counter, style, bold);

			for (AddressGroupMember addressGroupMember : addressGroupMembers)
			{
				if (addressGroupMember.isValidLinkMember())
				{
					dataMap = new LinkMap(addressGroupMember.getLink());
				}
				else if (addressGroupMember.isValidAddressMember())
				{
					dataMap = new AddressMap(addressGroupMember.getAddress());
				}
				this.addRow(keys, dataMap, sheet, ++counter, style, normal);
			}
			this.packColumns(sheet, 0, keys.length);

			try
			{
				saveFile(workbook, filename);
				workbook.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			if (monitor.isCanceled())
			{
				break;
			}
			monitor.worked(1);
		}
		monitor.done();
		return status;
	}
		
	@Override
	public IStatus buildDocument(IProgressMonitor monitor, File file, DataMap<?>[] map)
	{
		return Status.CANCEL_STATUS;
	}

//	@Override
//	public IStatus buildDocument(IProgressMonitor monitor, final File file, final List<DataMap> maps)
//	{
//		return Status.CANCEL_STATUS;
//	}

	@Override
	public IStatus buildDocument(IProgressMonitor monitor, final File file, final DataMap<?> map)
	{
		return Status.CANCEL_STATUS;
	}
	
	private void createCell(final Row row, final int col, Class<?> type, String value, final Font font,
			final CellStyle style)
	{
		Cell cell = row.createCell(col);
		cell.setCellStyle(style);
		if (type.equals(Double.class))
		{
			cell.setCellType(CellType.NUMERIC);
			cell.setCellValue(value == null || value.isEmpty() ? 0D : Double.valueOf(value));
		}
		else
		{
			// Treat all other as string!
			value = value == null ? "" : value;
			style.setWrapText(value.contains("\n"));
			RichTextString string = new XSSFRichTextString(value);
			string.applyFont(font);
			cell.setCellValue(string);
		}
	}

//	private void createCell(final Row row, final int col, final Integer value, final Font font,
//			final CellStyle style)
//	{
//		Cell cell = row.createCell(col);
//		cell.setCellStyle(style);
//		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
//		cell.setCellValue(value);
//	}
//
//	private void createCell(final Row row, final int col, final Double value, final Font font,
//			final CellStyle style)
//	{
//		Cell cell = row.createCell(col);
//		cell.setCellStyle(style);
//		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
//		cell.setCellValue(value);
//	}
//
//	protected void createCell(final Row row, final int col, final Date value, final Font font,
//			final CellStyle style)
//	{
//		Cell cell = row.createCell(col);
//		cell.setCellStyle(style);
//		cell.setCellType(Cell.);
//		cell.setCellValue(value);
//	}
//
//	protected void createCell(final Row row, final int col, String value, final Font font,
//			final CellStyle style)
//	{
//		if (value == null)
//		{
//			value = "";
//		}
//		RichTextString string = new XSSFRichTextString(value);
//		string.applyFont(font);
//		Cell cell = row.createCell(col);
//		if (value.contains("\n"))
//			style.setWrapText(true);
//		cell.setCellStyle(style);
//		cell.setCellValue(string);
//	}
//
	protected Font createFont(final Workbook workbook, final String name, final boolean bold, final short height)
	{
		Font font = workbook.createFont();
		font.setBold(bold);
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

	protected CellStyle createStyle(final Workbook workbook, final BorderStyle[] borders)
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
	
	private Sheet createSheet(Workbook workbook, AddressGroup addressGroup) throws IllegalArgumentException
	{
		boolean ok = false;
		String name = addressGroup.getName();
		do
		{
			try
			{
				Sheet sheet = this.createSheet(workbook, name);
				ok = true;
				return sheet;
			}
			catch (IllegalArgumentException iae)
			{
				String msg = iae.getMessage();
				int idx = msg.indexOf("index (");
				String s = msg.substring(idx + "index(".length() + 1);
				s = s.substring(0, s.indexOf(")"));
				int i = Integer.valueOf(s).intValue();
				name = name.replace(name.substring(i, i + 1), "X");
			}
		}
		while (!ok);
		throw new IllegalArgumentException("Der Adressgruppenname konnte nicht normalisiert werden.");
	}

	private void saveFile(Workbook workbook, String filename) throws IllegalArgumentException
	{
		boolean ok = false;
		String pathname = System.getProperty("user.home") + File.separator + filename;

		do
		{
			try
			{
				File file = new File(pathname);
				FileOutputStream fos = new FileOutputStream(file);
				workbook.write(fos);
				fos.close();
				ok = true;
			}
			catch (IOException ioe)
			{
				String msg = ioe.getMessage();
				int idx = msg.indexOf("index (");
				String s = msg.substring(idx + "index(".length() + 1);
				s = s.substring(0, s.indexOf(")"));
				int i = Integer.valueOf(s).intValue();
				pathname = pathname.replace(pathname.substring(i, i + 1), "X");
			}
		}
		while (!ok);
	}

	private DataMapKey[] getKeys()
	{
		List<DataMapKey> keys = new ArrayList<DataMapKey>();
		keys.add(AddressGroupMemberMap.Key.ID);
		keys.add(PersonMap.Key.SEX);
		keys.add(PersonMap.Key.FORM);
		keys.add(AddressGroupMemberMap.Key.SALUTATION);
		keys.add(PersonMap.Key.TITLE);
		keys.add(PersonMap.Key.FIRSTNAME);
		keys.add(PersonMap.Key.LASTNAME);
		keys.add(AddressMap.Key.NAME);
		keys.add(AddressGroupMemberMap.Key.ANOTHER_LINE);
		keys.add(PersonMap.Key.BIRTHDATE);
		keys.add(PersonMap.Key.PROFESSION);
		keys.add(LinkMap.Key.FUNCTION);
		keys.add(LinkMap.Key.PHONE);
		keys.add(AddressMap.Key.PHONE);
		keys.add(PersonMap.Key.PHONE);
		keys.add(AddressMap.Key.FAX);
		keys.add(PersonMap.Key.EMAIL);
		keys.add(LinkMap.Key.EMAIL);
		keys.add(AddressMap.Key.EMAIL);
		keys.add(PersonMap.Key.WEBSITE);
		keys.add(AddressMap.Key.WEBSITE);
		keys.add(AddressMap.Key.ADDRESS);
		keys.add(AddressMap.Key.POB);
		keys.add(AddressMap.Key.COUNTRY);
		keys.add(AddressMap.Key.ZIP);
		keys.add(AddressMap.Key.CITY);
		keys.add(AddressMap.Key.COUNTY);
		keys.add(AddressGroupMemberMap.Key.POLITE);
		keys.add(AddressGroupMap.Key.NAME);
		keys.add(LinkMap.Key.MEMBER);
		keys.add(PersonMap.Key.NOTE);
		keys.add(AddressMap.Key.NOTES);
		keys.addAll(PersonMap.getExtendedFieldKeys());
		keys.addAll(LinkMap.getExtendedFieldKeys());
		return keys.toArray(new DataMapKey[0]);
	}

}
