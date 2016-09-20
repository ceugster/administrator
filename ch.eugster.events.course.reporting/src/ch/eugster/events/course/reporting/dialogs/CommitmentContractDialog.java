package ch.eugster.events.course.reporting.dialogs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.course.reporting.Activator;
import ch.eugster.events.documents.maps.CourseGuideMap;
import ch.eugster.events.documents.maps.DataMap;
import ch.eugster.events.documents.services.DocumentBuilderService;
import ch.eugster.events.persistence.model.Course;
import ch.eugster.events.persistence.model.CourseGuide;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.model.UserProperty;
import ch.eugster.events.persistence.queries.UserQuery;
import ch.eugster.events.persistence.service.ConnectionService;

public class CommitmentContractDialog extends TitleAreaDialog
{
	private ComboViewer documentPath;

	private Button documentSelector;

	private final StructuredSelection selection;

	private String templatePath;

	private UserProperty userPropertyTemplatePath;

	private final String message = "W�hlen Sie das Dokument, das als Vorlage verwendet werden soll.";

	private static final String MSG_NO_SERVICE_AVAILABLE = "Es ist kein Service f�r die Verarbeitung des Dokuments verf�gbar.";

	private static final String MSG_TITLE_NO_COURSES = "Keine Kurse vorhanden";

	private static final String OK_BUTTON_TEXT = "Generieren";

	private static final String CANCEL_BUTTON_TEXT = "Abbrechen";

	private static final String DIALOG_TITLE = "Vorlage Einsatzvertrag";

	private boolean isPageComplete = false;

	public CommitmentContractDialog(final Shell parentShell, final StructuredSelection selection)
	{
		super(parentShell);
		this.selection = selection;
	}

