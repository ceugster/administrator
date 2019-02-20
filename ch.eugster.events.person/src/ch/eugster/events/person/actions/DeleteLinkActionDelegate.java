/*
 * Created on 18.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.person.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;

public class DeleteLinkActionDelegate<FullPersonEditor> implements IEditorActionDelegate, IPropertyListener
{
	protected IEditorPart editorPart;

	protected IAction action;

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor)
	{
		this.action = action;
		this.editorPart = targetEditor;
		this.editorPart.addPropertyListener(this);
		action.setEnabled(editorPart.isDirty());
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection)
	{
	}

	@Override
	public void run(IAction action)
	{
	}

	@Override
	public void propertyChanged(Object source, int propId)
	{
	}
}
