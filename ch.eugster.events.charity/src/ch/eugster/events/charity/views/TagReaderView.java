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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import ch.eugster.events.charity.listeners.FileTagReportListener;
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

import com.impinj.octane.AntennaChangeListener;
import com.impinj.octane.AntennaConfig;
import com.impinj.octane.AntennaEvent;
import com.impinj.octane.AntennaEventType;
import com.impinj.octane.AntennaStatus;
import com.impinj.octane.BufferOverflowEvent;
import com.impinj.octane.BufferOverflowListener;
import com.impinj.octane.BufferWarningEvent;
import com.impinj.octane.BufferWarningListener;
import com.impinj.octane.ConnectionAttemptEvent;
import com.impinj.octane.ConnectionAttemptListener;
import com.impinj.octane.ConnectionCloseEvent;
import com.impinj.octane.ConnectionCloseListener;
import com.impinj.octane.ConnectionLostListener;
import com.impinj.octane.DiagnosticReport;
import com.impinj.octane.DiagnosticsReportListener;
import com.impinj.octane.DirectionReport;
import com.impinj.octane.DirectionReportListener;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.ReaderStartEvent;
import com.impinj.octane.ReaderStartListener;
import com.impinj.octane.ReaderStopEvent;
import com.impinj.octane.ReaderStopListener;
import com.impinj.octane.Settings;
import com.impinj.octane.Tag;

public class TagReaderView extends ViewPart
{
	private ComboViewer charityRunViewer;

	private Text hostname;

	private Button[] tagListenerControls;

	private Group antennaGroup;

	private Label[] antennas;

	private Label bufferState;

	private Button connectDisconnect;

	private Button startStop;

	private Text tag;

	private Display currentDisplay;

	private EntityListener charityRunListener;

	private IPartListener partListener;

