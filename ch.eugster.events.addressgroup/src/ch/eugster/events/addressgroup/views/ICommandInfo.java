package ch.eugster.events.addressgroup.views;

import java.util.List;
import java.util.Map;

public interface ICommandInfo
{
	String getCommandId();

	Map getRefreshFilter();

	List<String> getAvailableStateIds();
}