	private void buildDocument()
	{
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try
		{
			dialog.run(true, true, new IRunnableWithProgress()
			{
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					DataMap[] maps = createDataMaps().toArray(new DataMap[0]);
					if (maps.length == 0)
					{
						MessageDialog.openConfirm(getShell(), MSG_TITLE_NO_COURSES, MSG_TITLE_NO_COURSES);
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
												maps.length), new File(templatePath), maps);
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
								MessageDialog.openWarning(getShell(), "Service nicht aktiv", MSG_NO_SERVICE_AVAILABLE);
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

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, OK_BUTTON_TEXT, true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, CANCEL_BUTTON_TEXT, false);
		this.getButton(IDialogConstants.OK_ID).setEnabled(!documentPath.getSelection().isEmpty());
	}

	private List<DataMap> createDataMaps()
	{
		List<DataMap> dataMaps = new ArrayList<DataMap>();
		Object[] elements = selection.toArray();
		for (Object element : elements)
		{
			if (element instanceof Course)
			{
				Course course = (Course) element;
				List<CourseGuide> courseGuides = course.getCourseGuides();
				for (CourseGuide courseGuide : courseGuides)
				{
					dataMaps.add(new CourseGuideMap(courseGuide, true));
				}
			}
			if (element instanceof CourseGuide)
			{
				CourseGuide courseGuide = (CourseGuide) element;
				dataMaps.add(new CourseGuideMap(courseGuide, true));
			}
		}
		return dataMaps;
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle(DIALOG_TITLE);
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Vorlage");

		Combo combo = new Combo(composite, SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setText("TEST");
		combo.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e) 
			{
				File file = new File(((Combo)e.getSource()).getText());
				if (CommitmentContractDialog.this.getButton(IDialogConstants.OK_ID) != null)
					CommitmentContractDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(file.isFile());
			}
		});
		combo.addFocusListener(new FocusAdapter() 
		{
			@Override
			public void focusLost(FocusEvent e) 
			{
				File file = new File(((Combo)e.getSource()).getText());
				if (file.exists())
				{
					updatePathList(file.getAbsolutePath());
				}
			}
		});
		documentPath = new ComboViewer(combo);
		documentPath.setContentProvider(new ArrayContentProvider());
		documentPath.setLabelProvider(new LabelProvider());
		documentPath.addSelectionChangedListener(new ISelectionChangedListener() 
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				if (!event.getSelection().isEmpty())
				{
					String path = ((IStructuredSelection) event.getSelection()).getFirstElement().toString();
					updatePathList(path);
				}
				if (CommitmentContractDialog.this.getButton(IDialogConstants.OK_ID) != null)
					CommitmentContractDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(!documentPath.getSelection().isEmpty());
			}
		});
		documentPath.setInput(getPathList());
		documentPath.setSelection(new StructuredSelection(new String[] { getPathList()[0] }));
		templatePath = getPathList()[0];

		documentSelector = new Button(composite, SWT.PUSH);
		documentSelector.setText("...");
		documentSelector.setLayoutData(new GridData());
		documentSelector.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				String path = null;
				IStructuredSelection ssel = (IStructuredSelection) CommitmentContractDialog.this.documentPath.getSelection();
				if (!ssel.isEmpty())
				{
					path = ssel.getFirstElement().toString();
				}
				FileDialog dialog = new FileDialog(CommitmentContractDialog.this.getShell());
				dialog.setFilterPath(path);
				dialog.setFilterExtensions(new String[] { "*.odt" });
				dialog.setText(DIALOG_TITLE);
				path = dialog.open();
				if (path != null)
				{
					File file = new File(path);
					if (file.isFile())
					{
						updatePathList(path);
					}
				}
				CommitmentContractDialog.this.getButton(IDialogConstants.OK_ID).setEnabled(!documentPath.getSelection().isEmpty());
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
		savePathList();
		super.okPressed();
		buildDocument();
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
	
	private String[] getPathList()
	{
		if (this.userPropertyTemplatePath == null)
		{
			this.userPropertyTemplatePath = User.getCurrent() == null 
						? null 
						: User.getCurrent().getProperty(UserProperty.Property.COMMITMENT_CONTRACT_TEMPLATE_PATH.key());
			if (this.userPropertyTemplatePath == null)
			{
				this.userPropertyTemplatePath = UserProperty.newInstance(User.getCurrent());
				this.userPropertyTemplatePath.setKey(UserProperty.Property.COMMITMENT_CONTRACT_TEMPLATE_PATH.key());
				this.userPropertyTemplatePath.setValue(System.getProperty("user.home") + "|");
			}
		}
		return this.userPropertyTemplatePath.getValue().split("[|]");
	}

	private void updatePathList(String path)
	{
		int size = getPathList().length;
		String paths = this.userPropertyTemplatePath.getValue();
		while (paths.contains(path + "|"))
		{
			paths = paths.replace(path + "|", "");
		}
		paths = path + "|" + paths;
		String[] pathList = getPathList();
		for (String pathInList : pathList)
		{
			if (!new File(pathInList).isFile())
			{
				paths = paths.replace(pathInList + "|", "");
			}
		}
		userPropertyTemplatePath.setValue(paths);
		pathList = getPathList();
		if (pathList.length > 4)
		{
			for (int i = 4; i < pathList.length; i++)
			{
				paths = paths.replace(pathList[i] + "|", "");
			}
		}
		userPropertyTemplatePath.setValue(paths);
		templatePath = path;
		if (size != pathList.length)
		{
			documentPath.setInput(getPathList());
			documentPath.setSelection(new StructuredSelection(new String[] { templatePath }));
		}
	}
	
	private void savePathList()
	{
		if (User.getCurrent() != null)
		{
			ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class, null);
			tracker.open();
			try
			{
				Object service = tracker.getService();
				if (service instanceof ConnectionService)
				{
					this.userPropertyTemplatePath.setUser(User.getCurrent());
					ConnectionService connectionService = (ConnectionService) service;
					User.getCurrent().setProperty(this.userPropertyTemplatePath);
					UserQuery query = (UserQuery) connectionService.getQuery(User.class);
					User.setCurrent(query.merge(User.getCurrent()));
				}
			}
			finally
			{
				tracker.close();
			}
		}
	}
}
