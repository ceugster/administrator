/*
 * Created on 18.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.queries.AbstractEntityQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.Activator;

public abstract class AbstractEntityFormEditor<T extends AbstractEntity> extends FormEditor implements
		IPropertyListener, Saveable, Clearable
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.forms.editor.FormEditor#createToolkit(org.eclipse.swt.
	 * widgets.Display)
	 */
	@Override
	protected FormToolkit createToolkit(final Display display)
	{
		return new FormToolkit(Activator.getDefault().getFormColors(display));
	}

	@Override
	public void doSave(final IProgressMonitor monitor)
	{
		if (this.validate())
		{
			this.saveValues();
			this.clearDirty();

			@SuppressWarnings("unchecked")
			AbstractEntityEditorInput<T> input = (AbstractEntityEditorInput<T>) this.getEditorInput();
			T entity = input.getEntity();

			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			try
			{
				tracker.open();
				ConnectionService service = (ConnectionService) tracker.getService();
				if (service != null)
				{
					@SuppressWarnings("unchecked")
					AbstractEntityQuery<T> query = (AbstractEntityQuery<T>) service.getQuery(entity.getClass());
					input.setEntity(query.merge(entity));
					clearDirty();
					updateControls();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				clearDirty();
				tracker.close();
			}
		}
	}

	@Override
	public void doSaveAs()
	{
	}

	@Override
	public boolean isSaveAsAllowed()
	{
		return false;
	}

	@Override
	public void propertyChanged(final Object source, final int propId)
	{
		if (propId == PROP_DIRTY)
		{
			this.firePropertyChange(IEditorPart.PROP_DIRTY);
		}
	}

	protected abstract void reset();

	public void reset(final boolean ask)
	{
		if (this.isDirty())
		{
			boolean reset = true;
			if (ask)
			{
				reset = MessageDialog.openQuestion(this.getEditorSite().getShell(), "Änderungen verwerfen",
						"Sollen die Änderungen verworfen werden?");
			}
			if (reset)
			{
				reset();
			}
		}
	}

	protected abstract void updateControls();

	protected abstract boolean validate();
}