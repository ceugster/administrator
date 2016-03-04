package ch.eugster.events.documents.odfdom.internal.services;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.element.OdfStylableElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeBodyElement;
import org.odftoolkit.odfdom.dom.element.office.OfficeTextElement;
import org.odftoolkit.odfdom.dom.element.style.StyleParagraphPropertiesElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableRowElement;
import org.odftoolkit.odfdom.dom.element.text.TextConditionalTextElement;
import org.odftoolkit.odfdom.dom.element.text.TextSpanElement;
import org.odftoolkit.odfdom.dom.element.text.TextTextInputElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.pkg.OdfElement;
import org.odftoolkit.odfdom.pkg.OdfFileDom;
import org.odftoolkit.simple.TextDocument;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;
import ch.eugster.events.documents.maps.EmptyDataMap;
import ch.eugster.events.documents.odfdom.internal.Activator;
import ch.eugster.events.documents.odfdom.internal.preferences.PreferenceInitializer;
import ch.eugster.events.documents.services.DocumentBuilderService;

public class OdfDocumentBuilderService implements DocumentBuilderService
{
	@Override
	public IStatus buildDocument(IProgressMonitor monitor, final File template, final DataMap[] maps)
	{
		IStatus status = Status.CANCEL_STATUS;
		if (!template.getName().endsWith(".odt"))
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
	public IStatus buildDocument(IProgressMonitor monitor, final File template, final DataMap map)
	{
		IStatus status = Status.CANCEL_STATUS;
		if (!template.getName().endsWith(".odt"))
		{
			return status;
		}

		try
		{
			monitor.beginTask("Dokument wird erstellt...", 1);
			DataMap[] maps = new DataMap[] { map };
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
	public IStatus buildDocument(IProgressMonitor monitor, DataMapKey[] keys, DataMap[] maps)
	{
		return Status.CANCEL_STATUS;
	}

	private IStatus buildTextDocument(IProgressMonitor monitor, final File template, final DataMap[] maps)
	{
		IStatus status = Status.OK_STATUS;
		try
		{
			monitor.beginTask("Dokumente werden erstellt...", maps.length);
			String styleName = "break-before";
			TextDocument document = TextDocument.loadDocument(template);
			OdfContentDom contentDom = document.getContentDom();
			OdfOfficeAutomaticStyles styles = contentDom.getAutomaticStyles();
			OdfStyle style = styles.getStyle(styleName, OdfStyleFamily.Paragraph);
			if (style == null)
			{
				style = new OdfStyle(contentDom);
				style.setStyleFamilyAttribute(OdfStyleFamily.Paragraph.getName());
				style.setStyleNameAttribute(styleName);
				style.setProperty(StyleParagraphPropertiesElement.BreakBefore, "page");
				styles.appendChild(style);
			}

			OfficeBodyElement body = (OfficeBodyElement) document.getContentRoot().getParentNode();
			OfficeTextElement text = OdfElement.findFirstChildNode(OfficeTextElement.class, body);
			if (text != null)
			{
				OfficeTextElement textCopy = (OfficeTextElement) text.cloneNode(true);
				OdfStylableElement stylableElement = OdfElement.findFirstChildNode(OdfStylableElement.class, text);
				while (stylableElement != null)
				{
					text.removeChild(stylableElement);
					stylableElement = OdfElement.findFirstChildNode(OdfStylableElement.class, text);
				}

				for (int i = 0; i < maps.length; i++)
				{
					stylableElement = OdfElement.findFirstChildNode(OdfStylableElement.class, textCopy);
					if (i == 0)
						style.setStyleParentStyleNameAttribute(stylableElement.getStyleName());
					else
						stylableElement.setStyleName(styleName);

					while (stylableElement != null)
					{
						OdfStylableElement clonedStylableElement = (OdfStylableElement) stylableElement.cloneNode(true);
						this.replaceContent(contentDom, clonedStylableElement, maps[i]);
						text.appendChild(clonedStylableElement);
						stylableElement = OdfElement.findNextChildNode(OdfStylableElement.class, stylableElement);
					}
					monitor.worked(1);
				}

			}

			showDocument(document);
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

	private TableTableRowElement[] collectTableRows(final TableTableElement table)
	{
		List<TableTableRowElement> rows = new ArrayList<TableTableRowElement>();
		NodeList list = table.getChildNodes();
		for (int i = 0; i < list.getLength(); i++)
		{
			if (list.item(i) instanceof TableTableRowElement)
				rows.add((TableTableRowElement) list.item(i));
		}
		if (rows.size() < 1)
			return new TableTableRowElement[0];
		if (rows.size() > 3)
			return new TableTableRowElement[0];

		TableTableRowElement[] tableRows = rows.toArray(new TableTableRowElement[0]);
		rows = new ArrayList<TableTableRowElement>();
		for (int i = 0; i < tableRows.length; i++)
			rows.add(tableRows[i]);

		return rows.toArray(new TableTableRowElement[0]);
	}

	private Node fillDummyRow(final OdfFileDom fileDom, final Node node, final DataMap map)
	{
		if (node instanceof TextTextInputElement)
		{
			this.replaceOdfTextInputWithOdfTextSpan(fileDom, (TextTextInputElement) node, map);
		}
		else if (node instanceof TextConditionalTextElement)
		{
			this.replaceConditionalTextWithCurrentValue((TextConditionalTextElement) node, map);
		}
		else
		{
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++)
			{
				this.fillDummyRow(fileDom, list.item(i), map);
			}
		}
		return node;
	}

	private void fillTable(final OdfFileDom fileDom, final TableTableElement table, final DataMap map)
	{
		String name = table.getTableNameAttribute();
		DataMap[] maps = map.getTableMaps(name).toArray(new DataMap[0]);
		TableTableRowElement[] rows = this.collectTableRows(table);
		TableTableRowElement totalRow = null;

		TableTableRowElement templateRow = null;
		TableTableRowElement inputRow = null;
		int startingRow = 0;
		if (rows.length == 1)
		{
			inputRow = rows[0];
			templateRow = (TableTableRowElement) rows[0].cloneNode(true);
			startingRow = 0;
		}
		else
		{
			inputRow = rows[1];
			templateRow = (TableTableRowElement) rows[1].cloneNode(true);
			startingRow = 1;
		}

		if (rows.length > 2)
		{
			totalRow = rows[2];
			table.removeChild(totalRow);
		}

		if (maps == null || maps.length == 0)
		{
			this.fillDummyRow(fileDom, inputRow, new EmptyDataMap());
		}
		else
		{
			this.fillTableRow(fileDom, rows[startingRow], maps[0]);
			for (int i = 1; i < maps.length; i++)
			{
				table.appendChild(this.fillTableRow(fileDom, (TableTableRowElement) templateRow.cloneNode(true), maps[i]));
			}
			if (totalRow != null)
			{
				table.appendChild(this.fillTableRow(fileDom, totalRow, map));
			}
		}
	}

	private TableTableRowElement fillTableRow(final OdfFileDom fileDom, final TableTableRowElement row, final DataMap map)
	{
		return (TableTableRowElement) this.replaceTableContent(fileDom, row, map);
	}

	private void replaceConditionalTextWithCurrentValue(final TextConditionalTextElement element, final DataMap map)
	{
		TextConditionalTextElement conditionalText = element;
		String condition = conditionalText.getTextConditionAttribute();
		if (condition.contains("person_form"))
		{
			if (condition.contains("person_form EQ 1") || condition.contains("person_form == 1")
					|| condition.contains("person_form EQ \"1\"") || condition.contains("person_form == \"1\"")
					|| condition.contains("person_form EQ persönlich")
					|| condition.contains("person_form == persönlich")
					|| condition.contains("person_form EQ \"persönlich\"")
					|| condition.contains("person_form == \"persönlich\""))
			{
				if (map.getProperty("person_form") == null)
				{
					conditionalText.setTextCurrentValueAttribute(Boolean.FALSE);
					conditionalText.setTextContent(conditionalText.getTextStringValueIfFalseAttribute());
				}
				else
				{
					String property = map.getProperty("person_form", "höflich");
					if (property.equals("persönlich"))
					{
						conditionalText.setTextCurrentValueAttribute(Boolean.TRUE);
						conditionalText.setTextContent(conditionalText.getTextStringValueIfTrueAttribute());
					}
					else
					{
						conditionalText.setTextCurrentValueAttribute(Boolean.FALSE);
						conditionalText.setTextContent(conditionalText.getTextStringValueIfFalseAttribute());
					}
				}
			}
		}
		else if (condition.contains("person_sex"))
		{
			if (condition.contains("person_sex EQ 0") || condition.contains("person_sex == 0")
					|| condition.contains("person_sex EQ \"0\"") || condition.contains("person_sex == \"0\"")
					|| condition.contains("person_sex EQ \"Ohne\"") || condition.contains("person_sex == \"Ohne\""))
			{
				conditionalText.setTextCurrentValueAttribute(Boolean.valueOf(map.getProperty("person_sex", "0")));
				conditionalText.setTextContent(conditionalText.getTextStringValueIfTrueAttribute());
			}
			else if (condition.contains("person_sex EQ 1") || condition.contains("person_sex == 1")
					|| condition.contains("person_sex EQ \"1\"") || condition.contains("person_sex == \"1\"")
					|| condition.contains("person_sex EQ \"Frau\"") || condition.contains("person_sex == \"Frau\""))
			{
				conditionalText.setTextCurrentValueAttribute(Boolean.valueOf(map.getProperty("person_sex", "1")));
				conditionalText.setTextContent(conditionalText.getTextStringValueIfTrueAttribute());
			}
			else if (condition.contains("person_sex EQ 2") || condition.contains("person_sex == 2")
					|| condition.contains("person_sex EQ \"2\"") || condition.contains("person_sex == \"2\"")
					|| condition.contains("person_sex EQ \"Herr\"") || condition.contains("person_sex == \"Herr\""))
			{
				conditionalText.setTextCurrentValueAttribute(Boolean.valueOf(map.getProperty("person_sex", "2")));
				conditionalText.setTextContent(conditionalText.getTextStringValueIfTrueAttribute());
			}
		}
	}

	private void replaceContent(final OdfFileDom fileDom, final Node node, final DataMap map)
	{
		if (node instanceof TextTextInputElement)
		{
			this.replaceOdfTextInputWithOdfTextSpan(fileDom, (TextTextInputElement) node, map);
		}
		else if (node instanceof TextConditionalTextElement)
		{
			this.replaceConditionalTextWithCurrentValue((TextConditionalTextElement) node, map);
		}
		else if (node instanceof TableTableElement)
		{
			this.fillTable(fileDom, (TableTableElement) node, map);
		}
		else
		{
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++)
			{
				this.replaceContent(fileDom, list.item(i), map);
			}
		}
	}

	private void replaceOdfTextInputWithOdfTextSpan(final OdfFileDom fileDom, final TextTextInputElement element,
			final DataMap map)
	{
		TextTextInputElement input = element;
		String desc = input.getTextDescriptionAttribute();
		String property = map.getProperty(desc, "");
		TextSpanElement span = fileDom.newOdfElement(TextSpanElement.class);
		String[] values = property.split("\n");
		if (values.length == 1)
		{
			span.setTextContent(values[0]);
		}
		else
		{
			for (String value : values)
			{
				TextSpanElement child = span.newTextSpanElement();
				child.setTextContent(value);
				span.appendChild(child);
				span.appendChild(span.newTextLineBreakElement());
			}
		}
		OdfElement parent = (OdfElement) input.getParentNode();
		parent.replaceChild(span, input);
	}

	private Node replaceTableContent(final OdfFileDom fileDom, final Node node, final DataMap map)
	{
		if (node instanceof TextTextInputElement)
		{
			this.replaceOdfTextInputWithOdfTextSpan(fileDom, (TextTextInputElement) node, map);
		}
		else if (node instanceof TextConditionalTextElement)
		{
			this.replaceConditionalTextWithCurrentValue((TextConditionalTextElement) node, map);
		}
		else
		{
			NodeList list = node.getChildNodes();
			for (int i = 0; i < list.getLength(); i++)
			{
				this.replaceTableContent(fileDom, list.item(i), map);
			}
		}
		return node;
	}

	private IStatus showDocument(final TextDocument document) throws Exception
	{
		IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), Activator.getDefault().getBundle()
				.getSymbolicName());
		// String office =
		// store.getString(PreferenceInitializer.KEY_OFFICE_PACKAGE);
		String writer = store.getString(PreferenceInitializer.KEY_OFFICE_WRITER_PATH);

		File file = File.createTempFile("odt", ".odt");
		file.deleteOnExit();
		document.save(file);

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
