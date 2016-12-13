package ch.eugster.events.visits.editors;

import java.awt.Color;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.RGB;
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

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.VisitTheme;
import ch.eugster.events.persistence.queries.VisitThemeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;
import ch.eugster.events.visits.Activator;

public class VisitThemeEditor extends AbstractEntityEditor<VisitTheme>
{
	public static final String ID = "ch.eugster.events.visits.theme.editor";

	private Text name;

	private Text description;

	private ColorSelector color;

	private RGB selectedRGB;

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(VisitTheme.class, this);
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
				VisitThemeEditor.this.scrolledForm.reflow(true);
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
			@Override
			public void modifyText(ModifyEvent e)
			{
				VisitThemeEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Beschreibung", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 124;

		this.description = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL);
		this.description.setLayoutData(gridData);
		this.description.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				VisitThemeEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Farbe", SWT.NONE);
		label.setLayoutData(new GridData());

		this.color = new ColorSelector(composite);
		this.color.addListener(new IPropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent event)
			{
				selectedRGB = VisitThemeEditor.this.color.getColorValue();
				VisitThemeEditor.this.setDirty(true);
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
			msg = this.getUniqueNameMessage();
		}
		return msg;
	}

	private Message getUniqueNameMessage()
	{
		Message msg = null;

		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				VisitThemeEditorInput input = (VisitThemeEditorInput) this.getEditorInput();
				VisitTheme visitTheme = (VisitTheme) input.getAdapter(VisitTheme.class);
				String name = this.name.getText();
				VisitThemeQuery query = (VisitThemeQuery) service.getQuery(VisitTheme.class);
				if (!query.isNameUnique(name, visitTheme.getId()))
				{
					msg = new Message(this.name, "Ungültige Bezeichnung");
					msg.setMessage("Die gewählte Bezeichnung wird bereits verwendet.");
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

		if (this.name.getText().isEmpty())
		{
			msg = new Message(this.name, "Fehler");
			msg.setMessage("Die Kategorie muss eine Bezeichnung haben.");
		}

		return msg;
	}

	@Override
	protected String getName()
	{
		VisitThemeEditorInput input = (VisitThemeEditorInput) this.getEditorInput();
		VisitTheme visitTheme = (VisitTheme) input.getAdapter(VisitTheme.class);
		return visitTheme.getId() == null ? "Neu" : visitTheme.getName();
	}

	@Override
	protected String getText()
	{
		VisitThemeEditorInput input = (VisitThemeEditorInput) this.getEditorInput();
		VisitTheme visitTheme = (VisitTheme) input.getAdapter(VisitTheme.class);
		return visitTheme.getId() == null ? "Neues Thema" : "Thema " + visitTheme.getName();
	}

	@Override
	protected void loadValues()
	{
		VisitThemeEditorInput input = (VisitThemeEditorInput) this.getEditorInput();
		VisitTheme visitTheme = (VisitTheme) input.getAdapter(VisitTheme.class);
		if (visitTheme != null)
		{
			this.name.setText(visitTheme.getName());
			this.description.setText(visitTheme.getDescription());
			if (visitTheme.getColor() != null)
			{
				Color color = new java.awt.Color(visitTheme.getColor().intValue());
				this.color.setColorValue(new RGB(color.getRed(), color.getGreen(), color.getBlue()));
			}
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		VisitThemeEditorInput input = (VisitThemeEditorInput) this.getEditorInput();
		VisitTheme visitTheme = (VisitTheme) input.getAdapter(VisitTheme.class);
		if (visitTheme != null)
		{
			visitTheme.setName(this.name.getText());
			visitTheme.setDescription(this.description.getText());
			if (selectedRGB != null)
			{
				java.awt.Color color = new java.awt.Color(selectedRGB.red, selectedRGB.green, selectedRGB.blue);
				visitTheme.setColor(color.getRGB());
			}
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
	protected boolean validateType(AbstractEntityEditorInput<VisitTheme> input)
	{
		return input.getAdapter(VisitTheme.class) instanceof VisitTheme;
	}

	@Override
	public void setFocus()
	{
		this.name.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(VisitTheme.class, this);
	}
}
