package ch.eugster.events.course.wizards;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.model.BookingType;
import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.persistence.queries.MembershipQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class BookingTypeWizardPage extends WizardPage implements Listener, SelectionListener
{

	private Text codeText;

	private Text nameText;

	private ComboViewer membershipViewer;

	private Spinner maxAge;

	private Text priceText;

	private Text annulationChargesText;

	private NumberFormat nf;

	public BookingTypeWizardPage(final String pageName)
	{
		super(pageName);
	}

	@Override
	public void createControl(final Composite parent)
	{
		this.nf = DecimalFormat.getInstance();
		this.nf.setMaximumFractionDigits(DecimalFormat.getCurrencyInstance().getMaximumFractionDigits());
		this.nf.setMinimumFractionDigits(DecimalFormat.getCurrencyInstance().getMinimumFractionDigits());

		ImageDescriptor image = Activator.getDefault().getImageRegistry().getDescriptor("NEW_WIZARD");
		this.setImageDescriptor(image);

		BookingTypeWizard wizard = (BookingTypeWizard) this.getWizard();
		String course = CourseFormatter.getInstance().formatComboEntry(wizard.getBookingType().getCourse());
		if (wizard.getBookingType().getId() == null)
		{
			this.setTitle("Buchungsart hinzufügen");
			this.setMessage("Hinzufügen einer Buchungsart zu '" + course + "'.");
		}
		else
		{
			this.setTitle("Buchungsart bearbeiten");
			this.setMessage("Bearbeiten einer Buchungsart zu '" + course + "'.");
		}

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.makeColumnsEqualWidth = false;

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(gridLayout);

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Code");

		GridData gridData = new GridData();
		gridData.widthHint = 100;

		this.codeText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		this.codeText.setLayoutData(gridData);

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Bezeichnung");

		this.nameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		this.nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Bundle membershipBundle = Platform.getBundle("ch.eugster.events.member");
		if (membershipBundle != null)
		{
			label = new Label(composite, SWT.None);
			label.setLayoutData(new GridData());
			label.setText("Mitgliedschaft");

			Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
			combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			membershipViewer = new ComboViewer(combo);
			membershipViewer.setContentProvider(new ArrayContentProvider());
			membershipViewer.setLabelProvider(new MembershipLabelProvider());
			membershipViewer.setSorter(new MembershipSorter());

			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			tracker.open();
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				MembershipQuery query = (MembershipQuery) service.getQuery(Membership.class);
				List<Membership> memberships = query.selectAll();
				memberships.add(Membership.newInstance());
				membershipViewer.setInput(memberships.toArray(new Membership[0]));
			}
			tracker.close();
		}

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Alter bis maximal");

		this.maxAge = new Spinner(composite, SWT.BORDER);
		this.maxAge.setValues(0, 0, 120, 0, 1, 10);
		this.maxAge.setLayoutData(new GridData());

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Kurskosten");

		gridData = new GridData();
		gridData.widthHint = 100;

		this.priceText = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		this.priceText.setLayoutData(gridData);
		this.priceText.setText("0.00");
		this.priceText.addVerifyListener(new VerifyListener()
		{
			@Override
			public void verifyText(final VerifyEvent event)
			{
				String temp = BookingTypeWizardPage.this.priceText.getText();
				StringBuffer test = new StringBuffer(temp.substring(0, event.start));
				test.append(event.text);
				test.append(temp.substring(event.end, temp.length()));
				try
				{
					new Double(test.toString());
				}
				catch (NumberFormatException e)
				{
					event.doit = false;
				}
			}
		});
		this.priceText.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent event)
			{
				BookingTypeWizardPage.this.priceText.selectAll();
			}

			@Override
			public void focusLost(final FocusEvent event)
			{
				BookingTypeWizardPage.this.priceText.setText(BookingTypeWizardPage.this.nf.format(new Double(
						BookingTypeWizardPage.this.priceText.getText())));
			}
		});

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Annulationskosten");

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.BEGINNING;
		gridData.widthHint = 100;
		gridData.grabExcessHorizontalSpace = false;

		this.annulationChargesText = new Text(composite, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
		this.annulationChargesText.setLayoutData(gridData);
		this.annulationChargesText.setText("0.00");
		this.annulationChargesText.addVerifyListener(new VerifyListener()
		{
			@Override
			public void verifyText(final VerifyEvent event)
			{
				String temp = BookingTypeWizardPage.this.annulationChargesText.getText();
				StringBuffer test = new StringBuffer(temp.substring(0, event.start));
				test.append(event.text);
				test.append(temp.substring(event.end, temp.length()));
				try
				{
					new Double(test.toString());
				}
				catch (NumberFormatException e)
				{
					event.doit = false;
				}
			}
		});
		this.annulationChargesText.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(final FocusEvent event)
			{
				BookingTypeWizardPage.this.annulationChargesText.selectAll();
			}

			@Override
			public void focusLost(final FocusEvent event)
			{
				BookingTypeWizardPage.this.annulationChargesText.setText(BookingTypeWizardPage.this.nf
						.format(new Double(BookingTypeWizardPage.this.annulationChargesText.getText())));
			}
		});

		this.setValues();

		this.setControl(composite);
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}

	private double getAnnulationCharges()
	{
		double charges = 0d;
		try
		{
			charges = new Double(this.annulationChargesText.getText()).doubleValue();
		}
		catch (NumberFormatException e)
		{
		}
		finally
		{
		}
		return charges;
	}

	private double getPrice()
	{
		double price = 0d;
		try
		{
			price = new Double(this.priceText.getText()).doubleValue();
		}
		catch (NumberFormatException e)
		{
		}
		finally
		{
		}
		return price;
	}

	@Override
	public void handleEvent(final Event event)
	{
	}

	@Override
	public boolean isPageComplete()
	{
		return true;
	}

	private void setAnnulationCharges()
	{
		BookingTypeWizard wizard = (BookingTypeWizard) this.getWizard();
		double charges = wizard.getBookingType().getAnnulationCharges();
		this.annulationChargesText.setText(this.nf.format(new Double(charges)));
	}

	private void setCode()
	{
		BookingTypeWizard wizard = (BookingTypeWizard) this.getWizard();
		this.codeText.setText(wizard.getBookingType().getCode());
	}

	private void setMaxAge()
	{
		BookingTypeWizard wizard = (BookingTypeWizard) this.getWizard();
		this.maxAge.setSelection(wizard.getBookingType().getMaxAge());
	}

	private void setMembership()
	{
		BookingTypeWizard wizard = (BookingTypeWizard) this.getWizard();
		Membership membership = wizard.getBookingType().getMembership();
		if (membership != null)
		{
			if (membershipViewer != null)
			{
				membershipViewer.setSelection(new StructuredSelection(new Object[] { membership }));
			}
		}
	}

	private void setName()
	{
		BookingTypeWizard wizard = (BookingTypeWizard) this.getWizard();
		this.nameText.setText(wizard.getBookingType().getName());
	}

	private void setPrice()
	{
		BookingTypeWizard wizard = (BookingTypeWizard) this.getWizard();
		double price = wizard.getBookingType().getPrice();
		this.priceText.setText(this.nf.format(new Double(price)));
	}

	private void setValues()
	{
		this.setCode();
		this.setName();
		this.setMembership();
		this.setMaxAge();
		this.setPrice();
		this.setAnnulationCharges();
	}

	public BookingType updateBookingType(final BookingType bookingType)
	{
		bookingType.setCode(this.codeText.getText());
		bookingType.setName(this.nameText.getText());
		if (membershipViewer != null)
		{
			StructuredSelection ssel = (StructuredSelection) membershipViewer.getSelection();
			if (ssel.getFirstElement() instanceof Membership)
			{
				Membership membership = (Membership) ssel.getFirstElement();
				bookingType.setMembership(membership.getId() == null ? null : membership);
			}
		}
		bookingType.setMaxAge(this.maxAge.getSelection());
		bookingType.setPrice(this.getPrice());
		bookingType.setAnnulationCharges(this.getAnnulationCharges());
		return bookingType;
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event)
	{
		this.widgetSelected(event);
	}

	@Override
	public void widgetSelected(final SelectionEvent event)
	{
	}
}
