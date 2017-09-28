package ch.eugster.events.visits.editors;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.formattedtext.MaskFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.formatters.AddressFormatter;
import ch.eugster.events.persistence.formatters.LinkPersonAddressFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.AddressSalutation;
import ch.eugster.events.persistence.model.Appliance;
import ch.eugster.events.persistence.model.ISelectedEmailProvider;
import ch.eugster.events.persistence.model.ISelectedPhoneProvider;
import ch.eugster.events.persistence.model.LinkPersonAddress;
import ch.eugster.events.persistence.model.LinkPersonAddressChild;
import ch.eugster.events.persistence.model.SchoolLevel;
import ch.eugster.events.persistence.model.SelectedEmail;
import ch.eugster.events.persistence.model.SelectedPhone;
import ch.eugster.events.persistence.model.Teacher;
import ch.eugster.events.persistence.model.Visit;
import ch.eugster.events.persistence.model.VisitAppliance;
import ch.eugster.events.persistence.model.VisitTheme;
import ch.eugster.events.persistence.model.VisitVisitor;
import ch.eugster.events.persistence.model.VisitVisitor.VisitorType;
import ch.eugster.events.persistence.model.Visitor;
import ch.eugster.events.persistence.queries.ApplianceQuery;
import ch.eugster.events.persistence.queries.SchoolLevelQuery;
import ch.eugster.events.persistence.queries.TeacherQuery;
import ch.eugster.events.persistence.queries.VisitThemeQuery;
import ch.eugster.events.persistence.queries.VisitorQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.person.editors.LinkPersonAddressEditorInput;
import ch.eugster.events.person.editors.PersonEditor;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;
import ch.eugster.events.visits.Activator;

public class VisitEditor extends AbstractEntityEditor<Teacher>
{
	public static final String ID = "ch.eugster.events.visits.visit.editor";

	/*
	 * Visit
	 */
	private ComboViewer stateViewer;

	private ComboViewer themeViewer;

	/*
	 * State
	 */
	/*
	 * Teacher Data
	 */
	private ComboViewer teacherViewer;

	private Text reachableTime;

	private ComboViewer teacherPhoneViewer;

	private ComboViewer teacherEmailViewer;

	private ImageHyperlink editPerson;

	private ImageHyperlink sendTeacherEmail;

	/*
	 * SchoolClass
	 */
	private ComboViewer schoolLevelViewer;

	private Text floor;

	private Text classRoom;

	private Text className;

	private Spinner pupils;

	/*
	 * Address
	 */
	private Text salutation;
	
	private Text name;
	
	private Text address;

	private Text pob;
	
	private Text anotherLine;

	private Text city;

	private Text province;
	
	private Text addressPhone;

	private Text addressFax;
	
	private Text addressEmail;

	private ImageHyperlink sendAddressEmail;

	/*
	 * Visitors
	 */
	private ComboViewer[] visitorViewers;

	private ComboViewer[] visitorPhoneViewers;

	private ComboViewer[] visitorEmailViewers;

	private ImageHyperlink[] sendVisitorEmails;

	private CDateTime start;

	private CDateTime end;

	private CheckboxTableViewer applianceViewer;

	/*
	 * Bemerkungen
	 */
	private Text notes;
	
	private ServiceTracker<ConnectionService, ConnectionService> connectionServiceTracker;

	@Override
	protected void initialize()
	{
	}

	@Override
	protected void createSections(ScrolledForm parent)
	{
		this.createVisitSection(parent);
		this.createThemeSection(parent);
		this.createTeacherSection(parent);
		this.createSchoolClassSection(parent);
		this.createAddressSection(parent);
		this.createVisitorSection(parent);
		this.createApplianceSection(parent);
		this.createNotesSection(parent);

		startConnectionTracking();
		addEntityListeners();
	}

	@Override
	public void postPersist(AbstractEntity entity)
	{
		if (entity instanceof VisitTheme)
		{
			themeViewer.add(entity);
		}
		else if (entity instanceof Teacher)
		{
			teacherViewer.add(entity);
		}
		else if (entity instanceof Visitor)
		{
			for (VisitorType visitorType : VisitVisitor.VisitorType.values())
			{
				visitorViewers[visitorType.ordinal()].add(entity);
			}
		}
		else if (entity instanceof SchoolLevel)
		{
			schoolLevelViewer.add(entity);
		}
		else if (entity instanceof Appliance)
		{
			applianceViewer.add(entity);
		}
		else if (entity instanceof Visit)
		{
			VisitEditorInput input = (VisitEditorInput) this.getEditorInput();
			if (input.getEntity().getId() != null && entity.getId().equals(input.getEntity().getId()))
			{
				Visit visit = (Visit) entity;
				input.setEntity(visit);
				this.loadValues();
			}
		}
	}

	@Override
	public void postUpdate(AbstractEntity entity)
	{
		if (entity instanceof VisitTheme)
		{
			themeViewer.refresh();
		}
		else if (entity instanceof Teacher)
		{
			teacherViewer.refresh();
		}
		else if (entity instanceof Visitor)
		{
			for (VisitorType visitorType : VisitVisitor.VisitorType.values())
			{
				visitorViewers[visitorType.ordinal()].refresh();
			}
		}
		else if (entity instanceof SchoolLevel)
		{
			schoolLevelViewer.refresh();
		}
		else if (entity instanceof Appliance)
		{
			applianceViewer.refresh();
		}
		else if (entity instanceof Visit)
		{
			VisitEditorInput input = (VisitEditorInput) this.getEditorInput();
			if (input.getEntity().getId() != null && entity.getId().equals(input.getEntity().getId()))
			{
				Visit visit = (Visit) entity;
				input.setEntity(visit);
				this.loadValues();
			}
		}
	}

	@Override
	public void postDelete(AbstractEntity entity)
	{
		if (entity instanceof VisitTheme)
		{
			themeViewer.refresh();
		}
		else if (entity instanceof Teacher)
		{
			teacherViewer.refresh();
		}
		else if (entity instanceof Visitor)
		{
			for (VisitorType visitorType : VisitVisitor.VisitorType.values())
			{
				visitorViewers[visitorType.ordinal()].refresh();
			}
		}
		else if (entity instanceof SchoolLevel)
		{
			schoolLevelViewer.refresh();
		}
		else if (entity instanceof Appliance)
		{
			applianceViewer.refresh();
		}
	}

