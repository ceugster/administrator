package ch.eugster.events.person.dialogs;

import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.IFormPage;

import ch.eugster.events.persistence.model.AddressType;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.person.editors.FormEditorLinkPage;
import ch.eugster.events.person.editors.FormEditorPersonPage;
import ch.eugster.events.person.editors.PersonFormEditor;

public class ChangeAddressTypeDialog extends TitleAreaDialog
{
	private final AddressType[] selectableAddressTypes;

	private final FormEditorLinkPage currentPage;

	private ComboViewer addressTypeViewer;

	public ChangeAddressTypeDialog(final Shell shell, final FormEditorLinkPage currentPage,
			final AddressType[] selectableAddressTypes)
	{
		super(shell);
		this.selectableAddressTypes = selectableAddressTypes;
		this.currentPage = currentPage;
	}

	@Override
	protected void buttonPressed(final int buttonId)
	{
		if (buttonId == IDialogConstants.OK_ID)
		{
			if (!addressTypeViewer.getSelection().isEmpty())
			{
				StructuredSelection ssel = (StructuredSelection) addressTypeViewer.getSelection();
				PersonFormEditor editor = currentPage.getEditor();
				AddressType addressType = (AddressType) ssel.getFirstElement();
				LinkPersonAddress link = currentPage.getLink();

				editor.removePage(currentPage.getId());
				try
				{
					link.setDeleted(false);
					link.setAddressType(addressType);
					String pageId = "link.page." + addressType.getId().toString();
					FormEditorLinkPage page = new FormEditorLinkPage(editor, getPersonPage(editor), pageId, link,
							addressType);
					editor.addPage(page);
					editor.setActivePage(pageId);
					page.setDirty();
				}
				catch (PartInitException e)
				{
				}
				editor.setDirty();
			}
		}
		this.close();
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);

		StructuredSelection ssel = new StructuredSelection();
		ssel = new StructuredSelection(new AddressType[] { currentPage.getLink().getAddressType() });
		this.addressTypeViewer.setSelection(ssel);
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle("Ändern des Adresstyps");

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Adresstyp");

		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		addressTypeViewer = new ComboViewer(combo);
		addressTypeViewer.setContentProvider(new ArrayContentProvider());
		addressTypeViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public Image getImage(final Object element)
			{
				if (element instanceof AddressType)
				{
					AddressType type = (AddressType) element;
					return type.getImage();
				}
				return null;
			}

			@Override
			public String getText(final Object element)
			{
				if (element instanceof AddressType)
				{
					AddressType type = (AddressType) element;
					return type.getName();
				}
				return "";
			}
		});
		addressTypeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				if (ssel.getFirstElement() instanceof AddressType)
				{
					AddressType addressType = (AddressType) ssel.getFirstElement();

					getButton(IDialogConstants.OK_ID).setEnabled(
							!addressType.getId().equals(currentPage.getLink().getAddressType().getId()));
				}
			}
		});
		addressTypeViewer.setSorter(new ViewerSorter()
		{
			@Override
			public int compare(final Viewer viewer, final Object e1, final Object e2)
			{
				AddressType at1 = (AddressType) e1;
				AddressType at2 = (AddressType) e2;
				return at1.getName().compareTo(at2.getName());
			}
		});
		addressTypeViewer.setInput(selectableAddressTypes);

		return parent;
	}

	private FormEditorPersonPage getPersonPage(final PersonFormEditor editor)
	{
		Collection<IFormPage> pages = editor.getPages();
		for (IFormPage page : pages)
		{
			if (page instanceof FormEditorPersonPage)
			{
				return (FormEditorPersonPage) page;
			}
		}
		return null;
	}

}
