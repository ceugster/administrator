package ch.eugster.events.documents.services;

import java.io.File;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;

import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.service.ConnectionService;

public interface DocumentBuilderService
{
//	IStatus buildDocument(IProgressMonitor monitor, DataMapKey[] keys, List<DataMap> maps);

	IStatus buildDocument(IProgressMonitor monitor, DataMapKey[] keys, DataMap<?>[] maps);

//	IStatus buildDocument(IProgressMonitor monitor, File file, Collection<DataMap> maps);

	IStatus buildDocument(IProgressMonitor monitor, File template, DataMap<?>[] maps);

	IStatus buildDocument(IProgressMonitor monitor, File template, DataMap<?> maps);

	IStatus buildDocument(IProgressMonitor monitor,
			ConnectionService connectionService, Shell shell);
}
