package ch.eugster.events.documents.poi.internal.services;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlOptions;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.osgi.framework.FrameworkUtil;

import pl.jsolve.templ4docx.core.Docx;
import pl.jsolve.templ4docx.variable.TableVariable;
import pl.jsolve.templ4docx.variable.TextVariable;
import pl.jsolve.templ4docx.variable.Variable;
import pl.jsolve.templ4docx.variable.Variables;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;
import ch.eugster.events.documents.poi.internal.preferences.PreferenceConstants;
import ch.eugster.events.documents.services.DocumentBuilderService;

public class TextDocumentBuilderService implements DocumentBuilderService 
{
	private static final String EXT = ".docx";
	
	@Override
	public IStatus buildDocument(IProgressMonitor monitor, DataMapKey[] keys,
			DataMap<?>[] maps) 
	{
		return null;
	}

	@Override
	public IStatus buildDocument(IProgressMonitor monitor, File template,
			DataMap<?>[] maps) 
	{
		IStatus status = Status.CANCEL_STATUS;
		if (!template.getName().endsWith(EXT))
		{
			return status;
		}
		try
		{
			monitor.beginTask("Dokument wird erstellt...", 1);
			status = buildTextDocument(new SubProgressMonitor(monitor, maps.length), template, maps);
			monitor.worked(1);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			monitor.done();
		}
		return status;
	}

	@Override
	public IStatus buildDocument(IProgressMonitor monitor, File template,
			DataMap<?> map) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	private IStatus buildTextDocument(IProgressMonitor monitor, File template, DataMap<?>[] maps) throws Exception
	{
		XWPFDocument document = null;
		
		for (DataMap<?> map : maps)
		{
			Docx docx = new Docx(template.getAbsolutePath());
			Variables variables = new Variables();
			Set<Entry<Object, Object>> entries = map.getProperties().entrySet();
			for (Entry<Object, Object> entry : entries)
			{
				String value = entry.getValue() == null ? "" : entry.getValue().toString();
				variables.addTextVariable(new TextVariable("${" + entry.getKey() + "}", value));
			}
			DataMapKey[] tableKeys = map.getTableKeys();
			for (DataMapKey tableKey : tableKeys)
			{
				TableVariable tableVariable = new TableVariable();
				Map<Object, List<Variable>> colVars = new HashMap<Object, List<Variable>>();
				List<DataMap<?>> tableMaps = map.getTableMaps(tableKey.getKey());
				for (DataMap<?> tableMap : tableMaps)
				{
					Properties tableMapProperties = tableMap.getProperties();
					Set<Entry<Object, Object>> tableEntries = tableMapProperties.entrySet();
					for (Entry<Object, Object> tableEntry : tableEntries)
					{
						if (colVars.get(tableEntry.getKey().toString()) == null)
						{
							List<Variable> colVar = new ArrayList<Variable>();
							colVars.put(tableEntry.getKey().toString(), colVar);
						}
						colVars.get(tableEntry.getKey().toString()).add(new TextVariable("${" + tableEntry.getKey().toString() + "}", tableEntry.getValue().toString()));
					}
				}
				Collection<List<Variable>> colVar = colVars.values();
				for (List<Variable> col : colVar)
				{
					tableVariable.addVariable(col);
				}
				variables.addTableVariable(tableVariable);
			}
			docx.fillTemplate(variables);
			if (document == null)
			{
				document = docx.getXWPFDocument();
			}
			else
			{
				merge(document, docx.getXWPFDocument());
			}
		}
		return showDocument(document);
	}
	
	private void merge(XWPFDocument document1, XWPFDocument document2) throws Exception
	{
	    XmlOptions optionsOuter = new XmlOptions();
	    optionsOuter.setSaveOuter();
	    String appendString = document2.getDocument().getBody().xmlText(optionsOuter);
	    String mainString = document1.getDocument().getBody().xmlText();
	    String prefix = mainString.substring(0, mainString.indexOf(">") + 1);
	    String mainPart = mainString.substring(mainString.indexOf(">") + 1, mainString.lastIndexOf("<"));
	    String sufix = mainString.substring(mainString.lastIndexOf("<"));
	    String appendPart = appendString.substring(appendString.indexOf(">") + 1, appendString.lastIndexOf("<"));
	    CTBody makeBody = CTBody.Factory.parse(prefix + mainPart + "<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>" + appendPart + sufix);
	    document1.getDocument().getBody().set(makeBody);
	}
	
	private IStatus showDocument(final XWPFDocument document) throws Exception
	{
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, FrameworkUtil.getBundle(this.getClass())
				.getSymbolicName());
		String writer = store.getString(PreferenceConstants.KEY_OFFICE_WRITER_PATH);

		File file = File.createTempFile("docx", EXT);
		file.deleteOnExit();
		OutputStream fos = new FileOutputStream(file);
		document.write(fos);
		fos.close();

		if (Desktop.isDesktopSupported() && (writer == null || writer.isEmpty()))
		{
			try
			{
				Desktop.getDesktop().open(file);
			}
			catch (Exception e)
			{
				this.showDocumentWithProgram(writer, file);
			}
		}
		else
		{
			this.showDocumentWithProgram(writer, file);
		}
		return Status.OK_STATUS;
	}

	private void showDocumentWithProgram(final String writer, final File file) throws Exception
	{
		Runtime.getRuntime().exec(writer + " " + file.getAbsolutePath());
	}
	
}