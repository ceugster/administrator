package ch.eugster.events.addressgroup.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.progress.UIJob;

import ch.eugster.events.addressgroup.Activator;
import ch.eugster.events.addressgroup.dnd.AddressGroupViewerDropAdapter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.AddressGroup;
import ch.eugster.events.persistence.model.AddressGroupMember;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.person.editors.AddressEditor;
import ch.eugster.events.person.editors.AddressEditorInput;
import ch.eugster.events.person.editors.EditorSelector;
import ch.eugster.events.ui.dnd.EntityTransfer;
import ch.eugster.events.ui.views.AbstractEntityView;

public class AddressGroupMemberView extends AbstractEntityView implements IDoubleClickListener, ISelectionListener
{
	public static final String ID = "ch.eugster.events.addressgroup.view.memberView";

	private Text expressionFilter;

	private Button clearExpressionFilter;

	private TableViewer viewer;

	private Label countLabel;

	private IDialogSettings dialogSettings;

	private AddressGroupMemberSorter sorter;

	private IContextActivation ctxActivation;

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener()
		{
			@Override
			public void menuAboutToShow(final IMenuManager manager)
			{
			}
		});

		Menu menu = menuManager.createContextMenu(this.viewer.getControl());
		this.viewer.getControl().setMenu(menu);

		this.getSite().registerContextMenu(menuManager, this.viewer);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(final Composite parent)
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxActivation = ctxService.activateContext("ch.eugster.events.addressgroup.context");

		parent.setLayout(new GridLayout(3, false));

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Filter");

		final AddressGroupMemberFilter memberFilter = new AddressGroupMemberFilter();
		memberFilter.setFilter(this.dialogSettings.get("member.filter"));

		this.expressionFilter = new Text(parent, SWT.BORDER);
		this.expressionFilter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.expressionFilter.setText(this.dialogSettings.get("member.filter"));
		this.expressionFilter.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				String value = AddressGroupMemberView.this.expressionFilter.getText();
				AddressGroupMemberView.this.dialogSettings.put("member.filter", value);
				memberFilter.setFilter(value);
				internalRefresh();
			}
		});

		this.clearExpressionFilter = new Button(parent, SWT.PUSH);
		this.clearExpressionFilter.setLayoutData(new GridData());
		this.clearExpressionFilter.setImage(Activator.getDefault().getImageRegistry().get("CLEAR"));
		this.clearExpressionFilter.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				clearExpressionFilter();
			}
		});

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;
		
		Table table = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);

		final ViewerFilter deletedFilter = new DeletedEntityFilter();

		int col = this.dialogSettings.getInt("member.sorter.column");
		boolean asc = this.dialogSettings.getBoolean("member.sorter.ascending");
		this.sorter = new AddressGroupMemberSorter(col, asc);

		this.viewer = new TableViewer(table);
		this.viewer.setContentProvider(new AddressGroupMemberContentProvider());
		this.viewer.setSorter(this.sorter);
		this.viewer.setFilters(new ViewerFilter[] { deletedFilter, memberFilter });
		this.viewer.addDoubleClickListener(this);
		this.viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				AddressGroupMemberView.this.updateCountLabel();
			}
		});

		TableViewerColumn tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof AddressGroupMember)
				{
					AddressGroupMember member = (AddressGroupMember) object;
					if (member.getLink() == null || !member.getLink().isValid())
					{
						cell.setText(AddressFormatter.getInstance().formatId(member.getAddress()));
						cell.setImage(Activator.getDefault().getImageRegistry().get("ADDRESS"));
					}
					else
					{
						cell.setText(PersonFormatter.getInstance().formatId(member.getLink().getPerson()));
						cell.setImage(Activator.getDefault().getImageRegistry().get("MEMBER"));
					}
				}
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Id");
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				if (AddressGroupMemberView.this.sorter.getColumn() == 0)
				{
					boolean asc = !AddressGroupMemberView.this.sorter.getAscending();
					AddressGroupMemberView.this.dialogSettings.put("member.sorter.ascending", asc);
					AddressGroupMemberView.this.sorter.setAscending(asc);
				}
				else
				{
					AddressGroupMemberView.this.dialogSettings.put("member.sorter.column", 0);
					AddressGroupMemberView.this.sorter.setColumn(0);

				}
				internalRefresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof AddressGroupMember)
				{
					AddressGroupMember member = (AddressGroupMember) object;
					if (member.getLink() == null || !member.getLink().isValid())
					{
						cell.setText(member.getAddress().getName());
					}
					else
					{
						cell.setText(PersonFormatter.getInstance()
								.formatLastnameFirstname(member.getLink().getPerson()));
					}
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Name");
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				if (AddressGroupMemberView.this.sorter.getColumn() == 1)
				{
					boolean asc = !AddressGroupMemberView.this.sorter.getAscending();
					AddressGroupMemberView.this.dialogSettings.put("member.sorter.ascending", asc);
					AddressGroupMemberView.this.sorter.setAscending(asc);
				}
				else
				{
					AddressGroupMemberView.this.dialogSettings.put("member.sorter.column", 1);
					AddressGroupMemberView.this.sorter.setColumn(1);

				}
				internalRefresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof AddressGroupMember)
				{
					AddressGroupMember member = (AddressGroupMember) object;
					if (member.getAddress() == null)
					{
						cell.setText(member.getLink().getAddress().getAddress());
					}
					else
					{
						cell.setText(member.getAddress().getAddress());
					}
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Strasse");
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				if (AddressGroupMemberView.this.sorter.getColumn() == 2)
				{
					boolean asc = !AddressGroupMemberView.this.sorter.getAscending();
					AddressGroupMemberView.this.dialogSettings.put("member.sorter.ascending", asc);
					AddressGroupMemberView.this.sorter.setAscending(asc);
				}
				else
				{
					AddressGroupMemberView.this.dialogSettings.put("member.sorter.column", 2);
					AddressGroupMemberView.this.sorter.setColumn(2);

				}
				internalRefresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof AddressGroupMember)
				{
					AddressGroupMember member = (AddressGroupMember) object;
					if (member.getLink() == null || !member.getLink().isValid())
					{
						cell.setText(AddressFormatter.getInstance().formatCityLine(member.getAddress()));
					}
					else
					{
						cell.setText(AddressFormatter.getInstance().formatCityLine(member.getLink().getAddress()));
					}
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Wohnort");
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				if (AddressGroupMemberView.this.sorter.getColumn() == 3)
				{
					boolean asc = !AddressGroupMemberView.this.sorter.getAscending();
					AddressGroupMemberView.this.dialogSettings.put("member.sorter.ascending", asc);
					AddressGroupMemberView.this.sorter.setAscending(asc);
				}
				else
				{
					AddressGroupMemberView.this.dialogSettings.put("member.sorter.column", 3);
					AddressGroupMemberView.this.sorter.setColumn(3);

				}
				internalRefresh();
			}
		});

		tableViewerColumn = new TableViewerColumn(this.viewer, SWT.LEFT);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof AddressGroupMember)
				{
					AddressGroupMember member = (AddressGroupMember) object;
					if (member.getLink() == null || !member.getLink().isValid())
					{
						cell.setText(member.getAddress().getEmail());
					}
					else
					{
						if (member.getLink().getPerson().getEmail().isEmpty())
						{
							cell.setText(member.getLink().getEmail());
						}
						else
						{
							cell.setText(member.getLink().getPerson().getEmail());
						}
					}
				}
			}
		});
		tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Email");
		tableColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				this.widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				if (AddressGroupMemberView.this.sorter.getColumn() == 4)
				{
					boolean asc = !AddressGroupMemberView.this.sorter.getAscending();
					AddressGroupMemberView.this.dialogSettings.put("member.sorter.ascending", asc);
					AddressGroupMemberView.this.sorter.setAscending(asc);
				}
				else
				{
					AddressGroupMemberView.this.dialogSettings.put("member.sorter.column", 3);
					AddressGroupMemberView.this.sorter.setColumn(3);

				}
				internalRefresh();
			}
		});

		Transfer[] transfers = new Transfer[] { EntityTransfer.getTransfer() };
		int ops = DND.DROP_COPY;
		this.viewer.addDropSupport(ops, transfers, new AddressGroupViewerDropAdapter(this.viewer));

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		
		this.countLabel = new Label(parent, SWT.NONE);
		this.countLabel.setLayoutData(gridData);

		this.getSite().setSelectionProvider(this.viewer);
		this.getSite().getPage().addSelectionListener(AddressGroupView.ID, this);

		EntityMediator.addListener(AddressGroupMember.class, this);
		EntityMediator.addListener(LinkPersonAddress.class, this);
		EntityMediator.addListener(Address.class, this);
		EntityMediator.addListener(Person.class, this);

		this.createContextMenu();
	}

	@Override
	public void dispose()
	{
		this.getSite().getPage().removeSelectionListener(AddressGroupView.ID, this);
		EntityMediator.removeListener(AddressGroupMember.class, this);
		EntityMediator.removeListener(LinkPersonAddress.class, this);
		EntityMediator.removeListener(Person.class, this);
		EntityMediator.removeListener(Address.class, this);
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxService.deactivateContext(ctxActivation);
		super.dispose();
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof AddressGroupMember)
		{
			this.editAddressGroupMember((AddressGroupMember) object);
		}
	}

	private void editAddress(final Address address)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new AddressEditorInput(address), AddressEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}

	private void editAddressGroupMember(final AddressGroupMember member)
	{
		if (member.isValidAddressMember())
		{
			editAddress(member.getAddress());
		}
		else if (member.isValidLinkMember())
		{
			editLink(member.getLink());
		}
	}

	private void editLink(final LinkPersonAddress link)
	{
		if (!link.isValid())
		{
			String title = "Entfernte Person";
			String message = "Eine entfernte Person kann nicht bearbeitet werden.";
			Shell shell = this.getSite().getShell();
			MessageDialog dialog = new MessageDialog(shell, title, null, message, MessageDialog.INFORMATION,
					new String[] { "OK" }, 0);
			dialog.open();
		}
		else
		{
			for (EditorSelector editorSelector : EditorSelector.values())
			{
				if (editorSelector.equals(EditorSelector.values()[PersonSettings.getInstance().getEditorSelector()]))
				{
					try
					{
						this.getSite().getPage()
								.openEditor(editorSelector.getEditorInput(link), editorSelector.getEditorId());
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

	public Label getCountLabel()
	{
		return this.countLabel;
	}

	public TableViewer getViewer()
	{
		return this.viewer;
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);

		this.dialogSettings = Activator.getDefault().getDialogSettings().getSection(AddressGroupMemberView.ID);
		if (this.dialogSettings == null)
		{
			this.dialogSettings = Activator.getDefault().getDialogSettings().addNewSection(AddressGroupMemberView.ID);
		}
		if (this.dialogSettings.get("member.filter") == null)
		{
			this.dialogSettings.put("member.filter", "");
		}
		if (this.dialogSettings.get("member.sorter.column") == null)
		{
			this.dialogSettings.put("member.sorter.column", "0");
		}
		if (this.dialogSettings.get("member.sorter.ascending") == null)
		{
			this.dialogSettings.put("member.sorter.ascending", "true");

		}
	}

	private void internalRefresh()
	{
		if (!viewer.getControl().isDisposed())
		{
			viewer.refresh();
			packColumns();
			updateCountLabel();
		}
	}

	private void internalRefresh(Object object)
	{
		if (!viewer.getControl().isDisposed())
		{
			while (viewer.isBusy())
			{
			}
			viewer.refresh(object);
			packColumns();
			updateCountLabel();
		}
	}

	private void refreshViewer()
	{
		UIJob job = new UIJob("")
		{

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				internalRefresh();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void refreshViewer(final Object object)
	{
		UIJob job = new UIJob("")
		{

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				internalRefresh(object);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private void updateCountLabel()
	{
		if (countLabel != null && !countLabel.isDisposed())
		{
			IStructuredSelection ssel = (IStructuredSelection) viewer.getSelection();
			countLabel.setText("Adressen: " + viewer.getTable().getItemCount() + " :: Selektiert: " + ssel.size());
		}
	}

	private boolean isVisible(final AddressGroup addressGroup, final AddressGroupMember addressGroupMember)
	{
		if (addressGroup == null)
		{
			return false;
		}
		return addressGroup.getId().equals(addressGroupMember.getAddressGroup().getId());
	}

	public void packColumns()
	{
		if (!viewer.getTable().isDisposed())
		{
			TableColumn[] tableColumns = this.viewer.getTable().getColumns();
			for (TableColumn tableColumn : tableColumns)
			{
				if (tableColumn != null && !tableColumn.isDisposed())
				{
					tableColumn.pack();
				}
			}
		}
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		final AddressGroup root = (AddressGroup) viewer.getInput();
		if (entity instanceof AddressGroupMember)
		{
			AddressGroupMember member = (AddressGroupMember) entity;
			if (isVisible(root, member))
			{
				root.removeAddressGroupMember(member);
				refreshViewer();
			}
		}
	}

	@Override
	public void postPersist(final AbstractEntity entity)
	{
		AddressGroup root = (AddressGroup) viewer.getInput();
		if (root != null)
		{
			if (entity instanceof AddressGroupMember)
			{
				final AddressGroupMember member = (AddressGroupMember) entity;
				if (isVisible(root, member))
				{
					refreshViewer();
				}
			}
		}
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
		final AddressGroup root = (AddressGroup) AddressGroupMemberView.this.viewer.getInput();
		if (entity instanceof AddressGroupMember)
		{
			final AddressGroupMember member = (AddressGroupMember) entity;
			if (isVisible(root, member))
			{
				refreshViewer(member);
			}
		}
		else if (entity instanceof AddressGroup)
		{
			AddressGroup group = (AddressGroup) entity;
			if (root.getId().equals(group.getId()))
			{
				viewer.setInput(group);
				refreshViewer();
			}
		}
		else if (entity instanceof Address)
		{
			Address address = (Address) entity;
			for (AddressGroupMember member : address.getAddressGroupMembers())
			{
				if (isVisible(root, member))
				{
					refreshViewer(member);
				}
			}
		}
		else if (entity instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) entity;
			for (AddressGroupMember member : link.getAddressGroupMembers())
			{
				if (isVisible(root, member))
				{
					refreshViewer(member);
				}
			}
		}
		else if (entity instanceof Person)
		{
			Person person = (Person) entity;
			for (LinkPersonAddress link : person.getLinks())
			{
				for (AddressGroupMember member : link.getAddressGroupMembers())
				{
					if (isVisible(root, member))
					{
						refreshViewer(member);
					}
				}
			}
		}
	}

	// private void refresh()
	// {
	// UIJob job = new UIJob("Aktualisiere Sicht...")
	// {
	// @Override
	// public IStatus runInUIThread(final IProgressMonitor monitor)
	// {
	// internalRefresh();
	// return Status.OK_STATUS;
	// }
	// };
	// job.setUser(true);
	// job.schedule();
	// }

	@Override
	public void selectionChanged(final IWorkbenchPart part, final ISelection selection)
	{
		if (part instanceof AddressGroupView)
		{
			StructuredSelection ssel = (StructuredSelection) selection;
			AddressGroup addressGroup = null;
			// if (ssel.getFirstElement() instanceof AddressGroupLink)
			// {
			// addressGroup = ((AddressGroupLink)
			// ssel.getFirstElement()).getChild();
			// }
			if (ssel.getFirstElement() instanceof AddressGroup)
			{
				addressGroup = (AddressGroup) ssel.getFirstElement();
			}
			updateViewer(addressGroup);
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus()
	{
		this.viewer.getControl().setFocus();
	}

	private void updateViewer(final AddressGroup addressGroup)
	{
		UIJob updateViewer = new UIJob("Adressen werden aufbereitet...")
		{
			@Override
			public IStatus runInUIThread(final IProgressMonitor monitor)
			{
				if (!AddressGroupMemberView.this.getViewer().getControl().isDisposed())
				{
					String partName = "???";
					if (addressGroup != null)
					{
						partName = (addressGroup.getCode() == null ? (addressGroup.getName() == null ? "???"
								: addressGroup.getName()) : addressGroup.getCode());
					}
					AddressGroupMemberView.this.getViewer().getTable().setEnabled(false);
					AddressGroupMemberView.this.showBusy(true);
					AddressGroupMemberView.this.setPartName(partName);
					AddressGroupMemberView.this.setInput(addressGroup);
					AddressGroupMemberView.this.internalRefresh();
					AddressGroupMemberView.this.showBusy(false);
					AddressGroupMemberView.this.getViewer().getTable().setEnabled(true);
				}
				return Status.OK_STATUS;
			}

		};
		updateViewer.setUser(true);
		updateViewer.schedule();
	}

	private void setInput(Object object)
	{
		if (!this.viewer.getTable().isDisposed())
		{
			this.viewer.setInput(object);
		}
	}

	private void clearExpressionFilter()
	{
		expressionFilter.setText("");
	}
}