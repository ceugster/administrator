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
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;

/**
 * @author christian
 *
 */
public class DonationPurposeViewerDragListener extends DragSourceAdapter
{
	private final TableViewer tableViewer;

	public DonationPurposeViewerDragListener(final TableViewer tableViewer)
	{
		this.tableViewer = tableViewer;
	}

	@Override
	public void dragSetData(final DragSourceEvent event)
	{
		final IStructuredSelection ssel = (IStructuredSelection) this.tableViewer.getSelection();
		if (ssel.size() == 1)
		{
			LocalSelectionTransfer.getTransfer().setSelection(this.tableViewer.getSelection());
		}
	}

}
