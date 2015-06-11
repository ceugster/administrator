package ch.eugster.events.importer.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.importer.Activator;
import ch.eugster.events.importer.wizards.MappingNames;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSex;
import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.persistence.queries.PersonSexQuery;
import ch.eugster.events.persistence.queries.PersonTitleQuery;
import ch.eugster.events.persistence.queries.ZipCodeQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class SelectPersonDialog extends TitleAreaDialog implements ISelectionChangedListener, ICheckStateListener
{
	private IDialogSettings settings;
	
	private Map<MappingNames, String> importValues;
	
	private Membership membership;
	
	private Map<String, String> criteria = new HashMap<String, String>();
	
	private Label label;
	
	private TableViewer viewer;

	private final String message = "Zuordnen der Mitgliedschaft zu einer Person.";

	private boolean isPageComplete = false;

	private ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
			ConnectionService.class.getName(), null);

	private ConnectionService connectionService;
	
	public SelectPersonDialog(final Shell parentShell, Membership membership, Map<MappingNames, String> importValues)
	{
		super(parentShell);
		this.setShellStyle(SWT.SHELL_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.APPLICATION_MODAL);
		this.importValues = importValues;
		this.membership = membership;

		tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class.getName(), null);
		tracker.open();
		connectionService = (ConnectionService) tracker.getService();
		
		settings = Activator.getDefault().getDialogSettings().getSection("import.wizard.search.select");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("import.wizard.search.select");
		}
	}

	@Override
	public boolean close() 
	{
		tracker.close();
		return super.close();
	}

	@Override
	public void checkStateChanged(final CheckStateChangedEvent event)
	{
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, "Neu", false);
		final Button button = this.createButton(parent, IDialogConstants.YES_ID, "Zuordnen", false);
		button.setEnabled(false);
		this.createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
		this.createButton(parent, IDialogConstants.ABORT_ID, "Beenden", false);

		this.viewer.addSelectionChangedListener(new ISelectionChangedListener() 
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				button.setEnabled(!viewer.getSelection().isEmpty());
			}
		});
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		String id = importValues.get(MappingNames.EXTERNAL_ID);
		String salutation = importValues.get(MappingNames.SALUTATION);
		String lastname = importValues.get(MappingNames.LASTNAME);
		String firstname = importValues.get(MappingNames.FIRSTNAME);
		String address = importValues.get(MappingNames.ADDRESS);
		String country = importValues.get(MappingNames.COUNTRY);
		String zip = importValues.get(MappingNames.ZIP);
		String city = importValues.get(MappingNames.CITY);
		String phone = importValues.get(MappingNames.PHONE_FIX);
		String mobile = importValues.get(MappingNames.PHONE_MOBILE);
		StringBuilder builder = new StringBuilder(id);
		builder = builder.append(salutation.isEmpty() ? "" : " " + salutation);
		builder = builder.append(firstname.isEmpty() ? "" : " " + firstname);
		builder = builder.append(lastname.isEmpty() ? "" : " " + lastname);
		builder = builder.append(builder.length() == 0 ? "" : ", ");
		builder = builder.append(address.isEmpty() ? "" : address + ",");
		builder = builder.append(country.isEmpty() ? "" : " " + country);
		builder = builder.append(country.isEmpty() ? "" : "-" + zip + " " + city + ", ");
		builder = builder.append(phone.isEmpty() ? "" : " " + phone);
		builder = builder.append(mobile.isEmpty() ? "" : " " + mobile);
		label = new Label(composite, SWT.BORDER);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label.setText(builder.toString());

		final Table table = new Table(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(final Event event)
			{
				if (event.detail == SWT.CHECK)
				{
					if (!(event.item.getData() instanceof Person))
					{
						event.detail = SWT.NONE;
						event.type = SWT.None;
						event.doit = false;
						try
						{
							table.setRedraw(false);
							TreeItem item = (TreeItem) event.item;
							item.setChecked(!item.getChecked());
						}
						finally
						{
							table.setRedraw(true);
						}
					}
				}
			}
		});


		this.viewer = new TableViewer(table);
		this.viewer.setContentProvider(new ArrayContentProvider());

		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Id");
		viewerColumn.getColumn().setData("key", "id");
		viewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell viewerCell)
			{
				if (viewerCell.getElement() instanceof Person)
				{
					Person person = (Person) viewerCell.getElement();
					viewerCell.setText(person.getId().toString());
				}
			}
		});
		viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Geschlecht");
		viewerColumn.getColumn().setData("key", "sex");
		viewerColumn.getColumn().addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				boolean selected = !settings.getBoolean("sex");
				settings.put("sex", selected);
				selectData();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});
		viewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell viewerCell)
			{
				if (viewerCell.getElement() instanceof Person)
				{
					Person person = (Person) viewerCell.getElement();
					viewerCell.setText(person.getSex().getSalutation());
				}
			}
		});
		viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Anrede");
		viewerColumn.getColumn().setData("key", "salutation");
		viewerColumn.getColumn().addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				boolean selected = !settings.getBoolean("salutation");
				settings.put("salutation", selected);
				selectData();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});
		viewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell viewerCell)
			{
				if (viewerCell.getElement() instanceof Person)
				{
					Person person = (Person) viewerCell.getElement();
					viewerCell.setText(person.getSex().getSalutation());
				}
			}
		});
		viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Vorname");
		viewerColumn.getColumn().setData("key", "firstname");
		viewerColumn.getColumn().addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				boolean selected = !settings.getBoolean("firstname");
				settings.put("firstname", selected);
				selectData();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});
		viewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell viewerCell)
			{
				if (viewerCell.getElement() instanceof Person)
				{
					Person person = (Person) viewerCell.getElement();
					viewerCell.setText(person.getFirstname());
				}
			}
		});
		viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Nachname");
		viewerColumn.getColumn().setData("key", "lastname");
		viewerColumn.getColumn().addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				boolean selected = !settings.getBoolean("lastname");
				settings.put("lastname", selected);
				selectData();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});
		viewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell viewerCell)
			{
				if (viewerCell.getElement() instanceof Person)
				{
					Person person = (Person) viewerCell.getElement();
					viewerCell.setText(person.getLastname());
				}
			}
		});
		viewerColumn = new TableViewerColumn(viewer, SWT.LEFT);
		viewerColumn.getColumn().setText("Adresse");
		viewerColumn.getColumn().setData("key", "address");
		viewerColumn.getColumn().addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				boolean selected = !settings.getBoolean("address");
				settings.put("address", selected);
				selectData();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});
		viewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell viewerCell)
			{
				if (viewerCell.getElement() instanceof Person)
				{
					Person person = (Person) viewerCell.getElement();
					viewerCell.setText(person.getDefaultLink().getAddress().getAddress());
				}
			}
		});

		selectData();
		
		return parent;
	}
	
	private void selectData()
	{
		if (connectionService != null)
		{
			criteria.clear();
			if (settings.getBoolean("lastname"))
			{
				String value = importValues.get(MappingNames.LASTNAME);
				criteria.put("lastname", "%" + value + "%");
			}
			if (settings.getBoolean("firstname"))
			{
				String value = importValues.get(MappingNames.FIRSTNAME);
				criteria.put("firstname", "%" + value + "%");
			}
			if (settings.getBoolean("address"))
			{
				String value = importValues.get(MappingNames.ADDRESS);
				criteria.put("address", "%" + value + "%");
			}
//			if (!settings.getBoolean("city"))
//			{
//				String value = importValues.get(MappingNames.CITY);
//				criteria.put("city", "%" + value + "%");
//			}
			List<Person> nonMembers = new ArrayList<Person>();
			PersonQuery personQuery = (PersonQuery) connectionService.getQuery(Person.class);
			List<Person> persons = personQuery.selectByCriteria(criteria, 150);
			for (Person person : persons)
			{
				if (!person.isMember(membership))
				{
					nonMembers.add(person);
				}
			}
			this.viewer.setInput(nonMembers.toArray(new Person[0]));

			TableColumn[] columns = this.viewer.getTable().getColumns();
			for (int i = 0; i < columns.length; i++)
			{
				TableColumn column = this.viewer.getTable().getColumn(i);
				boolean selected = settings.getBoolean(column.getData("key").toString());
				column.setImage(selected ? Activator.getDefault().getImageRegistry().get("selected") : null);

				TableItem[] items = viewer.getTable().getItems();
				Display display = viewer.getTable().getDisplay();
				for (TableItem item : items)
				{
					item.setBackground(i, selected ? display.getSystemColor(SWT.COLOR_INFO_BACKGROUND) : display.getSystemColor(SWT.COLOR_WHITE));
				}
				column.pack();
			}
		}
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void buttonPressed(int buttonId) 
	{
		if (buttonId == IDialogConstants.ABORT_ID || buttonId == IDialogConstants.CANCEL_ID)
		{
			this.setReturnCode(buttonId);
			close();
		}
		else if (buttonId == IDialogConstants.OK_ID)
		{
			insert();
			this.setReturnCode(buttonId);
			close();
		}
		else if (buttonId == IDialogConstants.YES_ID)
		{
			IStructuredSelection ssel = (IStructuredSelection) this.viewer.getSelection();
			update((Person)ssel.getFirstElement());
			this.setReturnCode(buttonId);
			close();
		}
	}

	@Override
	protected void okPressed()
	{
		super.okPressed();
	}

	@Override
	public void selectionChanged(final SelectionChangedEvent event)
	{
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
		super.setTitle("Zuordnungen zu Adressgruppen");
		int width = this.getShell().getDisplay().getClientArea().width;
		int height = this.getShell().getDisplay().getClientArea().height;
		super.getShell().setMinimumSize(width > 500 ? 500 : width, height > 500 ? 500 : height);
	}

	private void insert()
	{
		Person person = Person.newInstance();
		person.setFirstname(importValues.get(MappingNames.FIRSTNAME));
		person.setLastname(importValues.get(MappingNames.LASTNAME));
		String value = importValues.get(MappingNames.SEX);
		PersonSexQuery personSexQuery = (PersonSexQuery) connectionService.getQuery(PersonSex.class);
		List<PersonSex> sexes = personSexQuery.selectBySymbol(value.equals("f") ? "w" : value);
		if (sexes.size() > 0) person.setSex(sexes.get(0));
		value = importValues.get(MappingNames.TITLE);
		if (!value.isEmpty())
		{
			PersonTitleQuery personTitleQuery = (PersonTitleQuery) connectionService.getQuery(PersonTitle.class);
			List<PersonTitle> titles = personTitleQuery.selectByTitle(value);
			if (titles.size() > 0) person.setTitle(titles.get(0));
		}
		person.setPhone(importValues.get(MappingNames.PHONE_MOBILE));
		
		Address address = Address.newInstance();
		address.setAnotherLine(importValues.get(MappingNames.ADDITIONAL_LINE));
		address.setAddress(importValues.get(MappingNames.ADDRESS));
		address.setPob(importValues.get(MappingNames.POB));
		value = importValues.get(MappingNames.COUNTRY);
		if (value.isEmpty())
		{
			address.setCountry(GlobalSettings.getInstance().getCountry());
		}
		else
		{
			CountryQuery query = (CountryQuery) connectionService.getQuery(Country.class);
			List<Country> countries = query.selectByIso3166alpha2Code(value);
			if (countries.isEmpty())
			{
				address.setCountry(GlobalSettings.getInstance().getCountry());
			}
			else
			{
				address.setCountry(countries.get(0));
			}
		}
		value = importValues.get(MappingNames.ZIP);
		ZipCodeQuery zipCodeQuery = (ZipCodeQuery) connectionService.getQuery(ZipCode.class);
		List<ZipCode> zipCodes = zipCodeQuery.selectByCountryAndZipCode(address.getCountry(), value);
		if (!zipCodes.isEmpty())
		{
			address.setZipCode(zipCodes.get(0));
		}

		LinkPersonAddress link = LinkPersonAddress.newInstance(person, address);
		link.setPhone(importValues.get(MappingNames.PHONE_FIX));

		AddressTypeQuery addressTypeQuery = (AddressTypeQuery) connectionService.getQuery(AddressType.class);
		List<AddressType> addressTypes = addressTypeQuery.selectAll();
		link.setAddressType(addressTypes.get(0));

		Member member = Member.newInstance(this.membership, link);
		member.setCode(importValues.get(MappingNames.EXTERNAL_ID));

		link.addMember(member);
		
		LinkPersonAddressQuery linkQuery = (LinkPersonAddressQuery) connectionService.getQuery(LinkPersonAddress.class);
		linkQuery.merge(person.getDefaultLink());
	}
	
	private void update(Person person)
	{
		Member member = Member.newInstance(this.membership, person.getDefaultLink());
		member.setCode(importValues.get(MappingNames.EXTERNAL_ID));
		person.getDefaultLink().addMember(member);
		LinkPersonAddressQuery linkQuery = (LinkPersonAddressQuery) connectionService.getQuery(LinkPersonAddress.class);
		person.setDefaultLink(linkQuery.merge(person.getDefaultLink()));
	}
}
