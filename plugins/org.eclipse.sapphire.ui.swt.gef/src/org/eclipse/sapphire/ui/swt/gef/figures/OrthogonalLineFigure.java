/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.presentation.LineShapePresentation;
import org.eclipse.swt.graphics.Color;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class OrthogonalLineFigure extends Shape 
{
	private LineShapePresentation linePresentation;
	private DiagramResourceCache resourceCache;
	
	public OrthogonalLineFigure(LineShapePresentation linePresentation, DiagramResourceCache resourceCache)
	{
		this.linePresentation = linePresentation;
		this.resourceCache = resourceCache;
	}
	
	@Override
	protected void fillShape(Graphics graphics) 
	{

	}

	@Override
	protected void outlineShape(Graphics graphics) 
	{
		// Save existing graphics attributes
		final int oldLineWidth = graphics.getLineWidth();
		final Color oldColor = graphics.getForegroundColor();
		final int oldLineStyle = graphics.getLineStyle();
		
		Rectangle bbox = getBounds();
		int weight = this.linePresentation.getWeight();
		graphics.setLineWidth(weight);
		graphics.setForegroundColor(resourceCache.getColor(this.linePresentation.getColor()));
		graphics.setLineStyle(FigureUtil.convertLineStyle(this.linePresentation.getStyle()));
		
		int inset = Math.max(1, weight / 2);
		
		if (this.linePresentation.isHorizontal())
		{
			graphics.drawLine(bbox.x + inset, bbox.y, bbox.x + bbox.width - inset - inset, bbox.y);
		}
		else
		{
			graphics.drawLine(bbox.x + inset, bbox.y, bbox.x + inset, bbox.y + bbox.height);
		}
		
		// Restore previous graphics attributes
		graphics.setLineWidth(oldLineWidth);
		graphics.setForegroundColor(oldColor);
		graphics.setLineStyle(oldLineStyle);
		
	}
	
	@Override
	public Dimension getPreferredSize(int wHint, int hHint)
	{
		if (this.linePresentation.isHorizontal())
		{
			return new Dimension(wHint, this.linePresentation.getWeight());
		}
		else
		{
			return new Dimension(this.linePresentation.getWeight(), hHint);
		}
	}

	@Override
	public Dimension getMaximumSize()
	{
		if (this.linePresentation.isHorizontal())
		{
			return new Dimension(Integer.MAX_VALUE, this.linePresentation.getWeight());
		}
		else
		{
			return new Dimension(this.linePresentation.getWeight(), Integer.MAX_VALUE);
		}
	}
}
