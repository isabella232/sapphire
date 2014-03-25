/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924]  Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.policies;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Transposer;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editpolicies.OrderedLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.sapphire.ui.swt.gef.layout.SapphireSequenceLayout;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.parts.ShapeEditPart;
import org.eclipse.sapphire.ui.swt.gef.presentation.ShapePresentation;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public abstract class SequenceLayoutEditPolicy extends OrderedLayoutEditPolicy {

	private Polyline insertionLine;

	protected void eraseLayoutTargetFeedback(Request request) {
		if (insertionLine != null) {
			removeFeedback(insertionLine);
			insertionLine = null;
		}
	}

	private Rectangle getAbsoluteBounds(GraphicalEditPart ep) {		
		Rectangle bounds = ep.getContentPane().getBounds().getCopy();
		ep.getContentPane().translateToAbsolute(bounds);
		return bounds;
	}

	/**
	 * @param request
	 *            the Request
	 * @return the index for the insertion reference
	 */
	
	protected int getFeedbackIndexFor(Request request) {
        List<?> children = getHost().getChildren();
		if (children.isEmpty())
			return -1;

		Transposer transposer = new Transposer();
		transposer.setEnabled(!isLayoutHorizontal(request));

		Point p = transposer.t(getLocationFromRequest(request));

		// Current row bottom, initialize to above the top.
		int rowBottom = Integer.MIN_VALUE;
		int candidate = -1;
		for (int i = 0; i < children.size(); i++) {
			EditPart child = (EditPart) children.get(i);
			if (child instanceof ShapeEditPart) {
				ShapePresentation presentation = ((ShapeEditPart)child).getShapePresentation();
				if (presentation != null && presentation.isSeparator()) {
					continue;
				}
			}
			Rectangle rect = transposer
					.t(getAbsoluteBounds(((GraphicalEditPart) child)));
			if (rect.y > rowBottom) {
				/*
				 * We are in a new row, so if we don't have a candidate but yet
				 * are within the previous row, then the current entry becomes
				 * the candidate. This is because we know we must be to the
				 * right of center of the last Figure in the previous row, so
				 * this Figure (which is at the start of a new row) is the
				 * candidate.
				 */
				if (p.y <= rowBottom) {
					if (candidate == -1)
						candidate = i;
					break;
				} else
					candidate = -1; // Mouse's Y is outside the row, so reset
									// the candidate
			}
			rowBottom = Math.max(rowBottom, rect.bottom());
			if (candidate == -1) {
				/*
				 * See if we have a possible candidate. It is a candidate if the
				 * cursor is left of the center of this candidate.
				 */
				if (p.x <= rect.x + (rect.width / 2))
					candidate = i;
			}
			if (candidate != -1) {
				// We have a candidate, see if the rowBottom has grown to
				// include the mouse Y.
				if (p.y <= rowBottom) {
					/*
					 * Now we have determined that the cursor.Y is above the
					 * bottom of the current row of figures. Stop now, to
					 * prevent the next row from being searched
					 */
					break;
				}
			}
		}
		return candidate;
	}

	protected EditPart getInsertionReference(Request request) {
		List<?> children = getHost().getChildren();

		if (request.getType().equals(RequestConstants.REQ_CREATE)) {
			int i = getFeedbackIndexFor(request);
			if (i == -1)
				return null;
			return (EditPart) children.get(i);
		}

		int index = getFeedbackIndexFor(request);
		if (index != -1) {
			List<?> selection = getHost().getViewer().getSelectedEditParts();
			do {
				EditPart editpart = (EditPart) children.get(index);
				if (!selection.contains(editpart))
					return editpart;
			} while (++index < children.size());
		}
		return null; // Not found, add at the end.
	}

	/**
	 * Lazily creates and returns a <code>Polyline</code> Figure for use as
	 * feedback.
	 * 
	 * @return a Polyline figure
	 */
	protected Polyline getLineFeedback() {
		if (insertionLine == null) {
			insertionLine = new Polyline();
			insertionLine.setLineWidth(2);
			if (getHost() instanceof ShapeEditPart) {
				ShapeEditPart shapeEditPart = (ShapeEditPart)getHost();
				DiagramResourceCache resourceCache = shapeEditPart.getNodeEditPart().getCastedModel().getDiagramModel().getResourceCache();
				insertionLine.setForegroundColor(resourceCache.getOutlineColor());
			}
			insertionLine.addPoint(new Point(0, 0));
			insertionLine.addPoint(new Point(10, 10));
			addFeedback(insertionLine);
		}
		return insertionLine;
	}

	private Point getLocationFromRequest(Request request) {
		return ((DropRequest) request).getLocation();
	}

	/**
	 * Shows an insertion line if there is one or more current children.
	 */
	protected void showLayoutTargetFeedback(Request request) {
		if (getHost().getChildren().size() == 0)
			return;
		Polyline fb = getLineFeedback();
		Transposer transposer = new Transposer();
		transposer.setEnabled(!isLayoutHorizontal(request));

		boolean before = true;
		int epIndex = getFeedbackIndexFor(request);
		Rectangle r = null;
		if (epIndex == -1) {
			before = false;
			epIndex = getHost().getChildren().size() - 1;
			EditPart editPart = (EditPart) getHost().getChildren().get(epIndex);
			r = transposer.t(getAbsoluteBounds((GraphicalEditPart) editPart));
		} else {
			EditPart editPart = (EditPart) getHost().getChildren().get(epIndex);
			r = transposer.t(getAbsoluteBounds((GraphicalEditPart) editPart));
			Point p = transposer.t(getLocationFromRequest(request));
			if (p.x <= r.x + (r.width / 2))
				before = true;
			else {
				/*
				 * We are not to the left of this Figure, so the emphasis line
				 * needs to be to the right of the previous Figure, which must
				 * be on the previous row.
				 */
				before = false;
				epIndex--;
				editPart = (EditPart) getHost().getChildren().get(epIndex);
				r = transposer
						.t(getAbsoluteBounds((GraphicalEditPart) editPart));
			}
		}
		int x = Integer.MIN_VALUE;
		if (before) {
			/*
			 * Want the line to be halfway between the end of the previous and
			 * the beginning of this one. If at the beginning of a line, then
			 * start halfway between the left edge of the parent and the
			 * beginning of the box, but no more than 5 pixels (it would be too
			 * far and be confusing otherwise).
			 */
			if (epIndex > 0) {
				// Need to determine if a line break.
				Rectangle boxPrev = transposer
						.t(getAbsoluteBounds((GraphicalEditPart) getHost()
								.getChildren().get(epIndex - 1)));
				int prevRight = boxPrev.right();
				if (prevRight < r.x) {
					// Not a line break
					x = prevRight + (r.x - prevRight) / 2;
				} else if (prevRight == r.x) {
					x = prevRight + 1;
				}
			}
			if (x == Integer.MIN_VALUE) {
				// It is a line break.
				Rectangle parentBox = transposer
						.t(getAbsoluteBounds((GraphicalEditPart) getHost()));
				x = r.x - 5;
				if (x < parentBox.x)
					x = parentBox.x + (r.x - parentBox.x) / 2;
			}
		} else {
			/*
			 * We only have before==false if we are at the end of a line, so go
			 * halfway between the right edge and the right edge of the parent,
			 * but no more than 5 pixels.
			 */
			Rectangle parentBox = transposer
					.t(getAbsoluteBounds((GraphicalEditPart) getHost()));
			int rRight = r.x + r.width;
			int pRight = parentBox.x + parentBox.width;
			x = rRight + 5;
			if (x > pRight)
				x = rRight + (pRight - rRight) / 2;
		}
		Point p1 = new Point(x, r.y);
		p1 = transposer.t(p1);
		fb.translateToRelative(p1);
		Point p2 = new Point(x, r.y + r.height);
		p2 = transposer.t(p2);
		fb.translateToRelative(p2);
		fb.setPoint(p1, 0);
		fb.setPoint(p2, 1);
	}

	private boolean isLayoutHorizontal(Request request) 
	{
		ChangeBoundsRequest cbReq = (ChangeBoundsRequest)request;
		List<?> editParts = cbReq.getEditParts();
		GraphicalEditPart shapeEditPart = (GraphicalEditPart)editParts.get(0);		
		IFigure figure = shapeEditPart.getContentPane().getParent();
		return ((SapphireSequenceLayout) figure.getLayoutManager()).isHorizontal();
	}

}
