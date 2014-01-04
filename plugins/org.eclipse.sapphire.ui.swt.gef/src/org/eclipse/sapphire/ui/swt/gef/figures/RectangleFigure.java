/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [383924]  Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ui.diagram.shape.def.BackgroundDef;
import org.eclipse.sapphire.ui.diagram.shape.def.GradientBackgroundDef;
import org.eclipse.sapphire.ui.diagram.shape.def.GradientSegmentDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SelectionPresentation;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SequenceLayoutOrientation;
import org.eclipse.sapphire.ui.diagram.shape.def.ShapeLayoutDef;
import org.eclipse.sapphire.ui.diagram.shape.def.SolidBackgroundDef;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;
import org.eclipse.sapphire.ui.swt.gef.layout.SapphireSequenceLayout;
import org.eclipse.sapphire.ui.swt.gef.layout.SapphireStackLayout;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.presentation.RectanglePresentation;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Path;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class RectangleFigure extends ContainerShapeFigure implements IShapeFigure
{	
    private static final org.eclipse.sapphire.Color DEFAULT_BACKGROUND_START = new org.eclipse.sapphire.Color(0xFF, 0xFF, 0xFF);
    private static final org.eclipse.sapphire.Color DEFAULT_BACKGROUND_END = new org.eclipse.sapphire.Color(0xD4, 0xE7, 0xF8);
	
	private RectanglePresentation rectPresentation;
	private ShapeLayoutDef layout;
	private DiagramResourceCache resourceCache;
    private boolean selected;
	
	public RectangleFigure(RectanglePresentation rectPresentation, DiagramResourceCache resourceCache,
			DiagramConfigurationManager configManager)
	{
		super(rectPresentation, resourceCache, configManager);
		this.rectPresentation = rectPresentation;
		this.layout = rectPresentation.getLayout();
		this.resourceCache = resourceCache;		
		
		if (this.layout instanceof SequenceLayoutDef)
		{
			if (((SequenceLayoutDef)this.layout).getOrientation().content() != SequenceLayoutOrientation.STACKED)
			{
				SapphireSequenceLayout sequenceLayout = new SapphireSequenceLayout((SequenceLayoutDef)layout);
				this.setLayoutManager(sequenceLayout);
			}
			else 
			{
				SapphireStackLayout sapphireStackLayout = new SapphireStackLayout((SequenceLayoutDef)this.layout);
				this.setLayoutManager(sapphireStackLayout);
			}
		}
		setBorder(new RectangleBorder(this.rectPresentation, this.resourceCache));
	}
	
	@Override
	protected void fillShape(Graphics graphics) 
	{
		BackgroundDef bg = null;
		if (selected) {
			SelectionPresentation selectionPresentation = this.rectPresentation.getSelectionPresentation();
			bg = (selectionPresentation != null) ? selectionPresentation.getBackground().content() : null;
		}
		if (bg == null) {
			bg = this.rectPresentation.getBackground();
		}
		
		if (bg != null)
		{
			final Color foregroundSave = graphics.getForegroundColor();
			final Color backgroundSave = graphics.getBackgroundColor();

			org.eclipse.draw2d.geometry.Rectangle fillRectangle = 
					new org.eclipse.draw2d.geometry.Rectangle(getBounds());

			if (this.rectPresentation.getCornerRadius() > 0)
			{
				Path path = createPath(fillRectangle, graphics, true, this.rectPresentation.getCornerRadius());
				graphics.clipPath(path);
			}
			
			if (bg instanceof SolidBackgroundDef)
			{
				org.eclipse.sapphire.Color color = ((SolidBackgroundDef)bg).getColor().content();
				if (color != null)
				{
					graphics.setBackgroundColor(resourceCache.getColor(color));
				}
				else
				{
					graphics.setBackgroundColor(resourceCache.getColor(DEFAULT_BACKGROUND_END));
				}
				graphics.fillRectangle(fillRectangle);
			}
			else if (bg instanceof GradientBackgroundDef)
			{
				boolean isVertical = ((GradientBackgroundDef)bg).isVertical().content();
				ElementList<GradientSegmentDef> segments = ((GradientBackgroundDef)bg).getGradientSegments();
				Color backgroundStart = null;
				Color backgrounEnd = null;
				if (segments.size() == 0)
				{
					backgrounEnd = resourceCache.getColor(DEFAULT_BACKGROUND_END);
					backgroundStart = resourceCache.getColor(DEFAULT_BACKGROUND_START);
				}
				else if (segments.size() == 1)
				{
					GradientSegmentDef segment0 = segments.get(0);
					backgrounEnd = resourceCache.getColor(segment0.getColor().content());
					backgroundStart = backgrounEnd;
				}
				else
				{
					GradientSegmentDef segment0 = segments.get(0);
					GradientSegmentDef segment1 = segments.get(1);
					backgrounEnd = resourceCache.getColor(segment0.getColor().content());
					backgroundStart = resourceCache.getColor(segment1.getColor().content());
				}
				
				graphics.setForegroundColor(backgrounEnd);
				graphics.setBackgroundColor(backgroundStart);
				graphics.fillGradient(fillRectangle.x, fillRectangle.y, fillRectangle.width, fillRectangle.height, isVertical);
			}
			
			graphics.setForegroundColor(foregroundSave);
			graphics.setBackgroundColor(backgroundSave);
		}
	}
	
	@Override
	public Dimension getMaximumSize()
	{
		if (this.getLayoutManager() instanceof SapphireSequenceLayout)
		{
			SapphireSequenceLayout sapphireSequenceLayout = (SapphireSequenceLayout)this.getLayoutManager();
			return sapphireSequenceLayout.calculateMaximumSize(this);
		}
		else if (this.getLayoutManager() instanceof SapphireStackLayout)
		{
			SapphireStackLayout sapphireStackLayout = (SapphireStackLayout)this.getLayoutManager();
			return sapphireStackLayout.calculateMaximumSize(this);
		}
		else 
		{
			return super.getMaximumSize();
		}
	}
	
	public void setSelected(boolean b) 
	{
		selected = b;
		repaint();
	}

	public void setFocus(boolean b) 
	{
		repaint();
	}
	
	private Path createPath(org.eclipse.draw2d.geometry.Rectangle outerBounds, Graphics graphics, boolean isFill,
			int cornerRadius)
	{
		Path path = new Path(null);
		
		float x = outerBounds.x;
		float y = outerBounds.y;
		float height = outerBounds.height;
		float width = outerBounds.width;
		float bottom = y + height;
		float right = x + width;		

		// the half cornersize is the length of the arc,
		// so two time the half cornersize must not be longer than the side
		// itself
		float cornerWidth = cornerRadius;
		float cornerHeight = cornerRadius;
		cornerWidth = (cornerWidth > width) ? width : cornerWidth;
		cornerHeight = (cornerHeight > height) ? height : cornerHeight;

		if (isFill) {
			// adjust corner for the inner path (formula found by experimenting)
			cornerHeight = Math.max(1, cornerHeight - (getLineWidth() + cornerHeight / 64));
			cornerWidth = Math.max(1, cornerWidth - (getLineWidth() + cornerWidth / 64));
		}

		// workaround: path must be usual rectangle, if corner=0
		// otherwise the path is not drawn at all (same happens
		// RoundedRectangles)
		if (cornerHeight <= 0 || cornerWidth <= 0) {
			path.addRectangle(x, y, width, height);
		} else {
			path.moveTo(x, y);
			path.addArc(x, y, cornerWidth, cornerHeight, 90, 90);
			path.addArc(x, bottom - cornerHeight, cornerWidth, cornerHeight, 180, 90);
			path.addArc(right - cornerWidth, bottom - cornerHeight, cornerWidth, cornerHeight, 270, 90);
			path.addArc(right - cornerWidth, y, cornerWidth, cornerHeight, 0, 90);
			path.close();
		}

		return path;
		
	}
}
