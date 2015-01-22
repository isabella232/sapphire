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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class SelfLoopConnectionRouter extends BendpointConnectionRouter 
{
	private static double MIN_CONTROLPT_DIST = 18;
	private static double MAX_CONTROLPT_DIST = 36;
	
	public void route(Connection conn)
	{
        PointList points = conn.getPoints();
        points.removeAllPoints();
		
		Point ref1, ref2;
		ref1 = conn.getTargetAnchor().getReferencePoint();
		ref2 = conn.getSourceAnchor().getReferencePoint();
        PrecisionPoint startPoint = new PrecisionPoint(conn.getSourceAnchor().getLocation(ref1));
        conn.translateToRelative(startPoint);
    	PrecisionPoint endPoint = new PrecisionPoint(conn.getTargetAnchor().getLocation(ref2));    	
    	conn.translateToRelative(endPoint);

    	DoublePoint p1, p2, p3, p4;
    	p1 = new DoublePoint(startPoint.preciseX(), startPoint.preciseY());
    	p4 = new DoublePoint(endPoint.preciseX(), endPoint.preciseY());
    	double dist = PathUtil.dist(p1, p4) / 3;
    	if (dist < MIN_CONTROLPT_DIST)
    		dist = MIN_CONTROLPT_DIST;
    	if (dist > MAX_CONTROLPT_DIST)
    		dist = MAX_CONTROLPT_DIST;
    	p2 = new DoublePoint(startPoint.preciseX() + dist * 2, startPoint.y - dist * 1.5);
    	p3 = new DoublePoint(endPoint.preciseX() + dist * 2, endPoint.y + dist * 1.5);
    	List<Double> doubles = new ArrayList<Double>();
		PathUtil.recursiveBezier(p1.x, p1.y,
				p2.x, p2.y,
				p3.x, p3.y,
				p4.x, p4.y,
				4, doubles);
				
    	points.addPoint(startPoint);
    	for (int i = 2; i < doubles.size() - 2; )
    	{
    		PrecisionPoint p = new PrecisionPoint(doubles.get(i), doubles.get(i+1));
    		points.addPoint(p);
    		i += 2;
    	}
    	points.addPoint(endPoint);
    	conn.setPoints(points); 
	}
	
}
