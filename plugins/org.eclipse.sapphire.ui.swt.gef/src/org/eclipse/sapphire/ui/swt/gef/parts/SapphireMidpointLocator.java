/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.parts;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionLocator;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.sapphire.ui.swt.gef.DiagramConfigurationManager;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class SapphireMidpointLocator extends ConnectionLocator {
	
	private Point delta = null;
	private DiagramConfigurationManager manager;

	public SapphireMidpointLocator(DiagramConfigurationManager manager, Connection connection) {
		super(connection);
		this.manager = manager;
	}

	public SapphireMidpointLocator(DiagramConfigurationManager manager, Connection connection, int deltaX, int deltaY) {
		super(connection);
		this.manager = manager;
		
		delta = new Point(deltaX, deltaY);
	}
	
	public Point getMidpoint() {
		Point p = getLocation(getConnection().getPoints());
		getConnection().translateToAbsolute(p);
		return p;
	}

	@Override
	protected Rectangle getNewBounds(Dimension size, Point center) {
		return new Rectangle(center, size);
	}

	@Override
	protected Point getReferencePoint() {
		Point midPoint = super.getReferencePoint();
		if (delta != null) {
			Point realDelta = new Point(delta.x, delta.y);
			double zoom = manager.getDiagramEditor().getZoomLevel();
			realDelta = realDelta.getScaled(zoom);
			midPoint.x += realDelta.x;
			midPoint.y += realDelta.y;
		} else {
			Connection conn = getConnection();
			PointList points = conn.getPoints();
			Point p1;
			Point p2;
			int size = points.size();
			if (size % 2 == 0) {
				int i = points.size() / 2;
				int j = i - 1;
				p1 = points.getPoint(j);
				p2 = points.getPoint(i);

				double value = Math.atan2(p1.y-p2.y, p1.x-p2.x);
				double angle = Math.toDegrees(value);
				if ((angle > 0 && angle < 90) || (angle > -180 && angle < -90)) {
					midPoint.y -= 12; 
				}
			} else {
				int index = size / 2;
				p1 = points.getPoint(index - 1);
				p2 = points.getPoint(index + 1);
				
				if (p1.x > midPoint.x && p2.x > midPoint.x) {
					midPoint.x += 8; 
				}
				if (p1.y > midPoint.y && p2.y > midPoint.y) {
					midPoint.y += 8; 
				}
			}
		}
		return midPoint;
	}
}
