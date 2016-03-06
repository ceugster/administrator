package ch.eugster.events.documents.poi.internal.services;

import java.io.File;

import ch.eugster.events.documents.services.DocumentExtractorService;

public class SpreadSheetExtractorService implements DocumentExtractorService
{

	@Override
	public String[][] extractDocument(final File file)
	{
//		InputStream inputStream;
//		try
//		{
//			inputStream = new FileInputStream(file);
//			Workbook workbook = WorkbookFactory.create(inputStream);
//			Sheet sheet = workbook.getSheetAt(0);
//			inputStream.close();
//		}
//		catch (FileNotFoundException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		catch (InvalidFormatException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		catch (IOException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return new String[0][0];
	}
}
