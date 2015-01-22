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

package org.eclipse.sapphire.ui.swt.gef.figures;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.sapphire.ui.def.HorizontalAlignment;
import org.eclipse.sapphire.ui.def.VerticalAlignment;
import org.eclipse.sapphire.ui.swt.gef.presentation.ImagePresentation;
import org.eclipse.swt.graphics.Image;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SapphireImageFigure extends SmoothImageFigure implements IShapeFigure
{
	public SapphireImageFigure(ImagePresentation imagePresentation, Image image)
	{
		super(image);
		HorizontalAlignment horizontalAlign = imagePresentation.getLayoutConstraint().getHorizontalAlignment().content();		
		VerticalAlignment verticalAlign = imagePresentation.getLayoutConstraint().getVerticalAlignment().content();
		int alignment = getDraw2dImageAlignment(horizontalAlign);
		alignment |= getDraw2dImageAlignment(verticalAlign);
		setAlignment(alignment);
	}
	
	private int getDraw2dImageAlignment(HorizontalAlignment horizontalAlign)
	{
		int alignment = PositionConstants.CENTER;
		switch (horizontalAlign) 
		{
			case LEFT:
				alignment = PositionConstants.WEST;
				break;
			case RIGHT:
				alignment = PositionConstants.EAST;
				break;
			default:			
				break;
		}
		return alignment;
	}
	
	private int getDraw2dImageAlignment(VerticalAlignment verticalAlign)
	{
		int alignment = PositionConstants.CENTER;
		switch (verticalAlign) 
		{
			case TOP:
				alignment = PositionConstants.NORTH;
				break;
			case BOTTOM:
				alignment = PositionConstants.SOUTH;
				break;
			default:			
				break;
		}
		return alignment;
	}

	@Override
	public void setSelected(boolean b) {
	}

	@Override
	public void setFocus(boolean b) {
	}
	
}
