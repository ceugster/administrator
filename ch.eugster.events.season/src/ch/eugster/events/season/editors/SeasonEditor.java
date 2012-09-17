package ch.eugster.events.season.editors;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.Season;
import ch.eugster.events.persistence.queries.SeasonQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.season.Activator;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class SeasonEditor extends AbstractEntityEditor<Season>
{
	public static final String ID = "ch.eugster.events.season.editor";

	private Text code;

	private Text title;

	private CDateTime start;

	private CDateTime end;

	private Button closed;

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(Season.class, this);
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
				SeasonEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Code", SWT.NONE);
		label.setLayoutData(new GridData());

		this.code = this.formToolkit.createText(composite, "");
		this.code.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.code.addVerifyListener(new VerifyListener()
		{
			@Override
			public void verifyText(VerifyEvent e)
			{
				if (!e.text.isEmpty())
				{
					try
					{
						Long.valueOf(e.text);
					}
					catch (NumberFormatException nfe)
					{
						e.doit = false;
					}
				}
			}

		});
		this.code.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				SeasonEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		this.title = this.formToolkit.createText(composite, "");
		this.title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.title.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				SeasonEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Saisonbeginn", SWT.NONE);
		label.setLayoutData(new GridData());

		this.start = new CDateTime(composite, CDT.DATE_MEDIUM | CDT.SPINNER);
		this.start.setLayoutData(new GridData());
		this.start.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.start.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				SeasonEditor.this.setDirty(true);
				if (start.getSelection() instanceof java.util.Date)
				{
					if (SeasonEditor.this.end.getSelection() == null
							|| SeasonEditor.this.end.getSelection().before(SeasonEditor.this.start.getSelection()))
					{
						SeasonEditor.this.end.setSelection(SeasonEditor.this.start.getSelection());
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event)
			{
				this.widgetSelected(event);
			}
		});
		this.formToolkit.adapt(this.start);

		label = this.formToolkit.createLabel(composite, "Saisonende", SWT.NONE);
		label.setLayoutData(new GridData());

		this.end = new CDateTime(composite, CDT.DATE_MEDIUM | CDT.SPINNER);
		this.end.setLayoutData(new GridData());
		this.end.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.end.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				SeasonEditor.this.setDirty(true);
				if (end.getSelection() instanceof java.util.Date)
				{
					if (SeasonEditor.this.start.getSelection() == null
							|| SeasonEditor.this.start.getSelection().after(SeasonEditor.this.end.getSelection()))
					{
						SeasonEditor.this.start.setSelection(SeasonEditor.this.end.getSelection());
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event)
			{
				this.widgetSelected(event);
			}
		});
		this.formToolkit.adapt(this.end);

		label = this.formToolkit.createLabel(composite, "", SWT.NONE);
		label.setLayoutData(new GridData());

		this.closed = this.formToolkit.createButton(composite, "Abgeschlossen", SWT.CHECK);
		this.closed.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.closed.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent event)
			{
				SeasonEditor.this.setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event)
			{
				this.widgetSelected(event);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	@Override
	protected Message getMessage(PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		if (errorCode.equals(""))
		{
			msg = this.getUniqueCodeMessage();
		}
		return msg;
	}

	private Message getUniqueCodeMessage()
	{
		Message msg = null;

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();

		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			SeasonEditorInput input = (SeasonEditorInput) this.getEditorInput();
			Season season = (Season) input.getAdapter(Season.class);
			String code = this.code.getText();
			SeasonQuery query = (SeasonQuery) service.getQuery(Season.class);
			if (!query.isCodeUnique(code, season.getId()))
			{
				msg = new Message(this.code, "Ungültiger Code");
				msg.setMessage("Der gewählte Code wird bereits verwendet.");
				return msg;
			}
		}
		tracker.close();

		return msg;
	}

	private Message getEmptyNameMessage()
	{
		Message msg = null;

		if (this.title.getText().isEmpty())
		{
			msg = new Message(this.title, "Fehler");
			msg.setMessage("Die Domäne muss eine Bezeichnung haben.");
		}

		return msg;
	}

	@Override
	protected String getName()
	{
		SeasonEditorInput input = (SeasonEditorInput) this.getEditorInput();
		Season season = (Season) input.getAdapter(Season.class);
		return season.getId() == null ? "Neu" : (season.getCode().length() == 0 ? "???" : season.getCode());
	}

	@Override
	protected String getText()
	{
		SeasonEditorInput input = (SeasonEditorInput) this.getEditorInput();
		Season season = (Season) input.getAdapter(Season.class);
		return season.getId() == null ? "Neue Saison" : "Saison "
				+ (season.getCode().length() == 0 ? season.getTitle() : season.getCode());
	}

	@Override
	protected void loadValues()
	{
		SeasonEditorInput input = (SeasonEditorInput) this.getEditorInput();
		Season season = (Season) input.getAdapter(Season.class);

		if (season != null)
		{
			this.code.setText(season.getCode());
			this.title.setText(season.getTitle());

			Date date = null;

			if (season.getStart() != null)
			{
				date = season.getStart().getTime();
			}
			this.start.setSelection(date);

			if (season.getEnd() == null)
			{
				date = null;
			}
			else
			{
				date = season.getEnd().getTime();
			}
			this.end.setSelection(date);

			this.closed.setSelection(season.isClosed());
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		SeasonEditorInput input = (SeasonEditorInput) this.getEditorInput();
		Season season = (Season) input.getAdapter(Season.class);
		if (season != null)
		{
			season.setCode(this.code.getText());
			season.setTitle(this.title.getText());

			Calendar calendar = null;
			if (this.start.getSelection() != null)
			{
				calendar = GregorianCalendar.getInstance();
				calendar.setTime(this.start.getSelection());
			}
			season.setStart(calendar);

			if (this.end.getSelection() == null)
			{
				calendar = null;
			}
			else
			{
				calendar = GregorianCalendar.getInstance();
				calendar.setTime(this.end.getSelection());
			}
			season.setEnd(calendar);

			season.setClosed(this.closed.getSelection());
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
	protected boolean validateType(AbstractEntityEditorInput<Season> input)
	{
		return input.getAdapter(Season.class) instanceof Season;
	}

	@Override
	public void setFocus()
	{
		this.code.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Season.class, this);
		super.dispose();
	}
}
