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

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.sapphire.ui.swt.gef.parts.DiagramConnectionEditPart;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramConnectionEndpointEditPolicy extends ConnectionEndpointEditPolicy {

    private static final org.eclipse.sapphire.Color CONNECTION_FOREGROUND = new org.eclipse.sapphire.Color(0xFF, 0xA5, 0x00);
    
    private DiagramResourceCache resourceCache;
    
    public DiagramConnectionEndpointEditPolicy(DiagramResourceCache resourceCache) {
    	this.resourceCache = resourceCache;
    }

    protected void addSelectionHandles() {
		super.addSelectionHandles();
		getConnectionFigure().setForegroundColor(resourceCache.getColor(CONNECTION_FOREGROUND));
	}

	protected PolylineConnection getConnectionFigure() {
		return (PolylineConnection) ((GraphicalEditPart) getHost()).getFigure();
	}

	protected void removeSelectionHandles() {
		super.removeSelectionHandles();
		DiagramConnectionEditPart editPart = (DiagramConnectionEditPart)getHost();
		editPart.updateStyle(getConnectionFigure());
	}

}
