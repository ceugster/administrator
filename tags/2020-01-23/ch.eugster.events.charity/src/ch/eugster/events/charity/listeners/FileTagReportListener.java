package ch.eugster.events.charity.listeners;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import com.impinj.octane.Tag;

public class FileTagReportListener extends AbstractStartableTagReportListener
{
	private PrintWriter writer;

	private String line;

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss.SSS");

	private NumberFormat numberFormat = NumberFormat.getIntegerInstance();
	
	public String label()
	{
		return "Zieldatei aktiviert";
	}
	
	public void starting()
	{
		File file = new File(System.getProperty("user.home") + File.separator + ".administrator" + File.separator + "sponsorlauf" + File.separator + "run_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(GregorianCalendar.getInstance().getTime()) + ".log");
		if (!file.isFile())
		{
			file.getParentFile().mkdirs();
			try 
			{
				this.writer = new PrintWriter(new FileOutputStream(file));
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public void stopping()
	{
		if (this.writer != null)
		{
			this.writer.flush();
			this.writer.close();
		}
	}
	
	public void process(Tag tag) 
	{
		short antennaPort = tag.getAntennaPortNumber();
		String tagData = tag.getEpc().toHexString();
		int count = tag.getTagSeenCount();
		line = new String(Short.valueOf(antennaPort) + "\t" + tagData + "\t" + dateFormat.format(tag.getFirstSeenTime().getLocalDateTime()) + "\t" + dateFormat.format(tag.getLastSeenTime().getLocalDateTime()) + "\t" + numberFormat.format(count));
		writer.println(line);
	}
}
