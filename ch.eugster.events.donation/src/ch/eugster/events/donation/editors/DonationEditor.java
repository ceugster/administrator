package ch.eugster.events.donation.editors;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.donation.Activator;
import ch.eugster.events.donation.views.DonationPurposeContentProvider;
import ch.eugster.events.donation.views.DonationPurposeLabelProvider;
import ch.eugster.events.donation.views.DonationPurposeSorter;
import ch.eugster.events.persistence.exceptions.PersistenceException.ErrorCode;
import ch.eugster.events.persistence.formatters.DonationFormatter;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class DonationEditor extends AbstractEntityEditor<Donation>
{
	public static final String ID = "ch.eugster.events.donation.editor";

	private Section section;

	private CDateTime date;

	private FormattedText amount;

	private ComboViewer purposeViewer;

	private ComboViewer domainViewer;

	private void createSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.section.setLayoutData(layoutData);
		this.section.setLayout(sectionLayout);
		this.section.setText("Spendedaten");
		this.section.setClient(this.fillSection(this.section));
		this.section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				DonationEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	@Override
	protected void createSections(final ScrolledForm parent)
	{
		this.createSection(parent);
	}

	private Control fillSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Datum", SWT.NONE);
		label.setLayoutData(new GridData());

		this.date = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		this.date.setPattern("dd.MM.yyyy");
		this.date.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.date.setLayoutData(new GridData());
		this.date.setNullText("");
		this.date.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				DonationEditor.this.setDirty(true);
			}
		});
		this.formToolkit.adapt(this.date);

		label = this.formToolkit.createLabel(composite, "Betrag", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData();
		gridData.widthHint = 72;

		Text amount = this.formToolkit.createText(composite, "", SWT.RIGHT);
		amount.setLayoutData(gridData);
		amount.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DonationEditor.this.setDirty(true);
			}
		});

		this.amount = new FormattedText(amount);
		this.amount.setFormatter(new NumberFormatter("#,###,###.00"));
		this.amount.getControl().addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(FocusEvent e)
			{
				DonationEditor.this.amount.getControl().setSelection(0,
						DonationEditor.this.amount.getControl().getText().length());
			}
		});

		label = this.formToolkit.createLabel(composite, "Zweck");
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.FLAT | SWT.READ_ONLY);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DonationEditor.this.setDirty(true);
			}

		});
		this.formToolkit.adapt(combo);

		this.purposeViewer = new ComboViewer(combo);
		this.purposeViewer.setContentProvider(new DonationPurposeContentProvider());
		this.purposeViewer.setLabelProvider(new DonationPurposeLabelProvider());
		this.purposeViewer.setSorter(new DonationPurposeSorter());

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		this.purposeViewer.setInput(service);
		this.purposeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				DonationEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Domäne");
		label.setLayoutData(new GridData());

		combo = new CCombo(composite, SWT.FLAT | SWT.READ_ONLY);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				DonationEditor.this.setDirty(true);
			}

		});
		this.formToolkit.adapt(combo);

		this.domainViewer = new ComboViewer(combo);
		this.domainViewer.setContentProvider(new DonationDomainContentProvider());
		this.domainViewer.setLabelProvider(new DonationDomainLabelProvider());
		this.domainViewer.setSorter(new DonationDomainSorter());
		this.domainViewer.setInput(service);
		tracker.close();

		this.purposeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				DonationEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	protected Message getMessage()
	{
		if (this.date.getSelection() == null)
		{
			return new Message(date);
		}

		if (((Number) this.amount.getValue()).doubleValue() == 0)
		{
			return new Message(amount.getControl());
		}

		if (this.purposeViewer.getSelection().isEmpty())
		{
			return new Message(this.purposeViewer.getCCombo());
		}
		return null;
	}

	@Override
	protected Message getMessage(final ErrorCode errorCode)
	{
		return null;
	}

	@Override
	protected String getName()
	{
		DonationEditorInput input = (DonationEditorInput) this.getEditorInput();
		Calendar calendar = input.getEntity().getDonationDate();
		return calendar == null ? "Neue Spende" : "Spende: " + DateFormat.getDateInstance().format(calendar.getTime());
	}

	@Override
	protected String getText()
	{
		DonationEditorInput input = (DonationEditorInput) this.getEditorInput();
		return DonationFormatter.getInstance().formatDonatorName(input.getEntity());
	}

	@Override
	protected void initialize()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void loadValues()
	{
		DonationEditorInput input = (DonationEditorInput) this.getEditorInput();
		Donation donation = input.getEntity();

		if (donation.getDonationDate() != null)
		{
			this.date.setSelection(donation.getDonationDate().getTime());
		}
		this.amount.setValue(Double.valueOf(donation.getAmount()));

		if (donation.getPurpose() == null)
		{
			if (this.purposeViewer.getElementAt(0) != null)
			{
				this.purposeViewer.setSelection((new StructuredSelection(new Object[] { this.purposeViewer
						.getElementAt(0) })));
			}
		}
		else
		{
			this.purposeViewer.setSelection((new StructuredSelection(new DonationPurpose[] { donation.getPurpose() })));
		}

		Domain domain = null;
		if (donation.getId() == null)
		{
			if (donation.getLink() != null)
			{
				domain = donation.getLink().getPerson().getDomain();
			}
			if (domain == null)
			{
				domain = User.getCurrent().getDomain();
			}
			if (domain == null)
			{
				domain = (Domain) domainViewer.getElementAt(0);
			}
			if (domain != null)
			{
				StructuredSelection ssel = new StructuredSelection(new Domain[] { domain });
				domainViewer.setSelection(ssel);
			}

		}
		else
		{
			domain = donation.getDomain();
			if (domain == null)
			{
				domain = (Domain) domainViewer.getElementAt(0);
			}
			if (domain != null)
			{
				StructuredSelection ssel = new StructuredSelection(new Domain[] { domain });
				domainViewer.setSelection(ssel);
			}
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		DonationEditorInput input = (DonationEditorInput) this.getEditorInput();
		Donation donation = input.getEntity();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(this.date.getSelection());
		donation.setDonationDate(calendar);
		donation.setYear(calendar.get(Calendar.YEAR));

		double value = ((Number) amount.getValue()).doubleValue();
		donation.setAmount(value);

		StructuredSelection ssel = (StructuredSelection) this.purposeViewer.getSelection();
		DonationPurpose purpose = (DonationPurpose) ssel.getFirstElement();
		donation.setPurpose(purpose);

		ssel = (StructuredSelection) this.domainViewer.getSelection();
		Domain domain = ssel.isEmpty() ? Domain.newInstance() : (Domain) ssel.getFirstElement();
		donation.setDomain((domain == null || domain.getId() == null) ? null : domain);
	}

	@Override
	public void setFocus()
	{
		this.date.setFocus();
	}

	@Override
	protected boolean validate()
	{
		if (this.date.getSelection() == null)
		{
			MessageDialog.openError(this.getSite().getShell(), "Kein Datum", "Sie haben kein Spendendatum erfasst.");
			return false;
		}

		DonationEditorInput input = (DonationEditorInput) this.getEditorInput();
		Donation donation = input.getEntity();
		if (donation.getId() == null)
		{
			Calendar before = GregorianCalendar.getInstance();
			before.set(Calendar.YEAR, before.get(Calendar.YEAR) - 5);
			if (this.date.getSelection().before(before.getTime())
					|| this.date.getSelection().after(GregorianCalendar.getInstance().getTime()))
			{
				MessageDialog
						.openError(this.getSite().getShell(), "Ungültiges Datum", "Das Spendendatum ist ungültig.");
				return false;
			}
		}

		if (((Number) this.amount.getValue()).doubleValue() == 0)
		{
			MessageDialog.openError(this.getSite().getShell(), "Fehlender Betrag", "Sie haben keinen Betrag erfasst.");
			return false;
		}

		if (this.purposeViewer.getSelection().isEmpty())
		{
			MessageDialog.openError(this.getSite().getShell(), "Fehlender Spendenzweck",
					"Sie haben keinen Spendenzweck ausgewählt.");
			return false;
		}
		return true;
	}

	@Override
	protected boolean validateType(final AbstractEntityEditorInput<Donation> input)
	{
		return input.getEntity() instanceof Donation;
	}

}
