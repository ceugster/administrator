package ch.eugster.events.documents.poi.internal.services;

import java.io.File;

import ch.eugster.events.documents.services.DocumentExtractorService;

public class SpreadSheetExtractorService implements DocumentExtractorService
{

	@Override
	public String[][] extractDocument(final File file)
	{
		return new String[0][0];
	}
}
