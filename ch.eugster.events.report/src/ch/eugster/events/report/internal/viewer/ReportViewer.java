/*
 * SWTJasperViewer - Free SWT/JFace report viewer for JasperReports.
 * Copyright (C) 2004  Peter Severin (peter_p_s@users.sourceforge.net)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 */
package ch.eugster.events.report.internal.viewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRPrintXmlLoader;
import net.sf.jasperreports.view.JRHyperlinkListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * SWT based report viewer implementation.
 * 
 * @author Peter Severin (peter_p_s@users.sourceforge.net)
 */
public class ReportViewer implements IReportViewer
{

	private static final double[] DEFAULT_ZOOM_LEVELS = new double[] { 0.5f, 0.75f, 1.0f, 1.25f, 1.50f, 1.75f, 2.0f };

	private EventListenerList listenerList = new EventListenerList();

	private JasperPrint document;

	private String reason;

	private double zoom = 1.0f;

	private double[] zoomLevels = DEFAULT_ZOOM_LEVELS;

	private int zoomMode = ZOOM_MODE_NONE;

	private int pageIndex;

	private String fileName;

	private boolean xml;

	private int style;

	private ViewerCanvas viewerComposite;

	@SuppressWarnings("rawtypes")
	private List hyperlinkListeners;

	/**
	 * Default constructor. The default style will be used for the SWT control
	 * associated to the viewer.
	 */
	public ReportViewer()
	{
		this(SWT.NONE);
	}

