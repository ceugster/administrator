package ch.eugster.events.charity.views;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.charity.Activator;
import ch.eugster.events.charity.TagReader;
import ch.eugster.events.charity.listeners.AbstractStartableTagReportListener;
import ch.eugster.events.charity.listeners.DatabaseTagReportListener;
import ch.eugster.events.charity.listeners.StartableTagReportListener;
import ch.eugster.events.charity.preferences.PreferenceInitializer;
import ch.eugster.events.persistence.events.EntityAdapter;
import ch.eugster.events.persistence.events.EntityListener;
import ch.eugster.events.persistence.events.EntityMediator;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.CharityRun;
import ch.eugster.events.persistence.model.CharityRunTagRead;
import ch.eugster.events.persistence.queries.CharityRunQuery;
import ch.eugster.events.persistence.queries.CharityRunTagReadQuery;
import ch.eugster.events.persistence.service.ConnectionService;

import com.impinj.octane.AntennaStatus;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.ReaderStartEvent;
import com.impinj.octane.ReaderStartListener;
import com.impinj.octane.ReaderStopEvent;
import com.impinj.octane.ReaderStopListener;
import com.impinj.octane.Tag;

public class StartNumberAssignView extends ViewPart
{
	private ComboViewer charityRunViewer;
	
	private TableViewer charityTagViewer;

	private Display currentDisplay;

	private StartableTagReportListener tagReportListener;
	
	private PartListener partListener;
	
	private EntityAdapter charityRunListener;
	
	public StartNumberAssignView()
	{
	}
	
	@Override
	public void init(IViewSite site) throws PartInitException 
	{
		super.init(site);
		this.charityRunListener = new EntityAdapter()
		{
			@Override
			public void postDelete(AbstractEntity entity) 
			{
				charityTagViewer.remove(entity);
			}

			@Override
			public void postPersist(AbstractEntity entity) 
			{
				charityTagViewer.add(entity);
				if (!TagReader.isConnected())
				{
					charityTagViewer.getTable().setEnabled(true);
				}
			}

			@Override
			public void postUpdate(AbstractEntity entity) 
			{
				charityTagViewer.refresh(entity);
			}
			
		};
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		ConnectionService connectionService = getConnectionService();

		IPreferenceStore store = new ScopedPreferenceStore(
				InstanceScope.INSTANCE, Activator.PLUGIN_ID);
		String readerAddress = store
				.getString(PreferenceInitializer.KEY_READER_ADDRESS);

		this.currentDisplay = this.getSite().getShell().getDisplay();

		parent.setLayout(new FillLayout());

		Composite composite = new Composite(parent, SWT.None);
		composite.setLayout(new GridLayout(3, false));

		Label label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Sponsorlauf");

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		Combo combo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(gridData);

		this.charityRunViewer = new ComboViewer(combo);
		this.charityRunViewer.setContentProvider(new ArrayContentProvider());
		this.charityRunViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public Image getImage(Object element)
			{
				return Activator.getDefault().getImageRegistry().get("RUNNER");
			}

			@Override
			public String getText(Object element)
			{
				CharityRun charityRun = (CharityRun) element;
				return charityRun.getName()
						+ (charityRun.getDate() == null ? "" : " "
								+ SimpleDateFormat.getDateTimeInstance()
										.format(charityRun.getDate().getTime()));
			}
		});
		this.charityRunViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection ssel = (IStructuredSelection) event
						.getSelection();
				if (ssel.isEmpty())
				{
					TagReader.setCharityRun(null);
					connectDisconnect.setEnabled(false);
				}
				else
				{
					TagReader.setCharityRun((CharityRun) ssel
							.getFirstElement());
					connectDisconnect.setEnabled(true);
				}
			}
		});

		Label label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Sponsorlauf");
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;
		
		Table table = new Table(composite, SWT.BORDER);
		table.setLayoutData(gridData);

		this.charityTagViewer = new TableViewer(table);
		this.charityTagViewer.setContentProvider(new ArrayContentProvider());
		this.charityTagViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public Image getImage(Object element) 
			{
				return null;
			}

			@Override
			public String getText(Object element) 
			{
				CharityRun charityRun = (CharityRun) element;
				return charityRun.getName() + (charityRun.getDate() == null ? "" : " " + SimpleDateFormat.getDateTimeInstance().format(charityRun.getDate().getTime()));
			}
		});
		this.charityTagViewer.addSelectionChangedListener(new ISelectionChangedListener() 
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				IStructuredSelection ssel = (IStructuredSelection) event.getSelection();
				if (ssel.isEmpty())
				{
					TagReader.setCharityRun(null);
					connectDisconnect.setEnabled(false);
				}
				else
				{
					TagReader.setCharityRun((CharityRun) ssel.getFirstElement());
					connectDisconnect.setEnabled(true);
				}
			}
		});
	
		CharityRunTagReadQuery query = (CharityRunTagReadQuery) connectionService.getQuery(CharityRunTagRead.class);
		StartableTagReportListener tagReportListener = new DatabaseTagReportListener(query);
		TagReader.addTagReportListener(tagReportListener);
        TagReader.setReaderStartListener(new ReaderStartListener() 
        {
			@Override
			public void onReaderStart(ImpinjReader reader, ReaderStartEvent event) 
			{
				currentDisplay.asyncExec(new Runnable() 
				{
					@Override
					public void run() 
					{
						startStop.setText("Stop");
						startStop.setEnabled(true);
						connectDisconnect.setEnabled(false);
					}
				});
			}
		});
        TagReader.setReaderStopListener(new ReaderStopListener() 
        {
			@Override
			public void onReaderStop(ImpinjReader reader, ReaderStopEvent event) 
			{
				currentDisplay.asyncExec(new Runnable() 
				{
					@Override
					public void run() 
					{
						startStop.setText("Start");
						startStop.setEnabled(true);
						connectDisconnect.setEnabled(true);
					}
				});
			}
		});
