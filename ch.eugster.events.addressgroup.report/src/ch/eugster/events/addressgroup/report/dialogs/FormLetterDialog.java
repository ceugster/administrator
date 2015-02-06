package ch.eugster.events.addressgroup.report.dialogs;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.report.Activator;
import ch.eugster.events.documents.maps.AddressGroupMemberMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.editors.EditorSelector;

public class FormLetterDialog extends TitleAreaDialog
{
	private Text documentPath;

	private Button documentSelector;

	private Button useCollectionAddress;

	private final StructuredSelection selection;

	private final String message = "Wählen Sie das Dokument, das als Serienbriefvorlage verwendet werden soll.";

	private boolean isPageComplete = false;

	/**
	 * @param parentShell
	 * @param parent
	 *            <code>parent</code> must be of type
	 *            ch.eugster.events.data.objects.Customer
	 * @param addressGroup
	 *            Falls eine neue Adressgruppe erfasst wird, muss diese bereit
	 *            vor der Übergabe an den Konstruktor von
	 *            <code>AddressGroupDialog</code> instantiiert sein und der
	 *            Parent <code>Domain</code> muss - falls ein solcher gesetzt
	 *            werden soll, ebenfalls dem Konstruktur von
	 *            <code>AddressGroup</code> übergeben worden sein.
	 * 
	 */
	public FormLetterDialog(final Shell parentShell, final StructuredSelection selection)
	{
		super(parentShell);
		this.selection = selection;
	}

