package ch.eugster.events.person.dialogs;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.queries.AddressTypeQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.editors.AddressEditor;
import ch.eugster.events.person.editors.AddressEditorInput;
import ch.eugster.events.person.editors.EditorSelector;
import ch.eugster.events.person.views.PersonView;

public class SelectEditorInputTypeDialog extends TitleAreaDialog
{
	private ConnectionService connectionService;
	
	private final Button[] buttons = new Button[EditorInputType.values().length];

	private IDialogSettings settings;
	
	public SelectEditorInputTypeDialog(Shell shell, ConnectionService connectionService)
	{
		super(shell);
		this.connectionService = connectionService;
		this.settings = Activator.getDefault().getDialogSettings().getSection("select.editor.input.type.dialog");
		if (this.settings == null)
		{
			this.settings = Activator.getDefault().getDialogSettings().addNewSection("select.editor.input.type.dialog");
			try
			{
				this.settings.getInt("selected.editor.input.type");
			}
			catch (NumberFormatException e)
			{
				this.settings.put("selected.editor.input.type", EditorInputType.PERSON.ordinal());
			}
		}
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		this.setTitle("Auswahl");
		this.setMessage("Wählen Sie die Art des Objekts, das Sie neu erfassen wollen.");
		
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		Composite top = new Composite(composite, SWT.NONE);
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		top.setLayout(new GridLayout());

		for (final EditorInputType type : EditorInputType.values())
		{
			buttons[type.ordinal()] = new Button(top, SWT.RADIO);
			buttons[type.ordinal()].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			buttons[type.ordinal()].setText(type.label());
			buttons[type.ordinal()].addSelectionListener(new SelectionListener() 
			{
				@Override
				public void widgetSelected(SelectionEvent e) 
				{
					settings.put("selected.editor.input.type", type.ordinal());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e)
				{
					widgetSelected(e);
				}
			});
		}
		buttons[this.settings.getInt("selected.editor.input.type")].setSelection(true);
		
		return composite;
	}
	

	@Override
	protected void okPressed() 
	{
		EditorInputType.values()[this.settings.getInt("selected.editor.input.type")].run(this.getShell(), this.connectionService);
		super.okPressed();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
	}

	enum EditorInputType
	{
		PERSON, ADDRESS;
		
		public String label()
		{
			switch (this)
			{
			case PERSON:
			{
				return "Neue Person erfassen";
			}
			case ADDRESS:
			{
				return "Neue Adresse erfassen";
			}
			default:
			{
				throw new RuntimeException("Invalid editor input type");
			}
			}
		}

		public void run(Shell shell, ConnectionService connectionService)
		{
			switch (this)
			{
			case PERSON:
			{
				if (connectionService != null)
				{
					AddressTypeQuery query = (AddressTypeQuery) connectionService.getQuery(AddressType.class);
					List<AddressType> addressTypes = query.selectAll(false);
					if (addressTypes.isEmpty())
					{
						MessageDialog
								.openWarning(shell, "Kein Adresstyp vorhanden",
										"Bevor Sie Personen erfassen können, muss mindestens ein Adresstyp vorhanden sein.");
					}
					else
					{
						Person person = Person.newInstance();
						person.setCountry(GlobalSettings.getInstance().getCountry());
						Address address = Address.newInstance();
						address.setCountry(GlobalSettings.getInstance().getCountry());
						LinkPersonAddress link = LinkPersonAddress.newInstance(person, address);
						link.setAddressType(addressTypes.get(0));

						Map<String, String> initialValues = null;
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						IViewReference[] references = window.getActivePage().getViewReferences();
						for (IViewReference reference : references)
						{
							if (reference.getId().equals(PersonView.ID))
							{
								IViewPart part = reference.getView(false);
								if (part instanceof PersonView)
								{
									PersonView view = (PersonView) part;
									view.getViewer().setSelection(new StructuredSelection());
									initialValues = view.getSearcher().getInitialValues();
								}
							}
						}

						for (EditorSelector editorSelector : EditorSelector.values())
						{
							if (editorSelector.equals(EditorSelector.values()[PersonSettings.getInstance().getEditorSelector()]))
							{
								try
								{
									window.getActivePage().openEditor(editorSelector.getEditorInput(link, initialValues),
											editorSelector.getEditorId());
									break;
								}
								catch (PartInitException e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
				break;
			}
			case ADDRESS:
			{
				try
				{
					Address address = Address.newInstance();
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IViewReference[] references = window.getActivePage().getViewReferences();
					for (IViewReference reference : references)
					{
						if (reference.getId().equals(PersonView.ID))
						{
							IViewPart part = reference.getView(false);
							if (part instanceof PersonView)
							{
								PersonView view = (PersonView) part;
								view.getViewer().setSelection(new StructuredSelection());
							}
						}
					}
					window.getActivePage().openEditor(new AddressEditorInput(address), AddressEditor.ID);
				}
				catch (PartInitException e)
				{
					e.printStackTrace();
				}
				break;
			}
			default:
			{
				throw new RuntimeException("Invalid editor input type");
			}
			}
		}
	}
}
