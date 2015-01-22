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

package org.eclipse.sapphire.ui.swt.gef.commands;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.diagram.DiagramConnectionPart;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodeBounds;
import org.eclipse.sapphire.ui.swt.gef.SapphireDiagramEditor;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramConnectionModel;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramNodeModel;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramNodeEditPart;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class MoveNodeCommand extends Command {
	
	private DiagramNodeModel node;
	private Rectangle rectangle;

	public MoveNodeCommand(DiagramNodeModel node, Rectangle rectangle) {
		this.node = node;
		this.rectangle = rectangle;
	}

	@Override
	public void execute() {
		moveBendpoints();
		node.getModelPart().setNodeBounds(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}
	
	private void moveBendpoints() {
		DiagramNodeBounds bounds = node.getModelPart().getNodeBounds();
		int deltaX = this.rectangle.x - bounds.getX();
		int deltaY = this.rectangle.y - bounds.getY();
		if (deltaX != 0 || deltaY != 0) {
			SapphireDiagramEditor editor = node.getDiagramModel().getSapphireDiagramEditor();
			List<DiagramConnectionModel> srcConnections = node.getSourceConnections();
			for (GraphicalEditPart part : editor.getSelectedEditParts()) {
				if (part instanceof DiagramNodeEditPart) {
					DiagramNodeModel otherNode = ((DiagramNodeEditPart)part).getCastedModel();
					List<DiagramConnectionModel> targetConnections = otherNode.getTargetConnections();
					for (DiagramConnectionModel conn : targetConnections) {
						if (srcConnections.contains(conn)) {
							moveAllBendpoints(conn, deltaX, deltaY);
						}
					}
				}
			}
		}
	}
	
	private void moveAllBendpoints(DiagramConnectionModel conn, int deltaX, int deltaY) {
		DiagramConnectionPart part = conn.getModelPart();
		List<Point> pts = part.getBendpoints();
		for (int i = 0; i < pts.size(); i++) {
			Point current = pts.get(i);
			part.updateBendpoint(i, current.getX() + deltaX, current.getY() + deltaY);
		}
	}
}
