/******************************************************************************
 * Copyright (c) 2014 SAP and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP - initial implementation
 *    Shenxue Zhou - adaptation for Sapphire and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.utils;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.handles.AbstractHandle;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.figures.FigureUtil;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * A rectangular handle, which completely surrounds the owner edit-part. It
 * serves as selection highlighting and can also be used to move the owner
 * edit-part.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 *
 * @author SAP
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * 
 */

public class SapphireSurroundingHandle extends AbstractHandle 
{
	private int lineWidth = 1;
	
	private int lineOffset = 0;
	
	private int lineStyle = SWT.LINE_DASH;
	
	private Color lineColor;

	/**
	 * The resource cache, which can be used to access the environment.
	 */
	private DiagramResourceCache resourceCache;

	private DiagramConfigurationManager configManager;

	/**
	 * Indicates, if moving the owner edit-part via this handle is supported.
	 */
	private boolean movable;
	

	/**
	 * Creates a new GFSurroundingHandle.
	 * 
	 * @param owner
	 *            The owner editpart associated with this handle.
	 * @param configurationProvider
	 *            The configuration provider, which can be used to access the
	 *            environment.
	 * @param supportedResizeDirections
	 *            The supported resize directions (see
	 *            {@link org.eclipse.gef.editpolicies.ResizableEditPolicy#getResizeDirections()})
	 * @param movable
	 *            Indicates, if moving the owner edit-part via this handle is
	 *            supported.
	 */
	public SapphireSurroundingHandle(GraphicalEditPart owner, DiagramConfigurationManager configManager, 
			DiagramResourceCache resourceCache, boolean movable) 
	{
		this.resourceCache = resourceCache;
		this.configManager = configManager;
		this.movable = movable;

		setOwner(owner);
		int lineWidth = getLineWidth();
		setLocator(new ZoomingInsetsHandleLocator(owner.getFigure(), 
				new Insets(lineWidth, lineWidth, lineWidth, lineWidth)));

		setOpaque(false);
		
		this.lineColor = this.resourceCache.getOutlineColor();

		if (movable) 
		{
			setCursor(Cursors.SIZEALL);
		} 
		else 
		{
			setCursor(null);
		}
	}

	/**
	 * Overridden to create a {@link DragEditPartsTracker}, if moving is
	 * supported.
	 */
	@Override
	protected DragTracker createDragTracker() 
	{
		if (movable) 
		{
			DragEditPartsTracker tracker = new DragEditPartsTracker(getOwner());
			tracker.setDefaultCursor(getCursor());
			return tracker;
		} 
		else 
		{
			return null;
		}
	}

	/**
	 * Returns <code>true</code> if the point (x,y) is contained within this
	 * handle. This means, that the point is on the outline of the handle, not
	 * inside the handle.
	 * 
	 * @return <code>true</code> if the point (x,y) is contained within this
	 *         handle.
	 */
	@Override
	public boolean containsPoint(int x, int y) 
	{
		// true, if inside bounds but not inside inner rectangle
		if (!getBounds().contains(x, y))
			return false;
		Rectangle inner = FigureUtil.getAdjustedRectangle(getBounds(), 1.0, 2 * getLineWidth());
		return !inner.contains(x, y);
	}

	/**
	 * Returns a point along the right edge of the handle.
	 * 
	 * @see org.eclipse.gef.Handle#getAccessibleLocation()
	 */
	@Override
	public Point getAccessibleLocation() 
	{
		Point p = getBounds().getTopRight().translate(-1, getBounds().height / 4);
		translateToAbsolute(p);
		return p;
	}

	/**
	 * Paints a rectangular handle surrounding the owner edit-part.
	 */
	@Override
	public void paintFigure(Graphics g) 
	{
		g.setAntialias(SWT.ON);
		g.setLineWidth(getLineWidth());

		Rectangle r = new Rectangle(getBounds());
		int zoom = this.configManager.getDiagramEditor().getPart().getZoomLevel();
		int scaledInset = -getLineOffset() * zoom / 100;
		r.shrink(scaledInset, scaledInset);
		r = FigureUtil.getAdjustedRectangle(r, 1.0, getLineWidth());

		prepareForDrawing(g, PositionConstants.NORTH);
		g.drawLine(r.getTopLeft(), r.getTopRight());
		prepareForDrawing(g, PositionConstants.SOUTH);
		g.drawLine(r.getBottomLeft(), r.getBottomRight());
		prepareForDrawing(g, PositionConstants.EAST);
		g.drawLine(r.getTopRight(), r.getBottomRight());
		prepareForDrawing(g, PositionConstants.WEST);
		g.drawLine(r.getTopLeft(), r.getBottomLeft());
	}

	public int getLineWidth() 
	{
		return this.lineWidth;
	}
	
	public void setLineWidth(int lineWidth)
	{
		this.lineWidth = lineWidth;
	}

	public int getLineOffset()
	{
		return this.lineOffset;
	}
	
	public void setLineOffset(int lineOffset)
	{
		this.lineOffset = lineOffset;
	}
		
	public void setLineStyle(int style)
	{
		this.lineStyle = style;
	}
		
	public void setLineColor(org.eclipse.sapphire.Color color)
	{
		this.lineColor = this.resourceCache.getColor(color);
	}
	
	/**
	 * Prepares the graphics to paint the rectangle-side for the given
	 * direction. This will set the line-style and foreground color.
	 * 
	 * @param g
	 *            The graphics which to prepare.
	 * @param direction
	 *            The direction, for which to prepare the graphics.
	 */
	private void prepareForDrawing(Graphics g, int direction) 
	{
		g.setLineStyle(this.lineStyle);
		// It is necessary to set the color. This ensures the support for the high contrast mode.
		setForegroundColor(this.lineColor);
		g.setForegroundColor(getForegroundColor());
	}

}
