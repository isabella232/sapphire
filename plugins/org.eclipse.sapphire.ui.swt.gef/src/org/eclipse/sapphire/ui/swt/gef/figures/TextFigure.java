/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924] Extend Sapphire Diagram Framework to support SQL Schema diagram like editors
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.figures;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.sapphire.ui.def.HorizontalAlignment;
import org.eclipse.sapphire.ui.def.VerticalAlignment;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.presentation.TextPresentation;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class TextFigure extends Label implements IShapeFigure
{
	private DiagramResourceCache resourceCache;
	private TextPresentation textPresentation;
	private Rectangle availableArea;
	private int horizontalAlignment;
	private int verticalAlignment;
	
	public TextFigure(DiagramResourceCache resourceCache, TextPresentation textPresentation)
	{
		this.resourceCache = resourceCache;
		this.textPresentation = textPresentation;
		setForegroundColor(resourceCache.getColor(textPresentation.getTextColor()));
		this.horizontalAlignment = getSwtTextAlignment(textPresentation.getLayoutConstraint().getHorizontalAlignment().content());
		setLabelAlignment(this.horizontalAlignment);
		// TODO how to reconcile both horizontal and vertical alignment with draw2d label alignment
		this.verticalAlignment = getSwtTextAlignment(textPresentation.getLayoutConstraint().getVerticalAlignment().content());
		setFont(this.resourceCache.getFont(textPresentation.getFontDef()));
		setText(textPresentation.getContent());
	}
	
	public Rectangle getAvailableArea() {
		IFigure nodeFigure = this.textPresentation.getNodeFigure();
		Rectangle nodeBounds = nodeFigure.getBounds();
		if (this.availableArea != null) {
			return new Rectangle(this.availableArea.x + nodeBounds.x, this.availableArea.y + nodeBounds.y,
					this.availableArea.width, this.availableArea.height);
		} else {
			return nodeBounds;
		}
	}

	public void setAvailableArea(Rectangle availableArea) {
		// Translate the available area to relative to the node. We don't need to
		// adjust the available area when node is moved.
		IFigure nodeFigure = this.textPresentation.getNodeFigure();
		Rectangle nodeBounds = nodeFigure.getBounds();
		this.availableArea = new Rectangle(availableArea.x - nodeBounds.x, availableArea.y - nodeBounds.y,
				availableArea.width, availableArea.height);
		
	}

	public int getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public int getVerticalAlignment() {
		return this.verticalAlignment;
	}
	
	public void setHorizontalAlignment(int horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
		setLabelAlignment(this.horizontalAlignment);
	}
	
	public TextPresentation getTextPresentation()
	{
		return this.textPresentation;
	}
	
	/**
	 * @see IFigure#getMinimumSize(int, int)
	 */
	public Dimension getMinimumSize(int w, int h) 
	{
		Dimension minSize = new Dimension();
		if (getLayoutManager() != null)
			minSize.setSize(getLayoutManager().getMinimumSize(this, w, h));

		Dimension labelSize;
		if (getTextPresentation().truncatable())
		{
			labelSize = calculateLabelSize(getTextUtilities()
				.getTextExtents(getTruncationString(), getFont())
				.intersect(
						getTextUtilities().getTextExtents(getText(), getFont())));
		}
		else
		{
			labelSize = calculateLabelSize(getTextUtilities().getTextExtents(getText(), getFont()));
		}
		Insets insets = getInsets();
		labelSize.expand(insets.getWidth(), insets.getHeight());
		minSize.union(labelSize);
		return minSize;
	}
	
	@Override
	public Dimension getMaximumSize()
	{
		return calculateLabelSize(getTextUtilities().getTextExtents(getText(), getFont()));
	}
	
	@Override
	public Dimension getPreferredSize(int wHint, int hHint) 
	{
		return calculateLabelSize(getTextUtilities().getTextExtents(getText(), getFont()));
	}

	private int getSwtTextAlignment(HorizontalAlignment horizontalAlign)
	{
		int alignment = PositionConstants.CENTER;
		switch (horizontalAlign) 
		{
			case LEFT:
				alignment = PositionConstants.LEFT;
				break;
			case RIGHT:
				alignment = PositionConstants.RIGHT;
				break;
			default:			
				break;
		}
		return alignment;
	}
	
	private int getSwtTextAlignment(VerticalAlignment verticalAlign)
	{
		int alignment = PositionConstants.CENTER;
		switch (verticalAlign) 
		{
			case TOP:
				alignment = PositionConstants.TOP;
				break;
			case BOTTOM:
				alignment = PositionConstants.BOTTOM;
				break;
			default:			
				break;
		}
		return alignment;
	}

	@Override
	public void setSelected(boolean b) 
	{
	}

	@Override
	public void setFocus(boolean b) 
	{
	}
}
