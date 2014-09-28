package ch.eugster.events.zipcode.editors;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.country.views.CountryContentProvider;
import ch.eugster.events.country.views.CountryLabelProvider;
import ch.eugster.events.country.views.CountrySorter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.ZipCode;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;
import ch.eugster.events.zipcode.Activator;

public class ZipCodeEditor extends AbstractEntityEditor<ZipCode>
{
	public static final String ID = "ch.eugster.events.zipcode.editor";

	private Text zip;

	private Text city;

	private Text state;

	private ComboViewer countryViewer;

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(ZipCode.class, this);
	}

	@Override
	protected void createSections(ScrolledForm parent)
	{
		this.createZipCodeSection(parent);
	}

	private void createZipCodeSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Postleitzahl");
		section.setClient(this.fillZipCodeSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				ZipCodeEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillZipCodeSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Postleitzahl", SWT.NONE);
		label.setLayoutData(new GridData());

		this.zip = this.formToolkit.createText(composite, "");
		this.zip.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.zip.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				ZipCodeEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Ort", SWT.NONE);
		label.setLayoutData(new GridData());

		this.city = this.formToolkit.createText(composite, "");
		this.city.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.city.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				ZipCodeEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Bundesland", SWT.NONE);
		label.setLayoutData(new GridData());

		this.state = this.formToolkit.createText(composite, "");
		this.state.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.state.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				ZipCodeEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Land", SWT.NONE);
		label.setLayoutData(new GridData());

		Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData());

		countryViewer = new ComboViewer(combo);
		countryViewer.setContentProvider(new CountryContentProvider());
		countryViewer.setLabelProvider(new CountryLabelProvider());
		countryViewer.setSorter(new CountrySorter());
		
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class.getName(), null);
		try
		{
			tracker.open();
			ConnectionService service = (ConnectionService) tracker.getService();
			CountryQuery query = (CountryQuery) service.getQuery(Country.class);
			Country country = query.selectDefault();
			IStructuredSelection ssel = new StructuredSelection(country);
			countryViewer.setInput(service);
			countryViewer.setSelection(ssel);
		}
		finally
		{
			tracker.close();
		}
		
		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	@Override
	protected Message getMessage(PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		return msg;
	}

	@Override
	protected String getName()
	{
		ZipCodeEditorInput input = (ZipCodeEditorInput) this.getEditorInput();
		ZipCode zipCode = (ZipCode) input.getAdapter(ZipCode.class);
		return zipCode.getId() == null ? "Neu" : zipCode.getZip() == null ? "???" : zipCode.getZip();
	}

	@Override
	protected String getText()
	{
		ZipCodeEditorInput input = (ZipCodeEditorInput) this.getEditorInput();
		ZipCode zipCode = (ZipCode) input.getAdapter(ZipCode.class);
		return zipCode.getId() == null ? "Neue Plz" : zipCode.getZip() == null ? "???" : zipCode.getZip();
	}

	@Override
	protected void loadValues()
	{
		ZipCodeEditorInput input = (ZipCodeEditorInput) this.getEditorInput();
		ZipCode zipCode = (ZipCode) input.getAdapter(ZipCode.class);

		this.zip.setText(zipCode.getZip());
		this.city.setText(zipCode.getCity());
		this.state.setText(zipCode.getState());
		this.countryViewer.setSelection(new StructuredSelection(zipCode.getCountry()));
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		ZipCodeEditorInput input = (ZipCodeEditorInput) this.getEditorInput();
		ZipCode zipCode = (ZipCode) input.getAdapter(ZipCode.class);

		zipCode.setCity(city.getText());
		zipCode.setZip(zip.getText());
		zipCode.setState(state.getText());
		IStructuredSelection ssel = (IStructuredSelection) countryViewer.getSelection();
		Country country = (Country) ssel.getFirstElement();
		zipCode.setCountry(country);
	}

	@Override
	protected boolean validate()
	{
		Message msg = null;

		return msg == null;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<ZipCode> input)
	{
		return input.getAdapter(ZipCode.class) instanceof ZipCode;
	}

	@Override
	public void setFocus()
	{
		this.zip.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(ZipCode.class, this);
	}
}
