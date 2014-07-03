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
import ch.eugster.events.documents.maps.AbstractDataMap;
import ch.eugster.events.documents.maps.AddressGroupCategoryMap;
import ch.eugster.events.documents.maps.AddressGroupMap;
import ch.eugster.events.documents.maps.AddressGroupMemberMap;
import ch.eugster.events.documents.maps.AddressMap;
import ch.eugster.events.documents.maps.BookingMap;
import ch.eugster.events.documents.maps.BookingTypeMap;
import ch.eugster.events.documents.maps.CategoryMap;
import ch.eugster.events.documents.maps.CompensationMap;
import ch.eugster.events.documents.maps.CourseDetailMap;
import ch.eugster.events.documents.maps.CourseGuideMap;
import ch.eugster.events.documents.maps.CourseMap;
import ch.eugster.events.documents.maps.DomainMap;
import ch.eugster.events.documents.maps.DonationMap;
import ch.eugster.events.documents.maps.GuideMap;
import ch.eugster.events.documents.maps.LinkMap;
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
					AbstractDataMap.getDataMap(DomainMap.class).printHTML(writer, "domain", "Domänen");
					AbstractDataMap.getDataMap(PersonMap.class).printHTML(writer, "person", "Personen");
					AbstractDataMap.getDataMap(LinkMap.class).printHTML(writer, "link", "Link Personen/Adressen");
					AbstractDataMap.getDataMap(AddressMap.class).printHTML(writer, "address", "Adressen");
					AbstractDataMap.getDataMap(AddressGroupCategoryMap.class).printHTML(writer, "addressGroupCategory", "Adressgruppenkategorien");
					AbstractDataMap.getDataMap(AddressGroupMap.class).printHTML(writer, "addressGroup", "Adressgruppen");
					AbstractDataMap.getDataMap(AddressGroupMemberMap.class).printHTML(writer, "addressGroupMember", "Adressgruppenmitglieder");
					AbstractDataMap.getDataMap(SeasonMap.class).printHTML(writer, "season", "Saisons");
					AbstractDataMap.getDataMap(CategoryMap.class).printHTML(writer, "category", "Kurskategorien");
					AbstractDataMap.getDataMap(RubricMap.class).printHTML(writer, "rubric", "Kursrubriken");
					AbstractDataMap.getDataMap(CourseMap.class).printHTML(writer, "course", "Kurse");
					AbstractDataMap.getDataMap(CourseGuideMap.class).printHTML(writer, "course_guide", "Kursleitungen");
					AbstractDataMap.getDataMap(CompensationMap.class).printHTML(writer, "#compensation", "Entschädigungen");
					AbstractDataMap.getDataMap(GuideMap.class).printHTML(writer, "guide", "Leitungspersonen");
					AbstractDataMap.getDataMap(BookingMap.class).printHTML(writer, "booking", "Kursbuchungen");
					AbstractDataMap.getDataMap(BookingTypeMap.class).printHTML(writer, "booking_type", "Buchungsarten");
					AbstractDataMap.getDataMap(CourseDetailMap.class).printHTML(writer, "course_detail", "Kursdetails");
					AbstractDataMap.getDataMap(DonationMap.class).printHTML(writer, "donation", "Spenden");
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

	private static void startBody(Writer writer)
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

	private static void endBody(Writer writer)
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

}
