/*******************************************************************************
 * Copyright (c) 2017 Christian Eugster.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Christian Eugster - initial API and implementation
 *******************************************************************************/

package ch.eugster.events.donation.views;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.TableItem;

import ch.eugster.events.persistence.model.DonationPurpose;
import ch.eugster.events.persistence.queries.DonationPurposeQuery;
import ch.eugster.events.persistence.service.ConnectionService;

/**
 * @author christian
 *
 */
public class DonationPurposeViewerDropListener extends ViewerDropAdapter
{
	private final ConnectionService connectionService;
	
	private DonationPurpose target;
	
	/**
	 * @param viewer
	 */
	public DonationPurposeViewerDropListener(final TableViewer tableViewer, final ConnectionService connectionService)
	{
		super(tableViewer);
		this.connectionService = connectionService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	@Override
	public boolean performDrop(final Object data)
	{
		if (this.connectionService != null)
		{
			if (this.target instanceof DonationPurpose)
			{
				if (data instanceof IStructuredSelection && !((IStructuredSelection) data).isEmpty())
				{
					final DonationPurposeQuery query = (DonationPurposeQuery) this.connectionService.getQuery(DonationPurpose.class);
					final IStructuredSelection ssel = (IStructuredSelection) data;
					final DonationPurpose movedPurpose = (DonationPurpose) ssel.getFirstElement();
					final TableItem[] tableItems = ((TableViewer) this.getViewer()).getTable().getItems();
					int i = 0;
					for (final TableItem tableItem : tableItems)
					{
						if (!tableItem.getData().equals(movedPurpose))
						{
							if (tableItem.getData().equals(this.target))
							{
								if (this.getCurrentLocation() == ViewerDropAdapter.LOCATION_BEFORE)
								{
									movedPurpose.setOrder(i++);
									query.merge(movedPurpose);
									this.target.setOrder(i++);
									tableItem.setData(query.merge(this.target));
								}
								else if (this.getCurrentLocation() == ViewerDropAdapter.LOCATION_AFTER)
								{
									this.target.setOrder(i++);
									tableItem.setData(query.merge(this.target));
									movedPurpose.setOrder(i++);
									query.merge(movedPurpose);
								}
							}
							else
							{
								((DonationPurpose) tableItem.getData()).setOrder(Double.valueOf(i++).doubleValue());
								tableItem.setData(query.merge((DonationPurpose) tableItem.getData()));
							}
						}
					}
					this.getViewer().refresh();
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@Override
	public boolean validateDrop(final Object target, final int operation, final TransferData transferType)
	{
		if (this.getCurrentLocation() == ViewerDropAdapter.LOCATION_BEFORE || this.getCurrentLocation() == ViewerDropAdapter.LOCATION_AFTER)
		{
			if (target instanceof DonationPurpose)
			{
				this.target = (DonationPurpose) target;
				if (operation == DND.DROP_MOVE)
				{
					if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType))
					{
						if (this.getSelectedObject() instanceof DonationPurpose)
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}
