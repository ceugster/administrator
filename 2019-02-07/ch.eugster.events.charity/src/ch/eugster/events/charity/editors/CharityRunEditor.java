package ch.eugster.events.charity.editors;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
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

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.CharityRun;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class CharityRunEditor extends AbstractEntityEditor<CharityRun>
{
	public static final String ID = "ch.eugster.events.charity.run.editor";

	private Text name;

	private Text description;

	private Text place;
	
	private CDateTime dateTime;

	private ComboViewer stateViewer;
	
	@Override
	protected void initialize()
	{
		EntityMediator.addListener(CharityRun.class, this);
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
				CharityRunEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.name = this.formToolkit.createText(composite, "");
		this.name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.name.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Beschreibung", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.minimumHeight = 126;

		this.description = this.formToolkit.createText(composite, "");
		this.description.setLayoutData(gridData);
		this.description.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Durchführungsort", SWT.NONE);
		label.setLayoutData(new GridData());

		this.place = this.formToolkit.createText(composite, "", SWT.SINGLE);
		this.place.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.place.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				CharityRunEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Durchführungsdatum/-zeit", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 112;

		this.dateTime = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.dateTime.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.dateTime.setLayoutData(gridData);
		this.dateTime.setNullText("");
		this.dateTime.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CharityRunEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Status", SWT.NONE);
		label.setLayoutData(new GridData());

		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);

		stateViewer = new ComboViewer(combo);
		stateViewer.setContentProvider(new ArrayContentProvider());
		stateViewer.setLabelProvider(new LabelProvider() 
		{
			@Override
			public String getText(Object element) 
			{
				CharityRun.CharityRunState state = (CharityRun.CharityRunState) element;
				return state.label();
			}
		});
		stateViewer.setInput(CharityRun.CharityRunState.values());
		
		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	@Override
	protected Message getMessage(PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		if (errorCode.equals(""))
		{
			msg = this.getEmptyNameMessage();
		}
		return msg;
	}

	private Message getEmptyNameMessage()
	{
		Message msg = null;

		if (this.name.getText().isEmpty())
		{
			msg = new Message(this.name, "Fehler");
			msg.setMessage("Der Sponsorlauf muss eine Bezeichnung haben.");
		}

		return msg;
	}

	@Override
	protected String getName()
	{
		CharityRunEditorInput input = (CharityRunEditorInput) this.getEditorInput();
		CharityRun charityRun = input.getEntity();
		return charityRun.getId() == null ? "Neu" : (charityRun.getName().length() == 0 ? "???" : charityRun.getName());
	}

	@Override
	protected String getText()
	{
		CharityRunEditorInput input = (CharityRunEditorInput) this.getEditorInput();
		CharityRun charityRun = input.getEntity();
		return charityRun.getId() == null ? "Neuer Sponsorlauf" : "Sponsorlauf "
				+ (charityRun.getName().isEmpty() ? "???" : charityRun.getName());
	}

	@Override
	protected void loadValues()
	{
		CharityRunEditorInput input = (CharityRunEditorInput) this.getEditorInput();
		CharityRun charityRun = input.getEntity();
		if (charityRun != null)
		{
			this.name.setText(charityRun.getName());
			this.description.setText(charityRun.getDescription());
			this.place.setText(charityRun.getPlace());
			if (charityRun.getId() == null)
			{
				this.stateViewer.setSelection(new StructuredSelection( new CharityRun.CharityRunState[] { CharityRun.CharityRunState.ACTIVE }));
			}
			else
			{
				this.stateViewer.setSelection(charityRun.getState() == null ? new StructuredSelection() : new StructuredSelection( new CharityRun.CharityRunState[] { charityRun.getState() }));
			}
			this.dateTime.setSelection(charityRun.getDate() == null ? null : charityRun.getDate().getTime());
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		CharityRunEditorInput input = (CharityRunEditorInput) this.getEditorInput();
		CharityRun charityRun = input.getEntity();
		if (charityRun != null)
		{
			charityRun.setName(this.name.getText());
			charityRun.setDescription(this.description.getText());
			charityRun.setPlace(this.place.getText());
			Date date = this.dateTime.getSelection();
			if (date == null)
			{
				charityRun.setDate(null);
			}
			else
			{
				Calendar calendar = GregorianCalendar.getInstance();
				calendar.setTime(date);
				charityRun.setDate(calendar);
			}
			charityRun.setState((CharityRun.CharityRunState) ((IStructuredSelection) stateViewer.getSelection()).getFirstElement());
		}
	}

	@Override
	protected boolean validate()
	{
		Message msg = this.getEmptyNameMessage();

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<CharityRun> input)
	{
		return input.getAdapter(CharityRun.class) instanceof CharityRun;
	}

	@Override
	public void setFocus()
	{
		this.name.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(CharityRun.class, this);
	}
}
