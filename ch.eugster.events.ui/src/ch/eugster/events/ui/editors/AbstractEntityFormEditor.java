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
		IPropertyListener
{
	protected abstract boolean validate();

	protected abstract void saveValues();

	public abstract void setDirty(boolean dirty);

	public void reset(boolean ask)
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

	protected abstract void reset();

	@Override
	public void doSave(IProgressMonitor monitor)
	{
		if (this.validate())
		{
			this.saveValues();

			@SuppressWarnings("unchecked")
			AbstractEntityEditorInput<T> input = (AbstractEntityEditorInput<T>) this.getEditorInput();
			T entity = input.getEntity();

			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			tracker.open();
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				@SuppressWarnings("unchecked")
				AbstractEntityQuery<T> query = (AbstractEntityQuery<T>) service.getQuery(entity.getClass());
				input.setEntity(query.merge(entity));
				setDirty(false);
				// updatePages();
				// updateContentOutlinePage();
				if (input.hasParent())
				{
					AbstractEntityQuery<? extends AbstractEntity> parentQuery = service.getQuery(input.getParent()
							.getClass());
					parentQuery.refresh(input.getParent());
				}
			}
			tracker.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.forms.editor.FormEditor#createToolkit(org.eclipse.swt.
	 * widgets.Display)
	 */
	@Override
	protected FormToolkit createToolkit(Display display)
	{
		return new FormToolkit(Activator.getDefault().getFormColors(display));
	}

	@Override
	public void propertyChanged(Object source, int propId)
	{
		if (propId == PROP_DIRTY)
		{
			this.firePropertyChange(IEditorPart.PROP_DIRTY);
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
}