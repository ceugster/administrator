package ch.eugster.events.report.engine;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface ReportService
{
	void export(URL report, final Comparable<?>[] beanArray, final Map<String, Object> parameters, Format format,
			File file) throws IllegalArgumentException;

	void print(URL report, Comparable<?>[] beanArray, Map<String, Object> parameters, boolean doNotShowPrintDialog)
			throws IllegalArgumentException;

	void processLabels(final Comparable<?>[] beanArray, Map<String, Object> parameters, Destination[] destinations);

	void view(final URL report, final Comparable<?>[] beanArray, final Map<String, Object> parameters)
			throws IllegalArgumentException;

	List<String> getLabelFormats();
	
	public enum Destination
	{
		PREVIEW, PRINTER, EXPORT;

		public String label()
		{
			switch (this)
			{
				case PREVIEW:
				{
					return "Vorschau";
				}
				case PRINTER:
				{
					return "Drucker";
				}
				case EXPORT:
				{
					return "Export";
				}
				default:
				{
					throw new RuntimeException("Invalid destination");
				}
			}
		}
	}

	public enum Format
	{
		PDF, HTML, XML;

		public String extension()
		{
			switch (this)
			{
				case PDF:
				{
					return ".pdf";
				}
				case HTML:
				{
					return ".html";
				}
				case XML:
				{
					return ".xml";
				}
				default:
				{
					throw new RuntimeException("Invalid format");
				}
			}
		}

		public String label()
		{
			switch (this)
			{
				case PDF:
				{
					return "Portable Document Format (PDF)";
				}
				case HTML:
				{
					return "Hypertext Markup Language (HTML)";
				}
				case XML:
				{
					return "Extensible Markup Language (XML)";
				}
				default:
				{
					throw new RuntimeException("Invalid format");
				}
			}
		}

		public static String[] extensions()
		{
			String[] extensions = new String[Format.values().length];
			for (int i = 0; i < Format.values().length; i++)
			{
				extensions[i] = "*" + Format.values()[i].extension();
			}
			return extensions;
		}

		public static Format format(final String filename)
		{
			Format[] formats = Format.values();
			for (Format format : formats)
			{
				if (filename.endsWith(format.extension()))
				{
					return format;
				}
			}
			return null;
		}
	}
}
