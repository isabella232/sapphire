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

package org.eclipse.sapphire.ui.swt.gef.policies;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Handle;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.handles.MoveHandle;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramConnectionLabelEditPart;
import org.eclipse.swt.graphics.Cursor;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramConnectionLabelEditPolicy extends NonResizableEditPolicy {

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected List createSelectionHandles() {
		List handles = new ArrayList();
		//createMoveHandle(list);
		if (isDragAllowed()) {
			// display 'move' handle to allow dragging
			// Replace ResizableHandleKit.addMoveHandle to customize color and style
			handles.add(moveHandle((GraphicalEditPart) getHost(), Cursors.SIZEALL));
		} else {
			// display 'move' handle only to indicate selection
			// Replace ResizableHandleKit.addMoveHandle to customize color and style
			handles.add(moveHandle((GraphicalEditPart) getHost(), SharedCursors.ARROW));
		}
		return handles;
	}

	public static Handle moveHandle(GraphicalEditPart owner, Cursor cursor) {
		MoveHandle moveHandle = new MoveHandle(owner);
		//moveHandle.setDragTracker(tracker);
		moveHandle.setCursor(cursor);

    	DiagramResourceCache resourceCache = ((DiagramConnectionLabelEditPart)owner).getCastedModel().getDiagramModel().getResourceCache();
		moveHandle.setBorder(new LineBorder(resourceCache.getOutlineColor(), 1, Graphics.LINE_DASH));

		return moveHandle;
	}
}