	@Override
	public void postRemove(AbstractEntity entity)
	{
		if (entity instanceof VisitTheme)
		{
			themeViewer.refresh();
		}
		else if (entity instanceof Teacher)
		{
			teacherViewer.refresh();
		}
		else if (entity instanceof Visitor)
		{
			for (VisitorType visitorType : VisitVisitor.VisitorType.values())
			{
				visitorViewers[visitorType.ordinal()].refresh();
			}
		}
		else if (entity instanceof SchoolLevel)
		{
			schoolLevelViewer.refresh();
		}
		else if (entity instanceof Appliance)
		{
			applianceViewer.refresh();
		}
	}

	private void startConnectionTracking()
	{
		connectionServiceTracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null)
		{
			@Override
			public ConnectionService addingService(ServiceReference<ConnectionService> reference)
			{
				ConnectionService service = (ConnectionService) super.addingService(reference);
				VisitThemeQuery themeQuery = (VisitThemeQuery) service.getQuery(VisitTheme.class);
				themeViewer.setInput(themeQuery.selectAll(true).toArray(new VisitTheme[0]));
				TeacherQuery teacherQuery = (TeacherQuery) service.getQuery(Teacher.class);
				teacherViewer.setInput(teacherQuery.selectAll(false).toArray(new Teacher[0]));
				VisitorQuery visitorQuery = (VisitorQuery) service.getQuery(Visitor.class);
				Collection<Visitor> visitors = new ArrayList<Visitor>();
				visitors.add(Visitor.newInstance());
				visitors.addAll(visitorQuery.selectAll(Visitor.class));
				for (VisitorType visitorType : VisitorType.values())
				{
					ISelection selection = visitorViewers[visitorType.ordinal()].getSelection();
					visitorViewers[visitorType.ordinal()].setInput(visitors.toArray(new Visitor[0]));
					visitorViewers[visitorType.ordinal()].setSelection(selection);
				}
				SchoolLevelQuery schoolLevelQuery = (SchoolLevelQuery) service.getQuery(SchoolLevel.class);
				schoolLevelViewer.setInput(schoolLevelQuery.selectAll().toArray(new SchoolLevel[0]));
				ApplianceQuery applianceQuery = (ApplianceQuery) service.getQuery(Appliance.class);
				applianceViewer.setInput(applianceQuery.selectAll().toArray(new Appliance[0]));
				TableColumn[] tableColumns = applianceViewer.getTable().getColumns();
				for (TableColumn tableColumn : tableColumns)
				{
					tableColumn.pack();
				}
				return service;
			}

			@Override
			public void remove(ServiceReference<ConnectionService> reference)
			{
				themeViewer.setInput(new VisitTheme[0]);
				teacherViewer.setInput(new Teacher[0]);
				for (VisitorType visitorType : VisitorType.values())
				{
					ISelection selection = visitorViewers[visitorType.ordinal()].getSelection();
					visitorViewers[visitorType.ordinal()].setInput(new Visitor[0]);
					visitorViewers[visitorType.ordinal()].setSelection(selection);
				}
				schoolLevelViewer.setInput(new SchoolLevel[0]);
			}

		};
		connectionServiceTracker.open();
	}

	private void addEntityListeners()
	{
		EntityMediator.addListener(VisitTheme.class, this);
		EntityMediator.addListener(Teacher.class, this);
		EntityMediator.addListener(SchoolLevel.class, this);
		EntityMediator.addListener(Visitor.class, this);
		EntityMediator.addListener(Visit.class, this);
	}

	private void createThemeSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Thema");
		section.setClient(this.fillThemeSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				VisitEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillThemeSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Thema", SWT.NONE);
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT | SWT.FULL_SELECTION);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setCursor(combo.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

		this.themeViewer = new ComboViewer(combo);
		this.themeViewer.setContentProvider(new ArrayContentProvider());
		this.themeViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof VisitTheme)
				{
					VisitTheme theme = (VisitTheme) element;
					return theme.getName();
				}
				return "";
			}
		});
		this.themeViewer.setSorter(new ViewerSorter()
		{
			@Override
			public int compare(Viewer viewer, Object e1, Object e2)
			{
				VisitTheme vt1 = (VisitTheme) e1;
				VisitTheme vt2 = (VisitTheme) e2;
				return vt1.getName().compareTo(vt2.getName());
			}
		});
		this.themeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				setDirty(true);
			}
		});
		this.themeViewer.addFilter(new DeletedEntityFilter());

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private void createApplianceSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Geräte");
		section.setClient(this.fillApplianceSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				VisitEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillApplianceSection(Section parent)
	{
		GridLayout layout = new GridLayout();

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Table table = formToolkit.createTable(composite, SWT.CHECK | SWT.HIDE_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		this.applianceViewer = new CheckboxTableViewer(table);
		this.applianceViewer.setContentProvider(new ArrayContentProvider());
		this.applianceViewer.setSorter(new ViewerSorter()
		{
			@Override
			public int compare(Viewer viewer, Object e1, Object e2)
			{
				Appliance a1 = (Appliance) e1;
				Appliance a2 = (Appliance) e2;
				return a1.getName().compareTo(a2.getName());
			}
		});
		this.applianceViewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				setDirty(true);
			}
		});
		this.applianceViewer.addFilter(new DeletedEntityFilter());

		TableViewerColumn viewerColumn = new TableViewerColumn(applianceViewer, SWT.LEFT);
		viewerColumn.setLabelProvider(new CellLabelProvider()
		{
			@Override
			public void update(ViewerCell cell)
			{
				if (cell.getElement() instanceof Appliance)
				{
					cell.setText(((Appliance) cell.getElement()).getName());
				}
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private void createNotesSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Bemerkungen");
		section.setClient(this.fillNotesSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				VisitEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillNotesSection(Section parent)
	{
		GridLayout layout = new GridLayout();

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 48;
		notes = formToolkit.createText(composite, "");
		notes.setLayoutData(gridData);
		notes.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e) 
			{
				setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private void createTeacherSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Lehrperson");
		section.setClient(this.fillTeacherSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				VisitEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillTeacherSection(Section parent)
	{
		final Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(3, false));

		Label label = this.formToolkit.createLabel(composite, "Lehrperson", SWT.NONE);
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

		this.teacherViewer = new ComboViewer(combo);
		this.teacherViewer.setContentProvider(new ArrayContentProvider());
		this.teacherViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof Teacher)
				{
					Teacher teacher = (Teacher) element;
					return LinkPersonAddressFormatter.getInstance().formatPersonAndAddress(teacher.getLink());
				}
				return "";
			}
		});
		this.teacherViewer.setSorter(new ViewerSorter()
		{
			@Override
			public int compare(Viewer viewer, Object e1, Object e2)
			{
				Teacher t1 = (Teacher) e1;
				Teacher t2 = (Teacher) e2;
				String name1 = PersonFormatter.getInstance().formatLastnameFirstname(t1.getLink().getPerson());
				String name2 = PersonFormatter.getInstance().formatLastnameFirstname(t2.getLink().getPerson());
				return name1.compareTo(name2);
			}
		});
		this.teacherViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if (event.getSelection().isEmpty())
				{
					teacherPhoneViewer.setInput(new SelectedPhone[0]);
					VisitEditor.this.reachableTime.setText("");
					VisitEditor.this.teacherEmailViewer.setInput(new SelectedEmail[0]);
					VisitEditor.this.editPerson.setEnabled(false);
					VisitEditor.this.sendTeacherEmail.setEnabled(false);
					setAddressData(null);
				}
				else
				{
					StructuredSelection ssel = (StructuredSelection) event.getSelection();
					Teacher teacher = (Teacher) ssel.getFirstElement();

					VisitEditor.this.editPerson.setEnabled(true);

					SelectedPhone[] selectablePhones = getSelectablePhones(teacher);
					teacherPhoneViewer.setInput(selectablePhones);
					teacherPhoneViewer.setSelection(getSelectedPhone(teacher));

					VisitEditor.this.reachableTime.setText(getReachableTime(teacher));

					SelectedEmail[] selectableEmails = getSelectableEmails(teacher.getLink());
					teacherEmailViewer.setInput(selectableEmails);
					StructuredSelection sel = getSelectedEmail(teacher);
					teacherEmailViewer.setSelection(sel);
					VisitEditor.this.sendTeacherEmail.setEnabled(!sel.isEmpty());

					setAddressData(teacher);
				}
				setDirty(true);
			}
		});
		this.teacherViewer.addFilter(new DeletedEntityFilter());

		editPerson = new ImageHyperlink(composite, SWT.CENTER);
		editPerson.setImage(Activator.getDefault().getImageRegistry().get("edit"));
		editPerson.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		editPerson.addHyperlinkListener(new HyperlinkAdapter()
		{
			@Override
			public void linkActivated(HyperlinkEvent e)
			{
				if (!teacherViewer.getSelection().isEmpty())
				{
					StructuredSelection ssel = (StructuredSelection) teacherViewer.getSelection();
					Teacher teacher = (Teacher) ssel.getFirstElement();
					try
					{
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.openEditor(new LinkPersonAddressEditorInput(teacher.getLink()), PersonEditor.ID);
					}
					catch (PartInitException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});
		editPerson.setLayoutData(new GridData());
		formToolkit.adapt(editPerson);

		label = this.formToolkit.createLabel(composite, "Erreichbarkeit", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = composite.getFont().getFontData()[0].getHeight() * 4;
		gridData.horizontalSpan = 2;

		this.reachableTime = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL);
		this.reachableTime.setLayoutData(gridData);
		this.reachableTime.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				VisitEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Telefon", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setLayoutData(gridData);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

		this.teacherPhoneViewer = new ComboViewer(combo);
		this.teacherPhoneViewer.setContentProvider(new ArrayContentProvider());
		this.teacherPhoneViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof SelectedPhone)
				{
					if (!teacherViewer.getSelection().isEmpty())
					{
						StructuredSelection ssel = (StructuredSelection) teacherViewer.getSelection();
						Teacher teacher = (Teacher) ssel.getFirstElement();
						SelectedPhone phone = (SelectedPhone) element;
						return getFormattedPhone(teacher, teacher, phone);
					}
				}
				return "";
			}
		});
		this.teacherPhoneViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				VisitEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Email", SWT.NONE);
		label.setLayoutData(new GridData());

		combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

		this.teacherEmailViewer = new ComboViewer(combo);
		this.teacherEmailViewer.setContentProvider(new ArrayContentProvider());
		this.teacherEmailViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof SelectedEmail)
				{
					if (!teacherViewer.getSelection().isEmpty())
					{
						StructuredSelection ssel = (StructuredSelection) teacherViewer.getSelection();
						Teacher teacher = (Teacher) ssel.getFirstElement();
						SelectedEmail email = (SelectedEmail) element;
						return teacher.getEmail(email);
					}
				}
				return "";
			}
		});
		this.teacherEmailViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				VisitEditor.this.setDirty(true);
			}
		});

		sendTeacherEmail = new ImageHyperlink(composite, SWT.CENTER);
		sendTeacherEmail.setImage(Activator.getDefault().getImageRegistry().get("email"));
		sendTeacherEmail.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		sendTeacherEmail.addHyperlinkListener(new HyperlinkAdapter()
		{
			@Override
			public void linkActivated(HyperlinkEvent e)
			{
				StructuredSelection ssel = (StructuredSelection) teacherEmailViewer.getSelection();
				SelectedEmail email = (SelectedEmail) ssel.getFirstElement();

				if (!teacherViewer.getSelection().isEmpty())
				{
					ssel = (StructuredSelection) teacherViewer.getSelection();
					Teacher teacher = (Teacher) ssel.getFirstElement();
					sendEmail(teacher.getEmail(email));
				}
			}
		});
		sendTeacherEmail.setLayoutData(new GridData());
		formToolkit.adapt(sendTeacherEmail);

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private void createSchoolClassSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Schulklasse");
		section.setClient(this.fillSchoolClassSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				VisitEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillSchoolClassSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);


		Label label = this.formToolkit.createLabel(composite, "Schulklasse", SWT.NONE);
		label.setLayoutData(new GridData());

		this.className = this.formToolkit.createText(composite, "", SWT.FLAT);
		this.className.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.className.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				VisitEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Auswertungsstufe", SWT.NONE);
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

		this.schoolLevelViewer = new ComboViewer(combo);
		this.schoolLevelViewer.setContentProvider(new ArrayContentProvider());
		this.schoolLevelViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof SchoolLevel)
				{
					SchoolLevel schoolLevel = (SchoolLevel) element;
					return schoolLevel.getName();
				}
				return "";
			}
		});
		this.schoolLevelViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				VisitEditor.this.setDirty(true);
			}
		});
		this.schoolLevelViewer.addFilter(new DeletedEntityFilter());

		label = this.formToolkit.createLabel(composite, "Anzahl Schüler/innen", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData();
		gridData.widthHint = 64;

		this.pupils = new Spinner(composite, SWT.WRAP);
		this.pupils.setDigits(0);
		this.pupils.setIncrement(1);
		this.pupils.setPageIncrement(10);
		this.pupils.setMaximum(Integer.MAX_VALUE);
		this.pupils.setMinimum(0);
		this.pupils.setLayoutData(gridData);
		this.pupils.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				VisitEditor.this.setDirty(true);
			}
		});
		this.pupils.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		this.formToolkit.adapt(pupils);
		this.pupils.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				VisitEditor.this.setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private void createAddressSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Adresse");
		section.setClient(this.fillAddressSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				VisitEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillAddressSection(Section parent)
	{
		GridLayout layout = new GridLayout(3, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Anrede", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.salutation = this.formToolkit.createText(composite, "", SWT.FLAT);
		this.salutation.setLayoutData(gridData);
		this.salutation.setEnabled(false);

		label = this.formToolkit.createLabel(composite, "Bezeichnung", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.name = this.formToolkit.createText(composite, "", SWT.FLAT);
		this.name.setLayoutData(gridData);
		this.name.setEnabled(false);

		label = this.formToolkit.createLabel(composite, "Strasse", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.address = this.formToolkit.createText(composite, "");
		this.address.setLayoutData(gridData);
		this.address.setEnabled(false);
		
		label = this.formToolkit.createLabel(composite, "Postfach", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.pob = this.formToolkit.createText(composite, "", SWT.FLAT);
		this.pob.setLayoutData(gridData);
		this.pob.setEnabled(false);

		label = this.formToolkit.createLabel(composite, "Zusatzzeile", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.anotherLine = this.formToolkit.createText(composite, "", SWT.FLAT);
		this.anotherLine.setLayoutData(gridData);
		this.anotherLine.setEnabled(false);

		label = this.formToolkit.createLabel(composite, "Ort", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);

		this.city = this.formToolkit.createText(composite, "", SWT.FLAT);
		this.city.setLayoutData(gridData);
		this.city.setEnabled(false);

		gridData = new GridData();

		this.province = this.formToolkit.createText(composite, "", SWT.FLAT);
		this.province.setLayoutData(gridData);
		this.province.setEnabled(false);

		label = this.formToolkit.createLabel(composite, "Telefon", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.addressPhone = this.formToolkit.createText(composite, "", SWT.FLAT);
		this.addressPhone.setLayoutData(gridData);
		this.addressPhone.setEnabled(false);

		label = this.formToolkit.createLabel(composite, "Fax", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.addressFax = this.formToolkit.createText(composite, "", SWT.FLAT);
		this.addressFax.setLayoutData(gridData);
		this.addressFax.setEnabled(false);

		label = this.formToolkit.createLabel(composite, "Email", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.addressEmail = this.formToolkit.createText(composite, "", SWT.FLAT);
		this.addressEmail.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.addressEmail.setEnabled(false);

		this.sendAddressEmail = new ImageHyperlink(composite, SWT.CENTER);
		sendAddressEmail.setImage(Activator.getDefault().getImageRegistry().get("email"));
		sendAddressEmail.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		sendAddressEmail.addHyperlinkListener(new HyperlinkAdapter()
		{
			@Override
			public void linkActivated(HyperlinkEvent e)
			{
				sendEmail(addressEmail.getText());
			}
		});
		sendTeacherEmail.setLayoutData(new GridData());
		formToolkit.adapt(sendTeacherEmail);

		label = this.formToolkit.createLabel(composite, "Stockwerk", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.floor = this.formToolkit.createText(composite, "", SWT.FLAT);
		this.floor.setLayoutData(gridData);
		this.floor.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Schulzimmer", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.classRoom = this.formToolkit.createText(composite, "", SWT.FLAT);
		this.classRoom.setLayoutData(gridData);
		this.classRoom.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private void createVisitorSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Besucher/in");
		section.setClient(this.fillVisitorSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				VisitEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillVisitorSection(Section parent)
	{
		GridLayout layout = new GridLayout(3, false);

		final Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		VisitorType[] visitorTypes = VisitorType.values();
		visitorViewers = new ComboViewer[visitorTypes.length];
		visitorPhoneViewers = new ComboViewer[visitorTypes.length];
		visitorEmailViewers = new ComboViewer[visitorTypes.length];
		sendVisitorEmails = new ImageHyperlink[visitorTypes.length];
		for (int i = 0; i < visitorTypes.length; i++)
		{
			Label label = this.formToolkit.createLabel(composite, visitorTypes[i].label(), SWT.NONE);
			label.setLayoutData(new GridData());

			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;

			CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
			combo.setLayoutData(gridData);
			combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

			this.visitorViewers[i] = new ComboViewer(combo);
			this.visitorViewers[i].setData("visitor.type", visitorTypes[i]);
			this.visitorViewers[i].setContentProvider(new ArrayContentProvider());
			this.visitorViewers[i].setLabelProvider(new LabelProvider()
			{
				@Override
				public String getText(Object element)
				{
					if (element instanceof Visitor)
					{
						Visitor visitor = (Visitor) element;
						return visitor.isValid() ? LinkPersonAddressFormatter.getInstance().formatPersonAndAddress(visitor.getLink()) : "";
					}
					return "";
				}
			});
			this.visitorViewers[i].setSorter(new ViewerSorter()
			{
				@Override
				public int compare(Viewer viewer, Object e1, Object e2)
				{
					Visitor v1 = (Visitor) e1;
					Visitor v2 = (Visitor) e2;
					String name1 = v1.isValid() ? PersonFormatter.getInstance().formatLastnameFirstname(
							v1.getLink().getPerson()) : "";
					String name2 = v2.isValid() ? PersonFormatter.getInstance().formatLastnameFirstname(
							v2.getLink().getPerson()) : "";
					return name1.compareTo(name2);
				}
			});
			this.visitorViewers[i].addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(SelectionChangedEvent event)
				{
					ComboViewer viewer = (ComboViewer) event.getSource();
					VisitorType type = (VisitorType) viewer.getData("visitor.type");

					StructuredSelection ssel = (StructuredSelection) event.getSelection();
					Visitor visitor = (Visitor) ssel.getFirstElement();
					((VisitorPhoneLabelProvider) visitorPhoneViewers[type.ordinal()].getLabelProvider())
							.setVisitor(visitor);
					((VisitorEmailLabelProvider) visitorEmailViewers[type.ordinal()].getLabelProvider())
							.setVisitor(visitor);

					if (visitor == null || !visitor.isValid())
					{
						visitorPhoneViewers[type.ordinal()].setInput(new SelectedPhone[0]);
						visitorEmailViewers[type.ordinal()].setInput(new SelectedEmail[0]);
						sendVisitorEmails[type.ordinal()].setEnabled(false);
					}
					else
					{
						Visit visit = (Visit) getEditorInput().getAdapter(Visit.class);
						VisitVisitor v = visit.getVisitor(type);
						if (v == null || !v.getVisitor().getId().equals(visitor.getId()))
						{
							SelectedPhone[] selectablePhones = getSelectablePhones(visitor);
							visitorPhoneViewers[type.ordinal()].setInput(selectablePhones);
							visitorPhoneViewers[type.ordinal()].setSelection(selectPhone(selectablePhones));

							SelectedEmail[] selectableEmails = getSelectableEmails(visitor.getLink());
							visitorEmailViewers[type.ordinal()].setInput(selectableEmails);
							StructuredSelection sel = selectEmail(selectableEmails);
							visitorEmailViewers[type.ordinal()].setSelection(sel);
							sendVisitorEmails[type.ordinal()].setEnabled(!sel.isEmpty());
						}
						else if (v.getVisitor().getId().equals(visitor.getId()))
						{
							SelectedPhone[] selectablePhones = getSelectablePhones(visitor);
							visitorPhoneViewers[type.ordinal()].setInput(selectablePhones);
							visitorPhoneViewers[type.ordinal()].setSelection(getSelectedPhone(visitor));

							SelectedEmail[] selectableEmails = getSelectableEmails(visitor.getLink());
							visitorEmailViewers[type.ordinal()].setInput(selectableEmails);
							StructuredSelection sel = getSelectedEmail(visitor);
							visitorEmailViewers[type.ordinal()].setSelection(sel);
							sendVisitorEmails[type.ordinal()].setEnabled(!sel.isEmpty());
						}
					}
					setDirty(true);
				}
			});
			this.visitorViewers[i].addFilter(new DeletedEntityFilter());

			label = this.formToolkit.createLabel(composite, "Telefon", SWT.NONE);
			label.setLayoutData(new GridData());

			gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;

			combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
			combo.setLayoutData(gridData);
			combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

			this.visitorPhoneViewers[i] = new ComboViewer(combo);
			this.visitorPhoneViewers[i].setData("visitor.type", visitorTypes[i]);
			this.visitorPhoneViewers[i].setContentProvider(new ArrayContentProvider());
			this.visitorPhoneViewers[i].setLabelProvider(new VisitorPhoneLabelProvider());
			this.visitorPhoneViewers[i].addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(SelectionChangedEvent event)
				{
					VisitEditor.this.setDirty(true);
				}
			});

			label = this.formToolkit.createLabel(composite, "Email", SWT.NONE);
			label.setLayoutData(new GridData());

			combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
			combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

			this.visitorEmailViewers[i] = new ComboViewer(combo);
			this.visitorEmailViewers[i].setData("visitor.type", visitorTypes[i]);
			this.visitorEmailViewers[i].setContentProvider(new ArrayContentProvider());
			this.visitorEmailViewers[i].setLabelProvider(new VisitorEmailLabelProvider(visitorEmailViewers[i]));
			this.visitorEmailViewers[i].addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(SelectionChangedEvent event)
				{
					VisitEditor.this.setDirty(true);
				}
			});

			sendVisitorEmails[i] = new ImageHyperlink(composite, SWT.CENTER);
			sendVisitorEmails[i].setImage(Activator.getDefault().getImageRegistry().get("email"));
			sendVisitorEmails[i].setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
			sendVisitorEmails[i].setData("visitor.type", VisitorType.values()[i]);
			sendVisitorEmails[i].addHyperlinkListener(new HyperlinkAdapter()
			{
				@Override
				public void linkActivated(HyperlinkEvent e)
				{
					VisitorType visitorType = (VisitorType) ((ImageHyperlink) e.getSource()).getData("visitor.type");
					if (!visitorEmailViewers[visitorType.ordinal()].getSelection().isEmpty())
					{
						StructuredSelection ssel = (StructuredSelection) visitorEmailViewers[visitorType.ordinal()]
								.getSelection();
						SelectedEmail email = (SelectedEmail) ssel.getFirstElement();

						if (!visitorViewers[visitorType.ordinal()].getSelection().isEmpty())
						{
							ssel = (StructuredSelection) visitorViewers[visitorType.ordinal()].getSelection();
							Visitor visitor = (Visitor) ssel.getFirstElement();
							sendEmail(visitor.getEmail(email));
						}
					}
				}

			});
			sendVisitorEmails[i].setLayoutData(new GridData());
			formToolkit.adapt(sendVisitorEmails[i]);
		}

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private void createVisitSection(ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		Section section = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		section.setLayoutData(layoutData);
		section.setLayout(sectionLayout);
		section.setText("Schulbesuch");
		section.setClient(this.fillVisitSection(section));
		section.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(ExpansionEvent e)
			{
				VisitEditor.this.scrolledForm.reflow(true);
			}
		});
	}

	private Control fillVisitSection(Section parent)
	{
		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Beginn", SWT.NONE);
		label.setLayoutData(new GridData());

		GC gc = new GC(composite.getDisplay());
		GridData gridData = new GridData();
		gridData.widthHint = gc.stringExtent("00.00.0000 00:00 0000").x;

		start = new CDateTime(composite, CDT.CLOCK_12_HOUR | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		start.setLayoutData(gridData);
		start.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		start.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (start.getSelection() != null)
				{
					if (end.getSelection() == null || end.getSelection().before(start.getSelection()))
					{
						Calendar calendar = GregorianCalendar.getInstance();
						calendar.setTime(start.getSelection());
						calendar.add(Calendar.HOUR_OF_DAY, 4);
						end.setSelection(calendar.getTime());
					}
				}
				VisitEditor.this.setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});
		formToolkit.adapt(start);

		label = this.formToolkit.createLabel(composite, "Ende", SWT.NONE);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = gc.stringExtent("00.00.0000 00:00 0000").x;
		gc.dispose();

		end = new CDateTime(composite, CDT.CLOCK_12_HOUR | CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		end.setLayoutData(gridData);
		end.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
		end.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				VisitEditor.this.setDirty(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				widgetSelected(e);
			}
		});
		formToolkit.adapt(end);

		label = this.formToolkit.createLabel(composite, "Status", SWT.NONE);
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

		this.stateViewer = new ComboViewer(combo);
		this.stateViewer.setContentProvider(new ArrayContentProvider());
		this.stateViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element instanceof Visit.State)
				{
					Visit.State state = (Visit.State) element;
					return state.label();
				}
				return "";
			}
		});
		this.stateViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				VisitEditor.this.setDirty(true);
			}
		});
		this.stateViewer.setInput(Visit.State.values());

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private String getFormattedPhone(LinkPersonAddressChild child, ISelectedPhoneProvider selectedPhoneProvider,
			SelectedPhone selectedPhone)
	{
		String phoneNumber = selectedPhoneProvider.getPhone(selectedPhone);
		if (phoneNumber.isEmpty())
		{
			return phoneNumber;
		}
		try
		{
			MaskFormatter formatter = new MaskFormatter(child.getLink().getAddress().getCountry().getPhonePattern());
			formatter.setValue(phoneNumber);
			return formatter.getDisplayString();
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		return "";
	}

	@Override
	protected Message getMessage(PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		if (errorCode.equals(""))
		{
			msg = this.getUniqueNameMessage();
		}
		return msg;
	}

	private Message getUniqueNameMessage()
	{
		Message msg = null;

		return msg;
	}

	private Message getEmptyNameMessage()
	{
		Message msg = null;

		return msg;
	}

	@Override
	protected String getName()
	{
		VisitEditorInput input = (VisitEditorInput) this.getEditorInput();
		Visit visit = (Visit) input.getAdapter(Visit.class);
		return visit.getId() == null ? "Neu" : "S" + visit.getId().toString();
	}

	@Override
	protected String getText()
	{
		VisitEditorInput input = (VisitEditorInput) this.getEditorInput();
		Visit visit = (Visit) input.getAdapter(Visit.class);
		DateFormat format = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		return visit.getId() == null ? "Neuer Schulbesuch" : "Schulbesuch "
				+ (visit.getStart() == null ? "" : format.format(visit.getStart().getTime()));
	}

	private SelectedPhone[] getSelectablePhones(LinkPersonAddressChild child)
	{
		List<SelectedPhone> phones = new ArrayList<SelectedPhone>();
		phones.add(SelectedPhone.NONE);
		if (!child.getLink().getPerson().getPhone().isEmpty())
		{
			phones.add(SelectedPhone.PERSON);
		}
		if (!child.getLink().getPhone().isEmpty())
		{
			phones.add(SelectedPhone.LINK);
		}
		if (!child.getLink().getAddress().getPhone().isEmpty())
		{
			phones.add(SelectedPhone.ADDRESS);
		}
		return phones.toArray(new SelectedPhone[0]);
	}

	private StructuredSelection getSelectedPhone(ISelectedPhoneProvider selectedPhoneProvider)
	{
		SelectedPhone phone = selectedPhoneProvider.getSelectedPhone();
		if (phone == null)
		{
			return new StructuredSelection();
		}
		else
		{
			return new StructuredSelection(new SelectedPhone[] { phone });
		}
	}

	private StructuredSelection selectPhone(SelectedPhone[] phones)
	{
		if (phones.length > 1)
		{
			return new StructuredSelection(new SelectedPhone[] { phones[1] });
		}
		else
		{
			return new StructuredSelection();
		}
	}

	private SelectedEmail[] getSelectableEmails(LinkPersonAddress link)
	{
		Collection<SelectedEmail> emails = new ArrayList<SelectedEmail>();
		emails.add(SelectedEmail.NONE);
		if (!link.getPerson().getEmail().isEmpty())
		{
			emails.add(SelectedEmail.PERSON);
		}
		if (!link.getEmail().isEmpty())
		{
			emails.add(SelectedEmail.LINK);
		}
		if (!link.getAddress().getEmail().isEmpty())
		{
			emails.add(SelectedEmail.ADDRESS);
		}
		return emails.toArray(new SelectedEmail[0]);
	}

	private StructuredSelection getSelectedEmail(ISelectedEmailProvider selectedEmailProvider)
	{
		SelectedEmail email = selectedEmailProvider.getSelectedEmail();
		if (email == null)
		{
			return new StructuredSelection();
		}
		else
		{
			return new StructuredSelection(new SelectedEmail[] { email });
		}
	}

	private StructuredSelection selectEmail(SelectedEmail[] emails)
	{
		if (emails.length > 1)
		{
			return new StructuredSelection(new SelectedEmail[] { emails[1] });
		}
		else
		{
			return new StructuredSelection();
		}
	}

	private String getReachableTime(Teacher teacher)
	{
		VisitEditorInput input = (VisitEditorInput) this.getEditorInput();
		Visit visit = (Visit) input.getAdapter(Visit.class);
		String text = visit.getBestReachTime();
		if (text == null || text.isEmpty())
		{
			text = teacher.getBestReachTime();
		}
		return text == null ? "" : text;
	}

	private void setAddressData(Teacher teacher)
	{
		if (teacher == null)
		{
			this.salutation.setText("");
			this.name.setText("");
			this.address.setText("");
			this.pob.setText("");
			this.anotherLine.setText("");
			this.city.setText("");
			this.province.setText("");
			this.addressPhone.setText("");
			this.addressFax.setText("");
			this.addressEmail.setText("");
			this.sendAddressEmail.setEnabled(!this.addressEmail.getText().isEmpty());
		}
		else
		{
			AddressSalutation salutation = teacher.getLink().getAddress().getSalutation();
			this.salutation.setText(salutation == null ? "" : salutation.getSalutation());
			this.name.setText(teacher.getLink().getAddress().getName());
			this.address.setText(teacher.getLink().getAddress().getAddress());
			this.pob.setText(teacher.getLink().getAddress().getPob());
			this.anotherLine.setText(teacher.getLink().getAddress().getAnotherLine());
			this.city.setText(AddressFormatter.getInstance().formatCityLine(teacher.getLink().getAddress()));
			String province = teacher.getLink().getAddress().getZipCode() == null ? teacher.getLink().getAddress().getProvince() : teacher.getLink()
					.getAddress().getZipCode().getState();
			if (province != null)
			{
				this.province.setText(province);
			}
			this.addressPhone.setText(teacher.getLink().getAddress().getPhone());
			this.addressFax.setText(teacher.getLink().getAddress().getPhone());
			this.addressEmail.setText(teacher.getLink().getAddress().getEmail());
			this.sendAddressEmail.setEnabled(!this.addressEmail.getText().isEmpty());
		}
	}

	@Override
	protected void loadValues()
	{
		VisitEditorInput input = (VisitEditorInput) this.getEditorInput();
		Visit visit = (Visit) input.getAdapter(Visit.class);

		StructuredSelection ssel = visit.getTheme() == null ? new StructuredSelection() : new StructuredSelection(
				new VisitTheme[] { visit.getTheme() });
		this.themeViewer.setSelection(ssel);

		SchoolLevel schoolLevel = visit.getSchoolLevel();
		if (schoolLevel != null)
		{
			schoolLevelViewer.setSelection(new StructuredSelection(new SchoolLevel[] { schoolLevel }));
		}
		ssel = new StructuredSelection(visit.getState() == null ? Visit.State.PROVISORILY : visit.getState());
		this.stateViewer.setSelection(ssel);

		Teacher teacher = visit.getTeacher();
		if (teacher == null)
		{
			this.teacherViewer.setSelection(new StructuredSelection());
		}
		else
		{
			ssel = new StructuredSelection(new Teacher[] { teacher });
			this.teacherViewer.setSelection(ssel);
			this.setAddressData(teacher);
		}

		ssel = visit.getSelectedPhone() == null ? new StructuredSelection() : new StructuredSelection(
				new SelectedPhone[] { visit.getSelectedPhone() });
		if (ssel.isEmpty())
		{
			ssel = teacher == null || teacher.getSelectedPhone() == null ? new StructuredSelection()
					: new StructuredSelection(new SelectedPhone[] { teacher.getSelectedPhone() });
		}
		this.teacherPhoneViewer.setSelection(ssel);

		String time = visit.getBestReachTime();
		if (time == null)
		{
			if (teacher != null)
			{
				time = teacher.getBestReachTime();
			}
		}
		this.reachableTime.setText(Visit.stringValueOf(time == null ? "" : time));

		ssel = visit.getSelectedEmail() == null ? new StructuredSelection() : new StructuredSelection(
				new SelectedEmail[] { visit.getSelectedEmail() });
		if (ssel.isEmpty())
		{
			ssel = teacher == null || teacher.getSelectedEmail() == null ? new StructuredSelection()
					: new StructuredSelection(new SelectedEmail[] { teacher.getSelectedEmail() });
		}

		this.end.setSelection(visit.getEnd() == null ? null : visit.getEnd().getTime());
		this.start.setSelection(visit.getStart() == null ? null : visit.getStart().getTime());

		this.className.setText(visit.getClassName() == null ? "" : visit.getClassName());
		this.pupils.setSelection(visit.getPupils());

		this.floor.setText(visit.getFloor() == null ? "" : visit.getFloor());
		this.classRoom.setText(visit.getClassRoom() == null ? "" : visit.getClassRoom());

		for (VisitorType visitorType : VisitorType.values())
		{
			for (VisitVisitor visitVisitor : visit.getVisitors())
			{
				if (visitVisitor.getType().equals(visitorType))
				{
					if (!visitVisitor.isDeleted())
					{
						StructuredSelection sel = new StructuredSelection(new Visitor[] { visitVisitor.getVisitor() });
						visitorViewers[visitorType.ordinal()].setSelection(sel);
						sel = new StructuredSelection(
								new SelectedPhone[] { visitVisitor.getSelectedPhone() == null ? SelectedPhone.NONE
										: visitVisitor.getSelectedPhone() });
						visitorPhoneViewers[visitorType.ordinal()].setSelection(sel);
						sel = new StructuredSelection(
								new SelectedEmail[] { visitVisitor.getSelectedEmail() == null ? SelectedEmail.NONE
										: visitVisitor.getSelectedEmail() });
						visitorEmailViewers[visitorType.ordinal()].setSelection(sel);
					}
				}
			}
		}
		Collection<VisitAppliance> visitAppliances = visit.getAppliances();
		Appliance[] appliances = (Appliance[]) applianceViewer.getInput();
		for (VisitAppliance visitAppliance : visitAppliances)
		{
			for (Appliance appliance : appliances)
			{
				if (visitAppliance.getAppliance().getId().equals(appliance.getId()))
				{
					applianceViewer.setChecked(appliance, true);
				}
			}
		}
		this.notes.setText(visit.getNotes());
		this.setDirty(false);
	}

	@Override
	protected void saveValues()
	{
		VisitEditorInput input = (VisitEditorInput) this.getEditorInput();
		Visit visit = input.getEntity();

		StructuredSelection ssel = (StructuredSelection) themeViewer.getSelection();
		visit.setTheme(ssel.isEmpty() ? null : (VisitTheme) ssel.getFirstElement());
		Calendar calendar = GregorianCalendar.getInstance();
		if (start.getSelection() != null)
		{
			calendar.setTime(start.getSelection());
		}
		visit.setStart(start.getSelection() == null ? null : calendar);
		calendar = GregorianCalendar.getInstance();
		if (end.getSelection() != null)
		{
			calendar.setTime(end.getSelection());
		}
		visit.setEnd(end.getSelection() == null ? null : calendar);
		ssel = (StructuredSelection) stateViewer.getSelection();
		visit.setState(ssel.isEmpty() ? Visit.State.PROVISORILY : (Visit.State) ssel.getFirstElement());
		ssel = (StructuredSelection) schoolLevelViewer.getSelection();
		visit.setSchoolLevel(ssel.isEmpty() ? null : (SchoolLevel) ssel.getFirstElement());
		ssel = (StructuredSelection) teacherViewer.getSelection();
		visit.setTeacher(ssel.isEmpty() ? null : (Teacher) ssel.getFirstElement());
		visit.setBestReachTime(reachableTime.getText());
		ssel = (StructuredSelection) teacherPhoneViewer.getSelection();
		visit.setSelectedPhone(ssel.isEmpty() ? SelectedPhone.NONE : (SelectedPhone) ssel.getFirstElement());
		ssel = (StructuredSelection) teacherEmailViewer.getSelection();
		visit.setSelectedEmail(ssel.isEmpty() ? SelectedEmail.NONE : (SelectedEmail) ssel.getFirstElement());
		visit.setClassName(className.getText());
		visit.setPupils(pupils.getSelection());
		visit.setFloor(floor.getText());
		visit.setClassRoom(classRoom.getText());
		for (VisitorType visitorType : VisitorType.values())
		{
			ssel = (StructuredSelection) visitorViewers[visitorType.ordinal()].getSelection();
			Visitor visitor = (Visitor) ssel.getFirstElement();
			boolean found = false;
			for (VisitVisitor visitVisitor : visit.getVisitors())
			{
				if (visitVisitor.getType().equals(visitorType))
				{
					found = true;
					if (visitor == null || !visitor.isValid())
					{
						visitVisitor.setDeleted(true);
					}
					else
					{
						if (!visitVisitor.getVisitor().getId().equals(visitor.getId()))
						{
							visitVisitor.setVisitor(visitor);
						}
						visitVisitor.setDeleted(false);
						StructuredSelection sel = (StructuredSelection) visitorPhoneViewers[visitorType.ordinal()]
								.getSelection();
						visitVisitor.setSelectedPhone(sel.isEmpty() ? SelectedPhone.NONE : (SelectedPhone) sel
								.getFirstElement());
						sel = (StructuredSelection) visitorEmailViewers[visitorType.ordinal()].getSelection();
						visitVisitor.setSelectedEmail(sel.isEmpty() ? SelectedEmail.NONE : (SelectedEmail) sel
								.getFirstElement());
					}
				}
			}
			if (!found)
			{
				if (visitor != null && visitor.getLink() != null)
				{
					VisitVisitor v = VisitVisitor.newInstance(visit, visitor);
					StructuredSelection sel = (StructuredSelection) visitorPhoneViewers[visitorType.ordinal()]
							.getSelection();
					v.setType(visitorType);
					v.setSelectedPhone(sel.isEmpty() ? SelectedPhone.NONE : (SelectedPhone) sel.getFirstElement());
					sel = (StructuredSelection) visitorEmailViewers[visitorType.ordinal()].getSelection();
					v.setSelectedEmail(sel.isEmpty() ? SelectedEmail.NONE : (SelectedEmail) sel.getFirstElement());
					visit.addVisitor(v);
				}
			}
		}
		Collection<VisitAppliance> visitAppliances = visit.getAppliances();
		Appliance[] appliances = (Appliance[]) applianceViewer.getInput();
		for (Appliance appliance : appliances)
		{
			boolean found = false;
			for (VisitAppliance visitAppliance : visitAppliances)
			{
				if (visitAppliance.getAppliance().getId().equals(appliance.getId()))
				{
					found = true;
					visitAppliance.setDeleted(!applianceViewer.getChecked(appliance));
					break;
				}
			}
			if (applianceViewer.getChecked(appliance) && !found)
			{
				VisitAppliance visitAppliance = VisitAppliance.newInstance(visit, appliance);
				visit.addAppliance(visitAppliance);
			}
		}
		visit.setNotes(this.notes.getText());
	}

	@Override
	protected boolean validate()
	{
		Message msg = this.getUniqueNameMessage();

		if (msg == null)
			msg = this.getEmptyNameMessage();

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(AbstractEntityEditorInput<Teacher> input)
	{
		return input.getAdapter(Teacher.class) instanceof Teacher;
	}

	@Override
	public void setFocus()
	{
		this.reachableTime.setFocus();
	}

	@Override
	public void dispose()
	{
		connectionServiceTracker.close();

		EntityMediator.removeListener(VisitTheme.class, this);
		EntityMediator.removeListener(Teacher.class, this);
		EntityMediator.removeListener(SchoolLevel.class, this);
		EntityMediator.removeListener(Visitor.class, this);
		EntityMediator.removeListener(Visit.class, this);
	}

	private void sendEmail(String email)
	{
		if (email == null || email.isEmpty())
		{
			return;
		}
		if (Desktop.getDesktop().isSupported(Action.MAIL))
		{
			if (!teacherEmailViewer.getSelection().isEmpty())
			{
				URI uri;
				try
				{
					uri = new URI("mailto:" + email);
					Desktop.getDesktop().mail(uri);
				}
				catch (URISyntaxException e1)
				{
					MessageDialog
							.openWarning(
									this.getSite().getShell(),
									"Ungültige Email-Adresse",
									"Die gewählte Emailadresse ist ungültig. Korrigieren Sie die Adresse im Personen-Editor der Lehrperson und versuchen Sie es erneut.");
				}
				catch (IOException e2)
				{
					MessageDialog.openWarning(this.getSite().getShell(), "Initialisierungsfehler", "Das "
							+ "Emailprogramm kann nicht initialisiert werden. Der Vorgang wird abgebrochen.");
				}
			}
		}
	}
}
