/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram;

import java.util.List;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ui.Point;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.diagram.def.IDiagramConnectionDef;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public abstract class DiagramConnectionPart extends SapphirePart 
{
	public abstract boolean removable();
	
	public abstract void remove();
	
	public abstract String getId();
	
	public abstract String getConnectionTypeId();
	
	public abstract IDiagramConnectionDef getConnectionDef();
	
	public abstract DiagramConnectionPart reconnect(DiagramNodePart newSrc, DiagramNodePart newTarget);
	
	public abstract boolean canEditLabel();
	
	public abstract List<Point> getBendpoints();
	
	public abstract void removeAllBendpoints();
	
	public abstract void resetBendpoints(List<Point> bendpoints);
	
	public abstract void addBendpoint(int index, int x, int y);
	
	public abstract void updateBendpoint(int index, int x, int y);
	
	public abstract void removeBendpoint(int index);
	
	public abstract String getLabel();
	
	public abstract void setLabel(String newValue);
	
	public abstract Point getLabelPosition();
	
	public abstract void setLabelPosition(Point newPos);
	
	public abstract Element getEndpoint1();
	
	public abstract Element getEndpoint2();
}
