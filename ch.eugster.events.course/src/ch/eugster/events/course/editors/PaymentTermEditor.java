package ch.eugster.events.course.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.PaymentTerm;
import ch.eugster.events.persistence.queries.PaymentTermQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class PaymentTermEditor extends AbstractEntityEditor<PaymentTerm>
{
	public static final String ID = "ch.eugster.events.course.paymentterm.editor";

	private Text text;

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(PaymentTerm.class, this);
	}

	@Override
	protected void createSections(ScrolledForm parent)
	{
		this.createSection(parent);
	}

	private void createSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Beschreibung");
		section.setClient(this.fillSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				PaymentTermEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Text", SWT.NONE);
		label.setLayoutData(new GridData());

		this.text = this.formToolkit.createText(composite, "");
		this.text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.text.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				PaymentTermEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	@Override
	protected Message getMessage(PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;
		msg = this.getUniqueCodeMessage();
		return msg;
	}

	private Message getUniqueCodeMessage()
	{
		Message msg = null;

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				PaymentTermEditorInput input = (PaymentTermEditorInput) this.getEditorInput();
				PaymentTerm paymentTerm = (PaymentTerm) input.getAdapter(PaymentTerm.class);
				String code = this.text.getText();
				PaymentTermQuery query = (PaymentTermQuery) service.getQuery(PaymentTerm.class);
				if (!query.isTextUnique(code, paymentTerm.getId()))
				{
					msg = new Message();
					msg.setMessage("Die gewählte Zahlungsbedingung existiert bereits.");
					return msg;
				}
			}
		}
		finally
		{
			tracker.close();
		}

		return msg;
	}

	private Message getEmptyNameMessage()
	{
		Message msg = null;

		if (this.text.getText().isEmpty())
		{
			msg = new Message(this.text, "Fehler");
			msg.setMessage("Die Zahlungsbedingung muss eine Bezeichnung haben.");
		}

		return msg;
	}

	@Override
	protected String getName()
	{
		PaymentTermEditorInput input = (PaymentTermEditorInput) this.getEditorInput();
		PaymentTerm paymentTerm = (PaymentTerm) input.getAdapter(PaymentTerm.class);
		return paymentTerm.getId() == null ? "Neu" : (paymentTerm.getText().length() == 0 ? "???" : paymentTerm
				.getText());
	}

	@Override
	protected String getText()
	{
		PaymentTermEditorInput input = (PaymentTermEditorInput) this.getEditorInput();
		PaymentTerm paymentTerm = (PaymentTerm) input.getAdapter(PaymentTerm.class);
		return paymentTerm.getId() == null ? "Neu" : (paymentTerm.getText().length() == 0 ? "???" : paymentTerm
				.getText());
	}

	@Override
	protected void loadValues()
	{
		PaymentTermEditorInput input = (PaymentTermEditorInput) this.getEditorInput();
		PaymentTerm paymentTerm = (PaymentTerm) input.getAdapter(PaymentTerm.class);

		if (paymentTerm != null)
		{
			this.text.setText(paymentTerm.getText());
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		PaymentTermEditorInput input = (PaymentTermEditorInput) this.getEditorInput();
		PaymentTerm paymentTerm = (PaymentTerm) input.getAdapter(PaymentTerm.class);
		if (paymentTerm != null)
		{
			paymentTerm.setText(this.text.getText());
		}
	}

	@Override
	protected boolean validate()
	{
		Message msg = this.getUniqueCodeMessage();

		if (msg == null)
			msg = this.getEmptyNameMessage();

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<PaymentTerm> input)
	{
		return input.getAdapter(PaymentTerm.class) instanceof PaymentTerm;
	}

	@Override
	public void setFocus()
	{
		this.text.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(PaymentTerm.class, this);
		super.dispose();
	}
}
