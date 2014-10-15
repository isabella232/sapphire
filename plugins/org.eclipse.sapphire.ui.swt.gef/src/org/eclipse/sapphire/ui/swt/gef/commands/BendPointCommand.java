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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramConnectionModel;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class BendPointCommand extends Command {

	protected int index;
	protected Point location;
	protected DiagramConnectionModel diagramConnectionModel;
	private Dimension d1, d2;

	protected Dimension getFirstRelativeDimension() {
		return d1;
	}

	protected Dimension getSecondRelativeDimension() {
		return d2;
	}

	protected int getIndex() {
		return index;
	}

	protected Point getLocation() {
		return location;
	}

	public void redo() {
		execute();
	}

	public void setRelativeDimensions(Dimension dim1, Dimension dim2) {
		d1 = dim1;
		d2 = dim2;
	}

	public void setIndex(int i) {
		index = i;
	}

	public void setLocation(Point p) {
		location = p;
	}

	protected DiagramConnectionModel getDiagramConnectionModel() {
		return diagramConnectionModel;
	}

	public void setDiagramConnectionModel(DiagramConnectionModel connectionModel) {
		diagramConnectionModel = connectionModel;
	}
	
	protected boolean pointInBounds(Point p, Rectangle bounds)
	{
		if (bounds.x <= p.x && p.x <= bounds.x + bounds.width && 
				bounds.y <= p.y && p.y <= bounds.y + bounds.height)
		{
			return true;
		}
		return false;
	}

}
