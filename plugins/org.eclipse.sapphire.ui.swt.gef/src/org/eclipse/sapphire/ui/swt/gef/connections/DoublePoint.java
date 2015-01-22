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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.sapphire.util.HashCodeFactory;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DoublePoint 
{
	public double x;
	public double y;
	
	public DoublePoint()
	{
		this.x = 0;
		this.y = 0;
	}
	
	public DoublePoint(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public DoublePoint(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public DoublePoint(DoublePoint copy)
	{
		this.x = copy.x;
		this.y = copy.y;
	}
	
	public DoublePoint(PrecisionPoint p)
	{
		this.x = p.preciseX();
		this.y = p.preciseY();
	}
	
	public DoublePoint(Point p)
	{
		this.x = p.x;
		this.y = p.y;
	}
	
	public boolean equals(Object o) 
	{
		if (o instanceof DoublePoint) 
		{
			DoublePoint p = (DoublePoint)o;
			return p.x == x && p.y == y;
		}
		
		return false;
	}

    @Override
    public int hashCode()
    {
        return HashCodeFactory.start().add( this.x ).add( this.y ).result();
    }
	
}
