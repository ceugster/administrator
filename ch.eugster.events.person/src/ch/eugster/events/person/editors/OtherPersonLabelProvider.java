package ch.eugster.events.person.editors;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.formattedtext.MaskFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import ch.eugster.events.persistence.formatters.PersonFormatter;
import ch.eugster.events.persistence.model.LinkPersonAddress;

public class OtherPersonLabelProvider extends LabelProvider implements ITableLabelProvider
{
	private static final String[] columnNames = new String[] { "Id", "Nachname", "Vorname", "Mobile", "Telefon",
			"Email" };

	private static final int[] columnAlignments = new int[] { SWT.CENTER, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT,
			SWT.LEFT };

	public OtherPersonLabelProvider()
	{
	}

	public int[] getAlignments()
	{
		return OtherPersonLabelProvider.columnAlignments;
	}

	@Override
	public Image getColumnImage(final Object element, final int columnIndex)
	{
		return null;
	}

	public String[] getColumnNames()
	{
		return OtherPersonLabelProvider.columnNames;
	}

	@Override
	public String getColumnText(final Object element, final int columnIndex)
	{
		String text = null;
		if (element instanceof LinkPersonAddress)
		{
			LinkPersonAddress link = (LinkPersonAddress) element;
			switch (columnIndex)
			{
				case 0:
					text = PersonFormatter.getInstance().formatId(link.getPerson());
					break;
				case 1:
					text = link.getPerson().getLastname();
					break;
				case 2:
					text = link.getPerson().getFirstname();
					break;
				case 3:
					text = link.getPerson().getPhone();
					break;
				case 4:
				{
					if (link.getAddress().getCountry() == null
							|| link.getAddress().getCountry().getPhonePattern().isEmpty())
					{
						return link.getPhone().toString();
					}
					else
					{
						MaskFormatter formatter = new MaskFormatter(link.getAddress().getCountry().getPhonePattern());
						formatter.setValue(link.getPhone());
						text = formatter.getDisplayString();
					}
					break;
				}
				case 5:
					text = link.getPerson().getEmail();
					break;
			}
		}
		return text;
	}

}
