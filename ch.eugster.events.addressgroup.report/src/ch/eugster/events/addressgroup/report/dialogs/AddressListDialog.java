package ch.eugster.events.addressgroup.report.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.report.Activator;
import ch.eugster.events.documents.maps.AddressGroupMap;
import ch.eugster.events.documents.maps.AddressGroupMemberMap;
import ch.eugster.events.documents.maps.AddressMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.maps.DataMapKey;
import ch.eugster.events.documents.maps.LinkMap;
import ch.eugster.events.documents.maps.PersonMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupCategory;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.editors.EditorSelector;

public class AddressListDialog extends TitleAreaDialog
{
	private Button collectionSelector;

	private final StructuredSelection selection;

	private final String message = "Erstellen einer Adressliste der selektierten Adressgruppe.";

	private boolean isPageComplete = false;

	private IDialogSettings settings;

	private ComboViewer sortViewer;

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
	public AddressListDialog(final Shell parentShell, final StructuredSelection selection)
	{
		super(parentShell);
		this.selection = selection;
		settings = Activator.getDefault().getDialogSettings().getSection("address.list.dialog");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("address.list.dialog");
		}
		if (settings.getBoolean("selected.sort"))
		{
			settings.put("selected.sort", 0);
		}
	}

	private IStatus buildDocument(final DataMapKey[] keys, final DataMap[] dataMaps)
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
					status = builderService.buildDocument(keys, dataMaps);
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
			Collection<AddressGroupMember> members = addressGroup.getAddressGroupMembers();
			for (AddressGroupMember member : members)
			{
				computeAddressGroupMember(map, member);
			}
			// Collection<AddressGroupLink> children =
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
			Collection<AddressGroup> addressGroups = category.getAddressGroups();
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
			AddressGroupMemberMap memberMap = new AddressGroupMemberMap(member);
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
		composite.setLayout(new GridLayout(2, false));

		if (EditorSelector.values()[PersonSettings.getInstance().getEditorSelector()]
				.equals(EditorSelector.MULTI_PAGE_EDITOR))
		{
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;

			collectionSelector = new Button(composite, SWT.CHECK);
			collectionSelector.setText("Gruppenadressen nur einmal auflisten");
			collectionSelector.setLayoutData(gridData);
		}

		Label label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Sortierung");

		DataMapKey[] keys = this.getSortKeys();
		Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sortViewer = new ComboViewer(combo);
		sortViewer.setContentProvider(new ArrayContentProvider());
		sortViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof DataMapKey)
				{
					DataMapKey key = (DataMapKey) element;
					return key.getName();
				}
				return "";
			}
		});
		sortViewer.setInput(this.getSortKeys());
		sortViewer.setSelection(new StructuredSelection(new DataMapKey[] { keys[0] }));

		return parent;
	}

	private DataMapKey[] getSortKeys()
	{
		Collection<DataMapKey> keys = new ArrayList<DataMapKey>();
		keys.add(PersonMap.Key.LASTNAME);
		keys.add(LinkMap.Key.MEMBER);
		return keys.toArray(new DataMapKey[0]);
	}

	private DataMapKey[] getKeys()
	{
		Collection<DataMapKey> keys = new ArrayList<DataMapKey>();
		keys.add(AddressGroupMemberMap.Key.TYPE);
		keys.add(AddressGroupMemberMap.Key.ID);
		keys.add(PersonMap.Key.SEX);
		keys.add(PersonMap.Key.FORM);
		keys.add(AddressGroupMemberMap.Key.SALUTATION);
		keys.add(PersonMap.Key.TITLE);
		keys.add(PersonMap.Key.FIRSTNAME);
		keys.add(PersonMap.Key.LASTNAME);
		keys.add(AddressMap.Key.NAME);
		keys.add(AddressGroupMemberMap.Key.ANOTHER_LINE);
		keys.add(PersonMap.Key.BIRTHDATE);
		keys.add(PersonMap.Key.PROFESSION);
		keys.add(LinkMap.Key.FUNCTION);
		keys.add(LinkMap.Key.PHONE);
		keys.add(AddressMap.Key.PHONE);
		keys.add(PersonMap.Key.PHONE);
		keys.add(AddressMap.Key.FAX);
		keys.add(PersonMap.Key.EMAIL);
		keys.add(PersonMap.Key.WEBSITE);
		keys.add(AddressMap.Key.ADDRESS);
		keys.add(AddressMap.Key.POB);
		keys.add(AddressMap.Key.COUNTRY);
		keys.add(AddressMap.Key.ZIP);
		keys.add(AddressMap.Key.CITY);
		keys.add(AddressMap.Key.COUNTY);
		keys.add(AddressGroupMemberMap.Key.POLITE);
		keys.add(AddressGroupMap.Key.NAME);
		keys.add(LinkMap.Key.MEMBER);
		keys.addAll(PersonMap.getExtendedFieldKeys());
		keys.addAll(LinkMap.getExtendedFieldKeys());
		return keys.toArray(new DataMapKey[0]);
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		setCurrentUser();
		final DataMapKey[] keys = getKeys();
		final DataMap[] dataMaps = createDataMaps(selection).toArray(new DataMap[0]);
		Arrays.sort(dataMaps, new Comparator<DataMap>()
		{
			@Override
			public int compare(DataMap map1, DataMap map2)
			{
				if (sortViewer.getSelection() instanceof IStructuredSelection)
				{
					IStructuredSelection ssel = (IStructuredSelection) sortViewer.getSelection();
					if (ssel.getFirstElement() instanceof DataMapKey)
					{
						try
						{
							DataMapKey key = (DataMapKey) ssel.getFirstElement();
							String value1 = map1.getProperty(key.getKey());
							String value2 = map2.getProperty(key.getKey());
							return value1.compareTo(value2);
						}
						catch (Exception e)
						{
							return 0;
						}
					}
				}
				return 0;
			}
		});
		super.okPressed();

		UIJob job = new UIJob("Dokument wird generiert...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				return AddressListDialog.this.buildDocument(keys, dataMaps);
			}
		};
		job.addJobChangeListener(new JobChangeAdapter()
		{
			@Override
			public void done(final IJobChangeEvent event)
			{
				if (!event.getResult().isOK())
				{
					ErrorDialog.openError(AddressListDialog.this.getShell(), "Verarbeitungsfehler",
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
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			UserQuery query = (UserQuery) service.getQuery(User.class);
			User.setCurrent(query.merge(User.getCurrent()));
		}
		tracker.close();
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

	public void setTitle()
	{
		super.setTitle("Adressliste generieren");
	}

}
