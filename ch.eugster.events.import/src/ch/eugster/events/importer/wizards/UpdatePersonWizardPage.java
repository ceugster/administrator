package ch.eugster.events.importer.wizards;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.importer.Activator;
import ch.eugster.events.importer.dialogs.SelectPersonDialog;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.queries.MemberQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class UpdatePersonWizardPage extends WizardPage implements IPageChangedListener
{
	private final ImportWizard wizard;

	private TableViewer viewer;

	private Button update;

	private Rectangle dialogBounds;
	
	public UpdatePersonWizardPage(ImportWizard wizard)
	{
		super(UpdatePersonWizardPage.class.getName());
		this.wizard = wizard;
	}

	@Override
	public void createControl(Composite parent)
	{
		final Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(2, false));

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		
		Table table = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer = new TableViewer(table);
		viewer.setContentProvider(new ArrayContentProvider());
		MappingNames[] mappingNames = MappingNames.getTargetViewerColumns();
		for (int i = 0; i < mappingNames.length; i++)
		{
			final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, mappingNames[i].columnStyle());
			viewerColumn.getColumn().setText(mappingNames[i].label());
			viewerColumn.getColumn().setData("col", Integer.valueOf(i));
			viewerColumn.setLabelProvider(new CellLabelProvider()
			{
				@Override
				public void update(ViewerCell viewerCell)
				{
					String[] values = (String[]) viewerCell.getElement();
					Integer index = (Integer) viewerColumn.getColumn().getData("col");
					viewerCell.setText(values[index.intValue()]);
				}
			});
		}

		Composite filler = new Composite(composite, SWT.NONE);
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		update = new Button(composite, SWT.PUSH);
		update.setLayoutData(new GridData());
		update.setText("Aktualisieren");
		update.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				updateData();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});
		
		setControl(composite);
	}

	private void updateData()
	{
		boolean updateExisting = false;
		SelectSourceWizardPage selectPage = (SelectSourceWizardPage) this.wizard.getPage(SelectSourceWizardPage.class.getName());
		if (selectPage != null)
		{
			updateExisting = selectPage.isUpdateExisting();
		}
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class.getName(), null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				if (dialogBounds == null)
				{
					Rectangle bounds = this.getWizard().getContainer().getShell().getBounds();
					dialogBounds = new Rectangle(bounds.x, bounds.y + bounds.height, bounds.width, bounds.height);
				}
				int index = 0;
				String[] values = (String[]) viewer.getElementAt(index);
				while (values != null)
				{
					IStructuredSelection ssel = new StructuredSelection(values);
					this.viewer.setSelection(ssel, true);

					Map<MappingNames, String> map = new HashMap<MappingNames, String>();
					MappingNames[] keys = MappingNames.getTargetViewerColumns();
					for (int i = 0; i < keys.length; i++)
					{
						map.put(keys[i], values[i]);
					}
					String code = map.get(MappingNames.EXTERNAL_ID);
					MemberQuery memberQuery = (MemberQuery) service.getQuery(Member.class);
					List<Member> members = memberQuery.selectByMembershipAndCode(this.wizard.getMembership(), code);
					if (members.isEmpty())
					{
						Display display = this.getShell().getDisplay();
						Shell shell = new Shell(display);
						shell.addControlListener(new ControlListener() 
						{
							@Override
							public void controlMoved(ControlEvent e) 
							{
								dialogBounds = ((Shell) e.getSource()).getBounds();
							}

							@Override
							public void controlResized(ControlEvent e) 
							{
								dialogBounds = ((Shell) e.getSource()).getBounds();
							}
						});
						if (dialogBounds != null)
						{
							shell.setBounds(dialogBounds.x, dialogBounds.y, dialogBounds.width, dialogBounds.height);
						}
						SelectPersonDialog dialog = new SelectPersonDialog(shell, this.wizard.getMembership(), map);
						int returnValue = dialog.open();
						if (returnValue == IDialogConstants.ABORT_ID)
						{
							return;
						}
					}
					else
					{
						if (updateExisting)
						{
							
						}
					}
					index++;
					values = (String[]) viewer.getElementAt(index);
				}
			}
		}
		finally
		{
			tracker.close();
		}
	}
	
	@Override
	public void pageChanged(PageChangedEvent event)
	{
		if (event.getSelectedPage().equals(this))
		{
			MapFieldsWizardPage mappingPage = (MapFieldsWizardPage) this.wizard.getPage(MapFieldsWizardPage.class.getName());
			if (mappingPage != null)
			{
				Map<MappingNames, Integer> mappingNames = mappingPage.getMappingNames();
				if (!mappingNames.isEmpty())
				{
					Sheet sheet = this.wizard.getSheet();
					if (sheet != null && sheet.getFirstRowNum() > -1 && sheet.getLastRowNum() > sheet.getFirstRowNum())
					{
						List<String[]> values = new ArrayList<String[]>();
						MappingNames[] viewerColumns = MappingNames.getTargetViewerColumns();
						String[] rowValues = null;
						for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++)
						{
							rowValues = new String[viewerColumns.length];
							Row row = sheet.getRow(i);
							int birthdateIndex = -1;
							int birthyearIndex = -1;
							for (int j = 0; j < viewerColumns.length; j++)
							{
								if (viewerColumns[j].equals(MappingNames.BIRTH_DATE))
								{
									birthdateIndex = j;
								}
								if (viewerColumns[j].equals(MappingNames.BIRTH_YEAR))
								{
									birthyearIndex = j;
								}
							}
							for (int j = 0; j < viewerColumns.length; j++)
							{
								Integer index = mappingNames.get(viewerColumns[j]);
								if (index != null)
								{
									Cell cell = row.getCell(index);
									if (cell == null)
									{
										rowValues[j] = "";
									}
									else
									{
										if (viewerColumns[j].equals(MappingNames.ADDRESS))
										{
											StringBuilder street = new StringBuilder(MappingNames.getCellValue(row.getCell(index)));
											StringBuilder number = new StringBuilder(getValue(mappingNames, MappingNames.ADDRESS_NUMBER, row));
											number = number.append(getValue(mappingNames, MappingNames.ADDRESS_SUB_NUMBER, row));
											rowValues[j] = street.append(" " + number).toString().trim();
										}
										else if (viewerColumns[j].equals(MappingNames.BIRTH_DATE))
										{
											String birthdate = MappingNames.getCellValue(row.getCell(index));
											if (birthdate.isEmpty())
											{
												String day = formatDatePart(getValue(mappingNames, MappingNames.BIRTH_DAY, row));
												String month = formatDatePart(getValue(mappingNames, MappingNames.BIRTH_MONTH, row));
												String year = formatDatePart(getValue(mappingNames, MappingNames.BIRTH_YEAR, row));
												if (day.isEmpty() || month.isEmpty() || year.isEmpty())
												{
													if (rowValues[birthyearIndex] == null)
													{
														rowValues[birthyearIndex] = year.isEmpty() ? "" : year;
													}
												}
												else
												{
													if (rowValues[birthyearIndex] == null)
													{
														rowValues[birthyearIndex] = year.isEmpty() ? "" : year;
													}
													Calendar calendar = GregorianCalendar.getInstance();
													calendar.set(Calendar.YEAR, Integer.valueOf(year).intValue());
													calendar.set(Calendar.MONTH, Integer.valueOf(month).intValue());
													calendar.set(Calendar.DATE, Integer.valueOf(day).intValue());
													rowValues[j] = SimpleDateFormat.getDateInstance().format(calendar.getTime());
												}
											}
											else
											{
												rowValues[j] = birthdate;
											}
										}
										else if (viewerColumns[j].equals(MappingNames.BIRTH_YEAR))
										{
											String day = formatDatePart(getValue(mappingNames, MappingNames.BIRTH_DAY, row));
											String month = formatDatePart(getValue(mappingNames, MappingNames.BIRTH_MONTH, row));
											String year = formatDatePart(getValue(mappingNames, MappingNames.BIRTH_YEAR, row));
											if (day.isEmpty() || month.isEmpty() || year.isEmpty())
											{
												rowValues[j] = year.isEmpty() ? "" : year;
											}
											else
											{
												rowValues[j] = year.isEmpty() ? "" : year;
												if (rowValues[birthdateIndex] == null)
												{
													Calendar calendar = GregorianCalendar.getInstance();
													calendar.set(Calendar.YEAR, Integer.valueOf(year).intValue());
													calendar.set(Calendar.MONTH, Integer.valueOf(month).intValue());
													calendar.set(Calendar.DATE, Integer.valueOf(day).intValue());
													rowValues[birthdateIndex] = SimpleDateFormat.getDateInstance().format(calendar.getTime());
												}
											}
										}
										else if (viewerColumns[j].equals(MappingNames.BIRTH_MONTH))
										{
											String day = formatDatePart(getValue(mappingNames, MappingNames.BIRTH_DAY, row));
											String month = formatDatePart(getValue(mappingNames, MappingNames.BIRTH_MONTH, row));
											String year = formatDatePart(getValue(mappingNames, MappingNames.BIRTH_YEAR, row));
											if (day.isEmpty() || month.isEmpty() || year.isEmpty())
											{
												if (rowValues[birthyearIndex] == null)
												{
													rowValues[birthyearIndex] = year.isEmpty() ? "" : year;
												}
											}
											else
											{
												if (rowValues[birthyearIndex] == null)
												{
													rowValues[birthyearIndex] = year.isEmpty() ? "" : year;
												}
												if (rowValues[birthdateIndex] == null)
												{
													Calendar calendar = GregorianCalendar.getInstance();
													calendar.set(Calendar.YEAR, Integer.valueOf(year).intValue());
													calendar.set(Calendar.MONTH, Integer.valueOf(month).intValue());
													calendar.set(Calendar.DATE, Integer.valueOf(day).intValue());
													rowValues[birthdateIndex] = SimpleDateFormat.getDateInstance().format(calendar.getTime());
												}
											}
										}
										else if (viewerColumns[j].equals(MappingNames.BIRTH_DAY))
										{
											String day = formatDatePart(getValue(mappingNames, MappingNames.BIRTH_DAY, row));
											String month = formatDatePart(getValue(mappingNames, MappingNames.BIRTH_MONTH, row));
											String year = formatDatePart(getValue(mappingNames, MappingNames.BIRTH_YEAR, row));
											if (day.isEmpty() || month.isEmpty() || year.isEmpty())
											{
												if (rowValues[birthyearIndex] == null)
												{
													rowValues[birthyearIndex] = year.isEmpty() ? "" : year;
												}
											}
											else
											{
												if (rowValues[birthyearIndex] == null)
												{
													rowValues[birthyearIndex] = year.isEmpty() ? "" : year;
												}
												if (rowValues[birthdateIndex] == null)
												{
													Calendar calendar = GregorianCalendar.getInstance();
													calendar.set(Calendar.YEAR, Integer.valueOf(year).intValue());
													calendar.set(Calendar.MONTH, Integer.valueOf(month).intValue());
													calendar.set(Calendar.DATE, Integer.valueOf(day).intValue());
													rowValues[birthdateIndex] = SimpleDateFormat.getDateInstance().format(calendar.getTime());
												}
											}
										}
										else
										{
											rowValues[j] = MappingNames.getCellValue(row.getCell(index));
										}
									}
								}
							}
							values.add(rowValues);
						}
						this.viewer.setInput(values);
						TableColumn[] columns = this.viewer.getTable().getColumns();
						for (TableColumn column : columns)
						{
							column.pack();
						}
					}
				}
			}
		}
	}

	private String formatDatePart(String value)
	{
		if (value == null || value.isEmpty())
		{
			return "";
		}
		try
		{
			Integer i = Integer.valueOf(value);
			return i.intValue() == 0 ? "" : i.toString();
		}
		catch (NumberFormatException e)
		{
			return "";
		}
	}
	
	private String getValue(Map<MappingNames, Integer> mappings, MappingNames key, Row row)
	{
		Integer index = mappings.get(key);
		if (index == null)
		{
			return "";
		}
		Cell cell = row.getCell(index.intValue());
		if (cell == null)
		{
			return "";
		}
		return MappingNames.getCellValue(cell).trim();
	}
	
//	private class ErrorMessageDialog extends IconAndMessageDialog
//	{
//		private String message;
//		
//		private Wizard wizard;
//		
//		public ErrorMessageDialog(Shell parentShell, String message, Wizard wizard) 
//		{
//			super(parentShell);
//			this.message = message;
//			this.wizard = wizard;
//		}
//
//		@Override
//		protected Image getImage()
//		{
//			return null;
//		}
//
//		@Override
//		protected Control createDialogArea(Composite parent) 
//		{
//			Composite composite = new Composite(parent, SWT.NONE);
//			composite.setLayout(new FillLayout());
//			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
//			
//			GridData gridData = new GridData(GridData.FILL_BOTH);
//			gridData.widthHint = 300;
//			gridData.heightHint = 150;
//
//			Text text = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
//			text.setText(message);
//			return composite;
//		}
//
//		@Override
//		protected void buttonPressed(int buttonId) 
//		{
//			super.cancelPressed();
//			((WizardDialog) wizard.getContainer()).close();
//		}
//
//	}
}
