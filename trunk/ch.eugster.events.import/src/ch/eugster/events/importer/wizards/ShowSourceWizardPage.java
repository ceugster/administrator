package ch.eugster.events.importer.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.eugster.events.importer.Activator;

public class ShowSourceWizardPage extends WizardPage implements IPageChangedListener, ISelectionChangedListener
{
	private final ImportWizard wizard;

	private IDialogSettings settings;

	private Update update;

	private Button withHeader;

	private Spinner rowSpinner;

	private TableViewer viewer;

	public ShowSourceWizardPage(ImportWizard wizard)
	{
		super(ShowSourceWizardPage.class.getName());
		this.wizard = wizard;
		init();
	}

	private void init()
	{
		settings = Activator.getDefault().getDialogSettings().getSection("import.wizard.table");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("import.wizard.table");
		}
		if (settings.get("table.row.count") == null)
		{
			settings.put("table.row.count", 10);
		}
	}

	@Override
	public void createControl(Composite parent)
	{
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.widthHint = 400;

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Anzahl Zeilen anzeigen (0 = alle Zeilen)");

		gridData = new GridData();
		gridData.widthHint = 64;

		rowSpinner = new Spinner(composite, SWT.BORDER);
		rowSpinner.setLayoutData(gridData);
		rowSpinner.setIncrement(1);
		rowSpinner.setPageIncrement(10);
		rowSpinner.setDigits(0);
		rowSpinner.setMinimum(0);
		rowSpinner.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				settings.put("table.row.count", rowSpinner.getSelection());
				update = Update.ROWS;
				updateTable();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});
		rowSpinner.setSelection(settings.getInt("table.row.count"));

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		withHeader = new Button(composite, SWT.CHECK);
		withHeader.setText("Erste Zeile enthält Spaltentitel");
		withHeader.setLayoutData(gridData);
		withHeader.setSelection(settings.getBoolean("with.header"));
		withHeader.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				boolean header = ShowSourceWizardPage.this.withHeader.getSelection();
				settings.put("with.header", header);
				ShowSourceWizardPage.this.wizard.setWithHeader(header);
				update = Update.COLUMNS_AND_ROWS;
				updateTable();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});

		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;

		Table table = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer = new TableViewer(table);
		viewer.setContentProvider(new ArrayContentProvider());

		setControl(composite);
	}

	public int getVisibleRows()
	{
		return viewer.getTable().getItemCount();
	}

	public TableViewer getViewer()
	{
		return this.viewer;
	}

	private void updateTable()
	{
		if (update != null)
		{
			if (update.equals(Update.ROWS) || update.equals(Update.COLUMNS_AND_ROWS))
			{
				if (update.equals(Update.COLUMNS_AND_ROWS))
				{
					updateColumns(this.wizard.getSheet());
				}
				int firstRow = withHeader.getSelection() ? 1 : 0;
				int maxRows = Math.min(this.wizard.getSheet().getLastRowNum() - firstRow, Integer.MAX_VALUE);
				if (rowSpinner.getSelection() > maxRows)
				{
					rowSpinner.setSelection(maxRows);
				}
				rowSpinner.setMaximum(maxRows);
				int rowCount = rowSpinner.getSelection() == 0 ? maxRows : rowSpinner.getSelection() + firstRow;
				Collection<Row> rows = new ArrayList<Row>();
				for (int i = firstRow; i < rowCount; i++)
				{
					rows.add(this.wizard.getSheet().getRow(i));
				}
				this.viewer.setInput(rows.toArray(new Row[0]));
				TableColumn[] columns = this.viewer.getTable().getColumns();
				for (TableColumn column : columns)
				{
					column.pack();
				}
				update = Update.NONE;
			}
		}
	}

	private void updateColumns(Sheet sheet)
	{
		TableColumn[] tableColumns = viewer.getTable().getColumns();
		for (TableColumn tableColumn : tableColumns)
		{
			if (!tableColumn.isDisposed())
				tableColumn.dispose();
		}
		if (sheet != null)
		{
			Row row = sheet.getRow(sheet.getFirstRowNum());
			if (row != null)
			{
				Iterator<Cell> cells = row.cellIterator();
				int columnStyle = SWT.None;
				while (cells.hasNext())
				{
					Cell cell = cells.next();
					switch (cell.getCellStyle().getAlignmentEnum())
					{
						case CENTER:
						{
							columnStyle = SWT.CENTER;
						}
						case RIGHT:
						{
							columnStyle = SWT.RIGHT;
						}
						default:
						{
							columnStyle = SWT.LEFT;
						}
					}
					TableViewerColumn viewerColumn = new TableViewerColumn(viewer, columnStyle);
					viewerColumn.setLabelProvider(new CellLabelProvider()
					{
						@Override
						public void update(ViewerCell viewerCell)
						{
							if (viewerCell.getElement() instanceof Row)
							{
								Row row = (Row) viewerCell.getElement();
								Cell cell = row.getCell(viewerCell.getColumnIndex());
								if (cell != null)
								{
									viewerCell.setText(wizard.evaluateCell(cell));
									int alignment = wizard.evaluateAlignment(cell);
									if (viewer.getTable().getColumn(viewerCell.getColumnIndex()).getAlignment() != alignment)
									{
										viewer.getTable().getColumn(viewerCell.getColumnIndex()).setAlignment(alignment);
									}
								}
							}
						}
					});
					if (this.withHeader.getSelection())
					{
						String value = this.wizard.evaluateCell(cell);
						viewerColumn.getColumn().setText(value == null ? "" + cell.getColumnIndex() : value);
					}
					else
					{
						viewerColumn.getColumn().setText(Integer.valueOf(cell.getColumnIndex() + 1).toString());
					}
				}
			}
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event)
	{
		if (event.getSelection() instanceof StructuredSelection)
		{
			StructuredSelection ssel = (StructuredSelection) event.getSelection();
			this.wizard.setSheet((Sheet) ssel.getFirstElement());
			this.update = Update.COLUMNS_AND_ROWS;
		}
	}

	@Override
	public void pageChanged(PageChangedEvent event)
	{
		updateTable();
	}

	private enum Update
	{
		NONE, ROWS, COLUMNS_AND_ROWS;
	}

}
