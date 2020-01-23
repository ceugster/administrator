package ch.eugster.events.member.wizards;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.member.Activator;
import ch.eugster.events.persistence.model.Address;
import ch.eugster.events.persistence.model.Country;
import ch.eugster.events.persistence.model.FieldExtension;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.Member;
import ch.eugster.events.persistence.model.PersonSex;
import ch.eugster.events.persistence.model.PersonTitle;
import ch.eugster.events.persistence.queries.AddressQuery;
import ch.eugster.events.persistence.queries.CountryQuery;
import ch.eugster.events.persistence.queries.LinkPersonAddressQuery;
import ch.eugster.events.persistence.queries.MemberQuery;
import ch.eugster.events.persistence.queries.PersonSexQuery;
import ch.eugster.events.persistence.queries.PersonTitleQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class SelectPersonWizardPage extends WizardPage implements IWizardPage
{
	private Label[] labels;
	
	private Text[] inputTexts;
	
	private ComboViewer[] columnSelectors;
	
	private Button[] searchEnablers;

	private Button[] saveEnablers;
	
	private TableViewer personList;
	
	private ConnectionService connectionService;
	
	private ImportMemberWizard wizard;
	
	private Row titleRow;
	
	private int currentRow = 0;
	
	private Calendar currentUpdate;
	
	private boolean processModifyEvent = false;
	
	public SelectPersonWizardPage(ImportMemberWizard wizard)
	{
		super("import.member.select.person.wizard.page");
		this.wizard = wizard;
		this.currentUpdate = GregorianCalendar.getInstance();
	}
	
	@Override
	public void createControl(final Composite parent)
	{
		this.setTitle("Mitglieder synchronisieren");
		this.setMessage("Aktualisieren Sie die Mitgliedschaften durch Synchronisieren mit einer externen Excel Arbeitsmappe.");
		this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("SYNCHRONIZE"));
		
		this.titleRow = this.wizard.getSelectedSheet().getRow(currentRow);
		String[] titles = new String[this.titleRow.getLastCellNum() - this.titleRow.getFirstCellNum()];
		for (int currentCell = this.titleRow.getFirstCellNum(); currentCell < titleRow.getLastCellNum(); currentCell++)
		{
			titles[currentCell - this.titleRow.getFirstCellNum()] = titleRow.getCell(currentCell).getStringCellValue();
		}

		Composite parentComposite = new Composite(parent, SWT.BORDER);
		parentComposite.setLayout(new GridLayout());
		parentComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
		parentComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		SashForm sashForm = new SashForm(parentComposite, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final ScrolledComposite scrollComposite = new ScrolledComposite(sashForm, SWT.V_SCROLL | SWT.BORDER);
		scrollComposite.setLayout(new FillLayout());
		scrollComposite.setLayoutData(new GridData());
		scrollComposite.setExpandHorizontal(true);
		scrollComposite.setExpandVertical(true);
		scrollComposite.setMinHeight(500);

		final Composite composite = new Composite(scrollComposite, SWT.BORDER);
		composite.setLayout(new GridLayout(5, false));
		composite.setLayoutData(null);

		scrollComposite.setContent(composite);
		scrollComposite.addListener(SWT.Resize, new Listener() 
		{
			@Override
			public void handleEvent(Event event) 
			{
				  int width = scrollComposite.getClientArea().width;
				  scrollComposite.setMinSize(composite.computeSize( width, SWT.DEFAULT ));
			}
		});
//		composite.setSize(composite.computeSize(250, 250));
//	    scrollComposite.addControlListener(new ControlAdapter() 
//	    {
//	        public void controlResized(ControlEvent e) 
//	        {
//	          Rectangle r = scrollComposite.getClientArea();
//	          scrollComposite.setMinSize(composite.computeSize(SWT.DEFAULT,
//	              SWT.DEFAULT));
//	        }
//	    });

		Label label = new Label(composite, SWT.None);
		label.setText("Spaltentitel");
		label.setLayoutData(new GridData());
		
		label = new Label(composite, SWT.None);
		label.setText("Inhalt aktuelle Zeile");
		label.setLayoutData(new GridData());
		
		label = new Label(composite, SWT.None);
		label.setText("Verwenden in Feld");
		label.setLayoutData(new GridData());
		
		label = new Label(composite, SWT.None);
		label.setText("Suchkriterium");
		label.setLayoutData(new GridData());
		
		label = new Label(composite, SWT.None);
		label.setText("Übernehmen");
		label.setLayoutData(new GridData());
		
		labels = new Label[titles.length];
		inputTexts = new Text[titles.length];
		columnSelectors = new ComboViewer[titles.length];
		searchEnablers = new Button[titles.length];
		saveEnablers = new Button[titles.length];
		for (int i = 0; i < labels.length; i++)
		{
			final int count = i;
			labels[i] = new Label(composite, SWT.None);
			labels[i].setText(titleRow.getCell(i + this.titleRow.getFirstCellNum()).getStringCellValue());
			labels[i].setLayoutData(new GridData());
			
			GridData gridData = new GridData();
			gridData.widthHint = 200;
			
			inputTexts[i] = new Text(composite, SWT.BORDER);
			inputTexts[i].setLayoutData(gridData);
			inputTexts[i].addModifyListener(new ModifyListener() 
			{
				@Override
				public void modifyText(ModifyEvent e) 
				{
					if (processModifyEvent)
					{
						setInput(getPersonList());
					}
				}
			});

			Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.BORDER);
			combo.setLayoutData(new GridData());
			combo.setData(Integer.valueOf(i));
			
			columnSelectors[i] = new ComboViewer(combo);
			columnSelectors[i].setContentProvider(new ArrayContentProvider());
			columnSelectors[i].setLabelProvider(new LabelProvider()
			{
				@Override
				public String getText(Object element) 
				{
					Field field = (Field) element;
					return field.label();
				}
			});
			columnSelectors[i].setInput(SelectPersonWizardPage.Field.values());
			columnSelectors[i].setSelection(new StructuredSelection(new Field[] { Field.NO_FIELD }));
			columnSelectors[i].addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(SelectionChangedEvent event) 
				{
					String[] fieldIndexes = new String[columnSelectors.length];
					for (int i = 0; i < columnSelectors.length; i++)
					{
						IStructuredSelection ssel = (IStructuredSelection) columnSelectors[i].getSelection();
						fieldIndexes[i] = String.valueOf(ssel.getFirstElement() instanceof Field ? ((Field) ssel.getFirstElement()).ordinal() : Field.NO_FIELD.ordinal());
					}
					wizard.getDialogSettings().put("field.indexes", fieldIndexes);

					IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
					searchEnablers[count].setVisible(((Field) ssel.getFirstElement()).isSearchable());
					saveEnablers[count].setVisible(((Field) ssel.getFirstElement()).isSaveable());
					setInput(getPersonList());
				}
			});

			searchEnablers[i] = new Button(composite, SWT.CHECK);
			searchEnablers[i].setLayoutData(new GridData());
			searchEnablers[i].setVisible(false);
			searchEnablers[i].addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetSelected(SelectionEvent e) 
				{
					String[] checked = new String[searchEnablers.length];
					for (int i = 0; i < searchEnablers.length; i++)
					{
						checked[i] = String.valueOf(searchEnablers[i] == null ? false : searchEnablers[i].getSelection());
					}
					wizard.getDialogSettings().put("search.indexes", checked);

					setInput(getPersonList());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) 
				{
					widgetSelected(e);
				}
			});

			saveEnablers[i] = new Button(composite, SWT.CHECK);
			saveEnablers[i].setLayoutData(new GridData());
			saveEnablers[i].setVisible(false);
			saveEnablers[i].addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetSelected(SelectionEvent e) 
				{
					String[] checked = new String[saveEnablers.length];
					for (int i = 0; i < saveEnablers.length; i++)
					{
						checked[i] = String.valueOf(saveEnablers[i] == null ? false : saveEnablers[i].getSelection());
					}
					wizard.getDialogSettings().put("save.indexes", checked);

					setInput(getPersonList());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) 
				{
					widgetSelected(e);
				}
			});
		}
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 100;
		
