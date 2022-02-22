package ch.eugster.events.addressgroup.report.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
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

	private List<AddressGroupLine> addressGroupLines = null;
	
	private Set<AddressGroupMember> addressGroupMembers = new HashSet<AddressGroupMember>();
	
	private ComboViewer sortViewer;

	private Button shortList;
	
	private Button courseVisits;

	private DataMap<?>[] dataMaps = null;

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

	private void extractAddressGroupMembers() 
	{
		if (this.addressGroupLines == null)
		{
			Iterator<?> iter = selection.iterator();
			while (iter.hasNext())
			{
				Object object = iter.next();
				if (object instanceof AddressGroupCategory)
				{
					AddressGroupCategory category = (AddressGroupCategory) object;
					List<AddressGroup> addressGroups = category.getValidAddressGroups();
					for (AddressGroup addressGroup : addressGroups)
					{
						extractAddressGroupMembers(addressGroup);
					}
				}
				else if (object instanceof AddressGroup)
				{
					extractAddressGroupMembers((AddressGroup) object);
				}
				else if (object instanceof AddressGroupMember)
				{
					AddressGroupMember member = (AddressGroupMember) object;
					if (!this.addressGroupMembers.contains(member))
					{
						this.addressGroupMembers.add(member);
					}
				}
			}
		}
		else
		{
			for (AddressGroupLine addressGroupLine : this.addressGroupLines)
			{
				if (addressGroupLine.doAdd())
				{
					extractAddressGroupMembers(addressGroupLine);
				}
			}
			for (AddressGroupLine addressGroupLine : this.addressGroupLines)
			{
				if (addressGroupLine.doRemove())
				{
					removeAddressGroupMembers(addressGroupLine);
				}
			}
		}
	}

	private void extractAddressGroupMembers(AddressGroup addressGroup)
	{
		List<AddressGroupMember> members = addressGroup.getValidAddressGroupMembers();
		for (AddressGroupMember member : members)
		{
			if (!this.addressGroupMembers.contains(member))
			{
				this.addressGroupMembers.add(member);
			}
		}
	}
	
	private void extractAddressGroupMembers(AddressGroupLine addressGroupLine)
	{
		List<AddressGroupMember> members = addressGroupLine.getAddressGroup().getValidAddressGroupMembers();
		for (AddressGroupMember member : members)
		{
			if (!this.addressGroupMembers.contains(member))
			{
				this.addressGroupMembers.add(member);
			}
		}
	}
	
	private void removeAddressGroupMembers(AddressGroupLine addressGroupLine)
	{
		List<AddressGroupMember> members = addressGroupLine.getAddressGroup().getValidAddressGroupMembers();
		for (AddressGroupMember member : members)
		{
			AddressGroupMember[] existingMembers = this.addressGroupMembers.toArray(new AddressGroupMember[0]);
			for (AddressGroupMember existingMember : existingMembers)
			{
				if (existingMember.getLink() != null && member.getLink() != null)
				{
					if (existingMember.getLink().getPerson().getId().equals(member.getLink().getPerson().getId()))
					{
						this.addressGroupMembers.remove(existingMember);
					}
				}
				else if (existingMember.getAddress() != null && member.getAddress() != null)
				{
					if (existingMember.getAddress().getId().equals(member.getAddress().getId()))
					{
						this.addressGroupMembers.remove(existingMember);
					}
				}
			}
		}
	}
	
	private Set<AddressGroup> extractAddressGroups(StructuredSelection selection) 
	{
		Set<AddressGroup> addressGroups = new HashSet<AddressGroup>();
		Iterator<?> iter = selection.iterator();
		while (iter.hasNext())
		{
			Object object = iter.next();
			if (object instanceof AddressGroupCategory)
			{
				AddressGroupCategory category = (AddressGroupCategory) object;
				List<AddressGroup> groups = category.getValidAddressGroups();
				for (AddressGroup group : groups)
				{
					if (!addressGroups.contains(group))
					{
						addressGroups.add(group);
					}
				}
			}
			else if (object instanceof AddressGroup)
			{
				AddressGroup group = (AddressGroup) object;
				if (!addressGroups.contains(group))
				{
					addressGroups.add(group);
				}
			}
		}
		return addressGroups;
	}

	private IStatus buildDocument(IProgressMonitor monitor, final DataMapKey[] keys, final DataMap<?>[] dataMaps)
	{
		IStatus status = Status.CANCEL_STATUS;
		final ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle().getBundleContext(),
				DocumentBuilderService.class, null);
		tracker.open();
		try
		{
			Object[] services = tracker.getServices();
			for (Object service : services)
			{
				if (service instanceof DocumentBuilderService)
				{

					DocumentBuilderService builderService = (DocumentBuilderService) service;
					status = builderService.buildDocument(monitor, keys, dataMaps);
				}
			}
		}
		finally
		{
			tracker.close();
		}
		return status;
	}

	private void computeAddressGroupLine(final Map<String, DataMap<?>> map, final AddressGroupLine addressGroupLine)
	{
		if (!addressGroupLine.getAddressGroup().isDeleted())
		{
			List<AddressGroupMember> members = addressGroupLine.getAddressGroup().getValidAddressGroupMembers();
			if (addressGroupLine.doAdd())
			{
				for (AddressGroupMember member : members)
				{
					computeAddressGroupMember(map, member);
				}
			}
			else if (addressGroupLine.doRemove())
			{
				
			}
		}
	}

