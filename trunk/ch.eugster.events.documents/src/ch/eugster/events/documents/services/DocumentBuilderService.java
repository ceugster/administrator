package ch.eugster.events.documents.services;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;

import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;

public interface DocumentBuilderService
{
	IStatus buildDocument(DataMapKey[] keys, Collection<DataMap> maps);

	IStatus buildDocument(File file, Collection<DataMap> maps);

	IStatus buildDocument(File file, DataMap map);
}
