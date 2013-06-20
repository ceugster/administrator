package ch.eugster.events.merge.views;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.merge.Activator;
import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Guide;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.persistence.model.PersonForm;
import ch.eugster.events.persistence.model.PersonSex;
import ch.eugster.events.persistence.queries.PersonQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class PersonView extends ViewPart
{
	public static final String ID = "ch.eugster.events.merge.view";

	private TableViewer viewer;

	private Text lastname;

	private Text firstname;

	private Text address;

	private EntityAdapter entityAdapter;

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(this.viewer.getControl());
		this.viewer.getControl().setMenu(menu);

		this.getSite().registerContextMenu(menuManager, this.viewer);
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		Label label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Filter (Nachname)");

		lastname = new Text(composite, SWT.BORDER);
		lastname.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lastname.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setViewerInput();
			}
		});

		label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Filter (Vorname)");

		firstname = new Text(composite, SWT.BORDER);
		firstname.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		firstname.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setViewerInput();
			}
		});

		label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Filter (Strasse)");

		address = new Text(composite, SWT.BORDER);
		address.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		address.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				setViewerInput();
			}
		});

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;

		Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);

		viewer = new TableViewer(table);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setSorter(new TableSorter());
		viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });

		TableColumn tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_ID.ordinal());
		tableColumn.setText("Id");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_ID);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		TableViewerColumn tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getPerson().getId().toString());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.L_ID.ordinal());
		tableColumn.setText("Link");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.L_ID);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getId().toString());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.A_ID.ordinal());
		tableColumn.setText("Adresse");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.A_ID);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getAddress().getId().toString());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_BIRTHDATE.ordinal());
		tableColumn.setText("Geburtsdatum");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_BIRTHDATE);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				if (link.getPerson().getBirthdate() == null)
				{
					cell.setText("");
				}
				else if (link.getPerson().getBirthdate().longValue() < 1000L
						&& link.getPerson().getBirthdate().longValue() >= 0)
				{
					cell.setText(link.getPerson().getBirthdate().toString());
				}
				else
				{
					Calendar calendar = GregorianCalendar.getInstance();
					calendar.setTimeInMillis(link.getPerson().getBirthdate().longValue());
					cell.setText(SimpleDateFormat.getDateInstance().format(calendar.getTime()));
				}
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_SEX.ordinal());
		tableColumn.setText("Geschlecht");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_SEX);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				PersonSex sex = link.getPerson().getSex();
				if (sex == null)
				{
					cell.setText("");
				}
				else
				{
					cell.setText(sex.getSymbol());
				}
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_FORM.ordinal());
		tableColumn.setText("Anredeform");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_FORM);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				PersonForm form = link.getPerson().getForm();
				if (form == null)
				{
					cell.setText("");
				}
				else
				{
					cell.setText(form.toString());
				}
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_SALUTATION.ordinal());
		tableColumn.setText("Anrede");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_SALUTATION);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
					{
						if (link.getPerson().getSex() != null)
						{
							cell.setText(link.getPerson().getSex().getSalutation());
						}
					}
				}
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_TITLE.ordinal());
		tableColumn.setText("Titel");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_TITLE);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				if (link.getPerson().getTitle() == null)
				{
					cell.setText("");
				}
				else
				{
					cell.setText(link.getPerson().getTitle().getTitle());
				}
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_FIRSTNAME.ordinal());
		tableColumn.setText("Vorname");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_FIRSTNAME);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getPerson().getFirstname());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_LASTNAME.ordinal());
		tableColumn.setText("Nachname");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_LASTNAME);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getPerson().getLastname());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_ANOTHERLINE.ordinal());
		tableColumn.setText("Zusatzzeile");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_ANOTHERLINE);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getAddress().getAnotherLine());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_PROFESSION.ordinal());
		tableColumn.setText("Beruf");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_PROFESSION);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getPerson().getProfession());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_PHONE.ordinal());
		tableColumn.setText("Telefon");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_PHONE);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getPerson().getPhone());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_EMAIL.ordinal());
		tableColumn.setText("Email");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_EMAIL);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getPerson().getEmail());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.P_WEBSITE.ordinal());
		tableColumn.setText("Website");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.P_WEBSITE);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getPerson().getWebsite());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.L_FUNCTION.ordinal());
		tableColumn.setText("Funktion");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.L_FUNCTION);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getFunction());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.L_PHONE.ordinal());
		tableColumn.setText("2. Telefon");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.L_PHONE);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getPhone());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.L_EMAIL.ordinal());
		tableColumn.setText("2. Email");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.L_EMAIL);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getEmail());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.L_PARTICIPANT.ordinal());
		tableColumn.setText("Kurse");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.L_PARTICIPANT);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(NumberFormat.getIntegerInstance().format(link.getParticipants().size()));
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.L_ADDRESS_GROUP_MEMBER.ordinal());
		tableColumn.setText("Gruppen");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.L_ADDRESS_GROUP_MEMBER);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(NumberFormat.getIntegerInstance().format(link.getAddressGroupMembers().size()));
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.L_MEMBER.ordinal());
		tableColumn.setText("Mitglied");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.L_MEMBER);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(NumberFormat.getIntegerInstance().format(link.getMembers().size()));
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.L_DONATION.ordinal());
		tableColumn.setText("Spende");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.L_DONATION);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(NumberFormat.getIntegerInstance().format(link.getDonations().size()));
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.L_GUIDE.ordinal());
		tableColumn.setText("Leiter");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.L_GUIDE);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				Guide guide = link.getGuide();
				cell.setText(guide == null ? "Nein" : "Ja");
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.A_SALUTATION.ordinal());
		tableColumn.setText("2. Anrede");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.A_SALUTATION);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
					if (link.getPerson().getSex() != null)
					{
						cell.setText(link.getPerson().getSex().getSalutation());
					}
				}
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.A_NAME.ordinal());
		tableColumn.setText("Organisation");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.A_NAME);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getAddress().getName());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.A_ANOTHERLINE.ordinal());
		tableColumn.setText("Adresse Zusatzzeile");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.A_ANOTHERLINE);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getAddress().getAnotherLine());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.A_ADDRESS.ordinal());
		tableColumn.setText("Strasse");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.A_ADDRESS);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getAddress().getAddress());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.A_POB.ordinal());
		tableColumn.setText("Postfach");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.A_POB);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getAddress().getPob());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.A_COUNTRY.ordinal());
		tableColumn.setText("Land");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.A_COUNTRY);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				if (link.getAddress().getCountry() != null)
				{
					cell.setText(link.getAddress().getCountry().getIso3166alpha2());
				}
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.A_ZIP.ordinal());
		tableColumn.setText("PLZ");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.A_ZIP);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getAddress().getZip());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.A_CITY.ordinal());
		tableColumn.setText("Ort");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.A_CITY);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getAddress().getCity());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.A_PHONE.ordinal());
		tableColumn.setText("Telefon");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.A_PHONE);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getAddress().getPhone());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.A_FAX.ordinal());
		tableColumn.setText("Fax");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.A_FAX);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getAddress().getFax());
			}
		});

		tableColumn = new TableColumn(table, SWT.LEFT, ColumnOrder.A_WEBSITE.ordinal());
		tableColumn.setText("2. Website");
		tableColumn.setResizable(true);
		tableColumn.setMoveable(true);
		tableColumn.setData("order", ColumnOrder.A_WEBSITE);
		tableColumn.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TableColumn tableColumn = (TableColumn) e.getSource();
				ColumnOrder order = (ColumnOrder) tableColumn.getData("order");
				TableSorter sorter = (TableSorter) viewer.getSorter();
				sorter.setColumnOrder(order);
				viewer.refresh();
			}

		});

		tableViewerColumn = new TableViewerColumn(viewer, tableColumn);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				LinkPersonAddress link = (LinkPersonAddress) cell.getElement();
				cell.setText(link.getAddress().getWebsite());
			}
		});

		this.createContextMenu();

		this.getSite().setSelectionProvider(viewer);
	}

	public TableViewer getViewer()
	{
		return viewer;
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);
		entityAdapter = new EntityAdapter()
		{
			@Override
			public void postDelete(final AbstractEntity entity)
			{
				if (entity instanceof Person || entity instanceof LinkPersonAddress)
				{
					viewer.refresh();
				}
			}

			@Override
			public void postPersist(final AbstractEntity entity)
			{
				if (entity instanceof LinkPersonAddress)
				{
					PersonView.this.viewer.add(entity);
				}
			}

			@Override
			public void postUpdate(final AbstractEntity entity)
			{
				if (entity instanceof Person)
				{
					viewer.refresh(entity);
				}
				else if (entity instanceof LinkPersonAddress)
				{
					PersonView.this.viewer.refresh(entity);
				}
			}
		};
		EntityMediator.addListener(LinkPersonAddress.class, entityAdapter);
	}

	@Override
	public void setFocus()
	{
		viewer.getTable().setFocus();
	}

	private void setViewerInput()
	{
		int count = 0;
		Map<String, String> criteria = new HashMap<String, String>();
		if (!lastname.getText().isEmpty())
		{
			criteria.put("lastname", lastname.getText());
			count = count + lastname.getText().length();
		}
		if (!firstname.getText().isEmpty())
		{
			criteria.put("firstname", firstname.getText());
			count = count + firstname.getText().length();
		}
		if (!address.getText().isEmpty())
		{
			criteria.put("address", address.getText());
			count = count + address.getText().length();
		}
		if (!criteria.isEmpty() && count > 2)
		{
			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			tracker.open();
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				PersonQuery query = (PersonQuery) service.getQuery(Person.class);
				Collection<Person> persons = query.selectByCriteria(criteria);
				Collection<LinkPersonAddress> links = new ArrayList<LinkPersonAddress>();
				for (Person person : persons)
				{
					links.addAll(person.getLinks());
				}
				viewer.setInput(links.toArray(new LinkPersonAddress[0]));
				TableColumn[] tableColumns = viewer.getTable().getColumns();
				for (TableColumn tableColumn : tableColumns)
				{
					tableColumn.pack();
				}
			}
			tracker.close();
		}
	}

	private enum ColumnOrder
	{
		P_ID, L_ID, A_ID, P_BIRTHDATE, P_SEX, P_FORM, P_SALUTATION, P_TITLE, P_FIRSTNAME, P_LASTNAME, P_ANOTHERLINE, P_PROFESSION, P_PHONE, P_EMAIL, P_WEBSITE, L_FUNCTION, L_PHONE, L_EMAIL, L_PARTICIPANT, L_ADDRESS_GROUP_MEMBER, L_MEMBER, L_DONATION, L_GUIDE, A_SALUTATION, A_NAME, A_ANOTHERLINE, A_ADDRESS, A_POB, A_COUNTRY, A_ZIP, A_CITY, A_PHONE, A_FAX, A_WEBSITE;
	}

	private class TableSorter extends ViewerSorter
	{
		private ColumnOrder order = ColumnOrder.P_ID;

		private boolean descending = false;

		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2)
		{
			LinkPersonAddress l1 = null;
			LinkPersonAddress l2 = null;

			if (descending)
			{
				l2 = (LinkPersonAddress) e1;
				l1 = (LinkPersonAddress) e2;
			}
			else
			{
				l1 = (LinkPersonAddress) e1;
				l2 = (LinkPersonAddress) e2;
			}

			switch (order)
			{
				case P_ID:
				{
					return l1.getPerson().getId().compareTo(l2.getPerson().getId());
				}
				case P_BIRTHDATE:
				{
					if (l1.getPerson().getBirthdate() == null && l1.getPerson().getBirthdate() == null)
					{
						return 0;
					}
					else if (l1.getPerson().getBirthdate() == null)
					{
						return 1;
					}
					else if (l2.getPerson().getBirthdate() == null)
					{
						return -1;
					}
					return l1.getPerson().getBirthdate().compareTo(l2.getPerson().getBirthdate());
				}
				case P_SEX:
				{
					return l1.getPerson().getSex().getSymbol().compareTo(l2.getPerson().getSex().getSymbol());
				}
				case P_FORM:
				{
					return l1.getPerson().getForm().compareTo(l2.getPerson().getForm());
				}
				case P_SALUTATION:
				{
					return l1.getPerson().getSex().getSalutation().compareTo(l2.getPerson().getSex().getSalutation());
				}
				case P_TITLE:
				{
					return l1.getPerson().getTitle().getTitle().compareTo(l2.getPerson().getTitle().getTitle());
				}
				case P_FIRSTNAME:
				{
					return l1.getPerson().getFirstname().compareTo(l2.getPerson().getFirstname());
				}
				case P_LASTNAME:
				{
					int value = l1.getPerson().getLastname().compareTo(l2.getPerson().getLastname());
					if (value == 0)
					{
						return l1.getPerson().getFirstname().compareTo(l2.getPerson().getFirstname());
					}
					else
					{
						return value;
					}
				}
				case P_ANOTHERLINE:
				{
					return l1.getAddress().getAnotherLine().compareTo(l2.getAddress().getAnotherLine());
				}
				case P_PROFESSION:
				{
					return l1.getPerson().getProfession().compareTo(l2.getPerson().getProfession());
				}
				case P_PHONE:
				{
					return l1.getPerson().getPhone().compareTo(l2.getPerson().getPhone());
				}
				case P_EMAIL:
				{
					return l1.getPerson().getEmail().compareTo(l2.getPerson().getEmail());
				}
				case P_WEBSITE:
				{
					return l1.getPerson().getWebsite().compareTo(l2.getPerson().getWebsite());
				}
				case L_ID:
				{
					return l1.getId().compareTo(l2.getId());
				}
				case L_FUNCTION:
				{
					return l1.getFunction().compareTo(l2.getFunction());
				}
				case L_PHONE:
				{
					return l1.getPhone().compareTo(l2.getPhone());
				}
				case L_EMAIL:
				{
					return l1.getEmail().compareTo(l2.getEmail());
				}
				case A_ID:
				{
					return l1.getAddress().getId().compareTo(l2.getAddress().getId());
				}
				case A_SALUTATION:
				{
					return l1.getAddress().getSalutation().getSalutation()
							.compareTo(l2.getAddress().getSalutation().getSalutation());
				}
				case A_NAME:
				{
					return l1.getAddress().getName().compareTo(l2.getAddress().getName());
				}
				case A_ANOTHERLINE:
				{
					return l1.getAddress().getAnotherLine().compareTo(l2.getAddress().getAnotherLine());
				}
				case A_ADDRESS:
				{
					return l1.getAddress().getAddress().compareTo(l2.getAddress().getAddress());
				}
				case A_POB:
				{
					return l1.getAddress().getPob().compareTo(l2.getAddress().getPob());
				}
				case A_COUNTRY:
				{
					return l1.getAddress().getCountry().getIso3166alpha2()
							.compareTo(l2.getAddress().getCountry().getIso3166alpha2());
				}
				case A_ZIP:
				{
					return l1.getAddress().getZip().compareTo(l2.getAddress().getZip());
				}
				case A_CITY:
				{
					return l1.getAddress().getCity().compareTo(l2.getAddress().getCity());
				}
				case A_PHONE:
				{
					return l1.getAddress().getPhone().compareTo(l2.getAddress().getPhone());
				}
				case A_FAX:
				{
					return l1.getAddress().getFax().compareTo(l2.getAddress().getFax());
				}
				case A_WEBSITE:
				{
					return l1.getAddress().getWebsite().compareTo(l2.getAddress().getWebsite());
				}
			}
			return 0;
		}

		public void setColumnOrder(final ColumnOrder order)
		{
			if (this.order.equals(order))
			{
				this.descending = !this.descending;
			}
			else
			{
				this.order = order;
			}
		}

	}
}
