package ch.eugster.events.visits.editors;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.formattedtext.MaskFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.SelectedEmail;
import ch.eugster.events.persistence.model.SelectedPhone;
import ch.eugster.events.persistence.model.Teacher;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;
import ch.eugster.events.ui.formatters.PersonFormatter;

public class TeacherEditor extends AbstractEntityEditor<Teacher>
{
	public static final String ID = "ch.eugster.events.visits.teacher.editor";

	private ComboViewer reachablePhone;

	private Text reachableTime;

	private ComboViewer email;

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(Teacher.class, this);
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
		section.setText("Kontaktaufnahme");
		section.setClient(this.fillSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				TeacherEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Erreichbarkeit", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 124;

		this.reachableTime = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL);
		this.reachableTime.setLayoutData(gridData);
		this.reachableTime.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				TeacherEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Telefon", SWT.NONE);
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

		this.reachablePhone = new ComboViewer(combo);
		this.reachablePhone.setContentProvider(new ArrayContentProvider());
		this.reachablePhone.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof SelectedPhone)
				{
					SelectedPhone phone = (SelectedPhone) element;
					TeacherEditorInput input = (TeacherEditorInput) getEditorInput();
					Teacher teacher = (Teacher) input.getAdapter(Teacher.class);
					return getFormattedPhone(teacher, phone);
				}
				return "";
			}
		});
		this.reachablePhone.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				TeacherEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Email", SWT.NONE);
		label.setLayoutData(new GridData());

		combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

		this.email = new ComboViewer(combo);
		this.email.setContentProvider(new ArrayContentProvider());
		this.email.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof SelectedEmail)
				{
					SelectedEmail email = (SelectedEmail) element;
					TeacherEditorInput input = (TeacherEditorInput) getEditorInput();
					Teacher teacher = (Teacher) input.getAdapter(Teacher.class);
					return teacher.getEmail(email);
				}
				return "";
			}
		});
		this.email.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				TeacherEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private String getFormattedPhone(Teacher teacher, SelectedPhone phone)
	{
		String phoneNumber = teacher.getPhone(phone);
		if (phoneNumber.isEmpty())
		{
			return phoneNumber;
		}
		try
		{
			MaskFormatter formatter = new MaskFormatter(teacher.getLink().getAddress().getCountry().getPhonePattern());
			formatter.setValue(phoneNumber);
			return formatter.getDisplayString();
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		return "";
	}

	@Override
	protected Message getMessage(PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		if (errorCode.equals(""))
		{
			msg = this.getUniqueNameMessage();
		}
		return msg;
	}

	private Message getUniqueNameMessage()
	{
		Message msg = null;

		return msg;
	}

	private Message getEmptyNameMessage()
	{
		Message msg = null;

		return msg;
	}

	@Override
	protected String getName()
	{
		TeacherEditorInput input = (TeacherEditorInput) this.getEditorInput();
		Teacher teacher = (Teacher) input.getAdapter(Teacher.class);
		return PersonFormatter.getInstance().formatId(teacher.getLink().getPerson());
	}

	@Override
	protected String getText()
	{
		TeacherEditorInput input = (TeacherEditorInput) this.getEditorInput();
		Teacher teacher = (Teacher) input.getAdapter(Teacher.class);
		return PersonFormatter.getInstance().formatLastnameFirstname(teacher.getLink().getPerson());
	}

	private SelectedPhone[] getSelectablePhones(LinkPersonAddress link)
	{
		Collection<SelectedPhone> phones = new ArrayList<SelectedPhone>();
		phones.add(SelectedPhone.NONE);
		if (!link.getPerson().getPhone().isEmpty())
		{
			phones.add(SelectedPhone.PERSON);
		}
		if (!link.getPhone().isEmpty())
		{
			phones.add(SelectedPhone.LINK);
		}
		if (!link.getAddress().getPhone().isEmpty())
		{
			phones.add(SelectedPhone.ADDRESS);
		}
		return phones.toArray(new SelectedPhone[0]);
	}

	private SelectedEmail[] getSelectableEmails(LinkPersonAddress link)
	{
		Collection<SelectedEmail> emails = new ArrayList<SelectedEmail>();
		emails.add(SelectedEmail.NONE);
		if (!link.getPerson().getEmail().isEmpty())
		{
			emails.add(SelectedEmail.PERSON);
		}
		if (!link.getEmail().isEmpty())
		{
			emails.add(SelectedEmail.LINK);
		}
		if (!link.getAddress().getEmail().isEmpty())
		{
			emails.add(SelectedEmail.ADDRESS);
		}
		return emails.toArray(new SelectedEmail[0]);
	}

	@Override
	protected void loadValues()
	{
		TeacherEditorInput input = (TeacherEditorInput) this.getEditorInput();
		Teacher teacher = (Teacher) input.getAdapter(Teacher.class);
		if (teacher != null)
		{
			this.reachableTime.setText(teacher.getBestReachTime());

			SelectedPhone[] phones = getSelectablePhones(teacher.getLink());
			this.reachablePhone.setInput(phones);
			if (phones.length > 0)
			{
				StructuredSelection ssel = new StructuredSelection(
						new SelectedPhone[] { (teacher.getSelectedPhone() == null ? phones[0]
								: teacher.getSelectedPhone()) });
				this.reachablePhone.setSelection(ssel);
			}

			SelectedEmail[] emails = getSelectableEmails(teacher.getLink());
			this.email.setInput(emails);
			if (emails.length > 0)
			{
				StructuredSelection ssel = new StructuredSelection(
						new SelectedEmail[] { (teacher.getSelectedEmail() == null ? emails[0]
								: teacher.getSelectedEmail()) });
				this.email.setSelection(ssel);
			}
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		TeacherEditorInput input = (TeacherEditorInput) this.getEditorInput();
		Teacher teacher = (Teacher) input.getAdapter(Teacher.class);
		if (teacher != null)
		{
			teacher.setBestReachTime(this.reachableTime.getText());

			StructuredSelection ssel = (StructuredSelection) this.reachablePhone.getSelection();
			teacher.setSelectedPhone((SelectedPhone) ssel.getFirstElement());

			ssel = (StructuredSelection) this.email.getSelection();
			teacher.setSelectedEmail((SelectedEmail) ssel.getFirstElement());
		}
	}

	@Override
	protected boolean validate()
	{
		Message msg = this.getUniqueNameMessage();

		if (msg == null)
			msg = this.getEmptyNameMessage();

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<Teacher> input)
	{
		return input.getAdapter(Teacher.class) instanceof Teacher;
	}

	@Override
	public void setFocus()
	{
		this.reachableTime.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Teacher.class, this);
	}
}
