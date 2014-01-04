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

package org.eclipse.sapphire.ui.swt.gef.policies;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.handles.BendpointMoveHandle;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramConnectionEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramBendpointMoveHandle extends BendpointMoveHandle {
	
	private final static org.eclipse.sapphire.Color FOREGROUND_COLOR_PRIMARY = new org.eclipse.sapphire.Color(0xcc,0x6a,0x01);
	private final static org.eclipse.sapphire.Color FOREGROUND_COLOR_SECONDARY = new org.eclipse.sapphire.Color(0xcc,0x6a,0x01);
	private final static org.eclipse.sapphire.Color BACKGROUND_COLOR_PRIMARY = new org.eclipse.sapphire.Color(0xff,0xaa,0x2f);
	private final static org.eclipse.sapphire.Color BACKGROUND_COLOR_SECONDARY = new org.eclipse.sapphire.Color(0xff,0xff,0xff);

	public DiagramBendpointMoveHandle(ConnectionEditPart owner, int index, int locatorIndex) {
		super(owner, index, locatorIndex);
	}
	
	@Override
	protected Color getBorderColor() {
		DiagramConnectionEditPart editPart = (DiagramConnectionEditPart)this.getOwner();
		DiagramResourceCache cache = editPart.getCastedModel().getDiagramModel().getResourceCache();
		return isPrimary() ? cache.getColor(FOREGROUND_COLOR_PRIMARY) : cache.getColor(FOREGROUND_COLOR_SECONDARY);
	}

	@Override
	protected Color getFillColor() {
		DiagramConnectionEditPart editPart = (DiagramConnectionEditPart)this.getOwner();
		DiagramResourceCache cache = editPart.getCastedModel().getDiagramModel().getResourceCache();
		return isPrimary() ? cache.getColor(BACKGROUND_COLOR_PRIMARY) : cache.getColor(BACKGROUND_COLOR_SECONDARY);
	}

	@Override
	public void paintFigure(Graphics g) {
		g.setAntialias(SWT.ON);
		g.setLineWidth(1);

		Rectangle r = getBounds();
		r.shrink(1, 1);
		try {
			g.setForegroundColor(getBorderColor());
			g.setBackgroundColor(getFillColor());
			g.fillOval(r);
			g.drawOval(r);
		} finally {
			// We don't really own rect 'r', so fix it.
			r.expand(1, 1);
		}
	}

}
