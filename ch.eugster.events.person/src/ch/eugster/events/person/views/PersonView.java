package ch.eugster.events.person.views;

import java.util.Locale;

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
import org.eclipse.nebula.widgets.formattedtext.MaskFormatter;
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

import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedPersonAndAddressFilter;
import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonSettings;
import ch.eugster.events.person.Activator;
import ch.eugster.events.person.editors.AddressEditor;
import ch.eugster.events.person.editors.AddressEditorInput;
import ch.eugster.events.person.editors.EditorSelector;
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
		sorter.setCurrentColumn(dialogSettings.getInt("order.by"));
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
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					if (person.isDeleted())
					{
						if (person.isMember())
						{
							cell.setText(PersonFormatter.getInstance().formatId(person) + "*");
							cell.setImage(Activator.getDefault().getImageRegistry()
									.get(Activator.KEY_PERSON_GREY_WITH_STAR));
						}
						else
						{
							cell.setText(PersonFormatter.getInstance().formatId(person));
							cell.setImage(Activator.getDefault().getImageRegistry().get(Activator.KEY_PERSON_GREY));
						}
						cell.setBackground(deletedColor);
					}
					else
					{
						if (person.isMember())
						{
							cell.setText(PersonFormatter.getInstance().formatId(person) + "*");
							cell.setImage(Activator.getDefault().getImageRegistry()
									.get(Activator.KEY_PERSON_BLUE_WITH_STAR));
						}
						else
						{
							cell.setText(PersonFormatter.getInstance().formatId(person));
							cell.setImage(Activator.getDefault().getImageRegistry().get(Activator.KEY_PERSON_BLUE));
						}
						cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
					}
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					if (address.isDeleted())
					{
						if (address.isMember())
						{
							cell.setText(AddressFormatter.getInstance().formatId(address) + "*");
						}
						else
						{
							cell.setText(AddressFormatter.getInstance().formatId(address));
						}
						cell.setImage(Activator.getDefault().getImageRegistry().get(Activator.KEY_ADDRESS_GREY));
						cell.setBackground(deletedColor);
					}
					else
					{
						if (address.isMember())
						{
							cell.setText(AddressFormatter.getInstance().formatId(address) + "*");
						}
						else
						{
							cell.setText(AddressFormatter.getInstance().formatId(address));
						}
						cell.setImage(Activator.getDefault().getImageRegistry().get(Activator.KEY_ADDRESS_MAIN));
						cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
					}
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					if (link.getAddress().isDeleted())
					{
						if (link.getAddress().isMember())
						{
							cell.setText(AddressFormatter.getInstance().formatId(link.getAddress()) + "*");
						}
						else
						{
							cell.setText(AddressFormatter.getInstance().formatId(link.getAddress()));
						}
						cell.setBackground(deletedColor);
					}
					else
					{
						if (link.getAddress().isMember())
						{
							cell.setText(AddressFormatter.getInstance().formatId(link.getAddress()) + "*");
						}
						else
						{
							cell.setText(AddressFormatter.getInstance().formatId(link.getAddress()));
						}
						cell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
					}
				}
			}
		});
		TreeColumn treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Code");
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
				sorter.setCurrentColumn(0);
				PersonView.this.dialogSettings.put("order.by", sorter.getCurrentColumn());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.viewer.refresh();
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
					cell.setText(person.getLastname());
					deleted = person.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					deleted = address.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(link.getPerson().getLastname());
					deleted = link.isDeleted();
				}
				cell.setImage(null);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Nachname");
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
				sorter.setCurrentColumn(1);
				PersonView.this.dialogSettings.put("order.by", sorter.getCurrentColumn());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.viewer.refresh();
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
					deleted = person.isDeleted();
					cell.setText(person.getFirstname());
					cell.setImage(null);
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					deleted = link.isDeleted();
					cell.setText(link.getPerson().getFirstname());
					cell.setImage(null);
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					cell.setText("");
					cell.setImage(null);
					deleted = address.isDeleted();
				}
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Vorname");
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
				sorter.setCurrentColumn(2);
				PersonView.this.dialogSettings.put("order.by", sorter.getCurrentColumn());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.viewer.refresh();
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
					address = person.getDefaultLink().getAddress();
					image = person.getDefaultLink().getAddressType().getImage();
					deleted = person.isDeleted();
				}
				else if (object instanceof Address)
				{
					address = (Address) object;
					image = Activator.getDefault().getImageRegistry().get(Activator.KEY_ADDRESS);
					deleted = address.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					address = link.getAddress();
					image = link.getAddressType().getImage();
					deleted = link.isDeleted();
				}
				cell.setText(AddressFormatter.getInstance().formatId(address));
				cell.setImage(image);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Adresse");
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
				sorter.setCurrentColumn(3);
				PersonView.this.dialogSettings.put("order.by", sorter.getCurrentColumn());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.viewer.refresh();
			}
		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				String name = "";
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					name = person.getDefaultLink().getAddress().getName();
					deleted = person.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					name = address.getName();
					deleted = address.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					name = link.getAddress().getName();
					deleted = link.isDeleted();
				}
				cell.setText(name);
				cell.setImage(null);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Organisation");
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
				sorter.setCurrentColumn(3);
				PersonView.this.dialogSettings.put("order.by", sorter.getCurrentColumn());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.viewer.refresh();
			}
		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				boolean deleted = false;
				String address = "";
				Object object = cell.getElement();
				if (object instanceof Person)
				{
					Person person = (Person) object;
					address = person.getDefaultLink() == null ? "" : AddressFormatter.getInstance().formatAddressLine(
							person.getDefaultLink().getAddress());
					deleted = person.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address a = (Address) object;
					address = AddressFormatter.getInstance().formatAddressLine(a);
					deleted = a.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					address = AddressFormatter.getInstance().formatAddressLine(link.getAddress());
					deleted = link.isDeleted();
				}
				cell.setText(address);
				cell.setImage(null);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Strasse");
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
				sorter.setCurrentColumn(4);
				PersonView.this.dialogSettings.put("order.by", sorter.getCurrentColumn());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.viewer.refresh();
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
					cell.setText(person.getDefaultLink() == null ? "" : AddressFormatter.getInstance().formatCityLine(
							person.getDefaultLink().getAddress()));
					cell.setImage(null);
					deleted = person.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					cell.setText(AddressFormatter.getInstance().formatCityLine(address));
					cell.setImage(null);
					deleted = address.isDeleted();
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					cell.setText(AddressFormatter.getInstance().formatCityLine(link.getAddress()));
					cell.setImage(null);
					deleted = link.isDeleted();
				}
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Ort");
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
				sorter.setCurrentColumn(5);
				PersonView.this.dialogSettings.put("order.by", sorter.getCurrentColumn());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.viewer.refresh();
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
					deleted = person.isDeleted();
					try
					{
						if (person.getPhone().isEmpty())
						{
							cell.setText("");
						}
						else
						{
							if (person.getCountry() != null)
							{
								Country country = PersonFormatter.getInstance().getCountry();
								if (country == null)
								{
									Locale locale = Locale.getDefault();
									MaskFormatter formatter = new MaskFormatter(person.getCountry().getPhonePattern());
									formatter.setValue(person.getPhone());
									cell.setText(locale.getCountry().equals(person.getCountry().getIso3166alpha2()) ? formatter
											.getDisplayString() : person.getCountry().getPhonePrefix() + " "
											+ formatter.getDisplayString());
								}
								else
								{
									MaskFormatter formatter = new MaskFormatter(person.getCountry().getPhonePattern());
									formatter.setValue(person.getPhone());
									cell.setText(country.getId().equals(person.getCountry().getId()) ? formatter
											.getDisplayString() : person.getCountry().getPhonePrefix() + " "
											+ formatter.getDisplayString());
								}
							}
							else
							{
								cell.setText(person.getPhone());
							}
						}
					}
					catch (NumberFormatException e)
					{
						cell.setText("");
					}
					cell.setImage(null);
				}
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Handy");
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
				sorter.setCurrentColumn(6);
				PersonView.this.dialogSettings.put("order.by", sorter.getCurrentColumn());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.viewer.refresh();
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
				String phone = "";
				Country country = null;
				if (object instanceof Person)
				{
					Person person = (Person) object;
					deleted = person.isDeleted();
					LinkPersonAddress link = person.getDefaultLink();
					if (link == null)
					{
						country = person.getCountry();
					}
					else
					{
						phone = link.getPhone().isEmpty() ? link.getAddress().getPhone() : link.getPhone();
						country = link.getAddress().getCountry() == null ? person.getCountry() : link.getAddress()
								.getCountry();
					}
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					phone = link.getPhone().isEmpty() ? link.getAddress().getPhone() : link.getPhone();
					country = link.getAddress().getCountry();
					deleted = link.isDeleted();
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					phone = address.getPhone();
					country = address.getCountry();
					deleted = address.isDeleted();
				}
				if (phone.isEmpty())
				{
					cell.setText("");
				}
				else
				{
					if (country != null)
					{
						Country defaultCountry = AddressFormatter.getInstance().getCountry();
						if (defaultCountry == null)
						{
							Locale locale = Locale.getDefault();
							MaskFormatter formatter = new MaskFormatter(country.getPhonePattern());
							formatter.setValue(phone);
							String p = country.getIso3166alpha2().equals(locale.getCountry()) ? formatter
									.getDisplayString() : country.getPhonePrefix() + " " + formatter.getDisplayString();
							cell.setText(p);
						}
						else
						{
							MaskFormatter formatter = new MaskFormatter(country.getPhonePattern());
							formatter.setValue(phone);
							String p = country.getId().equals(defaultCountry.getId()) ? formatter.getDisplayString()
									: country.getPhonePrefix() + " " + formatter.getDisplayString();
							cell.setText(p);
						}
					}
					else
					{
						cell.setText(phone);
					}
					cell.setImage(null);
				}
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Telefon");
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
				sorter.setCurrentColumn(7);
				PersonView.this.dialogSettings.put("order.by", sorter.getCurrentColumn());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.viewer.refresh();
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
				String email = "";
				if (object instanceof Person)
				{
					Person person = (Person) object;
					deleted = person.isDeleted();
					email = person.getEmail();
					if (email.isEmpty())
					{
						LinkPersonAddress link = person.getDefaultLink();
						if (link != null)
						{
							email = link.getEmail().isEmpty() ? link.getAddress().getEmail() : link.getEmail();
						}
					}
				}
				else if (object instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) object;
					if (link != null)
					{
						email = link.getEmail().isEmpty() ? link.getAddress().getEmail() : link.getEmail();
						deleted = link.isDeleted();
					}
				}
				else if (object instanceof Address)
				{
					Address address = (Address) object;
					email = address.getEmail();
					deleted = address.isDeleted();
				}
				cell.setText(email.isEmpty() ? "" : email);
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Email");
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
				sorter.setCurrentColumn(8);
				PersonView.this.dialogSettings.put("order.by", sorter.getCurrentColumn());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.viewer.refresh();
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
					cell.setText(person.getDomain() == null ? "" : person.getDomain().getCode());
					cell.setImage(null);
					deleted = person.isDeleted();
				}
				cell.setBackground(deleted ? deletedColor : Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
			}
		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Domäne");
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
				sorter.setCurrentColumn(9);
				PersonView.this.dialogSettings.put("order.by", sorter.getCurrentColumn());
				PersonView.this.dialogSettings.put("order.asc", sorter.isAscending());
				PersonView.this.viewer.refresh();
			}
		});

		Composite info = new Composite(parent, SWT.NONE);
		info.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		info.setLayout(new GridLayout(3, true));

		this.showDeleted = new Button(info, SWT.CHECK);
		this.showDeleted.setText("Entfernte Personen zeigen");
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
				viewer.refresh();
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
				PersonView.this.selected.setText("Ausgewählt: " + ((StructuredSelection) event.getSelection()).size());
			}
		});

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.viewer);

		this.searcher.initialize();
	}

	@Override
	public void dispose()
	{
		IContextService ctxService = (IContextService) getSite().getService(IContextService.class);
		ctxService.deactivateContext(ctxActivation);
		deletedColor.dispose();
		searcher.dispose();
		super.dispose();
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof Person)
		{
			Person person = (Person) object;
			this.editLink(person.getDefaultLink());
		}
		else if (object instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) object;
			this.editLink(link.getPerson().getDefaultLink());
		}
		else if (object instanceof Address)
		{
			this.editAddress((Address) object);
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
				viewer.refresh();
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				ContentRoot root = (ContentRoot) viewer.getInput();
				viewer.add(root, entity);
				packColumns();
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				viewer.refresh(entity);
				packColumns();
			}
		});
		EntityMediator.addListener(LinkPersonAddress.class, new EntityAdapter()
		{
			@Override
			public void postDelete(final AbstractEntity entity)
			{
				viewer.refresh();
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				LinkPersonAddress link = (LinkPersonAddress) entity;
				viewer.add(link.getPerson(), link);
				packColumns();
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				viewer.refresh(entity);
				packColumns();
			}
		});
		EntityMediator.addListener(Address.class, new EntityAdapter()
		{
			@Override
			public void postDelete(final AbstractEntity entity)
			{
				viewer.refresh();
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				Address address = (Address) entity;
				if (address.getPersonLinks().size() == 0)
				{
					ContentRoot root = (ContentRoot) viewer.getInput();
					viewer.add(root, entity);
					packColumns();
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				viewer.refresh(entity);
				packColumns();
			}
		});
	}

	private void packColumns()
	{
		TreeColumn[] treeColumns = this.viewer.getTree().getColumns();
		for (TreeColumn treeColumn : treeColumns)
		{
			treeColumn.pack();
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