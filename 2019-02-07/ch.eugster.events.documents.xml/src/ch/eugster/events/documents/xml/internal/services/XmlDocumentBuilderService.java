package ch.eugster.events.documents.xml.internal.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.documents.xml.internal.Activator;

public class XmlDocumentBuilderService implements DocumentBuilderService
{
	private String[] applicableExtensions = new String[] { ".xml" };

	@Override
	public IStatus buildDocument(IProgressMonitor monitor, File template, final DataMap<?>[] maps)
	{
		IStatus status = Status.CANCEL_STATUS;
		if (template.getName().isEmpty())
		{
			template = new File(System.getProperty("user.home")+ File.separator + "kurse.xml");
			int no = 1;
			while (template.exists())
			{
				template = new File(System.getProperty("user.home")+ File.separator + "kurse-" + no + ".xml");
				no++;
			}
		}
		if (!validExtension(template))
		{
			return status;
		}

		try
		{
			monitor.beginTask("Dokument wird erstellt...", 1);
			status = buildTextDocument(new SubProgressMonitor(monitor, maps.length), template, maps);
			monitor.worked(1);
		}
		finally
		{
			monitor.done();
		}
		return status;
	}

	@Override
	public IStatus buildDocument(IProgressMonitor monitor, final File template, final DataMap<?> map)
	{
		IStatus status = Status.CANCEL_STATUS;
		if (!validExtension(template))
		{
			return status;
		}

		try
		{
			monitor.beginTask("Dokument wird erstellt...", 1);
			DataMap<?>[] maps = new DataMap<?>[] { map };
			status = buildTextDocument(new SubProgressMonitor(monitor, maps.length), template, maps);
			monitor.worked(1);
		}
		finally
		{
			monitor.done();
		}
		return status;
	}

	@Override
	public IStatus buildDocument(IProgressMonitor monitor, DataMapKey[] keys, DataMap<?>[] maps)
	{
		return Status.CANCEL_STATUS;
	}

	private boolean validExtension(File file)
	{
		for (String applicableExtension : applicableExtensions)
		{
			if (file.getName().endsWith(applicableExtension))
				return true;
		}
		return false;
	}
	
	private IStatus buildTextDocument(IProgressMonitor monitor, final File targetPath, final DataMap<?>[] maps)
	{
		IStatus status = Status.OK_STATUS;
		try
		{
			monitor.beginTask("Dokumente werden erstellt...", 1);
			
			Element courses = new Element("courses");
			Document document = new Document(courses);
			document.setProperty("encoding", "ISO-8859-1");
			document.setRootElement(courses);

			for (DataMap<?> map : maps)
			{
				Element course = new Element("course");
				Properties props = map.getProperties();
				@SuppressWarnings({ "rawtypes", "unchecked" })
				Enumeration<String> keys = (Enumeration) props.keys();
				while (keys.hasMoreElements())
				{
					String key = keys.nextElement();
					course.addContent(new Element(key).setText(props.getProperty(key)));
				}
				document.getRootElement().addContent(course);
				monitor.worked(1);
			}
	 
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat().setEncoding("ISO-8859-1"));
			Writer writer = new BufferedWriter(new FileWriter(targetPath));
			xmlOutput.output(document, writer);
			writer.close();
			
			
		}
		catch (Exception e)
		{
			status = new Status(IStatus.ERROR, Activator.getContext().getBundle().getSymbolicName(),
					"Beim Aufbereiten der Dokumente ist ein Fehler aufgetreten.", e);
		}
		finally
		{
			monitor.done();
		}
		return status;
	}

}
