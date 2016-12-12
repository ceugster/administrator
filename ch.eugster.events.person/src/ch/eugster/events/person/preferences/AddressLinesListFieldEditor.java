package ch.eugster.events.person.preferences;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.widgets.Composite;

public class AddressLinesListFieldEditor extends ListEditor
{
	private final String description;

	private final String[] variables;

	public AddressLinesListFieldEditor(String name, String label, String description, String[] variables,
			Composite parent)
	{
		super(name, label, parent);
		this.description = description;
		this.variables = variables;
		this.getRemoveButton().setText("Zeile entfernen");
		this.getAddButton().setText("Zeile hinzufügen");
		this.getDownButton().setText("Zeile abwärts");
		this.getUpButton().setText("Zeile aufwärts");
	}

	@Override
	protected String createList(String[] items)
	{
		StringBuilder addressFormat = new StringBuilder();
		for (int i = 0; i < items.length; i++)
		{
			if (i == 0)
				addressFormat = addressFormat.append(items[i]);
			else
				addressFormat = addressFormat.append("|" + items[i]);
		}
		return addressFormat.toString();
	}

	@Override
	protected String getNewInputObject()
	{
		InputDialog dialog = new InputDialog(this.getShell(), "Neue Zeile", description, "",
				new LabelInputValidator(variables));

		if (dialog.open() == 0)
			return dialog.getValue();
		return null;
	}

	@Override
	protected String[] parseString(String stringList)
	{
		if (stringList == null || stringList.isEmpty())
			return new String[0];

		String[] lines = stringList.split("[|]");
		return lines;
	}

}
