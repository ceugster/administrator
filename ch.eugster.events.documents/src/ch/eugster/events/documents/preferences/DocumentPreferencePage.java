package ch.eugster.events.documents.preferences;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
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
		final FileFieldEditor outFileEditor = new FileFieldEditor(PreferenceConstants.KEY_PRINT_OUT_KEYS,
				"Ausdruck der Schlüssel/Wert-Paare", this.getFieldEditorParent());
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
					Writer writer = new FileWriter(path);

					writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n");
					writer.write("<html>\n");
					writer.write("\t<head>\n");
					writer.write("\t\t<title>Liste der Schlüssel</title>\n");
					writer.write("\t</head>\n");
					writer.write("\t<body>\n");

					writer.write("\t\t<h1>Schlüssel für Domänen</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (DomainMap.Key key : DomainMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Personen</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (PersonMap.Key key : PersonMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					for (LinkMap.Key key : LinkMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Adressen</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (AddressMap.Key key : AddressMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Adressgruppenkategorien</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (AddressGroupCategoryMap.Key key : AddressGroupCategoryMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Adressgruppen</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (AddressGroupMap.Key key : AddressGroupMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Adressgruppenmitglieder</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (AddressGroupMemberMap.Key key : AddressGroupMemberMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Saison</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (SeasonMap.Key key : SeasonMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Kurskategorien</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (CategoryMap.Key key : CategoryMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Kursrubriken</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (RubricMap.Key key : RubricMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Kurse</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (CourseMap.Key key : CourseMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Kursleitungen</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (CourseGuideMap.Key key : CourseGuideMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					for (GuideMap.Key key : GuideMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Kursbuchungen</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (BookingTypeMap.Key key : BookingTypeMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					for (BookingMap.Key key : BookingMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Kursdetails (Zeiten und Orte)</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (CourseDetailMap.Key key : CourseDetailMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t\t<h1>Schlüssel für Spenden</h1>\n");
					writer.write("\t\t<table border=\"0\">\n");
					for (DonationMap.Key key : DonationMap.Key.values())
					{
						writer.write("\t\t\t<tr>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getKey() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t\t<td>\n");
						writer.write("\t\t\t\t\t" + key.getDescription() + "\n");
						writer.write("\t\t\t</td>\n");
						writer.write("\t\t\t</tr>\n");
					}
					writer.write("\t\t</table>\n");

					writer.write("\t</body>\n");
					writer.write("<html>\n");

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

				String value = this.templateEditor.getTextControl(this.getFieldEditorParent()).getText();
				if (value.isEmpty())
					value = newValue;
				else
					value = value.replace(oldValue, newValue);

				this.templateEditor.getTextControl(this.getFieldEditorParent()).setText(value);
			}
		}
		else if (event.getSource().equals(this.templateEditor))
		{

		}
	}

}
