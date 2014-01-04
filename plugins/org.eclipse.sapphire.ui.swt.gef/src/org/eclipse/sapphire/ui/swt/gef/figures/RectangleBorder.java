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

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.sapphire.ui.LineStyle;
import org.eclipse.sapphire.ui.diagram.shape.def.BorderComponent;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.presentation.RectanglePresentation;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class RectangleBorder extends AbstractBorder 
{
	private RectanglePresentation rectPresentation;
	private DiagramResourceCache resourceCache;
	private boolean hasUniformBorders;
	
	public RectangleBorder(RectanglePresentation rectPresentation, DiagramResourceCache resourceCache)
	{
		this.rectPresentation = rectPresentation;
		this.resourceCache = resourceCache;
		this.hasUniformBorders = hasUniformBorders();
	}
	
	public Insets getInsets(IFigure figure) 
	{
		int tw, bw, lw, rw;
		tw = this.rectPresentation.getTopBorder().getWeight().content();
		bw = this.rectPresentation.getBottomBorder().getWeight().content();
		lw = this.rectPresentation.getLeftBorder().getWeight().content();
		rw = this.rectPresentation.getRightBorder().getWeight().content();
		
		return new Insets(tw, lw, bw, rw);
	}

	public void paint(IFigure figure, Graphics graphics, Insets insets) 
	{
		tempRect.setBounds(getPaintRectangle(figure, insets));
		BorderComponent borderDef = this.rectPresentation.getTopBorder();
		if (this.hasUniformBorders && borderDef.getWeight().content() > 0)
		{			
			int w = borderDef.getWeight().content();
			tempRect = FigureUtil.getAdjustedRectangle(tempRect, 1.0, w);

			int cornerRadius = this.rectPresentation.getCornerRadius();
			float cornerWidth = cornerRadius;
			float cornerHeight = cornerRadius;
			// adjust corner for the inner path (formula found by experimenting)
			if (cornerRadius > 0) {
				cornerHeight = Math.max(1, cornerHeight - (w + cornerHeight / 64));
				cornerWidth = Math.max(1, cornerWidth - (w + cornerWidth / 64));
			}
						
			graphics.setLineWidth(borderDef.getWeight().content());
			graphics.setForegroundColor(resourceCache.getColor(borderDef.getColor().content()));
			graphics.setLineStyle(FigureUtil.convertLineStyle(borderDef.getStyle().content()));
			graphics.drawRoundRectangle(tempRect,
					Math.max(0, (int)cornerWidth),
					Math.max(0, (int)cornerHeight));
		}
		else
		{
			if( borderDef.getWeight().content() > 0 )
			{
				int w = borderDef.getWeight().content();
				graphics.setLineWidth(w);
				graphics.setForegroundColor(resourceCache.getColor(borderDef.getColor().content()));
				graphics.setLineStyle(FigureUtil.convertLineStyle(borderDef.getStyle().content()));
				int inset = Math.max(1, w >> 1);
				int x = tempRect.x;
				int y = tempRect.y + inset;
				int x2 = tempRect.x + tempRect.width;				
				graphics.drawLine(x, y, x2, y);
			}
			
			borderDef = this.rectPresentation.getBottomBorder();			
			if( borderDef.getWeight().content() > 0 )
			{	
				int w = borderDef.getWeight().content();
				graphics.setLineWidth(w);
				graphics.setForegroundColor(resourceCache.getColor(borderDef.getColor().content()));
				graphics.setLineStyle(FigureUtil.convertLineStyle(borderDef.getStyle().content()));
				int inset = Math.max(1, w >> 1);
				int x = tempRect.x;
				int y = tempRect.y + tempRect.height - inset;
				int x2 = tempRect.x + tempRect.width;
				graphics.drawLine(x, y, x2, y);
			}
	
			borderDef = this.rectPresentation.getLeftBorder();			
			if( borderDef.getWeight().content() > 0 )
			{	
				int w = borderDef.getWeight().content();
				graphics.setLineWidth(w);
				graphics.setForegroundColor(resourceCache.getColor(borderDef.getColor().content()));
				graphics.setLineStyle(FigureUtil.convertLineStyle(borderDef.getStyle().content()));
				int inset = Math.max(1, w >> 1);
				int x = tempRect.x + inset;
				int y = tempRect.y;
				int y2 = tempRect.y + tempRect.height;				
				graphics.drawLine(x, y, x, y2);
			}

			borderDef = this.rectPresentation.getRightBorder();			
			if( borderDef.getWeight().content() > 0 )
			{	
				int w = borderDef.getWeight().content();
				graphics.setLineWidth(w);
				graphics.setForegroundColor(resourceCache.getColor(borderDef.getColor().content()));
				graphics.setLineStyle(FigureUtil.convertLineStyle(borderDef.getStyle().content()));
				int inset = Math.max(1, w >> 1);
				int x = tempRect.x + tempRect.width - inset;
				int y = tempRect.y;
				int y2 = tempRect.y + tempRect.height;				
				graphics.drawLine(x, y, x, y2);
			}			
		}
	}
		
	private boolean hasUniformBorders()
	{
		org.eclipse.sapphire.Color tc, bc, lc, rc;
		tc = this.rectPresentation.getTopBorder().getColor().content();
		bc = this.rectPresentation.getBottomBorder().getColor().content();
		lc = this.rectPresentation.getLeftBorder().getColor().content();
		rc = this.rectPresentation.getRightBorder().getColor().content();
		if (!(tc.equals(bc) && tc.equals(lc) && tc.equals(rc)))
		{
			return false;
		}
		int tw, bw, lw, rw;
		tw = this.rectPresentation.getTopBorder().getWeight().content();
		bw = this.rectPresentation.getBottomBorder().getWeight().content();
		lw = this.rectPresentation.getLeftBorder().getWeight().content();
		rw = this.rectPresentation.getRightBorder().getWeight().content();
		if (!(tw == bw && tw == lw && tw == rw))
		{
			return false;
		}
		LineStyle ts, bs, ls, rs;
		ts = this.rectPresentation.getTopBorder().getStyle().content();
		bs = this.rectPresentation.getBottomBorder().getStyle().content();
		ls = this.rectPresentation.getLeftBorder().getStyle().content();
		rs = this.rectPresentation.getRightBorder().getStyle().content();
		if (!(ts == bs && ts == ls && ts == rs))
		{
			return false;
		}
		return true;
	}
		
}
