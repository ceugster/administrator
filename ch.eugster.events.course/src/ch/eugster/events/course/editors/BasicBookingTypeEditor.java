package ch.eugster.events.course.editors;

import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.progress.UIJob;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.BookingTypeProposition;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class BasicBookingTypeEditor extends AbstractEntityEditor<BookingTypeProposition> implements PropertyChangeListener
{
	public static final String ID = "ch.eugster.events.course.editors.basicBookingTypeEditor";

	private static final String BASIC_BOOKING_TYPE_EDITOR = "basic.BasicBookingType.type.editor";

	private static final String MAIN_SECTION_EXPANDED = "main.section.expanded";

	private static final String PAYMENT_SECTION_EXPANDED = "main.section.expanded";

	private Text code;

	private Text name;

	private FormattedText price;

	private FormattedText annulationCharges;

	private Spinner maxAge;

	private Section mainSection;

	private Section paymentSection;

	private IDialogSettings dialogSettings;

	private void createMainSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.mainSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.mainSection.setLayoutData(layoutData);
		this.mainSection.setLayout(sectionLayout);
		this.mainSection.setText("Buchungsart");
		this.mainSection.setClient(this.fillMainSection(this.mainSection));
		this.mainSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				BasicBookingTypeEditor.this.dialogSettings.put(BasicBookingTypeEditor.MAIN_SECTION_EXPANDED, e.getState());
				BasicBookingTypeEditor.this.scrolledForm.reflow(true);
			}
		});
		this.mainSection.setExpanded(this.dialogSettings.getBoolean(BasicBookingTypeEditor.MAIN_SECTION_EXPANDED));
	}

	private void createPaymentSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.paymentSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.paymentSection.setLayoutData(layoutData);
		this.paymentSection.setLayout(sectionLayout);
		this.paymentSection.setText("Zahlungen");
		this.paymentSection.setClient(this.fillPaymentSection(this.paymentSection));
		this.paymentSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				BasicBookingTypeEditor.this.dialogSettings.put(BasicBookingTypeEditor.PAYMENT_SECTION_EXPANDED, e.getState());
				BasicBookingTypeEditor.this.scrolledForm.reflow(true);
			}
		});
		this.paymentSection.setExpanded(this.dialogSettings.getBoolean(BasicBookingTypeEditor.PAYMENT_SECTION_EXPANDED));
	}

	@Override
	protected void createSections(final ScrolledForm parent)
	{
		this.createMainSection(parent);
		this.createPaymentSection(parent);
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Course.class, this);
		EntityMediator.removeListener(BookingTypeProposition.class, this);
		super.dispose();
	}

	private Control fillMainSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Code", SWT.NONE);
		label.setLayoutData(new GridData());

		this.code = this.formToolkit.createText(composite, "", SWT.SINGLE);
		this.code.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.code.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				BasicBookingTypeEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.name = this.formToolkit.createText(composite, "", SWT.SINGLE);
		this.name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.name.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				BasicBookingTypeEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Höchstalter", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData layoutData = new GridData();
		layoutData.widthHint = 48;

		this.maxAge = new Spinner(composite, SWT.WRAP);
		this.maxAge.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.maxAge.setLayoutData(layoutData);
		this.maxAge.setDigits(0);
		this.maxAge.setIncrement(1);
		this.maxAge.setPageIncrement(10);
		this.maxAge.setMaximum(120);
		this.maxAge.setMinimum(0);
		this.maxAge.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				BasicBookingTypeEditor.this.setDirty(true);
			}
		});
		
		this.formToolkit.adapt(this.maxAge);

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillPaymentSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Preis", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData layoutData = new GridData();
		layoutData.widthHint = 64;

		Text text = this.formToolkit.createText(composite, "", SWT.RIGHT);
		text.setLayoutData(layoutData);
		text.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				BasicBookingTypeEditor.this.setDirty(true);
			}
		});

		this.price = new FormattedText(text);
		this.price.setFormatter(new NumberFormatter("#,###,##0.00", Locale.getDefault()));

		label = this.formToolkit.createLabel(composite, "Annullationskosten", SWT.NONE);
		label.setLayoutData(new GridData());

		layoutData = new GridData();
		layoutData.widthHint = 64;

		text = this.formToolkit.createText(composite, "", SWT.RIGHT);
		text.setLayoutData(layoutData);
		text.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				BasicBookingTypeEditor.this.setDirty(true);
			}
		});

		this.annulationCharges = new FormattedText(text);
		this.annulationCharges.setFormatter(new NumberFormatter("#,###,##0.00", Locale.getDefault()));

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Class adapter)
	{
		// if (IContentOutlinePage.class.equals(adapter))
		// {
		// this.contentOutlinePage = new BasicBookingTypeEditorContentOutlinePage(this);
		// return this.contentOutlinePage;
		// }
		return super.getAdapter(adapter);
	}

	@Override
	protected Message getMessage(final PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		return msg;
	}

	@Override
	protected String getName()
	{
		BasicBookingTypeEditorInput input = (BasicBookingTypeEditorInput) this.getEditorInput();
		BookingTypeProposition BasicBookingType = (BookingTypeProposition) input.getAdapter(BookingTypeProposition.class);
		return BasicBookingType.getId() == null ? "Neu" : "Buchungsart: " + BasicBookingType.getId();
	}

	@Override
	protected String getText()
	{
		BasicBookingTypeEditorInput input = (BasicBookingTypeEditorInput) this.getEditorInput();
		BookingTypeProposition basicBookingType = (BookingTypeProposition) input.getAdapter(BookingTypeProposition.class);
		return (basicBookingType.getId() == null ? "Neue Buchungsart" : "Buchungsart " + basicBookingType.getId());
	}

	@Override
	protected void initialize()
	{
		Long id = ((BasicBookingTypeEditorInput) this.getEditorInput()).getEntity().getId();
		this.initializeDialogSettings(id == null ? BasicBookingTypeEditor.BASIC_BOOKING_TYPE_EDITOR : BasicBookingTypeEditor.BASIC_BOOKING_TYPE_EDITOR + "."
				+ id);

		EntityMediator.addListener(Course.class, this);
		EntityMediator.addListener(BookingTypeProposition.class, this);
	}

	private void initializeDialogSettings(final String section)
	{
		this.dialogSettings = Activator.getDefault().getDialogSettings().getSection(section);
		if (this.dialogSettings == null)
			this.dialogSettings = Activator.getDefault().getDialogSettings().addNewSection(section);
	}

	@Override
	protected void loadValues()
	{
		BasicBookingTypeEditorInput input = (BasicBookingTypeEditorInput) this.getEditorInput();
		BookingTypeProposition basicBookingType = input.getEntity();
		if (basicBookingType != null)
		{
			this.code.setText(basicBookingType.getCode());
			this.name.setText(basicBookingType.getName());
			this.maxAge.setSelection(basicBookingType.getMaxAge());
			this.price.setValue(basicBookingType.getPrice());
			this.annulationCharges.setValue(basicBookingType.getAnnulationCharges());
		}
		this.setDirty(false);
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				if (entity instanceof BookingTypeProposition)
				{
					BookingTypeProposition basicBookingType = (BookingTypeProposition) entity;
					BookingTypeProposition thisBasicBookingType = ((BasicBookingTypeEditorInput) getEditorInput()).getEntity();
					if (thisBasicBookingType != null && thisBasicBookingType.getId() != null && thisBasicBookingType.getId().equals(basicBookingType.getId()))
					{
						IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findEditor(getEditorInput());
						if (editor != null)
						{
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editor, false);
						}
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void propertyChange(final java.beans.PropertyChangeEvent event)
	{
		this.setDirty(true);
	}
	
	private double convert(String text, double oldValue)
	{
		try
		{
			return Double.valueOf(text).doubleValue();
		}
		catch (NumberFormatException e)
		{
			return oldValue;
		}
	}

	@Override
	protected void saveValues()
	{
		BasicBookingTypeEditorInput input = (BasicBookingTypeEditorInput) this.getEditorInput();
		BookingTypeProposition basicBookingType = input.getEntity();
		if (basicBookingType != null)
		{
			basicBookingType.setCode(this.code.getText());
			basicBookingType.setName(this.name.getText());
			basicBookingType.setMaxAge(this.maxAge.getSelection());
			basicBookingType.setPrice(convert(this.price.getControl().getText(), basicBookingType.getPrice()));
			basicBookingType.setAnnulationCharges(convert(this.annulationCharges.getControl().getText(), basicBookingType.getAnnulationCharges()));
		}
	}

	@Override
	public void setFocus()
	{
		this.code.setFocus();
	}

	@Override
	protected boolean validate()
	{
		Message msg = null;

		return msg == null;
	}

	@Override
	protected boolean validateType(final AbstractEntityEditorInput<BookingTypeProposition> input)
	{
		return input.getEntity() instanceof BookingTypeProposition;
	}

}
