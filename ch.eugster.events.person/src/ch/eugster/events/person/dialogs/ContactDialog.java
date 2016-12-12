package ch.eugster.events.person.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressContact;
import ch.eugster.events.persistence.model.Contact;
import ch.eugster.events.persistence.model.ContactType;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.LinkPersonAddressContact;
import ch.eugster.events.persistence.model.PersonContact;
import ch.eugster.events.persistence.queries.ContactQuery;
import ch.eugster.events.persistence.queries.ContactTypeQuery;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;

public class ContactDialog extends TitleAreaDialog implements ISelectionChangedListener
{
	private final ContactType[] contactTypes;

	private Contact contact;

	private ComboViewer contactTypeViewer;

	private Text name;
	
	private ComboViewer countryViewer;

	private Label valueLabel;
	
	private Text value;

	private Composite composite;
	
	private IDialogSettings settings;
	
	private static String msg = "Erfassen oder bearbeiten Sie einen Kontakt";

	public ContactDialog(Shell shell, Contact contact)
	{
		super(shell);
		this.contact = contact;
		this.contactTypes = this.getContactTypes();
		this.settings = Activator.getDefault().getDialogSettings().getSection("contact.dialog");
		if (this.settings == null)
		{
			this.settings = Activator.getDefault().getDialogSettings().addNewSection("contact.dialog");
		}
		try
		{
			this.settings.getInt("selected.contact.type");
		}
		catch (NumberFormatException e)
		{
			this.settings.put("selected.contact.type", Long.valueOf(this.contactTypes[0].getId()).intValue());
		}
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		String owner = "";
		if (this.contact instanceof PersonContact)
		{
			owner = PersonFormatter.getInstance().formatLastnameFirstname(((PersonContact) this.contact).getPerson());
		}
		if (this.contact instanceof LinkPersonAddressContact)
		{
			owner = LinkPersonAddressFormatter.getInstance().formatPersonAndAddress(((LinkPersonAddressContact) this.contact).getLink());
		}
		if (this.contact instanceof AddressContact)
		{
			Address address = ((AddressContact) this.contact).getAddress();
			owner = address.getName() + ", " + AddressFormatter.getInstance().formatAddressLine(address) + ", " + AddressFormatter.getInstance().formatCityLine(address);
		}
		this.setTitle("Kontakt für " + owner);
		this.setMessage(msg);

		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(2, false));

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Kontaktart");

		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		contactTypeViewer = new ComboViewer(combo);
		contactTypeViewer.setContentProvider(new ArrayContentProvider());
		contactTypeViewer.setLabelProvider(new LabelProvider() 
		{
			@Override
			public String getText(Object element) 
			{
				return ((ContactType) element).getName();
			}
		});
		contactTypeViewer.setInput(this.contactTypes);
		contactTypeViewer.addSelectionChangedListener(this);

		label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Bezeichnung");

		name = new Text(composite, SWT.BORDER);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Ländervorwahl");

		combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		countryViewer = new ComboViewer(combo);
		countryViewer.setContentProvider(new ArrayContentProvider());
		countryViewer.setLabelProvider(new LabelProvider()
		{

			@Override
			public Image getImage(Object element)
			{
				return super.getImage(element);
			}

			@Override
			public String getText(Object element)
			{
				if (element instanceof Country)
				{
					Country country = (Country) element;
					return country.getName() + " (" + country.getPhonePrefix() + ")";
				}
				return "";
			}
		});
		countryViewer.setSorter(new ViewerSorter()
		{
			@Override
			public int compare(Viewer viewer, Object e1, Object e2)
			{
				if (e1 instanceof Country && e2 instanceof Country)
				{
					Country c1 = (Country) e1;
					Country c2 = (Country) e2;
					return c1.getPhonePrefix().compareTo(c2.getPhonePrefix());
				}
				return super.compare(viewer, e1, e2);
			}
		});
		countryViewer.setInput(getAvailableCountries());
		countryViewer.addSelectionChangedListener(this);
		
		valueLabel = new Label(composite, SWT.None);
		valueLabel.setLayoutData(new GridData());
		IStructuredSelection ssel = (IStructuredSelection) contactTypeViewer.getSelection();
		valueLabel.setText(ssel.isEmpty() ? "Wert       " : ((ContactType) ssel.getFirstElement()).getName());

