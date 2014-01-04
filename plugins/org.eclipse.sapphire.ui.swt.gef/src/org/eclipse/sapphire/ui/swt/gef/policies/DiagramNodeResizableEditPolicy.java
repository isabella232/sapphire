/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - Maintain backward compatibility with Helios SR1
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.policies;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.handles.MoveHandle;
import org.eclipse.gef.handles.ResizableHandleKit;
import org.eclipse.gef.handles.ResizeHandle;
import org.eclipse.gef.tools.ResizeTracker;
import org.eclipse.sapphire.ui.diagram.shape.def.SelectionPresentation;
import org.eclipse.sapphire.ui.swt.gef.figures.FigureUtil;
import org.eclipse.sapphire.ui.swt.gef.figures.IShapeFigure;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramNodeEditPart;
import org.eclipse.swt.graphics.Color;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramNodeResizableEditPolicy extends ResizableEditPolicy 
{
	private DiagramResourceCache resourceCache;
	
	public DiagramNodeResizableEditPolicy(DiagramResourceCache resourceCache) {
		this.resourceCache = resourceCache;
	}
	
	private SelectionPresentation getSelectionPresentation() {
		DiagramNodeEditPart owner = (DiagramNodeEditPart) getHost();
		return owner.getCastedModel().getShapeModel().getShapePresentation().getSelectionPresentation();
	}
	
	private Color getOutlineColor() {
		SelectionPresentation selectionPresentation = getSelectionPresentation();
		if (selectionPresentation != null) {
			return resourceCache.getColor(selectionPresentation.getColor().content());
		} else {
			return resourceCache.getOutlineColor();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void createResizeHandle(List handles, int direction) {
		if ((getResizeDirections() & direction) == direction) {
			ResizeHandle handle = new ResizeHandle((GraphicalEditPart) getHost(), direction) {
				@Override
				protected Color getBorderColor() {
					return (isPrimary()) ? ColorConstants.white : getOutlineColor();
				}
				@Override
				protected Color getFillColor() {
					return (isPrimary()) ? getOutlineColor() : ColorConstants.white;
				}
			};
			handle.setDragTracker(getResizeTracker(direction));
			handle.setCursor( Cursors.getDirectionalCursor(direction, getHostFigure().isMirrored()));
			handles.add(handle);
		} else {
			// display 'resize' handle to allow dragging or indicate selection only
			createDragHandle(handles, direction);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void createMoveHandle(List handles) {
		if (isDragAllowed()) {
			// display 'move' handle to allow dragging
			MoveHandle moveHandle = new MoveHandle((GraphicalEditPart) getHost());
			moveHandle.setDragTracker(getDragTracker());
			moveHandle.setCursor(Cursors.SIZEALL);
			LineBorder border = new LineBorder(resourceCache.getOutlineColor(), 1, Graphics.LINE_DASH);
			SelectionPresentation selectionPresentation = getSelectionPresentation();
			if (selectionPresentation != null) {
				border.setColor(resourceCache.getColor(selectionPresentation.getColor().content()));
				border.setStyle(FigureUtil.convertLineStyle(selectionPresentation.getStyle().content()));
				border.setWidth(selectionPresentation.getWeight().content());
			}
			moveHandle.setBorder(border);
			handles.add(moveHandle);
		} else {
			// display 'move' handle only to indicate selection
			ResizableHandleKit.addMoveHandle((GraphicalEditPart) getHost(),
					handles, getSelectTracker(), SharedCursors.ARROW);
		}
	}

	/**
	 * Overwritten to ensure size constraints are respected.
	 */
	@Override
	protected ResizeTracker getResizeTracker(int direction) 
	{
		return new DiagramNodeResizeTracker((GraphicalEditPart) getHost(), direction);
	}

	private IShapeFigure getNodeFigure() 
	{
		DiagramNodeEditPart part = (DiagramNodeEditPart) getHost();
		if (part.getFigure() instanceof IShapeFigure)
		{
			return ((IShapeFigure) part.getFigure());
		}
		return null;
	}

	@Override
	protected void showFocus() {
		IShapeFigure shapeFigure = getNodeFigure();
		if (shapeFigure != null)
		{
			shapeFigure.setFocus(true);
		}
	}

	@Override
	protected void hideSelection() 
	{
		IShapeFigure shapeFigure = getNodeFigure();
		if (shapeFigure != null)
		{
			shapeFigure.setSelected(false);
			shapeFigure.setFocus(false);
			removeSelectionHandles();
		}
	}

	@Override
	protected void showPrimarySelection() 
	{
		IShapeFigure shapeFigure = getNodeFigure();
		if (shapeFigure != null)
		{
			shapeFigure.setSelected(true);
			shapeFigure.setFocus(true);
			addSelectionHandles();
		}
	}

	@Override
	protected void showSelection() 
	{
		IShapeFigure shapeFigure = getNodeFigure();
		if (shapeFigure != null)
		{
			shapeFigure.setSelected(true);
			shapeFigure.setFocus(false);
			addSelectionHandles();
		}
	}
	
}
