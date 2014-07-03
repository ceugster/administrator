package ch.eugster.events.course.wizards;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.model.Compensation;
import ch.eugster.events.persistence.model.CompensationType;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.queries.CompensationTypeQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class CompensationTableViewerComposite extends Composite
{
	private static final String[] COLUMN_NAMES = new String[] { "Entschädigungsart", "Betrag" };

	private static final int[] COLUMN_WIDTHS = new int[] { 200, 30 };

	private static final int[] COLUMN_ALIGNMENTS = new int[] { SWT.LEFT, SWT.RIGHT };

	private CourseGuide courseGuide;

	private CompensationList compensationList;

	private ViewerComposite viewerComposite;
	
	private ButtonComposite buttonComposite;

	public CompensationTableViewerComposite(final Composite parent, final int style)
	{
		super(parent, style);
		this.createControls();
	}

	private ButtonComposite createButtonComposite(final Composite parent, final int style)
	{
		return new ButtonComposite(parent, style);
	}

	private void createControls()
	{
		this.compensationList = new CompensationList();

		GridLayout layout = new GridLayout(2, false);
		layout.marginTop = 0;
		layout.marginLeft = 0;
		layout.marginBottom = 0;
		layout.marginRight = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;

		this.setLayout(layout);
		this.setLayoutData(new GridData(GridData.FILL_BOTH));

		this.viewerComposite = this.createViewerComposite(this, SWT.NONE);
		this.buttonComposite = this.createButtonComposite(this, SWT.NONE);
		this.viewerComposite.getViewer().addSelectionChangedListener(new ISelectionChangedListener() 
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				buttonComposite.getRemoveButton().setEnabled(!event.getSelection().isEmpty());
			}
		});
	}

	private ViewerComposite createViewerComposite(final Composite parent, final int style)
	{
		return new ViewerComposite(parent, style);
	}

	public Collection<Compensation> getCompensations()
	{
		return this.compensationList.getCompensations();
	}

	public boolean isComplete()
	{
		return this.compensationList.isComplete();
	}

	public void setCourseGuide(final CourseGuide courseGuide)
	{
		this.courseGuide = courseGuide;
		this.compensationList.setCompensations(courseGuide);
	}

	public class ButtonComposite extends Composite implements ISelectionChangedListener
	{

		Button addButton;

		Button removeButton;

		public ButtonComposite(final Composite composite, final int style)
		{
			super(composite, style);
			this.createControls();
		}

		private void addAddButton(final Composite composite)
		{
			this.addButton = new Button(composite, SWT.PUSH);
			this.addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			this.addButton.setText("Hinzufügen");
			this.addButton.addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetDefaultSelected(final SelectionEvent event)
				{
					this.widgetSelected(event);
				}

				@Override
				public void widgetSelected(final SelectionEvent event)
				{
					Compensation compensation = new Compensation(CompensationTableViewerComposite.this.courseGuide);
					CompensationTableViewerComposite.this.courseGuide.addCompensation(compensation);
					CompensationTableViewerComposite.this.compensationList.addWrapper(new CWrapper(compensation));
				}
			});
			this.addButton.setEnabled((CompensationTableViewerComposite.this.viewerComposite
					.getCompensationTypeEntries().length > 0));
		}

		private void addRemoveButton(final Composite composite)
		{
			this.removeButton = new Button(composite, SWT.PUSH);
			this.removeButton.setText("Entfernen");
			this.removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			this.removeButton.addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetDefaultSelected(final SelectionEvent event)
				{
					this.widgetSelected(event);
				}

				@Override
				public void widgetSelected(final SelectionEvent event)
				{
					IStructuredSelection ssel = (IStructuredSelection) CompensationTableViewerComposite.this.viewerComposite
							.getViewer().getSelection();
					if (!ssel.isEmpty())
					{
						Object[] objects = ssel.toArray();
						for (Object object : objects)
						{
							CWrapper wrapper = (CWrapper) object;
							Compensation compensation = wrapper.getCompensation();
							compensation.setDeleted(true);
//							CompensationTableViewerComposite.this.compensationList.removeWrapper(wrapper);
						}
						CompensationTableViewerComposite.this.viewerComposite.getViewer().refresh();
						CompensationTableViewerComposite.this.viewerComposite.pack();
						CompensationTableViewerComposite.this.viewerComposite.layout();
					}
				}
			});
