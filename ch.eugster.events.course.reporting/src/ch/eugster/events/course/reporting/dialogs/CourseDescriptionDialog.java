package ch.eugster.events.course.reporting.dialogs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
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

	private final String message = "Kursbeschreibungen erstellen.";

	private boolean isPageComplete = false;

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
		templatePath = settings.get("template.path") == null ? "" : settings.get("template.path");
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

		Label label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Vorlage");
		
		final Text path = new Text(composite, SWT.BORDER);
		path.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		path.setText(templatePath);
		path.addModifyListener(new ModifyListener() 
		{
			@Override
			public void modifyText(ModifyEvent e) 
			{
				templatePath = path.getText();
				settings.put("template.path", templatePath);
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
				FileDialog dialog = new FileDialog(CourseDescriptionDialog.this.getShell());
				dialog.setFileName(templatePath);
				dialog.setFilterExtensions(new String[] { "*.odt" });
				dialog.setFilterIndex(0);
				dialog.setFilterNames(new String[] { "OpenOffice Textverarbeitung" });
				dialog.setText("Wählen Sie die gewünschte Vorlage aus");
				String result = dialog.open();
				if (result != null)
				{
					path.setText(result);
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
//					Arrays.sort(factory.getCourses(), new Comparator<CourseMap>() 
//					{
//						@Override
//						public int compare(CourseMap map1, CourseMap map2) 
//						{
//							map1.getProperty(CourseMap.Key..FIRST_DATE.getKey())
//							return 0;
//						}
//					});
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
						ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle()
								.getBundleContext(), DocumentBuilderService.class.getName(), null);
						try
						{
							tracker.open();
							ServiceReference[] references = tracker.getServiceReferences();
							if (references != null)
							{
								try
								{
									monitor.beginTask("Dokumente werden erstellt...", references.length);
									for (ServiceReference reference : references)
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
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
				ConnectionService.class.getName(), null);
		tracker.open();
		ConnectionService service = (ConnectionService) tracker.getService();
		if (service != null)
		{
			UserQuery query = (UserQuery) service.getQuery(User.class);
			User.setCurrent(query.merge(User.getCurrent()));
		}
		tracker.close();
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
		super.setTitle("Kursliste generieren");
	}
}
