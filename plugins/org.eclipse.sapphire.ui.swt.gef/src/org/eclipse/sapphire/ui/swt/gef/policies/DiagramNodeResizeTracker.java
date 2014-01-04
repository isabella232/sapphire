/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - Maintain backward compatibility with Helios SR1
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.policies;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.ResizeTracker;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramNodeResizeTracker extends ResizeTracker 
{

	public DiagramNodeResizeTracker(GraphicalEditPart owner, int direction) 
	{
		super(owner, direction);
	}

	protected Dimension getMaximumSizeFor(ChangeBoundsRequest request) 
	{
		Dimension minSize = getOwner().getFigure().getMinimumSize();
		Dimension maxSize = getOwner().getFigure().getMaximumSize();
		int width = Math.max(maxSize.width, minSize.width);
		int height =  Math.max(maxSize.height, minSize.height);		
		
		return new Dimension(width, height);
	}

	protected Dimension getMinimumSizeFor(ChangeBoundsRequest request) 
	{
		return getOwner().getFigure().getMinimumSize();
	}
}

