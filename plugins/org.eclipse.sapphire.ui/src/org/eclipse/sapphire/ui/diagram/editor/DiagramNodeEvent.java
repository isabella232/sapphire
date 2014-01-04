/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924] Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.editor;


/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public class DiagramNodeEvent extends DiagramPartEvent 
{
    public static enum NodeEventType
    {
        NodeAdd,
        NodeDelete,
        NodeMove,
        NodeAboutToBeDeleted,
        NodeAdded
    }
	    
    private NodeEventType nodeEventType;

    public DiagramNodeEvent(final DiagramNodePart part)
    {
       super(part);
    }

    public DiagramNodeEvent(final DiagramNodePart part, final ShapePart shapePart)
    {
       super(part);
    }

	public NodeEventType getNodeEventType() {
		return nodeEventType;
	}

	public void setNodeEventType(NodeEventType nodeEventType) {
		this.nodeEventType = nodeEventType;
	}

}