	/**
	 * Constructor that allows to specify a SWT control style. For possible
	 * styles see the {@link org.eclipse.swt.widgets.Canvas} class. Most
	 * frequently you will wont to specify the <code>SWT.NONE<code> style.
	 * 
	 * @param style
	 *            the style
	 */
	public ReportViewer(final int style)
	{
		this.style = style;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#addHyperlinkListener(net.sf.jasperreports.view.JRHyperlinkListener)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addHyperlinkListener(final JRHyperlinkListener listener)
	{
		if (hyperlinkListeners == null)
		{
			hyperlinkListeners = new ArrayList();
		}
		else
		{
			hyperlinkListeners.remove(listener); // add once
		}

		hyperlinkListeners.add(listener);
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#addReportViewerListener(ch.eugster.events.report.internal.viewer.IReportViewerListener)
	 */
	@Override
	public void addReportViewerListener(final IReportViewerListener listener)
	{
		listenerList.add(IReportViewerListener.class, listener);
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#canChangeZoom()
	 */
	@Override
	public boolean canChangeZoom()
	{
		return hasDocument();
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#canGotoFirstPage()
	 */
	@Override
	public boolean canGotoFirstPage()
	{
		return hasDocument() && pageIndex > 0;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#canGotoLastPage()
	 */
	@Override
	public boolean canGotoLastPage()
	{
		return hasDocument() && pageIndex < getPageCount() - 1;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#canGotoNextPage()
	 */
	@Override
	public boolean canGotoNextPage()
	{
		return hasDocument() && pageIndex < getPageCount() - 1;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#canGotoPreviousPage()
	 */
	@Override
	public boolean canGotoPreviousPage()
	{
		return hasDocument() && pageIndex > 0;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#canReload()
	 */
	@Override
	public boolean canReload()
	{
		return fileName != null;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#canZoomIn()
	 */
	@Override
	public boolean canZoomIn()
	{
		return hasDocument() && getZoom() < getMaxZoom();
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#canZoomOut()
	 */
	@Override
	public boolean canZoomOut()
	{
		return hasDocument() && getZoom() > getMinZoom();
	}

	private double computeZoom()
	{
		switch (zoomMode)
		{
			case ZOOM_MODE_ACTUAL_SIZE:
				return 1.0;
			case ZOOM_MODE_FIT_WIDTH:
			{
				double ratio = ratio(viewerComposite.getFitSize().x, document.getPageWidth());
				return ratio(
						viewerComposite.getFitSize((int) (document.getPageWidth() * ratio),
								(int) (document.getPageHeight() * ratio)).x, document.getPageWidth());
			}
			case ZOOM_MODE_FIT_HEIGHT:
			{
				double ratio = ratio(viewerComposite.getFitSize().y, document.getPageHeight());
				return ratio(
						viewerComposite.getFitSize((int) (document.getPageWidth() * ratio),
								(int) (document.getPageHeight() * ratio)).y, document.getPageHeight());
			}
			case ZOOM_MODE_FIT_PAGE:
				Point fitSize = viewerComposite.getFitSize();
				return Math.min(ratio(fitSize.x, document.getPageWidth()), ratio(fitSize.y, document.getPageHeight()));
		}

		return zoom;
	}

	/**
	 * Creates the SWT control for the report viewer. Later calls to this method
	 * will return the same instance of the control.
	 * 
	 * @param parent
	 *            the parent
	 * @return the created control
	 */
	public Control createControl(final Composite parent)
	{
		if (viewerComposite == null)
		{
			viewerComposite = new ViewerCanvas(parent, style)
			{
				/**
				 * @see ch.eugster.events.report.internal.viewer.ViewerCanvas#resize()
				 */
				@Override
				protected void resize()
				{
					setZoom(computeZoom(), true);
					super.resize();
				}
			};
			viewerComposite.setReportViewer(this);
		}

		return viewerComposite;
	}

	private void fireViewerModelChanged()
	{
		Object[] listeners = listenerList.getListenerList();
		ReportViewerEvent e = null;

		for (int i = listeners.length - 2; i >= 0; i -= 2)
		{
			if (listeners[i] == IReportViewerListener.class)
			{
				if (e == null)
				{
					e = new ReportViewerEvent(this);
				}
				((IReportViewerListener) listeners[i + 1]).viewerStateChanged(e);
			}
		}
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#getDocument()
	 */
	@Override
	public JasperPrint getDocument()
	{
		return document;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#getHyperlinkListeners()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JRHyperlinkListener[] getHyperlinkListeners()
	{
		return hyperlinkListeners == null ? new JRHyperlinkListener[0] : (JRHyperlinkListener[]) hyperlinkListeners
				.toArray(new JRHyperlinkListener[hyperlinkListeners.size()]);
	}

	private double getMaxZoom()
	{
		return zoomLevels[zoomLevels.length - 1];
	}

	private double getMinZoom()
	{
		return zoomLevels[0];
	}

	private double getNextZoom()
	{
		for (int i = 0; i < zoomLevels.length; i++)
		{
			if (zoom < zoomLevels[i])
				return zoomLevels[i];
		}

		return getMaxZoom();
	}

	private int getPageCount()
	{
		return document == null ? 0 : document.getPages().size();
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#getPageIndex()
	 */
	@Override
	public int getPageIndex()
	{
		return pageIndex;
	}

	private double getPreviousZoom()
	{
		for (int i = zoomLevels.length - 1; i >= 0; i--)
		{
			if (zoom > zoomLevels[i])
				return zoomLevels[i];
		}

		return getMinZoom();
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#getReason()
	 */
	@Override
	public String getReason()
	{
		return reason;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#getZoom()
	 */
	@Override
	public double getZoom()
	{
		return zoom;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#getZoomLevels()
	 */
	@Override
	public double[] getZoomLevels()
	{
		return zoomLevels;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#getZoomMode()
	 */
	@Override
	public int getZoomMode()
	{
		return zoomMode;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#gotoFirstPage()
	 */
	@Override
	public void gotoFirstPage()
	{
		if (canGotoFirstPage())
		{
			setPageIndex(0);
		}
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#gotoLastPage()
	 */
	@Override
	public void gotoLastPage()
	{
		if (canGotoLastPage())
		{
			setPageIndex(getPageCount() - 1);
		}
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#gotoNextPage()
	 */
	@Override
	public void gotoNextPage()
	{
		if (canGotoNextPage())
		{
			setPageIndex(pageIndex + 1);
		}
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#gotoPreviousPage()
	 */
	@Override
	public void gotoPreviousPage()
	{
		if (canGotoPreviousPage())
		{
			setPageIndex(pageIndex - 1);
		}
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#hasDocument()
	 */
	@Override
	public boolean hasDocument()
	{
		return getDocument() != null;
	}

	private JasperPrint load() throws JRException
	{
		JasperPrint jasperPrint = null;

		if (xml)
		{
			jasperPrint = JRPrintXmlLoader.load(fileName);
		}
		else
		{
			jasperPrint = (JasperPrint) JRLoader.loadObject(new File(fileName));
		}

		return jasperPrint;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#loadDocument(java.lang.String,
	 *      boolean)
	 */
	@Override
	public void loadDocument(final String fileName, final boolean xml)
	{
		this.fileName = fileName;
		this.xml = xml;
		reload();
	}

	private double ratio(final int a, final int b)
	{
		return (a * 100 / b) / 100.0;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#reload()
	 */
	@Override
	public void reload()
	{
		try
		{
			pageIndex = 0;
			// zoom = 1.0f;
			// zoomMode = ZOOM_MODE_NONE;
			setDocument(load());
		}
		catch (JRException e)
		{
			unsetDocument(e.toString());
		}
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#removeHyperlinkListener(net.sf.jasperreports.view.JRHyperlinkListener)
	 */
	@Override
	public void removeHyperlinkListener(final JRHyperlinkListener listener)
	{
		if (hyperlinkListeners != null)
			hyperlinkListeners.remove(listener);
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#removeReportViewerListener(ch.eugster.events.report.internal.viewer.IReportViewerListener)
	 */
	@Override
	public void removeReportViewerListener(final IReportViewerListener listener)
	{
		listenerList.remove(IReportViewerListener.class, listener);
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#setDocument(net.sf.jasperreports.engine.JasperPrint)
	 */
	@Override
	public void setDocument(final JasperPrint document)
	{
		this.document = document;
		this.reason = null;
		this.pageIndex = Math.min(Math.max(0, pageIndex), getPageCount() - 1);
		setZoomInternal(computeZoom());
		fireViewerModelChanged();
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#setPageIndex(int)
	 */
	@Override
	public void setPageIndex(final int pageIndex)
	{
		if (pageIndex != getPageIndex())
		{
			this.pageIndex = Math.min(Math.max(0, pageIndex), getPageCount() - 1);
			fireViewerModelChanged();
		}
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#setZoom(double)
	 */
	@Override
	public void setZoom(final double zoom)
	{
		setZoom(zoom, false);
	}

	private void setZoom(final double zoom, final boolean keepMode)
	{
		if (!canChangeZoom())
			return;

		if (Math.abs(zoom - getZoom()) > 0.00001)
		{
			setZoomInternal(zoom);
			if (!keepMode)
				this.zoomMode = ZOOM_MODE_NONE;
			fireViewerModelChanged();
		}
	}

	private void setZoomInternal(final double zoom)
	{
		this.zoom = Math.min(Math.max(zoom, getMinZoom()), getMaxZoom());
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#setZoomLevels(double[])
	 */
	@Override
	public void setZoomLevels(final double[] levels)
	{
		this.zoomLevels = levels;
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#setZoomMode(int)
	 */
	@Override
	public void setZoomMode(final int zoomMode)
	{
		if (!canChangeZoom())
			return;

		if (zoomMode != getZoomMode())
		{
			this.zoomMode = zoomMode;
			setZoomInternal(computeZoom());
			fireViewerModelChanged();
		}
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#unsetDocument(java.lang.String)
	 */
	@Override
	public void unsetDocument(final String reason)
	{
		this.document = null;
		this.reason = reason;
		fireViewerModelChanged();
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#zoomIn()
	 */
	@Override
	public void zoomIn()
	{
		if (canZoomIn())
			setZoom(getNextZoom());
	}

	/**
	 * @see ch.eugster.events.report.internal.viewer.IReportViewer#zoomOut()
	 */
	@Override
	public void zoomOut()
	{
		if (canZoomOut())
			setZoom(getPreviousZoom());
	}
}