	public TagReaderView()
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
				charityRunViewer.remove(entity);
			}

			@Override
			public void postPersist(AbstractEntity entity) 
			{
				charityRunViewer.add(entity);
				if (!TagReader.isConnected())
				{
					charityRunViewer.getCombo().setEnabled(true);
				}
			}

			@Override
			public void postUpdate(AbstractEntity entity) 
			{
				charityRunViewer.refresh(entity);
			}
			
		};
		EntityMediator.addListener(CharityRun.class, this.charityRunListener);
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
		this.charityRunViewer
				.addSelectionChangedListener(new ISelectionChangedListener()
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

		label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Hostname des Tag Lesers");

		this.hostname = new Text(composite, SWT.BORDER);
		this.hostname.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		this.hostname.setText(readerAddress);
		this.hostname.setEnabled(false);

		this.connectDisconnect = new Button(composite, SWT.PUSH);
		this.connectDisconnect.setLayoutData(new GridData());
		this.connectDisconnect.setText(TagReader.isConnected() ? " Trennen "
				: " Verbinden ");
		this.connectDisconnect.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				connectDisconnect.setEnabled(false);
				if (TagReader.isConnected())
				{
					disconnectReader();
					startStop.setEnabled(false);
					connectDisconnect.setText("Verbinden");
					charityRunViewer.getCombo().setEnabled(true);
				}
				else
				{
					try
					{
						connectReader();
					}
					catch (OctaneSdkException ex)
					{
						MessageDialog
								.openConfirm(
										getSite().getShell(),
										"Verbindungsfehler",
										"Die Verbindung zum Tag Leser "
												+ hostname.getText()
												+ " kann nicht hergestellt werden. Bitte versichern Sie sich, dass der Leser eingeschaltet und mit dem Computer verbunden ist.");
					}
				}
				connectDisconnect.setEnabled(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				this.widgetSelected(e);
			}
		});

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		gridData.heightHint = 48;

		antennaGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		antennaGroup.setLayoutData(gridData);
		antennaGroup.setLayout(new GridLayout(8, true));
		antennaGroup.setText("Antennen");

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		gridData.heightHint = 28;

		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(8, true));
		group.setText("Buffer Status");

		bufferState = new Label(group, SWT.None);
		bufferState.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;

		group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(3, true));

		tagListenerControls = new Button[connectionService == null ? 2 : 3];
		StartableTagReportListener[] tagReportListeners = new StartableTagReportListener[tagListenerControls.length];
		tagReportListeners[0] = new UITagReportListener();
		tagReportListeners[1] = new FileTagReportListener();
		if (connectionService != null)
		{
			CharityRunTagReadQuery query = (CharityRunTagReadQuery) connectionService
					.getQuery(CharityRunTagRead.class);
			tagReportListeners[2] = new DatabaseTagReportListener(query);
		}
		for (int i = 0; i < tagListenerControls.length; i++)
		{
			tagListenerControls[i] = new Button(group, SWT.CHECK);
			tagListenerControls[i].setLayoutData(new GridData(
					GridData.FILL_HORIZONTAL));
			tagListenerControls[i].setText(tagReportListeners[i].label());
			tagListenerControls[i].setData(tagReportListeners[i]);
			tagListenerControls[i].addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					Button button = (Button) e.widget;
					StartableTagReportListener listener = (StartableTagReportListener) button
							.getData();
					listener.setActive(button.getSelection());
					if (!button.getSelection())
					{
						tag.setText("");
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e)
				{
					widgetSelected(e);
				}
			});
			// tagReportListeners[i].addStartStopListener(new
			// StartStopListener(tagListenerControls[i]));
		}

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		gridData.heightHint = 28;

		startStop = new Button(composite, SWT.PUSH);
		startStop.setLayoutData(gridData);
		startStop.setText(TagReader.isStarted() ? " Stop " : "Start");
		startStop.setEnabled(TagReader.isConnected());
		startStop.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (TagReader.isConnected())
				{
					if (TagReader.isStarted())
					{
						TagReader.stop();
					}
					else
					{
						TagReader.start();
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				this.widgetSelected(e);
			}
		});

		label = new Label(composite, SWT.None);
		label.setLayoutData(new GridData());
		label.setText("Gelesener Tag");

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;

		this.tag = new Text(composite, SWT.BORDER);
		this.tag.setLayoutData(gridData);

		if (TagReader.isConnected())
		{
			Settings settings = TagReader.querySettings();
			AntennaStatus[] states = TagReader.queryStatus()
					.getAntennaStatusGroup().getAntennaList()
					.toArray(new AntennaStatus[0]);

			antennaGroup.setLayout(new GridLayout(states.length, true));
			antennas = new Label[states.length];
			for (int i = 0; i < antennas.length; i++)
			{
				label = new Label(antennaGroup, SWT.None);
				label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				label.setText("Antenne " + Integer.toString(i + 1));
			}
			for (int i = 0; i < antennas.length; i++)
			{
				antennas[i] = new Label(antennaGroup, SWT.None);
				antennas[i]
						.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				if (states[i].isConnected())
				{
					AntennaConfig config;
					try
					{
						config = settings.getAntennas().getAntenna(
								states[i].getPortNumber());
						config.setEnabled(true);
						config.setIsMaxRxSensitivity(true);
						config.setIsMaxTxPower(true);
						antennas[i].setImage(Activator.getDefault()
								.getImageRegistry().get("GREEN"));
					}
					catch (OctaneSdkException e1)
					{
						e1.printStackTrace();
					}
				}
				else
				{
					antennas[i].setImage(Activator.getDefault()
							.getImageRegistry().get("RED"));
				}
			}
			antennaGroup.layout();
			bufferState.setImage(Activator.getDefault().getImageRegistry()
					.get("GREEN"));
		}

		for (Button tagListenerControl : tagListenerControls)
		{
			TagReader
					.addTagReportListener((StartableTagReportListener) tagListenerControl
							.getData());
		}
		TagReader.setAntennaChangeListener(new AntennaChangeListener()
		{
			@Override
			public void onAntennaChanged(ImpinjReader reader,
					final AntennaEvent event)
			{
				final short port = event.getPortNumber();
				currentDisplay.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						boolean antennaConnected = event.getState().equals(
								AntennaEventType.AntennaConnected);
						antennas[port - 1].setImage(Activator.getDefault()
								.getImageRegistry()
								.get(antennaConnected ? "GREEN" : "RED"));
					}
				});
			}
		});
		TagReader.setBufferOverflowListener(new BufferOverflowListener()
		{
			@Override
			public void onBufferOverflow(ImpinjReader reader,
					BufferOverflowEvent event)
			{
				currentDisplay.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						bufferState.setImage(Activator.getDefault()
								.getImageRegistry().get("RED"));
					}
				});
			}
		});
		TagReader.setMessageTimeout(store
				.getInt(PreferenceInitializer.KEY_REACHABLE_TIMEOUT));
		TagReader.setBufferWarningListener(new BufferWarningListener()
		{
			@Override
			public void onBufferWarning(ImpinjReader reader,
					BufferWarningEvent event)
			{
				currentDisplay.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						bufferState.setImage(Activator.getDefault()
								.getImageRegistry().get("YELLOW"));
					}
				});
			}
		});
		TagReader.setConnectionAttemptListener(new ConnectionAttemptListener()
		{
			@Override
			public void onConnectionAttempt(ImpinjReader reader,
					ConnectionAttemptEvent event)
			{
				System.out.println();
			}
		});
		TagReader.setConnectionCloseListener(new ConnectionCloseListener()
		{
			@Override
			public void onConnectionClose(ImpinjReader reader,
					ConnectionCloseEvent event)
			{
				currentDisplay.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						for (int i = 0; i < antennas.length; i++)
						{
							antennas[i].setImage(null);
						}
						bufferState.setImage(null);
					}
				});
			}
		});
		TagReader.setConnectionLostListener(new ConnectionLostListener()
		{
			@Override
			public void onConnectionLost(ImpinjReader reader)
			{
				currentDisplay.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						MessageDialog
								.openInformation(
										TagReaderView.this.getSite().getShell(),
										"Verbindung verloren",
										"Die Verbindung zum Tag Leser "
												+ hostname.getText()
												+ " ging verloren. Bitte versuchen Sie, den Leser wieder zu verbinden.");
						for (int i = 0; i < antennas.length; i++)
						{
							antennas[i].setImage(null);
						}
						bufferState.setImage(null);
						startStop.setText("Start");
						startStop.setEnabled(false);
						connectDisconnect.setEnabled(true);
						connectDisconnect.setText("Verbinden");
					}
				});
			}

		});
		TagReader.setDiagnosticsReportListener(new DiagnosticsReportListener()
		{
			@Override
			public void onDiagnosticsReported(ImpinjReader reader,
					DiagnosticReport report)
			{
				currentDisplay.asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
					}
				});
			}
		});
		TagReader.setDirectionReportListener(new DirectionReportListener()
		{
			@Override
			public void onDirectionReported(ImpinjReader arg0,
					DirectionReport arg1)
			{
				System.out.println(arg1.getReportType().name());
				System.out.println(SimpleDateFormat.getDateTimeInstance()
						.format(arg1.getLastReadTime().getLocalDateTime()));
			}
		});
		// reader.setGpiChangeListener(new GpiChangeListenerImplementation());
		// reader.setKeepaliveListener(new KeepAliveListenerImplementation());
		// reader.setLocationReportListener(new
		// LocationReportListenerImplementation());
		TagReader.setReaderStartListener(new ReaderStartListener()
		{
			@Override
			public void onReaderStart(ImpinjReader reader,
					ReaderStartEvent event)
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
		// reader.setTagOpCompleteListener(new
		// TagOpCompleteListenerImplementation());
		CharityRunQuery charityRunQuery = (CharityRunQuery) connectionService
				.getQuery(CharityRun.class);
		CharityRun[] charityRuns = charityRunQuery.selectActives().toArray(
				new CharityRun[0]);
		this.charityRunViewer.setInput(charityRuns);
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
					if (year1 == year2 && month1 == month2 && day1 == day2)
						;
					{
						this.charityRunViewer
								.setSelection(new StructuredSelection(
										new CharityRun[]
										{ charityRun }));
					}
				}
				if (this.charityRunViewer.getSelection().isEmpty())
				{
					this.charityRunViewer.setSelection(new StructuredSelection(
							new CharityRun[]
							{ charityRuns[0] }));
				}
			}
			else
			{
				this.charityRunViewer.setSelection(new StructuredSelection(
						new CharityRun[]
						{ charityRun }));
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
		ServiceTracker<ConnectionService, ConnectionService> tracker = new ServiceTracker<ConnectionService, ConnectionService>(
				Activator.getDefault().getBundleContext(),
				ConnectionService.class, null);
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
							charityRunViewer.getCombo().setEnabled(false);
							AntennaStatus[] states = TagReader.queryStatus()
									.getAntennaStatusGroup().getAntennaList()
									.toArray(new AntennaStatus[0]);
							antennaGroup.setLayout(new GridLayout(
									states.length, true));
							antennas = new Label[states.length];
							for (int i = 0; i < antennas.length; i++)
							{
								Label label = new Label(antennaGroup, SWT.None);
								label.setLayoutData(new GridData(
										GridData.FILL_HORIZONTAL));
								label.setText("Antenne "
										+ Integer.toString(i + 1));
							}
							for (int i = 0; i < antennas.length; i++)
							{
								antennas[i] = new Label(antennaGroup, SWT.None);
								antennas[i].setLayoutData(new GridData(
										GridData.FILL_HORIZONTAL));
								if (states[i].isConnected())
								{
									antennas[i].setImage(Activator.getDefault()
											.getImageRegistry().get("GREEN"));
								}
								else
								{
									antennas[i].setImage(Activator.getDefault()
											.getImageRegistry().get("RED"));
								}
							}
							antennaGroup.layout();
							bufferState.setImage(Activator.getDefault()
									.getImageRegistry().get("GREEN"));
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
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null)
		{
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().removePartListener(this.partListener);
		}
		if (TagReader.isStarted())
		{
			TagReader.stop();
		}
		if (TagReader.isConnected())
		{
			TagReader.disconnect();
		}
		EntityMediator
				.removeListener(CharityRun.class, this.charityRunListener);
		TagReader.setAntennaChangeListener(null);
		TagReader.setBufferOverflowListener(null);
		TagReader.setBufferWarningListener(null);
		TagReader.setConnectionAttemptListener(null);
		TagReader.setConnectionCloseListener(null);
		TagReader.setConnectionLostListener(null);
		TagReader.setDiagnosticsReportListener(null);
		TagReader.setDirectionReportListener(null);
		// reader.setGpiChangeListener(null);
		// reader.setKeepaliveListener(null);
		// reader.setLocationReportListener(null);
		TagReader.setReaderStartListener(null);
		TagReader.setReaderStopListener(null);
		// reader.setTagOpCompleteListener(new
		// TagOpCompleteListenerImplementation());
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
					if (!TagReaderView.this.tag.isDisposed())
					{
						TagReaderView.this.tag.setText(tag.getEpc()
								.toHexString());
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
			if (part2 != null && part.equals(part2)) 
			{
				for (Button tagListenerControl : tagListenerControls)
				{
					StartableTagReportListener listener = (StartableTagReportListener) tagListenerControl.getData();
					TagReader.removeTagReportListener(listener);
				}
			}
			else if (part1 != null && part.equals(part1)) 
			{
				for (Button tagListenerControl : tagListenerControls)
				{
					StartableTagReportListener listener = (StartableTagReportListener) tagListenerControl.getData();
					TagReader.addTagReportListener(listener);
				}
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
			System.out.println();
		}

		@Override
		public void partDeactivated(IWorkbenchPart part)
		{
			System.out.println();
		}

		@Override
		public void partOpened(IWorkbenchPart part)
		{
			System.out.println();
			this.partActivated(part);
		}
	}
}
