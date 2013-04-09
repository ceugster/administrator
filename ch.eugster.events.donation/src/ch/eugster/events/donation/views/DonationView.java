package ch.eugster.events.donation.views;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.donation.Activator;
import ch.eugster.events.donation.editors.DonationEditor;
import ch.eugster.events.donation.editors.DonationEditorInput;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.DonationFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.Donation;
import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.model.DonationYear;
import ch.eugster.events.persistence.queries.DonationQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.views.AbstractEntityView;

public class DonationView extends AbstractEntityView implements IDoubleClickListener
{
	public static final String ID = "ch.eugster.events.donation.view";

	private TreeViewer viewer;

	private static DateFormat df = SimpleDateFormat.getDateInstance();

	private static NumberFormat nf;

	private ServiceTracker connectionServiceTracker;

	private ConnectionService connectionService;

	private IDialogSettings settings;

	private CDateTime startSelectionDate;

	private CDateTime endSelectionDate;

	public DonationView()
	{
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
	public void createPartControl(final Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));

		Calendar calendar = GregorianCalendar.getInstance();
		long timeInMillis = settings.getLong("start.selection.date");
		if (timeInMillis == 0)
		{
			calendar.set(Calendar.MONTH, Calendar.JANUARY);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			timeInMillis = calendar.getTimeInMillis();
		}
		calendar.setTimeInMillis(timeInMillis);
		startSelectionDate = new CDateTime(composite, CDT.DATE_MEDIUM);
		startSelectionDate.setSelection(calendar.getTime());

		calendar = GregorianCalendar.getInstance();
		timeInMillis = settings.getLong("end.selection.date");
		if (timeInMillis == 0)
		{
			calendar.set(Calendar.MONTH, Calendar.DECEMBER);
			calendar.set(Calendar.DAY_OF_MONTH, 31);
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			calendar.set(Calendar.MILLISECOND, 999);
			timeInMillis = calendar.getTimeInMillis();
		}
		calendar.setTimeInMillis(timeInMillis);
		endSelectionDate = new CDateTime(composite, CDT.DATE_MEDIUM);
		endSelectionDate.setSelection(calendar.getTime());

