package ch.eugster.events.course.editors;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.nebula.jface.viewer.radiogroup.RadioGroupViewer;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.radiogroup.RadioGroup;
import org.eclipse.nebula.widgets.radiogroup.forms.RadioGroupFormToolkit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.course.views.CourseEditorContentOutlinePage;
import ch.eugster.events.domain.viewers.DomainComboContentProvider;
import ch.eugster.events.domain.viewers.DomainComboLabelProvider;
import ch.eugster.events.domain.viewers.DomainComboSorter;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.filters.DeletedEntityFilter;
import ch.eugster.events.persistence.model.Category;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseSexConstraint;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.Domain;
import ch.eugster.events.persistence.model.GlobalSettings;
import ch.eugster.events.persistence.model.PaymentTerm;
import ch.eugster.events.persistence.model.Rubric;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.CategoryQuery;
import ch.eugster.events.persistence.queries.CourseQuery;
import ch.eugster.events.persistence.queries.DomainQuery;
import ch.eugster.events.persistence.queries.PaymentTermQuery;
import ch.eugster.events.persistence.queries.RubricQuery;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.editors.AbstractEntityEditor;
import ch.eugster.events.ui.editors.AbstractEntityEditorInput;

public class CourseEditor extends AbstractEntityEditor<Course> implements PropertyChangeListener, EventHandler
{
	private static final int widthHint = 180;

	public static final String ID = "ch.eugster.events.course.editors.courseEditor";

	private static final String COURSE_EDITOR = "course.editor";

	private static final String NAME_SECTION_EXPANDED = "name.section.expanded";

	private static final String CLASSIFICATION_SECTION_EXPANDED = "classification.section.expanded";

	private static final String CONTENTS_SECTION_EXPANDED = "contents.section.expanded";

	private static final String CONSTRAINTS_SECTION_EXPANDED = "constraints.section.expanded";

	private static final String INFORMATION_SECTION_EXPANDED = "information.section.expanded";

	private static final String STATE_SECTION_EXPANDED = "state.section.expanded";

	private ComboViewer domainViewer;

	private ComboViewer categoryViewer;

	private ComboViewer rubricViewer;

	private ComboViewer userViewer;

	private ComboViewer paymentTermViewer;

	private Text code;

	private Text title;

	private Text teaser;

	private Text description;

	private Text boarding;

	private Text lodging;

//	private Text purpose;

	private Text contents;

	private Text materialParticipants;

	private Text materialOrganizer;

	private Text targetPublic;
	
	private Text prerequisites;

	private Spinner minParticipants;

	private Spinner maxParticipants;

	private Spinner minAge;

	private Spinner maxAge;

	private RadioGroupViewer sexRadioGroupViewer;

	// private Text infoMeeting;

	private Text information;

	private Text realization;
	
	private Text costNote;

	private CDateTime lastBookingDate;

	private CDateTime advanceNoticeDate;
	
	private CDateTime advanceNoticeDoneDate;
	
	private CDateTime invitationDate;

	private CDateTime invitationDoneDate;

	private CDateTime lastAnnulationDate;

	private CDateTime annulationDate;

	private ComboViewer stateViewer;

	private Button substituted;

	private Section nameSection;

	private Section classificationSection;

	private Section contentsSection;

	private Section constraintsSection;

	private Section informationSection;

	private Section stateSection;

	private IDialogSettings dialogSettings;

	private ServiceRegistration<EventHandler> eventHandlerRegistration;
	
