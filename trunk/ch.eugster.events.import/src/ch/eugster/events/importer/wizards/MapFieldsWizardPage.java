package ch.eugster.events.importer.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.importer.Activator;
import ch.eugster.events.persistence.model.Membership;
import ch.eugster.events.persistence.queries.MembershipQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class MapFieldsWizardPage extends WizardPage implements IPageChangedListener
{
	private final ImportWizard wizard;

	private Composite composite;

	private List<Text> sourceNames = new ArrayList<Text>();

	private List<ComboViewer> targetViewers = new ArrayList<ComboViewer>();

	private Map<String, MappingNames> fieldMappings = new HashMap<String, MappingNames>();

	private boolean dirtyMappings = false;

	private MembershipQuery membershipQuery;
	
	public MapFieldsWizardPage(ImportWizard wizard)
	{
		super(MapFieldsWizardPage.class.getName());
		this.wizard = wizard;
		init();
	}

	private void init()
	{
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite c = new Composite(parent, SWT.None);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		c.setLayout(new GridLayout());
		
		ScrolledComposite sc = new ScrolledComposite(c, SWT.V_SCROLL);
		sc.setLayoutData(new GridData(GridData.FILL_BOTH));
		sc.setLayout(new GridLayout());
		
		composite = new Composite(sc, SWT.None);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(2, true));
		sc.setContent(composite);
		
		setControl(c);
	}

	@Override
	public void pageChanged(PageChangedEvent event)
	{
		if (event.getSelectedPage().equals(this))
		{
			if (wizard.getSheet() != null && wizard.getSheet().getFirstRowNum() > -1)
			{
				Row row = wizard.getSheet().getRow(wizard.getSheet().getFirstRowNum());
				if (this.membershipQuery == null)
				{
					ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(), ConnectionService.class.getName(), null);
					tracker.open();
					try
					{
						ConnectionService connectionService = (ConnectionService) tracker.getService();
						this.membershipQuery = (MembershipQuery) connectionService.getQuery(Membership.class);
					}
					finally
					{
						tracker.close();
					}
				}
	
				if (wizard.getMembership() != null)
				{
					fieldMappings.clear();
					for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++)
					{
						fieldMappings.put(wizard.evaluateCell(row.getCell(i)), MappingNames.EMPTY);
					}
					String mapping = wizard.getMembership().getFieldMapping();
					if (mapping != null && !mapping.isEmpty())
					{
						String[] items = mapping.split("\t");
						for (String item : items)
						{
							String[] pair = item.split("[|]");
							if (pair.length == 1 || (pair.length == 2 && pair[1].isEmpty()))
							{
								fieldMappings.put(pair[0], MappingNames.EMPTY);
							}
							else if (pair.length == 2)
							{
								MappingNames targetNames = MappingNames.getMappingNameFromLabel(pair[1]);
								fieldMappings.put(pair[0], targetNames);
							}
						}
					}
				}
				
				int controlRow = 0;
				Iterator<Cell> iterator = row.cellIterator();
				while (iterator.hasNext())
				{
					final Cell cell = iterator.next();
					final String columnHeader = this.wizard.evaluateCell(cell);
					if (columnHeader != null)
					{
						Control[] children = this.composite.getChildren();
						if (children.length / 2 <= controlRow)
						{
							Text sourceName = new Text(composite, SWT.BORDER
									| wizard.evaluateAlignment(cell));
							sourceName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
							sourceName.setEditable(false);
							this.composite.setData("source" + controlRow, sourceName);
							this.sourceNames.add(sourceName);

							Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
							combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

							final ComboViewer targetViewer = new ComboViewer(combo);
							targetViewer.setContentProvider(new ArrayContentProvider());
							targetViewer.setLabelProvider(new LabelProvider()
							{
								@Override
								public Image getImage(Object element)
								{
									return null;
								}

								@Override
								public String getText(Object element)
								{
									return ((MappingNames) element).label();
								}
							});
							targetViewer.setInput(MappingNames.values());
							targetViewer.addSelectionChangedListener(new ISelectionChangedListener() 
							{
								@Override
								public void selectionChanged(SelectionChangedEvent event) 
								{
									Boolean sendEvent = (Boolean) targetViewer.getData("sendEvent");
									if (sendEvent.booleanValue())
									{
										String sourceName = (String) targetViewer.getData("sourceName");
										IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
										MappingNames targetNames = (MappingNames) ssel.getFirstElement();
										dirtyMappings = true;
										MapFieldsWizardPage.this.fieldMappings.put(sourceName, targetNames);
									}
									else
									{
										targetViewer.setData("sendEvent", Boolean.valueOf(true));
									}
								}
							});
							this.composite.setData("target" + controlRow, targetViewer);
							this.targetViewers.add(targetViewer);
						}

						Text sourceName = (Text) this.composite.getData("source" + controlRow);
						sourceName.setText(columnHeader);
						
						MappingNames mappingValue = this.fieldMappings.get(columnHeader);
						ComboViewer targetViewer = (ComboViewer) this.composite.getData("target" + controlRow);
						targetViewer.setData("sendEvent", Boolean.valueOf(false));
						targetViewer.setData("sourceName", columnHeader);
						targetViewer.setSelection(new StructuredSelection( new MappingNames[] { mappingValue }));
					}
					controlRow++;
				}
				int columnCount = (row.getLastCellNum() - row.getFirstCellNum()) * 2;
				if (this.composite.getChildren().length > columnCount)
				{
					Control[] controls = this.composite.getChildren();
					for (int i = columnCount + 1; i < controls.length; i++)
					{
						controls[i].dispose();
						controls[i] = null;
					}
				}
				composite.pack();
			}
		}
		else
		{
			if (dirtyMappings && this.wizard.getMembership() != null)
			{
				StringBuilder mapping = new StringBuilder();
				Set<Entry<String, MappingNames>> entrySet= this.fieldMappings.entrySet();
				Iterator<Entry<String, MappingNames>> entries = entrySet.iterator();
				{
					while (entries.hasNext())
					{
						Entry<String, MappingNames> entry = entries.next();
						mapping = mapping.append(entry.getKey() + "|" + entry.getValue().label() + "\t");
					}
					String fieldMapping = mapping.toString();
					if (fieldMapping.length() > 0)
					{
						fieldMapping = fieldMapping.substring(0, fieldMapping.length() - 1);
					}
					this.wizard.getMembership().setFieldMapping(fieldMapping);
					this.wizard.setMembership(membershipQuery.merge(this.wizard.getMembership()));
					this.dirtyMappings = false;
				}
			}
		}
	}

	public String[] getSourceNames()
	{
		List<String> names = new ArrayList<String>();
		for (Text sourceName : sourceNames)
		{
			names.add(sourceName.getText());
		}
		return names.toArray(new String[0]);
	}
	
	public Map<String, Integer> getSourceMapping()
	{
		Map<String, Integer> mappings = new HashMap<String, Integer>();
		for (int i = 0; i < sourceNames.size(); i++)
		{
			mappings.put(sourceNames.get(i).getText(), Integer.valueOf(i));
		}
		return mappings;
	}
	
	public Map<MappingNames, Integer> getMappingNames()
	{
		Map<MappingNames, Integer> names = new HashMap<MappingNames, Integer>();
		for (int i = 0; i < targetViewers.size(); i++)
		{
			IStructuredSelection ssel = (IStructuredSelection) targetViewers.get(i).getSelection();
			if (!ssel.getFirstElement().equals(MappingNames.EMPTY))
			{
				MappingNames mappingNames = (MappingNames) ssel.getFirstElement();
				names.put(mappingNames, Integer.valueOf(i));
			}
		}
		return names;
	}
}