//        reader.setTagOpCompleteListener(new TagOpCompleteListenerImplementation());
        CharityRunQuery charityRunQuery = (CharityRunQuery) connectionService.getQuery(CharityRun.class);
        CharityRun[] charityRuns = charityRunQuery.selectActives().toArray(new CharityRun[0]);
        this.charityTagViewer.setInput(charityRuns);
        if (charityRuns.length > 0)
        {
        	CharityRun charityRun = TagReader.getCharityRun();
        	if (charityRun == null)
        	{
	        	Calendar calendar = GregorianCalendar.getInstance();
	        	for (CharityRun crun : charityRuns)
	        	{
	        		int year1 = crun.getDate().get(Calendar.YEAR);
	        		int month1 = crun.getDate().get(Calendar.MONTH);
	        		int day1 = crun.getDate().get(Calendar.DATE);
	        		int year2 = calendar.get(Calendar.YEAR);
	        		int month2 = calendar.get(Calendar.MONTH);
	        		int day2 = calendar.get(Calendar.DATE);
	        		if (year1 == year2 && month1 == month2 && day1 == day2);
	        		{
	    	        	this.charityTagViewer.setSelection(new StructuredSelection(new CharityRun[] { charityRun }));
	        		}
	        	}
	        	if (this.charityTagViewer.getSelection().isEmpty())
	        	{
    	        	this.charityTagViewer.setSelection(new StructuredSelection(new CharityRun[] { charityRuns[0] }));
	        	}
        	}
        	else
        	{
	        	this.charityTagViewer.setSelection(new StructuredSelection(new CharityRun[] { charityRun }));
        	}
        }
        else
        {
        	this.connectDisconnect.setEnabled(false);
        }
        this.partListener = new PartListener();
        if (this.getSite().getPage() != null)
        {
        	this.getSite().getPage().addPartListener(this.partListener);
        }
	}

	private ConnectionService getConnectionService()
	{
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(Activator.getDefault().getBundleContext(), ConnectionService.class, null);
		tracker.open();
		try
		{
			return tracker.getService();
		}
		finally
		{
			tracker.close();
		}
	}
	
	private void connectReader() throws OctaneSdkException
	{
		TagReader.connect(hostname.getText(), new JobChangeAdapter() 
		{
			@Override
			public void done(IJobChangeEvent event) 
			{
				UIJob job = new UIJob("Benutzeroberfläche wird aktualisiert") 
				{
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) 
					{
						if (TagReader.isConnected())
						{
							charityTagViewer.getTable().setEnabled(false);
							AntennaStatus[] states = TagReader.queryStatus().getAntennaStatusGroup().getAntennaList().toArray(new AntennaStatus[0]);
							antennaGroup.setLayout(new GridLayout(states.length, true));
							antennas = new Label[states.length];
							for (int i = 0; i < antennas.length; i++)
							{
								Label label = new Label(antennaGroup, SWT.None);
								label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
								label.setText("Antenne " + Integer.toString(i + 1));
							}
							for (int i = 0; i < antennas.length; i++)
							{
								antennas[i] = new Label(antennaGroup, SWT.None);
								antennas[i].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
								if (states[i].isConnected())
								{
									antennas[i].setImage(Activator.getDefault().getImageRegistry().get("GREEN"));
								}
								else
								{
									antennas[i].setImage(Activator.getDefault().getImageRegistry().get("RED"));
								}
							}
							antennaGroup.layout();
							bufferState.setImage(Activator.getDefault().getImageRegistry().get("GREEN"));
							startStop.setEnabled(true);
							connectDisconnect.setText("Trennen");
						}
						return Status.OK_STATUS;
					}
				};
				job.setUser(true);
				job.schedule();
			}
		});
	}
	
	private void disconnectReader()
	{
		if (!antennaGroup.isDisposed())
		{
			Control[] children = antennaGroup.getChildren();
			for (Control child : children)
			{
				child.dispose();
				child = null;
			}
		}
		if (!bufferState.isDisposed())
		{
			bufferState.setImage(null);
		}
		TagReader.disconnect();
	}
	
	public void dispose()
	{
        if (this.getSite().getPage() != null)
        {
        	this.getSite().getPage().removePartListener(this.partListener);
        }
		EntityMediator.removeListener(CharityRun.class, this.charityRunListener);
        TagReader.setAntennaChangeListener(null);
        TagReader.setBufferOverflowListener(null);
        TagReader.setBufferWarningListener(null);
        TagReader.setConnectionAttemptListener(null);
        TagReader.setConnectionCloseListener(null);
        TagReader.setConnectionLostListener(null);
        TagReader.setDiagnosticsReportListener(null);
        TagReader.setDirectionReportListener(null);
//        reader.setGpiChangeListener(null);
//        reader.setKeepaliveListener(null);
//        reader.setLocationReportListener(null);
        TagReader.setReaderStartListener(null);
        TagReader.setReaderStopListener(null);
//        reader.setTagOpCompleteListener(new TagOpCompleteListenerImplementation());
		super.dispose();
	}
	
	@Override
	public void setFocus() 
	{
		this.connectDisconnect.setFocus();
	}
	
	class UITagReportListener extends AbstractStartableTagReportListener
	{
		private UIJob job;

		private Tag tag;
		
		private boolean canceled = false;
		
		public String label()
		{
			return "Anzeige aktiviert";
		}
		
		@Override
		public void starting() 
		{
			this.canceled = false;
			job = new UIJob("Process tag") 
			{
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) 
				{
					if (!StartNumberAssignView.this.tag.isDisposed())
					{
						StartNumberAssignView.this.tag.setText(tag.getEpc().toHexString());
					}
					return Status.OK_STATUS;
				}
			};
		}

		@Override
		public void stopping() 
		{
			this.canceled = true;
			if (job != null)
			{
				job.cancel();
			}
		}

		@Override
		public void process(final Tag tag) 
		{
			this.tag = tag;
			if (!canceled)
			{
				job.schedule();
			}
		}
	}

	class PartListener implements IPartListener
	{
		@Override
		public void partActivated(IWorkbenchPart part)
		{
			IViewPart part1 = null;
			IViewPart part2 = null;
			IWorkbenchPage[] pages = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages();
			for (IWorkbenchPage page : pages)
			{
				if (part1 == null)
				{
					part1 = page.findView("ch.eugster.events.charity.view.tagreader");
					
				}
				if (part2 == null)
				{
					part2 = page.findView("ch.eugster.events.charity.view.startnumberassign");
				}
				if (part1 != null && part2 != null)
				{
					break;
				}
			}
			if (part1 != null && part.equals(part1)) 
			{
				TagReader.removeTagReportListener(tagReportListener);
			}
			else if (part2 != null && part.equals(part2)) 
			{
				TagReader.addTagReportListener(tagReportListener);
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part)
		{
			this.partActivated(part);
		}

		@Override
		public void partClosed(IWorkbenchPart part)
		{
		}

		@Override
		public void partDeactivated(IWorkbenchPart part)
		{
		}

		@Override
		public void partOpened(IWorkbenchPart part)
		{
			this.partActivated(part);
		}
	}
}
