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

package org.eclipse.sapphire.ui.swt.gef;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.sapphire.ui.diagram.editor.SapphireDiagramEditorPagePart;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramModel;
import org.eclipse.swt.graphics.Color;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SapphireDiagramGridLayer extends GridLayer
{
	private static final org.eclipse.sapphire.Color DEFAULT_MAJOR_LINE_COLOR = new org.eclipse.sapphire.Color(206, 224, 242);
	private static final org.eclipse.sapphire.Color DEFAULT_MINOR_LINE_COLOR = new org.eclipse.sapphire.Color(227, 238, 249);
	
	private DiagramModel diagramModel;
	private SapphireDiagramEditorPagePart diagramPart;
	
	public SapphireDiagramGridLayer(DiagramModel diagramModel)
	{
		this.diagramModel = diagramModel;
		this.diagramPart = diagramModel.getSapphirePart();
	}
	
	@Override
	protected void paintGrid(Graphics g)
	{
		Rectangle clip = g.getClip(Rectangle.SINGLETON);
		
		Color majorLineColor = diagramModel.getResourceCache().getColor(DEFAULT_MAJOR_LINE_COLOR);
		Color minorLineColor = diagramModel.getResourceCache().getColor(DEFAULT_MINOR_LINE_COLOR);
		
		gridX = this.diagramPart.getGridUnit();
		gridY = this.diagramPart.getVerticalGridUnit();
		
		if (gridX > 0) 
		{
			int c = 0;
			int i = clip.x;
			while (i % gridX != 0)
				i++;

			for (; i < clip.x + clip.width; i += gridX) 
			{
				c++;
				prepareG(g, majorLineColor, minorLineColor, i, gridX);
				g.drawLine(i, clip.y, i, clip.y + clip.height);
			}
		}

		if (gridY > 0)
		{
			int c = 0;
			int i = clip.y;
			while (i % gridY != 0)
				i++;

			for (; i < clip.y + clip.height; i += gridY) 
			{
				c++;
				prepareG(g, majorLineColor, minorLineColor, i, gridY);
				g.drawLine(clip.x, i, clip.x + clip.width, i);
			}
		}
				
	}
	
	private void prepareG(Graphics g, Color gridColor, Color gridColorLight, int gridPosition, int gridSize) 
	{

		int p = 5 * gridSize;
		if (gridPosition % (p) == 0) 
		{
			g.setForegroundColor(gridColor);			
		} 
		else
		{
			g.setForegroundColor(gridColorLight);
		}

	}
	
}
