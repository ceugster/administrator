package ch.eugster.events.course.wizards;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.Activator;
import ch.eugster.events.persistence.formatters.CourseFormatter;
import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.Guide;
import ch.eugster.events.persistence.model.GuideType;
import ch.eugster.events.persistence.queries.GuideQuery;
import ch.eugster.events.persistence.queries.GuideTypeQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class CourseGuideWizardPage extends WizardPage implements Listener, SelectionListener
{
	private ComboViewer guideViewer;

	private ComboViewer guideTypeViewer;

	// private Text description;
	//
	// private Text phone;

	private CompensationTableViewerComposite compensationTableComposite;
	
	private Text note;

	public CourseGuideWizardPage(final String pageName)
	{
		super(pageName);
	}

	@Override
	public void createControl(final Composite parent)
	{
		ImageDescriptor image = ImageDescriptor.createFromImage(Activator.getDefault().getImageRegistry()
				.get("NEW_WIZARD"));
		this.setImageDescriptor(image);

		CourseGuideWizard wizard = (CourseGuideWizard) this.getWizard();
		if (wizard.getCourseGuide().getId() == null)
		{
			this.setTitle("Leitungsperson hinzufügen");
			String course = CourseFormatter.getInstance().formatComboEntry(wizard.getCourseGuide().getCourse());
			this.setDescription("Hinzufügen einer Leitungsperson zu '" + course + "'.");
		}
		else
		{
			this.setTitle("Leitungsperson bearbeiten");
			String person = PersonFormatter.getInstance().formatLastnameFirstname(
					wizard.getCourseGuide().getGuide().getLink().getPerson());
			this.setDescription("Bearbeiten der Leitungsangaben von " + person + ".");
		}

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.makeColumnsEqualWidth = false;

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(gridLayout);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.END;
		gridData.grabExcessHorizontalSpace = false;

		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(gridData);
		label.setText("Leitungsperson");

		Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.guideViewer = new ComboViewer(combo);
		this.guideViewer.setContentProvider(new GuideComboContentProvider());
		this.guideViewer.setLabelProvider(new GuideComboLabelProvider());
		this.guideViewer.setSorter(new GuideComboSorter());
		this.guideViewer.setFilters(new ViewerFilter[] { new GuideComboFilter(wizard) });

		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			GuideQuery query = (GuideQuery) service.getQuery(Guide.class);
			Collection<Guide> guides = query.selectAll();
			this.guideViewer.setInput(guides.toArray(new Guide[0]));
		}

		this.guideViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				StructuredSelection ssel = (StructuredSelection) event.getSelection();
				if (!ssel.isEmpty())
				{
					Guide guide = (Guide) ssel.getFirstElement();
					// CourseGuideWizardPage.this.description.setText(guide.getDescription());
					// CourseGuideWizardPage.this.phone.setText(guide.getPhone());
					CourseGuideWizardPage.this.setPageComplete(true);
				}
			}
		});
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.END;
		gridData.grabExcessHorizontalSpace = false;

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(gridData);
		label.setText("Leitungsfunktion");

		combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.guideTypeViewer = new ComboViewer(combo);
		this.guideTypeViewer.setContentProvider(new GuideTypeComboContentProvider());
		this.guideTypeViewer.setLabelProvider(new GuideTypeComboLabelProvider());
		this.guideTypeViewer.setSorter(new GuideTypeComboSorter());

		if (service != null)
		{
			GuideTypeQuery query = (GuideTypeQuery) service.getQuery(GuideType.class);
			Collection<GuideType> guideTypes = query.selectAll();
			this.guideTypeViewer.setInput(guideTypes);
		}

		tracker.close();

		this.guideTypeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				CourseGuideWizardPage.this.setPageComplete(true);
			}
		});
		// gridData = new GridData();
		// gridData.horizontalAlignment = GridData.END;
		// gridData.grabExcessHorizontalSpace = false;
		//
		// label = new Label(composite, SWT.NONE);
		// label.setLayoutData(gridData);
		// label.setText("Beschreibung");
		//
		// this.description = new Text(composite, SWT.BORDER | SWT.MULTI |
		// SWT.V_SCROLL);
		// this.description.setLayoutData(new GridData(GridData.FILL_BOTH));
		//
		// gridData = new GridData();
		// gridData.horizontalAlignment = GridData.END;
		// gridData.grabExcessHorizontalSpace = false;
		//
		// label = new Label(composite, SWT.NONE);
		// label.setLayoutData(gridData);
		// label.setText("Telefon/Kontakt");
		//
		// this.phone = new Text(composite, SWT.BORDER);
		// this.phone.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.END;
		gridData.grabExcessHorizontalSpace = false;
		gridData.verticalAlignment = SWT.TOP;

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(gridData);
		label.setText("Entschädigungen");

		this.compensationTableComposite = new CompensationTableViewerComposite(composite, SWT.NULL);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.END;
		gridData.grabExcessHorizontalSpace = false;

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(gridData);
		label.setText("Bemerkungen");

		this.note = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.VERTICAL);
		this.note.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.setValues();

		this.setControl(composite);
	}

	// private String getDesc()
	// {
	// return this.description.getText();
	// }

	private Guide getGuide()
	{
		StructuredSelection ssel = (StructuredSelection) this.guideViewer.getSelection();
		return (Guide) ssel.getFirstElement();
	}

	private GuideType getGuideType()
	{
		StructuredSelection ssel = (StructuredSelection) this.guideTypeViewer.getSelection();
		return (GuideType) ssel.getFirstElement();
	}

	// private String getPhone()
	// {
	// return this.phone.getText();
	// }

	@Override
	public void handleEvent(final Event event)
	{
	}

	@Override
	public boolean isPageComplete()
	{
		if (this.guideViewer.getSelection().isEmpty())
		{
			this.setErrorMessage("Sie haben keine Leitungsperson ausgewählt.");
			return false;
		}
		if (this.guideTypeViewer.getSelection().isEmpty())
		{
			this.setErrorMessage("Sie haben keine Leitungsfunktion ausgewählt");
			return false;
		}

		this.setErrorMessage(null);
		return true;
	}

	private void setCompensations()
	{
		CourseGuideWizard wizard = (CourseGuideWizard) this.getWizard();
		/*
		 * Setzen des Guides: Die Entschädigungen werden von dort geholt
		 */
		this.compensationTableComposite.setCourseGuide(wizard.getCourseGuide());
	}

	// private void setDesc()
	// {
	// CourseGuideWizard wizard = (CourseGuideWizard) this.getWizard();
	// Guide guide = wizard.getCourseGuide().getGuide();
	// if (guide == null)
	// guide = (Guide) this.guideViewer.getElementAt(0);
	//
	// if (guide != null)
	// {
	// this.description.setText(guide.getDescription());
	// }
	// }

	private void setGuideType()
	{
		CourseGuideWizard wizard = (CourseGuideWizard) this.getWizard();
		StructuredSelection ssel = null;
		GuideType type = wizard.getCourseGuide().getGuideType();
		if (type == null)
			type = (GuideType) this.guideTypeViewer.getElementAt(0);

		if (type != null)
		{
			ssel = new StructuredSelection(type);
			this.guideTypeViewer.setSelection(ssel);
		}
	}

	private void setPerson()
	{
		CourseGuideWizard wizard = (CourseGuideWizard) this.getWizard();
		StructuredSelection ssel = null;
		Guide guide = wizard.getCourseGuide().getGuide();
		if (guide == null)
			guide = (Guide) this.guideViewer.getElementAt(0);

		if (guide != null)
		{
			ssel = new StructuredSelection(guide);
			this.guideViewer.setSelection(ssel);
		}
	}

	 private void setCourseGuideValues()
	 {
		 CourseGuideWizard wizard = (CourseGuideWizard) this.getWizard();
		 CourseGuide courseGuide = wizard.getCourseGuide();
		 if (courseGuide != null)
		 {
			 this.note.setText(courseGuide.getNote());
		 }
	 }

	private void setValues()
	{
		this.setPerson();
		this.setGuideType();
		this.setCompensations();
		this.setCourseGuideValues();
	}

	public CourseGuide updateCourseGuide()
	{
		CourseGuideWizard wizard = (CourseGuideWizard) this.getWizard();
		wizard.getCourseGuide().setGuide(this.getGuide());
		wizard.getCourseGuide().setGuideType(this.getGuideType());
		wizard.getCourseGuide().setNote(this.note.getText());
		// wizard.getCourseGuide().setPhone(this.getPhone());
		wizard.getCourseGuide().setCompensations(this.compensationTableComposite.getCompensations());
		return wizard.getCourseGuide();
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event)
	{
		this.widgetSelected(event);
	}

	@Override
	public void widgetSelected(final SelectionEvent event)
	{
	}

	private class GuideComboContentProvider extends ArrayContentProvider
	{
		public GuideComboContentProvider()
		{
		}

		@Override
		public Object[] getElements(final Object inputElement)
		{
			if (inputElement instanceof Guide[])
			{
				return (Guide[]) inputElement;
			}
			return new Guide[0];
		}

	}

	private class GuideComboFilter extends ViewerFilter
	{
		private final CourseGuideWizard wizard;

		public GuideComboFilter(final CourseGuideWizard wizard)
		{
			this.wizard = wizard;
		}

		@Override
		public boolean select(final Viewer viewer, final Object parentElement, final Object element)
		{
			if (element instanceof Guide)
			{
				Guide guide = (Guide) element;
				if (this.wizard.getCourseGuide().getGuide() != null
						&& guide.getId().equals(this.wizard.getCourseGuide().getGuide().getId()))
				{
					return true;
				}
				else
				{
					return !guide.isDeleted();
				}
			}
			return true;
		}

	}

	private class GuideComboLabelProvider extends LabelProvider
	{
		public GuideComboLabelProvider()
		{
		}

		@Override
		public Image getImage(final Object element)
		{
			return null;
		}

		@Override
		public String getText(final Object element)
		{
			if (element instanceof Guide)
			{
				Guide guide = (Guide) element;
				return PersonFormatter.getInstance().formatLastnameFirstname(guide.getLink().getPerson());
			}
			return "";

		}
	}

	private class GuideComboSorter extends ViewerSorter
	{
		@Override
		public int compare(final Viewer viewer, final Object object1, final Object object2)
		{
			Guide guide1 = (Guide) object1;
			Guide guide2 = (Guide) object2;

			String name1 = PersonFormatter.getInstance().formatLastnameFirstname(guide1.getLink().getPerson());
			String name2 = PersonFormatter.getInstance().formatLastnameFirstname(guide2.getLink().getPerson());

			return name1.compareTo(name2);
		}
	}

	private class GuideTypeComboContentProvider extends ArrayContentProvider
	{
		@Override
		public Object[] getElements(final Object inputElement)
		{
			if (inputElement instanceof Collection)
			{
				Collection<GuideType> guideTypes = new ArrayList<GuideType>();
				Collection<?> gts = (Collection<?>) inputElement;
				for (Object gt : gts)
				{
					if (gt instanceof GuideType)
					{
						guideTypes.add((GuideType) gt);
					}
				}
				return guideTypes.toArray(new GuideType[0]);
			}
			if (inputElement instanceof GuideType[])
			{
				return (GuideType[]) inputElement;
			}
			else
				return new GuideType[0];
		}
	}

	private class GuideTypeComboLabelProvider extends LabelProvider
	{
		public GuideTypeComboLabelProvider()
		{
		}

		@Override
		public Image getImage(final Object element)
		{
			return null;
		}

		@Override
		public String getText(final Object element)
		{
			if (element instanceof GuideType)
			{
				GuideType guideType = (GuideType) element;
				return new CourseFormatter().formatComboEntry(guideType);
			}
			return "";

		}
	}

	private class GuideTypeComboSorter extends ViewerSorter
	{
		@Override
		public int compare(final Viewer viewer, final Object object1, final Object object2)
		{
			GuideType type1 = (GuideType) object1;
			GuideType type2 = (GuideType) object2;

			return type1.getId().compareTo(type2.getId());
		}
	}
}
