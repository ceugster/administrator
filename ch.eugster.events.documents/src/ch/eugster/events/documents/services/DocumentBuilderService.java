package ch.eugster.events.documents.services;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;

public interface DocumentBuilderService
{
//	IStatus buildDocument(IProgressMonitor monitor, DataMapKey[] keys, List<DataMap> maps);

	IStatus buildDocument(IProgressMonitor monitor, DataMapKey[] keys, DataMap<?>[] maps);

//	IStatus buildDocument(IProgressMonitor monitor, File file, Collection<DataMap> maps);

	IStatus buildDocument(IProgressMonitor monitor, File template, DataMap<?>[] maps);

	IStatus buildDocument(IProgressMonitor monitor, File template, DataMap<?> maps);
}
