package ch.eugster.events.ui.dnd;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import ch.eugster.events.persistence.model.IEntity;

public class EntityTransfer extends ByteArrayTransfer
{
	// First attempt to create a UUID for the type name to make sure that
	// different Eclipse applications use different "types" of
	// <code>AddressGroupSelectionTransfer</code>
	public static final String TYPE_NAME = "person-selection-transfer-format" + (new Long(System.currentTimeMillis())).toString(); //$NON-NLS-1$;

	public static final int TYPEID = registerType(TYPE_NAME);

	private static final EntityTransfer INSTANCE = new EntityTransfer();

	private IEntity[] data;

	private int operation;

	private long selectionSetTime;

	/**
	 * Returns the singleton.
	 * 
	 * @return the singleton
	 */
	public static EntityTransfer getTransfer()
	{
		return INSTANCE;
	}

	/**
	 * Only the singleton instance of this class may be used.
	 */
	protected EntityTransfer()
	{
	}

	/**
	 * Returns the local transfer data.
	 * 
	 * @return the local transfer data
	 */
	public IEntity[] getData()
	{
		return this.data;
	}

	public int getOperation()
	{
		return this.operation;
	}

	public boolean isEmpty()
	{
		if (this.data == null)
			return true;
		return this.data.length == 0;
	}

	/**
	 * Tests whether native drop data matches this transfer type.
	 * 
	 * @param result
	 *            result of converting the native drop data to Java
	 * @return true if the native drop data does not match this transfer type.
	 *         false otherwise.
	 */
	private boolean isInvalidNativeType(Object result)
	{
		return !(result instanceof byte[]) || !TYPE_NAME.equals(new String((byte[]) result));
	}

	/**
	 * Returns the type id used to identify this transfer.
	 * 
	 * @return the type id used to identify this transfer.
	 */
	@Override
	protected int[] getTypeIds()
	{
		return new int[] { TYPEID };
	}

	/**
	 * Returns the type name used to identify this transfer.
	 * 
	 * @return the type name used to identify this transfer.
	 */
	@Override
	protected String[] getTypeNames()
	{
		return new String[] { TYPE_NAME };
	}

	/**
	 * Overrides org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(Object,
	 * TransferData). Only encode the transfer type name since the selection is
	 * read and written in the same process.
	 * 
	 * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object,
	 *      org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public void javaToNative(Object object, TransferData transferData)
	{
		byte[] check = TYPE_NAME.getBytes();
		super.javaToNative(check, transferData);
	}

	/**
	 * Overrides
	 * org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(TransferData). Test if
	 * the native drop data matches this transfer type.
	 * 
	 * @see org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(TransferData)
	 */
	@Override
	public Object nativeToJava(TransferData transferData)
	{
		Object result = super.nativeToJava(transferData);
		if (this.isInvalidNativeType(result))
		{
			Policy.getLog().log(
					new Status(IStatus.ERROR, Policy.JFACE, IStatus.ERROR, JFaceResources
							.getString("LocalSelectionTransfer.errorMessage"), null)); //$NON-NLS-1$
		}
		return this.data;
	}

	/**
	 * Sets the transfer data for local use.
	 * 
	 * @param s
	 *            the transfer data
	 */
	public void setData(int operation, IEntity[] entities)
	{
		this.operation = operation;
		this.data = entities;
	}

	/**
	 * Returns the time when the selection operation this transfer is associated
	 * with was started.
	 * 
	 * @return the time when the selection operation has started
	 * 
	 * @see org.eclipse.swt.events.TypedEvent#time
	 */
	public long getSelectionSetTime()
	{
		return this.selectionSetTime;
	}

	/**
	 * Sets the time when the selection operation this transfer is associated
	 * with was started. If assigning this from an SWT event, be sure to use
	 * <code>setSelectionTime(event.time & 0xFFFF)</code>
	 * 
	 * @param time
	 *            the time when the selection operation was started
	 * 
	 * @see org.eclipse.swt.events.TypedEvent#time
	 */
	public void setSelectionSetTime(long time)
	{
		this.selectionSetTime = time;
	}
}
