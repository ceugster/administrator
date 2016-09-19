package ch.eugster.events.member.views;

import java.text.SimpleDateFormat;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.Person;
import ch.eugster.events.person.views.PersonView;
import ch.eugster.events.ui.views.AbstractEntityView;

public class MemberView extends AbstractEntityView implements ISelectionListener
{
	private TableViewer viewer;

	@Override
	public void init(IViewSite site) throws PartInitException
	{
		super.init(site);
		EntityMediator.addListener(Person.class, this);
		site.getPage().addSelectionListener(PersonView.ID, this);
	}

	@Override
	public void createPartControl(Composite parent)
	{
		TableLayout layout = new TableLayout();

		Table table = new Table(parent, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLayout(layout);
		table.setHeaderVisible(true);

		this.viewer = new TableViewer(table);
		this.viewer.setContentProvider(new MemberContentProvider());
		this.viewer.setSorter(new MemberSorter());
		this.viewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
		this.viewer.addDoubleClickListener(new IDoubleClickListener()
		{

			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				// TODO Auto-generated method stub

			}
		});

		TableViewerColumn tableViewerColumn = new TableViewerColumn(this.viewer, SWT.NONE);
		tableViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				Object object = cell.getElement();
				if (object instanceof Member)
				{
					String date = "";
					Member member = (Member) object;
					if (member.getDate() != null)
					{
						date = SimpleDateFormat.getInstance().format(member.getDate().getTime());
					}
					cell.setText(date);
				}
			}
		});
		TableColumn tableColumn = tableViewerColumn.getColumn();
		tableColumn.setResizable(true);
		tableColumn.setText("Datum");

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.viewer);
	}

	public void setInput(Object inputElement)
	{
		if (this.viewer != null)
		{
			this.viewer.setInput(inputElement);
			this.packColumns();
		}
	}

	public void packColumns()
	{
		TableColumn[] columns = this.viewer.getTable().getColumns();
		for (TableColumn column : columns)
			column.pack();
	}

	private void createContextMenu()
	{
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(this.viewer.getControl());
		this.viewer.getControl().setMenu(menu);

		this.getSite().registerContextMenu(menuManager, this.viewer);
	}

	@Override
	public void setFocus()
	{
		if (this.viewer != null)
			this.viewer.getTable().setFocus();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Person.class, this);
		this.getSite().getPage().removeSelectionListener(PersonView.ID, this);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection)
	{
		if (part instanceof PersonView)
		{
			PersonView view = (PersonView) part;
			if (!view.getViewer().getSelection().isEmpty())
			{
				StructuredSelection ssel = (StructuredSelection) view.getViewer().getSelection();
				if (ssel.size() == 1)
				{
					this.setInput(ssel.getFirstElement());
				}
				else
					this.setInput(null);
			}
		}
		// TODO Auto-generated method stub

	}

}
