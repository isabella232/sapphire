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

package org.eclipse.sapphire.ui.swt.gef.connections;

import java.util.List;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class PathUtil 
{
	public static void recursiveBezier(double x1, double y1, 
            double x2, double y2, 
            double x3, double y3, 
            double x4, double y4,
            int depth, List<Double> points)
	{
		if (depth == 0)
		{
			points.add(x1);
			points.add(y1);
			points.add(x4);
			points.add(y4);
			return;
		}
		depth--;
		// Calculate all the mid-points of the line segments
		//----------------------
		double x12   = (x1 + x2) / 2;
		double y12   = (y1 + y2) / 2;
		double x23   = (x2 + x3) / 2;
		double y23   = (y2 + y3) / 2;
		double x34   = (x3 + x4) / 2;
		double y34   = (y3 + y4) / 2;
		double x123  = (x12 + x23) / 2;
		double y123  = (y12 + y23) / 2;
		double x234  = (x23 + x34) / 2;
		double y234  = (y23 + y34) / 2;
		double x1234 = (x123 + x234) / 2;
		double y1234 = (y123 + y234) / 2;

		// Continue subdivision
		//----------------------
		recursiveBezier(x1, y1, x12, y12, x123, y123, x1234, y1234, depth, points); 
		recursiveBezier(x1234, y1234, x234, y234, x34, y34, x4, y4, depth, points); 
	}
	
	public static DoublePoint normv(DoublePoint v) 
	{
	    double d;
	    DoublePoint v1 = new DoublePoint(v);

	    d = Math.sqrt (v1.x * v1.x + v1.y * v1.y);
	    if (d != 0)
	    {
	        v1.x /= d;
	        v1.y /= d;
	    }
	    return v1;
	}
	
	public static double dist (DoublePoint p1, DoublePoint p2) 
	{
	    double dx, dy;

	    dx = p2.x - p1.x;
	    dy = p2.y - p1.y;
	    return Math.sqrt (dx * dx + dy * dy);
	}

	public static DoublePoint scale (DoublePoint p, double c) 
	{
		DoublePoint p1 = new DoublePoint(p);
	    p1.x *= c; 
	    p1.y *= c;
	    return p1;
	}
	
	public static double B0 (double t)
	{
	    double tmp = 1.0 - t;
	    return tmp * tmp * tmp;
	}

	public static double B1 (double t)
	{
	    double tmp = 1.0 - t;
	    return 3 * t * tmp * tmp;
	}

	public static double B2 (double t)
	{
	    double tmp = 1.0 - t;
	    return 3 * t * t * tmp;
	}

	public static double B3 (double t)
	{
	    return t * t * t;
	}
	
	public static double B01 (double t)
	{
	    double tmp = 1.0 - t;
	    return tmp * tmp * (tmp + 3 * t);
	}

	public static double B23 (double t)
	{
	    double tmp = 1.0 - t;
	    return t * t * (3 * tmp + t);
	}
	
	public static double dot (DoublePoint p1, DoublePoint p2) 
	{
	    return p1.x * p2.x + p1.y * p2.y;
	}
	
	public static DoublePoint add (DoublePoint p1, DoublePoint p2) {
		DoublePoint p = new DoublePoint(p1);
	    p.x += p2.x;
	    p.y += p2.y;
	    return p;
	}
	
	public static DoublePoint sub (DoublePoint p1, DoublePoint p2) {
		DoublePoint p = new DoublePoint(p1);
	    p.x -= p2.x;
	    p.y -= p2.y;
	    return p;
	}
	
}