//		Composite tableComposite = new Composite(parentComposite, SWT.None);
//		tableComposite.setLayout(new GridLayout());
//		tableComposite.setLayoutData(gridData);
		
		Table table = new Table(sashForm, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);
		
		personList = new TableViewer(table);
		personList.setContentProvider(new ArrayContentProvider());
		
		TableViewerColumn viewerColumn = new TableViewerColumn(personList, SWT.None);
		viewerColumn.setLabelProvider(new CellLabelProvider() 
		{
			@Override
			public void update(ViewerCell cell) 
			{
				Member member = null;
				Object  element = cell.getElement();
				if (element instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) element;
					member = link.getMember(wizard.getMembership());
				}
				else if (element instanceof Address)
				{
					Address address = (Address) element;
					member = address.getMember(wizard.getMembership());
				}
				cell.setText((member == null || member.isDeleted()) ? "" : member.getCode());
			}
		});
		viewerColumn.getColumn().setText("Mitgliednr.");
		
		viewerColumn = new TableViewerColumn(personList, SWT.None);
		viewerColumn.setLabelProvider(new CellLabelProvider() 
		{
			@Override
			public void update(ViewerCell cell) 
			{
				Object  element = cell.getElement();
				if (element instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) element;
					cell.setText(link.getPerson().getLastname());
				}
			}
		});
		viewerColumn.getColumn().setText("Nachname");
		
		viewerColumn = new TableViewerColumn(personList, SWT.None);
		viewerColumn.setLabelProvider(new CellLabelProvider() 
		{
			@Override
			public void update(ViewerCell cell) 
			{
				Object  element = cell.getElement();
				if (element instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) element;
					cell.setText(link.getPerson().getFirstname());
				}
			}
		});
		viewerColumn.getColumn().setText("Vorname");
		
		viewerColumn = new TableViewerColumn(personList, SWT.None);
		viewerColumn.setLabelProvider(new CellLabelProvider() 
		{
			@Override
			public void update(ViewerCell cell) 
			{
				Object  element = cell.getElement();
				if (element instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) element;
					cell.setText(link.getAddress().getName());
				}
				else if (element instanceof Address)
				{
					Address address = (Address) element;
					cell.setText(address.getName());
				}
			}
		});
		viewerColumn.getColumn().setText("Organisation");
		
		viewerColumn = new TableViewerColumn(personList, SWT.None);
		viewerColumn.setLabelProvider(new CellLabelProvider() 
		{
			@Override
			public void update(ViewerCell cell) 
			{
				Object  element = cell.getElement();
				if (element instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) element;
					cell.setText(link.getAddress().getAddress());
				}
				else if (element instanceof Address)
				{
					Address address = (Address) element;
					cell.setText(address.getAddress());
				}
			}
		});
		viewerColumn.getColumn().setText("Strasse");
		
		viewerColumn = new TableViewerColumn(personList, SWT.None);
		viewerColumn.setLabelProvider(new CellLabelProvider() 
		{
			@Override
			public void update(ViewerCell cell) 
			{
				Object  element = cell.getElement();
				if (element instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) element;
					cell.setText(link.getAddress().getPob());
				}
				else if (element instanceof Address)
				{
					Address address = (Address) element;
					cell.setText(address.getPob());
				}
			}
		});
		viewerColumn.getColumn().setText("Postfach");
		
		viewerColumn = new TableViewerColumn(personList, SWT.None);
		viewerColumn.setLabelProvider(new CellLabelProvider() 
		{
			@Override
			public void update(ViewerCell cell) 
			{
				Object  element = cell.getElement();
				if (element instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) element;
					cell.setText(link.getAddress().getZip());
				}
				else if (element instanceof Address)
				{
					Address address = (Address) element;
					cell.setText(address.getZip());
				}
			}
		});
		viewerColumn.getColumn().setText("PLZ");
		
		viewerColumn = new TableViewerColumn(personList, SWT.None);
		viewerColumn.setLabelProvider(new CellLabelProvider() 
		{
			@Override
			public void update(ViewerCell cell) 
			{
				Object  element = cell.getElement();
				if (element instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) element;
					cell.setText(link.getAddress().getCity());
				}
				else if (element instanceof Address)
				{
					Address address = (Address) element;
					cell.setText(address.getCity());
				}
			}
		});
		viewerColumn.getColumn().setText("Ort");
		
		viewerColumn = new TableViewerColumn(personList, SWT.None);
		viewerColumn.setLabelProvider(new CellLabelProvider() 
		{
			@Override
			public void update(ViewerCell cell) 
			{
				Object  element = cell.getElement();
				if (element instanceof LinkPersonAddress)
				{
					LinkPersonAddress link = (LinkPersonAddress) element;
					if (link.getPerson().getBirthday() == null)
					{
						if (link.getPerson().getBirthyear() != null)
						{
							cell.setText(link.getPerson().getBirthyear().toString());
						}
					}
					else
					{
						cell.setText(SimpleDateFormat.getDateInstance().format(link.getPerson().getBirthday()));
					}
				}
			}
		});
		viewerColumn.getColumn().setText("Geburtstag/jahr");

		Composite bottomComposite = new Composite(parentComposite, SWT.None);
		bottomComposite.setLayout(new GridLayout(2, false));
		bottomComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite filler = new Composite(bottomComposite, SWT.None);
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite buttonComposite = new Composite(bottomComposite, SWT.None);
		buttonComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		buttonComposite.setLayoutData(new GridData());
		
		final Button previous = new Button(buttonComposite, SWT.PUSH);
		previous.setText("Vorheriger");
		previous.setEnabled(false);

		Button clear = new Button(buttonComposite, SWT.PUSH);
		clear.setText("Auswahl leeren");
		clear.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				personList.setSelection(new StructuredSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});

		final Button save = new Button(buttonComposite, SWT.PUSH);
		save.setText("Auswahl als Mitglied übernehmen");
		save.setEnabled(false);
		
		final Button next = new Button(buttonComposite, SWT.PUSH);
		next.setText("Nächster");

		save.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				IStructuredSelection ssel = (IStructuredSelection) personList.getSelection();
				Object[] selections = ssel.toArray();
				for (Object selection : selections)
				{
					updateMember(selection);
				}
				if (wizard.getSelectedSheet().getLastRowNum() > currentRow)
				{
					showNextEntry();
				}
				save.setEnabled(!personList.getSelection().isEmpty());
				next.setEnabled(wizard.getSelectedSheet().getLastRowNum() > currentRow);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});

		previous.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (wizard.getSelectedSheet().getFirstRowNum() + 1 < currentRow)
				{
					processModifyEvent = false;
					nextRow(--currentRow);
					setInput(getPersonList());
					processModifyEvent = true;
				}
				previous.setEnabled(wizard.getSelectedSheet().getFirstRowNum() + 1 < currentRow);
				next.setEnabled(wizard.getSelectedSheet().getLastRowNum() > currentRow);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});

		next.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				if (wizard.getSelectedSheet().getLastRowNum() > currentRow)
				{
					showNextEntry();
				}
				next.setEnabled(wizard.getSelectedSheet().getLastRowNum() > currentRow);
				save.setEnabled(!personList.getSelection().isEmpty());
				previous.setEnabled(wizard.getSelectedSheet().getFirstRowNum() + 1 < currentRow);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});

		this.personList.addSelectionChangedListener(new ISelectionChangedListener() 
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				save.setEnabled(!event.getSelection().isEmpty());
			}
		});

		String[] indexes = wizard.getDialogSettings().getArray("field.indexes");
		if (indexes != null)
		{
			for (int i = 0; i < columnSelectors.length; i++)
			{
				int fieldOrdinal = Field.NO_FIELD.ordinal();
				try
				{
					fieldOrdinal = indexes[i] == null ? Field.NO_FIELD.ordinal() : Integer.valueOf(indexes[i]);
					Field field = Field.values()[fieldOrdinal];
					IStructuredSelection ssel = new StructuredSelection(new Field[] { field });
					this.columnSelectors[i].setSelection(ssel);
				}
				catch (NumberFormatException e)
				{
				}
			}
		}
		
		indexes = wizard.getDialogSettings().getArray("search.indexes");
		if (indexes != null)
		{
			for (int i = 0; i < searchEnablers.length; i++)
			{
				boolean checked = Boolean.valueOf(indexes[i] == null ? "false" : indexes[i]);
				if (this.searchEnablers[i] != null)
				{
					this.searchEnablers[i].setSelection(checked);
				}
			}
		}

		indexes = wizard.getDialogSettings().getArray("save.indexes");
		if (indexes != null)
		{
			for (int i = 0; i < saveEnablers.length; i++)
			{
				boolean checked = Boolean.valueOf(indexes[i] == null ? "false" : indexes[i]);
				if (this.saveEnablers[i] != null)
				{
					this.saveEnablers[i].setSelection(checked);
				}
			}
		}

		showNextEntry();
		
		this.setControl(scrollComposite);
	}
	
	private void showNextEntry()
	{
		processModifyEvent = false;
		nextRow(++currentRow);
		if (this.wizard.isSkipExistingMembers())
		{
			List<Member> members = getMembers();
			while (!members.isEmpty())
			{
				for (Member member : members)
				{
					if (this.wizard.isAutomaticUpdate())
					{
						this.updateMember(member);
					}
				}
				nextRow(++currentRow);
				members = getMembers();
			}
		}
		List<Object> entities = getPersonList();
		setNextAction(entities);
		processModifyEvent = true;
	}

	public void nextRow(int rowNum)
	{
		Row row = wizard.getSelectedSheet().getRow(rowNum);
		for (int i = titleRow.getFirstCellNum(); i < titleRow.getLastCellNum(); i++)
		{
			Cell cell = row.getCell(i);
			inputTexts[i - titleRow.getFirstCellNum()].setText(getCellValue(cell));
		}
	}

	public String getCellValue(Cell cell)
	{
		if (cell == null)
		{
			return "";
		}
		else
		{
			switch (cell.getCellType())
			{
			case Cell.CELL_TYPE_BOOLEAN:
			{
				return Boolean.toString(cell.getBooleanCellValue());
			}
			case Cell.CELL_TYPE_NUMERIC:
			{
			    if (HSSFDateUtil.isCellDateFormatted(cell)) 
			    {
					return SimpleDateFormat.getDateInstance().format(cell.getDateCellValue());
			    }
			    else
			    {
					double doubleValue = cell.getNumericCellValue();
					int intValue = new Double(doubleValue).intValue();
					if (doubleValue - intValue == 0)
					{
						return Integer.toString(intValue);
					}
					else
					{
						return Double.toString(doubleValue);
					}
			    }
			}
			case Cell.CELL_TYPE_STRING:
			{
				return cell.getStringCellValue().trim();
			}
			case Cell.CELL_TYPE_BLANK:
			{
				return "";
			}
			default:
			{
				return "";
			}
			}
		}
	}
	
	private void updateMember(Member member)
	{
		if (member.isValidLink())
		{
			updateMember(member.getLink());
		}
		else if (member.isValidAddress())
		{
			updateMember(member.getAddress());
		}
	}
	
	private void updateMember(Object entity)
	{
		if (entity instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) entity;
			Member member = link.getMember(wizard.getMembership());
			if (member == null)
			{
				member = Member.newInstance(wizard.getMembership(), link);
				member.setInserted(currentUpdate);
				link.addMember(member);
			}
			else if (member.isDeleted())
			{
				member.setDeleted(false);
			}
			if (member.getCode().equals(getMemberCode()))
			{
				updatePerson(link);
				updateAddress(link.getAddress());
			}
			member.setCode(getMemberCode());
			member.setUpdated(currentUpdate);
			LinkPersonAddressQuery query = (LinkPersonAddressQuery) connectionService.getQuery(LinkPersonAddress.class);
			query.merge(link);
		}
		else if (entity instanceof Address)
		{
			Address address = (Address) entity;
			Member member = address.getMember(wizard.getMembership());
			if (member == null)
			{
				member = Member.newInstance(wizard.getMembership(), address);
				member.setInserted(currentUpdate);
				address.addMember(member);
			}
			else if (member.isDeleted())
			{
				member.setDeleted(false);
			}
			if (member.getCode().equals(getMemberCode()))
			{
				updateAddress(address);
			}
			member.setCode(getMemberCode());
			member.setUpdated(currentUpdate);
			AddressQuery query = (AddressQuery) connectionService.getQuery(Address.class);
			query.merge(address);
		}
	}
	
	private void updatePerson(LinkPersonAddress link)
	{
		Row row = this.wizard.getSelectedSheet().getRow(this.currentRow);
		for (Field field : Field.values())
		{
			for (int i = 0; i < columnSelectors.length; i++)
			{
				if (saveEnablers[i] != null && saveEnablers[i].getSelection())
				{
					IStructuredSelection ssel = (IStructuredSelection) columnSelectors[i];
					if (ssel.getFirstElement().equals(field))
					{
						switch (field)
						{
						case BIRTHDATE:
						{
							Calendar birthdate = getBirthdate(row);
							if (birthdate != null && birthdate.getTimeInMillis() != link.getPerson().getBirthdate().longValue())
							{
								link.getPerson().setBirthdate(new Long(birthdate.getTimeInMillis()));
							}
						}
						case EMAIL_DIRECT:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !link.getEmail().equals(value))
								link.setEmail(value);
						}
						case EMAIL_PRIVATE:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !link.getPerson().getEmail().equals(value))
								link.getPerson().setEmail(value);
						}
						case FIRSTNAME:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !link.getPerson().getFirstname().equals(value))
								link.getPerson().setFirstname(value);
						}
						case LASTNAME:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !link.getPerson().getLastname().equals(value))
								link.getPerson().setLastname(value);
						}
						case PHONE_DIRECT:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !link.getPhone().equals(value))
								link.setPhone(value);
						}
						case PHONE_MOBILE:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !link.getPerson().getPhone().equals(value))
								link.getPerson().setPhone(value);
						}
						case SEX:
						{
							PersonSex sex = getSex(row);
							if (sex != null && !link.getPerson().getSex().equals(sex))
							{
								link.getPerson().setSex(sex);
							}
						}
						case TITLE:
						{
							PersonTitle title = getTitle(row);
							if (title != null && !link.getPerson().getTitle().equals(title))
								link.getPerson().setTitle(title);
						}
						default:
						{
							
						}
						}
					}
				}
			}
		}
	}
	
	private void updateAddress(Address address)
	{
		Row row = this.wizard.getSelectedSheet().getRow(this.currentRow);
		for (Field field : Field.values())
		{
			for (int i = 0; i < columnSelectors.length; i++)
			{
				if (saveEnablers[i] != null && saveEnablers[i].getSelection())
				{
					IStructuredSelection ssel = (IStructuredSelection) columnSelectors[i];
					if (ssel.getFirstElement().equals(field))
					{
						switch (field)
						{
						case ANOTHER_ADDRESS_LINE:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !address.getAnotherLine().equals(value))
								address.setAnotherLine(value);
						}
						case CITY:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !address.getCity().equals(value))
								address.setCity(value);
						}
						case COUNTRY:
						{
						Country country = null;
						String value = getValue(field, row);
						if (this.getConnectionService() != null)
						{
							CountryQuery query = (CountryQuery) this.getConnectionService().getQuery(Country.class);
							if (value.isEmpty())
							{
								country = query.selectDefault();
							}
							else
							{
								country = query.findByIso3166Alpha2Code(value);
							}
							address.setCountry(country);
						}
						}
						case EMAIL_ADDRESS:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !address.getEmail().equals(value))
								address.setEmail(value);
						}
						case FAX:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !address.getFax().equals(value))
								address.setFax(value);
						}
						case ORGANIZATION:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !address.getName().equals(value))
								address.setName(value);
						}
						case PHONE_ADDRESS:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !address.getPhone().equals(value))
								address.setPhone(value);
						}
						case POBOX:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !address.getPob().equals(value))
								address.setPob(value);
						}
						case PROVINCE:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !address.getProvince().equals(value))
								address.setProvince(value);
						}
						case  STREET:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !address.getAddress().equals(value))
								address.setAddress(value);
						}
						case ZIP:
						{
							String value = getValue(field, row);
							if (!value.isEmpty() && !address.getZip().equals(value))
								address.setZip(value);
						}
						default:
						}
					}
				}
			}
		}
	}
	
	private ConnectionService getConnectionService()
	{
		if (this.connectionService == null)
		{
			ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class, null);
			tracker.open();
			try
			{
				this.connectionService = tracker.getService();
			}
			finally
			{
				tracker.close();
			}
		}
		return this.connectionService;
	}
	
	private String getMemberCode()
	{
		for (int i = 0; i < this.columnSelectors.length; i++)
		{
			IStructuredSelection ssel = (IStructuredSelection) this.columnSelectors[i].getSelection();
			if (ssel.getFirstElement().equals(Field.MEMBER_ID))
			{
				return this.inputTexts[i].getText().trim();
			}
		}
		return "";
	}
	
	private boolean isPerson()
	{
		for (int i = 0; i < this.searchEnablers.length; i++)
		{
			if (this.searchEnablers[i].getSelection())
			{
				IStructuredSelection ssel = (IStructuredSelection) this.columnSelectors[i].getSelection();
				Field field = (Field) ssel.getFirstElement();
				if (field.getEntityName().equals("person"))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private List<Member> getMembers()
	{
		MemberQuery query = (MemberQuery) this.getConnectionService().getQuery(Member.class);
		List<Member> members = query.selectByMembershipAndCode(this.wizard.getMembership(), this.getMemberCode());
		return members;
	}
	
	private List<Object> getPersonList()
	{
		List<Object> entities = new ArrayList<Object>();
		if (this.getConnectionService() != null)
		{
			Map<String, String> criteria = new HashMap<String, String>();
			String value = getSearchValue(Field.ORGANIZATION);
			if (value != null)
			{
				criteria.put("name", value);
			}
			value = getSearchValue(Field.ANOTHER_ADDRESS_LINE);
			if (value != null)
			{
				criteria.put("another_line", value);
			}
			value = getSearchValue(Field.STREET);
			if (value != null)
			{
				criteria.put("address", value);
			}
			value = getSearchValue(Field.POBOX);
			if (value != null)
			{
				criteria.put("pob", value);
			}
			value = getSearchValue(Field.ZIP);
			if (value != null)
			{
				criteria.put("zip", value);
			}
			value = getSearchValue(Field.CITY);
			if (value != null)
			{
				criteria.put("city", value);
			}
			value = getSearchValue(Field.FAX);
			if (value != null)
			{
				criteria.put("fax", value);
			}
			if (isPerson())
			{
				value = getSearchValue(Field.LASTNAME);
				if (value != null)
				{
					criteria.put("lastname", value);
				}
				value = getSearchValue(Field.FIRSTNAME);
				if (value != null)
				{
					criteria.put("firstname", value);
				}
				if (criteria.size() > 0)
				{
					LinkPersonAddressQuery linkQuery = (LinkPersonAddressQuery) this.getConnectionService().getQuery(LinkPersonAddress.class);
					entities.addAll(linkQuery.selectByCriteria(criteria, new HashMap<String, FieldExtension>(), 100));
				}
			}
			else
			{
				if (criteria.size() > 0)
				{
					AddressQuery addressQuery = (AddressQuery) this.getConnectionService().getQuery(Address.class);
					entities.addAll(addressQuery.selectByCriteria(criteria, 100));
				}
			}
		}
		return entities;
	}

	private void setNextAction(List<Object> entities)
	{
		if (wizard.doSilentInsertIfSingleResult())
		{
			/*
			 * Update automatically
			 */
			while (entities.size() == 1)
			{
				if (wizard.isAutomaticUpdate())
				{
					this.updateMember(entities.get(0));
					this.nextRow(++currentRow);
					entities = this.getPersonList();
				}
			}
		}
		/*
		 * List for select
		 */
		setInput(entities);
	}

	private void setInput(List<Object> entities)
	{
		/*
		 * List for select
		 */
		Object[] input = entities.toArray(new Object[0]);
		this.personList.setInput(input);
		{
			if (input.length == 1)
			{
				IStructuredSelection ssel = new StructuredSelection(new Object[] { input[0] });
				this.personList.setSelection(ssel);
			}
		}
		TableColumn[] columns = this.personList.getTable().getColumns();
		for (TableColumn column : columns)
		{
			column.pack();
		}
	}
	
	private String getSearchValue(Field field)
	{
		for (int i = 0; i < this.columnSelectors.length; i++)
		{
			if (this.searchEnablers[i].isVisible() && this.searchEnablers[i].getSelection())
			{
				IStructuredSelection ssel = (IStructuredSelection) this.columnSelectors[i].getSelection();
				if (ssel.getFirstElement().equals(field))
				{
					if (ssel.getFirstElement().equals(Field.STREET))
					{
						String street = this.inputTexts[i].getText().trim();
						String houseNumber = getSearchValue(Field.HOUSE_NUMBER);
						return (street + " " + houseNumber).trim();
					}
					return this.inputTexts[i].getText().trim();
				}
			}
		}
		return null;
	}

	private String getValue(Field field, Row row)
	{
		for (int i = 0; i < this.columnSelectors.length; i++)
		{
			IStructuredSelection ssel = (IStructuredSelection) this.columnSelectors[i].getSelection();
			if (ssel.getFirstElement().equals(field))
			{
				Cell cell = row.getCell(i + this.titleRow.getFirstCellNum());
				if (ssel.getFirstElement().equals(Field.STREET))
				{
					String street = getCellValue(cell);
					String houseNumber = getValue(Field.HOUSE_NUMBER, row);
					return (street + " " + houseNumber).trim();
				}
				return getCellValue(cell);
			}
		}
		return "";
	}

	private Calendar getBirthdate(Row row)
	{
		Calendar birthdate = null;
		for (int i = 0; i < this.columnSelectors.length; i++)
		{
			IStructuredSelection ssel = (IStructuredSelection) this.columnSelectors[i].getSelection();
			Cell cell = row.getCell(i + this.titleRow.getFirstCellNum());
			if (ssel.getFirstElement().equals(Field.BIRTHDATE))
			{
				String birth = getCellValue(cell);
				if (birth.length() == 8 || birth.length() == 10)
				{
					String pattern = birth.length() == 8 ? "dd.MM.yy" : (birth.length() == 10 ? "dd.MM.yyyy" : "");
					try
					{
						birthdate = GregorianCalendar.getInstance();
						birthdate.setTime(new SimpleDateFormat(pattern).parse(birth));
					}
					catch (ParseException e)
					{
						
					}
						
				}
			}
		}
		return birthdate;
	}

	private PersonSex getSex(Row row)
	{
		List<PersonSex> sexes = null;
		for (int i = 0; i < this.columnSelectors.length; i++)
		{
			IStructuredSelection ssel = (IStructuredSelection) this.columnSelectors[i].getSelection();
			Cell cell = row.getCell(i + this.titleRow.getFirstCellNum());
			if (ssel.getFirstElement().equals(Field.SEX))
			{
				String sexSymbol = getCellValue(cell);
				PersonSexQuery query = (PersonSexQuery) this.getConnectionService().getQuery(PersonSex.class);
				sexes =  query.selectBySymbol(sexSymbol);
			}
		}
		return sexes == null ? null : (sexes.size() == 0 ? null : sexes.get(0));
	}

	private PersonTitle getTitle(Row row)
	{
		List<PersonTitle> titles = null;
		for (int i = 0; i < this.columnSelectors.length; i++)
		{
			IStructuredSelection ssel = (IStructuredSelection) this.columnSelectors[i].getSelection();
			Cell cell = row.getCell(i + this.titleRow.getFirstCellNum());
			if (ssel.getFirstElement().equals(Field.TITLE))
			{
				String title = getCellValue(cell);
				PersonTitleQuery query = (PersonTitleQuery) this.getConnectionService().getQuery(PersonTitle.class);
				titles =  query.selectByTitle(title);
			}
		}
		return titles == null ? null : (titles.size() == 0 ? null : titles.get(0));
	}

	public enum Field
	{
		NO_FIELD, MEMBER_ID, EMAIL_PRIVATE, EMAIL_DIRECT, EMAIL_ADDRESS, PHONE_MOBILE, PHONE_DIRECT, PHONE_ADDRESS, FAX, SEX, TITLE, FIRSTNAME, LASTNAME, BIRTHDATE, YEAR_OF_BIRTH,
		ORGANIZATION, ANOTHER_ADDRESS_LINE, STREET, HOUSE_NUMBER, POBOX, COUNTRY, ZIP, CITY, PROVINCE;
		
		public String label()
		{
			switch (this)
			{
			case NO_FIELD:
			{
				return "";
			}
			case MEMBER_ID:
			{
				return "Mitgliedernummer";
			}
			case EMAIL_PRIVATE:
			{
				return "Email (persönlich)";
			}
			case EMAIL_DIRECT:
			{
				return "Email (Direktadresse)";
			}
			case EMAIL_ADDRESS:
			{
				return "Email (Adresse)";
			}
			case PHONE_MOBILE:
			{
				return "Telefon (Handy)";
			}
			case PHONE_DIRECT:
			{
				return "Telefon (Direktnummer)";
			}
			case PHONE_ADDRESS:
			{
				return "Telefon (Adresse)";
			}
			case FAX:
			{
				return "Fax (Adresse)";
			}
			case SEX:
			{
				return "Geschlecht";
			}
			case TITLE:
			{
				return "Titel";
			}
			case FIRSTNAME:
			{
				return 	"Vorname";
			}
			case LASTNAME:
			{
				return "Nachname";
			}
			case BIRTHDATE:
			{
				return "Geburtsdatum";
			}
			case YEAR_OF_BIRTH:
			{
				return "Geburtsjahr";
			}
			case ORGANIZATION:
			{
				return "Organisation";
			}
			case ANOTHER_ADDRESS_LINE:
			{
				return 	"Adresszusatz";
			}
			case STREET:
			{
				return 	"Strasse";
			}
			case HOUSE_NUMBER:
			{
				return "Hausnummer";
			}
			case POBOX:
			{
				return "Postfach";
			}
			case COUNTRY:
			{
				return "Land";
			}
			case ZIP:
			{
				return "PLZ";
			}
			case CITY:
			{
				return "Ort";
			}
			case PROVINCE:
			{
				return "Kanton";
			}
			default:
			{
				throw new RuntimeException("Invalid field");
			}
			}
		}

		public String getColumnName()
		{
			switch (this)
			{
			case NO_FIELD:
			{
				return "";
			}
			case MEMBER_ID:
			{
				return "code";
			}
			case EMAIL_PRIVATE:
			{
				return "email";
			}
			case EMAIL_DIRECT:
			{
				return "email";
			}
			case EMAIL_ADDRESS:
			{
				return "email";
			}
			case PHONE_MOBILE:
			{
				return "phone";
			}
			case PHONE_DIRECT:
			{
				return "phone";
			}
			case PHONE_ADDRESS:
			{
				return "phone";
			}
			case FAX:
			{
				return "fax";
			}
			case SEX:
			{
				return "sex";
			}
			case TITLE:
			{
				return "title";
			}
			case FIRSTNAME:
			{
				return 	"firstname";
			}
			case LASTNAME:
			{
				return "lastname";
			}
			case BIRTHDATE:
			{
				return "birthdate";
			}
			case YEAR_OF_BIRTH:
			{
				return "birthdate";
			}
			case ORGANIZATION:
			{
				return "name";
			}
			case ANOTHER_ADDRESS_LINE:
			{
				return 	"another_line";
			}
			case STREET:
			{
				return 	"address";
			}
			case HOUSE_NUMBER:
			{
				return "address";
			}
			case POBOX:
			{
				return "pob";
			}
			case COUNTRY:
			{
				return "country";
			}
			case ZIP:
			{
				return "zip";
			}
			case CITY:
			{
				return "city";
			}
			case PROVINCE:
			{
				return "province";
			}
			default:
			{
				throw new RuntimeException("Invalid field");
			}
			}
		}

		public String getEntityName()
		{
			switch (this)
			{
			case NO_FIELD:
			{
				return "";
			}
			case MEMBER_ID:
			{
				return "member";
			}
			case EMAIL_PRIVATE:
			{
				return "person";
			}
			case EMAIL_DIRECT:
			{
				return "link";
			}
			case EMAIL_ADDRESS:
			{
				return "address";
			}
			case PHONE_MOBILE:
			{
				return "person";
			}
			case PHONE_DIRECT:
			{
				return "link";
			}
			case PHONE_ADDRESS:
			{
				return "address";
			}
			case FAX:
			{
				return "address";
			}
			case SEX:
			{
				return "person";
			}
			case TITLE:
			{
				return "person";
			}
			case FIRSTNAME:
			{
				return 	"person";
			}
			case LASTNAME:
			{
				return "person";
			}
			case BIRTHDATE:
			{
				return "person";
			}
			case YEAR_OF_BIRTH:
			{
				return "person";
			}
			case ORGANIZATION:
			{
				return "address";
			}
			case ANOTHER_ADDRESS_LINE:
			{
				return 	"address";
			}
			case STREET:
			{
				return 	"address";
			}
			case HOUSE_NUMBER:
			{
				return "address";
			}
			case POBOX:
			{
				return "address";
			}
			case COUNTRY:
			{
				return "address";
			}
			case ZIP:
			{
				return "address";
			}
			case CITY:
			{
				return "address";
			}
			case PROVINCE:
			{
				return "address";
			}
			default:
			{
				throw new RuntimeException("Invalid field");
			}
			}
		}

		public boolean isSearchable()
		{
			switch (this)
			{
			case NO_FIELD:
			{
				return false;
			}
			case MEMBER_ID:
			{
				return true;
			}
			case EMAIL_PRIVATE:
			{
				return true;
			}
			case EMAIL_DIRECT:
			{
				return true;
			}
			case EMAIL_ADDRESS:
			{
				return true;
			}
			case PHONE_MOBILE:
			{
				return true;
			}
			case PHONE_DIRECT:
			{
				return true;
			}
			case PHONE_ADDRESS:
			{
				return true;
			}
			case FAX:
			{
				return true;
			}
			case SEX:
			{
				return false;
			}
			case TITLE:
			{
				return false;
			}
			case FIRSTNAME:
			{
				return 	true;
			}
			case LASTNAME:
			{
				return true;
			}
			case BIRTHDATE:
			{
				return false;
			}
			case YEAR_OF_BIRTH:
			{
				return false;
			}
			case ORGANIZATION:
			{
				return true;
			}
			case ANOTHER_ADDRESS_LINE:
			{
				return false;
			}
			case STREET:
			{
				return 	true;
			}
			case HOUSE_NUMBER:
			{
				return true;
			}
			case POBOX:
			{
				return true;
			}
			case COUNTRY:
			{
				return false;
			}
			case ZIP:
			{
				return true;
			}
			case CITY:
			{
				return true;
			}
			case PROVINCE:
			{
				return false;
			}
			default:
			{
				throw new RuntimeException("Invalid field");
			}
			}
		}

		public boolean isSaveable()
		{
			switch (this)
			{
			case NO_FIELD:
			{
				return false;
			}
			default:
			{
				return true;
			}
			}
		}
	}
}
