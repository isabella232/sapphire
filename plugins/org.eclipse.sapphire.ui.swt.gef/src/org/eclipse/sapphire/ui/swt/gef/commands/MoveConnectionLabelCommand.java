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

package org.eclipse.sapphire.ui.swt.gef.commands;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramConnectionLabelEditPart;
import org.eclipse.sapphire.ui.swt.gef.parts.SapphireMidpointLocator;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class MoveConnectionLabelCommand extends Command {
	
	private DiagramConnectionLabelEditPart editPart;
	private Rectangle rectangle;

	public MoveConnectionLabelCommand(DiagramConnectionLabelEditPart editPart, Rectangle rectangle) {
		this.editPart = editPart;
		this.rectangle = rectangle;
	}

	@Override
	public void execute() {
		int x = rectangle.x;
		int y = rectangle.y;
		Connection connection = (Connection)editPart.getFigure().getParent();
		SapphireMidpointLocator location = new SapphireMidpointLocator(editPart.getConfigurationManager(), connection);
		Point midpoint = location.getMidpoint();
		midpoint = editPart.getConfigurationManager().getDiagramEditor().calculateRealMouseLocation(midpoint);
		org.eclipse.sapphire.ui.Point newPos = new org.eclipse.sapphire.ui.Point(x - midpoint.x, y - midpoint.y);
		editPart.getDiagramConnectionPart().setLabelPosition(newPos);
	}
	
}