	private IStatus buildDocument(IProgressMonitor monitor, final File template, final DataMap[] dataMaps)
	{
		IStatus status = Status.CANCEL_STATUS;
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				DocumentBuilderService.class.getName(), null);
		try
		{
			tracker.open();
			Object[] services = tracker.getServices();
			for (Object service : services)
			{
				if (status.isOK())
				{
					return status;
				}
				if (service instanceof DocumentBuilderService)
				{
					DocumentBuilderService builderService = (DocumentBuilderService) service;
					status = builderService.buildDocument(monitor, template, dataMaps);
				}
			}
		}
		finally
		{
			tracker.close();
		}
		return status;
	}

	private void computeAddressGroup(final Map<String, DataMap> map, final AddressGroup addressGroup)
	{
		if (!addressGroup.isDeleted())
		{
			List<AddressGroupMember> members = addressGroup.getAddressGroupMembers();
			for (AddressGroupMember member : members)
			{
				computeAddressGroupMember(map, member);
			}
			// List<AddressGroupLink> children =
			// addressGroup.getChildren();
			// for (AddressGroupLink child : children)
			// {
			// computeAddressGroup(map, child.getChild());
			// }
		}
	}

	private void computeAddressGroupCategory(final Map<String, DataMap> map, final AddressGroupCategory category)
	{
		if (!category.isDeleted())
		{
			List<AddressGroup> addressGroups = category.getAddressGroups();
			for (AddressGroup addressGroup : addressGroups)
			{
				computeAddressGroup(map, addressGroup);
			}
		}
	}

	private void computeAddressGroupMember(final Map<String, DataMap> map, final AddressGroupMember member)
	{
		if (!member.isDeleted())
		{
			AddressGroupMemberMap memberMap = new AddressGroupMemberMap(member, this.useCollectionAddress.getSelection());
			DataMap existing = map.get(memberMap.getId());
			if (existing == null)
			{
				map.put(memberMap.getId(), memberMap);
			}
		}
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, "Generieren", true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
		File file = new File(documentPath.getText());
		this.getButton(IDialogConstants.OK_ID).setEnabled(file.isFile());
	}

	private Collection<DataMap> createDataMaps(final StructuredSelection ssel)
	{
		Map<String, DataMap> maps = new HashMap<String, DataMap>();
		Object[] elements = ssel.toArray();
		for (Object element : elements)
		{
			if (element instanceof AddressGroupCategory)
			{
				AddressGroupCategory category = (AddressGroupCategory) element;
				computeAddressGroupCategory(maps, category);
			}
			else if (element instanceof AddressGroup)
			{
				AddressGroup addressGroup = (AddressGroup) element;
				computeAddressGroup(maps, addressGroup);
			}
			// else if (element instanceof AddressGroupLink)
			// {
			// AddressGroupLink addressGroupLink = (AddressGroupLink) element;
			// computeAddressGroup(maps, addressGroupLink.getChild());
			// }
			else if (element instanceof AddressGroupMember)
			{
				AddressGroupMember member = (AddressGroupMember) element;
				computeAddressGroupMember(maps, member);
			}
		}
		return maps.values();
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Serienbrief");

		File file = null;
		if (User.getCurrent() != null)
		{
			if (!User.getCurrent().getTemplateDirectory().isEmpty())
			{
				if (User.getCurrent().getLastUsedFormLetter().isEmpty())
				{
					file = new File(User.getCurrent().getTemplateDirectory());
				}
				else
				{
					file = new File(User.getCurrent().getTemplateDirectory() + File.separator
							+ User.getCurrent().getLastUsedFormLetter());
				}
			}
		}
		if (file == null || !file.exists())
		{
			file = new File(System.getProperty("user.home"));
		}
		documentPath = new Text(composite, SWT.BORDER | SWT.SINGLE);
		documentPath.setText(file.getAbsolutePath());
		documentPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		documentPath.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				File file = new File(documentPath.getText());
				User.getCurrent().setTemplateDirectory(file.getParentFile().getAbsolutePath());
				User.getCurrent().setLastUsedFormLetter(file.getName());
			}
		});

		documentSelector = new Button(composite, SWT.PUSH);
		documentSelector.setText("...");
		documentSelector.setLayoutData(new GridData());
		documentSelector.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				String path = FormLetterDialog.this.documentPath.getText();
				FileDialog dialog = new FileDialog(FormLetterDialog.this.getShell());
				dialog.setFilterPath(path);
				dialog.setFilterExtensions(new String[] { "*.odt", "*.doc", "*.docx" });
				dialog.setText("Vorlage Serienbrief");
				path = dialog.open();
				if (path != null)
				{
					FormLetterDialog.this.documentPath.setText(path);

				}
				File file = new File(FormLetterDialog.this.documentPath.getText());
				FormLetterDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(file.isFile());
			}
		});

		if (EditorSelector.values()[PersonSettings.getInstance().getEditorSelector()]
				.equals(EditorSelector.MULTI_PAGE_EDITOR))
		{
			label = new Label(composite, SWT.NONE);
			label.setLayoutData(new GridData());

			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;

			useCollectionAddress = new Button(composite, SWT.CHECK);
			useCollectionAddress.setText("Sammeladressen verwenden");
			useCollectionAddress.setLayoutData(gridData);
		}

		return parent;
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		setCurrentUser();
		final File template = setTemplateSetting(documentPath.getText());
		final DataMap[] dataMaps = createDataMaps(selection).toArray(new DataMap[0]);
		super.okPressed();

		UIJob job = new UIJob("Dokument wird generiert...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				return FormLetterDialog.this.buildDocument(monitor, template, dataMaps);
			}
		};
		job.addJobChangeListener(new JobChangeAdapter()
		{
			@Override
			public void done(final IJobChangeEvent event)
			{
				if (!event.getResult().isOK())
				{
					ErrorDialog.openError(FormLetterDialog.this.getShell(), "Verarbeitungsfehler",
							"Beim Generieren des Dokuments ist ein Fehler aufgetreten.", event.getResult(), 0);
				}
			}
		});
		job.schedule();
	}

	private void setCurrentUser()
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		try
		{
			tracker.open();
			Object service = tracker.getService();
			if (service instanceof ConnectionService)
			{
				ConnectionService connectionService = (ConnectionService) service;
				UserQuery query = (UserQuery) connectionService.getQuery(User.class);
				User.setCurrent(query.merge(User.getCurrent()));
			}
		}
		finally
		{
			tracker.close();
		}
	}

	@Override
	public void setErrorMessage(final String errorMessage)
	{
		super.setErrorMessage(errorMessage);
		this.setPageComplete(false);
	}

	public void setMessage()
	{
		this.setErrorMessage(null);
		super.setMessage(this.message);
		this.setPageComplete(true);
	}

	public void setPageComplete(final boolean isComplete)
	{
		this.isPageComplete = isComplete;
		if (this.getButton(IDialogConstants.OK_ID) != null)
			this.getButton(IDialogConstants.OK_ID).setEnabled(this.isPageComplete);
	}

	private File setTemplateSetting(final String path)
	{
		IDialogSettings settings = Activator.getDefault().getDialogSettings().getSection("form.letter.dialog");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("form.letter.dialog");
		}
		settings.put("form.letter.dialog.path", path);
		return new File(path);
	}

	public void setTitle()
	{
		super.setTitle("Vorlage Serienbrief");
		// int width = this.getShell().getDisplay().getClientArea().width;
		// int height = this.getShell().getDisplay().getClientArea().height;
		// super.getShell().setMinimumSize(width > 500 ? 500 : width, height);
	}

}
