package ch.eugster.events.documents.odfdom.internal.services;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.odftoolkit.odfdom.OdfElement;
import org.odftoolkit.odfdom.OdfFileDom;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.doc.office.OdfOfficeBody;
import org.odftoolkit.odfdom.doc.office.OdfOfficeText;
import org.odftoolkit.odfdom.doc.style.OdfStyle;
import org.odftoolkit.odfdom.doc.style.OdfStyleParagraphProperties;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.odftoolkit.odfdom.doc.text.OdfConditionalText;
import org.odftoolkit.odfdom.doc.text.OdfTextInput;
import org.odftoolkit.odfdom.doc.text.OdfTextSpan;
import org.odftoolkit.odfdom.dom.OdfAttributeNames;
import org.odftoolkit.odfdom.dom.element.OdfStylableElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
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
	public IStatus buildDocument(final DataMapKey[] keys, final Collection<DataMap> maps)
	{
		return Status.CANCEL_STATUS;
	}

	@Override
	public IStatus buildDocument(final File template, final Collection<DataMap> maps)
	{
		IStatus status = Status.CANCEL_STATUS;
		if (template.getName().endsWith(".odt"))
		{
			status = buildTextDocument(template, maps);
		}
		return status;
	}

	@Override
	public IStatus buildDocument(final File template, final DataMap map)
	{
		IStatus status = Status.CANCEL_STATUS;
		if (template.getName().endsWith(".odt"))
		{
			Collection<DataMap> maps = new ArrayList<DataMap>();
			maps.add(map);
			status = buildTextDocument(template, maps);
		}
		return status;
	}

	@Override
	public IStatus buildDocument(DataMapKey[] keys, DataMap[] maps)
	{
		return Status.CANCEL_STATUS;
	}

	private IStatus buildTextDocument(final File template, final Collection<DataMap> maps)
	{
		IStatus status = Status.OK_STATUS;
		DataMap[] values = maps.toArray(new DataMap[0]);
		try
		{
			String styleName = "break-before";
			OdfDocument document = OdfTextDocument.loadDocument(template);
			OdfFileDom fileDom = document.getContentDom();
			OdfOfficeAutomaticStyles styles = fileDom.getAutomaticStyles();
			OdfStyle style = styles.getStyle(styleName, OdfStyleFamily.Paragraph);
			if (style == null)
			{
				style = new OdfStyle(fileDom);
				style.setStyleFamilyAttribute(OdfStyleFamily.Paragraph.getName());
				style.setStyleNameAttribute(styleName);
				style.setProperty(OdfStyleParagraphProperties.BreakBefore, "page");
				styles.appendChild(style);
			}

			OdfOfficeBody body = document.getOfficeBody();
			OdfOfficeText text = OdfElement.findFirstChildNode(OdfOfficeText.class, body);
			if (text != null)
			{
				OdfOfficeText textCopy = (OdfOfficeText) text.cloneNode(true);
				OdfStylableElement stylableElement = OdfElement.findFirstChildNode(OdfStylableElement.class, text);
				while (stylableElement != null)
				{
					text.removeChild(stylableElement);
					stylableElement = OdfElement.findFirstChildNode(OdfStylableElement.class, text);
				}

				for (int i = 0; i < values.length; i++)
				{
					stylableElement = OdfElement.findFirstChildNode(OdfStylableElement.class, textCopy);
					if (i == 0)
						style.setStyleParentStyleNameAttribute(stylableElement.getStyleName());
					else
						stylableElement.setStyleName(styleName);

					while (stylableElement != null)
					{
						OdfStylableElement clonedStylableElement = (OdfStylableElement) stylableElement.cloneNode(true);
						this.replaceContent(fileDom, clonedStylableElement, values[i]);
						text.appendChild(clonedStylableElement);
						stylableElement = OdfElement.findNextChildNode(OdfStylableElement.class, stylableElement);
					}
				}

			}

			showDocument(document);
		}
		catch (Exception e)
		{
			status = new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
					"Beim Aufbereiten der Dokumente ist ein Fehler aufgetreten.", e);
		}
		return status;
	}

	protected OdfTableRow[] collectTableRows(final OdfTable table)
	{
		Collection<OdfTableRow> rows = new ArrayList<OdfTableRow>();
		NodeList list = table.getChildNodes();
		for (int i = 0; i < list.getLength(); i++)
		{
			if (list.item(i) instanceof OdfTableRow)
				rows.add((OdfTableRow) list.item(i));
		}
		if (rows.size() < 1)
			return new OdfTableRow[0];
		if (rows.size() > 3)
			return new OdfTableRow[0];

		OdfTableRow[] tableRows = rows.toArray(new OdfTableRow[0]);
		rows = new ArrayList<OdfTableRow>();
		for (int i = 0; i < tableRows.length; i++)
			rows.add(tableRows[i]);

		return rows.toArray(new OdfTableRow[0]);
	}

	protected Node fillDummyRow(final OdfFileDom fileDom, final Node node, final DataMap map)
	{
		if (node instanceof OdfTextInput)
		{
			this.replaceOdfTextInputWithOdfTextSpan(fileDom, (OdfTextInput) node, map);
		}
		else if (node instanceof OdfConditionalText)
		{
			this.replaceConditionalTextWithCurrentValue((OdfConditionalText) node, map);
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

	protected void fillTable(final OdfFileDom fileDom, final OdfTable table, final DataMap map)
	{
		String name = table.getOdfAttributeValue(OdfAttributeNames.TABLENAME.getOdfName());
		List<DataMap> collection = map.getTableMaps(name);

		OdfTableRow[] rows = this.collectTableRows(table);
		OdfTableRow totalRow = null;

		OdfTableRow templateRow = null;
		OdfTableRow inputRow = null;
		int startingRow = 0;
		if (rows.length == 1)
		{
			inputRow = rows[0];
			templateRow = (OdfTableRow) rows[0].cloneNode(true);
			startingRow = 0;
		}
		else
		{
			inputRow = rows[1];
			templateRow = (OdfTableRow) rows[1].cloneNode(true);
			startingRow = 1;
		}

		if (rows.length > 2)
		{
			totalRow = rows[2];
			table.removeChild(totalRow);
		}

		if (collection == null || collection.isEmpty())
		{
			this.fillDummyRow(fileDom, inputRow, new EmptyDataMap());
		}
		else
		{
			DataMap[] maps = collection.toArray(new DataMap[0]);
			this.fillTableRow(fileDom, rows[startingRow], maps[0]);
			for (int i = 1; i < maps.length; i++)
			{
				table.appendRow(this.fillTableRow(fileDom, (OdfTableRow) templateRow.cloneNode(true), maps[i]));
			}
			if (totalRow != null)
			{
				table.appendRow(this.fillTableRow(fileDom, totalRow, map));
			}
		}
	}

	protected OdfTableRow fillTableRow(final OdfFileDom fileDom, final OdfTableRow row, final DataMap map)
	{
		return (OdfTableRow) this.replaceTableContent(fileDom, row, map);
	}

	protected void replaceConditionalTextWithCurrentValue(final OdfConditionalText element, final DataMap map)
	{
		OdfConditionalText conditionalText = element;
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

	protected void replaceContent(final OdfFileDom fileDom, final Node node, final DataMap map)
	{
		if (node instanceof OdfTextInput)
		{
			this.replaceOdfTextInputWithOdfTextSpan(fileDom, (OdfTextInput) node, map);
		}
		else if (node instanceof OdfConditionalText)
		{
			this.replaceConditionalTextWithCurrentValue((OdfConditionalText) node, map);
		}
		else if (node instanceof OdfTable)
		{
			this.fillTable(fileDom, (OdfTable) node, map);
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

	protected void replaceOdfTextInputWithOdfTextSpan(final OdfFileDom fileDom, final OdfTextInput element,
			final DataMap map)
	{
		OdfTextInput input = element;
		String desc = input.getTextDescriptionAttribute();
		String value = map.getProperty(desc, "");
		// if (value.isEmpty()) value = "<Keine Angaben>";
		OdfTextSpan span = fileDom.newOdfElement(OdfTextSpan.class);
		span = span.addContentWhitespace(value);
		OdfElement parent = (OdfElement) input.getParentNode();
		parent.replaceChild(span, input);
	}

	protected Node replaceTableContent(final OdfFileDom fileDom, final Node node, final DataMap map)
	{
		if (node instanceof OdfTextInput)
		{
			this.replaceOdfTextInputWithOdfTextSpan(fileDom, (OdfTextInput) node, map);
		}
		else if (node instanceof OdfConditionalText)
		{
			this.replaceConditionalTextWithCurrentValue((OdfConditionalText) node, map);
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

	private IStatus showDocument(final OdfDocument document) throws Exception
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
