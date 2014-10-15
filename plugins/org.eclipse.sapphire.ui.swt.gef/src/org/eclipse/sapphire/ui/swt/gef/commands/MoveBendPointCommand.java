/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Shenxue Zhou - Bug 445831 - Bendpoint can be created under a node
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.commands;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.sapphire.ui.Bounds;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class MoveBendPointCommand extends BendPointCommand 
{

	@Override 
	public boolean canExecute()
	{
		List<DiagramNodeModel> nodes = getDiagramConnectionModel().getDiagramModel().getNodes();
		Point location = getLocation();
		for (DiagramNodeModel node : nodes)
		{
			if (pointInBounds(location, node.getShapePresentation().getFigure().getBounds()))
			{
				return false;
			}
		}
		return true;		
	}
	
	public void execute() 
	{
		getDiagramConnectionModel().getModelPart().updateBendpoint(getIndex(), getLocation().x, getLocation().y);
		super.execute();
	}
	
	protected boolean pointInBounds(Point p, Bounds bounds)
	{
		int x = bounds.getX();
		int y = bounds.getY();
		int x2 = x + bounds.getWidth();
		int y2 = y + bounds.getHeight();
		
		if (x <= p.x && p.x <= x2 && y <= p.y && p.y <= y2)
		{
			return true;
		}
		return false;
	}
	

}
