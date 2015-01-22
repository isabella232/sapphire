/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.presentation;

import org.eclipse.draw2d.IFigure;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.diagram.editor.SpacerPart;
import org.eclipse.sapphire.ui.swt.gef.figures.SpacerFigure;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SpacerPresentation extends ShapePresentation 
{
	private SpacerPart spacerPart;
	
	public SpacerPresentation(DiagramPresentation parent, SpacerPart spacerPart,
							DiagramResourceCache resourceCache)
	{
		super(parent, spacerPart, resourceCache);
		this.spacerPart = spacerPart;
	}
	
	public Point getSize()
	{
		return this.spacerPart.getSize();
	}

	public Point getMinimunSize()
	{
		return this.spacerPart.getMinimumSize();
	}

	public Point getMaximumSize()
	{
		return this.spacerPart.getMaximumSize();
	}
	
	@Override
	public void render()
	{
		IFigure figure = null;
		if (visible())
		{
			figure = new SpacerFigure(this);
		}
		setFigure(figure);
	}
}
