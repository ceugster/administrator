/*
 * Created on 18.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;

import ch.eugster.events.ui.editors.AbstractEntityFormEditor;

@SuppressWarnings("rawtypes")
public class ResetFormEditorActionDelegate<T extends AbstractEntityFormEditor> implements IEditorActionDelegate,
		IPropertyListener
{
	protected T editorPart;

	protected IAction action;

	@SuppressWarnings("unchecked")
	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor)
	{
		if (targetEditor instanceof AbstractEntityFormEditor<?>)
		{
			this.action = action;
			this.editorPart = (T) targetEditor;
			this.editorPart.addPropertyListener(this);
			action.setEnabled(editorPart.isDirty());
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection)
	{
	}

	@Override
	public void run(IAction action)
	{
		if (editorPart.isDirty())
		{
			editorPart.reset(true);
		}
	}

	@Override
	public void propertyChanged(Object source, int propId)
	{
		if (source instanceof AbstractEntityFormEditor<?>)
		{
			action.setEnabled(editorPart.isDirty());
		}
	}
}
