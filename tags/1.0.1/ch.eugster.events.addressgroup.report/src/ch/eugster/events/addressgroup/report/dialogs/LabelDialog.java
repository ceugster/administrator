package ch.eugster.events.addressgroup.report.dialogs;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.addressgroup.report.Activator;
import ch.eugster.events.report.engine.ReportService;
import ch.eugster.events.report.engine.ReportService.Destination;

public class LabelDialog extends TitleAreaDialog
{
	private Button groups;
	
	private ComboViewer labelViewer;

	private ComboViewer targetViewer;

	private IDialogSettings settings;

	private final String message = "Wählen Sie das Etikettenformat, das verwendet werden soll.";

	private Destination selectedDestination;

	private URL selectedURL;

	private boolean isPageComplete = false;

	/**
	 * @param parentShell
	 * @param parent
	 *            <code>parent</code> must be of type
	 *            ch.eugster.events.data.objects.Customer
	 * @param addressGroup
	 *            Falls eine neue Adressgruppe erfasst wird, muss diese bereit
	 *            vor der Übergabe an den Konstruktor von
	 *            <code>AddressGroupDialog</code> instantiiert sein und der
	 *            Parent <code>Domain</code> muss - falls ein solcher gesetzt
	 *            werden soll, ebenfalls dem Konstruktur von
	 *            <code>AddressGroup</code> übergeben worden sein.
	 * 
	 */
	public LabelDialog(final Shell parentShell)
	{
		super(parentShell);

		settings = Activator.getDefault().getDialogSettings().getSection("label.selection.dialog");
		if (settings == null)
		{
			settings = Activator.getDefault().getDialogSettings().addNewSection("label.selection.dialog");
		}
		settings.put("destination", Destination.PREVIEW.label());
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent)
	{
		this.createButton(parent, IDialogConstants.OK_ID, "Auswählen", true);
		this.createButton(parent, IDialogConstants.CANCEL_ID, "Abbrechen", false);
		// File file = new File(documentPath.getText());
		// this.getButton(IDialogConstants.OK_ID).setEnabled(file.isFile());
	}

	@Override
	protected Control createDialogArea(final Composite parent)
	{
		this.setTitle();
		this.setMessage();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(2, false));

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		groups = new Button(composite, SWT.CHECK);
		groups.setLayoutData(gridData);
		groups.setText("Gruppenadressen nur einmal verwenden");
		
		Label label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Etikettenvorlage");


		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		labelViewer = new ComboViewer(combo);
		labelViewer.setContentProvider(new ArrayContentProvider());
		labelViewer.setLabelProvider(new LabelProvider());
		labelViewer.addPostSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				String label = (String) ssel.getFirstElement();
				if (label != null)
				{
					settings.put("selected.label", label);
					selectedURL = Activator.getDefault().getBundle().getEntry("/labels/" + label + ".jrxml");

				}
			}
		});
		
		ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(), ReportService.class.getName(), null);
		try
		{
			tracker.open();
			ReportService service = (ReportService) tracker.getService();
			if (service != null)
			{
				List<String> labels = service.getLabelFormats();
				labelViewer.setInput(labels);
				String oldSelection = settings.get("selected.label");
				if (oldSelection == null)
				{
					oldSelection = labels.iterator().next();
				}
				String[] selection = new String[] { oldSelection };
				labelViewer.setSelection(new StructuredSelection(selection));
			}
		}
		finally
		{
			tracker.close();
		}

		label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Ausgabe");

		combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		targetViewer = new ComboViewer(combo);
		targetViewer.setContentProvider(new ArrayContentProvider());
		targetViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(final Object element)
			{
				Destination destination = (Destination) element;
				return destination.label();
			}
		});
		targetViewer.addPostSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(final SelectionChangedEvent event)
			{
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				Destination destination = (Destination) ssel.getFirstElement();
				if (destination != null)
				{
					settings.put("destination", destination.label());
					selectedDestination = destination;
				}
			}
		});

		List<Destination> destinations = new ArrayList<Destination>();
		destinations.add(Destination.PREVIEW);
		destinations.add(Destination.PRINTER);
		targetViewer.setInput(destinations);

		String name = settings.get("destination");
		for (Destination destination : destinations)
		{
			if (destination.label().equals(name))
			{
				targetViewer.setSelection(new StructuredSelection(destination));
			}
		}
		this.isPageComplete = !labelViewer.getSelection().isEmpty() && !targetViewer.getSelection().isEmpty();

		return parent;
	}

	public Destination getDestination()
	{
		return selectedDestination;
	}

	public URL getReport()
	{
		return selectedURL;
	}

	public boolean isPageComplete()
	{
		return this.isPageComplete;
	}

	@Override
	protected void okPressed()
	{
		super.okPressed();
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
		super.setTitle("Auswahl Etikettenvorlage");
	}

}
