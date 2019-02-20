package ch.eugster.events.person.preferences;

import org.eclipse.jface.dialogs.IInputValidator;

public class LabelInputValidator implements IInputValidator
{
	private final String[] validVariables;

	public LabelInputValidator(String[] validVariables)
	{
		this.validVariables = validVariables;
	}

	@Override
	public String isValid(String value)
	{
		String[] variables = value.split(" ");
		for (String variable : variables)
		{
			String[] vars = variable.split("-");
			for (String var : vars)
			{
				if (var.startsWith("${"))
				{
					if (var.endsWith("}"))
					{
						for (String validVariable : validVariables)
						{
							if (validVariable.equals(var))
							{
								return null;
							}
						}
						return "Die Variable '" + var + "' ist ungültig.";
					}
					else
					{
						return "Variablen müssen mit '}' abgeschlossen werden.";
					}
				}
				else
				{
					return null;
				}
			}
		}
		return null;
	}

}