		final Tree tree = new Tree(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
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
							DonationView.this.packColumns();
						}
					});
				}
			}
		});

		DeletedEntityFilter deletedFilter = new DeletedEntityFilter();
		ViewerFilter[] filters = new ViewerFilter[] { deletedFilter };

		final DonationSorter sorter = new DonationSorter();

		this.viewer = new TreeViewer(tree);
		this.viewer.setContentProvider(new DonationTreeViewerContentProvider());
		this.viewer.setFilters(filters);
		this.viewer.addDoubleClickListener(this);
		this.viewer.setSorter(sorter);

		TreeViewerColumn treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof DonationYear)
				{
					DonationYear donationYear = (DonationYear) cell.getElement();
					cell.setImage(Activator.getDefault().getImageRegistry().get("MONEY"));
					cell.setText(Integer.valueOf(donationYear.getYear()).toString());
				}
				else if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					cell.setImage(Activator.getDefault().getImageRegistry().get("MONEY"));
					cell.setText(df.format(donation.getDonationDate().getTime()));
				}
			}

		});
		TreeColumn treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Datum");
		treeColumn.setResizable(true);
		treeColumn.setData(Integer.valueOf(0));
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TreeColumn column = (TreeColumn) e.getSource();
				int col = ((Integer) column.getData()).intValue();
				if (sorter.column == col)
				{
					sorter.asc = !sorter.asc;
				}
				else
				{
					sorter.column = col;
				}
				viewer.refresh();
			}

		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.RIGHT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					cell.setText(nf.format(donation.getAmount()));
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Betrag");
		treeColumn.setResizable(true);
		treeColumn.setData(Integer.valueOf(1));
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TreeColumn column = (TreeColumn) e.getSource();
				int col = ((Integer) column.getData()).intValue();
				if (sorter.column == col)
				{
					sorter.asc = !sorter.asc;
				}
				else
				{
					sorter.column = col;
				}
				viewer.refresh();
			}

		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					DonationPurpose purpose = donation.getPurpose();
					cell.setText(purpose.getCode().isEmpty() ? purpose.getName() : purpose.getCode() + " - "
							+ purpose.getName());
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Zweck");
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					cell.setText(DonationFormatter.getInstance().formatDonatorName(donation));
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Name");
		treeColumn.setResizable(true);
		treeColumn.setData(Integer.valueOf(3));
		treeColumn.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				TreeColumn column = (TreeColumn) e.getSource();
				int col = ((Integer) column.getData()).intValue();
				if (sorter.column == col)
				{
					sorter.asc = !sorter.asc;
				}
				else
				{
					sorter.column = col;
				}
				viewer.refresh();
			}

		});

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					cell.setText(DonationFormatter.getInstance().formatDonatorAddress(donation));
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Strasse");
		treeColumn.setResizable(true);

		treeViewerColumn = new TreeViewerColumn(this.viewer, SWT.LEFT);
		treeViewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(final ViewerCell cell)
			{
				if (cell.getElement() instanceof Donation)
				{
					Donation donation = (Donation) cell.getElement();
					cell.setText(DonationFormatter.getInstance().formatDonatorCityLine(donation));
				}
			}

		});
		treeColumn = treeViewerColumn.getColumn();
		treeColumn.setText("Ort");
		treeColumn.setResizable(true);

		this.createContextMenu();

		this.getSite().setSelectionProvider(this.viewer);

		connectionServiceTracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public Object addingService(final ServiceReference reference)
			{
				connectionService = (ConnectionService) super.addingService(reference);
				UIJob job = new UIJob("Spenden aktualisieren")
				{
					@Override
					public IStatus runInUIThread(final IProgressMonitor monitor)
					{
						viewer.setInput(connectionService);
						DonationYear currentYear = new DonationYear(GregorianCalendar.getInstance().get(Calendar.YEAR));
						viewer.expandToLevel(currentYear, 3);
						packColumns();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
				return connectionService;
			}

			@Override
			public void removedService(final ServiceReference reference, final Object service)
			{
				connectionService = null;
				Display display = Display.getCurrent();
				if (display == null)
				{
					display = Display.getDefault();
				}
				display.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						if (viewer.getContentProvider() != null)
						{
							viewer.setInput(null);
						}
					}
				});
				super.removedService(reference, service);
			}

		};
		connectionServiceTracker.open();
	}

	@Override
	public void dispose()
	{
		EntityMediator.removeListener(Donation.class, this);
		super.dispose();
	}

	@Override
	public void doubleClick(final DoubleClickEvent event)
	{
		ISelection selection = event.getSelection();
		Object object = ((IStructuredSelection) selection).getFirstElement();
		if (object instanceof Donation)
		{
			this.editDonation((Donation) object);
		}
	}

	private void editDonation(final Donation donation)
	{
		try
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.openEditor(new DonationEditorInput(donation), DonationEditor.ID, true);
		}
		catch (PartInitException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void init(final IViewSite site) throws PartInitException
	{
		super.init(site);
		EntityMediator.addListener(Donation.class, this);
		nf = DecimalFormat.getNumberInstance();
		nf.setMinimumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
		nf.setMaximumFractionDigits(Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
	}

	private void packColumns()
	{
		TreeColumn[] columns = this.viewer.getTree().getColumns();
		for (TreeColumn column : columns)
			column.pack();
	}

	@Override
	public void postDelete(final AbstractEntity entity)
	{
		if (entity instanceof Donation)
		{
			Donation donation = (Donation) entity;
			DonationYear year = new DonationYear(donation.getDonationYear());
			this.viewer.refresh(year);
		}
	}

	@Override
	public void postPersist(final AbstractEntity entity)
	{
		if (entity instanceof Donation)
		{
			Donation donation = (Donation) entity;
			DonationYear year = new DonationYear(donation.getDonationYear());
			this.viewer.add(year, donation.getDonationYear());
		}
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
		if (entity instanceof Donation)
		{
			this.viewer.refresh(entity);
		}
	}

	@Override
	public void setFocus()
	{
		this.viewer.getTree().setFocus();
	}

	private class DonationSorter extends ViewerSorter
	{
		private boolean asc = true;

		private int column = 0;

		@Override
		public int compare(final Viewer viewer, final Object e1, final Object e2)
		{
			if (e1 instanceof DonationYear && e2 instanceof DonationYear)
			{
				DonationYear d1 = (DonationYear) e1;
				DonationYear d2 = (DonationYear) e2;

				return Integer.valueOf(d2.getYear()).compareTo(Integer.valueOf(d1.getYear()));
			}
			else if (e1 instanceof Donation && e2 instanceof Donation)
			{
				Donation d1 = (Donation) e1;
				Donation d2 = (Donation) e2;

				switch (column)
				{
					case 0:
					{
						Date dd1 = d1.getDonationDate().getTime();
						Date dd2 = d2.getDonationDate().getTime();
						return compareDates(dd1, dd2);
					}
					case 1:
					{
						return compareAmounts(Double.valueOf(d1.getAmount()), Double.valueOf(d2.getAmount()));
					}
					case 3:
					{
						String name1 = DonationFormatter.getInstance().formatDonatorName(d1);
						String name2 = DonationFormatter.getInstance().formatDonatorName(d2);
						return compareStrings(name1, name2);
					}
					default:
					{
						return 0;
					}
				}
			}
			return 0;
		}

		private int compareAmounts(final Double amount1, final Double amount2)
		{
			if (asc)
			{
				return amount1.compareTo(amount2);
			}
			else
			{
				return amount2.compareTo(amount1);
			}
		}

		private int compareDates(final Date date1, final Date date2)
		{
			if (asc)
			{
				return date1.compareTo(date2);
			}
			else
			{
				return date2.compareTo(date1);
			}
		}

		private int compareStrings(final String s1, final String s2)
		{
			if (asc)
			{
				return s1.compareTo(s2);
			}
			else
			{
				return s2.compareTo(s1);
			}
		}
	}

	private class DonationTreeViewerContentProvider implements ITreeContentProvider
	{
		private ConnectionService connectionService;

		public DonationTreeViewerContentProvider()
		{
		}

		@Override
		public void dispose()
		{
		}

		@Override
		public Object[] getChildren(final Object parentElement)
		{
			if (parentElement instanceof ConnectionService)
			{
				connectionService = (ConnectionService) parentElement;
				DonationQuery query = (DonationQuery) connectionService.getQuery(Donation.class);
				Collection<DonationYear> years = query.selectYears();
				return years.toArray(new DonationYear[0]);
			}
			else if (parentElement instanceof DonationYear)
			{
				DonationYear year = (DonationYear) parentElement;
				return year.getDonations().toArray(new Donation[0]);
			}
			return new Donation[0];
		}

		@Override
		public Object[] getElements(final Object inputElement)
		{
			return this.getChildren(inputElement);
		}

		@Override
		public Object getParent(final Object element)
		{
			if (element instanceof Donation)
			{
				Donation donation = (Donation) element;
				return Integer.valueOf(donation.getDonationDate().get(Calendar.YEAR));
			}
			return null;
		}

		@Override
		public boolean hasChildren(final Object element)
		{
			if (element instanceof ConnectionService)
			{
				ConnectionService service = (ConnectionService) element;
				DonationQuery query = (DonationQuery) service.getQuery(Donation.class);
				Collection<DonationYear> years = query.selectYears();
				return years.size() > 0;
			}
			else if (element instanceof DonationYear)
			{
				return ((DonationYear) element).getDonations().size() > 0;
			}
			return false;
		}

		@Override
		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
		{
		}

	}
}
