package ch.eugster.events.member.wizards;

import java.io.File;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.eugster.events.member.Activator;

public class ImportMemberSelectSourceFileWizardPage extends WizardPage implements IWizardPage
{
	private IDialogSettings settings;

	private Text path;

	public ImportMemberSelectSourceFileWizardPage()
	{
		super("import.member.select.source.file.wizard.page");

		settings = Activator.getDefault().getDialogSettings()
				.getSection("import.member.select.source.file.wizard.page");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings()
					.addNewSection("import.member.select.source.file.wizard.page");
		}
		if (settings.get("file.path") == null)
		{
			settings.put("file.path", "");
		}
	}

	@Override
	public void createControl(final Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.NONE);
		label.setText("Quelldatei");
		label.setLayoutData(new GridData());

		path = new Text(composite, SWT.BORDER | SWT.SINGLE);
		path.setText(settings.get("file.path"));
		path.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button button = new Button(composite, SWT.PUSH);
		button.setText("...");
		button.setLayoutData(new GridData());
		button.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(ImportMemberSelectSourceFileWizardPage.this.getShell(), SWT.NONE);
				dialog.setText("Datei für den Mitgliedernummernabgleich");
				dialog.setFilterExtensions(new String[] { "*.xls" });
				dialog.setFilterIndex(0);
				dialog.setFilterNames(new String[] { "Excel Arbeitsmappe (*.xls)" });
				dialog.setFilterPath(settings.get("file.path"));
				String path = dialog.open();
				if (path != null)
				{
					File file = new File(path);
					if (file.isFile())
					{
						ImportMemberSelectSourceFileWizardPage.this.path.setText(file.getAbsolutePath());

					}
				}
			}
		});

		this.setControl(composite);
	}

}
