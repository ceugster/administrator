package ch.eugster.events.addressgroup.views;

import java.util.Collection;
import java.util.Map;

public interface ICommandInfo
{
	String getCommandId();

	Map getRefreshFilter();

	Collection<String> getAvailableStateIds();
}