//	private void computeAddressGroupCategory(final Map<String, DataMap<?>> map, final AddressGroupCategory category)
//	{
//		if (!category.isDeleted())
//		{
//			List<AddressGroup> addressGroups = category.getAddressGroups();
//			for (AddressGroup addressGroup : addressGroups)
//			{
//				computeAddressGroup(map, addressGroup);
//			}
//		}
//	}

	private void computeAddressGroupMember(final Map<String, DataMap<?>> map, final AddressGroupMember member)
	{
		if (member.isValid())
		{
			AddressGroupMemberMap memberMap = new AddressGroupMemberMap(member, this.collectionSelector.getSelection());
			DataMap<?> existing = map.get(memberMap.getId());
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

	private Collection<DataMap<?>> createDataMaps()
	{
		Map<String, DataMap<?>> maps = new HashMap<String, DataMap<?>>();
		if (this.addressGroupMembers != null)
		{
			for (AddressGroupMember member : this.addressGroupMembers)
			{
				computeAddressGroupMember(maps, member);
			}
		}
		else if (this.addressGroupLines != null)
		{
			for (AddressGroupLine line : this.addressGroupLines)
			{
				computeAddressGroupLine(maps, line);
			}
		}
		return maps.values();
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		Set<AddressGroup> extractedAddressGroups = extractAddressGroups(selection);
		if (extractedAddressGroups.size() > 1)
		{
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.verticalSpacing = 0;

			GridData gridData = new GridData(GridData.FILL_BOTH);
//			gridData.heightHint = 120;
			
			final ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.BORDER | SWT.SHADOW_ETCHED_IN | SWT.V_SCROLL | SWT.H_SCROLL);
			scrolledComposite.addListener(SWT.Resize, new Listener() 
			{
				@Override
				public void handleEvent(Event event) 
				{
					int width = scrolledComposite.getClientArea().width;
					scrolledComposite.setMinSize( parent.computeSize( width, SWT.DEFAULT ) );
				}
			});
			scrolledComposite.setLayout(layout);
			scrolledComposite.setLayoutData(gridData);

			layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.verticalSpacing = 0;

			gridData = new GridData(GridData.FILL_HORIZONTAL);
			//gridData.heightHint = extractedAddressGroups.size() * 46 + 20;

			Composite mainAddressGroupComposite = new Composite(scrolledComposite, SWT.NONE);
			mainAddressGroupComposite.setLayout(layout);
			mainAddressGroupComposite.setLayoutData(gridData);

			AddressGroup[] addressGroups = extractedAddressGroups.toArray(new AddressGroup[0]);
			this.addressGroupLines = new ArrayList<AddressGroupLine>();
			for (int i = 0; i < addressGroups.length; i++)
			{
				Composite addressGroupComposite = new Composite(mainAddressGroupComposite, SWT.None);
				addressGroupComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				addressGroupComposite.setLayout(new GridLayout(3, false));
				this.addressGroupLines.add(new AddressGroupLine(i, addressGroupComposite, addressGroups[i]));
			}
			scrolledComposite.setContent(mainAddressGroupComposite);
			scrolledComposite.setExpandHorizontal(true);
			scrolledComposite.setExpandVertical(true);
			scrolledComposite.pack();
			scrolledComposite.layout();
		}
		
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

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		shortList = new Button(composite, SWT.CHECK);
		shortList.setLayoutData(gridData);
		shortList.setText("Kurzliste");
		shortList.setSelection(settings.getBoolean("short.list.selected"));
		shortList.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				settings.put("short.list.selected", shortList.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});
		
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		courseVisits = new Button(composite, SWT.CHECK);
		courseVisits.setLayoutData(gridData);
		courseVisits.setText("mit Kursteilnahmen");
		courseVisits.setSelection(settings.getBoolean("course.visits.selected"));
		courseVisits.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				settings.put("course.visits.selected", courseVisits.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});

		return parent;
	}

	private DataMapKey[] getSortKeys()
	{
		List<DataMapKey> keys = new ArrayList<DataMapKey>();
		keys.add(PersonMap.Key.LASTNAME);
		keys.add(LinkMap.Key.MEMBER);
		return keys.toArray(new DataMapKey[0]);
	}

	private DataMapKey[] getKeys()
	{
		List<DataMapKey> keys = new ArrayList<DataMapKey>();
		if (shortList.getSelection())
		{
			keys.add(LinkMap.Key.FUNCTION);
			keys.add(PersonMap.Key.LASTNAME);
			keys.add(PersonMap.Key.FIRSTNAME);
			keys.add(AddressMap.Key.ADDRESS);
			keys.add(AddressMap.Key.POB);
			keys.add(AddressMap.Key.COUNTRY);
			keys.add(AddressMap.Key.ZIP);
			keys.add(AddressMap.Key.CITY);
			keys.add(AddressMap.Key.COUNTY);
			keys.add(LinkMap.Key.PHONE);
			keys.add(AddressMap.Key.PHONE);
			keys.add(PersonMap.Key.PHONE);
			keys.add(PersonMap.Key.EMAIL);
			keys.add(LinkMap.Key.EMAIL);
			keys.add(AddressMap.Key.EMAIL);
			if (courseVisits.getSelection())
			{
				keys.add(LinkMap.Key.COURSE_VISITS);
			}
		}
		else
		{
//			keys.add(AddressGroupMemberMap.Key.TYPE);
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
			keys.add(LinkMap.Key.EMAIL);
			keys.add(AddressMap.Key.EMAIL);
			keys.add(PersonMap.Key.WEBSITE);
			keys.add(AddressMap.Key.WEBSITE);
			keys.add(AddressMap.Key.ADDRESS);
			keys.add(AddressMap.Key.POB);
			keys.add(AddressMap.Key.COUNTRY);
			keys.add(AddressMap.Key.ZIP);
			keys.add(AddressMap.Key.CITY);
			keys.add(AddressMap.Key.COUNTY);
			keys.add(AddressGroupMemberMap.Key.POLITE);
			keys.add(AddressGroupMap.Key.NAME);
			keys.add(LinkMap.Key.MEMBER);
			keys.add(PersonMap.Key.NOTE);
			keys.add(AddressMap.Key.NOTES);
			keys.addAll(PersonMap.getExtendedFieldKeys());
			keys.addAll(LinkMap.getExtendedFieldKeys());
			if (courseVisits.getSelection())
			{
				keys.add(LinkMap.Key.COURSE_VISITS);
			}
		}
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
		extractAddressGroupMembers();
		final DataMapKey[] keys = getKeys();
		dataMaps = createDataMaps().toArray(new DataMap[0]);
		Arrays.sort(dataMaps, new Comparator<DataMap<?>>()
		{
			@Override
			public int compare(DataMap<?> map1, DataMap<?> map2)
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
							if (value1 == null)
							{
								value1 = "";
							}
							String value2 = map2.getProperty(key.getKey());
							if (value2 == null)
							{
								value2 = "";
							}
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
				return AddressListDialog.this.buildDocument(monitor, keys, dataMaps);
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
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				UserQuery query = (UserQuery) service.getQuery(User.class);
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

	public void setTitle()
	{
		super.setTitle("Adressliste generieren");
	}

	private class AddressGroupLine
	{
		private AddressGroup addressGroup;
		private Button add;
		private Button remove;
		private Label name;
		
		public AddressGroupLine(int number, Composite parent, AddressGroup addressGroup)
		{
			this.addressGroup = addressGroup;
			if (number == 0)
			{
				Label label = new Label(parent, SWT.None);
				label.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
				label.setText("+");
				label = new Label(parent, SWT.None);
				label.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false));
				label.setText("-");
				label = new Label(parent, SWT.None);
				label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				label.setText("Adressgruppe");
			}
			this.add = new Button(parent, SWT.RADIO);
			this.add.setLayoutData(new GridData());
			this.add.setSelection(true);
			this.remove = new Button(parent, SWT.RADIO);
			this.remove.setLayoutData(new GridData());
			this.remove.setSelection(false);
			this.name = new Label(parent, SWT.None);
			this.name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			this.name.setText((addressGroup.getCode().isEmpty() ? addressGroup.getName() : (addressGroup.getCode() + (addressGroup.getName().isEmpty() ? "" : " - " + addressGroup.getName()))));
		}
		
		public AddressGroup getAddressGroup()
		{
			return this.addressGroup;
		}
		
		public boolean doAdd()
		{
			return add.getSelection();
		}
		
		public boolean doRemove()
		{
			return remove.getSelection();
		}
	}
}
