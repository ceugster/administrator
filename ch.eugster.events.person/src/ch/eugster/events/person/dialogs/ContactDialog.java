package ch.eugster.events.person.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.Contact;
import ch.eugster.events.persistence.model.ContactType;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;

public class ContactDialog extends TitleAreaDialog
{
	private final ContactType[] contactTypes;

	private Contact contact;

	private ComboViewer contactTypeViewer;

	private Composite currentTypeComposite;

	private ComboViewer countryViewer;

	private Text name;

	private FormattedText value;

	public ContactDialog(Shell shell, ContactType[] contactTypes, Contact contact)
	{
		super(shell);
		if (contactTypes == null)
		{
			contactTypes = new ContactType[0];
		}
		this.contactTypes = contactTypes;
		this.contact = contact;
	}

	public ContactDialog(Shell shell, Contact contact)
	{
		super(shell);
		this.contactTypes = new ContactType[] { contact.getType() };
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		Composite top = new Composite(composite, SWT.NONE);
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		top.setLayout(new GridLayout(2, false));

		Label label = new Label(top, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Kontaktart");

		Combo combo = new Combo(top, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		contactTypeViewer = new ComboViewer(combo);
		contactTypeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				if (ssel.getFirstElement() instanceof ContactType)
				{
					initializeSelectedContact((ContactType) ssel.getFirstElement(), composite);
				}
			}
		});

		StructuredSelection ssel = new StructuredSelection();
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

	private void initializeSelectedContact(ContactType contactType, Composite parent)
	{
		if (currentTypeComposite != null)
		{
			currentTypeComposite.dispose();
		}

		currentTypeComposite = new Composite(parent, SWT.NONE);
		currentTypeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		currentTypeComposite.setLayout(new GridLayout(2, false));

		Label label = new Label(currentTypeComposite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Bezeichnung");

		name = new Text(currentTypeComposite, SWT.BORDER);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (contactType.getProtocol().useCountry())
		{
			label = new Label(currentTypeComposite, SWT.None);
			label.setLayoutData(new GridData());
			label.setText("Ländervorwahl");

			Combo combo = new Combo(currentTypeComposite, SWT.READ_ONLY | SWT.DROP_DOWN);
			combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			ComboViewer countryViewer = new ComboViewer(combo);
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
		}

		label = new Label(currentTypeComposite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Adresse");

		Text text = new Text(currentTypeComposite, SWT.BORDER);
		value = new FormattedText(text);
		// value.setFormatter(new DefaultFormatter() {});
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	}

	private Country[] getAvailableCountries()
	{
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service == null)
		{
			return new Country[0];
		}
		CountryQuery query = (CountryQuery) service.getQuery(Country.class);
		return query.selectVisibles().toArray(new Country[0]);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
	}

}
