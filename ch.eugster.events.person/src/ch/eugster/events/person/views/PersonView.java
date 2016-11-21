package ch.eugster.events.person.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedPersonAndAddressFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.editors.AddressEditor;
import ch.eugster.events.person.editors.AddressEditorInput;
import ch.eugster.events.person.editors.EditorSelector;
import ch.eugster.events.person.views.PersonSorter.ViewerColumn;
import ch.eugster.events.ui.dnd.EntityTransfer;
import ch.eugster.events.ui.dnd.LinkPersonAddressDragSourceListener;
import ch.eugster.events.ui.views.AbstractEntityView;

public class PersonView extends AbstractEntityView implements IDoubleClickListener
{
	public static final String ID = "ch.eugster.events.person.view";

	private LinkSearcher searcher;

	private TreeViewer viewer;

	private Button showDeleted;

	private Label found;

	private Label selected;

	private IDialogSettings dialogSettings;

	private IContextActivation ctxActivation;

	private Color deletedColor;

	public PersonView()
	{
	}

	public void clearSearchFields()
	{
		this.searcher.clearSearchFields();
	}

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

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
		ctxActivation = ctxService.activateContext("ch.eugster.events.person.context");

		parent.setLayout(new GridLayout());

		this.searcher = new LinkSearcher(parent, true, SWT.NONE);
		this.searcher.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Tree tree = new Tree(parent, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setHeaderVisible(true);
		tree.addListener(SWT.Expand, new Listener()
		{
			@Override
			public void handleEvent(final Event event)
			{
				if (tree != null && !tree.isDisposed())
				{
					tree.getDisplay().asyncExec(new Runnable()
					{
						@Override
						public void run()
						{
							if (tree.isDisposed())
								return;
							packColumns();
						}
					});
				}
			}
		});

		this.viewer = new TreeViewer(tree);
		this.viewer.setContentProvider(new PersonContentProvider());
		PersonSorter sorter = new PersonSorter();
		int currentColumn = dialogSettings.getInt("order.by");
		if (currentColumn > ViewerColumn.values().length - 1 || currentColumn < 0)
		{
			currentColumn = 0;
		}
		sorter.setCurrentColumn(ViewerColumn.values()[currentColumn]);
		sorter.setAscending(dialogSettings.getBoolean("order.ascending"));
		this.viewer.setSorter(sorter);
		DeletedPersonAndAddressFilter filter = new DeletedPersonAndAddressFilter();
		filter.setShowDeleted(this.dialogSettings.getBoolean("show.deleted"));
		this.viewer.setFilters(new ViewerFilter[] { filter });
		this.viewer.addDoubleClickListener(this);

		Transfer[] transfers = new Transfer[] { EntityTransfer.getTransfer() };
		int ops = DND.DROP_MOVE | DND.DROP_COPY;
		this.viewer.addDragSupport(ops, transfers, new LinkPersonAddressDragSourceListener(this.viewer));

		this.searcher.addCriteriaChangedListener(new ICriteriaChangedListener()
		{
			@Override
			public void criteriaChanged(final AbstractEntity[] entities)
			{
				Display.getDefault().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						PersonView.this.viewer.setInput(new ContentRoot(entities));
						PersonView.this.viewer.expandAll();
						PersonView.this.packColumns();
						PersonView.this.found.setText("Gefunden: " + entities.length);
					}
				});
			}
		});

		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					cell.setText(ViewerColumn.CODE.value(person));
					cell.setImage(Activator.getDefault().getImageRegistry().get(person.getDefaultLink().getValidMembers().size() == 0 ? Activator.KEY_PERSON_BLUE : Activator.KEY_PERSON_BLUE_WITH_STAR));
					deleted = person.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(ViewerColumn.CODE.value(link));
					cell.setImage(Activator.getDefault().getImageRegistry().get(link.getValidMembers().size() == 0 ? Activator.KEY_PERSON_BLUE : Activator.KEY_PERSON_BLUE_WITH_STAR));
					deleted = link.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					cell.setText(ViewerColumn.CODE.value(address));
					cell.setImage(Activator.getDefault().getImageRegistry().get(Activator.KEY_ADDRESS_MAIN));
					deleted = address.isDeleted();
				}
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		TreeColumn treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText(ViewerColumn.CODE.label());
		treeColumn.setResizable(true);
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				PersonSorter sorter = (PersonSorter) PersonView.this.viewer.getSorter();
				sorter.setCurrentColumn(ViewerColumn.CODE);
				PersonView.this.dialogSettings.put("order.by", ViewerColumn.CODE.ordinal());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.refreshViewer();
			}
		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					cell.setText(ViewerColumn.LASTNAME.value(person));
					deleted = person.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(ViewerColumn.LASTNAME.value(link));
					deleted = link.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					cell.setText(ViewerColumn.LASTNAME.value(address));
					deleted = address.isDeleted();
				}
				cell.setImage(null);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText(ViewerColumn.LASTNAME.label());
		treeColumn.setResizable(true);
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				PersonSorter sorter = (PersonSorter) PersonView.this.viewer.getSorter();
				sorter.setCurrentColumn(ViewerColumn.LASTNAME);
				PersonView.this.dialogSettings.put("order.by", ViewerColumn.LASTNAME.ordinal());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.refreshViewer();
			}
		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					cell.setText(ViewerColumn.FIRSTNAME.value(person));
					deleted = person.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(ViewerColumn.FIRSTNAME.value(link));
					deleted = link.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					cell.setText("");
					deleted = address.isDeleted();
				}
				cell.setImage(null);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText(ViewerColumn.FIRSTNAME.label());
		treeColumn.setResizable(true);
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				PersonSorter sorter = (PersonSorter) PersonView.this.viewer.getSorter();
				sorter.setCurrentColumn(ViewerColumn.FIRSTNAME);
				PersonView.this.dialogSettings.put("order.by", ViewerColumn.FIRSTNAME.ordinal());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.refreshViewer();
			}
		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				Address address = null;
				Image image = null;
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					cell.setText(ViewerColumn.ADDRESS_ID.value(person));
					image = person.getDefaultLink().getAddressType().getImage();
					deleted = person.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(ViewerColumn.ADDRESS_ID.value(link));
					image = link.getAddressType().getImage();
					deleted = link.isDeleted();
				}
				else if (object instanceof Address)
				{
					address = (Address) object;
					cell.setText(ViewerColumn.ADDRESS_ID.value(address));
					image = Activator.getDefault().getImageRegistry().get(Activator.KEY_ADDRESS_MAIN);
					deleted = address.isDeleted();
				}
				cell.setImage(image);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText(ViewerColumn.ADDRESS_ID.label());
		treeColumn.setResizable(true);
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				PersonSorter sorter = (PersonSorter) PersonView.this.viewer.getSorter();
				sorter.setCurrentColumn(ViewerColumn.ADDRESS_ID);
				PersonView.this.dialogSettings.put("order.by", ViewerColumn.ADDRESS_ID.ordinal());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.refreshViewer();
			}
		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					cell.setText(ViewerColumn.ORGANISATION.value(person));
					deleted = person.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					cell.setText(ViewerColumn.ORGANISATION.value(address));
					deleted = address.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(ViewerColumn.ORGANISATION.value(link));
					deleted = link.isDeleted();
				}
				cell.setImage(null);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText(ViewerColumn.ORGANISATION.label());
		treeColumn.setResizable(true);
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				PersonSorter sorter = (PersonSorter) PersonView.this.viewer.getSorter();
				sorter.setCurrentColumn(ViewerColumn.ORGANISATION);
				PersonView.this.dialogSettings.put("order.by", ViewerColumn.ORGANISATION.ordinal());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.refreshViewer();
			}
		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					cell.setText(ViewerColumn.ADDRESS.value(person));
					deleted = person.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					cell.setText(ViewerColumn.ADDRESS.value(address));
					deleted = address.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(ViewerColumn.ADDRESS.value(link));
					deleted = link.isDeleted();
				}
				cell.setImage(null);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText(ViewerColumn.ADDRESS.label());
		treeColumn.setResizable(true);
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				PersonSorter sorter = (PersonSorter) PersonView.this.viewer.getSorter();
				sorter.setCurrentColumn(ViewerColumn.ADDRESS);
				PersonView.this.dialogSettings.put("order.by", ViewerColumn.ADDRESS.ordinal());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.refreshViewer();
			}
		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					cell.setText(ViewerColumn.CITY.value(person));
					deleted = person.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					cell.setText(ViewerColumn.CITY.value(address));
					deleted = address.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(ViewerColumn.CITY.value(link));
					deleted = link.isDeleted();
				}
				cell.setImage(null);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText(ViewerColumn.CITY.label());
		treeColumn.setResizable(true);
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				PersonSorter sorter = (PersonSorter) PersonView.this.viewer.getSorter();
				sorter.setCurrentColumn(ViewerColumn.CITY);
				PersonView.this.dialogSettings.put("order.by", ViewerColumn.CITY.ordinal());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.refreshViewer();
			}
		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					cell.setText(ViewerColumn.PHONE.value(person));
					deleted = person.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					cell.setText(ViewerColumn.PHONE.value(address));
					deleted = address.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(ViewerColumn.PHONE.value(link));
					deleted = link.isDeleted();
				}
				cell.setImage(null);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText(ViewerColumn.PHONE.label());
		treeColumn.setResizable(true);
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				PersonSorter sorter = (PersonSorter) PersonView.this.viewer.getSorter();
				sorter.setCurrentColumn(ViewerColumn.PHONE);
				PersonView.this.dialogSettings.put("order.by", ViewerColumn.PHONE.ordinal());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.refreshViewer();
			}
		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					cell.setText(ViewerColumn.MOBILE.value(person));
					deleted = person.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					cell.setText(ViewerColumn.MOBILE.value(address));
					deleted = address.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(ViewerColumn.MOBILE.value(link));
					deleted = link.isDeleted();
				}
				cell.setImage(null);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText(ViewerColumn.MOBILE.label());
		treeColumn.setResizable(true);
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				PersonSorter sorter = (PersonSorter) PersonView.this.viewer.getSorter();
				sorter.setCurrentColumn(ViewerColumn.MOBILE);
				PersonView.this.dialogSettings.put("order.by", ViewerColumn.MOBILE.ordinal());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.refreshViewer();
			}
		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					cell.setText(ViewerColumn.EMAIL.value(person));
					deleted = person.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					cell.setText(ViewerColumn.EMAIL.value(address));
					deleted = address.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(ViewerColumn.EMAIL.value(link));
					deleted = link.isDeleted();
				}
				cell.setImage(null);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText(ViewerColumn.EMAIL.label());
		treeColumn.setResizable(true);
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				PersonSorter sorter = (PersonSorter) PersonView.this.viewer.getSorter();
				sorter.setCurrentColumn(ViewerColumn.EMAIL);
				PersonView.this.dialogSettings.put("order.by", ViewerColumn.EMAIL.ordinal());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.refreshViewer();
			}
		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					cell.setText(ViewerColumn.DOMAIN.value(person));
					deleted = person.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					cell.setText(ViewerColumn.DOMAIN.value(address));
					deleted = address.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(ViewerColumn.DOMAIN.value(link));
					deleted = link.isDeleted();
				}
				cell.setImage(null);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText(ViewerColumn.DOMAIN.label());
		treeColumn.setResizable(true);
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				PersonSorter sorter = (PersonSorter) PersonView.this.viewer.getSorter();
				sorter.setCurrentColumn(ViewerColumn.DOMAIN);
				PersonView.this.dialogSettings.put("order.by", ViewerColumn.DOMAIN.ordinal());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.refreshViewer();
			}
		});

		Composite info = new Composite(parent, SWT.NONE);
		info.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		info.setLayout(new GridLayout(3, true));

		this.showDeleted = new Button(info, SWT.CHECK);
		this.showDeleted.setText("Gelöschte Objekte zeigen");
		this.showDeleted.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.showDeleted.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				boolean showDeleted = PersonView.this.showDeleted.getSelection();
				ViewerFilter[] filters = viewer.getFilters();
				for (ViewerFilter filter : filters)
				{
					if (filter instanceof DeletedPersonAndAddressFilter)
					{
						DeletedPersonAndAddressFilter f = (DeletedPersonAndAddressFilter) filter;
						f.setShowDeleted(showDeleted);
					}
				}
				refreshViewer();
				PersonView.this.found.setText("Gefunden: " + PersonView.this.viewer.getTree().getItemCount());
				PersonView.this.selected.setText("Ausgewählt: "
						+ ((StructuredSelection) PersonView.this.viewer.getSelection()).size());
				PersonView.this.dialogSettings.put("show.deleted", PersonView.this.showDeleted.getSelection());
			}
		});
		this.showDeleted.setSelection(this.dialogSettings.getBoolean("show.deleted"));

		this.found = new Label(info, SWT.NONE);
		this.found.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.selected = new Label(info, SWT.NONE);
		this.selected.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				// IStructuredSelection ssel = (IStructuredSelection)
				// event.getSelection();
				// Object object = ssel.getFirstElement();
				PersonView.this.selected.setText("Ausgewählt: " + ((StructuredSelection) event.getSelection()).size());
			}
		});

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.viewer);

		this.searcher.initialize();
	}

	// private Person refresh(Person person)
	// {
	// ServiceTracker tracker = new
	// ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
	// ConnectionService.class.getName(), null);
	// tracker.open();
	// try
	// {
	// ConnectionService service = (ConnectionService) tracker.getService();
	// PersonQuery query = (PersonQuery) service.getQuery(Person.class);
	// return (Person) query.refresh(person);
	// }
	// catch (Exception e)
	// {
	// ConnectionService service = (ConnectionService) tracker.getService();
	// PersonQuery query = (PersonQuery) service.getQuery(Person.class);
	// return query.find(Person.class, person.getId());
	// }
	// finally
	// {
	// tracker.close();
	// }
	// }

	// private Address refresh(Address address)
	// {
	// ServiceTracker tracker = new
	// ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
	// ConnectionService.class.getName(), null);
	// tracker.open();
	// try
	// {
	// ConnectionService service = (ConnectionService) tracker.getService();
	// AddressQuery query = (AddressQuery) service.getQuery(Address.class);
	// return (Address) query.refresh(address);
	// }
	// catch (Exception e)
	// {
	// ConnectionService service = (ConnectionService) tracker.getService();
	// AddressQuery query = (AddressQuery) service.getQuery(Address.class);
	// return query.find(Address.class, address.getId());
	// }
	// finally
	// {
	// tracker.close();
	// }
	// }

	// private LinkPersonAddress refresh(LinkPersonAddress link)
	// {
	// ServiceTracker tracker = new
	// ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
	// ConnectionService.class.getName(), null);
	// tracker.open();
	// try
	// {
	// ConnectionService service = (ConnectionService) tracker.getService();
	// LinkPersonAddressQuery query = (LinkPersonAddressQuery)
	// service.getQuery(LinkPersonAddress.class);
	// return (LinkPersonAddress) query.refresh(link);
	// }
	// catch (Exception e)
	// {
	// ConnectionService service = (ConnectionService) tracker.getService();
	// LinkPersonAddressQuery query = (LinkPersonAddressQuery)
	// service.getQuery(LinkPersonAddress.class);
	// return query.find(LinkPersonAddress.class, link.getId());
	// }
	// finally
	// {
	// tracker.close();
	// }
	// }

	@Override
	public void dispose()
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxService.deactivateContext(ctxActivation);
		if (deletedColor != null) deletedColor.dispose();
		if (searcher != null) searcher.dispose();
		super.dispose();
	}

	private Address refreshEntity(Address address)
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			address = (Address) service.refresh(address);
		}
		finally
		{
			tracker.close();
		}
		return address;
	}

	private LinkPersonAddress refreshEntity(LinkPersonAddress link)
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			link = (LinkPersonAddress) service.refresh(link);
		}
		finally
		{
			tracker.close();
		}
		return link;
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof Person)
		{
			Person person = (Person) object;
			this.editLink(refreshEntity(person.getDefaultLink()));
		}
		else if (object instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) object;
			this.editLink(refreshEntity(link.getPerson().getDefaultLink()));
		}
		else if (object instanceof Address)
		{
			Address address = refreshEntity((Address) object);
			this.editAddress(address);
		}
	}

	private void editAddress(final Address address)
	{
		if (address.isDeleted())
		{
			String title = "Entfernte Adresse";
			String message = "Eine entfernte Adresse kann nicht bearbeitet werden.";
			Shell shell = this.getSite().getShell();
			MessageDialog dialog = new MessageDialog(shell, title, null, message, MessageDialog.INFORMATION,
					new String[] { "OK" }, 0);
			dialog.open();
		}
		else
		{
			try
			{
				this.getSite().getPage().openEditor(new AddressEditorInput(address), AddressEditor.ID, true);
			}
			catch (PartInitException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void editLink(final LinkPersonAddress link)
	{
		if (link.getPerson().isDeleted() || link.isDeleted())
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

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter)
	{
		if (adapter.getClass().equals(String.class))
		{
			return ID;
		}
		return null;
	}

	public LinkSearcher getSearcher()
	{
		return this.searcher;
	}

	public TreeViewer getViewer()
	{
		return this.viewer;
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);

		this.dialogSettings = Activator.getDefault().getDialogSettings().getSection("person.view");

		if (this.dialogSettings == null)
			this.dialogSettings = Activator.getDefault().getDialogSettings().addNewSection("person.view");
		if (this.dialogSettings.get("show.deleted") == null)
			this.dialogSettings.put("show.deleted", false);
		try
		{
			this.dialogSettings.getInt("order.by");
		}
		catch (NumberFormatException e)
		{
			this.dialogSettings.put("order.by", 1);
		}

		deletedColor = new Color(Display.getCurrent(), 255, 180, 180);

		EntityMediator.addListener(Person.class, new EntityAdapter()
		{
			@Override
			public void postDelete(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						refreshViewer();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						if (!viewer.getTree().isDisposed())
						{
							ContentRoot root = (ContentRoot) viewer.getInput();
							viewer.add(root, entity);
							packColumns();
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						refreshViewer(entity);
						packColumns();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		});
		EntityMediator.addListener(LinkPersonAddress.class, new EntityAdapter()
		{
			@Override
			public void postDelete(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						refreshViewer();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						if (!viewer.getTree().isDisposed())
						{
							LinkPersonAddress link = (LinkPersonAddress) entity;
							viewer.add(link.getPerson(), link);
							packColumns();
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						refreshViewer();
						packColumns();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		});
		EntityMediator.addListener(Address.class, new EntityAdapter()
		{
			@Override
			public void postDelete(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						refreshViewer();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						Address address = (Address) entity;
						if (address.getPersonLinks().size() == 0)
						{
							ContentRoot root = (ContentRoot) viewer.getInput();
							viewer.add(root, entity);
							packColumns();
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						refreshViewer(entity);
						packColumns();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		});
		EntityMediator.addListener(Member.class, new EntityAdapter()
		{
			@Override
			public void postDelete(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						refreshViewer();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						refreshViewer();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				UIJob job = new UIJob("")
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						refreshViewer();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
		});
	}

	private void packColumns()
	{
		if (!this.viewer.getTree().isDisposed())
		{
			TreeColumn[] treeColumns = this.viewer.getTree().getColumns();
			for (TreeColumn treeColumn : treeColumns)
			{
				treeColumn.pack();
			}
		}
	}

	private void refreshViewer()
	{
		if (!this.viewer.getTree().isDisposed())
		{
			this.viewer.refresh();
		}
	}

	private void refreshViewer(AbstractEntity entity)
	{
		if (!this.viewer.getTree().isDisposed())
		{
			this.viewer.refresh(entity);
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus()
	{
		this.searcher.setFocus();
	}

	public class ContentRoot
	{
		private final AbstractEntity[] entities;

		public ContentRoot()
		{
			entities = new AbstractEntity[0];
		}

		public ContentRoot(final AbstractEntity[] entities)
		{
			this.entities = entities;
		}

		public AbstractEntity[] getEntities()
		{
			return entities;
		}
	}

}