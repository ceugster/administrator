package ch.eugster.events.addresstype.editors;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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

import ch.eugster.events.addresstype.Activator;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class AddressTypeEditor extends AbstractEntityEditor<AddressType>
{
	public static final String ID = "ch.eugster.events.addresstype.editor";

	private Text name;

	private Label symbol;

	@Override
	protected void initialize()
	{
		EntityMediator.addListener(AddressType.class, this);
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
				AddressTypeEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSection(Section parent)
	{
		GridLayout layout = new GridLayout(3, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.name = this.formToolkit.createText(composite, "");
		this.name.setLayoutData(gridData);
		this.name.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent e)
			{
				AddressTypeEditor.this.setDirty(true);
			}
		});

		Link selector = new Link(composite, SWT.NONE);
		selector.setText("<a>Symbol</a>");
		selector.setLayoutData(new GridData());
		selector.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(AddressTypeEditor.this.getSite().getShell());
				String filename = dialog.open();
				if (filename != null)
				{
					try
					{
						Image image = new Image(Display.getCurrent(), new FileInputStream(filename));
						symbol.setImage(image);
						symbol.getParent().layout();
						AddressTypeEditor.this.setDirty(true);
					}
					catch (FileNotFoundException fnfe)
					{

					}
				}
			}

		});

		this.symbol = this.formToolkit.createLabel(composite, "", SWT.NONE);
		symbol.setLayoutData(new GridData());
		Menu menu = new Menu(symbol);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText("Entfernen");
		item.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (symbol.getImage() != null)
				{
					symbol.setImage(null);
					AddressTypeEditor.this.setDirty(true);
				}
			}
		});
		symbol.setMenu(menu);

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

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();

		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			AddressTypeEditorInput input = (AddressTypeEditorInput) this.getEditorInput();
			AddressType addressType = (AddressType) input.getAdapter(AddressType.class);
			String name = this.name.getText();
			AddressTypeQuery query = (AddressTypeQuery) service.getQuery(AddressType.class);
			if (!query.isNameUnique(name, addressType.getId()))
			{
				msg = new Message(this.name, "Ungültige Bezeichnung");
				msg.setMessage("Die gewählte Bezeichnung wird bereits verwendet.");
				return msg;
			}
		}
		tracker.close();

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
		AddressTypeEditorInput input = (AddressTypeEditorInput) this.getEditorInput();
		AddressType addressType = (AddressType) input.getAdapter(AddressType.class);
		return addressType.getId() == null ? "Neu" : (addressType.getName().length() == 0 ? "???" : addressType
				.getName());
	}

	@Override
	protected String getText()
	{
		AddressTypeEditorInput input = (AddressTypeEditorInput) this.getEditorInput();
		AddressType addressType = (AddressType) input.getAdapter(AddressType.class);
		return addressType.getId() == null ? "Neue Adressart" : "Adressart: "
				+ (addressType.getName().length() == 0 ? "???" : addressType.getName());
	}

	@Override
	protected void loadValues()
	{
		AddressTypeEditorInput input = (AddressTypeEditorInput) this.getEditorInput();
		AddressType addressType = (AddressType) input.getAdapter(AddressType.class);
		if (addressType != null)
		{
			this.name.setText(addressType.getName());
			if (addressType.getImage() != null)
			{
				// BufferedImage bufferedImage = addressType.getImage();
				// ImageData imageData =
				// ImageConverter.convertToSWT(bufferedImage);
				// Image image = new Image(Display.getCurrent(), imageData);
				symbol.setImage(addressType.getImage());
			}
		}
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		AddressTypeEditorInput input = (AddressTypeEditorInput) this.getEditorInput();
		AddressType addressType = (AddressType) input.getAdapter(AddressType.class);
		if (addressType != null)
		{
			addressType.setName(this.name.getText());
			if (symbol.getImage() != null)
			{
				Image image = symbol.getImage();
				addressType.setImage(image, SWT.IMAGE_PNG);
				// BufferedImage bufferedImage =
				// ImageConverter.convertToAWT(image.getImageData());
				// addressType.setImage(bufferedImage);
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
	protected boolean validateType(AbstractEntityEditorInput<AddressType> input)
	{
		return input.getAdapter(AddressType.class) instanceof AddressType;
	}

	@Override
	public void setFocus()
	{
		this.name.setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(AddressType.class, this);
	}
}
