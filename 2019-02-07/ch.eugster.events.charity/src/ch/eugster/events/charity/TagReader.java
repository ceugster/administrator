package ch.eugster.events.charity;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.progress.UIJob;

import ch.eugster.events.charity.listeners.StartableTagReportListener;
import ch.eugster.events.persistence.model.CharityRun;

import com.impinj.octane.AntennaChangeListener;
import com.impinj.octane.BufferOverflowListener;
import com.impinj.octane.BufferWarningListener;
import com.impinj.octane.ConnectionAttemptListener;
import com.impinj.octane.ConnectionCloseListener;
import com.impinj.octane.ConnectionLostListener;
import com.impinj.octane.DiagnosticsReportListener;
import com.impinj.octane.DirectionReportListener;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.ReaderMode;
import com.impinj.octane.ReaderStartListener;
import com.impinj.octane.ReaderStopListener;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.Settings;
import com.impinj.octane.Status;
import com.impinj.octane.Tag;
import com.impinj.octane.TagOpCompleteListener;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;

public class TagReader 
{
	private static boolean connected = false;
	
	private static boolean started = false;
	
	private static ImpinjReader reader = new ImpinjReader();

	private static TagReportMediator tagReportMediator = new TagReportMediator();
	
	private static CharityRun charityRun; 
	
	public static void setCharityRun(CharityRun charityRun)
	{
		TagReader.charityRun = charityRun;
	}
	
	public static CharityRun getCharityRun()
	{
		return TagReader.charityRun;
	}
	
	public static boolean isConnected()
	{
		return connected;
	}
	
	public static boolean isStarted()
	{
		return started;
	}
	
	public static void connect(final String hostname, IJobChangeListener connectingListener) throws OctaneSdkException
	{
		if (!connected)
		{
			UIJob job = new UIJob("Verbindung zum Tag Leser wird hergestellt...") 
			{
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) 
				{
					try 
					{
						reader.setTagReportListener(tagReportMediator);
						connected = true;
						reader.connect(hostname);
						return org.eclipse.core.runtime.Status.OK_STATUS;
					} 
					catch (OctaneSdkException ose)
					{
						connected = false;
						return new org.eclipse.core.runtime.Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), ose.getLocalizedMessage(), ose);
					}
				}
			};
			job.setUser(true);
			job.addJobChangeListener(new JobChangeAdapter() 
			{
				@Override
				public void done(IJobChangeEvent event) 
				{
					if (event.getResult().equals(org.eclipse.core.runtime.Status.OK_STATUS))
					{
						Settings settings = TagReader.queryDefaultSettings();
				        ReportConfig report = settings.getReport();
				        report.setIncludeAntennaPortNumber(true);
				        report.setIncludeLastSeenTime(true);
				        report.setIncludeFirstSeenTime(true);
				        report.setIncludeSeenCount(true);
				        report.setMode(ReportMode.Individual);
				        settings.setReaderMode(ReaderMode.AutoSetDenseReader);
				        try 
				        {
							TagReader.applySettings(settings);
						} 
				        catch (OctaneSdkException e) 
				        {
							e.printStackTrace();
						}
					}
				}

			});
			job.addJobChangeListener(connectingListener);
			job.schedule();
		}
	}
	
	public static void disconnect()
	{
		if (connected)
		{
			if (started)
			{
				stop();
			}
			reader.disconnect();
			connected = false;
		}
	}
	
	public static void start()
	{
		if (connected)
		{
			try 
			{
				reader.start();
				tagReportMediator.startListeners();
				started = true;
			} 
			catch (OctaneSdkException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void stop()
	{
		if (connected)
		{
			try 
			{
				reader.stop();
				tagReportMediator.stopListeners();
				started = false;
			} 
			catch (OctaneSdkException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void setMessageTimeout(int timeout)
	{
		reader.setMessageTimeout(timeout);
	}
	
	public static void setAntennaChangeListener(AntennaChangeListener listener)
	{
		reader.setAntennaChangeListener(listener);
	}
	
	public static void setBufferOverflowListener(BufferOverflowListener listener)
	{
		reader.setBufferOverflowListener(listener);
	}
	
	public static void setBufferWarningListener(BufferWarningListener listener)
	{
		reader.setBufferWarningListener(listener);
	}
	
	public static void setConnectionAttemptListener(ConnectionAttemptListener listener)
	{
		reader.setConnectionAttemptListener(listener);
	}
	
	public static void setConnectionCloseListener(ConnectionCloseListener listener)
	{
		reader.setConnectionCloseListener(listener);
	}

	public static void setConnectionLostListener(ConnectionLostListener listener)
	{
		reader.setConnectionLostListener(listener);
	}
	
	public static void setDiagnosticsReportListener(DiagnosticsReportListener listener)
	{
		reader.setDiagnosticsReportListener(listener);
	}
	
	public static void setDirectionReportListener(DirectionReportListener listener)
	{
		reader.setDirectionReportListener(listener);
	}
	
	public static void setReaderStartListener(ReaderStartListener listener)
	{
		reader.setReaderStartListener(listener);
	}

	public static void setReaderStopListener(ReaderStopListener listener)
	{
		reader.setReaderStopListener(listener);
	}
	
	public static void addTagReportListener(StartableTagReportListener listener)
	{
		tagReportMediator.addTagReportListener(listener);
	}
	
	public static void removeTagReportListener(StartableTagReportListener listener)
	{
		tagReportMediator.removeTagReportListener(listener);
	}
	
	public static void setTagOpCompleteListener(TagOpCompleteListener listener)
	{
		reader.setTagOpCompleteListener(listener);
	}

	public static Settings queryDefaultSettings()
	{
		return reader.queryDefaultSettings();
	}
	
	public static Settings querySettings()
	{
		try 
		{
			return reader.querySettings();
		} 
		catch (OctaneSdkException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static Status queryStatus()
	{
		try 
		{
			return reader.queryStatus();
		} 
		catch (OctaneSdkException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static void applySettings(Settings settings) throws OctaneSdkException
	{
		reader.applySettings(settings);
	}

	static class TagReportMediator implements TagReportListener
	{
		private List<StartableTagReportListener> tagReportListeners = new ArrayList<StartableTagReportListener>();
		
		public void addTagReportListener(StartableTagReportListener listener)
		{
			if (!this.tagReportListeners.contains(listener))
			{
				this.tagReportListeners.add(listener);
			}
		}
		
		public void removeTagReportListener(StartableTagReportListener listener)
		{
			if (this.tagReportListeners.contains(listener))
			{
				this.tagReportListeners.remove(listener);
			}
		}

		public void startListeners()
		{
			for (StartableTagReportListener listener : tagReportListeners)
			{
				if (listener.isActive())
				{
					listener.start();
				}
			}
		}
		
		public void stopListeners()
		{
			for (StartableTagReportListener listener : tagReportListeners)
			{
				listener.stop();
			}
		}
		
		@Override
		public void onTagReported(ImpinjReader reader, TagReport report) 
		{
			List<Tag> tags = report.getTags();
			for (Tag tag : tags)
			{
				StartableTagReportListener[] listeners = this.tagReportListeners.toArray(new StartableTagReportListener[0]);
				for (StartableTagReportListener listener : listeners)
				{
					if (listener.isActive())
					{
						listener.process(tag);
					}
				}
			}
		}
	}
}
