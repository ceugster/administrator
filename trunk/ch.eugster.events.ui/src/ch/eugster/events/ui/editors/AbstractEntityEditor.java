/*
 * Created on 18.12.2008
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ch.eugster.events.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.progress.UIJob;
import org.osgi.util.tracker.ServiceTracker;

import ch.eugster.events.persistence.events.EntityListener;
import ch.eugster.events.persistence.exceptions.PersistenceException;
import ch.eugster.events.persistence.model.AbstractEntity;
import ch.eugster.events.persistence.model.DirtyMarkable;
import ch.eugster.events.persistence.model.User;
import ch.eugster.events.persistence.queries.AbstractEntityQuery;
import ch.eugster.events.persistence.service.ConnectionService;
import ch.eugster.events.ui.Activator;
import ch.eugster.events.ui.dialogs.Message;
import ch.eugster.events.ui.views.IEntityEditorContentOutlinePage;

public abstract class AbstractEntityEditor<T extends AbstractEntity> extends EditorPart implements IPropertyListener,
		EntityListener, DirtyMarkable
{
	protected ColorManager colorManager;

	protected FormToolkit formToolkit;

	protected Color normal;

	protected Color error;

	protected ScrolledForm scrolledForm;

	private boolean dirty;

	protected IEntityEditorContentOutlinePage contentOutlinePage;

	public AbstractEntityEditor()
	{
		super();
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		this.formToolkit = new FormToolkit(parent.getDisplay());
		this.colorManager = new ColorManager();

		this.normal = this.colorManager.getColor(new RGB(255, 255, 255));
		this.error = this.colorManager.getColor(new RGB(247, 196, 145));

		ColumnLayout columnLayout = new ColumnLayout();
		columnLayout.maxNumColumns = User.getCurrent().getMaxEditorColumns();
		columnLayout.minNumColumns = User.getCurrent().getMinEditorColumns();

		this.scrolledForm = this.formToolkit.createScrolledForm(parent);
		this.scrolledForm.getBody().setLayout(columnLayout);
		this.scrolledForm.setText(this.getText());

		ColumnLayoutData layoutData = new ColumnLayoutData();
		layoutData.widthHint = 200;
		TableWrapLayout sectionLayout = new TableWrapLayout();
		sectionLayout.numColumns = 1;

		this.createSections(this.scrolledForm);

		this.loadValues();
	}

	protected abstract void createSections(ScrolledForm parent);

	@SuppressWarnings({ "unchecked" })
	@Override
	public void doSave(final IProgressMonitor monitor)
	{
		if (this.validate())
		{
			this.saveValues();
			if (this.contentOutlinePage != null)
			{
				this.contentOutlinePage.update();
			}
			AbstractEntityEditorInput<T> input = (AbstractEntityEditorInput<T>) this.getEditorInput();
			T entity = input.getEntity();

			ServiceTracker tracker = new ServiceTracker(Activator.getDefault().getBundle().getBundleContext(),
					ConnectionService.class.getName(), null);
			tracker.open();
			ConnectionService service = (ConnectionService) tracker.getService();
			if (service != null)
			{
				AbstractEntityQuery<T> query = (AbstractEntityQuery<T>) service.getQuery(entity.getClass());
				input.setEntity(query.merge(entity));
				if (input.hasParent())
				{
					AbstractEntityQuery<? extends AbstractEntity> parentQuery = service.getQuery(input.getParent()
							.getClass());
					parentQuery.refresh(input.getParent());
				}
				this.setDirty(false);
				this.scrolledForm.setText(this.getText());
				this.updateControls();
			}
			tracker.close();
		}
	}

	@Override
	public void doSaveAs()
	{
	}

	protected abstract Message getMessage(PersistenceException.ErrorCode errorCode);

	protected abstract String getName();

	protected abstract String getText();

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException
	{
		this.setInput(input);
		this.setSite(site);
		this.setPartName(this.getName());
		this.initialize();
	}

	protected abstract void initialize();

	@Override
	public boolean isDirty()
	{
		return this.dirty;
	}

	@Override
	public boolean isSaveAsAllowed()
	{
		return false;
	}

	@Override
	public boolean isSaveOnCloseNeeded()
	{
		return this.isDirty();
	}

	protected abstract void loadValues();

	@SuppressWarnings("unchecked")
	@Override
	public void postDelete(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				AbstractEntityEditorInput<T> input = (AbstractEntityEditorInput<T>) getEditorInput();
				T edited = input.getEntity();
				if (edited.getId() != null)
				{
					if (entity.getId().equals(edited.getId()))
						getSite().getWorkbenchWindow().getActivePage().closeEditor(AbstractEntityEditor.this, false);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void postLoad(final AbstractEntity entity)
	{
	}

	@Override
	public void postPersist(final AbstractEntity entity)
	{
	}

	@SuppressWarnings("unchecked")
	@Override
	public void postRemove(final AbstractEntity entity)
	{
		UIJob job = new UIJob("")
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				AbstractEntityEditorInput<T> input = (AbstractEntityEditorInput<T>) getEditorInput();
				T edited = input.getEntity();
				if (edited.getId() != null)
				{
					if (entity.getId().equals(edited.getId()))
						getSite().getWorkbenchWindow().getActivePage().closeEditor(AbstractEntityEditor.this, false);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	@Override
	public void postUpdate(final AbstractEntity entity)
	{
	}

	@Override
	public void preDelete(final AbstractEntity entity)
	{
	}

	@Override
	public void prePersist(final AbstractEntity entity)
	{
	}

	@Override
	public void preRemove(final AbstractEntity entity)
	{
	}

	@Override
	public void preUpdate(final AbstractEntity entity)
	{
	}

	@Override
	public void propertyChanged(final Object source, final int propId)
	{
	}

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
				this.scrolledForm.setText(this.getText());
				this.loadValues();
				this.setDirty(false);
			}
		}
	}

	protected abstract void saveValues();

	@Override
	public void setDirty(final boolean dirty)
	{
		if (this.dirty == dirty)
			return;

		this.dirty = dirty;
		this.firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	public void setFocus(final Control control)
	{
		control.setFocus();
	}

	protected int showMessage(final String title, final Image image, final String message, final int dialogType,
			final String[] buttonLabels, final int defaultButton)
	{
		MessageDialog dialog = new MessageDialog(this.getEditorSite().getShell(), title, image, message, dialogType,
				buttonLabels, defaultButton);
		return dialog.open();
	}

	protected int showWarningMessage(final Message msg)
	{
		int result = this.showMessage(msg.getTitle(), Dialog.getImage(Dialog.DLG_IMG_MESSAGE_WARNING),
				msg.getMessage(), MessageDialog.WARNING, new String[] { "OK" }, 0);
		this.setFocus(msg.getControl());
		return result;
	}

	protected int showWarningMessage(final String title, final String message, final Control control)
	{
		int result = this.showMessage(title, Dialog.getImage(Dialog.DLG_IMG_MESSAGE_WARNING), message,
				MessageDialog.WARNING, new String[] { "OK" }, 0);
		this.setFocus(control);
		return result;
	}

	protected void updateControls()
	{
		this.setPartName(this.getName());
	}

	protected abstract boolean validate();

	protected abstract boolean validateType(AbstractEntityEditorInput<T> input);

}