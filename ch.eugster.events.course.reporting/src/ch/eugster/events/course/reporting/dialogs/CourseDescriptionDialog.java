package ch.eugster.events.course.reporting.dialogs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.course.reporting.CourseDescriptionFactory;
import ch.eugster.events.documents.maps.CourseMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.CourseState;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class CourseDescriptionDialog extends TitleAreaDialog
{
	private IDialogSettings settings;

	private Map<CourseState, Boolean> selectedStates = new HashMap<CourseState, Boolean>();

	private final IStructuredSelection ssel;

	private final String message = "Kursbeschreibungen als XML-Datei oder fertiges OpenOffice-Dokument erstellen.";

	private boolean isPageComplete = false;

	private Label pathLabel;
	
	private Text pathText;
	
	private ExportType exportType = ExportType.TYPE_XML;
	
	private String templatePath = null;

	public CourseDescriptionDialog(final Shell parentShell, IStructuredSelection ssel)
	{
		super(parentShell);
		this.ssel = ssel;
		settings = Activator.getDefault().getDialogSettings().getSection("course.description.dialog");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("course.description.dialog");
		}
		int type = 0;
		try
		{
			type = settings.getInt("export.type");
			if (type < 0 || type > ExportType.values().length - 1)
			{
				type = 0;
			}
		}
		catch (NumberFormatException e)
		{
			settings.put("export.type", type);
		}
		exportType = ExportType.values()[type];
		templatePath = settings.get(exportType.pathkey()) == null ? "" : settings.get(exportType.pathkey());
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, "Generieren", true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(3, false));

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(2, true));
		group.setText("Auswahl Ziel-Dokument");

		for (int i = 0; i < ExportType.values().length; i++)
		{
			final Button button = new Button(group, SWT.RADIO);
			button.setText(ExportType.values()[i].text());
			button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			button.setSelection(i == exportType.ordinal());
			button.setData("ordinal", ExportType.values()[i]);
			button.addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetDefaultSelected(final SelectionEvent e)
				{
					widgetSelected(e);
				}

				@Override
				public void widgetSelected(final SelectionEvent e)
				{
					if (((Button) e.getSource()).getSelection())
					{
						exportType = (ExportType) button.getData("ordinal");
						settings.put(ExportType.key(), exportType.ordinal());
						pathLabel.setText(exportType.pathlabel());
						templatePath = settings.get(exportType.pathkey()) == null ? "" : settings.get(exportType.pathkey());
						pathText.setText(templatePath);
					}
				}
			});
		}

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(3, true));
		group.setText("Auswahl Status");

		for (final CourseState state : CourseState.values())
		{
			boolean selected = settings.getBoolean(state.name());
			selectedStates.put(state, selected);
			final Button button = new Button(group, SWT.CHECK);
			button.setText(state.toString());
			button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			button.setSelection(selected);
			button.addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetDefaultSelected(final SelectionEvent e)
				{
					widgetSelected(e);
				}

				@Override
				public void widgetSelected(final SelectionEvent e)
				{
					selectedStates.put(state, button.getSelection());
					settings.put(state.name(), button.getSelection());
				}
			});
		}

		gridData = new GridData();
		gridData.widthHint = ExportType.maxLabelWith(composite);
		pathLabel = new Label(composite, SWT.None);
		pathLabel.setLayoutData(gridData);
		pathLabel.setText(exportType.pathlabel());
		
		pathText = new Text(composite, SWT.BORDER);
		pathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pathText.setText(templatePath);
		pathText.addModifyListener(new ModifyListener() 
		{
			@Override
			public void modifyText(ModifyEvent e) 
			{
				templatePath = pathText.getText();
				settings.put(exportType.pathkey(), templatePath);
			}
		});
		
		Button selectPath = new Button(composite, SWT.PUSH);
		selectPath.setLayoutData(new GridData());
		selectPath.setText("...");
		selectPath.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				FileDialog dialog = new FileDialog(CourseDescriptionDialog.this.getShell(), exportType.dialogType());
				dialog.setFileName(templatePath);
				dialog.setFilterExtensions(exportType.filterExtensions());
				dialog.setFilterIndex(exportType.ordinal());
				dialog.setFilterNames(exportType.filterNames());
				dialog.setText(exportType.dialogTitle());
				String result = dialog.open();
				if (result != null)
				{
					if (exportType.equals(ExportType.TYPE_XML) || new File(result).isFile())
					{
						pathText.setText(result);
						settings.put(exportType.pathkey(), result);
					}
				}
				if (exportType.equals(ExportType.TYPE_OPENOFFICE) && !(new File(result).isFile()))
				{
					MessageDialog.openWarning(CourseDescriptionDialog.this.getShell(), "Vorlage nicht vorhanden", "Die ausgewählte Vorlage ist ungültig oder nicht vorhanden.");
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) 
			{
				widgetSelected(e);
			}
		});
		
		return parent;
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		setCurrentUser();
		UIJob job = new UIJob("Generiere Kursbeschreibungen...")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{

				CourseDescriptionFactory factory = CourseDescriptionFactory.create(ssel, selectedStates);
				if (factory.size() == 0)
				{
					MessageDialog.openInformation(null, "Keine Kurse vorhanden", "Ihre Auswahl enthält keine Kurse.");
				}
				else
				{
					CourseMap[] courses = factory.getCourses();
					Arrays.sort(courses, new Comparator<CourseMap>() 
					{
						@Override
						public int compare(CourseMap map1, CourseMap map2) 
						{
							Date date1 = null;
							Date date2 = null;
							String strDate1 = map1.getProperty(CourseMap.Key.FIRST_DATE.getKey());
							try 
							{
								date1 = new SimpleDateFormat("dd.MM.yyyy hh:mm", Locale.getDefault()).parse(strDate1);
							} 
							catch (ParseException e1) 
							{
								try 
								{
									date1 = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(strDate1);
								} 
								catch (ParseException e2) 
								{
									date1 = null;
								}
							}
							String strDate2 = map2.getProperty(CourseMap.Key.FIRST_DATE.getKey());
							try 
							{
								date2 = new SimpleDateFormat("dd.MM.yyyy hh:mm", Locale.getDefault()).parse(strDate2);
							} 
							catch (ParseException e1) 
							{
								try 
								{
									date2 = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(strDate2);
								} 
								catch (ParseException e2) 
								{
									date2 = null;
								}
							}
							if (date1 == null)
							{
								return -1;
							}
							if (date2 == null)
							{
								return 1;
							}
							return date1.compareTo(date2);
						}
					});
					factory.setCourses(courses);
					buildDocument(factory);
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();

		super.okPressed();
	}

	private void buildDocument(final CourseDescriptionFactory factory)
	{
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try
		{
			dialog.run(true, true, new IRunnableWithProgress()
			{
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					if (factory.size() == 0)
					{
						MessageDialog.openConfirm(getShell(), "Keine Kurse gefunden", "Es wurden keine Kurse in der gewählten Selection gefunden");
					}
					else
					{
						ServiceTracker<DocumentBuilderService, DocumentBuilderService> tracker = new ServiceTracker<DocumentBuilderService, DocumentBuilderService>(Activator.getDefault().getBundle()
								.getBundleContext(), DocumentBuilderService.class, null);
						tracker.open();
						try
						{
							ServiceReference<DocumentBuilderService>[] references = tracker.getServiceReferences();
							if (references != null)
							{
								try
								{
									monitor.beginTask("Dokumente werden erstellt...", references.length);
									for (ServiceReference<DocumentBuilderService> reference : references)
									{
										DocumentBuilderService service = (DocumentBuilderService) tracker
												.getService(reference);
										DocumentBuilderService builderService = service;
										IStatus status = builderService.buildDocument(new SubProgressMonitor(monitor,
												factory.size()), new File(templatePath), factory.getCourses());
										if (status.isOK())
										{
											break;
										}
										monitor.worked(1);
									}
								}
								finally
								{
									monitor.done();
								}
							}
							else
							{
								MessageDialog.openWarning(getShell(), "Service nicht aktiv", "Der Service für die Verarbeitung der Daten ist nicht verfügbar.");
							}
						}
						finally
						{
							tracker.close();
						}
					}
				}
			});
		}
		catch (InvocationTargetException e)
		{
			MessageDialog.openWarning(getShell(), "Fehler", "Ein Fehler ist aufgetreten.\n(" + e.getLocalizedMessage()
					+ ")");
		}
		catch (InterruptedException e)
		{
		}
	}

	private void setCurrentUser()
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class, null);
		tracker.open();
		try
		{
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				UserQuery query = (UserQuery) service.getQuery(User.class);
				User.setCurrent(query.merge(User.getCurrent()));
			}
		}
		finally
		{
			tracker.close();
		}
	}

	@Override
	public void setErrorMessage(final String errorMessage)
	{
		super.setErrorMessage(errorMessage);
		this.setPageComplete(false);
	}

	public void setMessage()
	{
		this.setErrorMessage(null);
		super.setMessage(this.message);
		this.setPageComplete(true);
	}

	public void setPageComplete(final boolean isComplete)
	{
		this.isPageComplete = isComplete;
		if (this.getButton(IDialogConstants.OK_ID) != null)
			this.getButton(IDialogConstants.OK_ID).setEnabled(this.isPageComplete);
	}

	public void setTitle()
	{
		super.setTitle("Kursbeschreibungen erstellen");
	}

	public enum ExportType
	{
		TYPE_XML, TYPE_OPENOFFICE;
		
		public static int maxLabelWith(Composite composite)
		{
			int stringWidth = 0;
		    GC gc = new GC(composite);
			for (ExportType type : ExportType.values())
			{
			    int width = gc.stringExtent(type.pathlabel()).x;
			    if (width > stringWidth)
			    	stringWidth = width;
			}
		    gc.dispose();
		    return stringWidth;
		}
		
		public static String key()
		{
			return "export.type";
		}
		
		public String pathkey()
		{
			switch (this)
			{
			case TYPE_XML:
			{
				return "export.path.xml";
			}
			case TYPE_OPENOFFICE:
			{
				return "export.path.openoffice";
			}
			default:
			{
				throw new RuntimeException("Invalid export type.");
			}
			}
		}
		
		public String[] filterExtensions()
		{
			switch (this)
			{
			case TYPE_XML:
			{
				return new String[] { "*.xml" };
			}
			case TYPE_OPENOFFICE:
			{
				return new String[] { "*.odt" };
			}
			default:
			{
				throw new RuntimeException("Invalid export type.");
			}
			}
		}

		public int dialogType()
		{
			switch (this)
			{
			case TYPE_XML:
			{
				return SWT.SAVE;
			}
			case TYPE_OPENOFFICE:
			{
				return SWT.OPEN;
			}
			default:
			{
				throw new RuntimeException("Invalid export type.");
			}
			}
		}

		public String dialogTitle()
		{
			switch (this)
			{
			case TYPE_XML:
			{
				return "Speichern";
			}
			case TYPE_OPENOFFICE:
			{
				return "Öffnen";
			}
			default:
			{
				throw new RuntimeException("Invalid export type.");
			}
			}
		}

		public String[] filterNames()
		{
			switch (this)
			{
			case TYPE_XML:
			{
				return new String[] { "XML-Datei" };
			}
			case TYPE_OPENOFFICE:
			{
				return new String[] { "OpenOffice Textverarbeitung" };
			}
			default:
			{
				throw new RuntimeException("Invalid export type.");
			}
			}
		}

		
		public String pathlabel()
		{
			switch (this)
			{
			case TYPE_XML:
			{
				return "Name Zieldatei";
			}
			case TYPE_OPENOFFICE:
			{
				return "OpenOffice Vorlage";
			}
			default:
			{
				throw new RuntimeException("Invalid export type.");
			}
			}
		}
		
		public String text()
		{
			switch (this)
			{
			case TYPE_XML:
			{
				return "als XML-Datei";
			}
			case TYPE_OPENOFFICE:
			{
				return "als OpenOffice-Dokument";
			}
			default:
			{
				throw new RuntimeException("Invalid export type.");
			}
			}
		}
	}
}
