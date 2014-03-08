package ch.eugster.events.documents.preferences;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import ch.eugster.events.documents.Activator;
import ch.eugster.events.documents.maps.AddressGroupCategoryMap;
import ch.eugster.events.documents.maps.AddressGroupMap;
import ch.eugster.events.documents.maps.AddressGroupMemberMap;
import ch.eugster.events.documents.maps.AddressMap;
import ch.eugster.events.documents.maps.BookingMap;
import ch.eugster.events.documents.maps.BookingTypeMap;
import ch.eugster.events.documents.maps.CategoryMap;
import ch.eugster.events.documents.maps.CourseDetailMap;
import ch.eugster.events.documents.maps.CourseGuideMap;
import ch.eugster.events.documents.maps.CourseMap;
import ch.eugster.events.documents.maps.DomainMap;
import ch.eugster.events.documents.maps.DonationMap;
import ch.eugster.events.documents.maps.GuideMap;
import ch.eugster.events.documents.maps.LinkMap;
import ch.eugster.events.documents.maps.LinkMap.TableKey;
import ch.eugster.events.documents.maps.PersonMap;
import ch.eugster.events.documents.maps.RubricMap;
import ch.eugster.events.documents.maps.SeasonMap;

public class DocumentPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	private DirectoryFieldEditor pathEditor;

	private DirectoryFieldEditor templateEditor;

	public DocumentPreferencePage()
	{
		this(FieldEditorPreferencePage.GRID);
	}

	public DocumentPreferencePage(final int style)
	{
		super(style);
	}

	public DocumentPreferencePage(final String title, final ImageDescriptor image, final int style)
	{
		super(title, image, style);
	}

	public DocumentPreferencePage(final String title, final int style)
	{
		super(title, style);
	}

	@Override
	protected void createFieldEditors()
	{
		final SaveFileFieldEditor outFileEditor = new SaveFileFieldEditor(PreferenceConstants.KEY_PRINT_OUT_KEYS,
				"Ausdruck der Schlüssel/Wert-Paare", this.getFieldEditorParent());
		outFileEditor.setFileExtensions(new String[] { ".html" });
		outFileEditor.setPropertyChangeListener(this);
		outFileEditor.setChangeButtonText("...");
		this.addField(outFileEditor);

		Button button = new Button(this.getFieldEditorParent(), SWT.PUSH);
		button.setText("Schlüsselwörter drucken");
		button.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				try
				{
					String path = outFileEditor.getStringValue();
					File file = new File(path);
					if (file.isFile())
						file.getParentFile();
					Writer writer = new FileWriter(path);

					startBody(writer);

					printHeader(writer, 1, "domain", "Schlüssel für Domänen");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (DomainMap.Key key : DomainMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "person", "Schlüssel für Personen");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (PersonMap.Key key : PersonMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "link", "Schlüssel für Person/Adressen-Links");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (LinkMap.Key key : LinkMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 2, "Referenzen");
					startTable(writer, 0);
					startTableRow(writer);
					printCell(writer, "#person", "Person");
					endTableRow(writer);
					startTableRow(writer);
					printCell(writer, "#address", "Adresse");
					endTableRow(writer);

					printHeader(writer, 2, "Tabellen");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Verweis");
					endTableRow(writer);
					startTableRow(writer);
					for (TableKey key : LinkMap.TableKey.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, "#spende", key.getDescription());
					}
					endTableRow(writer);

					printHeader(writer, 1, "address", "Schlüssel für Adressen");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (AddressMap.Key key : AddressMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "addressgroupcategory", "Schlüssel für Adressgruppenkategorien");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (AddressGroupCategoryMap.Key key : AddressGroupCategoryMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "addressgroup", "Schlüssel für Adressgruppen");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (AddressGroupMap.Key key : AddressGroupMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "addressgroupmember", "Schlüssel für Adressgruppenmitglieder");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (AddressGroupMemberMap.Key key : AddressGroupMemberMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "season", "Schlüssel für Saisons");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (SeasonMap.Key key : SeasonMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "category", "Schlüssel für Kurskategorien");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (CategoryMap.Key key : CategoryMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "rubric", "Schlüssel für Kursrubriken");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (RubricMap.Key key : RubricMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "course", "Schlüssel für Kurse");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (CourseMap.Key key : CourseMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "courseguide", "Schlüssel für Kursleitungen");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (CourseGuideMap.Key key : CourseGuideMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "guide", "Schlüssel für Kursleiter");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (GuideMap.Key key : GuideMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "booking", "Schlüssel für Kursbuchungen");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (BookingMap.Key key : BookingMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "category", "Schlüssel für Kursbuchungsarten");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (BookingTypeMap.Key key : BookingTypeMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "coursedetail", "Schlüssel für Kursdetails (Zeiten und Orte)");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (CourseDetailMap.Key key : CourseDetailMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					printHeader(writer, 1, "donation", "Schlüssel für Spenden");
					startTable(writer, 0);
					startTableRow(writer);
					printHeaderCell(writer, "Bezeichnung");
					printHeaderCell(writer, "Bedeutung");
					endTableRow(writer);
					startTableRow(writer);
					for (DonationMap.Key key : DonationMap.Key.values())
					{
						printCell(writer, key.getKey());
						printCell(writer, key.getKey(), key.getDescription());
					}
					endTableRow(writer);
					endTable(writer);

					endBody(writer);

					writer.close();

					MessageDialog.openConfirm(DocumentPreferencePage.this.getFieldEditorParent().getShell(),
							"Liste gedruckt", "Der Ausdruck der Schlüssel/Wert-Paare wurde erfolgreich ausgedruckt.");
				}
				catch (IOException e1)
				{
				}
			}

		});
	}

	private void startBody(Writer writer)
	{
		try
		{
			writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n");
			writer.write("<html>\n");
			writer.write("\t<head>\n");
			writer.write("\t\t<title>Liste der Schlüssel</title>\n");
			writer.write("\t</head>\n");
			writer.write("\t<body>\n");
		}
		catch (IOException e)
		{

		}
	}

	private void endBody(Writer writer)
	{
		try
		{
			writer.write("\t</body>\n");
			writer.write("<html>\n");
		}
		catch (IOException e)
		{

		}
	}

	private void startTableRow(Writer writer)
	{
		try
		{
			writer.write("\t\t\t<tr>\n");
		}
		catch (IOException e)
		{

		}
	}

	private void endTableRow(Writer writer)
	{
		try
		{
			writer.write("\t\t\t</tr>\n");
		}
		catch (IOException e)
		{

		}
	}

	private void startTable(Writer writer, int border)
	{
		try
		{
			writer.write("\t\t\t<table border\"" + border + "\">\n");
		}
		catch (IOException e)
		{

		}
	}

	private void endTable(Writer writer)
	{
		try
		{
			writer.write("\t\t</table>\n");
		}
		catch (IOException e)
		{

		}
	}

	private void printHeaderCell(Writer writer, String value)
	{
		try
		{
			writer.write("\t\t\t\t<th>\n");
			writer.write("\t\t\t\t\t" + value + "\n");
			writer.write("\t\t\t\t</th>\n");
		}
		catch (IOException e)
		{

		}
	}

	private void printCell(Writer writer, String value)
	{
		printCell(writer, null, value);
	}

	private void printCell(Writer writer, String ref, String value)
	{
		try
		{
			writer.write("\t\t\t\t<td>\n");
			writer.write("\t\t\t\t\t" + (ref == null ? "" : "<a href=\"" + ref + "\">") + value
					+ (ref == null ? "" : "</a>") + "\n");
			writer.write("\t\t\t\t</td>\n");
		}
		catch (IOException e)
		{

		}
	}

	private void printHeader(Writer writer, int level, String title)
	{
		printHeader(writer, level, null, title);
	}

	private void printHeader(Writer writer, int level, String ref, String title)
	{
		try
		{
			writer.write("\t\t<h" + level + ">" + (ref == null ? "" : "<a name=\"" + ref + "\">") + title
					+ (ref == null ? "" : "</a>") + "</h" + level + ">\n");
		}
		catch (IOException e)
		{

		}
	}

	@Override
	public void init(final IWorkbench workbench)
	{
		IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), Activator.getDefault()
				.getBundleContext().getBundle().getSymbolicName());
		this.setPreferenceStore(store);
		this.setDescription("Pfade zu externen Ressourcen");
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event)
	{
		if (event.getSource().equals(this.pathEditor))
		{
			if (event.getProperty().equals("field_editor_value"))
			{
				String oldValue = (String) event.getOldValue();
				String newValue = (String) event.getNewValue();

				String value = this.pathEditor.getTextControl(this.getFieldEditorParent()).getText();
				if (!value.toLowerCase().endsWith(".html"))
				{
					value = value + ".html";
				}
				if (value.isEmpty())
					value = newValue;
				else
					value = value.replace(oldValue, newValue);

				this.pathEditor.getTextControl(this.getFieldEditorParent()).setText(value);
				File file = new File(value);
				if (!file.exists())
				{
					try
					{
						file.createNewFile();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		else if (event.getSource().equals(this.templateEditor))
		{

		}
	}

}
