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

package org.eclipse.sapphire.ui.swt.gef.figures;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Path;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramConnectionFigure extends PolylineConnection {
	
	private int generalBezierDistance = 15; 

	public void setGeneralBezierDistance(int bezierDistance) {
		this.generalBezierDistance = bezierDistance;
	}

	protected int getGeneralBezierDistance() {
		return generalBezierDistance;
	}


	protected List<BezierPoint> getBezierPoints(PointList points, double zoom) {
		List<BezierPoint> ret = new ArrayList<BezierPoint>(points.size());
		for (int i = 0; i < points.size(); i++) {
			int bezierDistance = (int) (getGeneralBezierDistance() * zoom);
			Point point = points.getPoint(i);
			ret.add(new BezierPoint(point.x, point.y, bezierDistance, bezierDistance));
		}
		return ret;
	}

	private Path createPath(Rectangle outerBoundss, Graphics graphics) {
		// instead of just zooming the translated-points (see
		// getTranslatedPoints()),
		// better do the calculation again by first zooming and then translating
		// to avoid rounding errors.
		final int lineWidth = 1;
		final double zoom = 1.0;
		PointList points = FigureUtil.getAdjustedPointList(getPoints(), zoom, lineWidth);

		List<BezierPoint> bezierPoints = getBezierPoints(points, zoom);
		boolean isClosed = bezierPoints.get(0).equals(bezierPoints.get(bezierPoints.size() - 1));

		Path path = FigureUtil.getBezierPath(bezierPoints, isClosed);
		return path;
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.setAntialias(SWT.ON);
		
		final int lineWidth = getLineWidth();
		
		int oldLineWidth = graphics.getLineWidth();
		graphics.setLineWidth(lineWidth);

		// get Path
		Rectangle pathbounds = getBounds();
		Path path = createPath(pathbounds, graphics);

		graphics.drawPath(path);

		// reset Graphics
		path.dispose();
		graphics.setLineWidth(oldLineWidth);
	} 
	
}