		value = new Text(composite, SWT.BORDER);
		value.setText(this.contact.getValue());
		value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/*
		 * initialize values
		 */
		ssel = new StructuredSelection();
		if (this.contact.getType() == null)
		{
			if (this.contactTypes.length > 0)
			{
				ssel = new StructuredSelection(new ContactType[] { contactTypes[0] });
			}
		}
		else
		{
			ssel = new StructuredSelection(new ContactType[] { this.contact.getType() });
		}
		this.contactTypeViewer.setSelection(ssel);

		
		return composite;
	}

	@Override
	protected void okPressed() 
	{
		IStructuredSelection ssel = (IStructuredSelection) contactTypeViewer.getSelection();
		ContactType contactType = (ContactType) ssel.getFirstElement();
		if (contactType.getProtocol().useCountry())
		{
			if (countryViewer.getSelection().isEmpty())
			{
				this.setErrorMessage("Sie haben keine Vorwahl ausgewählt.");
				return;
			}
		}

		ssel = (IStructuredSelection) this.contactTypeViewer.getSelection();
		this.contact.setType(ssel.isEmpty() ? (ContactType) null : (ContactType) ssel.getFirstElement());
		ssel = (IStructuredSelection) this.countryViewer.getSelection();
		this.contact.setCountry(ssel.isEmpty() ? (Country) null : (Country) ssel.getFirstElement());
		this.contact.setName(this.name.getText());
		this.contact.setValue(this.value.getText());

		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService,ConnectionService>(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = tracker.getService();
			if (service != null)
			{
				ContactQuery query = (ContactQuery) service.getQuery(Contact.class);
				this.contact = query.merge(this.contact);
			}
		}
		finally
		{
			tracker.close();
		}
		super.okPressed();
	}

	private Country[] getAvailableCountries()
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service == null)
			{
				return new Country[0];
			}
			CountryQuery query = (CountryQuery) service.getQuery(Country.class);
			return query.selectVisibles().toArray(new Country[0]);
		}
		finally
		{
			tracker.close();
		}
	}

	private ContactType[] getContactTypes()
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service == null)
			{
				return new ContactType[0];
			}
			ContactTypeQuery query = (ContactTypeQuery) service.getQuery(ContactType.class);
			return query.selectContactTypes().toArray(new ContactType[0]);
		}
		finally
		{
			tracker.close();
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
		okButton.setEnabled(isPageComplete());
	}

	private boolean isPageComplete()
	{
		boolean complete = false;
		IStructuredSelection ssel = (IStructuredSelection) contactTypeViewer.getSelection();
		complete = !ssel.isEmpty();
		if (complete)
		{
			ContactType contactType = (ContactType) ssel.getFirstElement();
			if (contactType.getProtocol().useCountry())
			{
				ssel = (IStructuredSelection) countryViewer.getSelection();
				complete = !ssel.isEmpty();
			}		
		}
		return complete;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event)
	{
		if (event.getSource().equals(contactTypeViewer))
		{
			IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
			if (ssel.isEmpty())
			{
				ContactDialog.this.name.setText("");
				ContactDialog.this.valueLabel.setText("Wert");
				ContactDialog.this.value.setText("");
				ContactDialog.this.countryViewer.getCombo().setVisible(false);
				ContactDialog.this.setErrorMessage("Sie haben keine Kontaktart ausgewählt.");
			}
			else
			{
				ContactType contactType = (ContactType) ssel.getFirstElement();
				ContactDialog.this.name.setText("");
				if (ContactDialog.this.name.getText().isEmpty())
				{
					ContactDialog.this.name.setText(this.contact.getName());
				}
				ContactDialog.this.valueLabel.setText(contactType.getProtocol().label());
				if (ContactDialog.this.value.getText().isEmpty())
				{
					ContactDialog.this.value.setText(this.contact.getValue());
				}
				ContactDialog.this.countryViewer.getCombo().setVisible(contactType.getProtocol().useCountry());
				if (contactType.getProtocol().useCountry())
				{
					if (ContactDialog.this.countryViewer.getSelection().isEmpty())
					{
						Country country = this.contact.getCountry();
						ssel = country == null ? new StructuredSelection() : new StructuredSelection( new Country[] { country });
						ContactDialog.this.countryViewer.setSelection(ssel);
					}
					if (this.countryViewer.getSelection().isEmpty())
					{
						ContactDialog.this.setErrorMessage("Sie haben keine Vorwahl ausgewählt.");
						ContactDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(false);
						return;
					}
				}
				ContactDialog.this.composite.layout();
				ContactDialog.this.setMessage(msg);
			}
		}
		else if (event.getSource().equals(countryViewer))
		{
			IStructuredSelection ssel = (IStructuredSelection) contactTypeViewer.getSelection();
			ContactType contactType = (ContactType) ssel.getFirstElement();
			if (contactType.getProtocol().useCountry())
			{
				if (ContactDialog.this.countryViewer.getSelection().isEmpty())
				{
					Country country = this.contact.getCountry();
					ssel = country == null ? new StructuredSelection() : new StructuredSelection( new Country[] { country });
					ContactDialog.this.countryViewer.setSelection(ssel);
				}
				if (this.countryViewer.getSelection().isEmpty())
				{
					ContactDialog.this.setErrorMessage("Sie haben keine Vorwahl ausgewählt.");
					ContactDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(false);
					return;
				}
			}
			ContactDialog.this.setMessage(msg);
		}
		if (ContactDialog.this.getButton(IDialogConstants.OK_ID) != null)
		{
			ContactDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(isPageComplete());
		}
	}

	public Contact getContact()
	{
		return this.contact;
	}
}
