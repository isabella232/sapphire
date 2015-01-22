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

package org.eclipse.sapphire.ui.swt.gef.connections;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SelfConnectionTargetAnchor extends AbstractConnectionAnchor 
{		
	public SelfConnectionTargetAnchor(IFigure source)
	{
		super(source);
	}

	public Point getLocation(Point reference) 
	{
		Rectangle r = getOwner().getBounds().getCopy();
		getOwner().translateToAbsolute(r);
		Point anchor = new Point(r.x + r.width, r.y + (r.height >> 1) + 6);
		return anchor;
	}
}
