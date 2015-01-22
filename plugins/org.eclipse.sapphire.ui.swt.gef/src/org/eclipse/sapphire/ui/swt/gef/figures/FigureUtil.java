/******************************************************************************
 * Copyright (c) 2015 Oracle
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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Vector;
import org.eclipse.sapphire.ui.LineStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Path;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class FigureUtil {

	public static PointList getAdjustedPointList(PointList points, double zoom, double lw) {
		Rectangle zoomedBounds = points.getBounds().getCopy().scale(zoom);
		double middlex = zoomedBounds.x + (zoomedBounds.width / 2);
		double middley = zoomedBounds.y + (zoomedBounds.height / 2);

		PointList ret = new PointList();
		for (int i = 0; i < points.size(); i++) {
			Point point = points.getPoint(i);
			point.scale(zoom);

			// translate all points towards the middle depending on the
			// line-width, so that the polyline remains inside the bounds
			// Note, that the delta has to be rounded up/down depending on the
			// relative location from point to middle.
			double dx;
			double dy;
			if (point.x < middlex) {
				dx = Math.ceil(((middlex - point.x) / zoomedBounds.width) * lw);
			} else {
				dx = Math.floor(((middlex - point.x) / zoomedBounds.width) * lw);
			}
			if (point.y < middley) {
				dy = Math.ceil(((middley - point.y) / zoomedBounds.height) * lw);
			} else {
				dy = Math.floor(((middley - point.y) / zoomedBounds.height) * lw);
			}

			point.translate((int) dx, (int) dy);

			ret.addPoint(point);
		}
		return ret;
	}

	public static Path getBezierPath(List<BezierPoint> origPoints, boolean isClosed) {
		Path path = new Path(null);
		// make a copy, so that we can change it
		List<BezierPoint> points = new ArrayList<BezierPoint>(origPoints.size() + 2);
		points.addAll(origPoints);

		// Draw simple lines, as bezier-curve doesn't make sense
		if (points.size() < 3 || !hasBezierDistance(origPoints)) {
			if (points.size() != 0) {
				path.moveTo(points.get(0).getX(), points.get(0).getY());
				for (int i = 1; i < points.size(); i++) {
					path.lineTo(points.get(i).getX(), points.get(i).getY());
				}
			}
		} else { // Draw bezier curve

			// Idea for the closed bezier curve:
			// The first two points are added to the end of the point-list.
			// Afterwards the bezier-curve through the points is drawn as usual,
			// except that the first line and the last line are not drawn.

			// Adjust point-list if closing is needed: add the first two points
			// again at the end
			if (isClosed) {
				if (!points.get(points.size() - 1).equals(points.get(0))) { // first
																			// !=
																			// last
																			// =>
																			// only
																			// then
																			// double
																			// first
																			// point
					points.add(points.get(0));
				}
				points.add(points.get(1));
			}

			// Initialize the points for calculation
			Point c = points.get(0).createDraw2dPoint(); // the current
															// control-point
			Point q = points.get(1).createDraw2dPoint(); // the point following
															// the current
			// control-point
			Point r = new Point();
			Point s = new Point();

			// If not closed, draw the first line from the first point to r,
			// otherwise move to r
			determineBezierPoints(c, q, r, s, points.get(0).getBezierDistanceAfter(), points.get(1).getBezierDistanceBefore());
			if (!isClosed) {
				path.moveTo(points.get(0).getX(), points.get(0).getY());
				path.lineTo(r.x, r.y);
			} else {
				path.moveTo(r.x, r.y);
			}

			for (int index = 2; index < points.size(); index++) {
				// Move c and q one position forward
				c.setLocation(q);
				points.get(index).copyToDraw2dPoint(q);

				// Update r and s
				determineBezierPoints(c, q, r, s, points.get(index - 1).getBezierDistanceAfter(), points.get(index)
						.getBezierDistanceBefore());

				// draw the curve
				path.quadTo(c.x, c.y, s.x, s.y);
				path.lineTo(r.x, r.y);
			}

			// If not closed, draw the final line from r to the last point,
			// otherwise do nothing
			if (!isClosed) {
				// Draw the final line from r to the last point.
				path.lineTo(points.get(points.size() - 1).getX(), points.get(points.size() - 1).getY());
			}
		}

		// The algorithm already takes care, that the line ends again at the
		// start-point.
		// But a final path.close() takes care of drawing problems.
		if (isClosed) {
			path.close();
		}

		return path;
	}

	public static boolean hasBezierDistance(List<BezierPoint> points) {
		for (BezierPoint point : points) {
			if (point.getBezierDistanceBefore() != 0 || point.getBezierDistanceAfter() != 0)
				return true;
		}
		return false;
	}
	
	private static void determineBezierPoints(Point c, Point q, Point r, Point s, int distanceAfterCurrent, int distanceBeforeNext) {
		// Determine v and m
		// Ray v = new Ray();
		int vx = q.x - c.x;
		int vy = q.y - c.y;
		Vector v = new Vector(vx, vy);
		double absV = v.getLength();
		// Ray m = new Ray();
		int mx = Math.round(c.x + ( (float) vx ) / 2);
		int my = Math.round(c.y + ( (float) vy ) / 2);

		// Determine tolerance
		// Idea:
		// The vector v is the line after the current control-point c.
		// If the sum of the bezier-distances is greater than the half
		// line-length of v,
		// then a simplified calculation for the bezier-points r and s must be
		// done.
		int tolerance = distanceAfterCurrent + distanceBeforeNext;

		// Determine the "results" r and s
		if (absV < tolerance) {
			// use the the midpoint m for r and s
			r.x = mx;
			r.y = my;
			s.x = mx;
			s.y = my;
		} else {
			double x = (absV - distanceBeforeNext) / absV;
			r.x = Math.round(c.x + (float) x * vx);
			r.y = Math.round(c.y + (float) x * vy);
			double y = distanceAfterCurrent / absV;
			s.x = Math.round(c.x + (float) y * vx);
			s.y = Math.round(c.y + (float) y * vy);
		}
	}
	
	public static int convertLineStyle(LineStyle style)
	{
		int swtStyle = SWT.LINE_SOLID;
		if (style == LineStyle.DASH)
		{
			swtStyle = SWT.LINE_DASH;
		}
		else if (style == LineStyle.DASH_DOT)
		{
			swtStyle = SWT.LINE_DASHDOT;
		}
		else if (style == LineStyle.DOT)
		{
			swtStyle = SWT.LINE_DOT;
		}
		return swtStyle;
	}
	
	public static Rectangle getAdjustedRectangle(Rectangle rectangle, double zoom, int lw) {
		if (rectangle == null) {
			return null;
		}

		Rectangle ret = new Rectangle(rectangle);

		if (zoom != 1.0) {
			ret.x = (int) (Math.floor(rectangle.x * zoom));
			ret.y = (int) (Math.floor(rectangle.y * zoom));
			ret.width = (int) (Math.floor(((rectangle.x + rectangle.width) * zoom))) - ret.x;
			ret.height = (int) (Math.floor(((rectangle.y + rectangle.height) * zoom))) - ret.y;
		}

		int adjustmentTopLeft = lw / 2;
		int adjustmentBottomRight = lw;
		ret.x += adjustmentTopLeft;
		ret.y += adjustmentTopLeft;
		ret.width -= adjustmentBottomRight;
		ret.height -= adjustmentBottomRight;

		return ret;
	}

}
