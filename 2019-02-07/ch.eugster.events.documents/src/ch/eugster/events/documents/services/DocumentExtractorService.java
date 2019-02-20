package ch.eugster.events.documents.services;

import java.io.File;

public interface DocumentExtractorService
{
	String[][] extractDocument(File file);
}
