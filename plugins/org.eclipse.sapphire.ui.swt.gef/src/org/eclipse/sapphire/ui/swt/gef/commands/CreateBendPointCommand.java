/******************************************************************************
 * Copyright (c) 2015 Oracle
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
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class CreateBendPointCommand extends BendPointCommand 
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
		getDiagramConnectionModel().getModelPart().addBendpoint(getIndex(), getLocation().x, getLocation().y);
		super.execute();
	}

}