//			this.removeButton.setEnabled(false);
		}

		private void createControls()
		{
			this.setLayout(new GridLayout());
			this.setLayoutData(new GridData(GridData.FILL_VERTICAL));
			this.addAddButton(this);
			this.addRemoveButton(this);
		}

		public Button getAddButton()
		{
			return this.addButton;
		}

		public Button getRemoveButton()
		{
			return this.removeButton;
		}

		@Override
		public void selectionChanged(final SelectionChangedEvent event)
		{
			StructuredSelection ssel = (StructuredSelection) event.getSelection();

			this.removeButton.setEnabled(ssel.size() > 0);
		}
	}

	private class CompensationList
	{
		public static final long serialVersionUID = 100000001l;

		private Collection<CWrapper> wrappers = new ArrayList<CWrapper>();

		private final Collection<Listener> listeners = new ArrayList<Listener>();

		public void addListener(final Listener listener)
		{
			if (!this.listeners.contains(listener))
				this.listeners.add(listener);
		}

		public void addWrapper(final CWrapper wrapper)
		{
			this.wrappers.add(wrapper);
			for (Listener listener : this.listeners)
				listener.addWrapper(wrapper);
		}

		public void removeWrapper(final CWrapper wrapper)
		{
			this.wrappers.remove(wrapper);
			for (Listener listener : this.listeners)
			{
				listener.removeWrapper(wrapper);
			}
		}

		public Collection<Compensation> getCompensations()
		{
			Collection<Compensation> compensations = new ArrayList<Compensation>();
			for (CWrapper wrapper : this.wrappers)
				compensations.add(wrapper.getCompensation());

			return compensations;
		}

		public Collection<CWrapper> getWrappers()
		{
			return this.wrappers;
		}

		public boolean isComplete()
		{
			for (CWrapper wrapper : this.wrappers)
			{
				if (wrapper.getCompensation().getCompensationType() == null)
					return false;
			}
			return true;
		}

		public void removeListener(final Listener listener)
		{
			if (this.listeners.contains(listener))
				this.listeners.remove(listener);
		}

		public void setCompensations(final CourseGuide courseGuide)
		{
			this.wrappers = new ArrayList<CWrapper>();
			for (Compensation compensation : courseGuide.getCompensations())
			{
				this.wrappers.add(new CWrapper(compensation));
			}
			CompensationTableViewerComposite.this.viewerComposite.setInput(this);
			CompensationTableViewerComposite.this.buttonComposite.getRemoveButton().setEnabled(!viewerComposite.getViewer().getSelection().isEmpty());
		}
	}

	private class CompensationTableContentProvider implements IStructuredContentProvider, Listener
	{
		private TableViewer viewer = null;

		public CompensationTableContentProvider(final TableViewer viewer)
		{
			this.viewer = viewer;
		}

		@Override
		public void addWrapper(final CWrapper wrapper)
		{
			this.viewer.add(wrapper);
			CompensationTableViewerComposite.this.courseGuide.addCompensation(wrapper.getCompensation());
			CompensationTableViewerComposite.this.viewerComposite.pack();
		}

		@Override
		public void dispose()
		{
		}

		@Override
		public Object[] getElements(final Object object)
		{
			if (object instanceof CompensationList)
			{
				return CompensationTableViewerComposite.this.compensationList.getWrappers().toArray(new CWrapper[0]);
			}
			return new Object[] {};
		}

		@Override
		public void inputChanged(final Viewer v, final Object oldInput, final Object newInput)
		{
			if (newInput != null)
				((CompensationList) newInput).addListener(this);
			if (oldInput != null)
				((CompensationList) oldInput).removeListener(this);
		}

		@Override
		public void removeWrapper(final CWrapper wrapper)
		{
			this.viewer.remove(wrapper);
			CompensationTableViewerComposite.this.courseGuide.removeCompensation(wrapper.getCompensation());
		}

		@Override
		public void setWrappers(final CompensationList list)
		{
			this.viewer.setInput(list);
		}

		@Override
		public void updateWrapper(final CWrapper wrapper)
		{
			this.viewer.update(wrapper, null);
		}
	}

	private static class CWrapper
	{
		private final Compensation compensation;

		public CWrapper(final Compensation compensation)
		{
			this.compensation = compensation;
		}

		public Compensation getCompensation()
		{
			return this.compensation;
		}
	}

	private interface Listener
	{
		public void addWrapper(CWrapper wrapper);

		public void removeWrapper(CWrapper wrapper);

		public void setWrappers(CompensationList list);

		public void updateWrapper(CWrapper wrapper);
	}

	private class ViewerComposite extends Composite
	{
		private TableViewer viewer = null;

		private NumberFormat amountFormat = null;

		private CompensationType[] compensationTypes = null;

		private String[] entries = null;

		public ViewerComposite(final Composite parent, final int style)
		{
			super(parent, style);
			this.createControls(this);
		}

		private CellEditor createAmountCellEditor()
		{
			final TextCellEditor textEditor = new TextCellEditor(this.viewer.getTable());
			((Text) textEditor.getControl()).addVerifyListener(new VerifyListener()
			{
				@Override
				public void verifyText(final VerifyEvent event)
				{
					String temp = ((Text) textEditor.getControl()).getText();
					StringBuffer test = new StringBuffer(temp.substring(0, event.start));
					test.append(event.text);
					test.append(temp.substring(event.end, temp.length()));
					try
					{
						new Double(test.toString());
					}
					catch (NumberFormatException e)
					{
						event.doit = false;
					}
				}
			});
			((Text) textEditor.getControl()).addFocusListener(new FocusAdapter()
			{
				@Override
				public void focusGained(final FocusEvent event)
				{
					((Text) textEditor.getControl()).selectAll();
				}

				@Override
				public void focusLost(final FocusEvent event)
				{
					((Text) textEditor.getControl()).setText(ViewerComposite.this.amountFormat.format(new Double(
							((Text) textEditor.getControl()).getText())));
				}
			});
			return textEditor;
		}

		private void createControls(final Composite parent)
		{
			this.amountFormat = NumberFormat.getNumberInstance();
			this.amountFormat.setMaximumFractionDigits(NumberFormat.getCurrencyInstance().getMaximumFractionDigits());
			this.amountFormat.setMinimumFractionDigits(NumberFormat.getCurrencyInstance().getMinimumFractionDigits());

			GridLayout layout = new GridLayout(2, false);
			layout.marginWidth = 0;
			layout.marginHeight = 0;

			this.setLayout(layout);
			this.setLayoutData(new GridData(GridData.FILL_BOTH));

			Table table = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			table.setLayoutData(new GridData(GridData.FILL_BOTH));
			this.viewer = new TableViewer(table);

			for (int i = 0; i < CompensationTableViewerComposite.COLUMN_NAMES.length; i++)
			{
				TableViewerColumn column = new TableViewerColumn(this.viewer, SWT.NONE);
				column.getColumn().setText(CompensationTableViewerComposite.COLUMN_NAMES[i]);
				column.getColumn().setWidth(CompensationTableViewerComposite.COLUMN_WIDTHS[i]);
				column.getColumn().setAlignment(CompensationTableViewerComposite.COLUMN_ALIGNMENTS[i]);
				column.getColumn().pack();
				column.getColumn().setData(new Integer(i));
				column.getColumn().addSelectionListener(new SelectionAdapter()
				{
					@Override
					public void widgetSelected(final SelectionEvent event)
					{
						Integer column = ((Integer) event.widget.getData());
						ViewerComposite.this.viewer.setSorter(new CompensationTableViewerSorter(column.intValue()));
					}
				});
			}

			this.viewer.setColumnProperties(CompensationTableViewerComposite.COLUMN_NAMES);
			this.viewer.setContentProvider(new CompensationTableContentProvider(this.viewer));
			this.viewer.setLabelProvider(new CompensationTableLabelProvider());
			this.viewer.setFilters(new ViewerFilter[] { new CompensationTableViewerFilter() });

			CellEditor[] editors = new CellEditor[2];
			editors[0] = new ComboBoxCellEditor(table, this.getCompensationTypeEntries(), SWT.READ_ONLY | SWT.DROP_DOWN);
			editors[1] = this.createAmountCellEditor();

			this.viewer.setCellEditors(editors);
			this.viewer.setCellModifier(new CompensationCellModifier(CompensationTableViewerComposite.COLUMN_NAMES));
		}

		private String[] getCompensationTypeEntries()
		{
			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			tracker.open();

			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				if (this.compensationTypes == null)
				{
					CompensationTypeQuery query = (CompensationTypeQuery) service.getQuery(CompensationType.class);
					Collection<CompensationType> compensationTypes = query.selectAll();
					this.compensationTypes = compensationTypes.toArray(new CompensationType[0]);
					this.entries = new String[this.compensationTypes.length];
					for (int i = 0; i < this.entries.length; i++)
					{
						StringBuilder builder = new StringBuilder();
						CompensationType type = this.compensationTypes[i];
						if (!type.getCode().isEmpty())
						{
							builder = builder.append(type.getCode());
							if (!type.getName().isEmpty())
								builder = builder.append(" - ");
						}
						if (!type.getName().isEmpty())
							builder = builder.append(type.getName());
						this.entries[i] = builder.toString();
					}
				}
			}
			tracker.close();

			return this.entries;
		}

		public TableViewer getViewer()
		{
			return this.viewer;
		}

		@Override
		public void pack()
		{
			Table table = this.viewer.getTable();
			for (TableColumn column : table.getColumns())
			{
				column.pack();
			}
		}

		public void setInput(final CompensationList list)
		{
			this.viewer.setInput(list);
			this.pack();
		}

		private class CompensationCellModifier implements ICellModifier
		{
			private final String[] columns;

			private NumberFormat nf = null;

			public CompensationCellModifier(final String[] columnNames)
			{
				this.columns = columnNames;
				this.nf = NumberFormat.getNumberInstance();
				this.nf.setMaximumFractionDigits(NumberFormat.getCurrencyInstance().getMaximumFractionDigits());
				this.nf.setMinimumFractionDigits(NumberFormat.getCurrencyInstance().getMinimumFractionDigits());
			}

			@Override
			public boolean canModify(final Object element, final String property)
			{
				return true;
			}

			@Override
			public Object getValue(final Object element, final String property)
			{
				int columnIndex = -1;

				for (int i = 0; i < this.columns.length; i++)
					if (this.columns[i].equals(property))
						columnIndex = i;

				Object result = null;
				CWrapper wrapper = (CWrapper) element;
				Compensation compensation = wrapper.getCompensation();

				switch (columnIndex)
				{
					case 0:
						for (int i = 0; i < ViewerComposite.this.compensationTypes.length; i++)
						{
							if (ViewerComposite.this.compensationTypes[i].equals(compensation.getCompensationType()))
								return new Integer(i);
						}
						return new Integer(0);

					case 1:
						result = this.nf.format(compensation.getAmount());
						break;
					default:
				}

				return result;
			}

			@Override
			public void modify(final Object element, final String property, final Object value)
			{
				int columnIndex = -1;

				for (int i = 0; i < CompensationTableViewerComposite.COLUMN_NAMES.length; i++)
					if (CompensationTableViewerComposite.COLUMN_NAMES[i].equals(property))
						columnIndex = i;

				TableItem item = (TableItem) element;
				CWrapper wrapper = (CWrapper) item.getData();
				Compensation compensation = wrapper.getCompensation();

				switch (columnIndex)
				{
					case 0:
						int index = ((Integer) value).intValue();
						compensation.setCompensationType(ViewerComposite.this.compensationTypes[index]);
						item.setText(0, ViewerComposite.this.entries[index]);
						break;
					case 1:
						try
						{
							Double amount = new Double((String) value);
							compensation.setAmount(amount);
							item.setText(1, this.nf.format(compensation.getAmount()));
						}
						catch (NumberFormatException e)
						{
							compensation.setAmount(new Double(0d));
							item.setText(1, this.nf.format(compensation.getAmount()));
						}
						break;
					default:
				}
			}
		}

		private class CompensationTableLabelProvider extends LabelProvider implements ITableLabelProvider
		{
			NumberFormat nf = NumberFormat.getNumberInstance();

			public CompensationTableLabelProvider()
			{
				this.nf.setMaximumFractionDigits(NumberFormat.getCurrencyInstance().getMaximumFractionDigits());
				this.nf.setMinimumFractionDigits(NumberFormat.getCurrencyInstance().getMinimumFractionDigits());
			}

			@Override
			public Image getColumnImage(final java.lang.Object element, final int columnIndex)
			{
				return null;
			}

			@Override
			public String getColumnText(final java.lang.Object element, final int columnIndex)
			{
				CWrapper wrapper = (CWrapper) element;
				Compensation compensation = wrapper.getCompensation();

				switch (columnIndex)
				{
					case 0:
						if (compensation.getCompensationType() == null)
						{
							CompensationType type = ViewerComposite.this.compensationTypes[0];
							compensation.setCompensationType(type);
						}

						if (!compensation.getCompensationType().getCode().isEmpty())
						{

						}
						return CourseFormatter.getInstance().formatComboEntry(compensation.getCompensationType());
					case 1:
						return this.nf.format(compensation.getAmount());
					default:
						return "";
				}
			}
		}

		private class CompensationTableViewerFilter extends ViewerFilter
		{
			@Override
			public boolean select(final Viewer viewer, final Object parentElement, final Object element)
			{
				CWrapper w1 = (CWrapper) element;
				Compensation c1 = w1.getCompensation();
				return !c1.isDeleted();
			}
		}

		private class CompensationTableViewerSorter extends ViewerSorter
		{
			private final int column;

			public CompensationTableViewerSorter(final int column)
			{
				this.column = column;
			}

			@Override
			public int compare(final Viewer viewer, final Object object1, final Object object2)
			{
				int value;
				CWrapper w1 = (CWrapper) object1;
				Compensation c1 = w1.getCompensation();
				CWrapper w2 = (CWrapper) object2;
				Compensation c2 = w2.getCompensation();

				switch (this.column)
				{
					case 0:
					{
						CompensationType type1 = c1.getCompensationType();
						CompensationType type2 = c2.getCompensationType();

						if (type1.getCode() == null)
							if (type2.getCode() == null)
								value = type1.getName().compareTo(type2.getName());
							else
								value = type1.getName().compareTo(type2.getCode());
						else if (type2.getCode() == null)
							value = type1.getCode().compareTo(type2.getName());
						else
							value = type1.getCode().compareTo(type2.getCode());

						break;
					}
					case 1:
						value = new Double(c1.getAmount()).compareTo(new Double(c2.getAmount()));
						break;
					default:
						value = 0;
				}
				return value;
			}
		}
	}
}