	private void createClassificationSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.classificationSection = this.formToolkit.createSection(this.scrolledForm.getBody(),
				ExpandableComposite.EXPANDED | ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR
						| ExpandableComposite.TWISTIE);
		this.classificationSection.setLayoutData(layoutData);
		this.classificationSection.setLayout(sectionLayout);
		this.classificationSection.setText("Klassifizierung");
		this.classificationSection.setClient(this.fillClassificationSection(this.classificationSection));
		this.classificationSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				CourseEditor.this.dialogSettings.put(CourseEditor.CLASSIFICATION_SECTION_EXPANDED, e.getState());
				CourseEditor.this.scrolledForm.reflow(true);
			}
		});
		this.classificationSection.setExpanded(this.dialogSettings
				.getBoolean(CourseEditor.CLASSIFICATION_SECTION_EXPANDED));
	}

	private void createConstraintsSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.constraintsSection = this.formToolkit.createSection(this.scrolledForm.getBody(),
				ExpandableComposite.EXPANDED | ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR
						| ExpandableComposite.TWISTIE);
		this.constraintsSection.setLayoutData(layoutData);
		this.constraintsSection.setLayout(sectionLayout);
		this.constraintsSection.setText("Teilnahmebeschränkungen");
		this.constraintsSection.setClient(this.fillConstraintsSection(this.constraintsSection));
		this.constraintsSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				CourseEditor.this.dialogSettings.put(CourseEditor.CONSTRAINTS_SECTION_EXPANDED, e.getState());
				CourseEditor.this.scrolledForm.reflow(true);
			}
		});
		this.constraintsSection.setExpanded(this.dialogSettings.getBoolean(CourseEditor.CONSTRAINTS_SECTION_EXPANDED));
	}

	private void createContentsSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.contentsSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.contentsSection.setLayoutData(layoutData);
		this.contentsSection.setLayout(sectionLayout);
		this.contentsSection.setText("Kursziele, Kursinhalte");
		this.contentsSection.setClient(this.fillContentsSection(this.contentsSection));
		this.contentsSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				CourseEditor.this.dialogSettings.put(CourseEditor.CONTENTS_SECTION_EXPANDED, e.getState());
				CourseEditor.this.scrolledForm.reflow(true);
			}
		});
		this.contentsSection.setExpanded(this.dialogSettings.getBoolean(CourseEditor.CONTENTS_SECTION_EXPANDED));
	}

	private void createInformationSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.informationSection = this.formToolkit.createSection(this.scrolledForm.getBody(),
				ExpandableComposite.EXPANDED | ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR
						| ExpandableComposite.TWISTIE);
		this.informationSection.setLayoutData(layoutData);
		this.informationSection.setLayout(sectionLayout);
		this.informationSection.setText("Informationen");
		this.informationSection.setClient(this.fillInformationSection(this.informationSection));
		this.informationSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				CourseEditor.this.dialogSettings.put(CourseEditor.INFORMATION_SECTION_EXPANDED, e.getState());
				CourseEditor.this.scrolledForm.reflow(true);
			}
		});
		this.informationSection.setExpanded(this.dialogSettings.getBoolean(CourseEditor.INFORMATION_SECTION_EXPANDED));
	}

	private void createNameSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.nameSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.nameSection.setLayoutData(layoutData);
		this.nameSection.setLayout(sectionLayout);
		this.nameSection.setText("Identifikation und Bezeichnung");
		this.nameSection.setClient(this.fillNameSection(this.nameSection));
		this.nameSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				CourseEditor.this.dialogSettings.put(CourseEditor.NAME_SECTION_EXPANDED, e.getState());
				CourseEditor.this.scrolledForm.reflow(true);
			}
		});
		this.nameSection.setExpanded(this.dialogSettings.getBoolean(CourseEditor.NAME_SECTION_EXPANDED));
	}

	@Override
	protected void createSections(final ScrolledForm parent)
	{
		this.createNameSection(parent);
		this.createClassificationSection(parent);
		this.createContentsSection(parent);
		this.createConstraintsSection(parent);
		this.createInformationSection(parent);
		this.createStateSection(parent);
	}

	private void createStateSection(final ScrolledForm parent)
	{
		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;

		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.stateSection = this.formToolkit.createSection(this.scrolledForm.getBody(), ExpandableComposite.EXPANDED
				| ExpandableComposite.COMPACT | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.stateSection.setLayoutData(layoutData);
		this.stateSection.setLayout(sectionLayout);
		this.stateSection.setText("Status");
		this.stateSection.setClient(this.fillStateSection(this.stateSection));
		this.stateSection.addExpansionListener(new ExpansionAdapter()
		{
			@Override
			public void expansionStateChanged(final ExpansionEvent e)
			{
				CourseEditor.this.dialogSettings.put(CourseEditor.STATE_SECTION_EXPANDED, e.getState());
				CourseEditor.this.scrolledForm.reflow(true);

			}
		});
		this.stateSection.setExpanded(this.dialogSettings.getBoolean(CourseEditor.STATE_SECTION_EXPANDED));
	}

	@Override
	public void dispose()
	{
		eventHandlerRegistration.unregister();
		super.dispose();
	}

	private Control fillClassificationSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
	
			if (GlobalSettings.getInstance().getCourseHasDomain())
			{
				Label label = this.formToolkit.createLabel(composite, "Domäne");
				label.setLayoutData(new GridData());
	
				CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
				combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
				combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				this.formToolkit.adapt(combo);
	
				this.domainViewer = new ComboViewer(combo);
				this.domainViewer.setContentProvider(new DomainComboContentProvider(GlobalSettings.getInstance()
						.isCourseDomainMandatory()));
				this.domainViewer.setLabelProvider(new DomainComboLabelProvider());
				this.domainViewer.setSorter(new DomainComboSorter());
				this.domainViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
	
				if (service != null)
				{
					DomainQuery query = (DomainQuery) service.getQuery(Domain.class);
					List<Domain> domains = query.selectAll();
					this.domainViewer.setInput(domains.toArray(new Domain[0]));
				}
				this.domainViewer.addSelectionChangedListener(new ISelectionChangedListener()
				{
					@Override
					public void selectionChanged(final SelectionChangedEvent event)
					{
						CourseEditor.this.setDirty(true);
					}
				});
			}
	
			if (GlobalSettings.getInstance().getCourseHasCategory())
			{
				Label label = this.formToolkit.createLabel(composite, "Kategorie");
				label.setLayoutData(new GridData());
	
				CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
				combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
				combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				this.formToolkit.adapt(combo);
	
				this.categoryViewer = new ComboViewer(combo);
				this.categoryViewer.setContentProvider(new CategoryContentProvider(GlobalSettings.getInstance()
						.isCourseCategoryMandatory()));
				this.categoryViewer.setLabelProvider(new CategoryLabelProvider());
				this.categoryViewer.setSorter(new CategorySorter());
				this.categoryViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
	
				if (service != null)
				{
					CategoryQuery query = (CategoryQuery) service.getQuery(Category.class);
					List<Category> categories = query.selectAll();
					this.categoryViewer.setInput(categories.toArray(new Category[0]));
				}
				this.categoryViewer.addSelectionChangedListener(new ISelectionChangedListener()
				{
					@Override
					public void selectionChanged(final SelectionChangedEvent event)
					{
						CourseEditor.this.setDirty(true);
					}
				});
			}
	
			if (GlobalSettings.getInstance().getCourseHasRubric())
			{
				Label label = this.formToolkit.createLabel(composite, "Rubrik");
				label.setLayoutData(new GridData());
	
				CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
				combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
				combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				this.formToolkit.adapt(combo);
	
				this.rubricViewer = new ComboViewer(combo);
				this.rubricViewer.setContentProvider(new RubricContentProvider(GlobalSettings.getInstance()
						.isCourseRubricMandatory()));
				this.rubricViewer.setLabelProvider(new RubricLabelProvider());
				this.rubricViewer.setSorter(new RubricSorter());
				this.rubricViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
	
				if (service != null)
				{
					RubricQuery query = (RubricQuery) service.getQuery(Rubric.class);
					List<Rubric> rubrics = query.selectAll();
					this.rubricViewer.setInput(rubrics.toArray(new Rubric[0]));
				}
				this.rubricViewer.addSelectionChangedListener(new ISelectionChangedListener()
				{
					@Override
					public void selectionChanged(final SelectionChangedEvent event)
					{
						CourseEditor.this.setDirty(true);
					}
				});
			}
	
			Label label = this.formToolkit.createLabel(composite, "Zahlungsbedingungen");
			label.setLayoutData(new GridData());
	
			CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
			combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
			combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			this.formToolkit.adapt(combo);
	
			this.paymentTermViewer = new ComboViewer(combo);
			this.paymentTermViewer.setContentProvider(new ArrayContentProvider());
			this.paymentTermViewer.setLabelProvider(new PaymentTermLabelProvider());
			this.paymentTermViewer.setInput(getPaymentTerms());
			this.paymentTermViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
			this.paymentTermViewer.addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(final SelectionChangedEvent event)
				{
					CourseEditor.this.setDirty(true);
				}
			});
	
			label = this.formToolkit.createLabel(composite, "Verantwortlich");
			label.setLayoutData(new GridData());
	
			combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
			combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
			combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			this.formToolkit.adapt(combo);
	
			this.userViewer = new ComboViewer(combo);
			this.userViewer.setContentProvider(new UserContentProvider(GlobalSettings.getInstance()
					.isCourseResponsibleUserMandatory()));
			this.userViewer.setLabelProvider(new UserLabelProvider());
			this.userViewer.setSorter(new UserSorter());
			this.userViewer.setFilters(new ViewerFilter[] { new DeletedEntityFilter() });
	
			if (service != null)
			{
				UserQuery query = (UserQuery) service.getQuery(User.class);
				List<User> users = query.selectAll();
				this.userViewer.setInput(users.toArray(new User[0]));
			}
			this.userViewer.addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(final SelectionChangedEvent event)
				{
					CourseEditor.this.setDirty(true);
				}
			});
		}
		finally
		{
			tracker.close();
		}
		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private List<PaymentTerm> getPaymentTerms()
	{
		List<PaymentTerm> paymentTerms = new ArrayList<PaymentTerm>();
		PaymentTerm term = PaymentTerm.newInstance();
		term.setId(Long.valueOf(0L));
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				PaymentTermQuery query = (PaymentTermQuery) service.getQuery(PaymentTerm.class);
				List<PaymentTerm> existingTerms = query.selectAll();
				paymentTerms.addAll(existingTerms);
			}
		}
		finally
		{
			tracker.close();
		}
		return paymentTerms;
	}

	private Control fillConstraintsSection(final Section parent)
	{

		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Zielgruppe", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = widthHint;

		this.targetPublic = this.formToolkit.createText(composite, "");
		this.targetPublic.setLayoutData(layoutData);
		this.targetPublic.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditorInput input = (CourseEditorInput) CourseEditor.this.getEditorInput();
				Course course = input.getEntity();
				course.setTargetPublic(CourseEditor.this.targetPublic.getText());
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Voraussetzungen", SWT.NONE);
		label.setLayoutData(new GridData());

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = widthHint;
		layoutData.heightHint = 48;

		this.prerequisites = this.formToolkit.createText(composite, "");
		this.prerequisites.setLayoutData(layoutData);
		this.prerequisites.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Minimale Teilnehmerzahl", SWT.NONE);
		label.setLayoutData(new GridData());

		this.minParticipants = new Spinner(composite, SWT.FLAT);
		this.minParticipants.setMinimum(0);
		this.minParticipants.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.minParticipants.setLayoutData(new GridData());
		this.minParticipants.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});
		this.minParticipants.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Maximale Teilnehmerzahl", SWT.NONE);
		label.setLayoutData(new GridData());

		this.maxParticipants = new Spinner(composite, SWT.FLAT);
		this.maxParticipants.setMinimum(0);
		this.maxParticipants.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.maxParticipants.setLayoutData(new GridData());
		this.maxParticipants.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});
		this.maxParticipants.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Minimales Alter", SWT.NONE);
		label.setLayoutData(new GridData());

		this.minAge = new Spinner(composite, SWT.FLAT);
		this.minAge.setMinimum(0);
		this.minAge.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.minAge.setLayoutData(new GridData());
		this.minAge.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});
		this.minAge.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Maximales Alter", SWT.NONE);
		label.setLayoutData(new GridData());

		this.maxAge = new Spinner(composite, SWT.FLAT);
		this.maxAge.setMinimum(0);
		this.maxAge.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.maxAge.setLayoutData(new GridData());
		this.maxAge.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});
		this.maxAge.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Geschlecht", SWT.NONE);
		label.setLayoutData(new GridData());

		RadioGroup radioGroup = RadioGroupFormToolkit.createRadioGroup(this.formToolkit, composite, SWT.FLAT
				| SWT.VERTICAL);
		radioGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		radioGroup.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent event)
			{
				this.widgetSelected(event);
			}

			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});
		// this.formToolkit.adapt(radioGroup, true, false);

		this.sexRadioGroupViewer = new RadioGroupViewer(radioGroup);
		this.sexRadioGroupViewer.setContentProvider(new ArrayContentProvider());
		this.sexRadioGroupViewer.setLabelProvider(new CourseSexConstraintLabelProvider());
		this.sexRadioGroupViewer.setInput(CourseSexConstraint.values());

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillContentsSection(final Section parent)
	{

		GridLayout layout = new GridLayout(2, false);

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		Label label = this.formToolkit.createLabel(composite, "Verpflegung", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 72;
		layoutData.widthHint = widthHint;

		this.boarding = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		this.boarding.setLayoutData(layoutData);
		this.boarding.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditorInput input = (CourseEditorInput) CourseEditor.this.getEditorInput();
				Course course = input.getEntity();
				course.setBoarding(CourseEditor.this.boarding.getText());
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Unterkunft", SWT.NONE);
		label.setLayoutData(new GridData());

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 72;
		layoutData.widthHint = widthHint;

		this.lodging = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		this.lodging.setLayoutData(layoutData);
		this.lodging.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditorInput input = (CourseEditorInput) CourseEditor.this.getEditorInput();
				Course course = input.getEntity();
				course.setLodging(CourseEditor.this.lodging.getText());
				CourseEditor.this.setDirty(true);
			}
		});

		// label = this.formToolkit.createLabel(composite, "Kurszweck",
		// SWT.NONE);
		// label.setLayoutData(new GridData());
		//
		// layoutData = new GridData(GridData.FILL_HORIZONTAL);
		// layoutData.heightHint = 72;
		// layoutData.widthHint = widthHint;
		//
		// this.purpose = this.formToolkit.createText(composite, "", SWT.MULTI |
		// SWT.V_SCROLL | SWT.WRAP);
		// this.purpose.setLayoutData(layoutData);
		// this.purpose.addModifyListener(new ModifyListener()
		// {
		// @Override
		// public void modifyText(final ModifyEvent e)
		// {
		// CourseEditorInput input = (CourseEditorInput)
		// CourseEditor.this.getEditorInput();
		// Course course = input.getEntity();
		// course.setPurpose(CourseEditor.this.purpose.getText());
		// CourseEditor.this.setDirty(true);
		// }
		// });

		label = this.formToolkit.createLabel(composite, "Programm", SWT.NONE);
		label.setLayoutData(new GridData());

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 72;
		layoutData.widthHint = widthHint;

		this.contents = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		this.contents.setLayoutData(layoutData);
		this.contents.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditorInput input = (CourseEditorInput) CourseEditor.this.getEditorInput();
				Course course = input.getEntity();
				course.setContents(CourseEditor.this.contents.getText());
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Kursmaterial\nTeilnehmer", SWT.NONE);
		label.setLayoutData(new GridData());

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 72;
		layoutData.widthHint = widthHint;

		this.materialParticipants = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		this.materialParticipants.setLayoutData(layoutData);
		this.materialParticipants.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditorInput input = (CourseEditorInput) CourseEditor.this.getEditorInput();
				Course course = input.getEntity();
				course.setMaterialParticipants(CourseEditor.this.materialParticipants.getText());
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Bereitzustellendes\nKursmaterial", SWT.NONE);
		label.setLayoutData(new GridData());

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 72;
		layoutData.widthHint = widthHint;

		this.materialOrganizer = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		this.materialOrganizer.setLayoutData(layoutData);
		this.materialOrganizer.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditorInput input = (CourseEditorInput) CourseEditor.this.getEditorInput();
				Course course = input.getEntity();
				course.setMaterialOrganizer(CourseEditor.this.materialOrganizer.getText());
				CourseEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillInformationSection(final Section parent)
	{
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;

		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(layout);

		// Label label = this.formToolkit.createLabel(composite,
		// "Informationstreffen", SWT.NONE);
		// label.setLayoutData(new GridData());
		//
		// GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		// layoutData.heightHint = 72;
		// layoutData.widthHint = widthHint;
		//
		// this.infoMeeting = this.formToolkit.createText(composite, "");
		// this.infoMeeting.setLayoutData(layoutData);
		// this.infoMeeting.addModifyListener(new ModifyListener()
		// {
		// @Override
		// public void modifyText(final ModifyEvent e)
		// {
		// CourseEditor.this.setDirty(true);
		// }
		// });

		Label label = this.formToolkit.createLabel(composite, "Auskunft", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = widthHint;

		this.information = this.formToolkit.createText(composite, "");
		this.information.setLayoutData(layoutData);
		this.information.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Durchführung", SWT.NONE);
		label.setLayoutData(new GridData());

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = widthHint;

		this.realization = this.formToolkit.createText(composite, "");
		this.realization.setLayoutData(layoutData);
		this.realization.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Bem. Kurskosten", SWT.NONE);
		label.setLayoutData(new GridData());

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = widthHint;

		this.costNote = this.formToolkit.createText(composite, "");
		this.costNote.setLayoutData(layoutData);
		this.costNote.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillNameSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Code", SWT.NONE);
		label.setLayoutData(new GridData());

		this.code = this.formToolkit.createText(composite, "");
		this.code.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.code.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Titel", SWT.NONE);
		label.setLayoutData(new GridData());

		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = widthHint;

		this.title = this.formToolkit.createText(composite, "");
		this.title.setLayoutData(layoutData);
		this.title.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Teaser", SWT.NONE);
		label.setLayoutData(new GridData());

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 72;
		layoutData.widthHint = widthHint;

		this.teaser = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		this.teaser.setLayoutData(layoutData);
		this.teaser.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Beschreibung", SWT.NONE);
		label.setLayoutData(new GridData());

		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 72;
		layoutData.widthHint = widthHint;

		this.description = this.formToolkit.createText(composite, "", SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		this.description.setLayoutData(layoutData);
		this.description.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent e)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	private Control fillStateSection(final Section parent)
	{
		Composite composite = this.formToolkit.createComposite(parent);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		composite.setLayout(new GridLayout(2, false));

		Label label = this.formToolkit.createLabel(composite, "Status", SWT.NONE);
		label.setLayoutData(new GridData());

		CCombo combo = new CCombo(composite, SWT.READ_ONLY | SWT.FLAT);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.formToolkit.adapt(combo);

		this.stateViewer = new ComboViewer(combo);
		this.stateViewer.setContentProvider(new ArrayContentProvider());
		this.stateViewer.setLabelProvider(new StateLabelProvider());
		this.stateViewer.setSorter(new StateSorter());
		this.stateViewer.setInput(CourseState.values());
		this.stateViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "");
		this.substituted = this.formToolkit.createButton(composite, "Verschoben", SWT.CHECK);
		this.substituted.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Anmeldefrist", SWT.TRAIL);
		label.setLayoutData(new GridData());

		GridData gridData = new GridData();
		gridData.widthHint = 112;

		this.lastBookingDate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.lastBookingDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.lastBookingDate.setLayoutData(gridData);
		this.lastBookingDate.setNullText("");
		this.lastBookingDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Versand Voranzeige", SWT.TRAIL);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 112;

		this.advanceNoticeDate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.advanceNoticeDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.advanceNoticeDate.setLayoutData(gridData);
		this.advanceNoticeDate.setSelection(new Date());
		this.advanceNoticeDate.setNullText("");
		this.advanceNoticeDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Voranzeige verschickt", SWT.TRAIL);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 112;

		this.advanceNoticeDoneDate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.advanceNoticeDoneDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.advanceNoticeDoneDate.setLayoutData(gridData);
		this.advanceNoticeDoneDate.setNullText("");
		this.advanceNoticeDoneDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Versand Einladungen", SWT.TRAIL);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 112;

		this.invitationDate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.invitationDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.invitationDate.setLayoutData(gridData);
		this.invitationDate.setSelection(new Date());
		this.invitationDate.setNullText("");
		this.invitationDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Einladungen verschickt", SWT.TRAIL);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 112;

		this.invitationDoneDate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.invitationDoneDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.invitationDoneDate.setLayoutData(gridData);
		this.invitationDoneDate.setNullText("");
		this.invitationDoneDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Letztmögliche Annulation", SWT.TRAIL);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 112;

		this.lastAnnulationDate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.lastAnnulationDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.lastAnnulationDate.setLayoutData(gridData);
		this.lastAnnulationDate.setNullText("");
		this.lastAnnulationDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		label = this.formToolkit.createLabel(composite, "Kurs annulliert am", SWT.TRAIL);
		label.setLayoutData(new GridData());

		gridData = new GridData();
		gridData.widthHint = 112;

		this.annulationDate = new CDateTime(composite, CDT.DROP_DOWN | CDT.DATE_MEDIUM | CDT.TIME_SHORT);
		this.annulationDate.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		this.annulationDate.setLayoutData(gridData);
		this.annulationDate.setNullText("");
		this.annulationDate.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				CourseEditor.this.setDirty(true);
			}
		});

		this.formToolkit.paintBordersFor(composite);

		return composite;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(final Class adapter)
	{
		if (IContentOutlinePage.class.equals(adapter))
		{
			if (this.contentOutlinePage == null)
				this.contentOutlinePage = new CourseEditorContentOutlinePage(this);
			return this.contentOutlinePage;
		}
		return null;
	}

	private Message getEmptyCodeMessage()
	{
		Message msg = null;

		if (this.code.getText().isEmpty())
		{
			msg = new Message(this.code, "Fehler");
			msg.setMessage("Der Kurs muss einen Code haben.");
			FormToolkit.ensureVisible(this.code);
			this.code.setFocus();
		}

		return msg;
	}

	@Override
	protected Message getMessage(final PersistenceException.ErrorCode errorCode)
	{
		Message msg = null;

		if (errorCode.equals(""))
		{
			msg = this.getUniqueCodeMessage();
		}
		return msg;
	}

	@Override
	protected String getName()
	{
		CourseEditorInput input = (CourseEditorInput) this.getEditorInput();
		Course course = (Course) input.getAdapter(Course.class);
		return course.getId() == null ? "Neu" : (course.getCode().length() == 0 ? "???" : course.getCode());
	}

	@Override
	protected String getText()
	{
		CourseEditorInput input = (CourseEditorInput) this.getEditorInput();
		Course course = (Course) input.getAdapter(Course.class);
		StringBuilder builder = new StringBuilder();
		if (course.getId() == null)
		{
			return "Neuer Kurs";
		}
		builder = builder.append("Kurs ");
		return builder.append(
				course.getCode().length() == 0 ? course.getTitle() : course.getCode() + " - " + course.getTitle())
				.toString();
	}

	private Message getUniqueCodeMessage()
	{
		Message msg = null;

		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				CourseEditorInput input = (CourseEditorInput) this.getEditorInput();
				Course course = (Course) input.getAdapter(Course.class);
				String code = this.code.getText();
				CourseQuery query = (CourseQuery) service.getQuery(Course.class);
				if (!query.isCodeUnique(code, course.getId()))
				{
					msg = new Message(this.code, "Ungültiger Code");
					msg.setMessage("Der gewählte Code wird bereits verwendet.");
					return msg;
				}
			}
		}
		finally
		{
			tracker.close();
		}
		return msg;
	}

	@Override
	protected void initialize()
	{
		Long id = ((CourseEditorInput) this.getEditorInput()).getEntity().getId();
		this.initializeDialogSettings(id == null ? CourseEditor.COURSE_EDITOR : CourseEditor.COURSE_EDITOR + "." + id);
		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, "ch/eugster/events/persistence/merge");		
		eventHandlerRegistration = Activator.getDefault().getBundle().getBundleContext().registerService(EventHandler.class, this, properties);
	}

	private void initializeDialogSettings(final String section)
	{
		this.dialogSettings = Activator.getDefault().getDialogSettings().getSection(section);
		if (this.dialogSettings == null)
			this.dialogSettings = Activator.getDefault().getDialogSettings().addNewSection(section);
	}

	@Override
	protected void loadValues()
	{
		CourseEditorInput input = (CourseEditorInput) this.getEditorInput();
		Course course = input.getEntity();
		if (course != null)
		{
			if (course.getAnnulationDate() == null)
				this.annulationDate.setSelection(null);
			else
			{
				this.annulationDate.setSelection(course.getAnnulationDate().getTime());
			}

			this.boarding.setText(course.getBoarding());

			if (this.categoryViewer != null)
			{
				if (course.getCategory() != null)
					this.categoryViewer.setSelection(new StructuredSelection(course.getCategory()));
			}
			this.code.setText(course.getCode());
			this.contents.setText(course.getContents());
			this.description.setText(course.getDescription());

			if (this.domainViewer != null)
			{
				if (course.getDomain() != null)
					this.domainViewer.setSelection(new StructuredSelection(course.getDomain()));
			}
			// this.infoMeeting.setText(course.getInfoMeeting());
			this.information.setText(course.getInformation());

			if (course.getAdvanceNoticeDate() == null)
			{
				this.advanceNoticeDate.setSelection(null);
			}
			else
			{
				this.advanceNoticeDate.setSelection(course.getAdvanceNoticeDate().getTime());
			}

			if (course.getAdvanceNoticeDoneDate() == null)
			{
				this.advanceNoticeDoneDate.setSelection(null);
			}
			else
			{
				this.advanceNoticeDoneDate.setSelection(course.getAdvanceNoticeDoneDate().getTime());
			}

			if (course.getInvitationDate() == null)
			{
				this.invitationDate.setSelection(null);
			}
			else
			{
				this.invitationDate.setSelection(course.getInvitationDate().getTime());
			}

			if (course.getInvitationDoneDate() == null)
			{
				this.invitationDoneDate.setSelection(null);
			}
			else
			{
				this.invitationDoneDate.setSelection(course.getInvitationDoneDate().getTime());
			}

			if (course.getLastAnnulationDate() == null)
			{
				this.lastAnnulationDate.setSelection(null);
			}
			else
			{
				this.lastAnnulationDate.setSelection(course.getLastAnnulationDate().getTime());
			}

			if (course.getLastBookingDate() == null)
			{
				this.lastBookingDate.setSelection(null);
			}
			else
			{
				this.lastBookingDate.setSelection(course.getLastBookingDate().getTime());
			}

			PaymentTerm term = course.getPaymentTerm();
			if (term == null)
			{
				term = PaymentTerm.newInstance();
				term.setId(Long.valueOf(0L));
			}
			this.paymentTermViewer.setSelection(new StructuredSelection(new PaymentTerm[] { term }));

			this.lodging.setText(course.getLodging());
			this.materialOrganizer.setText(course.getMaterialOrganizer());
			this.materialParticipants.setText(course.getMaterialParticipants());
			this.prerequisites.setText(course.getPrerequisites());
			this.maxAge.setSelection(course.getMaxAge());
			this.maxParticipants.setSelection(course.getMaxParticipants());
			this.minAge.setSelection(course.getMinAge());
			this.minParticipants.setSelection(course.getMinParticipants());
			// this.purpose.setText(course.getPurpose());
			this.realization.setText(course.getRealization());
			this.costNote.setText(course.getCostNote());
			if (this.rubricViewer != null)
			{
				if (course.getRubric() != null)
					this.rubricViewer.setSelection(new StructuredSelection(course.getRubric()));
			}
			this.sexRadioGroupViewer.setSelection(new StructuredSelection(course.getSex()));
			if (course.getState() == null)
				this.stateViewer.setSelection(new StructuredSelection(CourseState.FORTHCOMING));
			else
				this.stateViewer.setSelection(new StructuredSelection(course.getState()));
			this.substituted.setSelection(course.isSubstituted());
			this.targetPublic.setText(course.getTargetPublic());
			this.teaser.setText(course.getTeaser());
			this.title.setText(course.getTitle());
			if (course.getResponsibleUser() != null)
			{
				this.userViewer.setSelection(new StructuredSelection(course.getResponsibleUser()));
			}
		}
		this.setDirty(false);
	}

	@Override
	public void propertyChange(final java.beans.PropertyChangeEvent event)
	{
		this.setDirty(true);
	}

	@Override
	protected void saveValues()
	{
		CourseEditorInput input = (CourseEditorInput) this.getEditorInput();
		Course course = input.getEntity();
		if (course != null)
		{
			Calendar calendar = null;
			if (this.annulationDate.getSelection() != null)
			{
				calendar = GregorianCalendar.getInstance();
				calendar.setTime(this.annulationDate.getSelection());
			}
			course.setAnnulationDate(calendar);

			course.setBoarding(this.boarding.getText());

			if (this.categoryViewer != null)
			{
				StructuredSelection ssel = (StructuredSelection) this.categoryViewer.getSelection();
				if (!ssel.isEmpty())
					course.setCategory((Category) ssel.getFirstElement());
			}
			course.setCode(this.code.getText());
			course.setContents(this.contents.getText());
			course.setDescription(this.description.getText());
			if (this.domainViewer != null)
			{
				IStructuredSelection ssel = (StructuredSelection) this.domainViewer.getSelection();
				Domain domain = ssel.isEmpty() ? null : (Domain) ssel.getFirstElement();
				course.setDomain((domain == null || domain.getId() == null) ? null : domain);
			}
			// course.setInfoMeeting(this.infoMeeting.getText());
			course.setInformation(this.information.getText());

			if (this.advanceNoticeDate.getSelection() == null)
			{
				calendar = null;
			}
			else
			{
				calendar = GregorianCalendar.getInstance();
				calendar.setTime(this.advanceNoticeDate.getSelection());
			}
			course.setAdvanceNoticeDate(calendar);

			if (this.advanceNoticeDoneDate.getSelection() == null)
			{
				calendar = null;
			}
			else
			{
				calendar = GregorianCalendar.getInstance();
				calendar.setTime(this.advanceNoticeDoneDate.getSelection());
			}
			course.setAdvanceNoticeDoneDate(calendar);

			if (this.invitationDate.getSelection() == null)
			{
				calendar = null;
			}
			else
			{
				calendar = GregorianCalendar.getInstance();
				calendar.setTime(this.invitationDate.getSelection());
			}
			course.setInvitationDate(calendar);

			if (this.invitationDoneDate.getSelection() == null)
			{
				calendar = null;
			}
			else
			{
				calendar = GregorianCalendar.getInstance();
				calendar.setTime(this.invitationDoneDate.getSelection());
			}
			course.setInvitationDoneDate(calendar);

			if (this.lastAnnulationDate.getSelection() == null)
			{
				calendar = null;
			}
			else
			{
				calendar = GregorianCalendar.getInstance();
				calendar.setTime(this.lastAnnulationDate.getSelection());
			}
			course.setLastAnnulationDate(calendar);

			if (this.lastBookingDate.getSelection() == null)
			{
				calendar = null;
			}
			else
			{
				calendar = GregorianCalendar.getInstance();
				calendar.setTime(this.lastBookingDate.getSelection());
			}
			course.setLastBookingDate(calendar);

			PaymentTerm term = null;
			IStructuredSelection ssel = (IStructuredSelection) paymentTermViewer.getSelection();
			if (ssel.getFirstElement() instanceof PaymentTerm)
			{
				term = (PaymentTerm) ssel.getFirstElement();
				if (term.getId().equals(Long.valueOf(0L)))
				{
					term = null;
				}
			}
			course.setPaymentTerm(term);

			course.setLodging(this.lodging.getText());
			course.setMaterialOrganizer(this.materialOrganizer.getText());
			course.setMaterialParticipants(this.materialParticipants.getText());
			course.setPrerequisites(this.prerequisites.getText());
			course.setMaxAge(this.maxAge.getSelection());
			course.setMaxParticipants(this.maxParticipants.getSelection());
			course.setMinAge(this.minAge.getSelection());
			course.setMinParticipants(this.minParticipants.getSelection());
			// course.setPurpose(this.purpose.getText());
			course.setRealization(this.realization.getText());
			course.setCostNote(this.costNote.getText());
			if (this.rubricViewer != null)
			{
				ssel = (StructuredSelection) this.rubricViewer.getSelection();
				if (!ssel.isEmpty())
					course.setRubric((Rubric) ssel.getFirstElement());
			}
			ssel = (StructuredSelection) this.sexRadioGroupViewer.getSelection();
			if (!ssel.isEmpty())
				course.setSex((CourseSexConstraint) ssel.getFirstElement());
			ssel = (StructuredSelection) this.stateViewer.getSelection();
			if (!ssel.isEmpty())
				course.setState((CourseState) ssel.getFirstElement());
			course.setSubstituted(this.substituted.getSelection());
			course.setTargetPublic(this.targetPublic.getText());
			course.setTeaser(this.teaser.getText());
			course.setTitle(this.title.getText());
			ssel = (StructuredSelection) this.userViewer.getSelection();
			if (!ssel.isEmpty())
			{
				course.setResponsibleUser((User) ssel.getFirstElement());
			}
			if (this.contentOutlinePage instanceof CourseEditorContentOutlinePage)
			{
				CourseEditorContentOutlinePage page = (CourseEditorContentOutlinePage) this.contentOutlinePage;
				page.update();
			}
		}
	}

	@Override
	protected void updateControls()
	{
		this.setPartName(this.getName());
	}

	@Override
	public void setFocus()
	{
		this.code.setFocus();
	}

	@Override
	protected boolean validate()
	{
		Message msg = null;

		StructuredSelection ssel = (StructuredSelection) this.stateViewer.getSelection();
		if (ssel.isEmpty())
		{
			msg = new Message(this.code, "Kursstatus fehlt");
			msg.setMessage("Sie haben den Kursstatus nicht festgelegt.");
			FormToolkit.ensureVisible(this.stateViewer.getCCombo());
			this.stateViewer.getCCombo().setFocus();
		}

		if (msg == null)
		{
			if (GlobalSettings.getInstance().getCourseHasDomain())
			{
				if (GlobalSettings.getInstance().isCourseDomainMandatory())
				{
					ssel = (StructuredSelection) this.domainViewer.getSelection();
					if (ssel.isEmpty() || ((Domain) ssel.getFirstElement()).getId() == null)
					{
						msg = new Message(this.code, "Fehlende Domäne");
						msg.setMessage("Sie haben keine Domäne ausgewählt.");
						FormToolkit.ensureVisible(this.domainViewer.getCCombo());
						this.domainViewer.getCCombo().setFocus();
					}
				}
			}
		}

		if (msg == null)
		{
			if (GlobalSettings.getInstance().getCourseHasCategory())
			{
				if (GlobalSettings.getInstance().isCourseCategoryMandatory())
				{
					ssel = (StructuredSelection) this.categoryViewer.getSelection();
					if (ssel.isEmpty() || ((Category) ssel.getFirstElement()).getId() == null)
					{
						msg = new Message(this.code, "Fehlende Kategorie");
						msg.setMessage("Sie haben keine Kategorie ausgewählt.");
						FormToolkit.ensureVisible(this.categoryViewer.getCCombo());
						this.categoryViewer.getCCombo().setFocus();
					}
				}
			}
		}

		if (msg == null)
		{
			if (GlobalSettings.getInstance().getCourseHasRubric())
			{
				if (GlobalSettings.getInstance().isCourseRubricMandatory())
				{
					ssel = (StructuredSelection) this.rubricViewer.getSelection();
					if (ssel.isEmpty() || ((Rubric) ssel.getFirstElement()).getId() == null)
					{
						msg = new Message(this.code, "Fehlende Rubrik");
						msg.setMessage("Sie haben keine Rubrik ausgewählt.");
						FormToolkit.ensureVisible(this.rubricViewer.getCCombo());
						this.rubricViewer.getCCombo().setFocus();
					}
				}
			}
		}

		if (msg == null)
		{
			if (GlobalSettings.getInstance().isCourseResponsibleUserMandatory())
			{
				ssel = (StructuredSelection) this.userViewer.getSelection();
				if (ssel.isEmpty() || ((User) ssel.getFirstElement()).getId() == null)
				{
					msg = new Message(this.code, "Fehlende kursverantwortliche Person");
					msg.setMessage("Sie haben keine für den Kurs verantwortliche Person ausgewählt.");
					FormToolkit.ensureVisible(this.userViewer.getCCombo());
					this.userViewer.getCCombo().setFocus();
				}
			}
		}

		if (msg == null)
		{
			msg = this.getEmptyCodeMessage();
		}

		if (msg == null)
		{
			msg = this.getUniqueCodeMessage();
		}

		if (msg != null)
			this.showWarningMessage(msg);

		return msg == null;
	}

	@Override
	protected boolean validateType(final AbstractEntityEditorInput<Course> input)
	{
		return input.getEntity() instanceof Course;
	}

	@Override
	public void handleEvent(Event event) 
	{
		CourseEditorInput input = (CourseEditorInput) this.getEditorInput();
		Course course = (Course) input.getAdapter(Course.class);
		if (course.getId() == null)
		{
			return;
		}
		if (event.getTopic().equals("ch/eugster/events/persistence/merge"))
		{
			Object entity = event.getProperty("entity");
			if (entity instanceof Course)
			{

				Course updatedCourse = (Course) entity;
				if (course.getId().equals(updatedCourse.getId()))
				{
					if (course.isDeleted())
					{
						this.getSite().getPage().closeEditor(this, false);
					}
					else
					{
						input.setEntity(updatedCourse);
						loadValues();
					}
				}
			}
		}
	}

}
